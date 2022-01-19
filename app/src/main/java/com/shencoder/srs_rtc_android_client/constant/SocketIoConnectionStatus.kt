package com.shencoder.srs_rtc_android_client.constant

/**
 * socket.io 连接状态
 *
 * @author  ShenBen
 * @date    2022/1/19 10:49
 * @email   714081644@qq.com
 */
enum class SocketIoConnectionStatus(val status: String) {
    /**
     * 连接中
     */
    CONNECTING("CONNECTING"),

    /**
     * 已连接
     */
    CONNECTED("CONNECTED"),

    /**
     * 未连接
     */
    DISCONNECTED("DISCONNECTED")
}
