//package com.shencoder.srs_rtc_android_client.widget
//
//import android.content.Context
//import android.util.AttributeSet
//import android.widget.FrameLayout
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.core.view.isGone
//import androidx.core.view.isVisible
//import bd.nj.meetingsystem.call.R
//import com.shencoder.srs_rtc_android_client.R
//import org.webrtc.EglBase
//import org.webrtc.SurfaceViewRenderer
//import org.webrtc.VideoTrack
//
///**
// *
// * @author  ShenBen
// * @date    2021/12/9 11:10
// * @email   714081644@qq.com
// */
//class CustomVideoLayout @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null
//) : FrameLayout(context, attrs) {
//
//    private val surfaceViewRenderer: SurfaceViewRenderer
//    private val ivBg: ImageView
//    private val tvName: TextView
//
//    init {
//        inflate(context, R.layout.layout_custom_video_layout, this)
//        surfaceViewRenderer = findViewById(R.id.svr)
//        ivBg = findViewById(R.id.ivBg)
//        tvName = findViewById(R.id.tvName)
//    }
//
//    fun init(eglBaseContext: EglBase.Context, title: String?, isPublish: Boolean) {
//        surfaceViewRenderer.init(eglBaseContext, null)
//        post {
//            tvName.text = title
//        }
//    }
//
//    fun addVideoStream(videoTrack: VideoTrack) {
//        post {
//            ivBg.isGone = true
//            tvName.isGone = true
//        }
//        videoTrack.addSink(surfaceViewRenderer)
//    }
//
//    fun removeVideoStream(videoTrack: VideoTrack) {
//        post {
//            ivBg.isVisible = true
//            tvName.isVisible = true
//        }
//        videoTrack.removeSink(surfaceViewRenderer)
//    }
//
//    fun release() {
//        surfaceViewRenderer.release()
//    }
//
//}