package com.shencoder.srs_rtc_android_client.di

import com.shencoder.srs_rtc_android_client.ui.callee_chat.CalleeChatActivity
import com.shencoder.srs_rtc_android_client.ui.callee_chat.CalleeChatViewModel
import com.shencoder.srs_rtc_android_client.ui.caller_chat.CallerChatActivity
import com.shencoder.srs_rtc_android_client.ui.caller_chat.CallerChatViewModel
import com.shencoder.srs_rtc_android_client.ui.chat_room.ChatRoomActivity
import com.shencoder.srs_rtc_android_client.ui.chat_room.ChatRoomViewModel
import com.shencoder.srs_rtc_android_client.ui.check_user.CheckUserActivity
import com.shencoder.srs_rtc_android_client.ui.check_user.CheckUserViewModel
import com.shencoder.srs_rtc_android_client.ui.check_user.data.CheckUserDataSource
import com.shencoder.srs_rtc_android_client.ui.check_user.data.CheckUserRepository
import com.shencoder.srs_rtc_android_client.ui.login.data.LoginDataSource
import com.shencoder.srs_rtc_android_client.ui.login.data.LoginRepository
import com.shencoder.srs_rtc_android_client.ui.login.LoginActivity
import com.shencoder.srs_rtc_android_client.ui.main.MainActivity
import com.shencoder.srs_rtc_android_client.ui.register.RegisterUserActivity
import com.shencoder.srs_rtc_android_client.ui.login.LoginViewModel
import com.shencoder.srs_rtc_android_client.ui.main.MainViewModel
import com.shencoder.srs_rtc_android_client.ui.p2p.P2pCalleeActivity
import com.shencoder.srs_rtc_android_client.ui.p2p.P2pCalleeViewModel
import com.shencoder.srs_rtc_android_client.ui.p2p.P2pCallerActivity
import com.shencoder.srs_rtc_android_client.ui.p2p.P2pCallerViewModel
import com.shencoder.srs_rtc_android_client.ui.register.RegisterUserViewModel
import com.shencoder.srs_rtc_android_client.ui.register.data.RegisterUserDataSource
import com.shencoder.srs_rtc_android_client.ui.register.data.RegisterUserRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
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
 * [MainActivity]
 */
private val mainModule = module {
    viewModel { MainViewModel(get(), get()) }
}

/**
 * [RegisterUserActivity]
 */
private val registerUserModule = module {
    factory { RegisterUserDataSource(get()) }
    factory { RegisterUserRepository(get()) }
    viewModel { RegisterUserViewModel(get(), get()) }
}

/**
 * [CheckUserActivity]
 */
private val checkUserModule = module {
    factory { CheckUserDataSource(get()) }
    factory { CheckUserRepository(get()) }
    viewModel { CheckUserViewModel(get(), get()) }
}

/**
 * [CalleeChatActivity]
 */
private val calleeChatModule = module {
    viewModel { CalleeChatViewModel(get(), get()) }
}

/**
 * [CallerChatActivity]
 */
private val callerChatModule = module {
    viewModel { CallerChatViewModel(get(), get()) }
}

/**
 * [ChatRoomActivity]
 */
private val chatRoomModule = module {
    viewModel { ChatRoomViewModel(get(), get()) }
}

/**
 * [P2pCallerActivity]
 */
private val p2pCallerModule = module {
    viewModelOf(::P2pCallerViewModel)
}

/**
 * [P2pCalleeActivity]
 */
private val p2pCalleeModule = module {
    viewModelOf(::P2pCalleeViewModel)
}

/**
 * ViewModel相关
 */
val viewModelModule = arrayListOf(
    loginModule,
    mainModule,
    registerUserModule,
    checkUserModule,
    calleeChatModule,
    callerChatModule,
    chatRoomModule,
    p2pCallerModule,
    p2pCalleeModule
)