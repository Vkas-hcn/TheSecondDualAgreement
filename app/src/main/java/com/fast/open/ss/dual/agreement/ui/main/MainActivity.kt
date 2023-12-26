package com.fast.open.ss.dual.agreement.ui.main

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Chronometer
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.base.BaseActivity
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.bean.VpnServiceBean
import com.fast.open.ss.dual.agreement.databinding.ActivityMainBinding
import com.fast.open.ss.dual.agreement.model.MainViewModel
import com.fast.open.ss.dual.agreement.ui.agreement.AgreementActivity
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.fast.open.ss.dual.agreement.utils.SmileUtils
import com.fast.open.ss.dual.agreement.utils.SmileUtils.getSmileImage
import com.fast.open.ss.dual.agreement.utils.TimeData
import com.fast.open.ss.dual.agreement.utils.TimeUtils
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.StartService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.blinkt.openvpn.api.ExternalOpenVPNService
import de.blinkt.openvpn.api.IOpenVPNAPIService
import de.blinkt.openvpn.api.IOpenVPNStatusCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.text.format.Formatter
import com.fast.open.ss.dual.agreement.utils.SmileNetHelp
import com.fast.open.ss.dual.agreement.utils.UserConter


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(
    R.layout.activity_main, MainViewModel::class.java
),
    ShadowsocksConnection.Callback,
    OnPreferenceDataStoreChangeListener, TimeUtils.TimeUtilsListener {


    override fun intiView() {
        clickListener()
        initVpnSetting()
        setServiceData()

    }

    override fun initData() {
        if (viewModel.isCanUser(this) != 0) {
            viewModel.initData(this, this)
        }
    }

    private fun clickListener() {
        binding.imgBg.post {
            viewModel.adaptsToSmallScreens(this)
        }

        binding.agreement = SmileKey.connection_mode.ifEmpty { "0" }
        if (App.isAppRunning) {
            binding.showGuide = false
            App.isAppRunning = false
        } else {
            binding.showGuide = !App.vpnLink
        }
        viewModel.timeUtils = TimeUtils().apply {
            setListener(this@MainActivity)
        }

        binding.imgConnect.setOnClickListener {
            toClickConnect()
        }
        binding.llSwitch.setOnClickListener {
            viewModel.clickChange(this, nextFun = {
                toClickConnect()
            })
        }
        binding.lavSmile.setOnClickListener {
            viewModel.clickChange(this, nextFun = {
                toClickConnect()
            })
        }
        binding.viewGuideSmile.setOnClickListener {
        }
        binding.ilSetting.atvPp.setOnClickListener {
            startActivityFirst<AgreementActivity>()
        }
        binding.ilSetting.atvShare.setOnClickListener {
            viewModel.shareUrl(this)
        }

        binding.imgSetting.setOnClickListener {
            viewModel.clickDisConnect(this, nextFun = {
                viewModel.clickChange(this, nextFun = {
                    binding.ilSetting.root.visibility = View.VISIBLE
                })
            })
        }
        binding.ilSetting.clSetting.setOnClickListener {
            binding.ilSetting.root.visibility = View.GONE
        }
        binding.ilSetting.llSetting.setOnClickListener {
        }
        binding.ilSetting.atvPp.setOnClickListener {
            startActivityFirst<AgreementActivity>()
        }
        binding.ilSetting.atvShare.setOnClickListener {
            viewModel.shareUrl(this)
        }
        binding.tvAuto.setOnClickListener {
            viewModel.clickDisConnect(this, nextFun = {
                viewModel.clickChange(this, nextFun = {
                    checkAgreement("0")
                })
            })
        }
        binding.tvOpen.setOnClickListener {
            viewModel.clickDisConnect(this, nextFun = {
                viewModel.clickChange(this, nextFun = {
                    checkAgreement("1")
                })
            })
        }
        binding.tvSs.setOnClickListener {
            viewModel.clickDisConnect(this, nextFun = {
                viewModel.clickChange(this, nextFun = {
                    checkAgreement("2")
                })
            })
        }

        binding.llFlag.setOnClickListener {
            viewModel.clickChange(this, nextFun = {
                viewModel.jumpToServerList(this)
            })
        }
    }

    private fun setServiceData() {
        viewModel.liveInitializeServerData.observe(this) {
            it?.let {
                viewModel.setFastInformation(it, binding)
            }
        }

        viewModel.liveUpdateServerData.observe(this) {
            it?.let {
                viewModel.whetherRefreshServer = true
                toConnectVpn()
            }
        }
        viewModel.liveNoUpdateServerData.observe(this) {
            it?.let {
                viewModel.whetherRefreshServer = false
                viewModel.setFastInformation(it, binding)
                toConnectVpn()
            }
        }
        viewModel.showConnectLive.observe(this) {
            it?.let {
                viewModel.showConnectFun(this, it)
            }
        }
    }


    private fun checkAgreement(type: String) {
        if (App.vpnLink) {
            if (type == "1" && binding.agreement != "1") {
                showSwitching(type)
                return
            }
            if (type != "1" && binding.agreement == "1") {
                showSwitching(type)
                return
            }
            binding.agreement = type

        } else {
            binding.agreement = type
        }
    }

    private fun showSwitching(type: String) {
        val dialogVpn: androidx.appcompat.app.AlertDialog =
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tips")
                .setMessage("switching the connection mode will disconnect the current connection whether to continue")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    toConnectVpn()
                    binding.agreement = type
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.create()
        dialogVpn.setCancelable(false)
        dialogVpn.show()
    }

    private fun toClickConnect() {
        lifecycleScope.launch {
            SmileNetHelp.getLoadIp()
            SmileNetHelp.getLoadOthIp()
        }
        if (binding.serviceState == "1") {
            return
        }
        lifecycleScope.launch {
            toConnectVpn()
        }
    }

    private fun toConnectVpn() {
        binding.showGuide = false
        if (SmileUtils.isAppOnline(this)) {
            SmileAdLoad.loadOf(SmileKey.POS_CONNECT)
            SmileAdLoad.loadOf(SmileKey.POS_BACK)
            SmileAdLoad.loadOf(SmileKey.POS_RESULT)
            if (!App.vpnLink) {
                SmileKey.connection_mode = binding?.agreement!!
            }
            if (viewModel.isCanUser(this) == 0) {
                return
            }
            if (binding.agreement == "1") {
                viewModel.startOpenVpn(this)
            } else {
                connect.launch(null)
            }
        } else {
            Toast.makeText(this, "Please check your network connection", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun initVpnSetting() {
        if (UserConter.showAdCenter()) {
            binding.flAd.visibility = View.VISIBLE
        } else {
            binding.flAd.visibility = View.GONE
        }
        val data = UserConter.spoilerOrNot()
        SmileKey.smile_arrow = data
        bindService(
            Intent(this, ExternalOpenVPNService::class.java),
            mConnection,
            BIND_AUTO_CREATE
        )
        viewModel.requestPermissionForResultVPN =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                requestPermissionForResult(it)
            }
    }


    private val connect = registerForActivityResult(StartService()) {
        if (it) {
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.startTheJudgment(this)
        }
    }


    private fun requestPermissionForResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            viewModel.startTheJudgment(this)
        } else {
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show()
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName?,
            service: IBinder?,
        ) {
            viewModel.mService = IOpenVPNAPIService.Stub.asInterface(service)
            try {
                viewModel.mService?.registerStatusCallback(mCallback)
                Log.e("open vpn mService", "mService onServiceConnected")
            } catch (e: Exception) {
                Log.e("open vpn error", e.message.toString())
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            Log.e("open vpn mService", "mService onServiceDisconnected")
            viewModel.mService = null
        }
    }
    private val mCallback = object : IOpenVPNStatusCallback.Stub() {
        override fun newStatus(uuid: String?, state: String?, message: String?, level: String?) {
            // NOPROCESS 未连接 // CONNECTED 已连接
            // RECONNECTING 尝试重新链接 // EXITING 连接中主动掉用断开
            Log.e(
                TAG,
                "newStatus: state=$state;message=$message;agreement=${SmileKey.connection_mode}"
            )
            if (SmileKey.connection_mode != "1") {
                return
            }
            when (state) {
                "CONNECTED" -> {
                    App.vpnLink = true
                    viewModel.connectOrDisconnectSmile(this@MainActivity, true)
                    viewModel.changeState(
                        state = BaseService.State.Idle,
                        this@MainActivity,
                        App.vpnLink
                    )
//                    binding.serviceState = "2"
                    handleSmileTimerLock()
                }

                "RECONNECTING", "EXITING", "CONNECTRETRY" -> {
                    viewModel.mService?.disconnect()
                }

                "NOPROCESS" -> {
                    viewModel.mService?.disconnect()
                    App.vpnLink = false
                    viewModel.connectOrDisconnectSmile(this@MainActivity, true)
                    viewModel.changeState(
                        state = BaseService.State.Idle,
                        this@MainActivity,
                        App.vpnLink
                    )
                    handleSmileTimerLock()
                }


                else -> {}
            }

        }

    }


    override fun onStart() {
        super.onStart()
        viewModel.connection.bandwidthTimeout = 500
    }

    override fun onResume() {
        super.onResume()
        handleWarmBoot()
//        setOpenVpnState()
    }

    private fun handleWarmBoot() {
        viewModel.showHomeAd(this)
    }

    private fun handleSmileTimerLock() {
        if (App.vpnLink) {
            binding.showGuide = false
            viewModel.changeOfVpnStatus(this, "2")
            if (binding.tvTime.text.toString() == "00:00:00") {
                TimeData.startTiming()
            }
        } else {
            viewModel.changeOfVpnStatus(this, "0")
        }

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.connection.bandwidthTimeout = 0
        viewModel.stopOperate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DataStore.publicStore.unregisterChangeListener(this)
        viewModel.connection.disconnect(this)
        Log.e(TAG, "onDestroy: Main", )
        App.isBoot =false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x567 && viewModel.whetherRefreshServer) {
            viewModel.setFastInformation(viewModel.afterDisconnectionServerData, binding)
            val serviceData = Gson().toJson(viewModel.afterDisconnectionServerData)
            SmileKey.check_service = serviceData
            viewModel.currentServerData = viewModel.afterDisconnectionServerData
        }
        if (requestCode == 567) {
            when (App.serviceState) {
                "disconnect" -> {
                    viewModel.updateSkServer(false)
                }
                "connect" -> {
                    viewModel.updateSkServer(true)
                }
            }
            App.serviceState = "mo"
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.showGuide == true || binding.ilSetting.root.visibility == View.VISIBLE) {
                binding.showGuide = false
                binding.ilSetting.root.visibility = View.GONE
            } else {
                viewModel.clickChange(this, nextFun = {
                    finish()
                })
            }
        }
        return true
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        App.vpnLink = state.canStop
        viewModel.changeState(state, this)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = BaseService.State.values()[service.state]
        setSsVpnState(state.canStop)
    }

    private fun setSsVpnState(canStop: Boolean) {
        if (SmileKey.connection_mode != "1") {
            App.vpnLink = canStop
            handleSmileTimerLock()
        }
    }
    private fun setOpenVpnState() {
        if (SmileKey.connection_mode == "1") {
            handleSmileTimerLock()
        }
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                viewModel.connection.disconnect(this)
                viewModel.connection.connect(this, this)
            }
        }
    }

    override fun onTimeChanged() {
        lifecycleScope.launch {
            binding.tvTime.text = TimeData.getTiming()
        }
    }
}