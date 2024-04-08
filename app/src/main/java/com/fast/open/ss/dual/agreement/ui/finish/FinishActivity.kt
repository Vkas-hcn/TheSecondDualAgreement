package com.fast.open.ss.dual.agreement.ui.finish

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ContextParams
import android.content.ContextWrapper
import android.text.format.Formatter
import android.util.Log
import android.view.ContentInfo
import android.view.KeyEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.base.BaseActivity
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.bean.VpnServiceBean
import com.fast.open.ss.dual.agreement.databinding.ActivityFinishBinding
import com.fast.open.ss.dual.agreement.model.FinishViewModel
import com.fast.open.ss.dual.agreement.ui.main.MainActivity
import com.fast.open.ss.dual.agreement.utils.DaDianUtils
import com.fast.open.ss.dual.agreement.utils.SmileData
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.fast.open.ss.dual.agreement.utils.SmileNetHelp
import com.fast.open.ss.dual.agreement.utils.SmileUtils
import com.fast.open.ss.dual.agreement.utils.TimeData
import com.fast.open.ss.dual.agreement.utils.TimeUtils
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class FinishActivity : BaseActivity<ActivityFinishBinding, FinishViewModel>(
    R.layout.activity_finish, FinishViewModel::class.java
), TimeUtils.TimeUtilsListener {
    private lateinit var vpnServiceBean: VpnServiceBean
    private var isConnect: Boolean = false
    lateinit var timeUtils: TimeUtils
    var jobEndInt: Job? = null

    override fun intiView() {
        val bundle = intent.extras
        vpnServiceBean = Gson().fromJson(
            bundle?.getString(SmileKey.cuSmileConnected),
            object : TypeToken<VpnServiceBean?>() {}.type
        )
        isConnect = bundle?.getBoolean(SmileKey.isSmileConnected) ?: false
        binding.imgBack.setOnClickListener {
            viewModel.returnToHomePage(this)
        }
        timeUtils = TimeUtils().apply {
            setListener(this@FinishActivity)
        }
        timeUtils.startTimingEnd()
        SmileAdLoad.loadOf(SmileKey.POS_INT3)
        onBackPressedDispatcher.addCallback(this) {
            if (binding.showAddTime == true || binding.showAddSuccess == true || binding.showLoading == true) {
                return@addCallback
            } else {
                viewModel.returnToHomePage(this@FinishActivity)
            }
        }
        val connectState = SmileAdLoad.resultOf(SmileKey.POS_CONNECT) != null
        val vpnState = if (App.vpnLink) {
            "cont"
        } else {
            "dis"
        }
        SmileNetHelp.postPotListData(this, "oom11", "oo", connectState.toString(), "oo2", vpnState)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        SmileUtils.haveMoreTime({
            Toast.makeText(this@FinishActivity, "Usage limit reached today", Toast.LENGTH_SHORT)
                .show()
        }, {
            lifecycleScope.launch {
                SmileAdLoad.loadOf(SmileKey.POS_RE)
                delay(300)
                binding.showAddTime = App.vpnLink
                if(binding.showAddTime == true){
                    SmileNetHelp.postPotIntData(this@FinishActivity,"oom20","oo",App.top_activity_name)
                }
            }
        })
        if (App.vpnLink) {
            binding.serviceState = "2"
            binding.tvTitle.text = "Connected succeed"
            viewModel.getSpeedData(this)
            SmileAdLoad.loadOf(SmileKey.POS_RE)
        } else {
            binding.serviceState = "1"
            binding.tvTitle.text = "VPN disconnect"
        }
        binding.tvEnd30.setOnClickListener {
            DaDianUtils.oom21(this@FinishActivity,false)
            SmileUtils.haveMoreTime({
                Toast.makeText(this, "Usage limit reached today", Toast.LENGTH_SHORT).show()
            }, {
                clickAddTimeFun(true)
            })
        }
        binding.tvEnd60.setOnClickListener {
            DaDianUtils.oom22(this@FinishActivity,false)
            SmileUtils.haveMoreTime({
                Toast.makeText(this, "Usage limit reached today", Toast.LENGTH_SHORT).show()
            }, {
                clickAddTimeFun(false)
            })
        }

        binding.imgAddSuccessX.setOnClickListener {
            DaDianUtils.oom25(this)
            clickNextFun()
        }

        binding.tvMain30Dialog.setOnClickListener {
            DaDianUtils.oom21(this@FinishActivity,true)
            clickAddTimeFun(true)
        }
        binding.tvMain60Dialog.setOnClickListener {
            DaDianUtils.oom22(this@FinishActivity,true)
            clickAddTimeFun(false)
        }
        binding.imgAddX.setOnClickListener {
            DaDianUtils.oom23(this@FinishActivity)
            lifecycleScope.launch {
                loadSmileInt3(0, true)
            }
        }
        binding.tvEndGo.setOnClickListener {
            DaDianUtils.oom26(this)
            clickNextFun()
        }
    }

    private fun clickNextFun() {
        binding.showAddSuccess = false
        if (App.add30Num <= 0) {
            startCountdown()
        }
    }

    private fun clickAddTimeFun(isInt3: Boolean) {
        jobEndInt?.cancel()
        jobEndInt = null
        jobEndInt = lifecycleScope.launch {
            delay(100)
            if (binding.showLoading == true) {
                return@launch
            }
            if (isInt3) {
                if (App.add30Num > 0) {
                    loadSmileInt3(30 * 60)
                    App.add30Num--
                }
            } else {
                SmileAdLoad.loadOf(SmileKey.POS_RE)
                viewModel.loadSmileRewarded(this@FinishActivity)
            }
        }
    }

    private fun timeShowDetailAd(showAdFun: () -> Unit, nextFun: () -> Unit) {
        val num = SmileKey.getFlowJson().adNum.toIntOrNull()
        var loadNum = SmileKey.local_addNum
        if (num == null) {
            nextFun()
            return
        }
        if (num != 0 && loadNum <= 0) {
            loadNum = num ?: 0
            SmileKey.local_addNum = loadNum
            showAdFun()
            return
        }
        if (loadNum > 0) {
            loadNum--
            SmileKey.local_addNum = loadNum
            nextFun()
            return
        }
        showAdFun()
    }

    private suspend fun loadSmileInt3(addNum: Int, isDialogAd: Boolean = false) {
        binding.showLoading = true
        SmileUtils.rotateImageView(binding.imgLoad)
        timeShowDetailAd({
            jobEndInt = lifecycleScope.launch {
                SmileAdLoad.loadOf(SmileKey.POS_INT3)
                try {
                    withTimeout(5000L) {
                        delay(1000L)
                        while (isActive) {
                            if (SmileAdLoad.resultOf(SmileKey.POS_INT3) != null) {
                                Log.e(TAG, "loadSmileInt3: ")
                                SmileAdLoad.resultOf(SmileKey.POS_INT3)
                                    ?.let {
                                        viewModel.showInt3Fun(
                                            addNum,
                                            it,
                                            this@FinishActivity,
                                            isDialogAd
                                        )
                                    }
                                binding.showLoading = false
                                jobEndInt?.cancel()
                                jobEndInt = null
                                break
                            }
                            delay(500L)
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    binding.showLoading = false
                    if (!isDialogAd) {
                        viewModel.addTimeSuccess(addNum, this@FinishActivity)
                    } else {
                        binding.showAddTime = false
                    }
                }
            }
        }, {
            lifecycleScope.launch {
                delay(1000)
                binding.showLoading = false
                if (!isDialogAd) {
                    viewModel.addTimeSuccess(addNum, this@FinishActivity)
                } else {
                    binding.showAddTime = false
                }
            }

        })
    }

    private fun startCountdown() {
        val animator = ValueAnimator.ofInt(1, 5)
        animator.duration = 5000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            binding.tvEnd30.text = "Wait...${(5 - progress)}s"
            binding.tvMain30Dialog.text = "Wait...${(5 - progress)}s"
            binding.tvMain30Dialog.isEnabled = false
            binding.tvEnd30.isEnabled = false
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.tvEnd30.text = "+30mins"
                binding.tvEnd30.isEnabled = true
                binding.tvMain30Dialog.text = "+30mins"
                binding.tvMain30Dialog.isEnabled = true
                App.add30Num = 3
            }
        })
        animator.start()
    }

    override fun onTimeChanged() {
        val time = TimeData.getTiming()
        val userTime = TimeData.getUserTime()
        if (App.vpnLink) {
            binding.tvTime.text = time
            binding.tvDialogTime.text = time
        } else {
            binding.tvTime.text = userTime
            binding.tvDialogTime.text = userTime
        }
        SmileUtils.haveMoreTime({
            binding.tvEnd30.background = resources.getDrawable(R.drawable.ic_60mins)
            binding.tvEnd60.background = resources.getDrawable(R.drawable.ic_120mins)
        }, {})
    }

    override fun onResume() {
        super.onResume()

    }


    override fun onDestroy() {
        super.onDestroy()
        timeUtils.endTimingENd()
    }
}