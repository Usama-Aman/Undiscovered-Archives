package com.codingpixel.undiscoveredarchives.auth.models

data class LoginModel(
    val access_token: String,
    val `data`: UserDetailData,
    val message: String,
    val status: Boolean
)
