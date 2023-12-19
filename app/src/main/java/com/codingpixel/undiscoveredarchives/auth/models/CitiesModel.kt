package com.codingpixel.undiscoveredarchives.auth.models

data class CitiesModel(
    val `data`: List<CitiesData>,
    val message: String,
    val status: Boolean
)

data class CitiesData(
    val country_id: Int,
    val id: Int,
    val name: String
)