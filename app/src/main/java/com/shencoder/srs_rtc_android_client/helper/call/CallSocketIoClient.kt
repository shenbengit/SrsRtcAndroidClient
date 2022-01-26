package com.shencoder.srs_rtc_android_client.helper.call

import android.os.Handler
import android.os.Looper
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.util.MoshiUtil
import com.shencoder.srs_rtc_android_client.constant.SIGNAL
import com.shencoder.srs_rtc_android_client.helper.call.bean.BaseResponseBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.ClientInfoBean
import com.shencoder.srs_rtc_android_client.helper.call.bean.ResInviteSomePeopleBean
import com.shencoder.srs_rtc_android_client.util.ignoreCertificate
import com.squareup.moshi.Types
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
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
            //自定义事件
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

    /**
     * 邀请某人进行通话
     */
    fun reqInviteSomeone(
        userId: String,
        success: (ClientInfoBean) -> Unit = {},
        failure: (code: Int, reason: String) -> Unit = { _, _ -> }
    ) {
        //{"userId" : "123"}
        val jsonObject = JSONObject()
        jsonObject.put("userId", userId)
        socket.emit(ClientReqCmd.REQ_INVITE_SOMEONE, arrayOf(jsonObject)) {
            if (it != null && it.isNotEmpty()) {
                val json = it[0] as JSONObject

                val newParameterizedType =
                    Types.newParameterizedType(
                        BaseResponseBean::class.java,
                        ClientInfoBean::class.java
                    )
                val bean: BaseResponseBean<ClientInfoBean>? =
                    MoshiUtil.fromJson(json.toString(), newParameterizedType)
                mHandler.post {
                    bean?.run {
                        if (isSuccess()) {
                            success.invoke(data!!)
                        } else {
                            failure.invoke(code, msg)
                        }
                    }
                }
            }
        }
    }

    /**
     * 邀请一些人
     */
    fun reqInviteSomePeople(
        userIds: List<String>,
        success: (ResInviteSomePeopleBean) -> Unit = {},
        failure: (code: Int, reason: String) -> Unit = { _, _ -> }
    ) {
        //{userList:[{userId:"123"}]}
        val jsonArray = JSONArray(userIds.map {
            JSONObject().apply {
                put("userId", it)
            }
        })
        val objects = JSONObject()
        objects.put("userList", jsonArray)

        socket.emit(ClientReqCmd.REQ_INVITE_SOME_PEOPLE, arrayOf(objects)) {
            if (it != null && it.isNotEmpty()) {
                val jsonObject = it[0] as JSONObject

                val newParameterizedType =
                    Types.newParameterizedType(
                        BaseResponseBean::class.java,
                        ResInviteSomePeopleBean::class.java
                    )
                val bean: BaseResponseBean<ResInviteSomePeopleBean>? =
                    MoshiUtil.fromJson(jsonObject.toString(), newParameterizedType)
                mHandler.post {
                    bean?.run {
                        if (isSuccess()) {
                            success.invoke(data!!)
                        } else {
                            failure.invoke(code, msg)
                        }
                    }
                }
            }
        }
    }

    fun disconnect() {
        if (this::socket.isInitialized) {
            socket.disconnect()
        }
    }
}