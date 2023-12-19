package com.codingpixel.undiscoveredarchives.auth.models

    data class StatesModel(
    val `data`: List<StatesData>,
    val message: String,
    val status: Boolean
)

data class StatesData(
    val country_id: Int,
    val id: Int,
    val name: String
)