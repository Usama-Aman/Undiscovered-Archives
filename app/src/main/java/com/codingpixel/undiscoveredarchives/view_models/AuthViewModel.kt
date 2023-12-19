package com.codingpixel.undiscoveredarchives.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codingpixel.undiscoveredarchives.network.ApiTags
import com.codingpixel.undiscoveredarchives.network.Resource
import com.codingpixel.undiscoveredarchives.network.ResponseCallBack
import com.codingpixel.undiscoveredarchives.network.RetrofitClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

class AuthViewModel : ViewModel(), ResponseCallBack {

    private val apiResponse = MutableLiveData<Resource<JSONObject>>()

    /*Meta Data*/
    fun getCountries(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_COUNTRIES)
        RetrofitClient.apiCall(call, this, ApiTags.GET_COUNTRIES)
    }

    fun getStates(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_STATES)
        RetrofitClient.apiCall(call, this, ApiTags.GET_STATES)
    }

    fun getCities(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.GET_CITIES)
        RetrofitClient.apiCall(call, this, ApiTags.GET_CITIES)
    }


    /*Auth*/
    fun socialLogin(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.SOCIAL_LOGIN)
        RetrofitClient.apiCall(call, this, ApiTags.SOCIAL_LOGIN)
    }

    fun register(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.REGISTER)
        RetrofitClient.apiCall(call, this, ApiTags.REGISTER)
    }

    fun login(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.LOGIN)
        RetrofitClient.apiCall(call, this, ApiTags.LOGIN)
    }

    fun registerDevice(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.REGISTER_DEVICE)
        RetrofitClient.apiCall(call, this, ApiTags.REGISTER_DEVICE)
    }

    fun forgotPassword(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.FORGOT_PASSWORD)
        RetrofitClient.apiCall(call, this, ApiTags.FORGOT_PASSWORD)
    }

    fun resendOTP(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.RESEND_OTP)
        RetrofitClient.apiCall(call, this, ApiTags.RESEND_OTP)
    }

    fun verifyOTP(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.VERIFY_OTP)
        RetrofitClient.apiCall(call, this, ApiTags.VERIFY_OTP)
    }

    fun resetPassword(call: Call<ResponseBody>) {
        apiResponse.value = Resource.loading(ApiTags.RESET_PASSWORD)
        RetrofitClient.apiCall(call, this, ApiTags.RESET_PASSWORD)
    }


    override fun onSuccess(jsonObject: JSONObject, tag: String) {
        when (tag) {
            ApiTags.GET_COUNTRIES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_COUNTRIES))
            }
            ApiTags.GET_STATES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_STATES))
            }
            ApiTags.GET_CITIES -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.GET_CITIES))
            }
            ApiTags.SOCIAL_LOGIN -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.SOCIAL_LOGIN))
            }
            ApiTags.REGISTER -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.REGISTER))
            }
            ApiTags.LOGIN -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.LOGIN))
            }
            ApiTags.FORGOT_PASSWORD -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.FORGOT_PASSWORD))
            }
            ApiTags.RESEND_OTP -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.RESEND_OTP))
            }
            ApiTags.VERIFY_OTP -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.VERIFY_OTP))
            }
            ApiTags.RESET_PASSWORD -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.RESET_PASSWORD))
            }
            ApiTags.REGISTER_DEVICE -> {
                apiResponse.postValue(Resource.success(jsonObject, ApiTags.REGISTER_DEVICE))
            }
        }
    }

    override fun onError(error: String, tag: String) {
        when (tag) {
            ApiTags.GET_COUNTRIES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_COUNTRIES))
            }
            ApiTags.GET_STATES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_STATES))
            }
            ApiTags.GET_CITIES -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.GET_CITIES))
            }
            ApiTags.SOCIAL_LOGIN -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.SOCIAL_LOGIN))
            }
            ApiTags.REGISTER -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.REGISTER))
            }
            ApiTags.LOGIN -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.LOGIN))
            }
            ApiTags.FORGOT_PASSWORD -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.FORGOT_PASSWORD))
            }
            ApiTags.RESEND_OTP -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.RESEND_OTP))
            }
            ApiTags.VERIFY_OTP -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.VERIFY_OTP))
            }
            ApiTags.RESET_PASSWORD -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.RESET_PASSWORD))
            }
            ApiTags.REGISTER_DEVICE -> {
                apiResponse.postValue(Resource.error(error, null, ApiTags.REGISTER_DEVICE))
            }
        }
    }

    fun getApiResponse(): LiveData<Resource<JSONObject>> = apiResponse
}