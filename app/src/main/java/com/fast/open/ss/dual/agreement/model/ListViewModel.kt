package com.fast.open.ss.dual.agreement.model

import androidx.lifecycle.ViewModel
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.fast.open.ss.dual.agreement.app.App
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.base.SmileAdLoad
import com.fast.open.ss.dual.agreement.bean.VpnServiceBean
import com.fast.open.ss.dual.agreement.ui.list.ListActivity
import com.fast.open.ss.dual.agreement.ui.list.ListServiceAdapter
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ListViewModel:ViewModel() {
    lateinit var skVpnServiceBean: VpnServiceBean
    lateinit var allVpnListData: MutableList<VpnServiceBean>
    lateinit var recentlyVpnListData: MutableList<VpnServiceBean>
    lateinit var listServiceAdapter: ListServiceAdapter
    var ecVpnServiceBeanList: MutableList<VpnServiceBean> = ArrayList()
    lateinit var checkSkVpnServiceBean: VpnServiceBean
    lateinit var checkSkVpnServiceBeanClick: VpnServiceBean

    /**
     * 选中服务器
     */
    fun selectServer(activity: AppCompatActivity, position: Int) {
        if (ecVpnServiceBeanList[position].bloally == checkSkVpnServiceBeanClick.bloally && ecVpnServiceBeanList[position].best_smart == checkSkVpnServiceBeanClick.best_smart) {
            if (!App.vpnLink) {
                App.serviceState = "disconnect"
                activity.finish()
                SmileKey.check_service = Gson().toJson(checkSkVpnServiceBean)
            }
            return
        }
        ecVpnServiceBeanList.forEachIndexed { index, _ ->
            ecVpnServiceBeanList[index].check_smart = position == index
            if (ecVpnServiceBeanList[index].check_smart) {
                checkSkVpnServiceBean = ecVpnServiceBeanList[index]
            }
        }
        listServiceAdapter.notifyDataSetChanged()
        showDisconnectDialog(activity)
    }


    /**
     * 回显服务器
     */
    fun getAllServer() {
        allVpnListData = ArrayList()
        skVpnServiceBean = VpnServiceBean()
        allVpnListData = SmileKey.getAllVpnListData()
        ecVpnServiceBeanList = allVpnListData
        ecVpnServiceBeanList.forEachIndexed { index, _ ->
            if (checkSkVpnServiceBeanClick.best_smart) {
                ecVpnServiceBeanList[0].check_smart = true
            } else {
                ecVpnServiceBeanList[index].check_smart =
                    ecVpnServiceBeanList[index].bloally == checkSkVpnServiceBeanClick.bloally
                ecVpnServiceBeanList[0].check_smart = false
            }
        }
        Log.e(TAG, "ecVpnServiceBeanList=${Gson().toJson(ecVpnServiceBeanList)}")
        listServiceAdapter = ListServiceAdapter(ecVpnServiceBeanList)
    }

     fun returnToHomePage(activity: ListActivity) {
        val res = SmileAdLoad.resultOf(SmileKey.POS_BACK)
        if (res == null) {
            activity.finish()
        } else {
            showBackFun(res,activity)
        }
    }
    private fun showBackFun(it: Any,activity: ListActivity) {
        SmileAdLoad.showFullScreenOf(
            where = SmileKey.POS_BACK,
            context = activity,
            res = it,
            onShowCompleted = {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    activity.finish()
                }
            }
        )
    }
    /**
     * 是否断开连接
     */
    private fun showDisconnectDialog(activity: AppCompatActivity) {
        if (!App.vpnLink) {
            activity.finish()
            App.serviceState = "disconnect"
            SmileKey.check_service = Gson().toJson(checkSkVpnServiceBean)
            return
        }
        val dialog: AlertDialog? = AlertDialog.Builder(activity)
            .setTitle("Are you sure to disconnect current server")
            //设置对话框的按钮
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
                ecVpnServiceBeanList.forEachIndexed { index, _ ->
                    ecVpnServiceBeanList[index].check_smart =
                        (ecVpnServiceBeanList[index].bloally == checkSkVpnServiceBeanClick.bloally && ecVpnServiceBeanList[index].best_smart == checkSkVpnServiceBeanClick.best_smart)
                }
                listServiceAdapter.notifyDataSetChanged()
            }
            .setPositiveButton("DISCONNECT") { dialog, _ ->
                dialog.dismiss()
                activity.finish()
                App.serviceState = "connect"
                SmileKey.check_service = Gson().toJson(checkSkVpnServiceBean)
            }.create()
        val params = dialog!!.window!!.attributes
        params.width = 200
        params.height = 200
        dialog.window!!.attributes = params
        dialog.setCancelable(false)
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
    }
}