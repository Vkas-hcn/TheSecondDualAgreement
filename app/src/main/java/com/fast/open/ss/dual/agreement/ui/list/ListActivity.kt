package com.fast.open.ss.dual.agreement.ui.list

import android.util.Log
import android.view.KeyEvent
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.app.App.Companion.TAG
import com.fast.open.ss.dual.agreement.base.BaseActivity
import com.fast.open.ss.dual.agreement.bean.VpnServiceBean
import com.fast.open.ss.dual.agreement.databinding.ActivityListBinding
import com.fast.open.ss.dual.agreement.model.ListViewModel
import com.fast.open.ss.dual.agreement.utils.SmileKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListActivity: BaseActivity<ActivityListBinding, ListViewModel>(
    R.layout.activity_list, ListViewModel::class.java
) {
    override fun intiView() {
        binding.imgBack.setOnClickListener {
            viewModel.returnToHomePage(this)
        }

    }

    override fun initData() {
        viewModel.checkSkVpnServiceBean = VpnServiceBean()
        viewModel.checkSkVpnServiceBean = Gson().fromJson(
            SmileKey.check_service,
            object : TypeToken<VpnServiceBean?>() {}.type
        )
        viewModel.checkSkVpnServiceBeanClick = viewModel.checkSkVpnServiceBean
        viewModel.initAllAdapter(this) { activity, position ->
            viewModel.selectServer(activity, position)
        }
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            viewModel.returnToHomePage(this)
        }
        return true
    }
}