package com.codingpixel.undiscoveredarchives.home.manage_paypal

import android.os.Bundle
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityManagePaypalBinding
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class ManagePaypalActivity : BaseActivity() {
    private lateinit var binding: ActivityManagePaypalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagePaypalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        blackStatusBarIcons()

        initViews()
        initListeners()
    }

    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Manage PayPal Account"
        binding.topBar.ivBack.viewVisible()
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener {
            finish()
        }
    }
}