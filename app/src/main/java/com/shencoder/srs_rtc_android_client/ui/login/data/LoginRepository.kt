package com.shencoder.srs_rtc_android_client.ui.login.data

import com.shencoder.mvvmkit.base.repository.BaseRemoteRepository
import com.shencoder.srs_rtc_android_client.constant.Constant
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.http.bean.UserLoginBean
import com.shencoder.srs_rtc_android_client.http.bean.base.ApiResponse

class LoginRepository(dataSource: LoginDataSource) :
    BaseRemoteRepository<LoginDataSource>(dataSource) {

    @JvmOverloads
    suspend fun userLogin(
        userId: String,
        password: String,
        userType: String = Constant.USER_TYPE_CLIENT
    ): ApiResponse<UserInfoBean> {
        return remoteDataSource.userLogin(UserLoginBean(password, userId, userType))
    }
}