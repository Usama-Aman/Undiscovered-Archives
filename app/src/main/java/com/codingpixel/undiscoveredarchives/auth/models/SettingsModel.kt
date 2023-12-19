package com.codingpixel.undiscoveredarchives.auth.models

data class SettingsModel(
    val `data`: SettingsData,
    val message: String,
    val status: Boolean
)

data class SettingsData(
    val id: Int,
    val setting_key: String,
    val value: String
)