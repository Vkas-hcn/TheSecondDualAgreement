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
            viewModel.returnToHomePage(this)
        }
        viewModel.showEndAd(this)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        if (App.vpnLink) {
            binding.imgVpnEnd.setImageResource(R.drawable.ic_end_connect)
            binding.tvTitle.text = "Connected succeed"
            viewModel.getSpeedData(this)
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
            viewModel.returnToHomePage(this)
        }
        return true
    }


}