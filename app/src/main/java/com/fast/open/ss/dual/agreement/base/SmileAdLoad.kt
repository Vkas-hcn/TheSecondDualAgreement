package com.fast.open.ss.dual.agreement.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.bean.SmileAdBean
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.bean.AdInformation
import com.fast.open.ss.dual.agreement.utils.PutDataUtils
import com.fast.open.ss.dual.agreement.utils.SmileNetHelp
import com.fast.open.ss.dual.agreement.utils.SmileUtils.isVisible
import com.fast.open.ss.dual.agreement.utils.UserConter
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object SmileAdLoad {
    var isLoadOpenFist = false
    var openAdData = AdInformation()

    var connectAdData = AdInformation()

    var backAdData = AdInformation()
    var int3AdData = AdInformation()
    var reWardeAdData = AdInformation()

    fun init(context: Context) {
        GoogleAds.init(context) {
            preloadAds()
        }
    }

    fun loadOf(where: String) {
        Load.of(where)?.load()
    }

    fun resultOf(where: String): Any? {
        return Load.of(where)?.res
    }

    fun showFullScreenOf(
        where: String,
        context: AppCompatActivity,
        res: Any,
        preload: Boolean = false,
        onShowCompleted: () -> Unit
    ) {
        Show.of(where)
            .showFullScreen(
                context = context,
                res = res,
                callback = {
                    Load.of(where)?.let { load ->
                        load.clearCache()
                        if (preload) {
                            load.load()
                        }
                    }
                    onShowCompleted()
                }
            )
    }

    private fun preloadAds() {
        runCatching {
            Load.of(SmileKey.POS_OPEN)?.load()
            Load.of(SmileKey.POS_CONNECT)?.load()
        }
    }

    private class Load private constructor(private val where: String) {
        companion object {
            private val open by lazy { Load(SmileKey.POS_OPEN) }
            private val connect by lazy { Load(SmileKey.POS_CONNECT) }
            private val back by lazy { Load(SmileKey.POS_BACK) }
            private val int3 by lazy { Load(SmileKey.POS_INT3) }
            private val rewarded by lazy { Load(SmileKey.POS_RE) }

            fun of(where: String): Load? {
                return when (where) {
                    SmileKey.POS_OPEN -> open
                    SmileKey.POS_CONNECT -> connect
                    SmileKey.POS_BACK -> back
                    SmileKey.POS_INT3 -> int3
                    SmileKey.POS_RE -> rewarded
                    else -> null
                }
            }

        }


        private var createdTime = 0L
        var res: Any? = null
            set
        var isLoading = false
            set

        private fun printLog(content: String) {
            Log.d(TAG, "${where} ---${content}: ")
        }

        fun load(
            context: Context = App.getAppContext(),
            requestCount: Int = 1,
            inst: SmileAdBean = SmileKey.getAdJson(),
            isLoadType: Boolean = false
        ) {

            SmileKey.isAppGreenSameDayGreen()
            if (isLoading) {
                printLog("is requesting")
                return
            }

            val cache = res
            val cacheTime = createdTime
            if (cache != null) {
                if (cacheTime > 0L
                    && ((System.currentTimeMillis() - cacheTime) > (1000L * 60L * 60L))
                ) {
                    printLog("cache is expired")
                    Log.e(TAG, "load: clearCache")
                    clearCache()
                } else {
                    printLog("Existing cache")
                    return
                }
            }
            if ((cache == null || cache == "") && SmileKey.isThresholdReached()) {
                printLog("The ad reaches the go-live")
                SmileNetHelp.postPotIntData(context, "oom15", "oo", SmileKey.overrunType())
                res = ""
                return
            }
            if ((where == SmileKey.POS_BACK || where == SmileKey.POS_CONNECT || where == SmileKey.POS_INT3) && !UserConter.showAdCenter()) {
                res = ""
                return
            }
            if ((where == SmileKey.POS_BACK || where == SmileKey.POS_CONNECT || where == SmileKey.POS_INT3) && !UserConter.showAdBlacklist()) {
                res = ""
                return
            }
            isLoading = true
            val listData = when (where) {
                SmileKey.POS_OPEN -> {
                    inst.open_smile
                }

                SmileKey.POS_CONNECT -> inst.connect_smile

                SmileKey.POS_BACK -> inst.back_smile

                SmileKey.POS_INT3 -> inst.int3
                SmileKey.POS_RE -> inst.rewarded
                else -> emptyList()
            }
            val redListData = sortArrayByWeight(listData as MutableList)
            printLog("load started-data=${redListData}")
            doRequest(
                context, redListData
            ) {
                val isSuccessful = it != null
                printLog("load complete, result=$isSuccessful")
                if (isSuccessful) {
                    res = it
                    createdTime = System.currentTimeMillis()
                }
                isLoading = false
                if (!isSuccessful && where == SmileKey.POS_OPEN && requestCount < 2) {
                    load(context, requestCount + 1, inst)
                }
                if (!isSuccessful && where == SmileKey.POS_RE && requestCount < 3) {
                    load(context, requestCount + 1, inst)
                }
            }
        }

        fun sortArrayByWeight(items: MutableList<AdInformation>): MutableList<AdInformation> {
            val priorityMap = hashMapOf<String, Int>()
            ('a'..'z').forEachIndexed { index, string ->
                priorityMap[string.toString()] = index
                priorityMap[string.toUpperCase().toString()] = index
            }

            items.sortBy { priorityMap[it.we] }

            return items
        }

        private fun doRequest(
            context: Context,
            units: List<AdInformation>,
            startIndex: Int = 0,
            callback: ((result: Any?) -> Unit)
        ) {
            val unit = units.getOrNull(startIndex)
            if (unit == null) {
                callback(null)
                return
            }
            printLog("${where},on request: $unit")
            GoogleAds(where).load(context, unit) {
                if (it == null)
                    doRequest(context, units, startIndex + 1, callback)
                else
                    callback(it)
            }
        }

        fun clearCache() {
            res = null
            createdTime = 0L
        }
    }

    private class Show private constructor(private val where: String) {
        companion object {
            private var isShowingFullScreen = false

            fun of(where: String): Show {
                return Show(where)
            }

        }

        fun showFullScreen(
            context: AppCompatActivity,
            res: Any,
            callback: () -> Unit
        ) {
            if (isShowingFullScreen || !context.isVisible()) {
                callback()
                return
            }
            isShowingFullScreen = true
            Log.e(TAG, "showFullScreen: ")
            GoogleAds(where)
                .showFullScreen(
                    context = context,
                    res = res,
                    callback = {
                        isShowingFullScreen = false
                        callback()
                    }
                )
        }
    }

    private class GoogleAds(private val where: String) {
        private class GoogleFullScreenCallback(
            private val where: String,
            private val callback: () -> Unit
        ) : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "${where} ---dismissed")
                onAdComplete()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Log.d(TAG, "${where} ---fail to show, message=${p0.message}")
                onAdComplete()
            }

            private fun onAdComplete() {
                callback()
            }

            override fun onAdShowedFullScreenContent() {
                SmileKey.recordNumberOfAdDisplaysGreen()
                Log.d(TAG, "${where}--showed")

            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "${where}插屏广告点击")
                SmileKey.recordNumberOfAdClickGreen()
            }
        }

        companion object {
            fun init(context: Context, onInitialized: () -> Unit) {
                MobileAds.initialize(context) {
                    onInitialized()
                }
            }

        }

        fun load(
            context: Context,
            unit: AdInformation,
            callback: ((result: Any?) -> Unit)
        ) {
            SmileNetHelp.postPotListData(
                context,
                "oom13",
                "oo",
                "${unit.name}+${unit.id}+${App.top_activity_name}",
                "oo1",
                App.vpnLink.toString()
            )
            val requestContext = context.applicationContext
            when (unit.name) {
                SmileKey.POS_OPEN -> {
                    openAdData.id = unit.id
                    openAdData.type = unit.type
                    openAdData.name = unit.name
                    openAdData = PutDataUtils.beforeLoadLink(openAdData)
                    AppOpenAd.load(
                        requestContext,
                        unit.id,
                        AdRequest.Builder().build(),
                        AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                        object :
                            AppOpenAd.AppOpenAdLoadCallback() {
                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Log.d(TAG, "${where} ---request fail: ${loadAdError.message}")
                                callback(null)
                            }

                            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                                callback(appOpenAd)
                                SmileNetHelp.postPotIntData(
                                    context,
                                    "oom14",
                                    "oo",
                                    "${where}+${unit.id}"
                                )
                                appOpenAd.setOnPaidEventListener { adValue ->
                                    adValue.let {
                                        SmileNetHelp.postAdData(
                                            App.getAppContext(),
                                            adValue,
                                            appOpenAd.responseInfo,
                                            openAdData
                                        )
                                    }
                                    val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
                                    adRevenue.setRevenue(
                                        adValue.valueMicros / 1000000.0,
                                        adValue.currencyCode
                                    )
                                    adRevenue.setAdRevenueNetwork(appOpenAd.responseInfo.mediationAdapterClassName)
                                    Adjust.trackAdRevenue(adRevenue)
                                }
                            }
                        })
                }

                SmileKey.POS_CONNECT, SmileKey.POS_BACK, SmileKey.POS_INT3 -> {
                    if (unit.name == SmileKey.POS_CONNECT) {
                        connectAdData.id = unit.id
                        connectAdData.type = unit.type
                        connectAdData.name = unit.name
                        connectAdData = PutDataUtils.beforeLoadLink(connectAdData)
                    }
                    if (unit.name == SmileKey.POS_BACK) {
                        backAdData.id = unit.id
                        backAdData.type = unit.type
                        backAdData.name = unit.name
                        backAdData = PutDataUtils.beforeLoadLink(backAdData)
                    }
                    if (unit.name == SmileKey.POS_INT3) {
                        int3AdData.id = unit.id
                        int3AdData.type = unit.type
                        int3AdData.name = unit.name
                        int3AdData = PutDataUtils.beforeLoadLink(int3AdData)
                    }
                    InterstitialAd.load(
                        requestContext,
                        unit.id,
                        AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Log.d(TAG, "${where} ---request fail: ${loadAdError.message}")
                                callback(null)
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                SmileNetHelp.postPotIntData(
                                    context,
                                    "oom14",
                                    "oo",
                                    "${where}+${unit.id}"
                                )
                                callback(interstitialAd)
                                interstitialAd.setOnPaidEventListener { adValue ->
                                    val bean = when (unit.name) {
                                        SmileKey.POS_CONNECT -> {
                                            connectAdData
                                        }

                                        SmileKey.POS_BACK -> {
                                            backAdData
                                        }

                                        SmileKey.POS_INT3 -> {
                                            int3AdData
                                        }

                                        else -> {
                                            null
                                        }
                                    }
                                    adValue.let {
                                        bean?.let { it1 ->
                                            SmileNetHelp.postAdData(
                                                App.getAppContext(),
                                                adValue,
                                                interstitialAd.responseInfo,
                                                it1
                                            )
                                        }
                                        val adRevenue =
                                            AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
                                        adRevenue.setRevenue(
                                            adValue.valueMicros / 1000000.0,
                                            adValue.currencyCode
                                        )
                                        adRevenue.setAdRevenueNetwork(interstitialAd.responseInfo.mediationAdapterClassName)
                                        Adjust.trackAdRevenue(adRevenue)
                                    }
                                }
                            }
                        }
                    )
                }

                SmileKey.POS_RE -> {
                    reWardeAdData.id = unit.id
                    reWardeAdData.type = unit.type
                    reWardeAdData.name = unit.name
                    reWardeAdData = PutDataUtils.beforeLoadLink(reWardeAdData)
                    var adRequest = AdRequest.Builder().build()
                    RewardedAd.load(context, unit.id, adRequest, object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            println(adError?.toString())
                            Toast.makeText(
                                context,
                                "激励广告加载失败:${adError?.toString()}",
                                Toast.LENGTH_LONG
                            ).show()
                            callback(null)
                        }

                        override fun onAdLoaded(ad: RewardedAd) {
                            Toast.makeText(
                                context,
                                "激励广告加载成功",
                                Toast.LENGTH_LONG
                            ).show()
                            SmileNetHelp.postPotIntData(
                                context,
                                "oom14",
                                "oo",
                                "${where}+${unit.id}"
                            )
                            ad.setOnPaidEventListener { adValue ->
                                SmileNetHelp.postAdData(
                                    App.getAppContext(),
                                    adValue,
                                    ad.responseInfo,
                                    reWardeAdData
                                )
                                val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
                                adRevenue.setRevenue(
                                    adValue.valueMicros / 1000000.0,
                                    adValue.currencyCode
                                )
                                adRevenue.setAdRevenueNetwork(ad.responseInfo.mediationAdapterClassName)
                                Adjust.trackAdRevenue(adRevenue)
                            }
                            callback(ad)
                        }
                    })
                }

                else -> {
                    callback(null)
                }
            }
        }

        fun showFullScreen(
            context: AppCompatActivity,
            res: Any,
            callback: () -> Unit
        ) {

            when (res) {
                is AppOpenAd -> {
                    res.fullScreenContentCallback = GoogleFullScreenCallback(where, callback)
                    res.show(context)
                    openAdData = PutDataUtils.afterLoadLink(openAdData)
                }

                is InterstitialAd -> {
                    if (!UserConter.showAdCenter()) {
                        callback.invoke()
                        return
                    }
                    if (!UserConter.showAdBlacklist()) {
                        callback.invoke()
                        return
                    }
                    res.fullScreenContentCallback = GoogleFullScreenCallback(where, callback)
                    res.show(context)
                    when (where) {
                        SmileKey.POS_CONNECT -> {
                            connectAdData = PutDataUtils.afterLoadLink(connectAdData)
                        }

                        SmileKey.POS_BACK -> {
                            backAdData = PutDataUtils.afterLoadLink(backAdData)
                        }

                        SmileKey.POS_INT3 -> {
                            int3AdData = PutDataUtils.afterLoadLink(int3AdData)
                        }
                    }
                }

                is RewardedAd -> {
                    res.fullScreenContentCallback = GoogleFullScreenCallback(where, callback)
                    res?.let { ad ->
                        ad.show(context) { rewardItem ->
                            // Handle the reward.
                            val rewardAmount = rewardItem.amount
                            val rewardType = rewardItem.type
                        }
                        reWardeAdData = PutDataUtils.afterLoadLink(reWardeAdData)
                    }

                }

                else -> callback()
            }
        }
    }
}