package com.codingpixel.undiscoveredarchives.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.LoginModel
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivitySignInBinding
import com.codingpixel.undiscoveredarchives.home.main.HomeActivity
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.*
import com.codingpixel.undiscoveredarchives.view_models.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call


class SignInActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    /*Google SignIn*/
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var userDataModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        initializeGoogleLogin()
        observeApiResponse()
    }

    private fun initViews() {
        Boom(binding.tvForgotPassword)
        Boom(binding.btnLogin)
        Boom(binding.llRegister)
        Boom(binding.btnFacebookLogin)
        Boom(binding.btnGoogleLogin)
        Boom(binding.btnAppleLogin)
        Boom(binding.ivPasswordToggle)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.tvForgotPassword.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.llRegister.setOnClickListener(this)
        binding.btnFacebookLogin.setOnClickListener(this)
        binding.btnGoogleLogin.setOnClickListener(this)
        binding.btnAppleLogin.setOnClickListener(this)
        binding.ivPasswordToggle.setOnClickListener(this)

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.etEmail.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilEmail.error = null
                binding.tilEmail.isErrorEnabled = false
            }

        })
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.etPassword.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilPassword.error = null
                binding.tilPassword.viewGone()
            }

        })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnLogin -> {
                if (validation())
                    login()
            }
            R.id.tvForgotPassword -> {
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
            }
            R.id.llRegister -> {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            R.id.ivPasswordToggle -> {
                AppUtils.hideShowPassword(binding.etPassword, binding.ivPasswordToggle)
            }
            R.id.btnGoogleLogin -> {
                if (::mGoogleSignInClient.isInitialized) {
//                    googleSignIn(mGoogleSignInClient) {
                    googleSignIn(mGoogleSignInClient) {
                        if (it != null) {
                            socialLogin(
                                "gmail", it.id ?: "",
                                it.displayName ?: "", it.email ?: ""
                            )
                        }
                    }
                }
//                }
            }
            R.id.btnFacebookLogin -> {
                facebookLogin(binding.fbLoginButton) {
                    if (it != null) {
                        socialLogin(
                            "facebook", it.getString("id"),
                            it.getString("name"), it.getString("email")
                        )
                    }
                }
            }
            R.id.btnAppleLogin -> {
                signInWithApple {
                    if (it != null) {
                        if (it.user != null)
                            socialLogin(
                                "apple", it.user?.providerId ?: "",
                                it.user?.displayName ?: "", it.user?.email ?: ""
                            )
                    } else
                        AppUtils.showToast(this@SignInActivity, "Apple Failed", false)
                }
            }
        }
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this) {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this) {
                        if (this@SignInActivity::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    when (it.tag) {
                        ApiTags.SOCIAL_LOGIN -> {
                            userDataModel = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            SharedPreference.saveString(
                                this@SignInActivity,
                                Constants.accessToken,
                                userDataModel.access_token
                            )
                            registerDevice()
                        }
                        ApiTags.LOGIN -> {
                            userDataModel = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            SharedPreference.saveString(
                                this@SignInActivity,
                                Constants.accessToken,
                                userDataModel.access_token
                            )
                            registerDevice()
                        }
                        ApiTags.REGISTER_DEVICE -> {
                            Loader.hideLoader()

                            AppController.isGuestUser = false

                            SharedPreference.saveBoolean(this@SignInActivity, Constants.isUserLoggedIn, true)
                            AppUtils.saveUserModel(this@SignInActivity, userDataModel.data)
                            startActivity(Intent(this@SignInActivity, HomeActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        }
    }


    private fun initializeGoogleLogin() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun socialLogin(providerName: String, providerId: String, name: String, email: String) {
        apiCall = retrofitClient.socialLogin(providerName, providerId, name, email)
        viewModel.socialLogin(apiCall)
    }

    private fun login() {
        apiCall = retrofitClient.login(binding.etEmail.text.toString(), binding.etPassword.text.toString())
        viewModel.login(apiCall)
    }

    @SuppressLint("HardwareIds")
    private fun registerDevice() {
        val deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)
        apiCall = retrofitClient.registerDevice(
            userDataModel.access_token,
            deviceId, "Android",
            "dev", getDeviceName()
        )
        viewModel.registerDevice(apiCall)
    }

    private fun validation(): Boolean {
        val email = binding.etEmail
        val password = binding.etPassword

        if (email.text.toString().isBlank()) {
            email.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilEmail.error = "Enter Email!"
            email.requestFocus()
            return false
        } else if (!email.text.toString().isValidEmail()) {
            email.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilEmail.error = "Email is invalid!"
            email.requestFocus()
            return false
        } else if (password.text.toString().isBlank()) {
            password.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilPassword.error = "Enter Password!"
            binding.tilPassword.viewVisible()
            password.requestFocus()
            return false
        }
        return true
    }

}