package com.fast.open.ss.dual.agreement.bean

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
