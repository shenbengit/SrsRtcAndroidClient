package com.shencoder.srs_rtc_android_client.http.bean


import com.squareup.moshi.Json

data class SrsResponseBean(
    @Json(name = "code")
    val code: Int,
    @Json(name = "sdp")
    val sdp: String?,
    @Json(name = "server")
    val server: String?,
    @Json(name = "sessionid")
    val sessionId: String?
) {

    val isSuccess: Boolean
        get() = code == 0

    override fun toString(): String {
        return "SrsResponseBean(code=$code, sdp=$sdp, server=$server, sessionId=$sessionId)"
    }
}