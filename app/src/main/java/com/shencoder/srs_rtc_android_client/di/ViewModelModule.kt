package com.shencoder.srs_rtc_android_client.di

import com.shencoder.srs_rtc_android_client.ui.login.data.LoginDataSource
import com.shencoder.srs_rtc_android_client.ui.login.data.LoginRepository
import com.shencoder.srs_rtc_android_client.ui.login.LoginActivity
import com.shencoder.srs_rtc_android_client.ui.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 *
 * @author  ShenBen
 * @date    2022/1/18 16:08
 * @email   714081644@qq.com
 */

/**
 * [LoginActivity]
 */
private val loginModule = module {
    factory { LoginDataSource(get()) }
    factory { LoginRepository(get()) }
    viewModel { LoginViewModel(get(), get()) }
}

/**
 * ViewModel相关
 */
val viewModelModule = arrayListOf(
    loginModule
)