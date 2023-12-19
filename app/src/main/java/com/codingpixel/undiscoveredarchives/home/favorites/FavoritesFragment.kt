package com.codingpixel.undiscoveredarchives.home.favorites

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.FragmentFavoritesBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        addTabs()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Favorites"
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun addTabs() {
        val favPagerAdapter = FavoriteViewPager(requireActivity())
        binding.viewPager.adapter = favPagerAdapter
        binding.viewPager.offscreenPageLimit = 2
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    val v1 = LayoutInflater.from(mContext).inflate(R.layout.item_custom_tab_layout, null) as TextView
                    v1.text = "Items"
                    v1.setTextColor(ContextCompat.getColor(mContext, R.color.black))
                    v1.textSize = 17f
                    tab.customView = v1

                }
                1 -> {
                    val v2 = LayoutInflater.from(mContext).inflate(R.layout.item_custom_tab_layout, null) as TextView
                    v2.text = "Designers"
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

    inner class FavoriteViewPager(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0)
                FavItemsFragment()
            else
                FavDesignersFragment()
        }

    }

}
