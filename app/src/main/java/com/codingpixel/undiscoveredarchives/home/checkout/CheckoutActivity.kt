package com.codingpixel.undiscoveredarchives.home.checkout

//import com.braintreepayments.api.dropin.DropInActivity
//import com.braintreepayments.api.dropin.DropInRequest
//import com.braintreepayments.api.dropin.DropInResult
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModelProviders
import com.braintreepayments.api.BraintreeClient
import com.braintreepayments.api.PayPalClient
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.*
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.base.BaseActivityResult
import com.codingpixel.undiscoveredarchives.databinding.ActivityCheckoutBinding
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
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call


class CheckoutActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var countriesList: MutableList<CountriesData> = ArrayList()
    private var statesList: MutableList<StatesData> = ArrayList()
    private var citiesList: MutableList<CitiesData> = ArrayList()
    private var searchableList: ArrayList<SearchableSpinnerModel> = ArrayList()

    private var selectedCountryId = -1
    private var selectedStateId = -1
    private var selectedCityId = -1

    private lateinit var braintreeClient: BraintreeClient
    private lateinit var paypalClient: PayPalClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofitClient = RetrofitClient.getClient(this).create(Api::class.java)
        blackStatusBarIcons()

        braintreeClient = BraintreeClient(this, resources.getString(R.string.braintree_tokenization_key))
        paypalClient = PayPalClient(braintreeClient)


        initViews()
        initVM()
        initListeners()
        observeApiResponse()

        getCountries()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Checkout"
        binding.topBar.ivBack.viewVisible()
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initListeners() {
        binding.btnBuyWithPayPal.setOnClickListener(this)
        binding.topBar.ivBack.setOnClickListener(this)
        binding.citiesSpinner.setOnClickListener(this)
        binding.countriesSpinner.setOnClickListener(this)
        binding.statesSpinner.setOnClickListener(this)

        setEditText(binding.etFirstName, binding.tilFirstName)
        setEditText(binding.etLastName, binding.tilLastName)
        setEditText(binding.etZipCode, binding.tilZipCode)
        setEditText(binding.etStreetAddress, binding.tilStreetAddress)

        setSpinnerText(binding.citiesSpinner, binding.tilSelectCity)
        setSpinnerText(binding.countriesSpinner, binding.tilSelectCountry)
        setSpinnerText(binding.statesSpinner, binding.tilSelectState)


    }

    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(this) {
                        if (this@CheckoutActivity::apiCall.isInitialized)
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
            R.id.ivBack -> finish()
            R.id.btnBuyWithPayPal -> {
                checkout()
            }

        }
    }

    private fun checkout() {
        val result = braintreeClient.deliverBrowserSwitchResult(this)
        result?.let {
            paypalClient.onBrowserSwitchResult(it) { payPalAccountNonce, error ->
                // send paypalAccountNonce.string to server
                Log.d("","")
            }
        }
    }

//    private fun checkout() {
//        val dropInRequest = DropInRequest()
//            .tokenizationKey(resources.getString(R.string.braintree_tokenization_key))
//            .collectDeviceData(true)
//        activityLauncher.launch(dropInRequest.getIntent(this), object : BaseActivityResult.OnActivityResult<ActivityResult> {
//            override fun onActivityResult(result: ActivityResult) {
//                when (result.resultCode) {
//                    Activity.RESULT_OK -> {
//                        val dropInResult: DropInResult? = result.data?.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT)
//                        if (dropInResult != null) {
//                            //TODO
//                        }
//                    }
//                    Activity.RESULT_CANCELED -> {
//                        // the user canceled
//                    }
//                    else -> {
//                        // handle errors here, an exception may be available in
//                        val error = result.data?.getSerializableExtra(DropInActivity.EXTRA_ERROR) as Exception
//                        AppUtils.showToast(this@CheckoutActivity, error.localizedMessage ?: "", false)
//                    }
//                }
//            }
//        })
//    }

    private fun validate(): Boolean {
        if (binding.etFirstName.text.toString().trim().isBlank()) {
            binding.etFirstName.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilFirstName.error = "Please enter your First Name!"
            binding.tilFirstName.requestFocus()
            return false
        }

        if (binding.etLastName.text.toString().trim().isBlank()) {
            binding.etLastName.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilLastName.error = "Please enter your Last Name"
            binding.etLastName.requestFocus()
            return false
        }

        if (binding.etStreetAddress.text.toString().trim().isBlank()) {
            binding.etStreetAddress.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilStreetAddress.error = "Please enter Your Street Address"
            binding.etStreetAddress.requestFocus()
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
        if (binding.etZipCode.text.toString().trim().length < 5) {
            binding.etZipCode.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilZipCode.error = "Please enter 5 Digits!"
            binding.etZipCode.requestFocus()
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

    private fun setEditText(editText: EditText, layout: TextInputLayout) {
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

    private fun setSpinnerText(textView: TextView, layout: TextInputLayout) {
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