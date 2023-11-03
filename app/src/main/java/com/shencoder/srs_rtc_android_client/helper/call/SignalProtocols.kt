package com.shencoder.srs_rtc_android_client.helper.call

/**
 * 信令协议
 * @author  ShenBen
 * @date    2022/01/18 21=17
 * @email   714081644@qq.com
 */
object ReqCmd {

}

object NotifyCmd {
    /**
     * 单点登录：强制下线
     */
    const val NOTIFY_FORCED_OFFLINE = "notify_forced_offline"
}

/**
 * 客户端请求发送的命令
 */
object SfuClientReqCmd {
    /**
     * 邀请一个人，并创建房间 ——> 单聊
     * {"userId":"123"}
     */
    const val REQ_INVITE_SOMEONE = "req_invite_someone"

    /**
     * 邀请一些人，并创建房间 ——> 群聊
     * {"userList":[{userId:"123"}]}
     */
    const val REQ_INVITE_SOME_PEOPLE = "req_invite_some_people"

    /**
     * 邀请一个人进入邀请人房间——> 单聊
     * {"userId":"123", "roomId":"123"}
     */
    const val REQ_INVITE_SOMEONE_JOIN_ROOM = "req_invite_someone_join_room"

    /**
     * 邀请一些人进入邀请人房间 ——> 群聊
     * {"userList":[{userId:"123"}], "roomId":"123"}
     */
    const val REQ_INVITE_SOME_PEOPLE_JOIN_ROOM = "req_invite_some_people_join_room"

    /**
     * 拒接通话
     * roomId="123456"
     */
    const val REQ_REJECT_CALL = "req_reject_call"

    /**
     * 接受通话
     * roomId="123456"
     */
    const val REQ_ACCEPT_CALL = "req_accept_call"

    /**
     * 加入房间->用于聊天室
     * roomId="123456"
     */
    const val REQ_JOIN_CHAT_ROOM = "req_join_chat_room"

    /**
     * 离开房间->用于聊天室
     * roomId="123456"
     */
    const val REQ_LEAVE_CHAT_ROOM = "req_leave_chat_room"

    /**
     * 请求推流
     * {"roomId":"123", "publishStreamUrl":"webrtc://192.168.1.1:1990/live/livestream"}
     */
    const val REQ_PUBLISH_STREAM = "req_publish_stream"

    /**
     * 挂断
     * roomId="123456"
     */
    const val REQ_HANG_UP = "req_hang_up"


    /**
     * 用于重置状态，避免偶发情况下未关闭通话
     */
    const val REQ_RESET_STATUS = "req_reset_status"
}

object SfuClientNotifyCmd {
    /**
     * 通知请求通话
     */
    const val NOTIFY_REQUEST_CALL = "notify_request_call"

    /**
     * 通知邀请某人进入房间
     */
    const val NOTIFY_INVITE_SOMEONE_JOIN_ROOM = "notify_invite_someone_join_room"

    /**
     * 通知邀请某些人进入房间
     */
    const val NOTIFY_INVITE_SOME_PEOPLE_JOIN_ROOM = "notify_invite_some_people_join_room"

    /**
     * 通知拒接通话
     */
    const val NOTIFY_REJECT_CALL = "notify_reject_call"

    /**
     * 通知接受通话
     */
    const val NOTIFY_ACCEPT_CALL = "notify_accept_call"

    /**
     * 通知有人加入房间->用于聊天室
     */
    const val NOTIFY_JOIN_CHAT_ROOM = "notify_join_chat_room"

    /**
     * 通知有人离开房间->用于聊天室
     */
    const val NOTIFY_LEAVE_CHAT_ROOM = "notify_leave_chat_room"

    /**
     * 通知拉流
     */
    const val NOTIFY_PLAY_STREAM = "notify_play_stream"

    /**
     * 通知挂断
     */
    const val NOTIFY_HANG_UP = "notify_hang_up"

    /**
     * 通知房间内，有人通话中掉线
     */
    const val NOTIFY_OFFLINE_DURING_CALL = "notify_offline_during_call"
}

object P2PClientReqCmd {
    const val REQ_P2P_INVITE_SOMEONE = "req_p2p_invite_someone"
    const val REQ_P2P_REJECT_CALL = "req_p2p_reject_call"
    const val REQ_P2P_ACCEPT_CALL = "req_p2p_accept_call"
    const val REQ_P2P_SEND_OFFER = "req_p2p_send_offer"
    const val REQ_P2P_SEND_ANSWER = "req_p2p_send_answer"
    const val REQ_P2P_SEND_ICE = "req_p2p_send_ice"
    const val REQ_P2P_HANG_UP = "req_p2p_hang_up"
    const val REQ_P2P_RESET_STATUS = "req_p2p_reset_status"
}

object P2PClientNotifyCmd {
    const val NOTIFY_P2P_REQUEST_CALL = "notify_p2p_request_call"
    const val NOTIFY_P2P_REJECT_CALL = "notify_p2p_reject_call"
    const val NOTIFY_P2P_ACCEPT_CALL = "notify_p2p_accept_call"
    const val NOTIFY_P2P_RECEIVE_OFFER = "notify_p2p_receive_offer"
    const val NOTIFY_P2P_RECEIVE_ANSWER = "notify_p2p_receive_answer"
    const val NOTIFY_P2P_RECEIVE_ICE = "notify_p2p_receive_ice"
    const val NOTIFY_P2P_HANG_UP = "notify_p2p_hang_up"
    const val NOTIFY_P2P_OFFLINE_DURING_CALL = "notify_p2p_offline_during_call"
}
