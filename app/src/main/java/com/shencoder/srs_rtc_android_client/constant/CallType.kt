package com.shencoder.srs_rtc_android_client.constant

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * 通话类型
 *
 * @author Shenben
 * @date 2023/11/2 15:49
 * @description
 * @since
 */
sealed class CallType(val type: Int) : Parcelable {
    /**
     * 音频通话
     *
     * @constructor Create empty Audio
     */
    @Parcelize
    object Audio : CallType(CALL_TYPE_AUDIO)

    /**
     * 视频通话
     *
     * @constructor Create empty Video
     */
    @Parcelize
    object Video : CallType(CALL_TYPE_VIDEO)

    companion object {
        const val CALL_TYPE_AUDIO = 1
        const val CALL_TYPE_VIDEO = 2

        @JvmStatic
        fun createByType(type: Int): CallType {
            return when (type) {
                CALL_TYPE_AUDIO -> Audio
                else -> Video
            }
        }
    }
}
