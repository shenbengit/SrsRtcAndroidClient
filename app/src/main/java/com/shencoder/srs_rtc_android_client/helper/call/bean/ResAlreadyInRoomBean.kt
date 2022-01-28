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
data class ResAlreadyInRoomBean(
    /**
     * 已经存在房间内的客户端
     */
    @Json(name = "alreadyInRoomList")
    val alreadyInRoomList: List<ResClientStreamBean>,
    /**
     * 房间号
     */
    @Json(name = "roomId")
    val roomId: String
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ResClientStreamBean(
    /**
     * 客户端信息
     */
    @Json(name = "userInfo")
    val userInfo: ClientInfoBean,
    /**
     * 推流地址
     * 如果为空，则说明客户端已在房间还未推流
     */
    @Json(name = "publishStreamUrl")
    val publishStreamUrl: String?
) : Parcelable