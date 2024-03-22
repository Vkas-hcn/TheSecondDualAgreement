package com.fast.open.ss.dual.agreement.model

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.ui.finish.FinishActivity
import com.fast.open.ss.dual.agreement.utils.SmileKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FinishViewModel : ViewModel() {

    fun returnToHomePage(activity: FinishActivity) {
        val res = SmileAdLoad.resultOf(SmileKey.POS_BACK)
        if (res == null) {
            activity.finish()
        } else {
            showBackFun(res, activity)
        }
    }

    fun showEndAd(activity: FinishActivity) {
        activity.lifecycleScope.launch {
            SmileAdLoad.loadOf(SmileKey.POS_RESULT)
            delay(300)
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            activity.binding.nativeAdView.visibility = android.view.View.GONE
            activity.binding.imgAdType.visibility = android.view.View.VISIBLE
            while (isActive) {
                val res = SmileAdLoad.resultOf(SmileKey.POS_RESULT)
                if (res != null) {
                    Log.e(TAG, "showEndAd: 1")
                    activity.binding.nativeAdView.visibility = android.view.View.VISIBLE
                    showResultNativeAd(res, activity)
                    cancel()
                    break
                } else {
                    Log.e(TAG, "showEndAd: 2")
                }
                delay(500)
            }
        }
    }

    private fun showResultNativeAd(res: Any, activity: FinishActivity) {
        SmileAdLoad.showNativeOf(
            where = SmileKey.POS_RESULT,
            nativeRoot = activity.binding.nativeAdView,
            res = res,
            preload = true,
            onShowCompleted = {
            }
        )
    }

    private fun showBackFun(it: Any, activity: FinishActivity) {
        SmileAdLoad.showFullScreenOf(
            where = SmileKey.POS_BACK,
            context = activity,
            res = it,
            onShowCompleted = {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    activity.finish()
                }
            }
        )
    }

    fun getSpeedData(activity: FinishActivity) {
        activity.lifecycleScope.launch {
            while (isActive) {
                val speed_dow = App.mmkvSmile.decodeString("speed_dow", "0 B")
                val speed_up = App.mmkvSmile.decodeString("speed_up", "0 B")
                activity.binding.tvSpeedDownload.text = speed_dow
                activity.binding.tvSpeedUpload.text = speed_up
                delay(500)
            }
        }
    }
}