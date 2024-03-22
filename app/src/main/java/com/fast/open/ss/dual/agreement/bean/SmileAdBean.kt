package com.fast.open.ss.dual.agreement.bean

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class SmileAdBean(
    @SerializedName("bluvivics")
    val open_smile: String = "",

    @SerializedName("bluagr")
    val home_smile: String = "",

    @SerializedName("blumiso")
    val end_smile: String = "",

    @SerializedName("bluvity")
    val connect_smile: String = "",

    @SerializedName("bluhangan")
    val back_smile: String = "",

)

@Keep
data class AdInformation(
    var id: String?=null,
    var where: String?=null,
    var name:String?=null,
    var type: String?=null,
    var loadCity: String?=null,
    var showTheCity: String?=null,
    var loadIp: String?=null,
    var showIp: String?=null,
)
