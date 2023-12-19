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
import com.codingpixel.undiscoveredarchives.databinding.ActivityForgotPasswordBinding
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.auth.models.LoginModel
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import com.codingpixel.undiscoveredarchives.view_models.AuthViewModel
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        observeApiResponse()
    }

    private fun initViews() {
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
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.btnResetPassword.setOnClickListener {
            Boom(binding.btnResetPassword)
            if (validate())
                forgotPassword()
        }
    }

    private fun validate(): Boolean {
        if (binding.etEmail.text.toString().isBlank()) {
            binding.etEmail.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilEmail.error = "Please enter Email!"
            binding.etEmail.requestFocus()
            return false
        }

        if (!binding.etEmail.text.toString().isValidEmail()) {
            binding.etEmail.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilEmail.error = "Email is invalid!"
            binding.etEmail.requestFocus()
            return false
        }
        return true
    }

        private fun observeApiResponse() {
            viewModel.getApiResponse().observe(this, {
                when (it.status) {
                    ApiStatus.LOADING -> {
                        Loader.showLoader(this) {
                            if (this@ForgotPasswordActivity::apiCall.isInitialized)
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
                            ApiTags.FORGOT_PASSWORD -> {
                                val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                                AppUtils.showToast(this@ForgotPasswordActivity, model.message, true)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    val intent = Intent(this@ForgotPasswordActivity, OtpActivity::class.java)
                                    intent.putExtra("email", binding.etEmail.text.toString())
                                    startActivity(intent)
                                    finish()
                                }, 1000)


                            }
                        }
                    }
                }
            })
        }

    private fun forgotPassword() {
        apiCall = retrofitClient.forgotPassword(email = binding.etEmail.text.toString())
        viewModel.forgotPassword(apiCall)
    }


}