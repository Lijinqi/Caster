package east.orientation.caster.cast.service;

import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import east.orientation.caster.cast.record.CastGenerator;

import static east.orientation.caster.CastApplication.getAppInfo;

/**
 * Created by ljq on 2018/1/9.
 */

public class CastServiceHandler extends Handler {
    private static final String TAG = "CastServiceHandler";

    static final int HANDLER_START_STREAMING = 0;
    static final int HANDLER_STOP_STREAMING = 1;

    private static final int HANDLER_PAUSE_STREAMING = 10;
    private static final int HANDLER_RESUME_STREAMING = 11;
    //private static final int HANDLER_DETECT_ROTATION = 20;

    //private boolean mCurrentOrientation;
    private final CastGenerator mCastGenerator;

    public CastServiceHandler(Looper looper) {
        super(looper);
        mCastGenerator = new CastGenerator();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLER_START_STREAMING:
                if (getAppInfo().isStreamRunning()) break;
                mCastGenerator.start();
                getAppInfo().setStreamRunning(true);
                Log.e(TAG, "set is Running");
                break;
            case HANDLER_PAUSE_STREAMING:
                if (!getAppInfo().isStreamRunning()) break;
                mCastGenerator.stop();
                sendMessageDelayed(obtainMessage(HANDLER_RESUME_STREAMING), 250);
                break;
            case HANDLER_RESUME_STREAMING:
                if (!getAppInfo().isStreamRunning()) break;
                mCastGenerator.start();
                //sendMessageDelayed(obtainMessage(HANDLER_DETECT_ROTATION), 250);
                break;
            case HANDLER_STOP_STREAMING:
                if (!getAppInfo().isStreamRunning()) break;
                //removeMessages(HANDLER_DETECT_ROTATION);
                removeMessages(HANDLER_STOP_STREAMING);
                mCastGenerator.stop();
                final MediaProjection mediaProjection = CastScreenService.getMediaProjection();
                if (mediaProjection != null) mediaProjection.stop();
                getAppInfo().setStreamRunning(false);
                break;
//            case HANDLER_DETECT_ROTATION:
//                if (!getAppInfo().isStreamRunning()) break;
//                final boolean newOrientation = getOrientation();
//                if (mCurrentOrientation != newOrientation) {
//                    mCurrentOrientation = newOrientation;
//                    obtainMessage(HANDLER_PAUSE_STREAMING).sendToTarget();
//                } else {
//                    sendMessageDelayed(obtainMessage(HANDLER_DETECT_ROTATION), 250);
//                }
//                break;
            default:
                Log.e(TAG, "Cannot handle message");
        }
    }

}
