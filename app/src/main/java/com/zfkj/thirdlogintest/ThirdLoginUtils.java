package com.zfkj.thirdlogintest;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSONObject;
import com.example.user.utils.request.okhttp.OKHttpUtils;
import com.example.user.utils.request.okhttp.Param;
import com.example.user.utils.util.ToastUtils;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：thirdlogintest
 * 类描述：ThirdLoginUtils 描述: QQ微信第三方登录工具类
 * 创建人：songlijie
 * 创建时间：2017/11/8 11:41
 * 邮箱:814326663@qq.com
 */
public class ThirdLoginUtils {
    private String TAG = ThirdLoginUtils.class.getSimpleName();

    private static Tencent mTencent;
    private static IWXAPI wxapi;
    private static ThirdLoginUtils loginUtil = null;
    private static UserInfo mInfo;
    private static Activity mContext = null;
    private QQLoginListener listener;
    private QQLoginWatcher loginWatcher;

    private ThirdLoginUtils(Activity context) {
        if (context == null){
            throw new RuntimeException("please init first!");
        }
        //		qq登录API
        mTencent = Tencent.createInstance(Configs.QQ_APP_ID, context);
        //		微信登录API
        wxapi = WXAPIFactory.createWXAPI(context, Configs.WX_APP_ID);
        wxapi.registerApp(Configs.WX_APP_ID);
        loginWatcher = new QQLoginWatcher();
    }

    /**
     * 初始化工具类
     * @param cxt
     */
    public static synchronized void init(Activity cxt) {
        if (loginUtil == null) {
            loginUtil = new ThirdLoginUtils(cxt);
            mContext = cxt;
        }
    }

    /**
     * get instance of ThirdLoginUtils
     *
     * @param
     * @return
     */
    public synchronized static ThirdLoginUtils getInstance() {
        if (loginUtil == null) {
            loginUtil = new ThirdLoginUtils(mContext);
        }
        return loginUtil;
    }

    public Tencent getQQTencent() {
        return mTencent;
    }

    public IWXAPI getIWXAPI() {
        return wxapi;
    }

    /**
     * 发起qq登录
     */
    public void loginByQQ(final QQLoginListener loginListener) {
        this.listener = loginListener;
        if (mTencent != null) {
            if (!mTencent.isSessionValid()) {
                mTencent.login(mContext, Configs.SCOPE, loginWatcher);
            } else {
                mTencent.logout(mContext);
            }
        }
    }

    public void QQLoginActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, loginWatcher);
    }

    public interface QQLoginListener {
        void onSuccess(JSONObject object, String openId1);

        void onError(String error, int msg);

        void onCancle();
    }

    /**
     * 退出qq登录
     */
    public void logoutByQQ() {
        if (mTencent != null) {
            mTencent.logout(mContext);
            Log.e(TAG,"----登出QQ");
            destoryUitls();
        }
    }

    /**
     * 发起微信登录
     */
    public void loginByWX() {
        if (!wxapi.isWXAppInstalled()) {
            //提醒用户没有按照微信
            ToastUtils.showShort(mContext, "您还没安装微信,请下载微信后再试!");
            return;
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "qianlima";
        wxapi.sendReq(req);
    }

    /**
     * 获取微信授权的Token
     *
     * @param code
     * @param loginListener
     */
    public void getWxToken(String code, final WxLoginListener loginListener) {
        List<Param> params = new ArrayList<>();
        params.add(new Param("appid", Configs.WX_APP_ID));
        params.add(new Param("secret", Configs.WX_APP_SECRET));
        params.add(new Param("code", code));
        params.add(new Param("grant_type", "authorization_code"));
        new OKHttpUtils(mContext).post(params, Configs.WX_APP_OAUTH2_URL, new OKHttpUtils.HttpCallBack() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) {
                    Log.e(TAG,"----获取微信TOKEN"+jsonObject.toJSONString());
                    String unionid = jsonObject.getString("unionid");
                    final String openid = jsonObject.getString("openid");
                    String refresh_token = jsonObject.getString("refresh_token");
                    String expires_in = jsonObject.getString("expires_in");
                    String access_token = jsonObject.getString("access_token");
                    showWxInfo(access_token, openid, loginListener);
                }
            }

            @Override
            public void onFailure(String errorMsg) {

            }
        });
    }

    /**
     * 获取到Token后请求服务器获取个人信息
     *
     * @param access_token
     * @param openid
     * @param listener
     */
    private void showWxInfo(String access_token, final String openid, final WxLoginListener listener) {
        List<Param> paramsList = new ArrayList<Param>();
        paramsList.add(new Param("access_token", access_token));
        paramsList.add(new Param("openid", openid));
        new OKHttpUtils(mContext).post(paramsList, Configs.WX_APP_USERINFO_URL, new OKHttpUtils.HttpCallBack() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    try {
                        String nickname = response.getString("nickname");
                        String sex = response.getString("sex");
                        String province = response.getString("province");
                        String city = response.getString("city");
                        String country = response.getString("country");
                        String headimgurl = response.getString("headimgurl");
                        listener.onSuccess(openid, headimgurl, nickname, Configs.wxType);
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onError(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                listener.onError(errorMsg);
            }
        });
    }

    /**
     * 微信登录的回调
     */
    public interface WxLoginListener {
        void onSuccess(String openId, String headimgurl, String nickname, String thirdType);

        void onError(String msg);
    }

    private class QQLoginWatcher implements IUiListener {

        @Override
        public void onComplete(Object o) {
            if (o == null) {
                return;
            }
            try {
                JSONObject object = JSONObject.parseObject(o.toString());
                String token = object.getString(Constants.PARAM_ACCESS_TOKEN);
                String expires = object.getString(Constants.PARAM_EXPIRES_IN);
                String openId = object.getString(Constants.PARAM_OPEN_ID);
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                        && !TextUtils.isEmpty(openId)) {
                    mTencent.setAccessToken(token, expires);
                    mTencent.setOpenId(openId);
                }
                mInfo = new UserInfo(mContext, mTencent.getQQToken());
                mInfo.getUserInfo(new IUiListener() {
                    @Override
                    public void onComplete(final Object o) {
                        if (o == null) {
                            return;
                        }
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject jsonObject = JSONObject.parseObject(o.toString());
                                String openId1 = mTencent.getOpenId();
                                if (listener != null) {
                                    listener.onSuccess(jsonObject, openId1);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(UiError uiError) {
                        if (listener != null) {
                            listener.onError(uiError.errorMessage, uiError.errorCode);
                        }
                    }

                    @Override
                    public void onCancel() {
                        if (listener != null) {
                            listener.onCancle();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

        }
    }

    public void destoryUitls() {
        if (loginUtil != null) {
            loginUtil = null;
        }
    }
}
