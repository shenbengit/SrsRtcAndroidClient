package com.shencoder.srs_rtc_android_client.helper.call.bean

import android.os.Parcelable
import com.shencoder.srs_rtc_android_client.constant.CallType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 *
 * @author  ShenBen
 * @date    2022/1/27 10:13
 * @email   714081644@qq.com
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class P2pRequestCallBean(
    /**
     * 邀请人信息
     */
    @Json(name = "inviteInfo")
    val inviteInfo: ClientInfoBean,
    /**
     * 邀请人同时邀请除自己之外其他人信息
     */
    @Json(name = "callList")
    val callList: List<ClientInfoBean>?,
    /**
     * 通话类型
     */
    @Json(name = "callType")
    val type: Int,
    /**
     * 房间号
     */
    @Json(name = "roomId")
    val roomId: String
) : Parcelable {
    val callType: CallType
        get() = CallType.createByType(type)
}