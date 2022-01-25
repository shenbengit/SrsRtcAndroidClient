package com.shencoder.srs_rtc_android_client.webrtc.constant

/**
 *
 * @author  ShenBen
 * @date    2022/1/24 10:24
 * @email   714081644@qq.com
 */
enum class IntoRoomType {
    /**
     * 主动进入房间（主叫）
     */
    ACTIVELY_INTO_ROOM,

    /**
     * 被邀请进入房间（被叫）
     */
    BE_INVITED_INTO_ROOM,

    /**
     * 直接进入房间（聊天室）
     */
    DIRECTLY_INTO_ROOM
}