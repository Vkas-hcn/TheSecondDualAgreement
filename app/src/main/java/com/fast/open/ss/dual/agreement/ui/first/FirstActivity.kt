package com.fast.open.ss.dual.agreement.ui.first

import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.base.BaseActivity
import com.fast.open.ss.dual.agreement.databinding.ActivityFirstBinding
import com.fast.open.ss.dual.agreement.databinding.ActivityMainBinding
import com.fast.open.ss.dual.agreement.model.FirstViewModel
import com.fast.open.ss.dual.agreement.model.MainViewModel

class FirstActivity: BaseActivity<ActivityFirstBinding, FirstViewModel>(
R.layout.activity_first, FirstViewModel::class.java
) {
    override fun intiView() {

    }

    override fun initData() {
    }
}