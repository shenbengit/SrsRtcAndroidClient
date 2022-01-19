package com.shencoder.srs_rtc_android_client.ui.login.data

import com.shencoder.mvvmkit.base.repository.IRemoteDataSource
import com.shencoder.srs_rtc_android_client.http.RetrofitClient
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.http.bean.UserLoginBean
import com.shencoder.srs_rtc_android_client.http.bean.base.ApiResponse

class LoginDataSource(private val retrofitClient: RetrofitClient) : IRemoteDataSource {

    suspend fun userLogin(userLoginBean: UserLoginBean): ApiResponse<UserInfoBean> {
        return retrofitClient.getApiService().userLogin(userLoginBean)
    }
}