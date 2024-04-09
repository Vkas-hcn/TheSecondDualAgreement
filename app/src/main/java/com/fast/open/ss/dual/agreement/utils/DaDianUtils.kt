package com.fast.open.ss.dual.agreement.utils

import android.content.Context
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.ui.main.MainActivity

object DaDianUtils {
    fun oom1(context: Context, response: String) {
        if (response == "chintz") {
            SmileNetHelp.postPotIntData(context, "oom1")
        }
    }

    fun oom2(context: Context) {
        val data = UserConter.isItABuyingUser()
        if (data && SmileKey.isMlState != "1") {
            SmileNetHelp.postPotIntData(context, "oom2")
            SmileKey.isMlState = "1"
        }
    }

    fun oom3(context: Context) {
        SmileNetHelp.postPotIntData(context, "oom3")
    }

    fun oom4(context: Context) {
        SmileNetHelp.postPotIntData(context, "oom4")
    }

    fun oom5(activity: MainActivity) {
        if (activity.binding.agreement == "1") {
            SmileNetHelp.postPotIntData(activity, "oom5", "oo", "open")
        } else {
            SmileNetHelp.postPotIntData(activity, "oom5", "oo", "ss")
        }
    }

    fun oom21(context: Context, isPop: Boolean) {
        val pop = if (isPop) {
            "pop"
        } else {
            "page"
        }
        val isIn3 = SmileAdLoad.resultOf(SmileKey.POS_INT3) != null
        SmileNetHelp.postPotListData(
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
        SmileNetHelp.postPotListData(
            context, "oom22", "oo", pop, "oo1",
            App.top_activity_name, "oo2", re.toString()
        )
    }

    fun oom23(context: Context) {
        val isIn3 = SmileAdLoad.resultOf(SmileKey.POS_INT3) != null
        SmileNetHelp.postPotListData(
            context,
            "oom23",
            "oo",
            App.top_activity_name,
            "oo1",
            isIn3.toString()
        )
    }

    fun oom24(context: Context) {
        SmileNetHelp.postPotListData(
            context,
            "oom24",
            "oo",
            App.top_activity_name,
        )
    }

    fun oom25(context: Context) {
        SmileNetHelp.postPotIntData(context, "oom25")
    }

    fun oom26(context: Context) {
        SmileNetHelp.postPotIntData(context, "oom26")
    }
}