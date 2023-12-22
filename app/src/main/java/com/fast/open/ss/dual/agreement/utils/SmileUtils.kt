package com.fast.open.ss.dual.agreement.utils

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.bean.ScreenMetrics
import com.fast.open.ss.dual.agreement.bean.VpnServiceBean
import com.github.shadowsocks.database.Profile

object SmileUtils {
    fun String.getSmileImage():  Int {
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
        profile.name = bestData.blocuss + "-" + bestData.blogono
        profile.host = bestData.bloally
        profile.password = bestData.blowrite
        profile.method = bestData.bloira
        profile.remotePort = bestData.blodis.toInt()
        return profile
    }
    fun rotateImageViewInfinite(imageView: ImageView, duration: Long) {
        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
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
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
        return false
    }
    fun AppCompatActivity.isVisible(): Boolean {
        return lifecycle.currentState == Lifecycle.State.RESUMED
    }


    fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager
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


    /**
     * 重设 View 的宽高，保持宽高比。
     *
     * @param view 要调整的 View
     * @param newWidth 新的宽度，如果不改变则传入 -1
     * @param newHeight 新的高度，如果不改变则传入 -1
     */
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

}