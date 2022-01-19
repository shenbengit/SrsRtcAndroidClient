package com.shencoder.srs_rtc_android_client.ui.check_user.data

import com.shencoder.mvvmkit.base.repository.BaseRemoteRepository
import com.shencoder.srs_rtc_android_client.constant.Constant
import com.shencoder.srs_rtc_android_client.http.bean.CheckUserIdBean
import com.shencoder.srs_rtc_android_client.http.bean.RegisterUserBean
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.http.bean.base.ApiResponse
import com.shencoder.srs_rtc_android_client.ui.register.data.RegisterUserDataSource

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 14:45
 * @email   714081644@qq.com
 */
class CheckUserRepository(remoteDataSource: CheckUserDataSource) :
    BaseRemoteRepository<CheckUserDataSource>(remoteDataSource) {

    suspend fun getAllUser(): ApiResponse<List<UserInfoBean>> {
        return remoteDataSource.getAllUser()
    }
}