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
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.fast.open.ss.dual.agreement.utils.SmileNetHelp
import com.fast.open.ss.dual.agreement.utils.SmileUtils
import kotlinx.coroutines.Dispatchers
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

    override fun initData() {
        showOpenFun()
        countDown()

        viewModel.toMainLive.observe(this) {
            startActivityFirst<MainActivity>()
            finish()
        }
        lifecycleScope.launch {
            SmileNetHelp.getLoadIp()
            SmileNetHelp.getLoadOthIp()
            SmileNetHelp.getBlackData(this@FirstActivity)
        }
        SmileAdLoad.init(this)
        viewModel.getFileBaseData(this) {
            SmileAdLoad.isLoadOpenFist = false
            SmileAdLoad.loadOf(SmileKey.POS_OPEN)
            loadOpenAd()
        }

    }

    private fun countDown() {
        lifecycleScope.launch {
            //跳转到主页
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return true
    }
}