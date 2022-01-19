package com.shencoder.srs_rtc_android_client.http.bean


import com.shencoder.srs_rtc_android_client.constant.Constant
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterUserBean(
    @Json(name = "password")
    val password: String,
    @Json(name = "userId")
    val userId: String,
    @Json(name = "username")
    val username: String,
    @Json(name = "userType")
    val userType: String = Constant.USER_TYPE_CLIENT
)