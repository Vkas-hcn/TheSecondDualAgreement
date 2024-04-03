package com.fast.open.ss.dual.agreement.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.webkit.WebSettings
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.android.installreferrer.api.ReferrerDetails
import com.facebook.appevents.AppEventsLogger
import com.fast.open.ss.dual.agreement.BuildConfig
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.bean.AdInformation
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdapterResponseInfo
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import org.json.JSONObject
import java.util.Currency
import java.util.Locale
import java.util.UUID


object PutDataUtils {
    private fun firstJsonData(context: Context): JSONObject {
        val jsonData = JSONObject()
        //os_version
        jsonData.put("va", Build.VERSION.RELEASE)
        //app_version
        jsonData.put("freemen", getAppVersion(context))
        //log_id
        jsonData.put("hispanic", UUID.randomUUID().toString())
        //gaid
        jsonData.put("catkin", SmileKey.gidData)
        //os
        jsonData.put("blade", "director")
        //device_model
        jsonData.put("sorrow", Build.MODEL)
        //system_language
        jsonData.put("lemur", "${Locale.getDefault().language}_${Locale.getDefault().country}")
        //bundle_id
        jsonData.put("celia", context.packageName)
        //operator
        jsonData.put("somatic", getNetworkInfo(context))
        //distinct_id
        jsonData.put(
            "haze",
            SmileKey.uuid_smile
        )
        //android_id
        jsonData.put(
            "negligee",
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        )
        //manufacturer
        jsonData.put("callahan", Build.MODEL)


        //client_ts
        jsonData.put("rabbet", System.currentTimeMillis())

        return jsonData
    }

    fun getSessionJson(context: Context): String {
        val topLevelJson = firstJsonData(context)
        topLevelJson.apply {
            put("rollback", JSONObject())
        }
        return topLevelJson.toString()
    }

    fun getInstallJson(rd: ReferrerDetails, context: Context): String {
        val topLevelJson = firstJsonData(context)
        topLevelJson.apply {
            put("pegboard", JSONObject().apply {
                //build
                put("weekend", "build/${Build.ID}")

                //referrer_url
                put("handyman", rd.installReferrer)

                //install_version
                put("build", rd.installVersion)

                //user_agent
                put("affirm", getWebDefaultUserAgent(context))

                //lat
                put("daunt", getLimitTracking(context))

                //referrer_click_timestamp_seconds
                put("benign", rd.referrerClickTimestampSeconds)

                //install_begin_timestamp_seconds
                put("stimulus", rd.installBeginTimestampSeconds)

                //referrer_click_timestamp_server_seconds
                put("fervid", rd.referrerClickTimestampServerSeconds)

                //install_begin_timestamp_server_seconds
                put("deanna", rd.installBeginTimestampServerSeconds)

                //install_first_seconds
                put("sewn", getFirstInstallTime(context))

                //last_update_seconds
                put("delta", getLastUpdateTime(context))
            })
        }
        return topLevelJson.toString()
    }

    fun getAdJson(
        context: Context, adValue: AdValue,
        responseInfo: ResponseInfo,
        adInformation: AdInformation
    ): String {
        val topLevelJson = firstJsonData(context)
        topLevelJson.put("curate",JSONObject().apply {
            put("rid_yawn",adInformation.loadCity)
            put("rid_fist",adInformation.showTheCity)
        })
        //ad_pre_ecpm
        topLevelJson.put("bassett", adValue.valueMicros)
        //currency
        topLevelJson.put("arrogate", adValue.currencyCode)
        //ad_network
        topLevelJson.put(
            "cocoa",
            responseInfo.mediationAdapterClassName
        )
        //ad_source
        topLevelJson.put("diocese", "admob")

        //ad_code_id
        topLevelJson.put("dateline", adInformation.id)
        //ad_pos_id
        topLevelJson.put("expand", adInformation.name)

        //ad_format
        topLevelJson.put("kingsley", adInformation.type)
        //precision_type
        topLevelJson.put("breakup", getPrecisionType(adValue.precisionType))
        //ad_load_ip
        topLevelJson.put("stoic", adInformation.loadIp ?: "")
//        //ad_impression_ip
        topLevelJson.put("dally", adInformation.showIp ?: "")
        topLevelJson.put("scoop", "brain")

        return topLevelJson.toString()
    }

    fun getTbaDataJson(context: Context, name: String): String {
        return firstJsonData(context).apply {
            put("scoop", name)
        }.toString()
    }

    fun getTbaTimeDataJson(
        context: Context,
        name: String,
        parameterName: String,
        time: Any,

    ): String {
        val data = JSONObject()
        data.put(parameterName, time)
        return firstJsonData(context).apply {
            put("scoop", name)
            put(name, JSONObject().apply {
                put(parameterName, time)
            })
        }.toString()
    }

    private fun getAppVersion(context: Context): String {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "Version information not available"
    }


    private fun getNetworkInfo(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val carrierName = telephonyManager.networkOperatorName
        val networkOperator = telephonyManager.networkOperator
        val mcc = if (networkOperator.length >= 3) networkOperator.substring(0, 3) else ""
        val mnc = if (networkOperator.length >= 5) networkOperator.substring(3) else ""

        return """
        Carrier Name: $carrierName
        MCC: $mcc
        MNC: $mnc
    """.trimIndent()
    }


    private fun getWebDefaultUserAgent(context: Context): String {
        return try {
            WebSettings.getDefaultUserAgent(context)
        } catch (e: Exception) {
            ""
        }
    }

    private fun getLimitTracking(context: Context): String {
        return try {
            if (AdvertisingIdClient.getAdvertisingIdInfo(context).isLimitAdTrackingEnabled) {
                "mangrove"
            } else {
                "melanin"
            }
        } catch (e: Exception) {
            "melanin"
        }
    }

    private fun getFirstInstallTime(context: Context): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.firstInstallTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun getLastUpdateTime(context: Context): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.lastUpdateTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }


    private fun getPrecisionType(precisionType: Int): String {
        return when (precisionType) {
            0 -> {
                "UNKNOWN"
            }

            1 -> {
                "ESTIMATED"
            }

            2 -> {
                "PUBLISHER_PROVIDED"
            }

            3 -> {
                "PRECISE"
            }

            else -> {
                "UNKNOWN"
            }
        }
    }

//    fun String.putPointYep(context: Context) {
//        FlashOkHttpUtils().getTbaList(context, this)
//    }
//
//    fun putPointTimeYep(name: String, time: Any, parameterName: String, context: Context) {
//        FlashOkHttpUtils().getTbaList(context, name, parameterName, time, 1)
//    }

    fun postAdOnline(adValue: Long) {

        if (BuildConfig.DEBUG) {
            return
        }
        AppEventsLogger.newLogger(App.getAppContext()).logPurchase(
            (adValue / 1000000.0).toBigDecimal(), Currency.getInstance("USD")
        )
    }

    fun beforeLoadLink(adInformation: AdInformation): AdInformation {
        if (App.vpnLink && !SmileKey.smile_arrow) {
            adInformation.loadIp = SmileKey.vpn_ip
            adInformation.loadCity = SmileKey.vpn_city
        } else {
            adInformation.loadIp = SmileKey.ip_lo_sm
            adInformation.loadCity = "null"
        }
        return adInformation
    }

    fun afterLoadLink(adInformation: AdInformation): AdInformation {
        if (App.vpnLink && !SmileKey.smile_arrow) {
            adInformation.showIp = SmileKey.vpn_ip
            adInformation.showTheCity = SmileKey.vpn_city
        } else {
            adInformation.showIp = SmileKey.ip_lo_sm
            adInformation.showTheCity = "null"
        }
        return adInformation
    }
}