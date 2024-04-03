package com.fast.open.ss.dual.agreement.utils

import android.util.Log
import com.fast.open.ss.dual.agreement.app.App
import com.github.shadowsocks.Core
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DecimalFormat

import kotlinx.coroutines.*

object TimeData {
    var skTime = App.reConnectTime
    private var isCounting = false
    private var startTime = 0L
    private var pausedTime = 0L
    private var timerJob: Job? = null
    fun sendTimerInformation() {
        timerJob?.cancel()
        timerJob = null
        timerJob = GlobalScope.launch {
            while (isActive && skTime > 0) {
                delay(1000)
                if (isCounting) {
                    skTime =
                        (App.reConnectTime - ((System.currentTimeMillis() - startTime) / 1000)).toInt()
                    if (skTime <= 0) {
                        Core.stopService()
                        skTime = 0
                        isCounting = false
                    }
                }
            }
        }
    }

    fun startCountdown() {
        isCounting = true
        if (skTime == App.reConnectTime) {
            startTime = System.currentTimeMillis()
        } else {
            startTime += System.currentTimeMillis() - pausedTime
        }
    }

    fun pauseCountdown() {
        isCounting = false
        pausedTime = System.currentTimeMillis()
    }

    fun resetCountdown() {
        isCounting = false
        timerJob?.cancel()
        skTime = SmileKey.reConnectTime
    }

     fun formatTime(timerData: Int): String {
        val hh: String = DecimalFormat("00").format(timerData / 3600)
        val mm: String = DecimalFormat("00").format(timerData % 3600 / 60)
        val ss: String = DecimalFormat("00").format(timerData % 60)
        return "$hh:$mm:$ss"
    }


    fun getTiming(): String {
        return formatTime(skTime)
    }

    fun getTimingDialog(time:Int): String {
        return formatTime(skTime+time)
    }
}
