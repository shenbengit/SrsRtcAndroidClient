package com.shencoder.srs_rtc_android_client.http.bean.base

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
open class ApiResponse<T>(
    @Json(name = "code")
    val code: Int,
    @Json(name = "data")
    val data: T?,
    @Json(name = "msg")
    val msg: String
) : BaseResponse<T>() {

    override fun isSuccess(): Boolean {
        return code == Constant.RESULT_OK
    }

    override fun getResponseCode(): Int {
        return code
    }

    override fun getResponseMsg(): String {
        return msg
    }

    override fun getResponseData(): T? {
        return data
    }
}