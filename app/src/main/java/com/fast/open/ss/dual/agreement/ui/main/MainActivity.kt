package com.fast.open.ss.dual.agreement.ui.main

import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.base.BaseActivity
import com.fast.open.ss.dual.agreement.databinding.ActivityMainBinding
import com.fast.open.ss.dual.agreement.model.MainViewModel

class MainActivity: BaseActivity<ActivityMainBinding, MainViewModel>(
R.layout.activity_main, MainViewModel::class.java
) {
    override fun intiView() {

    }

    override fun initData() {
    }
}