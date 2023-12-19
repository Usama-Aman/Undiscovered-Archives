package com.codingpixel.undiscoveredarchives.home.main.models

data class CategoriesModel(
    val `data`: List<CategoriesData>,
    val message: String,
    val status: Boolean
)

data class CategoriesData(
    val add_by: Int,
    val description: String,
    val id: Int,
    val image: String,
    val image_path: String,
    val products: List<CategoriesProduct>,
    val products_count: Int,
    val title: String
)

data class CategoriesProduct(
    val category_id: Int,
    val condition: String,
    val deleted_at: Any,
    val designer_id: Int,
    val files: List<File>,
    val id: Int,
    val is_approved: Int,
    val measurements: String,
    val product_description: String,
    val product_name: String,
    val published_at: Any,
    val status: String,
    val sub_category_id: Int,
    val user_id: Int,
    val variants_max_price: String,
    val variants_min_price: String
)

data class File(
    val file_name: String,
    val file_path: String,
    val id: Int,
    val product_id: Int
)