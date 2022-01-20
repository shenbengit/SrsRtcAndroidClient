package com.shencoder.srs_rtc_android_client.http.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Json
import kotlinx.parcelize.IgnoredOnParcel


@Parcelize
@JsonClass(generateAdapter = true)
data class UserInfoBean(
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
    /**
     * 是否可选择
     *
     * can selectable
     */
    @Transient
    @IgnoredOnParcel
    var selectable = true

    /**
     * 是否是选择状态
     * 仅当[selectable] = true时，可为true。
     * can selected when [selectable] = true only.
     */
    @Transient
    @IgnoredOnParcel
    var isSelected = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserInfoBean

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
}