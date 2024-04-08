package com.fast.open.ss.dual.agreement.app

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.ui.first.FirstActivity
import com.fast.open.ss.dual.agreement.ui.main.MainActivity
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.fast.open.ss.dual.agreement.utils.SmileNetHelp
import com.fast.open.ss.dual.agreement.utils.SmileUtils
import com.fast.open.ss.dual.agreement.utils.TimeData
import com.fast.open.ss.dual.agreement.utils.UserConter
import com.github.shadowsocks.Core
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class App : Application(), LifecycleObserver {
    companion object {
        var TAG = "smile"
        var ad_activity_smart: Activity? = null
        var top_activity_smart: Activity? = null
        var top_activity_name: String? = null

        private lateinit var instance: App
        fun getAppContext() = instance
        var isBackDataSmile = false
        var vpnLink = false
        var isBoot = false
        var whetherBackgroundSmild = false
        var serviceState: String = "mo"
        val mmkvSmile by lazy {
            MMKV.mmkvWithID("smile", MMKV.MULTI_PROCESS_MODE)
        }
        var isAppRunning = false
        val timeVpnNum = 60 * 30

        //剩余连接时长
        var reConnectTime = timeVpnNum
        var add30Num = 3
        var isStopVpn = false
    }

    var flag = 0
    var job_smart: Job? = null
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        Core.init(this, MainActivity::class)
        iniApp()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        appOnStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopState() {
        appOnStop()
    }

    private fun setActivityLifecycleSmart(application: Application) {
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity !is AdActivity) {
                    top_activity_smart = activity
                } else {
                    ad_activity_smart = activity
                }
            }

            override fun onActivityStarted(activity: Activity) {
                if (activity !is AdActivity) {
                    top_activity_smart = activity
                    top_activity_name = activity.javaClass.simpleName
                } else {
                    ad_activity_smart = activity
                }

                flag++
                isBackDataSmile = false
            }

            override fun onActivityResumed(activity: Activity) {
                Adjust.onResume()
                if (activity !is AdActivity) {
                    top_activity_smart = activity
                }
            }

            override fun onActivityPaused(activity: Activity) {
                Adjust.onPause()
                if (activity is AdActivity) {
                    ad_activity_smart = activity
                } else {
                    top_activity_smart = activity
                }
            }

            override fun onActivityStopped(activity: Activity) {
                flag--
                if (flag == 0) {
                    isBackDataSmile = true
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                ad_activity_smart = null
                top_activity_smart = null
            }
        })
    }

    private fun appOnStart() {
        job_smart?.cancel()
        job_smart = null
        if (whetherBackgroundSmild && !isBackDataSmile) {
            isBoot = false
            whetherBackgroundSmild = false
            val intent = Intent(top_activity_smart, FirstActivity::class.java)
            top_activity_smart?.startActivity(intent)
            isAppRunning = true
        }
    }

    private fun appOnStop() {
        job_smart = GlobalScope.launch {
            whetherBackgroundSmild = false
            delay(3000L)
            whetherBackgroundSmild = true
            ad_activity_smart?.finish()
            if (top_activity_smart is FirstActivity) {
                top_activity_smart?.finish()
            }
        }
    }

    private fun isMainProcess(context: Context): Boolean {
        val pid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = activityManager.runningAppProcesses ?: return false

        val packageName = context.packageName
        for (appProcess in runningApps) {
            if (appProcess.pid == pid && packageName == appProcess.processName) {
                return true
            }
        }
        return false
    }


    fun iniApp() {
        if (isMainProcess(this)) {
            instance = this
            MobileAds.initialize(this) {}
            Firebase.initialize(this)
            FirebaseApp.initializeApp(this)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            setActivityLifecycleSmart(this)
            SmileKey.isAppGreenSameDayGreen()
            val data = SmileKey.uuid_smile
            if (data.isEmpty()) {
                SmileKey.uuid_smile = UUID.randomUUID().toString()
            }
            TimeData.sendTimerInformation()
            UserConter.getReferInformation(this)
            SmileNetHelp.postSessionData(this)
            initAdJust(this)
            getGid(this)
            Core.stopService()
            SmileKey.local_addNum = 0
        }
    }

    private fun initAdJust(application: Application) {
        Adjust.addSessionCallbackParameter("customer_user_id", SmileKey.uuid_smile)
        val appToken = "ih2pm2dr3k74"
        val environment: String = AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(application, appToken, environment)
        config.setOnAttributionChangedListener { attribution ->
            Timber.tag(TAG).e("adjust --data= %s", attribution)
            if (SmileKey.adjust_smile.isEmpty() && attribution.network.isNotEmpty() && attribution.network.contains(
                    "organic",
                    true
                ).not()
            ) {
                SmileKey.adjust_smile = attribution.network
            }
        }
        Adjust.onCreate(config)
    }

    fun getGid(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                SmileKey.gidData = AdvertisingIdClient.getAdvertisingIdInfo(context).id ?: ""
            }
        }
    }

}