package com.codingpixel.undiscoveredarchives.home.add_new_listing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.astritveliu.boom.Boom
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.auth.models.SearchableSpinnerModel
import com.codingpixel.undiscoveredarchives.base.AppController.Companion.addEditUserProductData
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.base.BaseActivityResult
import com.codingpixel.undiscoveredarchives.databinding.ActivityAddListing2Binding
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity.Companion.fromEdit
import com.codingpixel.undiscoveredarchives.home.add_new_listing.AddListingActivity.Companion.productGradients
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

class AddListingActivity2 : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddListing2Binding
    private lateinit var mContext: Context
    private lateinit var viewModel: HomeViewModel
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private var searchableList: ArrayList<SearchableSpinnerModel> = ArrayList()

    private var selectedConditionId = ""
    private var selectedCurrencyId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddListing2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        retrofitClient = RetrofitClient.getClient(this@AddListingActivity2).create(Api::class.java)

        blackStatusBarIcons()

        initViews()
        initVM()
        initListeners()
        setEditTexts()
        observeApiResponse()
        getProductsGradients()
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun initViews() {
        if (fromEdit)
            setUpProductData()

        binding.topBar.ivBack.viewVisible()
        binding.topBar.tvScreenTitle.text = "Add new listing"

        binding.etDescription.setOnTouchListener(View.OnTouchListener { v, event ->
            if (binding.etDescription.hasFocus()) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_SCROLL -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        return@OnTouchListener true
                    }
                }
            }
            false
        })
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }


    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.conditionSpinner.setOnClickListener(this)
        binding.currencySpinner.setOnClickListener(this)
        Boom(binding.btnNext)
        Boom(binding.conditionSpinner)
        Boom(binding.currencySpinner)

    }

    private fun setUpProductData() {
        //Condition
        selectedConditionId = productGradients.conditions.filter { it == addEditUserProductData?.condition }[0]
        binding.conditionSpinner.text = selectedConditionId

        //Currency
        binding.currencySpinner.text =
            productGradients.currencies.filter { it.id == addEditUserProductData?.currency?.id ?: -1 }[0].name
        selectedCurrencyId = productGradients.currencies.filter { it.id == addEditUserProductData?.currency?.id ?: -1 }[0].id

        //Product description
        binding.etDescription.setText(addEditUserProductData?.product_description ?: "")
        //Measurement
        binding.etMeasurements.setText(addEditUserProductData?.measurements ?: "")
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.btnNext -> {
                addEditUserProductData?.condition = selectedConditionId
                addEditUserProductData?.currency?.id = selectedCurrencyId
                addEditUserProductData?.product_description = binding.etDescription.text.toString()
                addEditUserProductData?.measurements = binding.etMeasurements.text.toString()
                if (validate())
                    startActivity(Intent(this, AddListingActivity3::class.java))
            }
            R.id.conditionSpinner -> {
                searchableList.clear()
                productGradients.conditions.forEachIndexed { index, conditionData ->
                    searchableList.add(
                        SearchableSpinnerModel(
                            index, conditionData, conditionData == selectedConditionId
                        )
                    )
                }

                val intent = Intent(this, SearchableSpinnerActivity::class.java)
                intent.putParcelableArrayListExtra("listData", searchableList)
                intent.putExtra("title", "Select Category")
                activityLauncher.launch(intent, object : BaseActivityResult.OnActivityResult<ActivityResult> {
                    override fun onActivityResult(result: ActivityResult) {
                        if (result.resultCode == Activity.RESULT_OK) {
                            selectedConditionId = result.data?.getStringExtra("selectedValue") ?: ""
                            binding.conditionSpinner.text =
                                result.data?.getStringExtra("selectedValue")
                        }
                    }
                })
            }
            R.id.currencySpinner -> {
                searchableList.clear()
                productGradients.currencies.forEachIndexed { _, currencyData ->
                    searchableList.add(
                        SearchableSpinnerModel(
                            currencyData.id, currencyData.name, currencyData.id == selectedCurrencyId
                        )
                    )
                }

                val intent = Intent(this, SearchableSpinnerActivity::class.java)
                intent.putParcelableArrayListExtra("listData", searchableList)
                intent.putExtra("title", "Select Category")
                activityLauncher.launch(intent, object : BaseActivityResult.OnActivityResult<ActivityResult> {
                    override fun onActivityResult(result: ActivityResult) {
                        if (result.resultCode == Activity.RESULT_OK) {
                            selectedCurrencyId = result.data?.getIntExtra("selectedId", -1) ?: -1
                            binding.currencySpinner.text =
                                result.data?.getStringExtra("selectedValue")
                        }
                    }
                })
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun observeApiResponse() {
        viewModel.getApiResponse().observe(this, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader((this as AppCompatActivity)) {
                        if (this::apiCall.isInitialized)
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
                        ApiTags.GET_PRODUCT_GRADIENTS -> {
                            val model = Gson().fromJson(it.data.toString(), ProductsGradientsModel::class.java)
                            productGradients = model.data
                        }
                    }
                }
            }
        })
    }


    private fun getProductsGradients() {
        apiCall = retrofitClient.getProductGradients()
        viewModel.getProductGradients(apiCall)
    }

    private fun setEditTexts() {
        binding.conditionSpinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.conditionSpinner.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilConditionSpinner.viewGone()
                binding.tilConditionSpinner.isErrorEnabled = false
            }
        })
        binding.currencySpinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.currencySpinner.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilCurrencySpinner.viewGone()
                binding.tilCurrencySpinner.isErrorEnabled = false
            }
        })
        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etDescription.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilDescription.viewGone()
                binding.tilDescription.isErrorEnabled = false
            }
        })
        binding.etMeasurements.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etMeasurements.setBackgroundResource(R.drawable.auth_edittext_drawable)
                binding.tilMeasurements.error = null
                binding.tilMeasurements.isErrorEnabled = false
            }
        })
    }


    private fun validate(): Boolean {
        if (binding.conditionSpinner.text.toString().isBlank()) {
            binding.conditionSpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilConditionSpinner.viewVisible()
            binding.tilConditionSpinner.error = "Select Condition!"
            binding.conditionSpinner.requestFocus()
            return false
        }
        if (binding.currencySpinner.text.toString().isBlank()) {
            binding.currencySpinner.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilCurrencySpinner.viewVisible()
            binding.tilCurrencySpinner.error = "Select Currency!"
            binding.currencySpinner.requestFocus()
            return false
        }
        if (binding.etDescription.text.toString().isBlank()) {
            binding.etDescription.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilDescription.viewVisible()
            binding.tilDescription.error = "Enter Description!"
            binding.etDescription.requestFocus()
            return false
        }
        if (binding.etMeasurements.text.toString().isBlank()) {
            binding.etMeasurements.setBackgroundResource(R.drawable.auth_edit_text_error)
            binding.tilMeasurements.viewVisible()
            binding.tilMeasurements.error = "Enter Measurements!"
            binding.etMeasurements.requestFocus()
            return false
        }
        return true
    }

}