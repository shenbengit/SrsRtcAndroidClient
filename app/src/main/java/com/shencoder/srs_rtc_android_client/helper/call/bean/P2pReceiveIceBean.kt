package com.shencoder.srs_rtc_android_client.helper.call.bean

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.webrtc.IceCandidate


/**
 *
 * @author Shenben
 * @date 2023/11/10 16:38
 * @description
 * @since
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class P2pReceiveIceBean(
    @Json(name = "userInfo")
    val userInfo: ClientInfoBean,
    @Json(name = "roomId")
    val roomId: String,
    @Json(name = "ice")
    val ice: Ice
) : Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Ice(
        @Json(name = "sdpMid")
        val sdpMid: String,
        @Json(name = "sdpMLineIndex")
        val sdpMLineIndex: Int,
        @Json(name = "sdp")
        val sdp: String
    ) : Parcelable {

        fun toIceCandidate(): IceCandidate {
            return IceCandidate(sdpMid, sdpMLineIndex, sdp)
        }
    }
}