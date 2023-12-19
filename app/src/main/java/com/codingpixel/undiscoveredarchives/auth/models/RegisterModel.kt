package com.codingpixel.undiscoveredarchives.auth.models

data class RegisterModel(
    val access_token: String,
    val `data`: UserDetailData,
    val message: String,
    val status: Boolean
)

data class UserDetailData(
    val allow_notifications: Int,
    val country_code: String,
    val email: String,
    val email_verified_at: Int,
    val id: Int,
    val is_active: Int,
    val is_age_verified: Int,
    val is_nineteen_plus: Int,
    val listings_count: Int,
    val location: UserLocation?,
    val mobile_number: String,
    val name: String,
    val new_notifications_count: Int,
    val phone_verified_at: Int,
    val photo_path: String,
    val transactions_count: Int,
    var user_detail: UserDetail,
    val is_vendor: Int,
    val company_name: String,
    val approval_status: String, //Approved, Pending
)

data class UserLocation(
    val city_id: Int,
    val country_id: Int,
    val id: Int,
    val state_id: Int,
    val street_address: String,
    val zip_code: String,
)

data class UserDetail(
    var admin_approvals_alert: Int = 0,
    val id: Int = 0,
    var messages_alert: Int = 0,
    var order_updates_alert: Int = 0,
    var payment_updates_alert: Int = 0,
    var profile_updates_alert: Int = 0,
    val user_id: Int = 0
)