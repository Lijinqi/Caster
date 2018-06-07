package east.orientation.caster.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.stroke.Stroke;
import com.stroke.common.CommonUtil;
import com.stroke.view.InkCanvas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import east.orientation.caster.R;
import east.orientation.caster.local.Common;
import east.orientation.caster.util.ToastUtil;

/**
 * Created by ljq on 2018/3/7.
 *
 * 画板
 */

public class WriteActivity extends AppCompatActivity {
    private static String TAG = "InkCanvas";
    /**************************************************************************
     * 菜单按钮
     **************************************************************************/
    private FloatingActionMenu mMenu;// 菜单
    private FloatingActionButton mBtnPenStyle;// 笔型
    private FloatingActionButton mBtnPenColor;// 笔色
    private FloatingActionButton mBtnRubber;// 橡皮
    private FloatingActionButton mBtnClean;// 清除

    private FloatingActionButton mBtnSavePic;// 存图
    private InkCanvas inkCanvas;
    private Bitmap mBitmap;
    /**
     * 渲染模式数组
     */
//    private String[] mPenRenderArray;
//
//    /**
//     * 笔型数组
//     */
//    private String[] mPenStyleArray;
//
//    /**
//     * 颜色名称数组
//     */
//    private String[] mColorNameArray;

    /**
     * 颜色数组
     */
    private TypedArray mColorArray;

    /**
     * 橡皮差模式是否打开
     */
    private boolean mErasingMode = false;

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_pen_style:// 设置笔型
                    setStrokeStyle();
                    break;
                case R.id.fab_pen_color:// 设置笔色
                    setColor();
                    break;
                case R.id.fab_rubber:// 设置橡皮
                    setEraser();
                    break;
                case R.id.fab_clean:// 清屏
                    clear();
                    break;
                case R.id.fab_save_pic:// 存图
                    save();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        inkCanvas = findViewById(R.id.inkCanvas);
        mMenu = findViewById(R.id.menu);
        //
        mBtnPenStyle = findViewById(R.id.fab_pen_style);
        mBtnPenColor = findViewById(R.id.fab_pen_color);
        mBtnRubber = findViewById(R.id.fab_rubber);
        mBtnClean = findViewById(R.id.fab_clean);
        mBtnSavePic = findViewById(R.id.fab_save_pic);
        //
        mBtnPenStyle.setOnClickListener(mClickListener);
        mBtnPenColor.setOnClickListener(mClickListener);
        mBtnRubber.setOnClickListener(mClickListener);
        mBtnClean.setOnClickListener(mClickListener);
        mBtnSavePic.setOnClickListener(mClickListener);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        // 初始化画布分辨率
        CommonUtil.initSize(this);
        // 设置context
        CommonUtil.setContext(this);
        // 初始化，很重要
        inkCanvas.init();
        // 加载之前保存的内容
        inkCanvas.load();
        // 初始化数组
        initArray();
        // 初始化菜单
        initMenu();
    }

    private void initMenu() {
        mMenu.hideMenuButton(false);
        mMenu.showMenuButton(true);
        createCustomAnimation();//
    }

    /**
     * 初始化数组
     */
    private void initArray() {
        Resources resource = getResources();
//        mPenStyleArray = resource.getStringArray(R.array.pen_type);
//        mColorNameArray = resource.getStringArray(R.array.pen_color_name);
        mColorArray = resource.obtainTypedArray(R.array.pen_color);
//        mPenRenderArray = resource.getStringArray(R.array.pen_render);
    }

    /**
     * 渲染模式
     */
    private void setRenderStyle() {
        new AlertDialog.Builder(this)
                .setTitle("选择渲染模式")
                .setItems(R.array.pen_render,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                inkCanvas.setRenderStyle(Stroke.RenderStyle.values()[which]);

                            }
                        }).show();
    }

    /**
     * 笔型
     */
    private void setStrokeStyle() {
        new AlertDialog.Builder(this)
                .setTitle("选择笔型")
                .setItems(R.array.pen_type,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Stroke.StrokeStyle style = Stroke.StrokeStyle.values()[which];
                                inkCanvas.setStrokeStyle(style);
                            }
                        }).show();
    }

    /**
     * 颜色
     */
    private void setColor() {
        new AlertDialog.Builder(this)
                .setTitle("选择颜色")
                .setItems(R.array.pen_color_name,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                int color = mColorArray.getColor(which, 0);
                                inkCanvas.setColor(color);

                            }
                        }).show();
    }

    /**
     * 设置橡皮擦模式
     */
    private void setEraser() {
        mErasingMode = !mErasingMode;
        if(mErasingMode){
            mBtnRubber.setImageResource(R.mipmap.ic_close);
            mBtnRubber.setLabelText(getString(R.string.in_rubber_mode));
        }else{
            mBtnRubber.setImageResource(R.mipmap.rubber);
            mBtnRubber.setLabelText(getString(R.string.btn_eraser));
        }
        inkCanvas.setErasing(mErasingMode);
    }

    /**
     * 清屏
     */
    private void clear() {
        new AlertDialog.Builder(this)
                .setTitle("确认清屏？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inkCanvas.clear();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * save
     */
    private void save() {
        new AlertDialog.Builder(this)
                .setTitle("确认存图？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveViewPic(inkCanvas);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 保存视图画面
     *
     * @param v 被保存视图
     */
    private void saveViewPic(View v){
        v.setDrawingCacheEnabled(true);
        mBitmap = v.getDrawingCache();
        mBitmap = mBitmap.createBitmap(mBitmap);
        v.setDrawingCacheEnabled(false);
        if(mBitmap != null){
            // save
            addJpgToGallery(mBitmap);
        }else{
            ToastUtil.showToast("失败！");
        }
    }

    /**
     * 找到pictures文件夹
     * @param albumName
     * @return
     */
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    /**
     * 将bitmap存为jpg
     *
     * @param bitmap
     * @param photo
     * @throws IOException
     */
    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    /**
     * 加入相册
     *
     * @param bitmap
     * @return
     */
    public boolean addJpgToGallery(Bitmap bitmap) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir(Common.SAVE_DIR_NAME), String.format("Caster_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(bitmap, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 扫描
     *
     * @param photo
     */
    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    /**
     * 出现动画
     */
    private void createCustomAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(mMenu.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(mMenu.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(mMenu.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(mMenu.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mMenu.getMenuIconView().setImageResource(mMenu.isOpened()
                        ? R.mipmap.ic_close : R.mipmap.ic_star);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        mMenu.setIconToggleAnimatorSet(set);
    }
}
