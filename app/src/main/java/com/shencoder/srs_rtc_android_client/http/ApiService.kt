package com.shencoder.srs_rtc_android_client.http

import com.shencoder.srs_rtc_android_client.constant.SRS
import com.shencoder.srs_rtc_android_client.http.bean.*
import com.shencoder.srs_rtc_android_client.http.bean.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/**
 *
 * @author  ShenBen
 * @date    2022/1/18 16:11
 * @email   714081644@qq.com
 */
interface ApiService {

    /**
     * 向SRS服务器请求拉流
     *
     * @param url [SRS.HTTP_REQUEST_PLAY_URL] or [SRS.HTTPS_REQUEST_PLAY_URL]
     */
    @POST
    suspend fun play(@Url url: String, @Body body: SrsRequestBean): SrsResponseBean

    /**
     * 向SRS服务器请求推流
     * @param url [SRS.HTTP_REQUEST_PUBLISH_URL] or [SRS.HTTPS_REQUEST_PUBLISH_URL]
     */
    @POST
    suspend fun publish(@Url url: String, @Body body: SrsRequestBean): SrsResponseBean

    /**
     * 用户登录
     */
    @POST("/srs_rtc/user/userLogin")
    suspend fun userLogin(@Body bean: UserLoginBean): ApiResponse<UserInfoBean>

    /**
     * 校验用户id是否可用
     */
    @POST("/srs_rtc/user/checkUserId")
    suspend fun checkUserId(@Body bean: CheckUserIdBean): ApiResponse<Any>

    /**
     * 注册用户
     */
    @POST("/srs_rtc/user/insertUser")
    suspend fun registerUser(@Body bean: RegisterUserBean): ApiResponse<Any>

    /**
     * 获取所有用户信息
     * 仅查询客户端；
     * query clients only.
     */
    @GET("/srs_rtc/user/getAllUserInfo")
    suspend fun getAllUser(): ApiResponse<List<UserInfoBean>>

}