package com.zfkj.thirdlogintest.kotlin

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.example.user.utils.request.okhttp.OKHttpUtils
import com.example.user.utils.request.okhttp.Param
import com.example.user.utils.util.ToastUtils
import com.tencent.connect.UserInfo
import com.tencent.connect.common.Constants
import com.tencent.mm.sdk.modelmsg.SendAuth
import com.tencent.mm.sdk.openapi.IWXAPI
import com.tencent.mm.sdk.openapi.WXAPIFactory
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.zfkj.thirdlogintest.Configs
import java.util.ArrayList

/**
 * 项目名称：ThirdLoginTest
 * 类描述：KotlinThirdLoginUtils 描述:kotlin关于QQ微信登录的第三方工具类
 * 创建人：songlijie
 * 创建时间：2018/4/12 17:26
 * 邮箱:814326663@qq.com
 */
object KotlinThirdLoginUtils {
    private val TAG = KotlinThirdLoginUtils::class.java.simpleName
    private var mTencent: Tencent? = null
    private var wxapi: IWXAPI? = null
    private var loginUtil: KotlinThirdLoginUtils? = null
    private var mInfo: UserInfo? = null
    private var mContext: Activity? = null
    private var listener: QQLoginListener? = null
    private var loginWatcher: QQLoginWatcher? = null

    /**
     * 初始化工具类
     * @param cxt
     */
    @Synchronized fun init(cxt: Activity) {
        if (cxt ==null){
           throw RuntimeException("please init first")
        }
        mContext = cxt
        loginUtil = this@KotlinThirdLoginUtils
        //		qq登录API
        mTencent = Tencent.createInstance(KotlinConfigs.QQ_APP_ID, cxt)
        //		微信登录API
        wxapi = WXAPIFactory.createWXAPI(cxt, KotlinConfigs.WX_APP_ID)
        wxapi!!.registerApp(KotlinConfigs.WX_APP_ID)
        loginWatcher = QQLoginWatcher()
    }

    fun getQQTencent(): Tencent? {
        return mTencent
    }

    fun getIWXAPI(): IWXAPI? {
        return wxapi
    }

    /**
     * 发起qq登录
     */
    fun loginByQQ(loginListener: QQLoginListener) {
        this.listener = loginListener
        if (mTencent != null) {
            if (!mTencent!!.isSessionValid) {
                mTencent!!.login(mContext, Configs.SCOPE, loginWatcher)
            } else {
                mTencent!!.logout(mContext)
            }
        }
    }

    fun QQLoginActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginWatcher)
    }

    interface QQLoginListener {
        fun onSuccess(jsonObject: JSONObject, openId1: String)

        fun onError(error: String, msg: Int)

        fun onCancle()
    }

    /**
     * 退出qq登录
     */
    fun logoutByQQ() {
        if (mTencent != null) {
            mTencent!!.logout(mContext)
            Log.e(TAG, "----登出QQ")
            destoryUitls()
        }
    }

    /**
     * 发起微信登录
     */
    fun loginByWX() {
        if (!wxapi!!.isWXAppInstalled) {
            //提醒用户没有按照微信
            ToastUtils.showShort(mContext, "您还没安装微信,请下载微信后再试!")
            return
        }
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "qianlima"
        wxapi!!.sendReq(req)
    }

    /**
     * 获取微信授权的Token

     * @param code
     * *
     * @param loginListener
     */
    fun getWxToken(code: String, loginListener: WxLoginListener) {
        val params = ArrayList<Param>()
        params.add(Param("appid", Configs.WX_APP_ID))
        params.add(Param("secret", Configs.WX_APP_SECRET))
        params.add(Param("code", code))
        params.add(Param("grant_type", "authorization_code"))
        OKHttpUtils(mContext).post(params, Configs.WX_APP_OAUTH2_URL, object : OKHttpUtils.HttpCallBack {
            override fun onResponse(jsonObject: JSONObject?) {
                if (jsonObject != null) {
                    Log.e(TAG, "----获取微信TOKEN" + jsonObject.toJSONString())
                    val unionid = jsonObject.getString("unionid")
                    val openid = jsonObject.getString("openid")
                    val refresh_token = jsonObject.getString("refresh_token")
                    val expires_in = jsonObject.getString("expires_in")
                    val access_token = jsonObject.getString("access_token")
                    showWxInfo(access_token, openid, loginListener)
                }
            }

            override fun onFailure(errorMsg: String) {

            }
        })
    }

    /**
     * 获取到Token后请求服务器获取个人信息

     * @param access_token
     * *
     * @param openid
     * *
     * @param listener
     */
    private fun showWxInfo(access_token: String, openid: String, listener: WxLoginListener) {
        val paramsList = ArrayList<Param>()
        paramsList.add(Param("access_token", access_token))
        paramsList.add(Param("openid", openid))
        OKHttpUtils(mContext).post(paramsList, Configs.WX_APP_USERINFO_URL, object : OKHttpUtils.HttpCallBack {
            override fun onResponse(response: JSONObject?) {
                if (response != null) {
                    try {
                        val nickname = response.getString("nickname")
                        val sex = response.getString("sex")
                        val province = response.getString("province")
                        val city = response.getString("city")
                        val country = response.getString("country")
                        val headimgurl = response.getString("headimgurl")
                        listener.onSuccess(openid, headimgurl, nickname, Configs.wxType)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        listener.onError(e.message.toString())
                    }

                }
            }

            override fun onFailure(errorMsg: String) {
                listener.onError(errorMsg)
            }
        })
    }

    /**
     * 微信登录的回调
     */
    interface WxLoginListener {
        fun onSuccess(openId: String, headimgurl: String, nickname: String, thirdType: String)

        fun onError(msg: String)
    }

    class QQLoginWatcher : IUiListener {

        override fun onComplete(o: Any?) {
            if (o == null) {
                return
            }
            try {
                val jsonObject = JSONObject.parseObject(o.toString())
                val token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN)
                val expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN)
                val openId = jsonObject.getString(Constants.PARAM_OPEN_ID)
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                        && !TextUtils.isEmpty(openId)) {
                    mTencent!!.setAccessToken(token, expires)
                    mTencent!!.openId = openId
                }
                mInfo = UserInfo(mContext, mTencent!!.qqToken)
                mInfo!!.getUserInfo(object : IUiListener {
                    override fun onComplete(o: Any?) {
                        if (o == null) {
                            return
                        }
                        mContext!!.runOnUiThread(Runnable {
                            val jsonObject = JSONObject.parseObject(o.toString())
                            val openId1 = mTencent!!.openId
                            if (listener != null) {
                                listener!!.onSuccess(jsonObject, openId1)
                            }
                        })
                    }

                    override fun onError(uiError: UiError) {
                        if (listener != null) {
                            listener!!.onError(uiError.errorMessage, uiError.errorCode)
                        }
                    }

                    override fun onCancel() {
                        if (listener != null) {
                            listener!!.onCancle()
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                if (listener != null) {
                    listener!!.onError(e.message.toString(), 0)
                }
            }

        }

        override fun onError(uiError: UiError) {
            if (listener != null) {
                listener!!.onError(uiError.errorMessage, uiError.errorCode)
            }
        }

        override fun onCancel() {
            if (listener != null) {
                listener!!.onCancle()
            }
        }
    }

    /**
     * 销毁工具类
     */
    fun destoryUitls() {
        if (loginUtil != null) {
            loginUtil = null
        }
    }
}