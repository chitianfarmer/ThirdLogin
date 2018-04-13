package com.zfkj.thirdlogintest.wxapi

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import com.tencent.mm.sdk.constants.ConstantsAPI
import com.tencent.mm.sdk.modelbase.BaseReq
import com.tencent.mm.sdk.modelbase.BaseResp
import com.tencent.mm.sdk.modelmsg.SendAuth
import com.tencent.mm.sdk.openapi.IWXAPI
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler
import com.zfkj.thirdlogintest.Configs
import com.zfkj.thirdlogintest.R
import com.zfkj.thirdlogintest.kotlin.KotlinConfigs
import com.zfkj.thirdlogintest.kotlin.KotlinThirdLoginUtils
import com.zfkj.thirdlogintest.kotlin.KotlinToastUtils

class WXEntryActivity : AppCompatActivity() , IWXAPIEventHandler {
    private var wxapi: IWXAPI ?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flash_activity)
        wxapi = KotlinThirdLoginUtils.getIWXAPI()
        wxapi!!.registerApp(KotlinConfigs.WX_APP_ID)
        //如果没回调onResp，八成是这句没有写
        wxapi!!.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        wxapi!!.handleIntent(intent, this)
    }
    override fun onResp(resp: BaseResp?) {
        if (resp!!.type == ConstantsAPI.COMMAND_SENDAUTH) {
            when (resp!!.errCode) {
                BaseResp.ErrCode.ERR_AUTH_DENIED, BaseResp.ErrCode.ERR_USER_CANCEL -> {
                    KotlinToastUtils.showShort(this@WXEntryActivity,"登录取消")
                    finish()
                }
                BaseResp.ErrCode.ERR_OK -> {
                    // 获取到code
                    val code = (resp as SendAuth.Resp).code
                    LocalBroadcastManager.getInstance(baseContext).sendBroadcast(Intent(Configs.LOGIN_BY_WECHAT_RESULT).putExtra(Configs.WX_RESULT_CODE, code))
                    finish()
                }
            }
        } else if (resp!!.type == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {

        }

    }

    override fun onReq(req: BaseReq?) {
    }

}
