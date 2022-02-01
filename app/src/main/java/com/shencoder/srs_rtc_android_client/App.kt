package com.shencoder.srs_rtc_android_client

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.shencoder.mvvmkit.coil.BufferedSourceFetcher
import com.shencoder.mvvmkit.coil.ByteArrayFetcher
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
class App : Application(), ImageLoaderFactory {

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

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .componentRegistry {
                //Coil 图片加载框架不支持 ByteArray ，需要自己实现
                add(ByteArrayFetcher())
                //直接使用网络流显示图片
                add(BufferedSourceFetcher())
            }
            .build()
    }
}