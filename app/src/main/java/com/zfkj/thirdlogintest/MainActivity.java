package com.zfkj.thirdlogintest;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.example.user.utils.media.GildeTools.GlideUtils;
import com.example.user.utils.util.ToastUtils;
import com.tencent.connect.common.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btn_login_qq,btn_login_wx;
    private TextView tv_show;
    private ImageView iv_avatar;

    private WxLoginBrocast brocast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getData();
        initView();
        initData();
        setListener();
    }

    private void getData() {
        ThirdLoginUtils.init(this);
        brocast = new WxLoginBrocast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Configs.LOGIN_BY_WECHAT_RESULT);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(brocast, filter);
    }

    private void initView() {
        btn_login_qq = (Button) findViewById(R.id.btn_login_qq);
        btn_login_wx = (Button) findViewById(R.id.btn_login_wx);
        tv_show = (TextView) findViewById(R.id.tv_show);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
    }

    private void initData() {

    }

    private void setListener() {
        btn_login_qq.setOnClickListener(this);
        btn_login_wx.setOnClickListener(this);
    }

    private void logoutByQQ(String thirdType) {
        if (Configs.qqType.equals(thirdType)) {
            ThirdLoginUtils.getInstance().logoutByQQ();
        } else {
            ThirdLoginUtils.getInstance().destoryUitls();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login_qq:
                showAlert("QQ",Configs.qqType);
                break;
            case R.id.btn_login_wx:
                showAlert("微信",Configs.wxType);
                break;
        }
    }
    private void showAlert(String message,final String type){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("登录提示");
        dialog.setMessage("ThirdLogin想要打开"+message);
        dialog.setPositiveButton("打开", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (Configs.qqType.equals(type)) {
                    ThirdLoginUtils.getInstance().loginByQQ(new ThirdLoginUtils.QQLoginListener() {
                        @Override
                        public void onSuccess(JSONObject object, String openId1) {
                            if (object == null && object.size() == 0) {
                                ThirdLoginUtils.getInstance().logoutByQQ();
                                return;
                            }
                            if (object.containsKey("nickname") && object.containsKey("figureurl_qq_2")) {
                                String nickname = object.getString("nickname");
                                String avatar = object.getString("figureurl_qq_2");
                                showMessage(openId1,avatar,nickname,Configs.qqType);
                            }
                        }

                        @Override
                        public void onError(String error, int msg) {

                        }

                        @Override
                        public void onCancle() {

                        }
                    });
                } else if (Configs.wxType.equals(type)) {
                    ThirdLoginUtils.getInstance().loginByWX();
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    /**
     * 监听微信回调
     */
    private class WxLoginBrocast extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            if (Configs.LOGIN_BY_WECHAT_RESULT.equals(intent.getAction())) {
                String resultCode = intent.getStringExtra(Configs.WX_RESULT_CODE);
                ThirdLoginUtils.getInstance().getWxToken(resultCode, new ThirdLoginUtils.WxLoginListener() {
                    @Override
                    public void onSuccess(String openId, String headimgurl, String nickname, String thirdType) {
                        showMessage(openId,headimgurl,nickname,Configs.wxType);
                    }

                    @Override
                    public void onError(String msg) {
                        ToastUtils.showShort(context, msg);
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (brocast != null) {
            LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(brocast);
        }
    }
    private void showMessage(String openId,String avatar,String nickname,String thirdType){
        logoutByQQ(thirdType);
        if (tv_show!=null){
            tv_show.setText("--openId:"+openId+"\n--nickname:"+nickname);
        }
        GlideUtils.downLoadCircleImage(this,avatar,iv_avatar,R.mipmap.ic_launcher,R.mipmap.ic_launcher);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN || requestCode == Constants.REQUEST_APPBAR) {
            ThirdLoginUtils.getInstance().QQLoginActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
