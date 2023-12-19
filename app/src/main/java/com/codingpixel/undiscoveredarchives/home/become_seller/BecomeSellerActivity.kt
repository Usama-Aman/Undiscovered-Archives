package com.codingpixel.undiscoveredarchives.home.become_seller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.*
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.base.BaseActivityResult
import com.codingpixel.undiscoveredarchives.databinding.ActivityCompleteSellerProfileBinding
import com.codingpixel.undiscoveredarchives.home.dialogs.AccountPending
import com.codingpixel.undiscoveredarchives.home.main.HomeActivity
import com.codingpixel.undiscoveredarchives.home.searchable_spinner.SearchableSpinnerActivity
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.AppUtils
import com.codingpixel.undiscoveredarchives.utils.viewGone
import com.codingpixel.undiscoveredarchives.utils.viewVisible
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.codingpixel.undiscoveredarchives.view_models.ViewModelFactory
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class BecomeSellerActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCompleteSellerProfileBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var selectedCountryId = -1
    private var selectedStateId = -1
    private var selectedCityId = -1

    private var countriesList: MutableList<CountriesData> = ArrayList()
    private var statesList: MutableList<StatesData> = ArrayList()
    private var citiesList: MutableList<CitiesData> = ArrayList()
    private var searchableList: ArrayList<SearchableSpinnerModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteSellerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this@BecomeSellerActivity).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        observeApiResponse()

        setEditTexts()

        getCountries()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Complete seller profile"
        binding.topBar.ivBack.viewVisible()
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        Boom(binding.btnBecomeSeller)
        Boom(binding.countriesSpinner)
        Boom(binding.citiesSpinner)
        Boom(binding.statesSpinner)

        binding.topBar.ivBack.setOnClickListener(this)
        binding.btnBecomeSeller.setOnClickListener(this)
        binding.countriesSpinner.setOnClickListener(this)
        binding.citiesSpinner.setOnClickListener(this)
        binding.statesSpinner.setOnClickListener(this)
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
            R.id.ivBack -> {
                finish()
            }
            R.id.btnBecomeSeller -> {
                if (validate()) {
                    becomeSeller()
                }
            }
        }
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this@BecomeSellerActivity, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this@BecomeSellerActivity) {
                        if (this::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(this@BecomeSellerActivity, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.BECOME_SELLER -> {
                            AccountPending(object : AccountPending.AccountPendingInterface {
                                override fun onDone() {
                                    val i = Intent(this@BecomeSellerActivity, HomeActivity::class.java)
                                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    i.putExtra("goToProfile", true)
                                    startActivity(i)
                                    finish()
                                }
                            }).show(supportFragmentManager, "AccountPendingDialog")
                        }
                        ApiTags.GET_COUNTRIES -> {
                            val model =
                                Gson().fromJson(it.data.toString(), CountriesModel::class.java)
                            countriesList.clear()
                            countriesList.addAll(model.data)
                        }
                        ApiTags.GET_STATES -> {
                            val model = Gson().fromJson(it.data.toString(), StatesModel::class.java)
                            statesList.clear()
                            statesList.addAll(model.data)
                        }
                        ApiTags.GET_CITIES -> {
                            val model = Gson().fromJson(it.data.toString(), CitiesModel::class.java)
                            citiesList.clear()
                            citiesList.addAll(model.data)
                        }
                    }
                }
            }
        })
    }

    private fun becomeSeller() {
        apiCall = retrofitClient.becomeSeller(
            binding.etCompanyName.text.toString(),
            binding.countryCodePicker.selectedCountryCodeWithPlus.toString(),
            binding.etPhoneNumber.text.toString(),
            selectedCountryId, selectedStateId, selectedCityId,
            binding.etZipCode.text.toString(),
            binding.etStreetAddress.text.toString(),
        )
        viewModel.becomeSeller(apiCall)
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
                binding.tilStreet.error = null
                binding.tilStreet.isErrorEnabled = false
            }
        })
    }

    private fun validate(): Boolean {
        if (binding.etCompanyName.text.toString().isBlank()) {
            binding.etCompanyName.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilCompanyName.error = "Please enter Company Name!"
            binding.etCompanyName.requestFocus()
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
            binding.tilStreet.viewVisible()
            binding.tilStreet.error = "Enter Street Address!"
            binding.etStreetAddress.requestFocus()
            return false
        }
        return true
    }
}