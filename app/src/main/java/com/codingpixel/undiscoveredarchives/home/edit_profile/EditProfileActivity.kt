package com.codingpixel.undiscoveredarchives.home.edit_profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModelProviders
import com.android.sitbak.utils.maskeditor.MaskTextWatcher
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.*
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.base.BaseActivityResult
import com.codingpixel.undiscoveredarchives.databinding.ActivityEditProfileBinding
import com.codingpixel.undiscoveredarchives.home.searchable_spinner.SearchableSpinnerActivity
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.*
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.google.gson.Gson
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Flash
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File
import kotlin.jvm.Throws

class EditProfileActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private lateinit var profileImagePath: Uri
    private lateinit var mContext: Context

    private var selectedCountryId = -1
    private var selectedStateId = -1
    private var selectedCityId = -1
    private var isPasswordEntered = 0
    private var isPixShowing = false
    private var isOldPasswordEntered = false
    private var isNewPasswordEntered = false
    private var isConfirmNewPasswordEntered = false

    private var countriesList: MutableList<CountriesData> = ArrayList()
    private var statesList: MutableList<StatesData> = ArrayList()
    private var citiesList: MutableList<CitiesData> = ArrayList()
    private var searchableList: ArrayList<SearchableSpinnerModel> = ArrayList()

    private lateinit var userDataModel: UserDetailData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this@EditProfileActivity).create(Api::class.java)
        mContext = this

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        observeApiResponse()


        getProfile()
        setEditTexts()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.tvScreenTitle.text = "Edit Profile"
        binding.ivBack.viewVisible()

        val mobileNumberWatcher = MaskTextWatcher(binding.etPhoneNumber, "###-###-####")
        binding.etPhoneNumber.addTextChangedListener(mobileNumberWatcher)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.countriesSpinner.setOnClickListener(this)
        binding.statesSpinner.setOnClickListener(this)
        binding.citiesSpinner.setOnClickListener(this)
        binding.ivOldPasswordToggle.setOnClickListener(this)
        binding.ivNewPasswordToggle.setOnClickListener(this)
        binding.ivConfirmNewPasswordToggle.setOnClickListener(this)
        binding.btnUpdateProfile.setOnClickListener(this)

        Boom(binding.btnUpdateProfile)
        Boom(binding.countriesSpinner)
        Boom(binding.statesSpinner)
        Boom(binding.citiesSpinner)

        val options = Options().apply {
            ratio = Ratio.RATIO_AUTO                        //Image/video capture ratio
            count = 1                                       //Number of images to restrict selection count
            spanCount = 4                                   //Number for columns in grid
            path = "Pix/Camera"                             //Custom Path For media Storage
            isFrontFacing = false                           //Front Facing camera on start
            mode = Mode.Picture                             //Option to select only pictures or videos or both
            flash = Flash.Auto                              //Option to select flash type
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivProfileImage.setOnClickListener {
            isPixShowing = true
            binding.container.viewVisible()
            addPixToActivity(R.id.container, options) {
                when (it.status) {
                    PixEventCallback.Status.SUCCESS -> {
                        onBackPressed()
                        profileImagePath = it.data[0]

                        Glide.with(this@EditProfileActivity)
                            .load(profileImagePath)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(binding.ivProfileImage)

                    }
                    PixEventCallback.Status.BACK_PRESSED -> {
                        Toast.makeText(this@EditProfileActivity, "Back", Toast.LENGTH_SHORT).show()
                        supportFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isPixShowing) {
            isPixShowing = false
            binding.container.viewGone()
        } else
            super.onBackPressed()
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    if (it.tag != ApiTags.GET_CITIES && it.tag != ApiTags.GET_STATES
                        && it.tag != ApiTags.GET_PROFILE
                    )
                        Loader.showLoader(this) {
                            if (this@EditProfileActivity::apiCall.isInitialized)
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
                            val model = Gson().fromJson(it.data.toString(), CountriesModel::class.java)
                            countriesList.clear()
                            countriesList.addAll(model.data)

                            getStates()
                        }
                        ApiTags.GET_PROFILE -> {
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            userDataModel = model.data

                            if (userDataModel.location != null) {
                                selectedCountryId = userDataModel.location?.country_id ?: 0
                                selectedStateId = userDataModel.location?.state_id ?: 0
                                selectedCityId = userDataModel.location?.city_id ?: 0
                            } else {
                                selectedCountryId = 0
                                selectedStateId = 0
                                selectedCityId = 0
                            }

                            if (userDataModel.is_vendor == 1)
                                getCountries()
                            else
                                setProfileData()
                        }
                        ApiTags.GET_STATES -> {
                            val model = Gson().fromJson(it.data.toString(), StatesModel::class.java)
                            statesList.clear()
                            statesList.addAll(model.data)

                            getCities()
                        }
                        ApiTags.GET_CITIES -> {
                            Loader.hideLoader()
                            val model = Gson().fromJson(it.data.toString(), CitiesModel::class.java)
                            citiesList.clear()
                            citiesList.addAll(model.data)

                            setProfileData()
                        }
                        ApiTags.SAVE_PROFILE -> {
                            Loader.hideLoader()
                            val model = Gson().fromJson(it.data.toString(), LoginModel::class.java)
                            AppUtils.showToast(this@EditProfileActivity, model.message, true)
                            AppUtils.saveUserModel(mContext, model.data)
                        }
                    }
                }
            }
        })
    }

    @Throws(Exception::class)
    private fun setProfileData() {
        Glide.with(mContext)
            .load(userDataModel.photo_path)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(binding.ivProfileImage)

        binding.etName.setText(userDataModel.name)
        binding.etUsername.setText(userDataModel.name)
        binding.etEmail.setText(userDataModel.email)

        if (userDataModel.is_vendor == 1) {

            binding.etCompanyName.setText(userDataModel.company_name)
            binding.countryCodePicker.setCountryForPhoneCode(userDataModel.country_code.toInt())
            binding.etPhoneNumber.setText(userDataModel.mobile_number)
            if (userDataModel.location != null) {
                if (selectedCountryId != 0)
                    binding.countriesSpinner.text =
                        (countriesList.filter { it.id == userDataModel.location?.country_id })[0].name
                if (selectedStateId != 0)
                    binding.statesSpinner.text =
                        (statesList.filter { it.id == userDataModel.location?.state_id })[0].name
                if (selectedCityId != 0)
                    binding.citiesSpinner.text =
                        (citiesList.filter { it.id == userDataModel.location?.city_id })[0].name
                binding.etZipCode.setText(userDataModel.location?.zip_code)
                binding.etStreetAddress.setText(userDataModel.location?.street_address)
            }

            binding.llVendorDetails.viewVisible()

        } else {
            binding.llVendorDetails.viewGone()
        }

    }


    override fun onClick(v: View?) {
        when (v?.id) {
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

                            selectedStateId = 0
                            binding.statesSpinner.text = ""
                            selectedCityId = 0
                            binding.citiesSpinner.text = ""

                            if (selectedStateId == 0)
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

                                    selectedCityId = 0
                                    binding.citiesSpinner.text = ""

                                    if (selectedCityId == 0)
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
            R.id.ivOldPasswordToggle -> {
                AppUtils.hideShowPassword(binding.etOldPassword, binding.ivOldPasswordToggle)
            }
            R.id.ivNewPasswordToggle -> {
                AppUtils.hideShowPassword(
                    binding.etNewPassword,
                    binding.ivNewPasswordToggle
                )
            }
            R.id.ivConfirmNewPasswordToggle -> {
                AppUtils.hideShowPassword(
                    binding.etConfirmNewPassword,
                    binding.ivConfirmNewPasswordToggle
                )
            }
            R.id.btnUpdateProfile -> {
                if (validate())
                    saveProfile()
            }
        }
    }

    private fun getProfile() {
        apiCall = retrofitClient.getProfile()
        viewModel.getProfile(apiCall)
    }

    private fun saveProfile() {

        val profileMultipartBody: MultipartBody.Part? = if (::profileImagePath.isInitialized) {
            val path = PhotoUtil.getPath(mContext, profileImagePath)
            if (path.isNullOrBlank())
                return
            val file = File(path)
            val requestBody: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("photo", file.name, requestBody)
        } else
            null

        apiCall = if (binding.etNewPassword.text.isNotBlank())
            retrofitClient.saveProfile(
                userDataModel.is_vendor.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etUsername.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etEmail.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etCompanyName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.countryCodePicker.selectedCountryCodeWithPlus.toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etPhoneNumber.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                selectedCountryId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                selectedStateId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                selectedCityId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etZipCode.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etZipCode.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etOldPassword.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etNewPassword.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etConfirmNewPassword.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                profileMultipartBody
            )
        else
            retrofitClient.saveProfileWithoutPassword(
                userDataModel.is_vendor.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etUsername.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etEmail.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etCompanyName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.countryCodePicker.selectedCountryCodeWithPlus.toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etPhoneNumber.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                selectedCountryId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                selectedStateId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                selectedCityId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etZipCode.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etStreetAddress.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                profileMultipartBody
            )

        viewModel.saveProfile(apiCall)
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

    private fun setEditTexts() {
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etName.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilFullName.error = null
                binding.tilFullName.isErrorEnabled = false
            }
        })
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etUsername.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilUsername.error = null
                binding.tilUsername.isErrorEnabled = false
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
        binding.etCompanyName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etCompanyName.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilCompanyName.error = null
                binding.tilCompanyName.isErrorEnabled = false
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
                binding.tilStreetAddress.error = null
                binding.tilStreetAddress.isErrorEnabled = false
            }
        })
        binding.etOldPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                isOldPasswordEntered = !s.isNullOrBlank()

                binding.etOldPassword.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilOldPassword.error = null
                binding.tilOldPassword.viewGone()
            }
        })
        binding.etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                isNewPasswordEntered = !s.isNullOrBlank()

                binding.etNewPassword.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilNewPassword.error = null
                binding.tilNewPassword.viewGone()
            }
        })
        binding.etConfirmNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                isConfirmNewPasswordEntered = !s.isNullOrBlank()

                binding.etConfirmNewPassword.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilConfirmNewPassword.error = null
                binding.tilConfirmNewPassword.viewGone()
            }
        })
    }

    private fun validate(): Boolean {
        if (binding.etName.text.toString().isBlank()) {
            binding.etName.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilFullName.error = "Please enter your Full Name!"
            binding.etName.requestFocus()
            return false
        }

        if (binding.etUsername.text.toString().isBlank()) {
            binding.etUsername.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilUsername.error = "Please enter Username!"
            binding.etUsername.requestFocus()
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

        if (userDataModel.is_vendor == 1) {

            if (binding.etCompanyName.text.toString().isBlank()) {
                binding.etCompanyName.setBackgroundResource(R.drawable.auth_edit_text_error)
                binding.tilCompanyName.error = "Please enter Company Name!"
                binding.tilCompanyName.requestFocus()
                return false
            }

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
                binding.tilStreetAddress.viewVisible()
                binding.tilStreetAddress.error = "Enter Street Address!"
                binding.etStreetAddress.requestFocus()
                return false
            }
        }

        if (isPasswordEntered == 1) {
            if (isOldPasswordEntered || isNewPasswordEntered || isConfirmNewPasswordEntered) {
                when {
                    binding.etOldPassword.text.toString().isBlank() -> {
                        binding.etOldPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
                        binding.tilOldPassword.viewVisible()
                        binding.tilOldPassword.error = "Enter Old Password!"
                        binding.etOldPassword.requestFocus()
                        return false
                    }
                    binding.etNewPassword.text.toString().isBlank() -> {
                        binding.etNewPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
                        binding.tilNewPassword.viewVisible()
                        binding.tilNewPassword.error = "Enter New Password!"
                        binding.etNewPassword.requestFocus()
                        return false
                    }
                    binding.etNewPassword.text.toString().length < 8 -> {
                        binding.etNewPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
                        binding.tilNewPassword.viewVisible()
                        binding.tilNewPassword.error = "Password must be at least 8 characters long!"
                        binding.etNewPassword.requestFocus()
                        return false
                    }
                    binding.etConfirmNewPassword.text.toString().isBlank() -> {
                        binding.etConfirmNewPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
                        binding.tilConfirmNewPassword.viewVisible()
                        binding.tilConfirmNewPassword.error = "Enter Confirm Password!"
                        binding.etConfirmNewPassword.requestFocus()
                        return false
                    }
                    binding.etConfirmNewPassword.text.toString() != binding.etNewPassword.text.toString() -> {
                        binding.etConfirmNewPassword.setBackgroundResource(R.drawable.auth_edit_text_error)
                        binding.tilConfirmNewPassword.viewVisible()
                        binding.tilConfirmNewPassword.error = "Confirm Password doesn't match!"
                        binding.etConfirmNewPassword.requestFocus()
                        return false
                    }
                }
            }
        }
        return true
    }
}