package com.codingpixel.undiscoveredarchives.welcome_slider

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityWelcomeBinding
import com.codingpixel.undiscoveredarchives.home.main.HomeActivity
import com.codingpixel.undiscoveredarchives.utils.Constants
import com.codingpixel.undiscoveredarchives.utils.SharedPreference
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class WelcomeActivity : BaseActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private var googleAccount: GoogleSignInAccount? = null
    private var facebookAccessToken: AccessToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)

        changeStatusBarColor(R.color.white)
        blackStatusBarIcons()

        /*To check whether is already sign in or not do it in splash*/
        googleAccount = GoogleSignIn.getLastSignedInAccount(this)
        facebookAccessToken = AccessToken.getCurrentAccessToken()

        initListeners()
        setUpPager()
    }

    private fun initListeners() {
        binding.btnSkip.setOnClickListener {
            AppController.isGuestUser = false
            SharedPreference.saveBoolean(this, Constants.isWelcomeDone, true)
            checkForLoggedIn()
        }
    }

    private fun checkForLoggedIn() {
        if (SharedPreference.getBoolean(this, Constants.isUserLoggedIn)) {
            if (googleAccount != null)
                if (!googleAccount!!.isExpired) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            if (facebookAccessToken != null) {
                if (!facebookAccessToken!!.isExpired) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        } else {
            AppController.isGuestUser = true
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun setUpPager() {
        val welcomePager = WelcomeScreenPager(this)
        binding.welcomeSlider.adapter = welcomePager

        binding.welcomeSlider.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    updateDots(position)
                }
            })
    }

    private fun updateDots(position: Int) {

        when (position) {
            0 -> {
                binding.dot1.background = ContextCompat.getDrawable(this, R.drawable.slider_circles_black)
                binding.dot2.background = ContextCompat.getDrawable(this, R.drawable.slider_circles_gray)
                binding.dot3.background = ContextCompat.getDrawable(this, R.drawable.slider_circles_gray)
            }
            1 -> {
                binding.dot1.background = ContextCompat.getDrawable(this, R.drawable.slider_circles_gray)
                binding.dot2.background = ContextCompat.getDrawable(this, R.drawable.slider_circles_black)
                binding.dot3.background = ContextCompat.getDrawable(this, R.drawable.slider_circles_gray)
            }
            else -> {
                binding.dot1.background = ContextCompat.getDrawable(this, R.drawable.slider_circles_gray)
                binding.dot2.background = ContextCompat.getDrawable(this, R.drawable.slider_circles_gray)
                binding.dot3.background = ContextCompat.getDrawable(this, R.drawable.slider_circles_black)
            }
        }

    }


    inner class WelcomeScreenPager(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> WelcomeScreenOne()
                1 -> WelcomeScreenTwo()
                else -> WelcomeScreenThree()
            }
        }
    }

}