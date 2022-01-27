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
data class OfflineDuringCallBean(
    /**
     * 拒接人信息
     */
    @Json(name = "userInfo")
    val userInfo: ClientInfoBean,
    /**
     * 是否需要结束通话
     */
    @Json(name = "callEnded")
    val callEnded: Boolean,
    /**
     * 断开连接原因
     */
    @Json(name = "reason")
    val reason: String?,
    /**
     * 房间号
     */
    @Json(name = "roomId")
    val roomId: String
) : Parcelable