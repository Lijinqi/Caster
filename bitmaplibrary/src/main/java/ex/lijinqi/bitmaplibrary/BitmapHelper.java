package ex.lijinqi.bitmaplibrary;

import android.graphics.Bitmap;

/**
 * @author ljq
 * @date 2019/4/12
 * @description
 */
public class BitmapHelper {
    static {
        System.loadLibrary("bitmap_handle");
    }

    public static native void init();

    public static native byte[] handle(Bitmap bitmap);

    public static native void release();
}
