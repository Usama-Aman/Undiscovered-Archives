package com.codingpixel.undiscoveredarchives.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.home.main.HomeActivity
import com.codingpixel.undiscoveredarchives.utils.Constants
import com.codingpixel.undiscoveredarchives.utils.SharedPreference
import com.codingpixel.undiscoveredarchives.welcome_slider.WelcomeActivity
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class SplashActivity : BaseActivity() {

    private var googleAccount: GoogleSignInAccount? = null
    private var facebookAccessToken: AccessToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        fullScreen()

        /*To check whether is already sign in or not do it in splash*/
        googleAccount = GoogleSignIn.getLastSignedInAccount(this)
        facebookAccessToken = AccessToken.getCurrentAccessToken()

        Handler(Looper.getMainLooper()).postDelayed({
            getLinkFromIntent(intent)
        }, 1000)

//        printHashKey(this)
    }

    private fun checkForLoggedIn() {
        if (SharedPreference.getBoolean(this@SplashActivity, Constants.isWelcomeDone)) {
            if (SharedPreference.getBoolean(this@SplashActivity, Constants.isUserLoggedIn)) {
                if (googleAccount != null)
                    if (!googleAccount!!.isExpired) {
                        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                        finish()
                    }
                if (facebookAccessToken != null) {
                    if (!facebookAccessToken!!.isExpired) {
                        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                    finish()
                }
            } else {
                AppController.isGuestUser = true
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
            finish()
        }
    }

//    @SuppressLint("PackageManagerGetSignatures")
//    private fun printHashKey(pContext: Context) {
//        try {
//            val info: PackageInfo = pContext.packageManager
//                .getPackageInfo(pContext.packageName, PackageManager.GET_SIGNATURES)
//            for (signature in info.signatures) {
//                val md: MessageDigest = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                val hashKey: String = String(Base64.encode(md.digest(), 0))
//                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
//            }
//        } catch (e: NoSuchAlgorithmException) {
//            Log.e("TAG", "printHashKey()", e)
//        } catch (e: Exception) {
//            Log.e("TAG", "printHashKey()", e)
//        }
//    }

    private fun getLinkFromIntent(intent: Intent?) {
        val data = intent?.data
        AppController.profileReferralCode = data?.getQueryParameter(Constants.referralCode).toString()
        checkForLoggedIn()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getLinkFromIntent(intent)
    }

}