package com.fast.open.ss.dual.agreement.ui.first

import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.base.BaseActivity
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.databinding.ActivityFirstBinding
import com.fast.open.ss.dual.agreement.model.FirstViewModel
import com.fast.open.ss.dual.agreement.ui.main.MainActivity
import com.fast.open.ss.dual.agreement.utils.DaDianUtils
import com.fast.open.ss.dual.agreement.utils.PutDataUtils
import com.fast.open.ss.dual.agreement.utils.SmileData
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.fast.open.ss.dual.agreement.utils.SmileNetHelp
import com.fast.open.ss.dual.agreement.utils.SmileUtils
import com.fast.open.ss.dual.agreement.utils.UserConter.isItABuyingUser
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class FirstActivity : BaseActivity<ActivityFirstBinding, FirstViewModel>(
    R.layout.activity_first, FirstViewModel::class.java
) {
    override fun intiView() {

    }

    private var jobOpenSmile: Job? = null
    var count = 0
    private lateinit var consentInformation: ConsentInformation
    override fun initData() {
        showOpenFun()
        countDown()
        updateUserOpinions()
        viewModel.toMainLive.observe(this) {
            startActivityFirst<MainActivity>()
            finish()
        }
        lifecycleScope.launch {
            SmileNetHelp.getOnlineSmData(this@FirstActivity)
            SmileNetHelp.getLoadIp()
            SmileNetHelp.getLoadOthIp()
            SmileNetHelp.getBlackData(this@FirstActivity)
            SmileNetHelp.postSessionData(this@FirstActivity)
            SmileNetHelp.getOnlyIp()
        }
        viewModel.getFileBaseData(this) {
            SmileAdLoad.isLoadOpenFist = false
            SmileAdLoad.init(this)
            waitForTheOpenAdToAppear()
            DaDianUtils.oom2(this)
        }
    }

    private fun countDown() {
        lifecycleScope.launch {
            while (true) {
                count += 1
                binding.progressBarFirst.progress = count
                if (count >= 100) {
                    cancel()
                    break
                }
                delay(120)
            }
        }
    }


    private fun loadOpenAd() {
        SmileKey.isAppGreenSameDayGreen()
        if (SmileKey.isThresholdReached()) {
            Log.d("TAG", "广告达到上线")
            viewModel.toMainLive.postValue("")
            return
        }
        jobOpenSmile?.cancel()
        jobOpenSmile = null
        jobOpenSmile = lifecycleScope.launch {
            try {
                withTimeout(10000L) {
                    while (isActive) {
                        SmileAdLoad.resultOf(SmileKey.POS_OPEN)?.let { res ->
                            viewModel.showOpen.value = res
                            cancel()
                            jobOpenSmile = null
                            count = 100
                            binding.progressBarFirst.progress = count
                        }
                        delay(500L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                cancel()
                jobOpenSmile = null
                count = 100
                binding.progressBarFirst.progress = count
                viewModel.toMainLive.postValue("")
            }
        }
    }

    private fun waitForTheOpenAdToAppear() {
        GlobalScope.launch {
            while (isActive) {
                if (SmileKey.ump_data_dialog) {
                    loadOpenAd()
                    cancel()
                }
                delay(500)
            }
        }
    }

    private fun showOpenFun() {
        viewModel.showOpen.observe(this) {
            SmileAdLoad.showFullScreenOf(
                where = SmileKey.POS_OPEN,
                context = this,
                res = it,
                onShowCompleted = {
                    lifecycleScope.launch(Dispatchers.Main) {
                        viewModel.toMainLive.postValue("")

                    }
                }
            )
        }
    }

    private fun updateUserOpinions() {
        if (SmileKey.ump_data_dialog) {
            return
        }

        val debugSettings =
            ConsentDebugSettings.Builder(this)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("AC2561437987A1BF036B1ADB0A89BDB4")
                .build()
        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params, {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { loadAndShowError ->
                    if (consentInformation.canRequestAds()) {
                        SmileKey.ump_data_dialog = true
                    }
                }
            },
            {
                SmileKey.ump_data_dialog = true
            }
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return true
    }
}