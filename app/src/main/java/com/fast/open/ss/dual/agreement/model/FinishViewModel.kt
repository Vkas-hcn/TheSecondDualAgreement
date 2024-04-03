package com.fast.open.ss.dual.agreement.model

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.ui.finish.FinishActivity
import com.fast.open.ss.dual.agreement.ui.main.MainActivity
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.fast.open.ss.dual.agreement.utils.SmileUtils
import com.fast.open.ss.dual.agreement.utils.SmileUtils.isVisible
import com.fast.open.ss.dual.agreement.utils.TimeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.DecimalFormat

class FinishViewModel : ViewModel() {
    var jobRewardedSmile: Job? = null

    fun returnToHomePage(activity: FinishActivity) {
        val res = SmileAdLoad.resultOf(SmileKey.POS_BACK)
        if (res == null) {
            activity.finish()
        } else {
            showBackFun(res, activity)
        }
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

    fun showInt3Fun(addNum: Int, it: Any, activity: FinishActivity, isDialogAd: Boolean = false) {
        SmileAdLoad.showFullScreenOf(
            where = SmileKey.POS_INT3,
            context = activity,
            res = it,
            preload = true,
            onShowCompleted = {
                if (!isDialogAd) {
                    activity.lifecycleScope.launch(Dispatchers.Main) {
                        addTimeSuccess(addNum, activity)
                    }
                } else {
                    activity.binding.showAddTime = false
                }
            }
        )
    }

    suspend fun addTimeSuccess(time: Int, activity: FinishActivity) {
        Log.e(TAG, "addTimeSuccess-fin: ${TimeData.userTime}")
        delay(300)
        if (activity.isVisible()) {
            App.reConnectTime = App.reConnectTime + time
            activity.binding.tvAddTime.text = if (time >= 60 * 60) {
                "+60mins"
            } else {
                "+30mins"
            }
            activity.binding.tvTimeTip.text =
                "Your remaining duration is ${TimeData.getTimingDialog(time)}"
            activity.binding.showAddSuccess = true
            activity.binding.showAddTime = false
            SmileAdLoad.loadOf(SmileKey.POS_RE)
        }

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

    fun loadSmileRewarded(activity: FinishActivity) {
        jobRewardedSmile = activity.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withTimeout(12000) {
                    activity.binding.showLoading = true
                    SmileUtils.rotateImageView(activity.binding.imgLoad)
                    delay(1000L)
                    while (isActive) {
                        if (SmileAdLoad.resultOf(SmileKey.POS_RE) != null) {
                            SmileAdLoad.resultOf(SmileKey.POS_RE)
                                ?.let { showRewardedFun(activity, it) }
                            cancel()
                            jobRewardedSmile?.cancel()
                            jobRewardedSmile = null
                        }
                        delay(500L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                activity.binding.showLoading = false
                addTimeSuccess(60 * 60, activity)
            }
        }
    }

    private fun showRewardedFun(activity: FinishActivity, it: Any) {
        SmileAdLoad.showFullScreenOf(
            where = SmileKey.POS_RE,
            context = activity,
            res = it,
            preload = true,
            onShowCompleted = {
                activity.lifecycleScope.launch {
                    activity.binding.showLoading = false
                    addTimeSuccess(60 * 60, activity)
                }
            }
        )
    }
}