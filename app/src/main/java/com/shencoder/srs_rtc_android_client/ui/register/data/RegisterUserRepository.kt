package com.shencoder.srs_rtc_android_client.ui.register.data

import com.shencoder.mvvmkit.base.repository.BaseRemoteRepository
import com.shencoder.srs_rtc_android_client.constant.Constant
import com.shencoder.srs_rtc_android_client.http.bean.CheckUserIdBean
import com.shencoder.srs_rtc_android_client.http.bean.RegisterUserBean
import com.shencoder.srs_rtc_android_client.http.bean.base.ApiResponse

/**
 *
 * @author  ShenBen
 * @date    2022/1/19 14:45
 * @email   714081644@qq.com
 */
class RegisterUserRepository(remoteDataSource: RegisterUserDataSource) :
    BaseRemoteRepository<RegisterUserDataSource>(remoteDataSource) {

    /**
     * 校验userId是否可用
     */
    @JvmOverloads
    suspend fun checkUserId(
        userId: String,
        userType: String = Constant.USER_TYPE_CLIENT
    ): ApiResponse<Any> {
        return remoteDataSource.checkUserId(CheckUserIdBean(userId, userType))
    }

    /**
     * 注册用户
     */
    @JvmOverloads
    suspend fun registerUser(
        userId: String,
        username: String,
        password: String,
        userType: String = Constant.USER_TYPE_CLIENT
    ): ApiResponse<Any> {
        return remoteDataSource.registerUser(RegisterUserBean(password, userId, username, userType))
    }
}