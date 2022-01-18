package com.shencoder.srs_rtc_android_client.helper.call

import com.elvishew.xlog.XLog
import com.shencoder.srs_rtc_android_client.constant.ClientNotifyCmd
import com.shencoder.srs_rtc_android_client.constant.NotifyCmd
import com.shencoder.srs_rtc_android_client.constant.SIGNAL
import com.shencoder.srs_rtc_android_client.util.ignoreCertificate
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient

/**
 * 用于通话的socket.io客户端
 * 这是一个单例
 *
 * @author  ShenBen
 * @date    2022/01/18 21:30
 * @email   714081644@qq.com
 */
class CallSocketIoClient private constructor() {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .ignoreCertificate()
        .build()

    private object SingleHolder {
        val INSTANCE = CallSocketIoClient()
    }

    companion object {
        private const val TAG = "CallSocketIoClient"

        @JvmStatic
        fun getInstance() = SingleHolder.INSTANCE
    }

    private lateinit var socket: Socket

    /**
     * 开始连接
     * @param userId userId
     */
    fun connect(userId: String) {
        val options = IO.Options.builder()
            .build()
        options.webSocketFactory = okHttpClient
        options.callFactory = okHttpClient
        val url = "${SIGNAL.SOCKET_IO_WSS_CLIENT_URL}?userId=${userId}"
        XLog.i("${TAG}->connect url: $url")
        socket = IO.socket(url, options).apply {
            on(Socket.EVENT_CONNECT) {
                XLog.i("${TAG}->EVENT_CONNECT")
            }
            on(Socket.EVENT_DISCONNECT) {
                XLog.i("${TAG}->EVENT_DISCONNECT")
            }
            on(Socket.EVENT_CONNECT_ERROR) {
                XLog.i("${TAG}->EVENT_CONNECT_ERROR")
            }
            on(NotifyCmd.NOTIFY_FORCED_OFFLINE) {

            }
            on(ClientNotifyCmd.NOTIFY_REQUEST_CALL) {

            }
            on(ClientNotifyCmd.NOTIFY_INVITE_SOMEONE_JOIN_ROOM) {

            }
            on(ClientNotifyCmd.NOTIFY_INVITE_SOME_PEOPLE_JOIN_ROOM) {

            }
            on(ClientNotifyCmd.NOTIFY_REJECT_CALL) {

            }
            on(ClientNotifyCmd.NOTIFY_ACCEPT_CALL) {

            }
            on(ClientNotifyCmd.NOTIFY_JOIN_CHAT_ROOM) {

            }
            on(ClientNotifyCmd.NOTIFY_LEAVE_CHAT_ROOM) {

            }
            on(ClientNotifyCmd.NOTIFY_PLAY_STREAM) {

            }
            on(ClientNotifyCmd.NOTIFY_HANG_UP) {

            }
            on(ClientNotifyCmd.NOTIFY_OFFLINE_DURING_CALL) {

            }
        }
        socket.connect()
    }

    fun disconnect() {
        if (this::socket.isInitialized) {
            socket.disconnect()
        }
    }
}