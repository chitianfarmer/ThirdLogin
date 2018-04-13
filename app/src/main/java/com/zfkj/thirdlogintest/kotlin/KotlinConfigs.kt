package com.zfkj.thirdlogintest.kotlin

/**
 * 项目名称：ThirdLoginTest
 * 类描述：KotlinConfigs 描述:
 * 创建人：songlijie
 * 创建时间：2018/4/12 17:10
 * 邮箱:814326663@qq.com
 */
object KotlinConfigs {
    //关于第三方登录
    // APP_ID 替换为你的应用从官方网站申请到的合法appId
    val QQ_APP_ID : String= "1106760611"
    //申请的开发appid
    val WX_APP_ID : String = ""
    val WX_APP_SECRET : String = ""
    //关于微信的信息授权地址
    val WX_APP_OAUTH2_URL : String = "https://api.weixin.qq.com/sns/oauth2/access_token"
    //关于微信的获取用户地址
    val WX_APP_USERINFO_URL : String = "https://api.weixin.qq.com/sns/userinfo"
    val SCOPE : String = "all"// QQ 微博申请的权限
    val qqType : String = "qq"
    val wxType  : String= "wx"
    //    微信登录的Action
    val LOGIN_BY_WECHAT_RESULT : String = "LOGIN_BY_WECHAT_RESULT"
    //获取微信返回的CODE
    val WX_RESULT_CODE  : String= "WX_RESULT_CODE"

}