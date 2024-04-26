package com.fast.open.ss.dual.agreement.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
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
import android.view.animation.LinearInterpolator
import androidx.activity.addCallback
import com.fast.open.ss.dual.agreement.utils.DaDianUtils
import com.fast.open.ss.dual.agreement.utils.PutDataUtils
import com.fast.open.ss.dual.agreement.utils.SmileNetHelp
import com.fast.open.ss.dual.agreement.utils.SmileUtils.isVisible
import com.fast.open.ss.dual.agreement.utils.UserConter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.system.exitProcess
import android.app.ActivityManager
import android.content.Context
import android.os.Process
import androidx.core.view.isVisible
import com.fast.open.ss.dual.agreement.BuildConfig
import com.fast.open.ss.dual.agreement.ui.speed.SpeedActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(
    R.layout.activity_main, MainViewModel::class.java
), ShadowsocksConnection.Callback, OnPreferenceDataStoreChangeListener,
    TimeUtils.TimeUtilsListener {
    var jobMainInt: Job? = null
    var jobSpeed: Job? = null
    override fun intiView() {
        clickListener()
        initVpnSetting()
        setServiceData()
    }

    override fun initData() {
        binding.showAddTime = false
        if (viewModel.isCanUser(this) != 0) {
            viewModel.initData(this, this)
        }
        onBackPressedDispatcher.addCallback(this) {
            if (binding.showGuide == true || binding.ilSetting.root.visibility == View.VISIBLE || binding.showSpeedConnect == true) {
                binding.showGuide = false
                binding.showSpeedConnect = false
                binding.ilSetting.root.visibility = View.GONE
            } else {
                if (binding.showAddTime == true || binding.showAddSuccess == true || binding.showLoading == true) {
                    return@addCallback
                }
                viewModel.clickChange(this@MainActivity, nextFun = {
                    finish()
                })
            }
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
        binding.viewGuideSmile.setOnClickListener {

        }
        binding.viewAddTimeSmile.setOnClickListener {

        }
        binding.viewConnectSpeed.setOnClickListener {

        }
        binding.viewLoadingSmile.setOnClickListener {

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
        binding.viewGuideSmile.setOnClickListener {}
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
        binding.ilSetting.llSetting.setOnClickListener {}
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
        binding.tvMain30.setOnClickListener {
            DaDianUtils.oom21(this@MainActivity, false)
            SmileUtils.haveMoreTime({
                Toast.makeText(this, "Usage limit reached today", Toast.LENGTH_SHORT).show()

            }, {
                clickAddTimeFun(true)
            })
        }
        binding.tvMain60.setOnClickListener {
            DaDianUtils.oom22(this@MainActivity, false)
            SmileUtils.haveMoreTime({
                Toast.makeText(this, "Usage limit reached today", Toast.LENGTH_SHORT).show()

            }, {
                clickAddTimeFun(false)
            })
        }


        binding.tvMain30Dialog.setOnClickListener {
            DaDianUtils.oom21(this@MainActivity, true)
            clickAddTimeFun(true)
        }
        binding.tvMain60Dialog.setOnClickListener {
            DaDianUtils.oom22(this@MainActivity, true)
            clickAddTimeFun(false)
        }

        binding.imgAddX.setOnClickListener {
            lifecycleScope.launch {
                DaDianUtils.oom23(this@MainActivity)
                loadSmileInt3(0)
            }
        }
        binding.imgAddSuccessX.setOnClickListener {
            DaDianUtils.oom25(this)

            clickNextFun()
        }
        binding.tvMainGo.setOnClickListener {
            DaDianUtils.oom26(this)
            clickNextFun()
        }
        binding.llSpeed.setOnClickListener {
            viewModel.clickDisConnect(this, nextFun = {
                viewModel.clickChange(this, nextFun = {
                    Log.e(TAG, "clickListener:llSpeed=${binding.serviceState} ")
                    if (binding.serviceState == "1") {
                        return@clickChange
                    }
                    if (App.vpnLink) {
                        showSpeed()
                    } else {
                        binding.showSpeedConnect = true
                    }
                })
            })
        }
        binding.tvConnect.setOnClickListener {
            binding.showSpeedConnect = false
            toClickConnect()
        }
    }

    private fun showSpeed() {
        jobSpeed?.cancel()
        jobSpeed = null
        var isP = false
        var isError = false
        jobSpeed = lifecycleScope.launch {
            binding.llSpeedLottie.isVisible = true
            withTimeoutOrNull(15000L) {
                SmileUtils.apply {
                    internetSpeedDetection { isP = true }
                }
                withContext(Dispatchers.IO) {
                    SmileUtils.pingServiceIp()
                }
                while (isP.not()) delay(400)
            }
            if (SmileKey.dowLoadSpeed == "0" || SmileKey.upLoadSpeed == "0") {
                closeSpeedTestPage()
            } else {
                jumpToSpeedPaper()
            }
        }
    }

    private fun jumpToSpeedPaper() {
        if (binding.llSpeedLottie.isVisible) {
            val intent = Intent(this@MainActivity, SpeedActivity::class.java)
            startActivity(intent)
        }
        binding.llSpeedLottie.isVisible = false
    }

    private fun closeSpeedTestPage() {
        binding.llSpeedLottie.isVisible = false
        Toast.makeText(
            this@MainActivity,
            "Speed test timed out. Please try again later.",
            Toast.LENGTH_SHORT
        ).show()
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
            androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Tips")
                .setMessage("switching the connection mode will disconnect the current connection whether to continue")
                .setCancelable(false).setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    toConnectVpn()
                    binding.agreement = type
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.create()
        dialogVpn.setCancelable(false)
        dialogVpn.show()
    }

    private fun toClickConnect() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                SmileNetHelp.getLoadIp()
                SmileNetHelp.getLoadOthIp()
            }
            if (binding.serviceState == "1") {
                return@launch
            }
        }
        toConnectVpn()
    }

    private fun toConnectVpn() {
        lifecycleScope.launch {
            binding.showGuide = false
            if (!App.vpnLink) {
                DaDianUtils.oom5(this@MainActivity)
            }
            if (!SmileKey.isHaveServeData(this@MainActivity)) {
                binding.pbLoading.visibility = View.VISIBLE
                delay(2000)
                binding.pbLoading.visibility = View.GONE
                viewModel.initServerData()
                return@launch
            }
            if (SmileUtils.isAppOnline(this@MainActivity)) {
                SmileAdLoad.loadOf(SmileKey.POS_CONNECT)
                SmileAdLoad.loadOf(SmileKey.POS_BACK)
                if (!App.vpnLink) {
                    SmileKey.connection_mode = binding?.agreement!!
                }
                if (viewModel.isCanUser(this@MainActivity) == 0) {
                    return@launch
                }
                viewModel.startOpenVpn(this@MainActivity)

            } else {
                Toast.makeText(
                    this@MainActivity, "Please check your network connection", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initVpnSetting() {
        val data = UserConter.spoilerOrNot()
        SmileKey.smile_arrow = data
        bindService(
            Intent(this, ExternalOpenVPNService::class.java), mConnection, BIND_AUTO_CREATE
        )
        viewModel.requestPermissionForResultVPN =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                requestPermissionForResult(it)
            }
    }

    private fun requestPermissionForResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            if (!SmileKey.permiss) {
                SmileKey.permiss = true
                SmileNetHelp.postPotNet(this, "oom7")
            }
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
                    handleSmileTimerLock()
                    viewModel.connectionStatusJudgment(App.vpnLink, this@MainActivity)
                }

                "RECONNECTING", "EXITING", "CONNECTRETRY" -> {
                    viewModel.mService?.disconnect()
                    SmileNetHelp.postPotNet(this@MainActivity, "oom10")
                    Toast.makeText(this@MainActivity, "The connection failed!", Toast.LENGTH_SHORT)
                        .show()
                }

                "NOPROCESS" -> {
                    viewModel.mService?.disconnect()
                    App.vpnLink = false
                    handleSmileTimerLock()
                    viewModel.connectOrDisconnectSmile(this@MainActivity, true)
                    viewModel.changeState(
                        state = BaseService.State.Idle, this@MainActivity, App.vpnLink
                    )
                }

                else -> {}
            }

        }

    }


    override fun onStart() {
        super.onStart()
        viewModel.connection.bandwidthTimeout = 500
        DaDianUtils.oom3(this@MainActivity)

    }

    override fun onResume() {
        super.onResume()
        if (!App.vpnLink) {
            binding.tvTime.text = TimeData.getTiming()
        }
    }

    private fun handleSmileTimerLock() {
        if (App.vpnLink) {
            binding.showGuide = false
            viewModel.changeOfVpnStatus(this, "2")
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
        binding.llSpeedLottie.isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        DataStore.publicStore.unregisterChangeListener(this)
        viewModel.connection.disconnect(this)
        App.isBoot = false
        Log.e(TAG, "onDestroy: Main")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x567) {
            App.isBoot = false
        }
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

        if ((requestCode == 0x567) && App.vpnLink) {
            SmileUtils.haveMoreTime({}, {
                lifecycleScope.launch {
                    SmileAdLoad.loadOf(SmileKey.POS_RE)
                    delay(300)
                    if (requestCode == 0x567) {
                    }
                    binding.showAddTime = true
                    SmileNetHelp.postPotNet(
                        this@MainActivity, "oom20", "oo", App.top_activity_name
                    )
                }
            })
        }

    }


    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        App.vpnLink = state.canStop
        viewModel.changeState(state, this, App.vpnLink)
        Log.e(TAG, "stateChanged: ${App.vpnLink}")
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

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                viewModel.connection.disconnect(this)
                viewModel.connection.connect(this, this)
            }
        }
    }

    override fun onTimeChanged() {
        if (App.vpnLink) {
            if (App.isStopVpn) {
                lifecycleScope.launch {
                    Core.stopService()
                }
                TimeData.resetCountdown()
                binding.serviceState = "0"
                SmileUtils.stopRotation(binding.imgSwitch)
                binding.imgSwitch.setImageResource(R.drawable.ic_swith)
            } else {
                binding.tvTime.text = TimeData.getTiming()
                binding.tvDialogTime.text = TimeData.getTiming()
            }
        }
        SmileUtils.haveMoreTime({
            binding.tvMain30.background = resources.getDrawable(R.drawable.ic_60mins)
            binding.tvMain60.background = resources.getDrawable(R.drawable.ic_120mins)
        }, {})
    }

    private fun clickNextFun() {
        binding.showAddSuccess = false
        if (App.add30Num <= 0) {
            startCountdown()
        }
    }

    private fun clickAddTimeFun(isInt3: Boolean) {

        jobMainInt?.cancel()
        jobMainInt = null
        jobMainInt = lifecycleScope.launch {
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
                viewModel.loadSmileRewarded(this@MainActivity)
            }
        }
    }

    private suspend fun loadSmileInt3(addNum: Int) {
        binding.showLoading = true
        SmileUtils.rotateImageView(binding.imgLoad)
        timeShowDetailAd({
            jobMainInt = lifecycleScope.launch {
                SmileAdLoad.loadOf(SmileKey.POS_INT3)
                try {
                    withTimeout(5000L) {
                        delay(1000L)
                        while (isActive) {
                            if (SmileAdLoad.resultOf(SmileKey.POS_INT3) != null) {
                                SmileAdLoad.resultOf(SmileKey.POS_INT3)
                                    ?.let { viewModel.showInt3Fun(addNum, it, this@MainActivity) }
                                binding.showLoading = false
                                jobMainInt?.cancel()
                                jobMainInt = null
                                break
                            }
                            delay(500L)
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    binding.showLoading = false
                    if (addNum != 0) {
                        viewModel.addTimeSuccess(addNum, this@MainActivity)
                    } else {
                        binding.showAddTime = false
                    }
                }
            }
        }, {
            lifecycleScope.launch {
                delay(1000)
                binding.showLoading = false
                if (addNum != 0) {
                    viewModel.addTimeSuccess(addNum, this@MainActivity)
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
            binding.tvMain30.text = "Wait...${(5 - progress)}s"
            binding.tvMain30Dialog.text = "Wait...${(5 - progress)}s"
            binding.tvMain30Dialog.isEnabled = false
            binding.tvMain30.isEnabled = false
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.tvMain30.text = "+30mins"
                binding.tvMain30.isEnabled = true
                binding.tvMain30Dialog.text = "+30mins"
                binding.tvMain30Dialog.isEnabled = true
                App.add30Num = 3
            }
        })
        animator.start()
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
}