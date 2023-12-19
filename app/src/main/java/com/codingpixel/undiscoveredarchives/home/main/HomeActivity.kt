package com.codingpixel.undiscoveredarchives.home.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.SignInActivity
import com.codingpixel.undiscoveredarchives.auth.SignUpActivity
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityHomeBinding
import com.codingpixel.undiscoveredarchives.home.cart.CartActivity
import com.codingpixel.undiscoveredarchives.home.dialogs.GuestDialog
import com.codingpixel.undiscoveredarchives.home.favorites.FavoritesFragment
import com.codingpixel.undiscoveredarchives.home.notifications.NotificationsFragment
import com.codingpixel.undiscoveredarchives.home.profile.ProfileFragment
import com.codingpixel.undiscoveredarchives.utils.FragmentEnums.*

class HomeActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var currentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blackStatusBarIcons()

        initViews()
        binding.clHomeTab.callOnClick()


        if (intent?.getBooleanExtra("goToProfile", false) == true) {
            binding.clProfileTab.callOnClick()
        }
    }

    private fun initViews() {
        binding.clHomeTab.setOnClickListener(this)
        binding.clFavTab.setOnClickListener(this)
        binding.clNotificationsTab.setOnClickListener(this)
        binding.clProfileTab.setOnClickListener(this)
        binding.clCartTab.setOnClickListener(this)

        Boom(binding.clHomeTab)
        Boom(binding.clFavTab)
        Boom(binding.clNotificationsTab)
        Boom(binding.clProfileTab)

    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.clHomeTab -> {
                bottomBarClick(v)
                currentFragment = HomeFragment()
                changeFragmentWithoutReCreation(currentFragment, HOME_FRAGMENT.type)
            }
            R.id.clFavTab -> {
                if (!checkForGuestUser()) {
                    bottomBarClick(v)
                    currentFragment = FavoritesFragment()
                    changeFragmentWithoutReCreation(currentFragment, FAVORITES_FRAGMENT.type)
                }
            }
            R.id.clNotificationsTab -> {
                if (!checkForGuestUser()) {
                    bottomBarClick(v)
                    currentFragment = NotificationsFragment()
                    changeFragmentWithoutReCreation(currentFragment, NOTIFICATION_FRAGMENT.type)
                }
            }
            R.id.clProfileTab -> {
                if (!checkForGuestUser()) {
                    bottomBarClick(v)
                    currentFragment = ProfileFragment()
                    changeFragmentWithoutReCreation(currentFragment, PROFILE_FRAGMENT.type)
                }
            }
            R.id.clCartTab -> {
                startActivity(Intent(this, CartActivity::class.java))
            }
        }
    }

    private fun checkForGuestUser(): Boolean {
        if (AppController.isGuestUser) {
            val guestUserDialog = GuestDialog(object : GuestDialog.GuestDialogInterface {
                override fun onSignIn() {
                    val i = Intent(this@HomeActivity, SignInActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    finish()
                }

                override fun onSignUp() {
                    val i = Intent(this@HomeActivity, SignUpActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    finish()
                }
            })
            guestUserDialog.show(supportFragmentManager, "GuestUserDialog")

            return true
        }
        return false
    }

    private fun bottomBarClick(v: View) {
        updateBottomBar(v)
    }

    private fun updateBottomBar(v: View) {
        when (v.id) {
            R.id.clHomeTab -> {
                binding.ivHomeTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_tabbar_sel))
                binding.ivFavTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fav_tabbar_unsl))
                binding.ivNotificationsTab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_notification_tabbar_unsl
                    )
                )
                binding.ivProfileTab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_profile_tabbar_unsl
                    )
                )
            }
            R.id.clFavTab -> {
                binding.ivHomeTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_tabbar_unsl))
                binding.ivFavTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fav_tabbar_sel))
                binding.ivNotificationsTab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_notification_tabbar_unsl
                    )
                )
                binding.ivProfileTab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_profile_tabbar_unsl
                    )
                )
            }
            R.id.clNotificationsTab -> {
                binding.ivHomeTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_tabbar_unsl))
                binding.ivFavTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fav_tabbar_unsl))
                binding.ivNotificationsTab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_notification_tabbar_sel
                    )
                )
                binding.ivProfileTab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_profile_tabbar_unsl
                    )
                )
            }
            R.id.clProfileTab -> {
                binding.ivHomeTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_tabbar_unsl))
                binding.ivFavTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fav_tabbar_unsl))
                binding.ivNotificationsTab.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_notification_tabbar_unsl
                    )
                )
                binding.ivProfileTab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_profile_tabbar_sel))
            }
        }
    }

//    inner class HomeViewPager(fa: FragmentActivity) : FragmentStateAdapter(fa) {
//        override fun getItemCount(): Int = 4
//
//        override fun createFragment(position: Int): Fragment {
//            return when (position) {
//                0 -> HomeFragment()
//                1 -> FavoritesFragment()
//                2 -> NotificationsFragment()
//                else -> ProfileFragment()
//            }
//        }
//    }


}