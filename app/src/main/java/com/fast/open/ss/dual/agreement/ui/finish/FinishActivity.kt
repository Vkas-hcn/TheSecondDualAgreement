package com.fast.open.ss.dual.agreement.ui.finish

import android.annotation.SuppressLint
import android.text.format.Formatter
import android.util.Log
import android.view.KeyEvent
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
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FinishActivity : BaseActivity<ActivityFinishBinding, FinishViewModel>(
    R.layout.activity_finish, FinishViewModel::class.java
) {
    private lateinit var vpnServiceBean: VpnServiceBean
    override fun intiView() {
        val bundle = intent.extras
        vpnServiceBean = Gson().fromJson(
            bundle?.getString(SmileKey.cuSmileConnected),
            object : TypeToken<VpnServiceBean?>() {}.type
        )
        binding.imgBack.setOnClickListener {
            returnToHomePage()
        }
        showEndAd()
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        if (App.vpnLink) {
            binding.imgVpnEnd.setImageResource(R.drawable.ic_end_connect)
            binding.tvTitle.text = "Connected succeed"
            getSpeedData()
        } else {
            binding.imgVpnEnd.setImageResource(R.drawable.ic_end_dis)
            binding.tvTitle.text = "VPN disconnect"
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            returnToHomePage()
        }
        return true
    }

    private fun returnToHomePage() {
        val res = SmileAdLoad.resultOf(SmileKey.POS_BACK)
        if (res == null) {
            finish()
        } else {
            showBackFun(res)
        }
    }

    private fun showEndAd() {
        lifecycleScope.launch {
            delay(300)
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            Log.e(TAG, "showEndAd: 1", )

            while (isActive) {
                val res = SmileAdLoad.resultOf(SmileKey.POS_RESULT)
                if (res != null) {
                    Log.e(TAG, "showEndAd: 2", )
                    showResultNativeAd(res)
                    cancel()
                    break
                }
                delay(500)
            }
        }
    }

    private fun showResultNativeAd(res: Any) {
        SmileAdLoad.showNativeOf(
            where = SmileKey.POS_RESULT,
            nativeRoot = binding.nativeAdView,
            res = res,
            preload = true,
            onShowCompleted = {
            }
        )
    }

    private fun showBackFun(it: Any) {
        SmileAdLoad.showFullScreenOf(
            where = SmileKey.POS_BACK,
            context = this,
            res = it,
            onShowCompleted = {
                lifecycleScope.launch(Dispatchers.Main) {
                    finish()
                }
            }
        )
    }

    fun getSpeedData(){
        lifecycleScope.launch {
            while (isActive){
                val speed_dow = App.mmkvSmile.decodeString("speed_dow","0 B")
                val speed_up = App.mmkvSmile.decodeString("speed_up","0 B")
                binding.tvSpeedDownload.text = speed_dow
                binding.tvSpeedUpload.text = speed_up
                delay(500)
            }
        }
    }
}