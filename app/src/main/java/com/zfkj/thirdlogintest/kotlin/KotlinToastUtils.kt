package com.zfkj.thirdlogintest.kotlin

import android.content.Context
import android.widget.Toast

/**
 * 项目名称：ThirdLoginTest
 * 类描述：KotlinToastUtils 描述:
 * 创建人：songlijie
 * 创建时间：2018/4/13 9:28
 * 邮箱:814326663@qq.com
 */
object KotlinToastUtils {
    private var toast : Toast?=null
    fun showShort(mContext : Context,msg : Any){
        if (mContext!! ==null){
            return
        }
        if (msg is String){
            showShort(mContext,msg.toString())
        }else if (msg is Int){
            showShort(mContext,msg.toInt())
        }
    }
    fun showShort(mContext : Context,msg : String){
        if (mContext!! ==null){
            return
        }
        if (toast==null){
            toast = Toast.makeText(mContext!!,msg,Toast.LENGTH_SHORT)
        }else{
            toast!!.setText(msg)
        }
        toast!!.show()
    }
    fun showShort(mContext : Context,msg : Int){
        if (mContext!! ==null){
            return
        }
        if (toast ==null){
            toast = Toast.makeText(mContext!!,msg,Toast.LENGTH_SHORT)
        }else{
            toast!!.setText(msg)
        }
        toast!!.show()
    }
    fun showLong(mContext : Context,msg : Any){
        if (mContext!! ==null){
            return
        }
        if (msg is String){
            showLong(mContext,msg.toString())
        }else if (msg is Int){
            showLong(mContext,msg.toInt())
        }
    }
    fun showLong(mContext : Context,msg : String){
        if (mContext!! ==null){
            return
        }
        if (toast==null){
            toast = Toast.makeText(mContext!!,msg,Toast.LENGTH_LONG)
        }else{
            toast!!.setText(msg)
        }
        toast!!.show()
    }
    fun showLong(mContext : Context,msg : Int){
        if (mContext!! ==null){
            return
        }
        if (toast ==null){
            toast = Toast.makeText(mContext!!,msg,Toast.LENGTH_LONG)
        }else{
            toast!!.setText(msg)
        }
        toast!!.show()
    }
}