package com.shencoder.srs_rtc_android_client.di

import com.shencoder.srs_rtc_android_client.http.RetrofitClient
import org.koin.dsl.module

/**
 *
 * @author  ShenBen
 * @date    2022/1/18 16:07
 * @email   714081644@qq.com
 */

private val singleModule = module {
    single { RetrofitClient() }
}


val appModule = mutableListOf(singleModule).apply {
    addAll(viewModelModule)
}