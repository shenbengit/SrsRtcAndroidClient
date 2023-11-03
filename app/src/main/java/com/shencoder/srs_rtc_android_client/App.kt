package com.shencoder.srs_rtc_android_client

import android.app.Application
import android.util.Log
import com.shencoder.mvvmkit.ext.globalInit
import com.shencoder.srs_rtc_android_client.constant.Constant
import com.shencoder.srs_rtc_android_client.di.appModule
import org.koin.android.java.KoinAndroidApplication
import org.koin.core.logger.Level
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory

/**
 *
 * @author  ShenBen
 * @date    2022/1/18 14:45
 * @email   714081644@qq.com
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        //初始化WebRTC连接工厂，必须调用一次
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(applicationContext)
                .setInjectableLogger(
                    { message, severity, label ->
                        val message = "label: $label, message: $message"
                        when (severity) {
                            Logging.Severity.LS_VERBOSE -> {
                                Log.v("WebRtcLog", message)
                            }

                            Logging.Severity.LS_INFO -> {
                                Log.i("WebRtcLog", message)
                            }

                            Logging.Severity.LS_WARNING -> {
                                Log.w("WebRtcLog", message)
                            }

                            Logging.Severity.LS_ERROR -> {
                                Log.e("WebRtcLog", message)
                            }

                            Logging.Severity.LS_NONE -> {
                                Log.d("WebRtcLog", message)
                            }
                        }
                    },
                    if (BuildConfig.DEBUG) Logging.Severity.LS_VERBOSE else Logging.Severity.LS_WARNING
                )
                .createInitializationOptions()
        )

//        Logging.enableLogToDebugOutput(if (BuildConfig.DEBUG) Logging.Severity.LS_VERBOSE else Logging.Severity.LS_WARNING)

        val koinApplication =
            KoinAndroidApplication
                .create(
                    this,
                    if (BuildConfig.DEBUG) Level.ERROR else Level.ERROR
                )
                .modules(appModule)

        globalInit(BuildConfig.DEBUG, Constant.TAG, koinApplication)
    }
}