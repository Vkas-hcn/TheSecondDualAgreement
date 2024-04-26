package com.fast.open.ss.dual.agreement.utils

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.bean.ScreenMetrics
import com.fast.open.ss.dual.agreement.bean.VpnServiceBean
import com.github.shadowsocks.database.Profile
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.math.BigDecimal


object SmileUtils {
    fun String.getSmileImage(): Int {
        return when (this) {
            "United States" -> R.drawable.unitedstates
            "Australia" -> R.drawable.australia
            "Canada" -> R.drawable.canada
            "China" -> R.drawable.canada
            "France" -> R.drawable.france
            "Germany" -> R.drawable.germany
            "Hong Kong" -> R.drawable.hongkong
            "India" -> R.drawable.india
            "Japan" -> R.drawable.japan
            "koreasouth" -> R.drawable.koreasouth
            "Singapore" -> R.drawable.singapore
            "Taiwan" -> R.drawable.taiwan
            "Brazil" -> R.drawable.brazil
            "United Kingdom" -> R.drawable.unitedkingdom
            "India" -> R.drawable.india
            "Nether Lands" -> R.drawable.netherlands
            else -> R.drawable.fast
        }
    }

    fun setSkServerData(profile: Profile, bestData: VpnServiceBean): Profile {
        SmileKey.vpn_city = bestData.city
        SmileKey.vpn_ip = bestData.ip
        profile.name = bestData.country_name + "-" + bestData.city
        profile.host = bestData.ip
        profile.password = bestData.user_pwd
        profile.method = bestData.mode
        profile.remotePort = bestData.port
        return profile
    }

    fun rotateImageViewInfinite(imageView: ImageView, duration: Long) {
        val rotateAnimation = RotateAnimation(
            0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = duration
        rotateAnimation.repeatCount = Animation.INFINITE
        rotateAnimation.repeatMode = Animation.RESTART

        imageView.startAnimation(rotateAnimation)
    }

    fun stopRotation(imageView: ImageView) {
        imageView.clearAnimation()
    }

    fun isAppOnline(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (networkCapabilities != null) {
            return (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) || networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ))
        }
        return false
    }

    fun AppCompatActivity.isVisible(): Boolean {
        return lifecycle.currentState == Lifecycle.State.RESUMED
    }


    fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager =
            context.getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses ?: return false
        for (processInfo in runningProcesses) {
            if (processInfo.processName == packageName) {
                return true
            }
        }
        return false
    }

    fun getScreenMetrics(context: Context): ScreenMetrics {
        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val dpi = metrics.densityDpi

        return ScreenMetrics(width, height, dpi)
    }


    fun resizeView(view: View, newWidth: Int, newHeight: Int) {
        view.post {
            val width = view.width
            val height = view.height

            val aspectRatio = width.toFloat() / height.toFloat()

            val layoutParams = view.layoutParams

            when {
                newWidth > 0 -> {
                    layoutParams.width = newWidth
                    layoutParams.height = (newWidth / aspectRatio).toInt()
                }

                newHeight > 0 -> {
                    layoutParams.width = (newHeight * aspectRatio).toInt()
                    layoutParams.height = newHeight
                }
            }

            view.layoutParams = layoutParams
        }
    }


    fun decodeTheData(responseString: String): String {
        val trimmedString = responseString.substring(14)
        val reversedString = trimmedString.reversed()
        return String(Base64.decode(reversedString?.toByteArray(), Base64.DEFAULT))
    }

    fun rotateImageView(imageView: ImageView, duration: Long = 1000L) {
        val rotationAnimator = ObjectAnimator.ofFloat(imageView, View.ROTATION, 0f, 360f)
        rotationAnimator.duration = duration
        rotationAnimator.repeatCount = ObjectAnimator.INFINITE
        rotationAnimator.start()
    }

    fun haveMoreTime(nextFun1: () -> Unit, nextFun2: () -> Unit) {
        if (App.reConnectTime >= 1000 * 60) {
            nextFun1()
        } else {
            nextFun2()
        }
    }

    fun CoroutineScope.internetSpeedDetection(block: () -> Unit) {
        launch(Dispatchers.IO) {
            var finishStateDow = false
            var finishStateUP = false
            var finishStateDowNum = BigDecimal(0)
            var finishStateUpNum = BigDecimal(0)
            val speedTestSocket = SpeedTestSocket()
            SmileKey.dowLoadSpeed = "0"
            SmileKey.upLoadSpeed = "0"
            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport) {
                    SmileKey.dowLoadSpeed = unitConversion(report.transferRateBit)
                    Log.e("TAG", "下载-完成- bit/s   : " + SmileKey.dowLoadSpeed)
                    finishStateDow = true
                    if (finishStateUP ) {
                        block()
                    }
                }

                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    Log.e("TAG", "下载-onError: errorMessage=$errorMessage")
//                    if(SmileKey.dowLoadSpeed == "0"){
//                        onErrorAction()
//                    }
                }

                @SuppressLint("LogNotTimber")
                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    Log.e("TAG", "下载-progress : $percent%")
                    Log.e(
                        "TAG",
                        "下载-rate in bit/s   : " + unitConversion(report.transferRateBit)
                    )
                    val num = if (report.transferRateBit > finishStateDowNum) {
                        finishStateDowNum = report.transferRateBit
                        finishStateDowNum
                    } else {
                        report.transferRateBit
                    }
                    SmileKey.dowLoadSpeed = unitConversion(num)
                }
            })
            speedTestSocket.downloadSetupTime = 10000
            speedTestSocket.startDownload("https://speed.cloudflare.com/__down?during=download&bytes=5000000")
            withTimeoutOrNull(7000) {
                while (finishStateDow.not()) delay(400)
            }
            val speedTestSocket1 = SpeedTestSocket()
            speedTestSocket1.addSpeedTestListener(object : ISpeedTestListener {
                @SuppressLint("LogNotTimber")
                override fun onCompletion(report: SpeedTestReport) {
                    Log.e("TAG", "上传-完成-: " + unitConversion(report.transferRateBit))
                    SmileKey.upLoadSpeed = unitConversion(report.transferRateBit)
                    finishStateUP = true
                    if (finishStateDow && SmileKey.upLoadSpeed == "0") {
                        block()
                    }
                }

                @SuppressLint("LogNotTimber")
                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    Log.e("TAG", "上传-Error=$errorMessage")
//                    if(SmileKey.upLoadSpeed == "0"){
//                        onErrorAction()
//                    }
                }

                @SuppressLint("LogNotTimber")
                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    Log.e("TAG", "上传-progress : $percent%")
                    Log.e(
                        "TAG",
                        "上传-rate in bit/s   : " + unitConversion(report.transferRateBit)
                    )
                    val num = if (report.transferRateBit > finishStateUpNum) {
                        finishStateUpNum = report.transferRateBit
                        finishStateUpNum
                    } else {
                        report.transferRateBit
                    }
                    SmileKey.upLoadSpeed = unitConversion(num)
                }
            })
            speedTestSocket1.startUpload("https://speed.cloudflare.com/__down?during=download&bytes=5000000", 5000000)
        }

    }

    fun unitConversion(transferRateBit: BigDecimal): String {
        val transferRateMegaBitPerSecond: BigDecimal = transferRateBit.divide(BigDecimal("1000000"))
        return transferRateMegaBitPerSecond.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "m/s"
    }

    private fun getPingTime(serverAddress: String): Int {
        val command = "ping -c 4 $serverAddress"
        val process = Runtime.getRuntime().exec(command)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var pingTime = -1
        while (true) {
            val line = reader.readLine() ?: break
            if (line.contains("time=")) {
                // 从输出中提取 ping 值
                val startIndex = line.indexOf("time=")
                val endIndex = line.indexOf(" ms", startIndex)
                if (startIndex != -1 && endIndex != -1) {
                    pingTime = try {
                        val pingValue = line.substring(startIndex + 5, endIndex)
                        pingValue.toInt()
                    } catch (e: NumberFormatException) {
                        -1
                    }
                    break
                }
            }
        }
        reader.close()
        return pingTime
    }

    fun pingServiceIp() {
        val serverAddress = SmileKey.vpn_ip
        Log.e("TAG", "pingServiceIp: $serverAddress")
        SmileKey.pingSpeed = "timeOut"
        try {
            val pingTime = getPingTime(serverAddress)
            if (pingTime != -1) {
                Log.e("TAG", "Ping data =：" + pingTime + "ms")
                SmileKey.pingSpeed = "$pingTime ms"
            } else {
                Log.e("TAG", "Unable to get ping.")
                SmileKey.pingSpeed = "timeOut"
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}