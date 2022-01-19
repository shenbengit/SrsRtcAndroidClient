package com.shencoder.srs_rtc_android_client.helper.call

import android.os.Handler
import android.os.Looper
import com.elvishew.xlog.XLog
import com.shencoder.srs_rtc_android_client.constant.ClientNotifyCmd
import com.shencoder.srs_rtc_android_client.constant.NotifyCmd
import com.shencoder.srs_rtc_android_client.constant.SIGNAL
import com.shencoder.srs_rtc_android_client.util.ignoreCertificate
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

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
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)//修改Ping的时间间隔
        .build()

    private object SingleHolder {
        val INSTANCE = CallSocketIoClient()
    }

    companion object {
        private const val TAG = "CallSocketIoClient"

        /**
         * 超时时间，单位：秒
         */
        private const val TIMEOUT_SECONDS = 5L

        /**
         * ping的间隔，单位：秒
         */
        private const val PING_INTERVAL_SECONDS = 5L

        @JvmStatic
        fun getInstance() = SingleHolder.INSTANCE
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var socket: Socket

    private val connectionStatusCallbackList: MutableList<SocketIoConnectionStatusCallback> =
        CopyOnWriteArrayList()


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
                mHandler.post {
                    connectionStatusCallbackList.forEach { it.connected() }
                }
            }
            on(Socket.EVENT_DISCONNECT) {
                mHandler.post {
                    connectionStatusCallbackList.forEach { it.disconnected() }
                }
            }
            on(Socket.EVENT_CONNECT_ERROR) {
                mHandler.post {
                    connectionStatusCallbackList.forEach { it.connectError() }
                }
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

    fun addConnectionStatusCallback(callback: SocketIoConnectionStatusCallback) {
        connectionStatusCallbackList.add(callback)
    }

    fun removeConnectionStatusCallback(callback: SocketIoConnectionStatusCallback) {
        connectionStatusCallbackList.remove(callback)
    }

    fun disconnect() {
        if (this::socket.isInitialized) {
            socket.disconnect()
        }
    }
}