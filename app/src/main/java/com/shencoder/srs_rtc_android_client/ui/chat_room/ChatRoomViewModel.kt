package com.shencoder.srs_rtc_android_client.ui.chat_room

import android.app.Application
import com.shencoder.mvvmkit.base.repository.BaseNothingRepository
import com.shencoder.mvvmkit.base.viewmodel.BaseViewModel

/**
 *
 * @author  ShenBen
 * @date    2022/01/19 21:10
 * @email   714081644@qq.com
 */
class ChatRoomViewModel(
    application: Application,
    repo: BaseNothingRepository
) : BaseViewModel<BaseNothingRepository>(application, repo) {

}