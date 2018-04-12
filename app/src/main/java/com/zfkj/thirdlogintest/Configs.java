package com.zfkj.thirdlogintest;

/**
 * 项目名称：ThirdLoginTest
 * 类描述：Configs 描述:
 * 创建人：songlijie
 * 创建时间：2018/4/12 11:39
 * 邮箱:814326663@qq.com
 */
public class Configs {
    //关于第三方登录
    // APP_ID 替换为你的应用从官方网站申请到的合法appId
    public static final String QQ_APP_ID = "1106760611";
    //申请的开发appid
    public static final String WX_APP_ID = "";
    public static final String WX_APP_SECRET = "";
    //关于微信的信息授权地址
    public static final String WX_APP_OAUTH2_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    //关于微信的获取用户地址
    public static final String WX_APP_USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo";
    public static final String SCOPE = "all";// QQ 微博申请的权限
    public static final String qqType = "qq";
    public static final String wxType = "wx";
    //    微信登录的Action
    public static final String LOGIN_BY_WECHAT_RESULT = "LOGIN_BY_WECHAT_RESULT";
    //获取微信返回的CODE
    public static final String WX_RESULT_CODE = "WX_RESULT_CODE";

}
