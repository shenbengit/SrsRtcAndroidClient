package com.shencoder.srs_rtc_android_client.helper.call.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Json


@Parcelize
@JsonClass(generateAdapter = true)
data class ResInviteSomePeopleBean(
    /**
     * 可通话人员列表
     */
    @Json(name = "callList")
    val callList: List<ClientInfoBean>,
    /**
     * 忙碌人员列表
     */
    @Json(name = "busyList")
    val busyList: List<ClientInfoBean>,
    /**
     * 离线或不存在列表列表
     */
    @Json(name = "offlineOrNotExistsList")
    val offlineOrNotExistsList: List<ClientInfoBean>,
    /**
     * 已经在房间内列表，属于无效邀请
     */
    @Json(name = "alreadyInRoomList")
    val alreadyInRoomList: List<ClientInfoBean>,
    /**
     * 房间号
     */
    @Json(name = "roomId")
    val roomId: String,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResInviteSomePeopleBean

        if (callList != other.callList) return false
        if (busyList != other.busyList) return false
        if (offlineOrNotExistsList != other.offlineOrNotExistsList) return false
        if (alreadyInRoomList != other.alreadyInRoomList) return false
        if (roomId != other.roomId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = callList.hashCode()
        result = 31 * result + busyList.hashCode()
        result = 31 * result + offlineOrNotExistsList.hashCode()
        result = 31 * result + alreadyInRoomList.hashCode()
        result = 31 * result + roomId.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResInviteSomePeopleBean(callList=$callList, busyList=$busyList, offlineOrNotExistsList=$offlineOrNotExistsList, alreadyInRoomList=$alreadyInRoomList, roomId='$roomId')"
    }

}