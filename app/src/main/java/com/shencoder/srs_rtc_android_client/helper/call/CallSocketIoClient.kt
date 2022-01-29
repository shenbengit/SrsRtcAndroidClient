package com.shencoder.srs_rtc_android_client.helper.call

import android.os.Handler
import android.os.Looper
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.util.MoshiUtil
import com.shencoder.srs_rtc_android_client.constant.SIGNAL
import com.shencoder.srs_rtc_android_client.helper.call.bean.*
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

    private val signalEventCallbackList: MutableList<SignalEventCallback> =
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
                post {
                    connectionStatusCallbackList.forEach { it.connected() }
                }
            }
            on(Socket.EVENT_DISCONNECT) {
                post {
                    connectionStatusCallbackList.forEach { it.disconnected() }
                }
            }
            on(Socket.EVENT_CONNECT_ERROR) {
                post {
                    connectionStatusCallbackList.forEach { it.connectError() }
                }
            }
            //自定义事件
            on(NotifyCmd.NOTIFY_FORCED_OFFLINE) {
                post {
                    signalEventCallbackList.forEach { it.forcedOffline() }
                }
            }
            on(ClientNotifyCmd.NOTIFY_REQUEST_CALL) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyRequestCall(json)
            }
            on(ClientNotifyCmd.NOTIFY_INVITE_SOMEONE_JOIN_ROOM) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyInviteSomeoneJoinRoom(json)
            }
            on(ClientNotifyCmd.NOTIFY_INVITE_SOME_PEOPLE_JOIN_ROOM) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyInviteSomePeopleJoinRoom(json)
            }
            on(ClientNotifyCmd.NOTIFY_REJECT_CALL) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyRejectCall(json)
            }
            on(ClientNotifyCmd.NOTIFY_ACCEPT_CALL) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyAcceptCall(json)
            }
            on(ClientNotifyCmd.NOTIFY_JOIN_CHAT_ROOM) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyJoinChatRoom(json)
            }
            on(ClientNotifyCmd.NOTIFY_LEAVE_CHAT_ROOM) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyLeaveChatRoom(json)
            }
            on(ClientNotifyCmd.NOTIFY_PLAY_STREAM) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyPlayStream(json)
            }
            on(ClientNotifyCmd.NOTIFY_HANG_UP) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyHangUp(json)
            }
            on(ClientNotifyCmd.NOTIFY_OFFLINE_DURING_CALL) {
                val json = analysisResponseToJson(it)
                if (json.isNullOrBlank()) {
                    return@on
                }
                notifyOfflineDuringCall(json)
            }
        }
        socket.connect()
    }

    /**
     * 添加连接状态监听
     */
    fun addConnectionStatusCallback(callback: SocketIoConnectionStatusCallback) {
        connectionStatusCallbackList.add(callback)
    }

    /**
     * 移除连接状态监听
     */
    fun removeConnectionStatusCallback(callback: SocketIoConnectionStatusCallback) {
        connectionStatusCallbackList.remove(callback)
    }

    /**
     * 添加事件监听
     */
    fun addSignalEventCallback(callback: SignalEventCallback) {
        signalEventCallbackList.add(callback)
    }

    /**
     * 移除事件监听
     */
    fun removeSignalEventCallback(callback: SignalEventCallback) {
        signalEventCallbackList.remove(callback)
    }

    /**
     * 邀请某人进行通话
     */
    fun reqInviteSomeone(
        userId: String,
        success: ((ResInviteeInfoBean) -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //{"userId" : "123"}
        val jsonObject = JSONObject()
        jsonObject.put("userId", userId)
        socket.emit(ClientReqCmd.REQ_INVITE_SOMEONE, arrayOf(jsonObject)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    ResInviteeInfoBean::class.java
                )
            val bean: BaseResponseBean<ResInviteeInfoBean>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke(data!!)
                    } else {
                        failure?.invoke(code, msg)
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
        success: ((ResInviteSomePeopleBean) -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //{"userList":[{userId:"123"}]}
        val jsonArray = JSONArray(userIds.map {
            JSONObject().apply {
                put("userId", it)
            }
        })
        val objects = JSONObject()
        objects.put("userList", jsonArray)

        socket.emit(ClientReqCmd.REQ_INVITE_SOME_PEOPLE, arrayOf(objects)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    ResInviteSomePeopleBean::class.java
                )
            val bean: BaseResponseBean<ResInviteSomePeopleBean>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke(data!!)
                    } else {
                        failure?.invoke(code, msg)
                    }
                }
            }
        }
    }

    /**
     * 邀请某人进入邀请人房间
     * @param userId 被邀请人userId
     * @param roomId 当前邀请人的房间号
     */
    fun reqInviteSomeoneIntoRoom(
        userId: String,
        roomId: String,
        success: ((ResInviteeInfoBean) -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //{"userId":"123", "roomId":"123"}
        val jsonObject = JSONObject()
        jsonObject.put("userId", userId)
        jsonObject.put("roomId", roomId)
        socket.emit(ClientReqCmd.REQ_INVITE_SOMEONE_JOIN_ROOM, arrayOf(jsonObject)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    ResInviteeInfoBean::class.java
                )
            val bean: BaseResponseBean<ResInviteeInfoBean>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke(data!!)
                    } else {
                        failure?.invoke(code, msg)
                    }
                }
            }
        }
    }

    /**
     * 邀请一些人进入邀请人房间
     * @param userIds 邀请人列表
     * @param roomId 当前邀请人的房间号
     */
    fun reqInviteSomePeopleIntoRoom(
        userIds: List<String>,
        roomId: String,
        success: ((ResInviteSomePeopleBean) -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //{"userList":[{userId:"123"}], "roomId":"123"}
        val jsonArray = JSONArray(userIds.map {
            JSONObject().apply {
                put("userId", it)
            }
        })
        val objects = JSONObject()
        objects.put("userList", jsonArray)
        objects.put("roomId", roomId)

        socket.emit(ClientReqCmd.REQ_INVITE_SOME_PEOPLE_JOIN_ROOM, arrayOf(objects)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    ResInviteSomePeopleBean::class.java
                )
            val bean: BaseResponseBean<ResInviteSomePeopleBean>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke(data!!)
                    } else {
                        failure?.invoke(code, msg)
                    }
                }
            }
        }
    }

    /**
     * 发送拒接命令
     */
    fun reqRejectCall(
        roomId: String,
        success: (() -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //roomId="123456"
        socket.emit(ClientReqCmd.REQ_REJECT_CALL, arrayOf(roomId)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    Any::class.java
                )
            val bean: BaseResponseBean<Any>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke()
                    } else {
                        failure?.invoke(code, msg)
                    }
                }
            }
        }
    }

    /**
     * 发送接受命令
     * @param roomId 接受进入的房间号
     * @param success 房间中已存在的流信息
     * @param failure 失败
     */
    fun reqAcceptCall(
        roomId: String,
        success: ((ResAlreadyInRoomBean) -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //roomId="123456"
        socket.emit(ClientReqCmd.REQ_ACCEPT_CALL, arrayOf(roomId)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    ResAlreadyInRoomBean::class.java
                )
            val bean: BaseResponseBean<ResAlreadyInRoomBean>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke(data!!)
                    } else {
                        failure?.invoke(code, msg)
                    }
                }
            }
        }
    }

    /**
     * 发送加入聊天室请求
     */
    fun reqJoinChatRoom(
        roomId: String,
        success: ((ResAlreadyInRoomBean) -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //roomId="123456"
        socket.emit(ClientReqCmd.REQ_JOIN_CHAT_ROOM, arrayOf(roomId)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    ResAlreadyInRoomBean::class.java
                )
            val bean: BaseResponseBean<ResAlreadyInRoomBean>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke(data!!)
                    } else {
                        failure?.invoke(code, msg)
                    }
                }
            }
        }
    }

    /**
     * 发送离开聊天室请求
     */
    fun reqLeaveChatRoom(
        roomId: String,
        success: (() -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //roomId="123456"
        socket.emit(ClientReqCmd.REQ_LEAVE_CHAT_ROOM, arrayOf(roomId)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    Any::class.java
                )
            val bean: BaseResponseBean<Any>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke()
                    } else {
                        failure?.invoke(code, msg)
                    }
                }
            }
        }
    }

    /**
     * 发送推流命令
     * @param roomId
     * @param publishStreamUrl webrtc://ip:port/xxx
     */
    fun reqPublishStream(
        roomId: String,
        publishStreamUrl: String,
        success: (() -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //{"roomId":"123", "publishStreamUrl":"webrtc://192.168.1.1:1990/live/livestream"}
        val jsonObject = JSONObject()
        jsonObject.put("roomId", roomId)
        jsonObject.put("publishStreamUrl", publishStreamUrl)
        socket.emit(ClientReqCmd.REQ_PUBLISH_STREAM, arrayOf(jsonObject)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    Any::class.java
                )
            val bean: BaseResponseBean<Any>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke()
                    } else {
                        failure?.invoke(code, msg)
                    }
                }
            }
        }
    }

    /**
     * 发送挂断命令
     */
    fun reqHangUp(
        roomId: String,
        success: (() -> Unit)? = null,
        failure: ((code: Int, reason: String) -> Unit)? = null
    ) {
        //roomId="123456"
        socket.emit(ClientReqCmd.REQ_HANG_UP, arrayOf(roomId)) {
            val json = analysisResponseToJson(it)
            if (json.isNullOrBlank()) {
                return@emit
            }
            val type =
                Types.newParameterizedType(
                    BaseResponseBean::class.java,
                    Any::class.java
                )
            val bean: BaseResponseBean<Any>? = MoshiUtil.fromJson(json, type)
            post {
                bean?.run {
                    if (isSuccess()) {
                        success?.invoke()
                    } else {
                        failure?.invoke(code, msg)
                    }
                }
            }
        }
    }

    /**
     * 用于重置状态
     */
    fun reqResetStatus() {
        socket.emit(ClientReqCmd.REQ_RESET_STATUS)
    }


    /**
     * 断开连接
     */
    fun disconnect() {
        if (this::socket.isInitialized) {
            socket.disconnect()
        }
    }

    /**
     * 请求通话通知
     */
    private fun notifyRequestCall(json: String) {
        val bean = MoshiUtil.fromJson(json, RequestCallBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.requestCall(it)
                }
            }
        }
    }

    /**
     * 通知邀请某人进入房间
     */
    private fun notifyInviteSomeoneJoinRoom(json: String) {
        val bean = MoshiUtil.fromJson(json, InviteSomeoneBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.inviteSomeoneIntoRoom(it)
                }
            }
        }
    }

    /**
     * 通知邀请某些人进入房间
     */
    private fun notifyInviteSomePeopleJoinRoom(json: String) {
        val bean = MoshiUtil.fromJson(json, InviteSomePeopleBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.inviteSomePeopleIntoRoom(it)
                }
            }
        }
    }

    /**
     * 通知有人拒接通话
     */
    private fun notifyRejectCall(json: String) {
        val bean = MoshiUtil.fromJson(json, RejectCallBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.rejectCall(it)
                }
            }
        }
    }

    /**
     * 通知有人接受通话
     */
    private fun notifyAcceptCall(json: String) {
        val bean = MoshiUtil.fromJson(json, AcceptCallBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.acceptCall(it)
                }
            }
        }
    }

    /**
     * 通知有人进入聊天室
     */
    private fun notifyJoinChatRoom(json: String) {
        val bean = MoshiUtil.fromJson(json, JoinChatRoomBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.joinChatRoom(it)
                }
            }
        }
    }

    /**
     * 通知有人离开聊天室
     */
    private fun notifyLeaveChatRoom(json: String) {
        val bean = MoshiUtil.fromJson(json, LeaveChatRoomBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.leaveChatRoom(it)
                }
            }
        }
    }

    /**
     * 通知推流了
     */
    private fun notifyPlayStream(json: String) {
        val bean = MoshiUtil.fromJson(json, PlayStreamBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.playSteam(it)
                }
            }
        }
    }

    /**
     * 通知有人挂断
     */
    private fun notifyHangUp(json: String) {
        val bean = MoshiUtil.fromJson(json, HangUpBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.hangUp(it)
                }
            }
        }
    }

    /**
     * 通知有人在通话中离线了
     */
    private fun notifyOfflineDuringCall(json: String) {
        val bean = MoshiUtil.fromJson(json, OfflineDuringCallBean::class.java)
        bean?.let {
            post {
                signalEventCallbackList.forEach { callback ->
                    callback.offlineDuringCall(it)
                }
            }
        }
    }


    private fun analysisResponseToJson(args: Array<Any>?): String? {
        if (args.isNullOrEmpty()) {
            return null
        }
        return (args[0] as JSONObject).toString()
    }

    private fun post(runnable: Runnable) {
        mHandler.post(runnable)
    }
}