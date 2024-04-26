package com.fast.open.ss.dual.agreement.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object DaDianUtils {
    fun oom1(context: Context, response: String) {
        if (response == "chintz") {
            SmileNetHelp.postPotNet(context, "oom1")
        }
    }

    fun oom2(context: Context) {
        val data = UserConter.isItABuyingUser()
        if (data && SmileKey.isMlState != "1") {
            SmileNetHelp.postPotNet(context, "oom2")
            SmileKey.isMlState = "1"
        }
    }

    fun oom3(context: Context) {
        SmileNetHelp.postPotNet(context, "oom3")
    }

    fun oom4(context: Context) {
        SmileNetHelp.postPotNet(context, "oom4")
    }

    fun oom5(activity: MainActivity) {
        val type = if (SmileNetHelp.isVpnConnected(activity)) {
            "s"
        } else {
            "f"
        }
        if (activity.binding.agreement == "1") {
            SmileNetHelp.postPotNet(activity, "oom5", "oo", "open", "oo1", type)
        } else {
            SmileNetHelp.postPotNet(activity, "oom5", "oo", "ss", "oo1", type)
        }
    }

    @SuppressLint("LogNotTimber")
    fun oom9(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            Log.e(TAG, "o12Fun: 开始检测")
            val isHaveConnectAd = if (SmileAdLoad.resultOf(SmileKey.POS_CONNECT) != null) {
                "true"
            } else {
                "false"
            }
            val netState = SmileNetHelp.isNetworkReachable()
            if (netState) {
                Log.e(TAG, "o12Fun: 开始检测-1")
                SmileNetHelp.postPotNet(context, "oom9", "oo", isHaveConnectAd, "oo1", "1")
            } else {
                Log.e(TAG, "o12Fun: 开始检测-2")
                SmileNetHelp.postPotNet(context, "oom9", "oo", isHaveConnectAd, "oo1", "2")
            }

        }
    }

    fun oom21(context: Context, isPop: Boolean) {
        val pop = if (isPop) {
            "pop"
        } else {
            "page"
        }
        val isIn3 = SmileAdLoad.resultOf(SmileKey.POS_INT3) != null
        SmileNetHelp.postPotNet(
            context, "oom21", "oo", pop, "oo1",
            App.top_activity_name, "oo2", isIn3.toString()
        )
    }

    fun oom22(context: Context, isPop: Boolean) {
        val pop = if (isPop) {
            "pop"
        } else {
            "page"
        }
        val re = SmileAdLoad.resultOf(SmileKey.POS_RE) != null
        SmileNetHelp.postPotNet(
            context, "oom22", "oo", pop, "oo1",
            App.top_activity_name, "oo2", re.toString()
        )
    }

    fun oom23(context: Context) {
        val isIn3 = SmileAdLoad.resultOf(SmileKey.POS_INT3) != null
        SmileNetHelp.postPotNet(
            context,
            "oom23",
            "oo",
            App.top_activity_name,
            "oo1",
            isIn3.toString()
        )
    }

    fun oom24(context: Context) {
        SmileNetHelp.postPotNet(
            context,
            "oom24",
            "oo",
            App.top_activity_name,
        )
    }

    fun oom25(context: Context) {
        SmileNetHelp.postPotNet(context, "oom25")
    }

    fun oom26(context: Context) {
        SmileNetHelp.postPotNet(context, "oom26")
    }
}