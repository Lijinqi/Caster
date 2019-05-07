package east.orientation.caster.cast.record;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import east.orientation.caster.cast.service.CastScreenService;
import east.orientation.caster.local.Common;
import east.orientation.caster.local.VideoConfig;
import east.orientation.caster.util.SharePreferenceUtil;


import static east.orientation.caster.CastApplication.getAppContext;
import static east.orientation.caster.CastApplication.getAppInfo;

/**
 * Created by ljq on 2018/3/28.
 */

public class ScreenRecorder {
    private static final String TAG = "ScreenRecorder";

    private MediaCodec mVideoCodec;

    private MediaCodec.BufferInfo mVideoBufferInfo;

    private MediaProjection mediaProjection;

    private VirtualDisplay mVirtualDisplay;

    private Surface mInputSurface;

    // h264帧数据 sps 为满足多连接 每帧数据添加 sps pps
    private byte[] sps = null;

    // h264帧数据 pps
    private byte[] pps = null;

    private int mIndexSize;

    private int mIndexBitrate;

    private int mIndexFps;


    public void start() {
        mediaProjection = CastScreenService.getMediaProjection();

        mIndexSize = SharePreferenceUtil.get(getAppContext(), Common.KEY_SIZE, 0);
        mIndexBitrate = SharePreferenceUtil.get(getAppContext(), Common.KEY_BITRATE, 0);
        mIndexFps = SharePreferenceUtil.get(getAppContext(), Common.KEY_FPS, 0);

        if (mediaProjection == null) {
            return;
        }

        mVideoBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, VideoConfig.RESOLUTION_OPTIONS[0][mIndexSize], VideoConfig.RESOLUTION_OPTIONS[1][mIndexSize]);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, VideoConfig.BITRATE_OPTIONS[mIndexBitrate]);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VideoConfig.FPS_OPTIONS[mIndexFps]);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VideoConfig.DEFAULT_I_FRAME_INTERVAL);
        // 当画面静止时,重复最后一帧
        // https://stackoverflow.com/questions/36578660/android-mediaformatkey-repeat-previous-frame-after-setting
        mediaFormat.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, (6000 * 1000) / VideoConfig.FPS_OPTIONS[mIndexFps]);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

//        MediaFormat mediaFormat = new MediaFormat();
//        mediaFormat.setString(MediaFormat.KEY_MIME, "video/avc");
//        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, VideoConfig.BITRATE_OPTIONS[0]);
//        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
//        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
//        // display the very first frame, and recover from bad quality when no new frames
//        mediaFormat.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1_000_000 * 6 / 60);
//        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, 1200);
//        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, 1920);

        try {
            mVideoCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mVideoCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(MediaCodec codec, int inputBufferId) {
                    Log.d(TAG, "onInputBufferAvailable");
                }

                @Override
                public void onOutputBufferAvailable(MediaCodec codec, int outputBufferId, MediaCodec.BufferInfo info) {
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

                        //Log.e(TAG,"录屏 .."+b.length+" == is key ");
                        // 添加数据到队列
                        getAppInfo().getScreenVideoStream().add(b);
                        //Log.i("@@","video add:"+getAppInfo().getScreenVideoStream().size()+ " "+new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS") .format(new Date() ));

                    }
                    if (mVideoCodec != null) {
                        mVideoCodec.releaseOutputBuffer(outputBufferId, false);
                    }
                    if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.e(TAG, "End of Stream");
                        stop();
                    }
                }

                @Override
                public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                    Log.e(TAG, "MediaEncoder onError : " + e);
                    e.printStackTrace();
                }

                @Override
                public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                    Log.e(TAG, "onOutputFormatChanged");
                    // 得到sps pps
                    getSpsPpsByteBuffer(format);
                }
            });

            mVideoCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = mVideoCodec.createInputSurface();
            mVideoCodec.start();
        } catch (IOException e) {
            Log.e(TAG, "Failed to initial mVideoCodec, e: " + e);
            stop();
        }
        DisplayManager displayManager = (DisplayManager) getAppContext().getSystemService(Context.DISPLAY_SERVICE);

        mVirtualDisplay = displayManager.createVirtualDisplay("cast_Recording",VideoConfig.RESOLUTION_OPTIONS[0][mIndexSize],
                VideoConfig.RESOLUTION_OPTIONS[1][mIndexSize], VideoConfig.DEFAULT_SCREEN_DPI,mInputSurface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                        | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE);
//        mVirtualDisplay = mediaProjection.createVirtualDisplay("Recording Display", VideoConfig.RESOLUTION_OPTIONS[0][mIndexSize],
//                VideoConfig.RESOLUTION_OPTIONS[1][mIndexSize], VideoConfig.DEFAULT_SCREEN_DPI, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mInputSurface, null, null);


    }

    /**
     * 获取编码SPS和PPS信息
     *
     * @param newFormat
     */
    private void getSpsPpsByteBuffer(MediaFormat newFormat) {
        sps = newFormat.getByteBuffer("csd-0").array();
        pps = newFormat.getByteBuffer("csd-1").array();
    }

    /**
     * 释放编码器
     */
    synchronized public void stop() {
        if (mVideoCodec != null) {
            mVideoCodec.stop();
            mVideoCodec.release();
            mVideoCodec = null;
        }
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        mVideoBufferInfo = null;
    }
}
