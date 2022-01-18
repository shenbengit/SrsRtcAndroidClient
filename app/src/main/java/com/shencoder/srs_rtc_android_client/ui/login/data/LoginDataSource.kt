package com.shencoder.srs_rtc_android_client.ui.login.data

import com.shencoder.mvvmkit.base.repository.IRemoteDataSource
import com.shencoder.srs_rtc_android_client.http.RetrofitClient
import com.shencoder.srs_rtc_android_client.ui.login.data.model.LoggedInUser
import java.io.IOException

class LoginDataSource(private val retrofitClient: RetrofitClient) : IRemoteDataSource {

}