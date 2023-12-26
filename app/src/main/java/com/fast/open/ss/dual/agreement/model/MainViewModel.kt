package com.fast.open.ss.dual.agreement.model

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.bean.VpnServiceBean
import com.fast.open.ss.dual.agreement.databinding.ActivityMainBinding
import com.fast.open.ss.dual.agreement.ui.finish.FinishActivity
import com.fast.open.ss.dual.agreement.ui.list.ListActivity
import com.fast.open.ss.dual.agreement.ui.main.MainActivity
import com.fast.open.ss.dual.agreement.utils.SmileData
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.fast.open.ss.dual.agreement.utils.SmileUtils
import com.fast.open.ss.dual.agreement.utils.SmileUtils.getSmileImage
import com.fast.open.ss.dual.agreement.utils.TimeData
import com.fast.open.ss.dual.agreement.utils.TimeUtils
import com.fast.open.ss.dual.agreement.utils.UserConter
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.blinkt.openvpn.api.IOpenVPNAPIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import kotlin.system.exitProcess

class MainViewModel : ViewModel() {
    val showConnectLive = MutableLiveData<Any>()
    lateinit var timeUtils: TimeUtils
    val connection = ShadowsocksConnection(true)
    var whetherRefreshServer = false
    var jobStartSmile: Job? = null

    var nowClickState: Int = 1

    var mService: IOpenVPNAPIService? = null
    lateinit var requestPermissionForResultVPN: ActivityResultLauncher<Intent?>

    companion object {
        var stateListener: ((BaseService.State) -> Unit)? = null
    }

    fun shareUrl(activity: MainActivity) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=${activity.packageName}"
        )
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, null)
        activity.startActivity(shareIntent)
    }

    fun initData(
        activity: MainActivity,
        call: ShadowsocksConnection.Callback
    ) {
        val binding = activity.binding
        changeState(BaseService.State.Idle, activity)
        connection.connect(activity, call)
        DataStore.publicStore.registerChangeListener(activity)
        if (SmileKey.check_service.isEmpty()) {
            initializeServerData()
        } else {
            val serviceData = SmileKey.check_service
            val currentServerData: VpnServiceBean =
                Gson().fromJson(serviceData, object : TypeToken<VpnServiceBean?>() {}.type)
            setFastInformation(currentServerData, binding)
        }

    }


    fun changeState(
        state: BaseService.State = BaseService.State.Idle,
        activity: AppCompatActivity,
        vpnLink: Boolean = false,
    ) {
        connectionStatusJudgment(vpnLink, activity)
        stateListener?.invoke(state)
    }

    fun jumpToServerList(activity: MainActivity) {
        activity.lifecycleScope.launch {
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            SmileAdLoad.loadOf(SmileKey.POS_BACK)
            activity.startActivityWithReFirst<ListActivity>(null, 567)
        }
    }

    fun setFastInformation(meteorVpnBean: VpnServiceBean, binding: ActivityMainBinding) {
        if (meteorVpnBean.best_smart) {
            binding.imgFlag.setImageResource("Fast Server".getSmileImage())
        } else {
            binding.imgFlag.setImageResource(meteorVpnBean.blocuss.getSmileImage())
        }
    }

    fun startTheJudgment(activity: AppCompatActivity) {
        startVpn(activity)
    }

    /**
     * 启动VPN
     */
    private fun startVpn(activity: AppCompatActivity) {
        jobStartSmile = activity.lifecycleScope.launch {
            nowClickState = if (App.vpnLink) {
                2
            } else {
                0
            }
            changeOfVpnStatus(activity as MainActivity, "1")
            connectVpn(activity)
            loadSmileAdvertisements(activity)
        }
    }

    private suspend fun connectVpn(activity: MainActivity) {
        if (!App.vpnLink) {
            if (activity.binding.agreement == "1") {
                mService?.let {
                    setOpenData(activity, it)
                }
                Core.stopService()
            } else {
                delay(2000)
                mService?.disconnect()
                Core.startService()
            }
        }

    }


    fun disconnectVpn() {
        if (App.vpnLink) {
            Core.stopService()
        }
    }


    private suspend fun loadSmileAdvertisements(activity: AppCompatActivity) {
        try {
            withTimeout(10000L) {
                delay(2000L)
                while (isActive) {
                    if (SmileAdLoad.resultOf(SmileKey.POS_CONNECT) != null) {
                        showConnectLive.value = SmileAdLoad.resultOf(SmileKey.POS_CONNECT)
                        cancel()
                        jobStartSmile?.cancel()
                        jobStartSmile = null
                    }
                    delay(500L)
                }
            }
        } catch (e: TimeoutCancellationException) {
            connectOrDisconnectSmile(activity as MainActivity)
        }
    }

    fun showConnectFun(activity: MainActivity, it: Any) {
        SmileAdLoad.showFullScreenOf(
            where = SmileKey.POS_CONNECT,
            context = activity,
            res = it,
            preload = true,
            onShowCompleted = {
                jobStartSmile?.cancel()
                jobStartSmile = null
                connectOrDisconnectSmile(activity, true)
            }
        )
    }

    fun connectOrDisconnectSmile(activity: MainActivity, isOpenJump: Boolean = false) {
        if (nowClickState == 2) {
//            if(App.vpnLink){
//                Toast.makeText(activity, "VPN is connecting. Please try again later.", Toast.LENGTH_SHORT).show()
//                return
//            }
            mService?.disconnect()
            disconnectVpn()
            changeOfVpnStatus(activity, "0")
            if (!App.isBackDataSmile) {
                jumpToFinishPage(activity,false)
            }
        }
        if (nowClickState == 0) {
//            if(!App.vpnLink){
//                Toast.makeText(activity, "The connection failed", Toast.LENGTH_SHORT).show()
//                return
//            }
            if (!isOpenJump) {
                if (activity.binding.agreement == "1") {
                    return
                }
            }
            if (!App.isBackDataSmile) {
                jumpToFinishPage(activity,true)
            }
            changeOfVpnStatus(activity, "2")
        }

    }

    private fun jumpToFinishPage(activity: MainActivity,isConnect: Boolean) {
        activity.lifecycleScope.launch {
            delay(300L)
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            val bundle = Bundle()
            bundle.putString(SmileKey.cuSmileConnected, SmileKey.check_service)
            bundle.putBoolean(SmileKey.isSmileConnected, isConnect)
            val intent = Intent(activity, FinishActivity::class.java)
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, 0x567)
        }

    }

    fun connectionStatusJudgment(
        vpnLink: Boolean,
        activity: AppCompatActivity
    ) {
        val binding = (activity as MainActivity).binding
        when (vpnLink) {
            true -> {
                // 连接成功
                connectionServerSuccessful(binding)
            }

            false -> {
                disconnectServerSuccessful(binding)
            }
        }
    }

    /**
     * 连接服务器成功
     */
    fun connectionServerSuccessful(binding: ActivityMainBinding) {
        binding.serviceState = "2"
    }

    /**
     * 断开服务器
     */
    fun disconnectServerSuccessful(binding: ActivityMainBinding) {


    }

    /**
     * vpn状态变化
     * 是否连接
     */
    fun changeOfVpnStatus(
        activity: MainActivity,
        stateInt: String
    ) {
        val binding = activity.binding
        binding.serviceState = stateInt
        Log.e(TAG, "changeOfVpnStatus: ${stateInt}")
        when (stateInt) {
            "0" -> {
                //断开
                SmileUtils.stopRotation(binding.imgSwitch)
                timeUtils.endTiming()
                binding.imgSwitch.setImageResource(R.drawable.ic_swith)
            }

            "1" -> {
                //连接中
                binding.imgSwitch.setImageResource(R.drawable.ic_connecting)
                SmileUtils.rotateImageViewInfinite(binding.imgSwitch, 600)
            }

            "2" -> {
                //连接成功
                SmileUtils.stopRotation(binding.imgSwitch)
                timeUtils.startTiming()
                binding.imgSwitch.setImageResource(R.drawable.ic_main_connect)

            }

            else -> {
                //未知
                SmileUtils.stopRotation(binding.imgSwitch)
            }

        }
    }

    fun showHomeAd(activity: MainActivity) {


        activity.lifecycleScope.launch {
            delay(300)
            if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) || App.isBoot) {
                return@launch
            }
            activity.binding.nativeAdView.visibility = android.view.View.GONE
            activity.binding.imgAdType.visibility = android.view.View.VISIBLE
            App.isBoot = true
            val adHomeData = SmileAdLoad.resultOf(SmileKey.POS_HOME)
            while (isActive) {
                if (adHomeData != null) {
                    activity.binding.nativeAdView.visibility = android.view.View.VISIBLE
                    SmileAdLoad.showNativeOf(
                        where = SmileKey.POS_HOME,
                        nativeRoot = activity.binding.nativeAdView,
                        res = adHomeData,
                        preload = true,
                        onShowCompleted = {
                        }
                    )
                    cancel()
                    break
                }
                delay(500)
            }
        }
    }

    val liveInitializeServerData: MutableLiveData<VpnServiceBean> by lazy {
        MutableLiveData<VpnServiceBean>()
    }

    val liveNoUpdateServerData: MutableLiveData<VpnServiceBean> by lazy {
        MutableLiveData<VpnServiceBean>()
    }

    val liveUpdateServerData: MutableLiveData<VpnServiceBean?> by lazy {
        MutableLiveData<VpnServiceBean?>()
    }

    var currentServerData: VpnServiceBean = VpnServiceBean()

    var afterDisconnectionServerData: VpnServiceBean = VpnServiceBean()

    fun initializeServerData() {
        val bestData = SmileKey.getFastVpn()
        ProfileManager.getProfile(DataStore.profileId).let {
            if (it != null) {
                ProfileManager.updateProfile(SmileUtils.setSkServerData(it, bestData))
            } else {
                val profile = Profile()
                ProfileManager.createProfile(SmileUtils.setSkServerData(profile, bestData))
            }
        }
        bestData.best_smart = true
        DataStore.profileId = 1L
        currentServerData = bestData
        val serviceData = Gson().toJson(currentServerData)
        SmileKey.check_service = serviceData
        liveInitializeServerData.postValue(bestData)
    }

    fun updateSkServer(isConnect: Boolean) {
        val skVpnServiceBean = Gson().fromJson<VpnServiceBean>(
            SmileKey.check_service,
            object : TypeToken<VpnServiceBean?>() {}.type
        )
        ProfileManager.getProfile(DataStore.profileId).let {
            if (it != null) {
                SmileUtils.setSkServerData(it, skVpnServiceBean)
                ProfileManager.updateProfile(it)
            } else {
                ProfileManager.createProfile(Profile())
            }
        }
        DataStore.profileId = 1L
        if (isConnect) {
            afterDisconnectionServerData = skVpnServiceBean
            liveUpdateServerData.postValue(skVpnServiceBean)
        } else {
            currentServerData = skVpnServiceBean
            val serviceData = Gson().toJson(currentServerData)
            SmileKey.check_service = serviceData
            liveNoUpdateServerData.postValue(skVpnServiceBean)
        }
    }

    fun setOpenData(activity: MainActivity, server: IOpenVPNAPIService): Job {
        return MainScope().launch(Dispatchers.IO) {
            val data = SmileKey.check_service.isEmpty().let {
                if (it) {
                    SmileKey.getAllVpnListData().firstOrNull()
                } else {
                    Gson().fromJson<VpnServiceBean>(
                        SmileKey.check_service,
                        object : TypeToken<VpnServiceBean?>() {}.type
                    )
                }
            }
            runCatching {
                val config = StringBuilder()
                activity.assets.open("fast_bloomingvpn.ovpn").use { inputStream ->
                    inputStream.bufferedReader().use { reader ->
                        reader.forEachLine { line ->
                            config.append(
                                when {
                                    line.contains(
                                        "remote 103",
                                        true
                                    ) -> "remote ${data?.bloally} 443"

                                    else -> line
                                }
                            ).append("\n")
                        }
                    }
                }
                Log.e(TAG, "step2: =$config")
                server.startVPN(config.toString())
            }.onFailure { exception ->
                Log.e(TAG, "Error in step2: ${exception.message}")
            }
        }
    }

    fun startOpenVpn(activity: MainActivity) {
        val state = checkVPNPermission(activity)
        if (state) {
            startTheJudgment(activity)
        } else {
            VpnService.prepare(activity).let {
                requestPermissionForResultVPN.launch(it)
            }
        }
    }

    fun checkVPNPermission(activity: MainActivity): Boolean {
        VpnService.prepare(activity).let {
            return it == null
        }
    }

    fun isConnectGuo(activity: MainActivity): Boolean {
        return !(nowClickState == 0 && activity.binding.serviceState == "1")
    }

    fun clickDisConnect(activity: MainActivity, nextFun: () -> Unit) {
        if (nowClickState == 2 && activity.binding.serviceState == "1") {
            stopOperate(activity)
        } else {
            nextFun()
        }
    }

    fun clickChange(activity: MainActivity, nextFun: () -> Unit) {
        if (isConnectGuo(activity)) {
            nextFun()
        } else {
            Toast.makeText(
                activity,
                "VPN is connecting. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun stopOperate(activity: MainActivity) {
        connection.bandwidthTimeout = 0
        jobStartSmile?.cancel() // 取消执行方法的协程
        jobStartSmile = null
        if (App.vpnLink) {
            changeOfVpnStatus(activity, "2")
        } else {
            changeOfVpnStatus(activity, "0")
        }
    }

    fun isCanUser(activity: MainActivity): Int {
//        if (isIllegalIp()) {
//            displayCannotUsePopUpBoxes(activity)
//            return 0
//        }
        return 1
    }

    private fun isIllegalIp(): Boolean {
        val ipData = SmileKey.ip_gsd
        if (ipData.isEmpty()) {
            return isIllegalIp2()
        }

        return ipData == "IR" || ipData == "CN" ||
                ipData == "HK" || ipData == "MO"
    }

    private fun isIllegalIp2(): Boolean {
        val ipData = SmileKey.ip_gsd_oth
        val locale = Locale.getDefault()
        val language = locale.language
        if (ipData.isEmpty()) {
            return language == "zh" || language == "fa"
        }
        return ipData == "IR" || ipData == "CN" ||
                ipData == "HK" || ipData == "MO"
    }


    private fun displayCannotUsePopUpBoxes(context: MainActivity) {
        val dialogVpn: AlertDialog = AlertDialog.Builder(context)
            .setTitle("Tip")
            .setMessage("Due to the policy reason , this service is not available in your country")
            .setCancelable(false)
            .setPositiveButton("confirm") { dialog, _ ->
                dialog.dismiss()
                exitProcess(0)
            }.create()
        dialogVpn.setCancelable(false)
        dialogVpn.show()
        dialogVpn.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        dialogVpn.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
    }

    //适配小屏幕
    fun adaptsToSmallScreens(activity: MainActivity) {
        val data = SmileUtils.getScreenMetrics(activity)
        val binding = activity.binding
        if (data.height <= 1900) {
            SmileUtils.resizeView(binding.imgBg, -1, 730)
//            SmileUtils.resizeView(binding.flAd, -1, 600)
        }
    }

}