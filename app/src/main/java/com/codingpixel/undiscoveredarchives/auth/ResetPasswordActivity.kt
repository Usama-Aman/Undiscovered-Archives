package com.codingpixel.undiscoveredarchives.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModelProviders
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityResetPasswordBinding
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.auth.models.LoginModel
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible
import com.codingpixel.undiscoveredarchives.view_models.AuthViewModel
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class ResetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var otp = ""
    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        observeApiResponse()
    }

    private fun initViews() {
        otp = intent?.getStringExtra("otp") ?: ""
        email = intent?.getStringExtra("email") ?: ""
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.ivPasswordToggle.setOnClickListener {
            hideShowPassword(binding.etPassword, binding.ivPasswordToggle)
        }
        binding.ivConfirmPasswordToggle.setOnClickListener {
            hideShowPassword(binding.etRenterPassword, binding.ivConfirmPasswordToggle)
        }

        binding.btnSavePassword.setOnClickListener {
            Boom(binding.btnSavePassword)
            if (validation())
                resetPassword()
        }
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etPassword.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilPassword.error = null
                binding.tilPassword.viewGone()
            }
        })
        binding.etRenterPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etRenterPassword.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilReEnterPassword.error = null
                binding.tilReEnterPassword.viewGone()
            }
        })
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this) {
                        if (this@ResetPasswordActivity::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.RESET_PASSWORD -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this@ResetPasswordActivity, model.message, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                startActivity(Intent(this@ResetPasswordActivity, SignInActivity::class.java))
                                finish()
                            }, 1000)
                        }
                    }
                }
            }
        })
    }

    private fun resetPassword() {
        apiCall =
            retrofitClient.resetPassword(
                otp = otp,
                email = email,
                password = binding.etPassword.text.toString(),
                password_confirmation = binding.etRenterPassword.text.toString()
            )
        viewModel.resetPassword(apiCall)
    }
    private fun validation(): Boolean {
        val password = binding.etPassword
        val confirmPassword = binding.etRenterPassword

        if (password.text.toString().isBlank()) {
            password.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilPassword.error = "Enter Password!"
            binding.tilPassword.viewVisible()
            password.requestFocus()
            return false
        }
        if (password.text.toString().length < 8) {
            password.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilPassword.error = "Password must be at least 8 characters long!"
            binding.tilPassword.viewVisible()
            password.requestFocus()
            return false
        }
        if (confirmPassword.text.toString().isBlank()) {
            confirmPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilReEnterPassword.viewVisible()
            binding.tilReEnterPassword.error = "Re-enter Password!"
            confirmPassword.requestFocus()
            return false
        }
        if (confirmPassword.text.toString() != password.text.toString()) {
            confirmPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilReEnterPassword.viewVisible()
            binding.tilReEnterPassword.error = "Re-entered Password doesn't match!"
            confirmPassword.requestFocus()
            return false
        }
        return true
    }

}