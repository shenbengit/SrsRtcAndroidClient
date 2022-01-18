package com.shencoder.srs_rtc_android_client.http

import android.os.Build
import com.elvishew.xlog.XLog
import com.shencoder.mvvmkit.http.BaseRetrofitClient
import com.shencoder.srs_rtc_android_client.BuildConfig
import com.shencoder.srs_rtc_android_client.constant.Constant
import com.shencoder.srs_rtc_android_client.constant.SIGNAL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/**
 *
 * @author  ShenBen
 * @date    2021/6/10 9:24
 * @email   714081644@qq.com
 */
class RetrofitClient : BaseRetrofitClient() {

    companion object {
        private const val DEFAULT_MILLISECONDS: Long = 30
    }

    private lateinit var apiService: ApiService

    override fun generateOkHttpBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        val xtm: X509TrustManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                object : X509ExtendedTrustManager() {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                        socket: Socket?
                    ) {
                    }

                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                        engine: SSLEngine?
                    ) {
                    }

                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                        socket: Socket?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                        engine: SSLEngine?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }

                }
            } else {
                object : X509TrustManager {

                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }

                }

            }

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(xtm), SecureRandom())
        val hostnameVerifier = HostnameVerifier { hostname, session -> true }

        val interceptor = HttpLoggingInterceptor { message -> XLog.i(message) }
        interceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE

        return builder.readTimeout(DEFAULT_MILLISECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .sslSocketFactory(sslContext.socketFactory)
            .hostnameVerifier(hostnameVerifier)
    }

    override fun generateRetrofitBuilder(builder: Retrofit.Builder): Retrofit.Builder {
        return builder.apply {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            addConverterFactory(MoshiConverterFactory.create(moshi))
        }
    }

    /**
     * 动态修改Retrofit-baseUrl
     */
    fun setBaseUrl(baseUrl: String) {
        apiService = getApiService(ApiService::class.java, baseUrl)
    }

    fun getApiService(): ApiService {
        if (this::apiService.isInitialized.not()) {
            setBaseUrl(SIGNAL.BASE_API_HTTPS_URL)
        }
        return apiService
    }
}