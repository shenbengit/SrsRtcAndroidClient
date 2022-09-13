package com.shencoder.srs_rtc_android_client

import android.app.Application
import com.shencoder.mvvmkit.ext.globalInit
import com.shencoder.srs_rtc_android_client.constant.Constant
import com.shencoder.srs_rtc_android_client.di.appModule
import org.koin.android.java.KoinAndroidApplication
import org.koin.core.logger.Level
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
                .builder(applicationContext).createInitializationOptions()
        )

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