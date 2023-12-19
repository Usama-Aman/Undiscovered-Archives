package com.codingpixel.undiscoveredarchives.home.add_new_listing

import com.codingpixel.undiscoveredarchives.home.favorites.models.ShipingZone

data class ProductsGradientsModel(
    val `data`: ProductsGradientsData,
    val message: String,
    val status: Boolean
)

data class ProductsGradientsData(
    val attributes: List<Attribute>,
    val categories: List<Category>,
    val conditions: List<String>,
    val currencies: List<Currency>,
    val designers: List<Designer>,
    val shiping_zones: List<ShipingZone>,
    val sorting_options: List<String>,
    val sub_categories: List<SubCategory>
)

data class Attribute(
    val category_id: Int,
    val id: Int,
    val type: String,
    val value: String
)

data class Category(
    val create_date: String,
    val description: String,
    val id: Int,
    val image_path: String,
    val title: String
)

data class Currency(
    val code: String,
    val id: Int,
    val name: String,
    val symbol: String
)

data class Designer(
    val id: Int,
    val logo_path: String,
    val name: String
)

data class SubCategory(
    val category_id: Int,
    val description: String,
    val id: Int,
    val image_path: String,
    val title: String
)