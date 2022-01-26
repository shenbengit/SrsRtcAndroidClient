package com.shencoder.srs_rtc_android_client.helper.call.bean

import com.shencoder.mvvmkit.http.bean.BaseResponse
import com.shencoder.srs_rtc_android_client.constant.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 *
 * @author  ShenBen
 * @date    2021/04/14 11:27
 * @email   714081644@qq.com
 */
@JsonClass(generateAdapter = true)
open class BaseResponseBean<T>(
    @Json(name = "code")
    val code: Int,
    @Json(name = "data")
    val data: T?,
    @Json(name = "msg")
    val msg: String
) {

    fun isSuccess(): Boolean {
        return code == Constant.RESULT_OK
    }

    fun getResponseCode(): Int {
        return code
    }

    fun getResponseMsg(): String {
        return msg
    }

    fun getResponseData(): T? {
        return data
    }

    override fun toString(): String {
        return "BaseResponseBean(code=$code, data=$data, msg='$msg')"
    }


}