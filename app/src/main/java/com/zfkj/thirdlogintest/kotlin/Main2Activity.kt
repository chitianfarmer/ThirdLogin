package com.zfkj.thirdlogintest.kotlin

import android.app.AlertDialog
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSONObject
import com.example.user.utils.media.GildeTools.GlideUtils
import com.tencent.connect.common.Constants
import com.zfkj.thirdlogintest.Configs
import com.zfkj.thirdlogintest.MainActivity
import com.zfkj.thirdlogintest.R

class Main2Activity : AppCompatActivity(), View.OnClickListener, KotlinThirdLoginUtils.QQLoginListener {

    private var btn_login_qq: Button? = null
    private var btn_login_wx: Button? = null
    private var btn_login_kotlin: Button? = null
    private var iv_avatar: ImageView? = null
    private var tv_show: TextView? = null
    private var brocast: WxLoginBrocast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getData()
        initView()
        initData()
        setListener()
    }

    fun getData() {
        KotlinThirdLoginUtils.init(this)
        brocast = WxLoginBrocast()
        val filter = IntentFilter()
        filter.addAction(Configs.LOGIN_BY_WECHAT_RESULT)
        LocalBroadcastManager.getInstance(baseContext).registerReceiver(brocast, filter)
    }

    fun initView() {
        btn_login_qq = findViewById(R.id.btn_login_qq) as Button
        btn_login_wx = findViewById(R.id.btn_login_wx) as Button
        btn_login_kotlin = findViewById(R.id.btn_login_kotlin) as Button
        iv_avatar = findViewById(R.id.iv_avatar) as ImageView
        tv_show = findViewById(R.id.tv_show) as TextView

    }

    fun initData() {
        btn_login_kotlin!!.text = "JAVA代码展示QQ/微信登录"
    }

    fun setListener() {
        btn_login_qq!!.setOnClickListener(this)
        btn_login_wx!!.setOnClickListener(this)
        btn_login_kotlin!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_login_qq) {
            showAleart("QQ", KotlinConfigs.qqType)
        } else if (v?.id == R.id.btn_login_wx) {
            showAleart("微信", KotlinConfigs.wxType)
        } else if (v?.id == R.id.btn_login_kotlin) {
            startActivity(Intent(this@Main2Activity, MainActivity::class.java))
            finish()
        }
    }

    fun showAleart(msg: String, type: String) {
        val dialog = AlertDialog.Builder(this@Main2Activity)
        dialog.setTitle("登录提示")
        dialog.setMessage("ThirdLogin想要打开" + msg)
        dialog.setNegativeButton("取消", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        dialog.setPositiveButton("打开", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
            when (type) {
                KotlinConfigs.qqType -> KotlinThirdLoginUtils.loginByQQ(this@Main2Activity)
                KotlinConfigs.wxType -> KotlinThirdLoginUtils.loginByWX()
            }
        })
        dialog.show()
    }

    override fun onSuccess(jsonObject: JSONObject, openId1: String) {
        if (jsonObject == null && jsonObject.size == 0) {
            KotlinThirdLoginUtils.logoutByQQ()
            return
        }
        if (jsonObject.containsKey("nickname") && jsonObject.containsKey("figureurl_qq_2")) {
            val nickname = jsonObject.getString("nickname")
            val avatar = jsonObject.getString("figureurl_qq_2")
            showMessage(openId1, avatar, nickname, KotlinConfigs.qqType)
        }
    }

    override fun onError(error: String, msg: Int) {
        KotlinToastUtils.showShort(this@Main2Activity,"登录出错:" + error)
    }

    override fun onCancle() {
        KotlinToastUtils.showShort(this@Main2Activity,"登录取消")
    }

    private fun showMessage(openId: String, avatar: String, nickname: String, thirdType: String) {
        logoutByQQ(thirdType)
        if (tv_show != null) {
            tv_show!!.text = "--openId:$openId\n--nickname:$nickname"
        }
        GlideUtils.downLoadCircleImage(this, avatar, iv_avatar, R.mipmap.ic_launcher, R.mipmap.ic_launcher)
    }

    private fun logoutByQQ(thirdType: String) {
        if (KotlinConfigs.qqType == thirdType) {
            KotlinThirdLoginUtils.logoutByQQ()
        } else {
            KotlinThirdLoginUtils.destoryUitls()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    /**
     * 监听微信回调
     */
    private inner class WxLoginBrocast : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (KotlinConfigs.LOGIN_BY_WECHAT_RESULT == intent.action) {
                val resultCode = intent.getStringExtra(Configs.WX_RESULT_CODE)
                KotlinThirdLoginUtils.getWxToken(resultCode, object : KotlinThirdLoginUtils.WxLoginListener {
                    override fun onSuccess(openId: String, headimgurl: String, nickname: String, thirdType: String) {
                        showMessage(openId, headimgurl, nickname, Configs.wxType)
                    }

                    override fun onError(msg: String) {
                        KotlinToastUtils.showShort(this@Main2Activity,msg)
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (brocast != null) {
            LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(brocast)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Constants.ACTIVITY_OK){
            when(requestCode){
                Constants.REQUEST_LOGIN
                    ,Constants.REQUEST_APPBAR ->
                    if (data!=null){
                        KotlinThirdLoginUtils.QQLoginActivityResult(requestCode, resultCode, data)
                    }else{
                        KotlinToastUtils.showShort(this@Main2Activity,"登录取消")
                    }
            }
        }else{
            KotlinToastUtils.showShort(this@Main2Activity,"登录取消")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
