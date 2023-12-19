package com.codingpixel.undiscoveredarchives.home.main.models

data class FeaturedProductsModel(
    val `data`: List<FeaturedProductsData>,
    val message: String,
    val status: Boolean
)

data class FeaturedProductsData(
    val category: Category,
    val condition: String,
    val designer: Designer,
    val files: List<File>,
    val id: Int,
    val is_favourite: Int,
    val is_stock_available: Int,
    val orders_count: Int,
    val product_description: String,
    val product_name: String,
    val published_at: String,
    val reviews: List<Any>,
    val reviews_avg_rating: Any,
    val status: String,
    val sub_category: SubCategory,
    val trashed_at: String,
    val variants: List<Variant>,
    val variants_max_price: String,
    val variants_min_price: String,
    val vendor: Vendor
)

data class Category(
    val add_by: Int,
    val description: String,
    val id: Int,
    val image: String,
    val image_path: String,
    val title: String
)

data class Designer(
    val description: String,
    val id: Int,
    val logo: String,
    val logo_path: String,
    val name: String,
    val owner_id: Int
)

data class SubCategory(
    val category_id: Int,
    val description: String,
    val id: Int,
    val image: String,
    val image_path: String,
    val title: String
)

data class Variant(
    val color_id: Int,
    val compare_at_price: String,
    val id: Int,
    val price: String,
    val product_id: Int,
    val quantity: Int,
    val size_id: Int
)

data class Vendor(
    val country_code: String,
    val email: String,
    val id: Int,
    val mobile_number: String,
    val name: String,
    val photo_path: Any,
    val products_count: Any
)