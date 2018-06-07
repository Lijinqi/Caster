package east.orientation.caster.extract;

import android.text.TextUtils;
import android.util.Log;

import org.reactivestreams.Subscriber;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ljq on 2018/6/1.
 */

public class ExtractManager extends BaseExtract{

    private static final String FLAG_ZIP = "zip";
    private static final String FLAG_RAR = "rar";

    private volatile static ExtractManager mInstance;
    private BaseExtract mCurrentArchiver;
    private Executor mThreadPool;
    public static ExtractManager getInstance()
    {
        if (mInstance==null)
        {
            synchronized (ExtractManager.class)
            {
                mInstance=new ExtractManager();
            }
        }
        return mInstance;
    }

    private ExtractManager(){
        mThreadPool= Executors.newSingleThreadExecutor();
    }

    @Override
    public void onExtract(String srcPath, String extractPath, String password, IExtractListener listener) {
        mCurrentArchiver=getCorrectExtract(getFileType(srcPath));
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                mCurrentArchiver.onExtract(srcPath,extractPath,password,listener);
            }
        });
    }

    /**
     * 获取文件类型
     *
     * @param filepath
     * @return
     */
    private String getFileType(String filepath)
    {
        String type=null;
        if (TextUtils.isEmpty(filepath))
            return type;
        String[] temp=filepath.split("\\.");
        type=temp[temp.length-1];
        return type;
    }

    private BaseExtract getCorrectExtract(String type)
    {
        switch (type)
        {
            case FLAG_ZIP:
                return new ZipExtract();
            case FLAG_RAR:
                return new RarExtract();
            default:
                return null;
        }
    }
}
