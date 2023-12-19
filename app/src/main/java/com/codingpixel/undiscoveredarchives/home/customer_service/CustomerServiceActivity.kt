package com.codingpixel.undiscoveredarchives.home.customer_service

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModelProviders
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.*
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.base.BaseActivityResult
import com.codingpixel.undiscoveredarchives.databinding.ActivityCustomerServiceBinding
import com.codingpixel.undiscoveredarchives.home.searchable_spinner.SearchableSpinnerActivity
import com.codingpixel.undiscoveredarchives.loader.Loader
import com.codingpixel.undiscoveredarchives.network.Api
import com.codingpixel.undiscoveredarchives.network.ApiStatus
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import com.codingpixel.undiscoveredarchives.utils.*
import com.codingpixel.undiscoveredarchives.view_models.HomeViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call

class CustomerServiceActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCustomerServiceBinding
    private var selectedCountryId = -1
    private var selectedStateId = -1
    private var selectedCityId = -1

    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var countriesList: MutableList<CountriesData> = ArrayList()
    private var statesList: MutableList<StatesData> = ArrayList()
    private var citiesList: MutableList<CitiesData> = ArrayList()
    private var searchableList: ArrayList<SearchableSpinnerModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        observeApiResponse()

        getCountries()


    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Customer Service"
        binding.topBar.ivBack.viewVisible()
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.btnSend.setOnClickListener {
            if (validate()) {

            }
        }
        binding.topBar.ivBack.setOnClickListener {
            finish()
        }

        binding.citiesSpinner.setOnClickListener(this)
        binding.countriesSpinner.setOnClickListener(this)
        binding.statesSpinner.setOnClickListener(this)

        setEditText(binding.etFullName, binding.tilFullName)
        setEditText(binding.etEmail, binding.tilEmail)
        setEditText(binding.etZipCode, binding.tilZipCode)
        setEditText(binding.etEnterMessage, binding.tilEnterMessage)

        setSpinnerText(binding.citiesSpinner, binding.tilCitiesSpinner)
        setSpinnerText(binding.countriesSpinner, binding.tilCountriesSpinner)
        setSpinnerText(binding.statesSpinner, binding.tilStatesSpinner)
    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this) {
                        if (this@CustomerServiceActivity::apiCall.isInitialized)
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


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.countriesSpinner -> {
                searchableList.clear()
                countriesList.forEachIndexed { _, countriesData ->
                    searchableList.add(
                        SearchableSpinnerModel(
                            countriesData.id,
                            countriesData.name,
                            countriesData.id == selectedCountryId
                        )
                    )
                }

                val intent = Intent(this, SearchableSpinnerActivity::class.java)
                intent.putParcelableArrayListExtra("listData", searchableList)
                intent.putExtra("title", "Select Country")
                activityLauncher.launch(
                    intent,
                    object : BaseActivityResult.OnActivityResult<ActivityResult> {
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
                if (!binding.countriesSpinner.text.toString().isBlank()) {
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
                    binding.tilCountriesSpinner.viewVisible()
                    binding.tilCountriesSpinner.error = "Select Country first!"
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
                if (!binding.statesSpinner.text.toString().isBlank()) {
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
                    binding.tilStatesSpinner.viewVisible()
                    binding.tilStatesSpinner.error = "Select State first!"
                }
            }

        }
    }

    private fun validate(): Boolean {
        if (binding.etFullName.text.toString().trim().isBlank()) {
            binding.etFullName.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilFullName.error = "Please enter your Full Name!"
            binding.etFullName.requestFocus()
            return false
        }

        if (binding.etEmail.text.toString().trim().isBlank()) {
            binding.etEmail.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilEmail.error = "Please enter your Email!"
            binding.etEmail.requestFocus()
            return false
        }

        if (binding.countriesSpinner.text.toString().isBlank()) {
            binding.countriesSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilCountriesSpinner.viewVisible()
            binding.tilCountriesSpinner.error = "Select Country!"
            binding.countriesSpinner.requestFocus()
            return false
        }
        if (binding.statesSpinner.text.toString().isBlank()) {
            binding.statesSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilStatesSpinner.viewVisible()
            binding.tilStatesSpinner.error = "Select State!"
            binding.statesSpinner.requestFocus()
            return false
        }
        if (binding.citiesSpinner.text.toString().isBlank()) {
            binding.citiesSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilCitiesSpinner.viewVisible()
            binding.tilCitiesSpinner.error = "Select City!"
            binding.citiesSpinner.requestFocus()
            return false
        }
        if (binding.etZipCode.text.toString().trim().isBlank()) {
            binding.etZipCode.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilZipCode.error = "Please enter Zip Code!"
            binding.etZipCode.requestFocus()
            return false
        }
        if (binding.etZipCode.text.toString().trim().contains("-")) {
            binding.etZipCode.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilZipCode.error = "Please enter Number Only!"
            binding.etZipCode.requestFocus()
            return false
        }
        if (binding.etEnterMessage.text.toString().trim().isBlank()) {
            binding.etEnterMessage.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilEnterMessage.error = "Please enter your Message!"
            binding.etEnterMessage.requestFocus()
            return false
        }
        return true

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
    
    private fun setEditText(editText: EditText, layout: TextInputLayout){
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                editText.setBackgroundResource(R.drawable.auth_edittext_drawable)
                layout.error = null
                layout.isErrorEnabled = false
            }
        })
    }
    private fun setSpinnerText(textView: TextView, layout: TextInputLayout){
        textView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                textView.setBackgroundResource(R.drawable.auth_edittext_drawable)
                layout.error = null
                layout.viewGone()

            }
        })
    }

}