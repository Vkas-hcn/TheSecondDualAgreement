package com.fast.open.ss.dual.agreement.ui.speed


import android.annotation.SuppressLint
import androidx.activity.addCallback
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.base.BaseActivity
import com.fast.open.ss.dual.agreement.databinding.ActivitySpeedBinding
import com.fast.open.ss.dual.agreement.model.FinishViewModel
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.fast.open.ss.dual.agreement.utils.TimeUtils

class SpeedActivity : BaseActivity<ActivitySpeedBinding, FinishViewModel>(
    R.layout.activity_speed, FinishViewModel::class.java
), TimeUtils.TimeUtilsListener {

    override fun intiView() {

        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        binding.tvSpeedUpload.text = SmileKey.upLoadSpeed
        binding.tvSpeedDownload.text = SmileKey.dowLoadSpeed
        binding.tvPing.text = SmileKey.pingSpeed
    }


    override fun onResume() {
        super.onResume()

    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onTimeChanged() {
    }
}