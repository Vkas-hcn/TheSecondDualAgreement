package com.fast.open.ss.dual.agreement.utils

import android.content.Context
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.bean.SmileAdBean
import com.fast.open.ss.dual.agreement.bean.SmileFlowBean
import com.fast.open.ss.dual.agreement.bean.SmileRefBean
import com.fast.open.ss.dual.agreement.bean.VpnServiceBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SmileKey {
    const val SHARE_URL = "https://play.google.com/store/apps/details?id="
    const val web_smile_url = "https://www.baidu.com/"
    const val isSmileConnected = "isSmileConnected"
    const val cuSmileConnected = "cuSmileConnected"

    const val POS_OPEN = "si_o"
    const val POS_HOME = "si_h"
    const val POS_CONNECT = "si_c"
    const val POS_BACK = "si_b"
    const val POS_RESULT = "si_r"



    const val vpn_data_type = "blo"
    const val fast_data_type = "blm"
    const val ad_data_type = "blu"
    const val user_data_type = "blg"
    const val lj_data_type = "bly"
    private val sharedPreferences by lazy {
        App.getAppContext().getSharedPreferences(
            "smile_key",
            Context.MODE_PRIVATE
        )
    }

    var local_service = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_service", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_service", "").toString()
    var local_service_fast = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_service_fast", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_service_fast", "").toString()
    var local_ad = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_ad", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_ad", "").toString()


    var local_ref_center = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_ref_center", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_ref_center", "").toString()
    var local_control = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_control", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_control", "").toString()
    var uuid_smile = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("uuid_smile", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("uuid_smile", "").toString()
    var check_service = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("check_service", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("check_service", "").toString()
    var connection_mode = "0"
        set(value) {
            sharedPreferences.edit().run {
                putString("connection_mode", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("connection_mode", "0").toString()?:"0"
    var local_ref = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_ref", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_ref", "").toString()

    var local_clock = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_clock", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_clock", "").toString()

    var ip_gsd = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("ip_gsd", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("ip_gsd", "").toString()
    var ip_gsd_oth = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("ip_gsd_oth", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("ip_gsd_oth", "").toString()

    var smile_arrow: Boolean = false
        set(value) {
            App.mmkvSmile.encode("smile_arrow", value)
            field = value
        }
        get() = App.mmkvSmile.decodeBool("smile_arrow", false)
    fun decodeBase64(str: String): String {
        return String(android.util.Base64.decode(str, android.util.Base64.DEFAULT))
    }
    fun getAllVpnListData(): MutableList<VpnServiceBean>{
        val list = mutableListOf<VpnServiceBean>()
        list.add(getFastVpn())
        list.addAll(getVpnList())
        return list
    }
    fun getVpnList(): MutableList<VpnServiceBean> {
        val data = if (local_service.isEmpty()) {
            SmileData.smile_service_data
        } else {
            decodeBase64(local_service)
        }
        return try {
            Gson().fromJson(data, object : TypeToken<ArrayList<VpnServiceBean>>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            Gson().fromJson(
                SmileData.smile_service_data,
                object : TypeToken<ArrayList<VpnServiceBean>>() {}.type
            )
        }
    }
    fun getFastVpn(): VpnServiceBean {
        val data = if (local_service_fast.isEmpty()) {
            SmileData.smile_service_fast_data
        } else {
            decodeBase64(local_service_fast)
        }

        val fast = try {
            Gson().fromJson<ArrayList<String>>(data, object : TypeToken<ArrayList<String>>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            Gson().fromJson(SmileData.smile_service_fast_data, object : TypeToken<ArrayList<String>>() {}.type)
        }
        var bean = VpnServiceBean()
        getVpnList().forEach {
            if (fast.getOrNull(0) == it.bloally) {
                bean = it
                bean.best_smart = true
                bean.smart_smart = true
                bean.blocuss = "Fast Server"
            }
        }
        return bean
    }
    fun getAdJson(): SmileAdBean {
        val dataJson = local_ad.let {
            if (it.isEmpty()) {
                SmileData.local_smile_ad_data
            } else {
                decodeBase64(it)
            }
        }
        return try {
            Gson().fromJson(dataJson, object : TypeToken<SmileAdBean>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            Gson().fromJson(
                SmileData.local_smile_ad_data,
                object : TypeToken<SmileAdBean>() {}.type
            )
        }
    }

    fun getRefJson(): SmileRefBean {
        val dataJson = local_ref_center.let {
            if (it.isEmpty()) {
                SmileData.local_smile_data
            } else {
                decodeBase64(it)
            }
        }
        return try {
            Gson().fromJson(dataJson, object : TypeToken<SmileRefBean>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            Gson().fromJson(
                SmileData.local_smile_data,
                object : TypeToken<SmileRefBean>() {}.type
            )
        }
    }

    fun getFlowJson(): SmileFlowBean {
        val dataJson = local_control.let {
            if (it.isEmpty()) {
                SmileData.local_smile_logic
            } else {
                decodeBase64(it)
            }
        }
        return try {
            Gson().fromJson(dataJson, object : TypeToken<SmileFlowBean>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            Gson().fromJson(
                SmileData.local_smile_logic,
                object : TypeToken<SmileFlowBean>() {}.type
            )
        }
    }

}