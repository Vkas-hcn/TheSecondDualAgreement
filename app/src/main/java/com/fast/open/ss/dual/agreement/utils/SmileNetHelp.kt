package com.fast.open.ss.dual.agreement.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.android.installreferrer.api.ReferrerDetails
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.bean.AdInformation
import com.fast.open.ss.dual.agreement.bean.PotIntInfo
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object SmileNetHelp {
    val smileNetManager = SmileNetManager()

    suspend fun getOnlyIp() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://ifconfig.me/ip")
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }

            SmileKey.ip_lo_sm = content
        } catch (e: Exception) {
        }
    }

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
                            DaDianUtils.oom1(context, response)
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

    private fun nextBlackFun(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(10000)
            getBlackData(context)
        }
    }


    @SuppressLint("HardwareIds")
    fun blackData(context: Context): Map<String, Any> {
        return mapOf<String, Any>(
            //distinct_id
            "haze" to SmileKey.uuid_smile,
            //client_ts
            "rabbet" to (System.currentTimeMillis()),
            //device_model
            "sorrow" to Build.MODEL,
            //bundle_id
            "celia" to ("com.blooming.unlimited.fast"),
            //os_version
            "va" to Build.VERSION.RELEASE,
            //gaid
            "catkin" to SmileKey.gidData,
            //android_id
            "negligee" to Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ),
            //os
            "blade" to "director",
            //app_version
            "freemen" to (getAppVersion(context) ?: ""),
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

    fun postSessionData(context: Context) {
        val data = PutDataUtils.getSessionJson(context)
        Log.e(TAG, "postSessionData: data=${data}")

        smileNetManager.postPutData(
            SmileKey.put_data_url,
            data,
            object : SmileNetManager.Callback {
                override fun onSuccess(response: String) {
                    Log.e(TAG, "postSessionData: onSuccess=${response}")
                }

                override fun onFailure(error: String) {
                    Log.e(TAG, "postSessionData: onFailure=${error}")

                }
            })
    }

    fun postAdData(
        context: Context, adValue: AdValue,
        responseInfo: ResponseInfo,
        adInformation: AdInformation
    ) {
        val data = PutDataUtils.getAdJson(context, adValue, responseInfo, adInformation)
        Log.e(TAG, "postAdData: data=${data}")
        smileNetManager.postPutData(
            SmileKey.put_data_url,
            data,
            object : SmileNetManager.Callback {
                override fun onSuccess(response: String) {
                    Log.e(TAG, "postAdData: onSuccess=${response}")
                }

                override fun onFailure(error: String) {
                    Log.e(TAG, "postAdData: onFailure=${error}")
                }
            })
        PutDataUtils.postAdOnline(adValue.valueMicros)
    }

    fun postInstallData(context: Context, rd: ReferrerDetails) {
        if (SmileKey.isInstall == "1") {
            return
        }
        val data = PutDataUtils.getInstallJson(rd, context)
        Log.e(TAG, "postInstallData: data=${data}")

        try {
            smileNetManager.postPutData(
                SmileKey.put_data_url,
                data,
                object : SmileNetManager.Callback {
                    override fun onSuccess(response: String) {
                        SmileKey.isInstall = "1"
                        Log.e(TAG, "postInstallData: onSuccess=${response}")

                    }

                    override fun onFailure(error: String) {
                        SmileKey.isInstall = "0"
                        Log.e(TAG, "postInstallData: onFailure=${error}")

                    }
                })
        } catch (e: Exception) {
            SmileKey.isInstall = "0"
            Log.e(TAG, "postInstallData: Exception=${e}")

        }
    }

    fun postPotNet(
        context: Context,
        name: String,
        key: String? = null,
        keyValue: String? = null,
        successFun: () -> Unit,
    ) {
        val data = if (key != null) {
            PutDataUtils.getTbaTimeDataJson(context, name, key, keyValue)
        } else {
            PutDataUtils.getTbaDataJson(context, name)
        }
        Log.e(TAG, "postPotNet--${name}: data=${data}")
        try {
            smileNetManager.postPutData(
                SmileKey.put_data_url,
                data,
                object : SmileNetManager.Callback {
                    override fun onSuccess(response: String) {
                        Log.e(TAG, "postPotNet--${name}: onSuccess=${response}")
                        successFun()
                    }

                    override fun onFailure(error: String) {
                        Log.e(TAG, "postPotNet--${name}: onFailure=${error}")
                        val bean = PotIntInfo()
                        bean.name = name
                        bean.parameterName = key
                        bean.parameterValue = keyValue
                        addAnErrorMessage(bean)
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "postPotNet--${name}: Exception=${e}")
        }
    }


    fun postPotNet(
        context: Context,
        name: String,
        key1: String? = null,
        keyValue1: String? = null,
        key2: String? = null,
        keyValue2: String? = null,
        key3: String? = null,
        keyValue3: String? = null,
        successFun: () -> Unit,
    ) {
        val data =
            PutDataUtils.getTbaTimeListDataJson(
                context,
                name,
                key1,
                keyValue1,
                key2,
                keyValue2,
                key3,
                keyValue3
            )

        Log.e(TAG, "postPotNet--${name}: data=${data}")
        try {
            smileNetManager.postPutData(
                SmileKey.put_data_url,
                data,
                object : SmileNetManager.Callback {
                    override fun onSuccess(response: String) {
                        Log.e(TAG, "postPotNet--${name}: onSuccess=${response}")
                        successFun()
                    }

                    override fun onFailure(error: String) {
                        Log.e(TAG, "postPotNet--${name}: onFailure=${error}")
                        val bean = PotIntInfo()
                        bean.name = name
                        bean.parameterName = key1
                        bean.parameterValue = keyValue1
                        bean.parameterName2 = key2
                        bean.parameterValue2 = keyValue2
                        bean.parameterName3 = key3
                        bean.parameterValue3 = keyValue3
                        addAnErrorMessage(bean)
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "postPotNet--${name}: Exception=${e}")
        }
    }


    fun postPotNet(
        context: Context,
        name: String,
        key1: String? = null,
        keyValue1: String? = null,
        key2: String? = null,
        keyValue2: String? = null,
        key3: String? = null,
        keyValue3: String? = null
    ) {
        if (key2 == null) {
            postPotNet(context, name, key1, keyValue1){}
        } else {
            postPotNet(context, name, key1, keyValue1, key2, keyValue2, key3, keyValue3){}
        }
    }

    fun getOnlineSmData(context: Context) {
        val timeStart = System.currentTimeMillis()
        postPotNet(context, "blom1")
        smileNetManager.getServiceData(context, SmileKey.put_sm_service_data_url, {
            val data = SmileUtils.decodeTheData(it)
            SmileKey.vpn_online_data = data
            Log.e(TAG, "getOnlineSmData: ${SmileKey.vpn_online_data}")
            val timeEnd = (System.currentTimeMillis() - timeStart) / 1000
            postPotNet(context, "blom2t", "time", timeEnd.toString())
            postPotNet(context, "blom2")

        }, {
            Log.e(TAG, "getOnlineSmData---error=: ${it}")

        })
    }

    fun getFailedDotData(): MutableList<PotIntInfo>? {
        val errorData = SmileKey.app_point_error
        return runCatching {
            val typeToken = object : TypeToken<MutableList<PotIntInfo>>() {}.type
            Gson().fromJson<MutableList<PotIntInfo>>(errorData, typeToken)
        }.getOrNull()
    }

    fun addAnErrorMessage(bean: PotIntInfo) {
        val errorDataList = getFailedDotData() ?: mutableListOf()
        errorDataList.add(bean)
        SmileKey.app_point_error = Gson().toJson(errorDataList)
    }

    fun deleteAnErrorMessage(bean: PotIntInfo) {
        val errorDataList = getFailedDotData()
        if (errorDataList != null && errorDataList.size > 0) {
            errorDataList.remove(bean)
        }
        SmileKey.app_point_error = Gson().toJson(errorDataList)
    }

    //重新请求打点数据
    fun reRequestDotData(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val netState = isNetworkReachable()
            if (netState) {
                val data = getFailedDotData()
                Log.e(TAG, "onCreate: ${Gson().toJson(data)}")
                if (data != null && data.size > 0) {
                    data.forEach {
                        postPotNet(
                            context,
                            it.name ?: "",
                            it.parameterName ?: "",
                            it.parameterValue ?: ""
                        ) {
                            deleteAnErrorMessage(it)
                        }
                    }
                }
            }
        }

    }

     fun isNetworkReachable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            process.destroy()
            val result = output.toString()
            val state = result.contains("1 packets transmitted, 1 received")
            Log.e(TAG, "isNetworkReachable: ${state}")
            return state
        } catch (e: Exception) {
            Log.e(TAG, "isNetworkReachable: ----fasle")

            false
        }
    }
    fun isVpnConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = connectivityManager.allNetworks
        for (network in networks) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                return true
            }
        }
        return false
    }
}