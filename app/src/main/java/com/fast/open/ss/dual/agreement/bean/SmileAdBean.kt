package com.fast.open.ss.dual.agreement.bean

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class SmileAdBean(
    @SerializedName("bluvivics")
    val open_smile: MutableList<AdInformation> = ArrayList(),


    @SerializedName("bluvity")
    val connect_smile: MutableList<AdInformation> = ArrayList(),

    @SerializedName("bluhangan")
    val back_smile: MutableList<AdInformation> = ArrayList(),

    @SerializedName("blupp")
    val int3: MutableList<AdInformation> = ArrayList(),

    @SerializedName("bluads")
    val rewarded: MutableList<AdInformation> = ArrayList(),

    val clickNum:Int,
    val showNum:Int,
)

@Keep
data class AdInformation(
    var id: String? = null,
    var name: String? = null,
    var type: String? = null,
    var we: String? = null,
    var loadCity: String? = null,
    var showTheCity: String? = null,
    var loadIp: String? = null,
    var showIp: String? = null,
)
@Keep
data class PotIntInfo(
    var name: String? = null,
    var parameterName: String? = null,
    var parameterValue: String? = null,

    var parameterName2: String? = null,
    var parameterValue2: String? = null,

    var parameterName3: String? = null,
    var parameterValue3: String? = null,
)
