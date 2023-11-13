package com.shencoder.srs_rtc_android_client.constant

import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean

/**
 *
 * @author  ShenBen
 * @date    2022/1/18 14:41
 * @email   714081644@qq.com
 */
object Constant {

    const val TAG = "SrsRtcAndroidClient"

    /**
     * 页面适配基准
     */
    const val DEFAULT_SIZE_IN_DP = 360f

    /**
     * 请求成功code
     */
    const val RESULT_OK = 200

    /**
     * 用户类型：客户端
     * 目前仅支持客户端
     * Only client types are supported.
     */
    const val USER_TYPE_CLIENT = "0"

    /**
     * 用户类型：管理员
     * 当前未支持
     * Not currently supported.
     */
    const val USER_TYPE_ADMINISTRATOR = "1"
}

/**
 * 信令服务相关
 */
object SIGNAL {
    /**
     * 信令服务地址
     * ip或域名
     */
    const val SERVER_ADDRESS = "192.168.2.2"

    /**
     * api请求http端口
     */
    const val API_HTTP_PORT = 9898

    /**
     * api请求https端口
     */
    const val API_HTTPS_PORT = 9899

    /**
     * socket.io http端口
     */
    const val SOCKET_IO_IP_HTTP_PORT = 9998

    /**
     * socket.io https端口
     */
    const val SOCKET_IO_IP_HTTPS_PORT = 9999


    const val BASE_API_HTTP_URL = "http://${SERVER_ADDRESS}:${API_HTTP_PORT}"
    const val BASE_API_HTTPS_URL = "https://${SERVER_ADDRESS}:${API_HTTPS_PORT}"

    const val BASE_SOCKET_IO_WS_URL = "ws://${SERVER_ADDRESS}:${SOCKET_IO_IP_HTTP_PORT}"
    const val BASE_SOCKET_IO_WSS_URL = "wss://${SERVER_ADDRESS}:${SOCKET_IO_IP_HTTPS_PORT}"

    const val SOCKET_IO_CLIENT_NAMESPACE = "/srs_rtc/signal/client"
    const val SOCKET_IO_ADMINISTRATOR_NAMESPACE = "/srs_rtc/signal/administrator"

    const val SOCKET_IO_WS_CLIENT_URL = "${BASE_SOCKET_IO_WS_URL}${SOCKET_IO_CLIENT_NAMESPACE}"
    const val SOCKET_IO_WSS_CLIENT_URL = "${BASE_SOCKET_IO_WSS_URL}${SOCKET_IO_CLIENT_NAMESPACE}"

    const val SOCKET_IO_WS_ADMINISTRATOR_URL =
        "${BASE_SOCKET_IO_WS_URL}${SOCKET_IO_ADMINISTRATOR_NAMESPACE}"
    const val SOCKET_IO_WSS_ADMINISTRATOR_URL =
        "${BASE_SOCKET_IO_WSS_URL}${SOCKET_IO_ADMINISTRATOR_NAMESPACE}"

}

/**
 * SRS服务相关
 */
object SRS {
    /**
     * SRS服务地址
     * ip或域名
     */
    const val SERVER_ADDRESS = "192.168.2.2"

    /**
     * SRS服务api请求http端口
     */
    const val API_HTTP_PORT = 1985

    /**
     * SRS服务api请求https端口
     */
    const val API_HTTPS_PORT = 1990

    /**
     * 拉起请求path
     */
    const val REQUEST_PLAY_PATH = "/rtc/v1/play/"

    /**
     * 推流请求path
     */
    const val REQUEST_PUBLISH_PATH = "/rtc/v1/publish/"

    const val BASE_HTTP_URL = "http://${SERVER_ADDRESS}:${API_HTTP_PORT}"
    const val BASE_HTTPS_URL = "https://${SERVER_ADDRESS}:${API_HTTPS_PORT}"

    const val HTTP_REQUEST_PLAY_URL = "${BASE_HTTP_URL}${REQUEST_PLAY_PATH}"
    const val HTTPS_REQUEST_PLAY_URL = "${BASE_HTTPS_URL}${REQUEST_PLAY_PATH}"

    const val HTTP_REQUEST_PUBLISH_URL = "${BASE_HTTP_URL}${REQUEST_PUBLISH_PATH}"
    const val HTTPS_REQUEST_PUBLISH_URL = "${BASE_HTTPS_URL}${REQUEST_PUBLISH_PATH}"

    /**
     * 生成推流WebRTC Url
     * @param bean 用户信息
     */
    @JvmStatic
    fun generatePublishWebRTCUrl(bean: UserInfoBean): String {
        return buildString {
            append("webrtc://")
            append(SERVER_ADDRESS)
            append("/srs_rtc/android/${bean.userId}/${bean.userType}/${System.currentTimeMillis()}")
        }
    }
}
