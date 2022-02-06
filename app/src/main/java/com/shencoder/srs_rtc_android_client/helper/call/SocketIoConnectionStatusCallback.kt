package com.shencoder.srs_rtc_android_client.helper.call

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 10:53
 * @email   714081644@qq.com
 */
interface SocketIoConnectionStatusCallback {
    /**
     * 已连接
     */
    fun connected() {}

    /**
     * 未连接
     */
    fun disconnected() {}

    /**
     * 连接错误
     */
    fun connectError() {}
}