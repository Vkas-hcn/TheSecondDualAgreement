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
import androidx.appcompat.app.AppCompatActivity
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
import com.fast.open.ss.dual.agreement.utils.SmileUtils.isVisible
import com.fast.open.ss.dual.agreement.utils.UserConter

object SmileAdLoad {
    var isLoadOpenFist = false
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

    fun showNativeOf(
        where: String,
        nativeRoot: View,
        res: Any,
        preload: Boolean = false,
        onShowCompleted: (() -> Unit)? = null
    ) {
        Show.of(where)
            .showNativeOf(
                nativeRoot = nativeRoot,
                res = res,
                callback = {
                    Load.of(where)?.let { load ->
                        load.clearCache()
//                        if (preload) {
//                            load.load()
//                        }
                    }
                    onShowCompleted?.invoke()
                }
            )
    }

    private fun preloadAds() {
        runCatching {
            Load.of(SmileKey.POS_OPEN)?.load()
            Load.of(SmileKey.POS_HOME)?.load()
            Load.of(SmileKey.POS_RESULT)?.load()
            Load.of(SmileKey.POS_CONNECT)?.load()
        }
    }

    private class Load private constructor(private val where: String) {
        companion object {
            private val open by lazy { Load(SmileKey.POS_OPEN) }
            private val home by lazy { Load(SmileKey.POS_HOME) }
            private val connect by lazy { Load(SmileKey.POS_CONNECT) }
            private val back by lazy { Load(SmileKey.POS_BACK) }
            private val result by lazy { Load(SmileKey.POS_RESULT) }

            fun of(where: String): Load? {
                return when (where) {
                    SmileKey.POS_OPEN -> open
                    SmileKey.POS_HOME -> home
                    SmileKey.POS_CONNECT -> connect
                    SmileKey.POS_BACK -> back
                    SmileKey.POS_RESULT -> result
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
            inst: SmileAdBean = SmileKey.getAdJson(),
        ) {
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
                    clearCache()
                } else {
                    printLog("Existing cache")
                    return
                }
            }

            if ((where == SmileKey.POS_BACK || where == SmileKey.POS_CONNECT || where == SmileKey.POS_HOME) && !UserConter.showAdCenter()) {
                Log.e(TAG, "买量屏蔽用户不加载${where}广告")
                res = ""
                return
            }
            if ((where == SmileKey.POS_BACK || where == SmileKey.POS_CONNECT) && !UserConter.showAdBlacklist()) {
                Log.e(TAG, "黑名单用户不加载${where}广告")
                res = ""
                return
            }
            isLoading = true
            printLog("load started")
            doRequest(
                context, when (where) {
                    SmileKey.POS_OPEN -> {
                        inst.open_smile
                    }

                    SmileKey.POS_CONNECT -> inst.connect_smile
                    SmileKey.POS_HOME -> inst.home_smile
                    SmileKey.POS_RESULT -> inst.end_smile
                    SmileKey.POS_BACK -> inst.back_smile
                    else -> ""
                }, where
            ) {
                val isSuccessful = it != null
                printLog("load complete, result=$isSuccessful")
                if (isSuccessful) {
                    res = it
                    createdTime = System.currentTimeMillis()
                }
                isLoading = false
                if (!isSuccessful && where == SmileKey.POS_OPEN && !isLoadOpenFist) {
                    load(context, inst)
                    isLoadOpenFist = true
                }
            }
        }

        private fun doRequest(
            context: Context,
            unit: String,
            way: String,
            callback: ((result: Any?) -> Unit)
        ) {
            if (unit == null) {
                callback(null)
                return
            }
            printLog("on request: $unit")
            GoogleAds(where).load(context, way, unit) {
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

        fun showNativeOf(
            nativeRoot: View,
            res: Any,
            callback: () -> Unit
        ) {
            GoogleAds(where)
                .showNativeOf(
                    nativeRoot = nativeRoot,
                    res = res,
                    callback = callback
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
                Log.d(TAG, "${where} ---在广告完成时")
                callback()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "${where} ---showed")

            }

            override fun onAdClicked() {
                super.onAdClicked()
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
            way: String,
            unit: String,
            callback: ((result: Any?) -> Unit)
        ) {
            val requestContext = context.applicationContext
            when (way) {
                SmileKey.POS_OPEN -> {
                    AppOpenAd.load(
                        requestContext,
                        unit,
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

                            }
                        })
                }

                SmileKey.POS_CONNECT, SmileKey.POS_BACK -> {

                    InterstitialAd.load(
                        requestContext,
                        unit,
                        AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Log.d(TAG, "${where} ---request fail: ${loadAdError.message}")
                                callback(null)
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                callback(interstitialAd)

                            }
                        }
                    )
                }

                SmileKey.POS_HOME, SmileKey.POS_RESULT -> {

                    AdLoader.Builder(requestContext, unit)
                        .forNativeAd {
                            callback(it)
                            it.setOnPaidEventListener { adValue ->
                                loadOf(where)
                            }
                        }
                        .withAdListener(object : AdListener() {
                            override fun onAdOpened() {
                                super.onAdOpened()
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Log.d(TAG, "${where} ---request fail: ${loadAdError.message}")
                                callback(null)
                            }

                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                Log.d(TAG, "${where} ---原生广告加载成功")
                            }
                        })
                        .withNativeAdOptions(
                            NativeAdOptions.Builder()
                                .setAdChoicesPlacement(
                                    when (where) {
                                        SmileKey.POS_HOME -> NativeAdOptions.ADCHOICES_TOP_RIGHT
                                        SmileKey.POS_RESULT -> NativeAdOptions.ADCHOICES_TOP_RIGHT
                                        else -> NativeAdOptions.ADCHOICES_BOTTOM_LEFT
                                    }
                                )
                                .build()
                        )
                        .build()
                        .loadAd(AdRequest.Builder().build())
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
                }

                is InterstitialAd -> {
                    if (!UserConter.showAdCenter()) {
                        Log.e(TAG, "根据买量屏蔽插屏广告。。。")
                        callback.invoke()
                        return
                    }
                    if (!UserConter.showAdBlacklist()) {
                        Log.e(TAG, "根据黑名单屏蔽插屏广告。。。")
                        callback.invoke()
                        return
                    }

                    res.fullScreenContentCallback = GoogleFullScreenCallback(where, callback)
                    res.show(context)

                }

                else -> callback()
            }
        }

        fun showNativeOf(
            nativeRoot: View,
            res: Any,
            callback: () -> Unit
        ) {
            val nativeAd = res as? NativeAd ?: return
            if (where == SmileKey.POS_HOME) {
                if (!UserConter.showAdCenter()) {
                    Log.e(TAG, "根据买量屏蔽home广告。。。")
                    return
                }
            }
            nativeRoot.findViewById<View>(R.id.img_ad_type)?.visibility = View.GONE
            val nativeAdView =
                nativeRoot.findViewById<NativeAdView>(R.id.native_ad_view) ?: return
            nativeAdView.visibility = View.VISIBLE
            nativeRoot.findViewById<MediaView>(R.id.ad_media)?.let { mediaView ->
                nativeAdView.mediaView = mediaView
                nativeAd.mediaContent?.let { mediaContent ->
                    mediaView.setMediaContent(mediaContent)
                    mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                }
            }

            nativeRoot.findViewById<TextView>(R.id.ad_headline)?.let { titleView ->
                nativeAdView.headlineView = titleView
                titleView.text = nativeAd.headline
            }

            nativeRoot.findViewById<TextView>(R.id.ad_body)?.let { bodyView ->
                nativeAdView.bodyView = bodyView
                bodyView.text = nativeAd.body
            }

            nativeRoot.findViewById<TextView>(R.id.ad_call_to_action)?.let { actionView ->
                nativeAdView.callToActionView = actionView
                actionView.text = nativeAd.callToAction
            }

            nativeRoot.findViewById<ImageView>(R.id.ad_app_icon)?.let { iconView ->
                nativeAdView.iconView = iconView
                iconView.setImageDrawable(nativeAd.icon?.drawable)
            }
            Log.d(TAG, "${where} ---showed")
            nativeAdView.setNativeAd(nativeAd)
            callback()
        }
    }
}