package com.shencoder.srs_rtc_android_client.helper.call

import com.shencoder.srs_rtc_android_client.helper.call.bean.*

/**
 *
 * @author  ShenBen
 * @date    2022/1/27 08:43
 * @email   714081644@qq.com
 */
interface SignalEventCallback {

    /**
     * 强制下线
     * 当前账号在别的设备上登录
     *
     * 这个时候要断开所有连接，包括srs推拉流
     */
    fun forcedOffline() {

    }

    /**
     * 请求通话事件
     */
    fun requestCall(bean: RequestCallBean) {

    }

    /**
     * 通知邀请某人进入房间
     */
    fun inviteSomeoneIntoRoom(bean: InviteSomeoneBean) {

    }

    /**
     * 通知邀请某些人进入房间
     */
    fun inviteSomePeopleIntoRoom(bean: InviteSomePeopleBean) {

    }

    /**
     * 某人拒接事件
     */
    fun rejectCall(bean: RejectCallBean) {

    }

    /**
     * 某人接受通话
     */
    fun acceptCall(bean: AcceptCallBean) {

    }

    /**
     * 加入聊天室
     */
    fun joinChatRoom(bean: JoinChatRoomBean) {

    }

    /**
     * 离开聊天室
     */
    fun leaveChatRoom(bean: LeaveChatRoomBean) {

    }

    /**
     * 通知拉流
     */
    fun playSteam(bean: PlayStreamBean) {

    }

    /**
     * 挂断
     */
    fun hangUp(bean: HangUpBean) {

    }

    /**
     * 通话时，有人离线了，这个方法不适用与聊天室，聊天室会走[leaveChatRoom]
     */
    fun offlineDuringCall(bean: OfflineDuringCallBean) {}

    //<editor-fold desc="P2P">
    fun p2pRequestCall(bean: P2pRequestCallBean) {}
    fun p2pRejectCall(bean: RejectCallBean) {}
    fun p2pAcceptCall(bean: AcceptCallBean) {}
    fun p2pReceiveOffer(bean: P2pReceiveSdpBean) {}
    fun p2pReceiveAnswer(bean: P2pReceiveSdpBean) {}
    fun p2pReceiveIce(bean: P2pReceiveIceBean) {}
    fun p2pHangUp(bean: HangUpBean) {}

    fun p2pOfflineDuringCall(bean: OfflineDuringCallBean) {}
    //</editor-fold>
}