package com.shencoder.srs_rtc_android_client.helper.call.bean

import android.os.Parcelable
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
data class JoinChatRoomBean(
    /**
     * 接听人信息
     */
    @Json(name = "userInfo")
    val userInfo: ClientInfoBean,
    /**
     * 房间号
     */
    @Json(name = "roomId")
    val roomId: String
) : Parcelable