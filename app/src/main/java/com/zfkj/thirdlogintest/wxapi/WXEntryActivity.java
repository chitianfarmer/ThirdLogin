package com.zfkj.thirdlogintest.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import com.example.user.utils.util.ToastUtils;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.zfkj.thirdlogintest.Configs;
import com.zfkj.thirdlogintest.R;
import com.zfkj.thirdlogintest.ThirdLoginUtils;

public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {
    private IWXAPI wxapi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flash_activity);
        //		微信登录API
        wxapi = ThirdLoginUtils.getInstance().getIWXAPI();
		wxapi.registerApp(Configs.WX_APP_ID);
        //如果没回调onResp，八成是这句没有写
        wxapi.handleIntent(getIntent(),this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        wxapi.handleIntent(intent, this);
    }

    // 微信发送消息给app，app接受并处理的回调函数
    @Override
    public void onReq(BaseReq baseReq) {

    }

    // app发送消息给微信，微信返回的消息回调函数,根据不同的返回码来判断操作是否成功
    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH){
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_AUTH_DENIED:
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    ToastUtils.showShort(this,"登录取消");
                    finish();
                    break;
                case BaseResp.ErrCode.ERR_OK:
                    // 获取到code
                    String code = ((SendAuth.Resp) resp).code;
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent(Configs.LOGIN_BY_WECHAT_RESULT).putExtra(Configs.WX_RESULT_CODE,code));
                    finish();
                    break;
            }
        }else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX){

        }

    }
}