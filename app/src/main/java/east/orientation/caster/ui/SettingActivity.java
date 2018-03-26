package east.orientation.caster.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stroke.Stroke;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;
import east.orientation.caster.R;
import east.orientation.caster.local.Common;
import east.orientation.caster.util.SharePreferenceUtil;
import east.orientation.caster.util.ToastUtil;

import static com.xuhao.android.libsocket.sdk.OkSocket.open;

/**
 * Created by ljq on 2018/3/8.
 */

public class SettingActivity extends AppCompatActivity {
    private CircleImageView mCivHead;// 头像
    private TextView mTextName;//姓名
    private TextView mTextSize;//分辨率
    private TextView mTextBitrate;// 比特率
    private TextView mTextFps;// fps
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // 初始化
        init();
    }

    /**
     * 初始化
     *
     * 设置默认配置
     */
    private void init() {
        mCivHead = findViewById(R.id.iv_head_icon);
        mTextName = findViewById(R.id.tv_name);
        mTextSize = findViewById(R.id.tv_size_selected);
        mTextBitrate = findViewById(R.id.tv_bitrate_selected);
        mTextFps = findViewById(R.id.tv_fps_selected);
        // 设置默认头像
        mCivHead.setImageResource(R.drawable.ic_launcher_background);
        mCivHead.setDisableCircularTransformation(false);

        // 设置姓名
        mTextName.setText(SharePreferenceUtil.get(getApplicationContext(),Common.KEY_NAME,"姓名"));
        // 设置分辨率
        mTextSize.setText(getResources().getStringArray(R.array.screen_size)[SharePreferenceUtil.get(getApplicationContext(),Common.KEY_SIZE,0)]);
        // 设置比特率
        mTextBitrate.setText(getResources().getStringArray(R.array.bitrate)[SharePreferenceUtil.get(getApplicationContext(),Common.KEY_BITRATE,0)]);
        // 设置fps
        mTextFps.setText(getResources().getStringArray(R.array.fps)[SharePreferenceUtil.get(getApplicationContext(),Common.KEY_FPS,0)]);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.ll_user:
                View input_content = LayoutInflater.from(this).inflate(R.layout.layout_setting_input,null);
                final EditText input = input_content.findViewById(R.id.input);
                new AlertDialog.Builder(this)
                        .setTitle("设置姓名")
                        .setView(input_content)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = input.getText().toString();
                                if (TextUtils.isEmpty(name)){
                                    ToastUtil.show(getApplicationContext(),"用户名不能为空！");
                                }else{
                                    mTextName.setText(name);
                                    SharePreferenceUtil.put(getApplicationContext(),Common.KEY_NAME,name);
                                }
                            }
                        })
                        .show();
                break;
            case R.id.ll_size:
                new AlertDialog.Builder(this)
                        .setTitle("选择分辨率")
                        .setItems(R.array.screen_size,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String size = getResources().getStringArray(R.array.screen_size)[which];
                                        if (!TextUtils.isEmpty(size)){
                                            mTextSize.setText(size);
                                            SharePreferenceUtil.put(getApplicationContext(),Common.KEY_SIZE,which);
                                            ToastUtil.show(getApplicationContext(),"重新启动录屏生效！");
                                        }
                                    }
                                }).show();
                break;
            case R.id.ll_bitrate:
                new AlertDialog.Builder(this)
                        .setTitle("选择比特率")
                        .setItems(R.array.bitrate,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String bitrate = getResources().getStringArray(R.array.bitrate)[which];
                                        if (!TextUtils.isEmpty(bitrate)){
                                            mTextBitrate.setText(bitrate);
                                            SharePreferenceUtil.put(getApplicationContext(),Common.KEY_BITRATE,which);
                                            ToastUtil.show(getApplicationContext(),"重新启动录屏生效！");
                                        }
                                    }
                                }).show();
                break;
            case R.id.ll_fps:
                new AlertDialog.Builder(this)
                        .setTitle("选择帧率")
                        .setItems(R.array.fps,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String fps = getResources().getStringArray(R.array.fps)[which];
                                        if (!TextUtils.isEmpty(fps)){
                                            mTextFps.setText(fps);
                                            SharePreferenceUtil.put(getApplicationContext(),Common.KEY_FPS,which);
                                            ToastUtil.show(getApplicationContext(),"重新启动录屏生效！");
                                        }
                                    }
                                }).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
