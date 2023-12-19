package com.codingpixel.undiscoveredarchives.home.purchases

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivitySalesPurchasesBinding
import com.codingpixel.undiscoveredarchives.utils.viewVisible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PurchasesActivity : BaseActivity() {

    private lateinit var binding: ActivitySalesPurchasesBinding
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesPurchasesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this

        blackStatusBarIcons()

        initViews()
        addTabs()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Purchases"
        binding.topBar.ivBack.viewVisible()
        binding.topBar.ivBack.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun addTabs() {
        val purchaseViewPager = PurchaseViewPager(this)
        binding.viewPager.adapter = purchaseViewPager
        binding.viewPager.offscreenPageLimit = 2


        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    val v1 = LayoutInflater.from(mContext).inflate(R.layout.item_custom_tab_layout, null) as TextView
                    v1.text = "Purchase History"
                    v1.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                    v1.textSize = 17f
                    tab.customView = v1

                }
                1 -> {
                    val v2 = LayoutInflater.from(mContext).inflate(R.layout.item_custom_tab_layout, null) as TextView
                    v2.text = "Current Orders"
                    v2.textSize = 17f
                    tab.customView = v2
                }
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabTextView = tab?.customView
                tabTextView?.findViewById<TextView>(R.id.tvTabTitle)
                    ?.setTextColor(ContextCompat.getColor(mContext, R.color.black))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val tabTextView = tab?.customView
                tabTextView?.findViewById<TextView>(R.id.tvTabTitle)
                    ?.setTextColor(ContextCompat.getColor(mContext, R.color.view_more_grey))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

    }

    inner class PurchaseViewPager(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0)
                PurchasesFragment()
            else
                CurrentOrdersFragment()
        }

    }
}