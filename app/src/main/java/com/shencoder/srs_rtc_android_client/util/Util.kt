package com.shencoder.srs_rtc_android_client.util

import android.Manifest
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX
import com.shencoder.srs_rtc_android_client.R
import okhttp3.OkHttpClient
import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 *
 * @author  ShenBen
 * @date    2022/1/18 17:52
 * @email   714081644@qq.com
 */

/**
 * okhttp忽略证书认证
 */
fun OkHttpClient.Builder.ignoreCertificate(): OkHttpClient.Builder {
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
    val hostnameVerifier = HostnameVerifier { _, _ -> true }
    return sslSocketFactory(sslContext.socketFactory)
        .hostnameVerifier(hostnameVerifier)
}

private val avatarList = listOf(
    R.drawable.ic_avatar01,
    R.drawable.ic_avatar02,
    R.drawable.ic_avatar03,
    R.drawable.ic_avatar04,
    R.drawable.ic_avatar05,
)

@DrawableRes
fun randomAvatar(): Int {
    return avatarList[(avatarList.indices).random()]
}


/**
 * 请求通话相关权限
 */
fun FragmentActivity.requestCallPermissions(result: (allGranted: Boolean) -> Unit) {
    PermissionX.init(this)
        .permissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        .request { allGranted, _, _ ->
            result.invoke(allGranted)
        }
}

/**
 * 请求通话相关权限
 */
fun Fragment.requestCallPermissions(result: (allGranted: Boolean) -> Unit) {
    PermissionX.init(this)
        .permissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        .request { allGranted, _, _ ->
            result.invoke(allGranted)
        }
}

