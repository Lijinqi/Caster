package east.orientation.caster.scrcpy;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;


import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import east.orientation.caster.scrcpy.wrappers.SurfaceControl;

import static east.orientation.caster.CastApplication.getAppInfo;

public class ScreenEncoder implements Device.RotationListener {

    private static final int DEFAULT_FRAME_RATE = 60; // fps
    private static final int DEFAULT_I_FRAME_INTERVAL = 10; // seconds

    private static final int REPEAT_FRAME_DELAY = 6; // repeat after 6 frames

    private static final int MICROSECONDS_IN_ONE_SECOND = 1_000_000;
    private static final int NO_PTS = -1;

    private final AtomicBoolean rotationChanged = new AtomicBoolean();
    private final ByteBuffer headerBuffer = ByteBuffer.allocate(12);

    private int bitRate;
    private int frameRate;
    private int iFrameInterval;
    private boolean sendFrameMeta;
    private long ptsOrigin;

    private boolean alive;

    private MediaCodec codec;
    private Surface surface;
    private Device mDevice;
    private IBinder display;

    // h264帧数据 sps
    private byte[] sps = null;
    // h264帧数据 pps
    private byte[] pps = null;

    public ScreenEncoder(boolean sendFrameMeta, int bitRate, int frameRate, int iFrameInterval) {
        this.sendFrameMeta = sendFrameMeta;
        this.bitRate = bitRate;
        this.frameRate = frameRate;
        this.iFrameInterval = iFrameInterval;
    }

    public ScreenEncoder(boolean sendFrameMeta, int bitRate) {
        this(sendFrameMeta, bitRate, DEFAULT_FRAME_RATE, DEFAULT_I_FRAME_INTERVAL);
    }

    @Override
    public void onRotationChanged(int rotation) {
        rotationChanged.set(true);
    }

    public boolean consumeRotationChange() {
        return rotationChanged.getAndSet(false);
    }

    public void stop() {
        codec.stop();
        destroyDisplay(display);
        codec.release();
        surface.release();
        mDevice.setRotationListener(null);
    }

    public void streamScreen(Device device) throws IOException {
        MediaFormat format = createFormat(bitRate, frameRate, iFrameInterval);
        device.setRotationListener(this);
        codec = createCodec();
        display = createDisplay();
        Rect contentRect = device.getScreenInfo().getContentRect();
        Rect videoRect = device.getScreenInfo().getVideoSize().toRect();
        setSize(format, videoRect.width(), videoRect.height());
        configure(codec, format);
        surface = codec.createInputSurface();
        setDisplaySurface(display, surface, contentRect, videoRect);
        mDevice = device;
        codec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int outputBufferId, @NonNull MediaCodec.BufferInfo info) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
                if (info.size > 0 && outputBuffer != null) {
                    outputBuffer.position(info.offset);
                    outputBuffer.limit(info.offset + info.size);

                    // 每帧都添加sps,和pps信息  为了每连接一个新的客户端可以解析
                    final byte[] b;
                    b = new byte[info.size + sps.length + pps.length];
                    System.arraycopy(sps, 0, b, 0, sps.length);
                    System.arraycopy(pps, 0, b, sps.length, pps.length);
                    outputBuffer.get(b, sps.length + pps.length, info.size);

//                        byte[] frame = new byte[info.size];
//                        System.arraycopy(b, sps.length + pps.length, frame, 0, info.size);

                    Log.i("SCRCPY","frame length "+b.length);

                    // 添加数据到队列
                    getAppInfo().getScreenVideoStream().add(b);
                }
                if (codec != null) {
                    codec.releaseOutputBuffer(outputBufferId, false);
                }
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.e("SCRCPY", "End of Stream");
                    stop();
                }
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                getSpsPpsByteBuffer(format);
            }
        });
        codec.start();
    }

    private boolean encode(MediaCodec codec) throws IOException {
        boolean eof = false;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        while (!consumeRotationChange() && !eof) {
            int outputBufferId = codec.dequeueOutputBuffer(bufferInfo, -1);
            eof = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
            try {
                if (consumeRotationChange()) {
                    // must restart encoding with new size
                    break;
                }
                if (outputBufferId >= 0) {
                    ByteBuffer codecBuffer = codec.getOutputBuffer(outputBufferId);
                    Log.d("SCRCPY","frame buffer :" +bufferInfo.size);

//                    if (sendFrameMeta) {
//                        writeFrameMeta(fd, bufferInfo, codecBuffer.remaining());
//                    }
//
//                    IO.writeFully(fd, codecBuffer);
                }
            } finally {
                if (outputBufferId >= 0) {
                    codec.releaseOutputBuffer(outputBufferId, false);
                }
            }
        }

        return !eof;
    }

    private void getSpsPpsByteBuffer(MediaFormat newFormat) {
        sps = newFormat.getByteBuffer("csd-0").array();
        pps = newFormat.getByteBuffer("csd-1").array();
    }

    private void writeFrameMeta(FileDescriptor fd, MediaCodec.BufferInfo bufferInfo, int packetSize) throws IOException {
        headerBuffer.clear();

        long pts;
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            pts = NO_PTS; // non-media data packet
        } else {
            if (ptsOrigin == 0) {
                ptsOrigin = bufferInfo.presentationTimeUs;
            }
            pts = bufferInfo.presentationTimeUs - ptsOrigin;
        }

        headerBuffer.putLong(pts);
        headerBuffer.putInt(packetSize);
        headerBuffer.flip();
        IO.writeFully(fd, headerBuffer);
    }

    private static MediaCodec createCodec() throws IOException {
        return MediaCodec.createEncoderByType("video/avc");
    }

    private static MediaFormat createFormat(int bitRate, int frameRate, int iFrameInterval) throws IOException {
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "video/avc");
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
        // display the very first frame, and recover from bad quality when no new frames
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, MICROSECONDS_IN_ONE_SECOND * REPEAT_FRAME_DELAY / frameRate); // µs
        return format;
    }

    private static IBinder createDisplay() {
        return SurfaceControl.createDisplay("scrcpy", true);
    }

    private static void configure(MediaCodec codec, MediaFormat format) {
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    private static void setSize(MediaFormat format, int width, int height) {
        format.setInteger(MediaFormat.KEY_WIDTH, width);
        format.setInteger(MediaFormat.KEY_HEIGHT, height);
    }

    private static void setDisplaySurface(IBinder display, Surface surface, Rect deviceRect, Rect displayRect) {
        SurfaceControl.openTransaction();
        try {
            SurfaceControl.setDisplaySurface(display, surface);
            SurfaceControl.setDisplayProjection(display, 0, deviceRect, displayRect);
            SurfaceControl.setDisplayLayerStack(display, 0);
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    private static void destroyDisplay(IBinder display) {
        SurfaceControl.destroyDisplay(display);
    }
}
