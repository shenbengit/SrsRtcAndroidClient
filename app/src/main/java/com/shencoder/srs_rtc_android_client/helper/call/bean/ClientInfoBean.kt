package com.shencoder.srs_rtc_android_client.helper.call.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Json


@Parcelize
@JsonClass(generateAdapter = true)
data class ClientInfoBean(
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "userId")
    val userId: String,
    @Json(name = "userType")
    val userType: String,
    @Json(name = "username")
    val username: String
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientInfoBean

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (userType != other.userType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId.hashCode()
        result = 31 * result + userType.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClientInfoBean(createdAt='$createdAt', id=$id, userId='$userId', userType='$userType', username='$username')"
    }

}