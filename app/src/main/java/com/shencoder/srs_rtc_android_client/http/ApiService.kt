package com.shencoder.srs_rtc_android_client.http

import com.shencoder.srs_rtc_android_client.constant.SRS
import com.shencoder.srs_rtc_android_client.http.bean.SrsRequestBean
import com.shencoder.srs_rtc_android_client.http.bean.SrsResponseBean
import retrofit2.http.Body
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


}