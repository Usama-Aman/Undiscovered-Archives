package com.codingpixel.undiscoveredarchives.utils

enum class OrderDetailEnum(val type: String) {
    CURRENT_SALES("current_sales"),
    SALES_HISTORY("sales_history"),
    CURRENT_ORDERS("current_orders"),
    PURCHASE_HISTORY("purchase_history"),
    PAYMENT("payment"),
    SHIPPING("shipping"),
    DRAFTS("drafts"),
    PUBLISHED_PRODUCTS("published_products"),
    TRASH_PRODUCTS("trash_products"),
}