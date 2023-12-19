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
import com.codingpixel.undiscoveredarchives.databinding.ActivityOtpBinding
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

class OtpActivity : BaseActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        observeApiResponse()
    }

    private fun initViews() {
        email = intent?.getStringExtra("email") ?: ""
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.btnVerify.setOnClickListener {
            Boom(binding.btnVerify)
            if (validate())
                verifyOTP()
        }

        binding.llResendCode.setOnClickListener {
            Boom(binding.llResendCode)
            resendOTP()
        }

        binding.etCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null)
                    setOtpNumbers(otp = s.toString())
                else
                    setOtpNumbers(otp = "")
                binding.tvDigit1.setBackgroundResource(R.drawable.bg_et_otp)
                binding.tvDigit2.setBackgroundResource(R.drawable.bg_et_otp)
                binding.tvDigit3.setBackgroundResource(R.drawable.bg_et_otp)
                binding.tvDigit4.setBackgroundResource(R.drawable.bg_et_otp)
                binding.tilError.error = null
                binding.tilError.viewGone()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }


    private fun setOtpNumbers(otp: String) {
        when {
            otp.isEmpty() -> {
                binding.tvDigit1.text = ""
                binding.tvDigit2.text = ""
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""
            }
            otp.length == 1 -> {
                binding.tvDigit1.text = otp
                binding.tvDigit2.text = ""
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""
            }
            otp.length == 2 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = ""
                binding.tvDigit4.text = ""
            }
            otp.length == 3 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = otp.substring(2, 3)
                binding.tvDigit4.text = ""
            }
            otp.length == 4 -> {
                binding.tvDigit1.text = otp.substring(0, 1)
                binding.tvDigit2.text = otp.substring(1, 2)
                binding.tvDigit3.text = otp.substring(2, 3)
                binding.tvDigit4.text = otp.substring(3, 4)
            }
        }
    }


    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this) {
                        if (this@OtpActivity::apiCall.isInitialized)
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
                        ApiTags.VERIFY_OTP -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this@OtpActivity, model.message, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this@OtpActivity, ResetPasswordActivity::class.java)
                                intent.putExtra("otp", binding.etCode.text.toString())
                                intent.putExtra("email", email)
                                startActivity(intent)
                                finish()
                            }, 1000)
                        }
                        ApiTags.RESEND_OTP -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this@OtpActivity, model.message, true)
                        }
                    }
                }
            }
        })
    }

    private fun resendOTP() {
        apiCall = retrofitClient.resendOtp(email = email)
        viewModel.resendOTP(apiCall)
    }

    private fun verifyOTP() {
        apiCall = retrofitClient.verifyOTP(email = email, otp = binding.etCode.text.toString())
        viewModel.verifyOTP(apiCall)
    }

    private fun validate(): Boolean {
        if (binding.tvDigit1.text.toString().isBlank()) {
            binding.tvDigit1.setBackgroundResource(R.drawable.bg_et_otp_error)
            binding.tilError.viewVisible()
            binding.tilError.error = "Enter OTP!"
            return false
        }
        if (binding.tvDigit2.text.toString().isBlank()) {
            binding.tvDigit2.setBackgroundResource(R.drawable.bg_et_otp_error)
            binding.tilError.viewVisible()
            binding.tilError.error = "Enter OTP!"
            return false
        }
        if (binding.tvDigit3.text.toString().isBlank()) {
            binding.tvDigit3.setBackgroundResource(R.drawable.bg_et_otp_error)
            binding.tilError.viewVisible()
            binding.tilError.error = "Enter OTP!"
            return false
        }
        if (binding.tvDigit4.text.toString().isBlank()) {
            binding.tvDigit4.setBackgroundResource(R.drawable.bg_et_otp_error)
            binding.tilError.viewVisible()
            binding.tilError.error = "Enter OTP!"
            return false
        }
        return true
    }


}