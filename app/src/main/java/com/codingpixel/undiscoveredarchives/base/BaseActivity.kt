package com.codingpixel.undiscoveredarchives.base

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.utils.HideUtil
import com.karumi.dexter.PermissionToken
import java.util.*
import androidx.activity.result.ActivityResult
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.codingpixel.undiscoveredarchives.utils.NetworkChangeReceiver
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import io.ak1.pix.helpers.getScreenSize
import io.ak1.pix.utility.WIDTH
import org.json.JSONObject

open class BaseActivity : AppCompatActivity(), NetworkChangeReceiver.ConnectivityReceiverListener {

    private var alert: androidx.appcompat.app.AlertDialog? = null
    lateinit var mViewGroup: ViewGroup
    var fontBold: Typeface? = null
    var fontSemiBold: Typeface? = null
    var fontRegular: Typeface? = null

    /*Facebook Login*/
    private var fbCallBackManager: CallbackManager? = null

    protected val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)
    private var mNetworkReceiver: BroadcastReceiver? = null


//    open lateinit var sp: SharedPreferences

    open fun onSetupViewGroup(mVG: ViewGroup) {
        mViewGroup = mVG
        HideUtil.init(this, mViewGroup)
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun hoverEffect1(customView: Any): AnimatorSet {

        var animatorSet = AnimatorSet();
        var fadeOut: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 1.0f, 0.1f)
        fadeOut.duration = 100
        var fadeIn: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 0.1f, 1.0f)
        fadeIn.duration = 100
        animatorSet.play(fadeIn).after(fadeOut)


        return animatorSet


    }

    fun String.isValidEmail(): Boolean =
        this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun String.isValidMobileNumber(): Boolean =
        this.isNotEmpty() && Patterns.PHONE.matcher(this).matches()

    @SuppressLint("ObjectAnimatorBinding")
    fun hoverEffect2(customView: Any) {

        var animatorSet = AnimatorSet()
        var fadeOut: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 1.0f, 0.1f)
        fadeOut.duration = 100
        var fadeIn: ObjectAnimator = ObjectAnimator.ofFloat(customView, "alpha", 0.1f, 1.0f)
        fadeIn.duration = 100
        animatorSet.play(fadeOut)
        animatorSet.start()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)

        mNetworkReceiver = NetworkChangeReceiver(this)
        registerNetworkBroadcastForNougat()
    }

    public fun hideStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun fullScreen() {
        window?.decorView?.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    fun transparentStatusBar() {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    //Hide show password
    fun hideShowPassword(editText: EditText, toggle: ImageView) {
        if (editText.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
            toggle.setImageResource(R.drawable.ic_hide_password);
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance();
        } else {
            toggle.setImageResource(R.drawable.ic_show_password);
            editText.transformationMethod = PasswordTransformationMethod.getInstance();
        }
        editText.setSelection(editText.text.toString().length)
    }

    fun clearLightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window: Window = activity.window
            window.statusBarColor = ContextCompat
                .getColor(activity, R.color.black)
        }
    }

    fun blackStatusBarIcons() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    fun showInfoToast(context: Context, message: String) {
//        Toasty.info(context, "" + message, Toast.LENGTH_SHORT, true).show();
    }

    @SuppressLint("InlinedApi")
    fun changeStatusBarColor(resourseColor: Int) {
        val window: Window = window

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(applicationContext, resourseColor)
        }

    }


    open fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun <T> List<T>.toArrayList(): ArrayList<T> {
        return ArrayList(this)
    }

    fun isAccessFineLocationGranted(context: Context): Boolean {
        return ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun isOnline(): Boolean {
        try {
            val p1 = Runtime.getRuntime().exec("ping -c 1 www.google.com")
            val returnVal = p1.waitFor()
            return returnVal == 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun showGPSNotEnabledDialog(context: Context) {
        alert = androidx.appcompat.app.AlertDialog.Builder(context)
            .setMessage("Please enable Gps")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//                alert?.dismiss()
            }
            .show()
    }

    fun showPermissionRationale(token: PermissionToken) {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_rationale_title)
            .setMessage(R.string.permission_rationale_message)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                token.cancelPermissionRequest()
            }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                token.continuePermissionRequest()
            }
            .setOnDismissListener { token.cancelPermissionRequest() }
            .show()
    }

    fun checkPermissions(permissionArray: Array<String>): Boolean {
        for (i in permissionArray.indices) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, permissionArray[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun hideViewSlideDown(view: View) {
        if (isPanelShown(view)) {
            // Hide the Panel
            val bottomDown: Animation = AnimationUtils.loadAnimation(
                this,
                R.anim.bottom_down
            )
            view.startAnimation(bottomDown)
            view.visibility = View.GONE
        }
    }

    fun showViewSlideUp(view: View) {
        if (!isPanelShown(view)) {
            // Show the panel
            val bottomUp: Animation = AnimationUtils.loadAnimation(
                this,
                R.anim.bottom_up
            )
            view.startAnimation(bottomUp)
            view.visibility = View.VISIBLE
        }
    }

    fun isPanelShown(view: View): Boolean {
        return view.visibility == View.VISIBLE
    }

    fun googleSignIn(mGoogleSignInClient: GoogleSignInClient, function: (GoogleSignInAccount?) -> Unit) {
        val signInIntent = mGoogleSignInClient.signInIntent
        activityLauncher.launch(signInIntent, object : BaseActivityResult.OnActivityResult<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                Log.d("GoogleResult", result.toString())
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task, function)
            }
        })
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>, function: (GoogleSignInAccount) -> Unit) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.

            function(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google_SignIn", "signInResult:failed code=" + e.statusCode)
        }
    }

    fun facebookLogin(fbLoginButton: LoginButton, function: (JSONObject?) -> Unit) {
        fbCallBackManager = CallbackManager.Factory.create()

        fbLoginButton.setPermissions(listOf("email", "public_profile"))
        fbLoginButton.callOnClick()

        fbLoginButton.registerCallback(
            fbCallBackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    if (loginResult != null) {
                        val request = GraphRequest.newMeRequest(
                            loginResult.accessToken
                        ) { `object`, response ->
                            Log.v("LoginActivity", response.toString())
                            function(`object`)
                        }
                        val parameters = Bundle()
                        parameters.putString("fields", "id,name,email")
                        request.parameters = parameters
                        request.executeAsync()
                    }
                }

                override fun onCancel() {
                    Log.e("Facebook_Login", "Cancelled")
                }

                override fun onError(exception: FacebookException) {
                    Log.e("Facebook_Login", exception.toString())
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fbCallBackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun signInWithApple(function: (AuthResult?) -> Unit) {
//        FirebaseAuth.getInstance().signOut();  // To Logout user, apple account

        val auth = FirebaseAuth.getInstance()

        val scopes = ArrayList<String>()
        scopes.add("email")
        scopes.add("name")

        val provider = OAuthProvider.newBuilder("apple.com")
        provider.scopes = scopes
        // Localize the Apple authentication screen in French.
        provider.addCustomParameter("locale", "en")

        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                Log.d("AppleLogin", "checkPending:onSuccess:$authResult")
                // Get the user profile with authResult.getUser() and
                // authResult.getAdditionalUserInfo(), and the ID
                // token from Apple with authResult.getCredential().

                function(authResult)
            }.addOnFailureListener { e ->
                Log.w("AppleLogin", "checkPending:onFailure", e)
            }
        } else {
            Log.d("AppleLogin", "pending: null")

            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener { authResult ->
                    // Sign-in successful!
                    Log.d("AppleLogin", "activitySignIn:onSuccess:${authResult.user}")
                    function(authResult)
                }
                .addOnFailureListener { e ->
                    Log.w("AppleLogin", "activitySignIn:onFailure", e)
                }
        }
    }

    fun changeFragmentWithoutReCreation(fragment: Fragment, tagFragmentName: String) {

        val mFragmentManager = supportFragmentManager
        val fragmentTransaction = mFragmentManager.beginTransaction()
        //fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        val currentFragment = mFragmentManager.primaryNavigationFragment
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment)
        }

        var fragmentTemp = mFragmentManager.findFragmentByTag(tagFragmentName)
        if (fragmentTemp == null) {
            fragmentTemp = fragment
            fragmentTransaction.add(R.id.fragment_container, fragmentTemp, tagFragmentName)
        } else {
            fragmentTransaction.show(fragmentTemp)
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragmentTemp)
        fragmentTransaction.setReorderingAllowed(true)
        fragmentTransaction.commitNowAllowingStateLoss()
    }

    fun Activity.setupScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        getScreenSize()
    }


    private fun Activity.getScreenSize() {
        WIDTH = DisplayMetrics().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display?.getRealMetrics(this)
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getMetrics(this)
            }
        }.widthPixels
    }


    fun Activity.showStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }


    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    private fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private lateinit var snackBar: Snackbar
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            if (this::snackBar.isInitialized)
                if (snackBar.isShown)
                    snackBar.dismiss()
        } else {
            snackBar =
                Snackbar.make(findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
            snackBar.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChanges()
    }

    open fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String): String {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first).toString() + s.substring(1)
        }
    }

}