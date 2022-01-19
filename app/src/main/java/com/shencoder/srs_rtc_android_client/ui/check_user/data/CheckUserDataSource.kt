package com.shencoder.srs_rtc_android_client.ui.check_user.data

import com.shencoder.mvvmkit.base.repository.IRemoteDataSource
import com.shencoder.srs_rtc_android_client.http.RetrofitClient
import com.shencoder.srs_rtc_android_client.http.bean.CheckUserIdBean
import com.shencoder.srs_rtc_android_client.http.bean.RegisterUserBean
import com.shencoder.srs_rtc_android_client.http.bean.UserInfoBean
import com.shencoder.srs_rtc_android_client.http.bean.base.ApiResponse

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 14:45
 * @email   714081644@qq.com
 */
class CheckUserDataSource(private val retrofitClient: RetrofitClient) : IRemoteDataSource {

    suspend fun getAllUser(): ApiResponse<List<UserInfoBean>>{
        return retrofitClient.getApiService().getAllUser()
    }
}