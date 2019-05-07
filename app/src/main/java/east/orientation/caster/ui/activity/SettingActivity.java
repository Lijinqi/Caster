package east.orientation.caster.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import east.orientation.caster.R;
import east.orientation.caster.evevtbus.ModeMessage;
import east.orientation.caster.local.Common;
import east.orientation.caster.util.RxShellTool;
import east.orientation.caster.util.SharePreferenceUtil;
import east.orientation.caster.util.ToastUtil;


/**
 * Created by ljq on 2018/3/8.
 * <p>
 * 设置
 */

public class SettingActivity extends AppCompatActivity {
    private CircleImageView mCivHead;// 头像
    private TextView mTextName;//姓名
    private TextView mTextSize;//分辨率
    private TextView mTextBitrate;// 比特率
    private TextView mTextFps;// fps
    private TextView mTextMode;//mode

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // 初始化
        init();
    }

    /**
     * 初始化
     * <p>
     * 设置默认配置
     */
    private void init() {
        mCivHead = findViewById(R.id.iv_head_icon);
        mTextName = findViewById(R.id.tv_name);
        mTextSize = findViewById(R.id.tv_size_selected);
        mTextBitrate = findViewById(R.id.tv_bitrate_selected);
        mTextFps = findViewById(R.id.tv_fps_selected);
        mTextMode = findViewById(R.id.tv_mode_selected);
        // 设置默认头像
        mCivHead.setImageResource(R.drawable.ic_launcher_background);
        mCivHead.setDisableCircularTransformation(false);

        // 设置姓名
        mTextName.setText(SharePreferenceUtil.get(getApplicationContext(), Common.KEY_NAME, "姓名"));
        // 设置分辨率
        mTextSize.setText(getResources().getStringArray(R.array.screen_size)[SharePreferenceUtil.get(getApplicationContext(), Common.KEY_SIZE, 0)]);
        // 设置比特率
        mTextBitrate.setText(getResources().getStringArray(R.array.bitrate)[SharePreferenceUtil.get(getApplicationContext(), Common.KEY_BITRATE, 0)]);
        // 设置fps
        mTextFps.setText(getResources().getStringArray(R.array.fps)[SharePreferenceUtil.get(getApplicationContext(), Common.KEY_FPS, 0)]);
        // 设置模式
        mTextMode.setText(getResources().getStringArray(R.array.mode)[SharePreferenceUtil.get(getApplicationContext(),Common.KEY_CAST_MODE,0)]);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_user:
                View input_content = LayoutInflater.from(this).inflate(R.layout.layout_setting_input, null);
                final EditText input = input_content.findViewById(R.id.input);
                new AlertDialog.Builder(this)
                        .setTitle("设置姓名")
                        .setView(input_content)
                        .setPositiveButton("确认", (dialogInterface, which) -> {
                            String name = input.getText().toString();
                            if (TextUtils.isEmpty(name)) {
                                ToastUtil.showToast("用户名不能为空！");
                            } else {
                                mTextName.setText(name);
                                SharePreferenceUtil.put(getApplicationContext(), Common.KEY_NAME, name);
                            }
                        }).show();
                break;
            case R.id.ll_size:
                new AlertDialog.Builder(this)
                        .setTitle("选择分辨率")
                        .setItems(R.array.screen_size, (dialogInterface, which) -> {
                            String size = getResources().getStringArray(R.array.screen_size)[which];
                            if (!TextUtils.isEmpty(size)) {
                                mTextSize.setText(size);
                                SharePreferenceUtil.put(getApplicationContext(), Common.KEY_SIZE, which);
                                ToastUtil.showToast("重新启动录屏生效！");
                            }
                        }).show();
                break;
            case R.id.ll_bitrate:
                new AlertDialog.Builder(this)
                        .setTitle("选择比特率")
                        .setItems(R.array.bitrate, (dialogInterface, which) -> {
                            String bitrate = getResources().getStringArray(R.array.bitrate)[which];
                            if (!TextUtils.isEmpty(bitrate)) {
                                mTextBitrate.setText(bitrate);
                                SharePreferenceUtil.put(getApplicationContext(), Common.KEY_BITRATE, which);
                                ToastUtil.showToast("重新启动录屏生效！");
                            }
                        }).show();
                break;
            case R.id.ll_fps:
                new AlertDialog.Builder(this)
                        .setTitle("选择帧率")
                        .setItems(R.array.fps, (dialogInterface, which) -> {
                            String fps = getResources().getStringArray(R.array.fps)[which];
                            if (!TextUtils.isEmpty(fps)) {
                                mTextFps.setText(fps);
                                SharePreferenceUtil.put(getApplicationContext(), Common.KEY_FPS, which);
                                ToastUtil.showToast("重新启动录屏生效！");
                            }
                        }).show();
                break;
            case R.id.ll_mode:
                new AlertDialog.Builder(this)
                        .setTitle("选择模式")
                        .setItems(R.array.mode, (dialogInterface, which) -> {
                            String mode = getResources().getStringArray(R.array.mode)[which];
                            if (!TextUtils.isEmpty(mode)) {
                                mTextMode.setText(mode);
                                switchMode(which);
                                SharePreferenceUtil.put(getApplicationContext(),Common.KEY_CAST_MODE,which);
                            }
                        }).show();
                break;
        }
    }

    private void switchMode(int mode) {
        switch (mode) {
            case Common.CAST_MODE_WIFI:// wifi
                onlyWifiMode();
                break;
            case Common.CAST_MODE_MIRACAST:// miracast
                onlyMiracastMode();
                break;
            case Common.CAST_MODE_HOTSPOT:// hotspot
                onlyHotspot();
                break;
        }
    }

    private void onlyWifiMode() {
        EventBus.getDefault().post(new ModeMessage(Common.CAST_MODE_WIFI));
    }

    private void onlyMiracastMode() {
        EventBus.getDefault().post(new ModeMessage(Common.CAST_MODE_MIRACAST));
    }

    private void onlyHotspot() {
        EventBus.getDefault().post(new ModeMessage(Common.CAST_MODE_HOTSPOT));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
