package com.shencoder.srs_rtc_android_client.webrtc.bean

/**
 *
 * @author  ShenBen
 * @date    2022/1/24 11:24
 * @email   714081644@qq.com
 */
data class WebRTCStreamInfoBean(
    val userId: String,
    val userType: String,
    /**
     * 用户名
     */
    val username: String? = "",
    /**
     * 头像信息
     */
    val avatar: Any? = null,
    /**
     * WebRTC推拉流地址
     */
    var webrtcUrl: String = ""
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WebRTCStreamInfoBean

        if (userId != other.userId) return false
        if (userType != other.userType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + userType.hashCode()
        return result
    }
}