package com.fast.open.ss.dual.agreement.model

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.fast.open.ss.dual.agreement.BuildConfig
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class FirstViewModel:ViewModel() {
    val toMainLive = MutableLiveData<String>()
    val showOpen = MutableLiveData<Any>()
    fun getFileBaseData(activity: AppCompatActivity, loadAdFun:()->Unit) {
        activity.lifecycleScope.launch {
            var isCa = false
            if (!BuildConfig.DEBUG) {
                val auth = Firebase.remoteConfig
                auth.fetchAndActivate().addOnSuccessListener {
                    SmileKey.local_service = auth.getString(SmileKey.vpn_data_type)
                    SmileKey.local_service_fast = auth.getString(SmileKey.fast_data_type)
                    SmileKey.local_ad = auth.getString(SmileKey.ad_data_type)
                    SmileKey.local_ref_center = auth.getString(SmileKey.user_data_type)
                    SmileKey.local_control = auth.getString(SmileKey.lj_data_type)
                    isCa = true
                }
            }
            try {
                withTimeout(4000L) {
                    while (true) {
                        if (!isActive) {
                            break
                        }
                        if (isCa) {
                            loadAdFun()
                            cancel()
                        }
                        delay(500)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                cancel()
                loadAdFun()
            }
        }
    }
}