package com.codingpixel.undiscoveredarchives.network

import com.codingpixel.undiscoveredarchives.base.AppController
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface Api {

    @GET("address_data")
    fun getCountries(
        @Query("type") type: String,
    ): Call<ResponseBody>

    @GET("settings/app_percentage")
    fun getCommissionedAmount(): Call<ResponseBody>

    @GET("address_data")
    fun getStates(
        @Query("type") type: String,
        @Query("country_id") country_id: Int,
    ): Call<ResponseBody>

    @GET("address_data")
    fun getCities(
        @Query("type") type: String,
        @Query("country_id") country_id: Int,
        @Query("state_id") state_id: Int,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("social_login")
    fun socialLogin(
        @Field("provider_name") provider_name: String,
        @Field("provider_id") provider_id: String,
        @Field("name") name: String,
        @Field("email") email: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("is_vendor") is_vendor: Int,
        @Field("name") name: String,
        @Field("company_name") company_name: String,
        @Field("email") email: String,
        @Field("country_code") country_code: String,
        @Field("mobile_number") mobile_number: String,
        @Field("country_id") country_id: Int,
        @Field("state_id") state_id: Int,
        @Field("city_id") city_id: Int,
        @Field("zip_code") zip_code: String,
        @Field("street_address") street_address: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/logout")
    fun logout(
        @Field("device_id") device_id: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("reset_password/send_otp")
    fun forgotPassword(
        @Field("type") type: String = "email",
        @Field("email") email: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("resend_otp")
    fun resendOtp(
        @Field("type") type: String = "email",
        @Field("email") email: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("verify_otp")
    fun verifyOTP(
        @Field("type") type: String = "email",
        @Field("email") email: String,
        @Field("otp") otp: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("reset_password")
    fun resetPassword(
        @Field("type") type: String = "email",
        @Field("otp") otp: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password_confirmation") password_confirmation: String,
    ): Call<ResponseBody>

    /*Home Apis*/
    @GET("user/notifications")
    fun getNotifications(): Call<ResponseBody>

    @GET("categories")
    fun getCategories(
        @Query("with_products") with_products: Int = 0,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = AppController.PAGINATION_COUNT,
    ): Call<ResponseBody>

    @GET("designers")
    fun getDesigners(
        @Query("with_products") with_products: Int = 0,
        @Query("name") name: String = ""
    ): Call<ResponseBody>

    @GET("featured_products")
    fun getFeaturedProducts(
        @Query("take") with_products: Int = 10,
    ): Call<ResponseBody>

    @GET("user/products")
    fun getFavoriteProducts(
        @Query("order_by") order_by: String,
        @Query("is_favourite_products") is_favourite_products: Int = 1,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.PAGINATION_COUNT,
    ): Call<ResponseBody>

    @GET("user/designers")
    fun getFavoriteDesigners(
        @Query("with_products") with_products: Int = 1,  //By Default one for all favoriteProducts
        @Query("is_favourite") is_favourite: Int = 1,
    ): Call<ResponseBody>

    @GET("user/favourite_product/{id}")
    fun addProductToFavorite(
        @Path("id") id: Int
    ): Call<ResponseBody>

    @GET("user/favourite_designer/{id}")
    fun addDesignerProductsToFavorite(
        @Path("id") id: Int
    ): Call<ResponseBody>

    @GET("user/profile")
    fun getProfile(): Call<ResponseBody>

    @Multipart
    @POST("user/profile")
    fun saveProfile(
        @Part("is_vendor") is_vendor: RequestBody,
        @Part("name") name: RequestBody,
        @Part("user_name") user_name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("company_name") company_name: RequestBody,
        @Part("country_code") country_code: RequestBody,
        @Part("mobile_number") mobile_number: RequestBody,
        @Part("country_id") country_id: RequestBody,
        @Part("state_id") state_id: RequestBody,
        @Part("city_id") city_id: RequestBody,
        @Part("zip_code") zip_code: RequestBody,
        @Part("street_address") street_address: RequestBody,
        @Part("old_password") old_password: RequestBody,
        @Part("password") password: RequestBody,
        @Part("password_confirmation") password_confirmation: RequestBody,
        @Part photo: MultipartBody.Part?,
    ): Call<ResponseBody>


    @Multipart
    @POST("user/profile")
    fun saveProfileWithoutPassword(
        @Part("is_vendor") is_vendor: RequestBody,
        @Part("name") name: RequestBody,
        @Part("user_name") user_name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("company_name") company_name: RequestBody,
        @Part("country_code") country_code: RequestBody,
        @Part("mobile_number") mobile_number: RequestBody,
        @Part("country_id") country_id: RequestBody,
        @Part("state_id") state_id: RequestBody,
        @Part("city_id") city_id: RequestBody,
        @Part("zip_code") zip_code: RequestBody,
        @Part("street_address") street_address: RequestBody,
        @Part photo: MultipartBody.Part?,
    ): Call<ResponseBody>


    @GET("products")
    fun getAllProducts(
        @Query("order_by") order_by: String,
        @Query("condition") condition: String,
        @Query("category_id") category_id: Int = 0,
        @Query("designer_id") designer_id: Int = 0,
        @Query("color_id") color_id: Int = 0,
        @Query("size_id") size_id: Int = 0,
        @Query("name") name: String = "",
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.PAGINATION_COUNT,
    ): Call<ResponseBody>

    @GET("products/{id}")
    fun getGuestProductDetail(
        @Path("id") id: Int,
    ): Call<ResponseBody>

    @GET("products/{id}")
    fun getUserProductDetail(
        @Path("id") id: Int,
    ): Call<ResponseBody>


    @GET("user/products")
    fun getDraftProducts(
        @Query("order_by") order_by: String,
        @Query("status") status: String = "Draft",
        @Query("is_profile_listing") is_profile_listing: Int = 1,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.PAGINATION_COUNT,
    ): Call<ResponseBody>


    @GET("user/products")
    fun getTrashProducts(
        @Query("order_by") order_by: String,
        @Query("status") status: String = "Trashed",
        @Query("is_profile_listing") is_profile_listing: Int = 1,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.PAGINATION_COUNT,
    ): Call<ResponseBody>

    @GET("user/products")
    fun getPublishedProducts(
        @Query("order_by") order_by: String,
        @Query("status") status: String = "Published",
        @Query("is_profile_listing") is_profile_listing: Int = 1,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.PAGINATION_COUNT,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/delete_products")
    fun deleteProduct(
        @Field("id") id: Int,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/update_status")
    fun updateProductStatus(
        @Field("id") id: Int,
        @Field("status") status: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/restore_products")
    fun restoreTrashedProduct(
        @Field("id") id: Int,
    ): Call<ResponseBody>


    @GET("user/orders")
    fun getPurchaseOrders(
        @Query("tab") tab: String = "purchase",
        @Query("type") type: String,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.PAGINATION_COUNT,
    ): Call<ResponseBody>

    @GET("user/orders")
    fun getSalesOrders(
        @Query("tab") tab: String = "sale",
        @Query("type") type: String,
        @Query("skip") skip: Int,
        @Query("take") take: Int = AppController.PAGINATION_COUNT,
    ): Call<ResponseBody>

    @GET("products_data")
    fun getProductGradients(): Call<ResponseBody>

    @Multipart
    @POST("user/products")
    fun createProduct(
        @Part("status") status: RequestBody,
        @Part("category_id") category_id: RequestBody,
        @Part("sub_category_id") sub_category_id: RequestBody,
        @Part("designer_id") designer_id: RequestBody,
        @Part("product_name") product_name: RequestBody,
        @Part("variants") variants: RequestBody,
        @Part("condition") condition: RequestBody,
        @Part("currency_id") currency_id: RequestBody,
        @Part("product_description") product_description: RequestBody,
        @Part("measurements") measurements: RequestBody,
        @Part("shiping_zones") shiping_zones: RequestBody,
        @Part photo: Array<MultipartBody.Part>?,
    ): Call<ResponseBody>

    @Multipart
    @POST("user/products/{id}")
    fun updateProduct(
        @Path("id") id: Int,
        @Part("status") status: RequestBody,
        @Part("category_id") category_id: RequestBody,
        @Part("sub_category_id") sub_category_id: RequestBody,
        @Part("designer_id") designer_id: RequestBody,
        @Part("product_name") product_name: RequestBody,
        @Part("variants") variants: RequestBody,
        @Part("condition") condition: RequestBody,
        @Part("currency_id") currency_id: RequestBody,
        @Part("product_description") product_description: RequestBody,
        @Part("measurements") measurements: RequestBody,
        @Part("shiping_zones") shiping_zones: RequestBody,
        @Part photo: Array<MultipartBody.Part>?,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/become_seller")
    fun becomeSeller(
        @Field("company_name") company_name: String,
        @Field("country_code") country_code: String,
        @Field("mobile_number") mobile_number: String,
        @Field("country_id") country_id: Int,
        @Field("state_id") state_id: Int,
        @Field("city_id") city_id: Int,
        @Field("zip_code") zip_code: String,
        @Field("street_address") street_address: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/register_device")
    fun registerDevice(
        @Field("token") token: String,
        @Field("device_id") device_id: String,
        @Field("type") type: String,
        @Field("app_mode") app_mode: String,
        @Field("device_name") device_name: String,
    ): Call<ResponseBody>

    @DELETE("user/product_files/{id}")
    fun deleteProductFiles(
        @Path("id") id: Int
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/notifications_setting")
    fun saveNotificationSettings(
        @Field("admin_approvals_alert") admin_approvals_alert: Int,
        @Field("order_updates_alert") order_updates_alert: Int,
        @Field("messages_alert") messages_alert: Int,
        @Field("payment_updates_alert") payment_updates_alert: Int,
        @Field("profile_updates_alert") profile_updates_alert: Int,
    ): Call<ResponseBody>


    @GET("designers")
    fun getAllDesigners(): Call<ResponseBody>


    @GET("categories")
    fun getDesignersCategoriesWithProducts(
        @Query("with_products") with_products: Int = 1,
        @Query("designer_id") designer_id: Int,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = AppController.PAGINATION_COUNT,
    ): Call<ResponseBody>

}





















