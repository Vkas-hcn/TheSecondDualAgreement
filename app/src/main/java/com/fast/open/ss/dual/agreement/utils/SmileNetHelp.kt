package com.fast.open.ss.dual.agreement.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

object SmileNetHelp {
    val smileNetManager = SmileNetManager()

    suspend fun getLoadIp() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.infoip.io/")
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }
            val data = content.split("\"country_short\":\"")[1].split("\"")[0]
            SmileKey.ip_gsd = data
        } catch (e: Exception) {
        }
    }

    suspend fun getLoadOthIp() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://ipinfo.io/json")
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }
            val data = content.split("\"country\":\"")[1].split("\"")[0]
            SmileKey.ip_gsd_oth = data
        } catch (e: Exception) {
        }
    }

    suspend fun getBlackData(context: Context) = withContext(Dispatchers.IO) {
        val data = SmileKey.local_clock
        if (data.isEmpty()) {
            val params = blackData(context)
            try {
                smileNetManager.getMapRequest(
                    "https://emery.bloomingunlimited.com/assam/meander",
                    params,
                    object : SmileNetManager.Callback {
                        override fun onSuccess(response: String) {
                            SmileKey.local_clock = response
                        }
                        override fun onFailure(error: String) {
                            nextBlackFun(context)
                        }
                    })
            } catch (e: Exception) {
                nextBlackFun(context)
            }
        }
    }

    private fun nextBlackFun(context: Context){
        GlobalScope.launch(Dispatchers.IO) {

            delay(10000)
            getBlackData(context)
        }
    }


    fun blackData(context: Context): Map<String, Any> {
        return mapOf<String, Any>(
            //distinct_id
            "haze" to SmileKey.uuid_smile,
            //client_ts
            "rabbet" to (System.currentTimeMillis()),//日志发生的客户端时间，毫秒数
            //device_model
            "sorrow" to Build.MODEL,
            //bundle_id
            "celia" to ("com.blooming.unlimited.fast"),//当前的包名称，a.b.c
            //os_version
            "va" to Build.VERSION.RELEASE,
            //gaid
            "catkin" to (runCatching { AdvertisingIdClient.getAdvertisingIdInfo(context).id }.getOrNull()
                ?: ""),
            //android_id
            "negligee" to Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ),
            //os
            "blade" to "executor",
            //app_version
            "freemen" to (getAppVersion(context) ?: ""),//应用的版本
            //operator
            "somatic" to "",
            //sdk_ver
            "armata" to ""
        )
    }

    private fun getAppVersion(context: Context): String? {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }
}