package com.shencoder.srs_rtc_android_client.webrtc.util

import org.webrtc.MediaConstraints

/**
 *
 * @author  ShenBen
 * @date    2022/1/21 09:34
 * @email   714081644@qq.com
 */
object WebRTCUtil {

    @JvmStatic
    fun createAudioConstraints(): MediaConstraints {
        val constraints = MediaConstraints()
        //回声消除
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googEchoCancellation",
                "true"
            )
        )
        //自动增益
        constraints.mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
        //高音过滤
        constraints.mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        //噪音处理
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googNoiseSuppression",
                "true"
            )
        )
        return constraints
    }

    /**
     * @param isReceive 是否接收
     * @return
     */
    @JvmStatic
    fun offerOrAnswerConstraint(isReceive: Boolean): MediaConstraints {
        val mediaConstraints = MediaConstraints()
        //是否接收音频
        mediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveAudio", isReceive.toString()
            )
        )
        //是否接收视频
        mediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", isReceive.toString()
            )
        )
        mediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("googCpuOveruseDetection", "true")
        )
        return mediaConstraints
    }

    /**
     * 转换Answer Sdp
     *
     * @param offerSdp
     * @param answerSdp
     * @return
     */
    @JvmStatic
    fun convertAnswerSdp(offerSdp: String, answerSdp: String?): String {
        if (answerSdp.isNullOrBlank()) {
            return ""
        }
        val indexOfOfferVideo = offerSdp.indexOf("m=video")
        val indexOfOfferAudio = offerSdp.indexOf("m=audio")
        if (indexOfOfferVideo == -1 || indexOfOfferAudio == -1) {
            return answerSdp
        }
        val indexOfAnswerVideo = answerSdp.indexOf("m=video")
        val indexOfAnswerAudio = answerSdp.indexOf("m=audio")
        if (indexOfAnswerVideo == -1 || indexOfAnswerAudio == -1) {
            return answerSdp
        }

        val isFirstOfferVideo = indexOfOfferVideo < indexOfOfferAudio
        val isFirstAnswerVideo = indexOfAnswerVideo < indexOfAnswerAudio
        return if (isFirstOfferVideo == isFirstAnswerVideo) {
            //顺序一致
            answerSdp
        } else {
            //需要调换顺序
            buildString {
                append(answerSdp.substring(0, indexOfAnswerVideo.coerceAtMost(indexOfAnswerAudio)))
                append(
                    answerSdp.substring(
                        indexOfAnswerVideo.coerceAtLeast(indexOfOfferVideo),
                        answerSdp.length
                    )
                )
                append(
                    answerSdp.substring(
                        indexOfAnswerVideo.coerceAtMost(indexOfAnswerAudio),
                        indexOfAnswerVideo.coerceAtLeast(indexOfOfferVideo)
                    )
                )
            }
        }
    }
}