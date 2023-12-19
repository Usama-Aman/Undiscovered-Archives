package com.codingpixel.undiscoveredarchives.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.utils.maskeditor.MaskTextWatcher
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.*
import com.codingpixel.undiscoveredarchives.base.AppController
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.base.BaseActivityResult
import com.codingpixel.undiscoveredarchives.databinding.ActivitySignUpBinding
import com.codingpixel.undiscoveredarchives.home.main.HomeActivity
import com.codingpixel.undiscoveredarchives.home.searchable_spinner.SearchableSpinnerActivity
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


class SignUpActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var isVendor = 1  // 0 for user and 1 for vendor
    private var selectedCountryId = -1
    private var selectedStateId = -1
    private var selectedCityId = -1

    private var countriesList: MutableList<CountriesData> = ArrayList()
    private var statesList: MutableList<StatesData> = ArrayList()
    private var citiesList: MutableList<CitiesData> = ArrayList()
    private var searchableList: ArrayList<SearchableSpinnerModel> = ArrayList()

    /*Google SignIn*/
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var userDataModel: LoginModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        initializeGoogleLogin()
        observeApiResponse()

        getCountries()
        setEditTexts()
    }

    private fun initViews() {
        val mobileNumberWatcher = MaskTextWatcher(binding.etPhoneNumber, "###-###-####")
        binding.etPhoneNumber.addTextChangedListener(mobileNumberWatcher)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.btnCreateAccount.setOnClickListener {
            if (validate())
                register()
        }
        binding.llLogin.setOnClickListener(this)
        binding.llUser.setOnClickListener(this)
        binding.llVendor.setOnClickListener(this)
        binding.countriesSpinner.setOnClickListener(this)
        binding.statesSpinner.setOnClickListener(this)
        binding.citiesSpinner.setOnClickListener(this)
        binding.ivPasswordToggle.setOnClickListener(this)
        binding.ivConfirmPasswordToggle.setOnClickListener(this)
        binding.btnGoogleSignUp.setOnClickListener(this)
        binding.btnFacebookSignUp.setOnClickListener(this)
        binding.btnAppleSignUp.setOnClickListener(this)
        binding.llLogin.setOnClickListener(this)

        Boom(binding.btnCreateAccount)
        Boom(binding.llLogin)
        Boom(binding.llUser)
        Boom(binding.llVendor)
        Boom(binding.countriesSpinner)
        Boom(binding.statesSpinner)
        Boom(binding.citiesSpinner)
        Boom(binding.ivPasswordToggle)
        Boom(binding.ivConfirmPasswordToggle)
        Boom(binding.btnGoogleSignUp)
        Boom(binding.btnFacebookSignUp)
        Boom(binding.btnAppleSignUp)
        Boom(binding.llLogin)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llLogin -> {
                finish()
            }
            R.id.llUser -> {
                isVendor = 0
                binding.ivUser.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_buyer_selected))
                binding.tvUser.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.ivVendor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_seller_unselected))
                binding.tvVendor.setTextColor(ContextCompat.getColor(this, R.color.auth_edittext_border))
                binding.llUser.setBackgroundResource(R.drawable.module_black_signup_drawable)
                binding.llVendor.setBackgroundResource(R.drawable.module_grey_signup_drawable)

                binding.tvCompanyName.viewGone()
                binding.tilCompanyName.viewGone()
                binding.llCountry.viewGone()
                binding.llCity.viewGone()
                binding.llMobileNumber.viewGone()
                binding.tvStreetAddress.viewGone()
                binding.tilStreet.viewGone()

            }
            R.id.llVendor -> {
                isVendor = 1
                binding.ivVendor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_seller))
                binding.tvVendor.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.ivUser.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_buyer))
                binding.tvUser.setTextColor(ContextCompat.getColor(this, R.color.auth_edittext_border))
                binding.llVendor.setBackgroundResource(R.drawable.module_black_signup_drawable)
                binding.llUser.setBackgroundResource(R.drawable.module_grey_signup_drawable)

                binding.tvCompanyName.viewVisible()
                binding.tilCompanyName.viewVisible()
                binding.llCountry.viewVisible()
                binding.llCity.viewVisible()
                binding.llMobileNumber.viewVisible()
                binding.tvStreetAddress.viewVisible()
                binding.tilStreet.viewVisible()

            }
            R.id.countriesSpinner -> {
                searchableList.clear()
                countriesList.forEachIndexed { _, countriesData ->
                    searchableList.add(
                        SearchableSpinnerModel(
                            countriesData.id, countriesData.name, countriesData.id == selectedCountryId
                        )
                    )
                }

                val intent = Intent(this, SearchableSpinnerActivity::class.java)
                intent.putParcelableArrayListExtra("listData", searchableList)
                intent.putExtra("title", "Select Country")
                activityLauncher.launch(intent, object : BaseActivityResult.OnActivityResult<ActivityResult> {
                    override fun onActivityResult(result: ActivityResult) {
                        if (result.resultCode == Activity.RESULT_OK) {
                            selectedCountryId = result.data?.getIntExtra("selectedId", -1) ?: -1
                            binding.countriesSpinner.text =
                                result.data?.getStringExtra("selectedValue")

                            selectedStateId = -1
                            binding.statesSpinner.text = ""
                            selectedCityId = -1
                            binding.citiesSpinner.text = ""
                            getStates()
                        }
                    }
                })
            }
            R.id.statesSpinner -> {
                searchableList.clear()
                statesList.forEachIndexed { _, statesData ->
                    searchableList.add(
                        SearchableSpinnerModel(
                            statesData.id, statesData.name, statesData.id == selectedStateId
                        )
                    )
                }
                if (binding.countriesSpinner.text.toString().isNotBlank()) {
                    val intent = Intent(this, SearchableSpinnerActivity::class.java)
                    intent.putParcelableArrayListExtra("listData", searchableList)
                    intent.putExtra("title", "Select State")
                    activityLauncher.launch(
                        intent,
                        object : BaseActivityResult.OnActivityResult<ActivityResult> {
                            override fun onActivityResult(result: ActivityResult) {
                                if (result.resultCode == Activity.RESULT_OK) {
                                    selectedStateId =
                                        result.data?.getIntExtra("selectedId", -1) ?: -1
                                    binding.statesSpinner.text =
                                        result.data?.getStringExtra("selectedValue")

                                    selectedCityId = -1
                                    binding.citiesSpinner.text = ""
                                    getCities()
                                }
                            }
                        })
                } else {
                    binding.countriesSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
                    binding.tilSelectCountry.viewVisible()
                    binding.tilSelectCountry.error = "Select Country first!"
                }

            }
            R.id.citiesSpinner -> {
                searchableList.clear()
                citiesList.forEachIndexed { _, citiesData ->
                    searchableList.add(
                        SearchableSpinnerModel(
                            citiesData.id, citiesData.name, citiesData.id == selectedCityId
                        )
                    )
                }
                if (binding.statesSpinner.text.toString().isNotBlank()) {
                    val intent = Intent(this, SearchableSpinnerActivity::class.java)
                    intent.putParcelableArrayListExtra("listData", searchableList)
                    intent.putExtra("title", "Select City")
                    activityLauncher.launch(
                        intent,
                        object : BaseActivityResult.OnActivityResult<ActivityResult> {
                            override fun onActivityResult(result: ActivityResult) {
                                if (result.resultCode == Activity.RESULT_OK) {
                                    selectedCityId =
                                        result.data?.getIntExtra("selectedId", -1) ?: -1
                                    binding.citiesSpinner.text =
                                        result.data?.getStringExtra("selectedValue")
                                }
                            }
                        })
                } else {
                    binding.statesSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
                    binding.tilSelectState.viewVisible()
                    binding.tilSelectState.error = "Select State first!"
                }
            }
            R.id.ivPasswordToggle -> {
                AppUtils.hideShowPassword(binding.etPassword, binding.ivPasswordToggle)
            }
            R.id.ivConfirmPasswordToggle -> {
                AppUtils.hideShowPassword(
                    binding.etConfirmPassword,
                    binding.ivConfirmPasswordToggle
                )
            }
            R.id.btnGoogleSignUp -> {
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
            R.id.btnFacebookSignUp -> {
                facebookLogin(binding.fbLoginButton) {
                    if (it != null) {
                        socialLogin(
                            "facebook", it.getString("id"),
                            it.getString("name"), it.getString("email")
                        )
                    }
                }
            }
            R.id.btnAppleSignUp -> {
                signInWithApple {
                    if (it != null) {
                        if (it.user != null)
                            socialLogin(
                                "apple", it.user?.providerId ?: "",
                                it.user?.displayName ?: "", it.user?.email ?: ""
                            )
                    } else
                        AppUtils.showToast(this@SignUpActivity, "Apple Failed", false)
                }
            }
        }
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this) {
                        if (this@SignUpActivity::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    when (it.tag) {
                        ApiTags.GET_COUNTRIES -> {
                            Loader.hideLoader()
                            val model =
                                Gson().fromJson(it.data.toString(), CountriesModel::class.java)
                            countriesList.clear()
                            countriesList.addAll(model.data)
                        }
                        ApiTags.GET_STATES -> {
                            Loader.hideLoader()
                            val model = Gson().fromJson(it.data.toString(), StatesModel::class.java)
                            statesList.clear()
                            statesList.addAll(model.data)
                        }
                        ApiTags.GET_CITIES -> {
                            Loader.hideLoader()
                            val model = Gson().fromJson(it.data.toString(), CitiesModel::class.java)
                            citiesList.clear()
                            citiesList.addAll(model.data)
                        }
                        ApiTags.SOCIAL_LOGIN -> {
                            userDataModel = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            SharedPreference.saveString(
                                this@SignUpActivity,
                                Constants.accessToken,
                                userDataModel.access_token
                            )
                            registerDevice()
                        }
                        ApiTags.REGISTER -> {
                            userDataModel = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            SharedPreference.saveString(
                                this@SignUpActivity,
                                Constants.accessToken,
                                userDataModel.access_token
                            )
                            registerDevice()
                        }
                        ApiTags.REGISTER_DEVICE -> {
                            Loader.hideLoader()

                            AppController.isGuestUser = false

                            SharedPreference.saveBoolean(this@SignUpActivity, Constants.isUserLoggedIn, true)
                            SharedPreference.saveString(
                                this@SignUpActivity,
                                Constants.accessToken,
                                userDataModel.access_token
                            )
                            AppUtils.saveUserModel(this@SignUpActivity, userDataModel.data)
                            startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        })
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

    private fun getCountries() {
        apiCall = retrofitClient.getCountries("country")
        viewModel.getCountries(apiCall)
    }

    private fun getStates() {
        apiCall = retrofitClient.getStates("state", selectedCountryId)
        viewModel.getStates(apiCall)
    }

    private fun getCities() {
        apiCall = retrofitClient.getCities("city", selectedCountryId, selectedStateId)
        viewModel.getCities(apiCall)
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

    private fun setEditTexts() {
        binding.etFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etFullName.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilFullName.error = null
                binding.tilFullName.isErrorEnabled = false
            }
        })
        binding.etCompanyName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etCompanyName.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilCompanyName.error = null
                binding.tilCompanyName.isErrorEnabled = false
            }
        })
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etEmail.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilEmail.error = null
                binding.tilEmail.isErrorEnabled = false
            }
        })
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etPhoneNumber.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilPhoneNumber.error = null
                binding.tilPhoneNumber.isErrorEnabled = false
            }
        })
        binding.countriesSpinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.countriesSpinner.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilSelectCountry.viewGone()
                binding.tilSelectCountry.viewGone()
            }
        })
        binding.statesSpinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.statesSpinner.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilSelectState.viewGone()
                binding.tilSelectState.isErrorEnabled = false
            }
        })
        binding.citiesSpinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.citiesSpinner.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilSelectCity.viewGone()
                binding.tilSelectCity.isErrorEnabled = false
            }
        })
        binding.etZipCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etZipCode.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilZipCode.error = null
                binding.tilZipCode.isErrorEnabled = false
            }
        })
        binding.etStreetAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etStreetAddress.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilStreet.error = null
                binding.tilStreet.isErrorEnabled = false
            }
        })
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etPassword.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilPassword.error = null
                binding.tilPassword.viewGone()
            }
        })
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etConfirmPassword.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilConfirmPassword.error = null
                binding.tilConfirmPassword.viewGone()
            }
        })
    }

    private fun validate(): Boolean {
        if (binding.etFullName.text.toString().isBlank()) {
            binding.etFullName.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilFullName.error = "Please enter your Full Name!"
            binding.etFullName.requestFocus()
            return false
        }

        if (isVendor == 1)
            if (binding.etCompanyName.text.toString().isBlank()) {
                binding.etCompanyName.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilCompanyName.error = "Please enter Company Name!"
                binding.etCompanyName.requestFocus()
                return false
            }

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
        if (isVendor == 1) {
            if (binding.etPhoneNumber.text.toString().isBlank()) {
                binding.etPhoneNumber.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilPhoneNumber.error = "Please enter mobile number!"
                binding.etPhoneNumber.requestFocus()
                return false
            }

            if (!binding.etPhoneNumber.text.toString().replace("-", "").isValidMobileNumber()) {
                binding.etPhoneNumber.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilPhoneNumber.error = "Mobile number is invalid!"
                binding.etPhoneNumber.requestFocus()
                return false
            }

            if (binding.countriesSpinner.text.toString().isBlank()) {
                binding.countriesSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilSelectCountry.viewVisible()
                binding.tilSelectCountry.error = "Select Country!"
                binding.countriesSpinner.requestFocus()
                return false
            }

            if (binding.statesSpinner.text.toString().isBlank()) {
                binding.statesSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilSelectState.viewVisible()
                binding.tilSelectState.error = "Select State!"
                binding.statesSpinner.requestFocus()
                return false
            }
            if (binding.citiesSpinner.text.toString().isBlank()) {
                binding.citiesSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilSelectCity.viewVisible()
                binding.tilSelectCity.error = "Select City!"
                binding.citiesSpinner.requestFocus()
                return false
            }
            if (binding.etZipCode.text.toString().isBlank()) {
                binding.etZipCode.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilZipCode.viewVisible()
                binding.tilZipCode.error = "Enter Zip Code!"
                binding.etZipCode.requestFocus()
                return false
            }
            if (binding.etStreetAddress.text.toString().isBlank()) {
                binding.etStreetAddress.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilStreet.viewVisible()
                binding.tilStreet.error = "Enter Street Address!"
                binding.etStreetAddress.requestFocus()
                return false
            }
        }
        if (binding.etPassword.text.toString().isBlank()) {
            binding.etPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilPassword.viewVisible()
            binding.tilPassword.error = "Enter Password!"
            binding.etPassword.requestFocus()
            return false
        }
        if (binding.etPassword.text.toString().length < 8) {
            binding.etPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilPassword.viewVisible()
            binding.tilPassword.error = "Password must be at least 8 characters long!"
            binding.etPassword.requestFocus()
            return false
        }

        if (binding.etConfirmPassword.text.toString().isBlank()) {
            binding.etConfirmPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilConfirmPassword.viewVisible()
            binding.tilConfirmPassword.error = "Enter Confirm Password!"
            binding.etConfirmPassword.requestFocus()
            return false
        }
        if (binding.etConfirmPassword.text.toString() != binding.etPassword.text.toString()) {
            binding.etConfirmPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilConfirmPassword.viewVisible()
            binding.tilConfirmPassword.error = "Confirm Password doesn't match!"
            binding.etConfirmPassword.requestFocus()
            return false
        }
        return if (!binding.checkboxName.isChecked) {
            AppUtils.showToast(this@SignUpActivity, "Please accept our terms and conditions", false)
            false
        } else true

    }

    private fun socialLogin(providerName: String, providerId: String, name: String, email: String) {
        apiCall = retrofitClient.socialLogin(providerName, providerId, name, email)
        viewModel.socialLogin(apiCall)
    }

    private fun register() {
        apiCall = retrofitClient.register(
            isVendor, binding.etFullName.text.toString(), binding.etFullName.text.toString(),
            binding.etEmail.text.toString(), binding.countryCodePicker.selectedCountryCodeWithPlus,
            binding.etPhoneNumber.text.toString().replace("-", ""),
            selectedCountryId, selectedStateId, selectedCityId, binding.etZipCode.text.toString(),
            binding.etStreetAddress.text.toString(), binding.etPassword.text.toString(),
            binding.etConfirmPassword.text.toString()
        )
        viewModel.register(apiCall)
    }

}
