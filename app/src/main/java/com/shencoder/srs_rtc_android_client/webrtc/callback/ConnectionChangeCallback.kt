package com.shencoder.srs_rtc_android_client.webrtc.callback

import com.shencoder.srs_rtc_android_client.webrtc.widget.PlayStreamSurfaceViewRenderer
import com.shencoder.srs_rtc_android_client.webrtc.widget.PublishStreamSurfaceViewRenderer
import org.webrtc.PeerConnection

/**
 *
 * @author  ShenBen
 * @date    2022/1/28 15:05
 * @email   714081644@qq.com
 */
interface ConnectionChangeCallback {

    fun onPublishConnectionChange(
        renderer: PublishStreamSurfaceViewRenderer,
        newState: PeerConnection.PeerConnectionState
    )

    fun onPlayConnectionChange(
        renderer: PlayStreamSurfaceViewRenderer,
        newState: PeerConnection.PeerConnectionState
    )
}