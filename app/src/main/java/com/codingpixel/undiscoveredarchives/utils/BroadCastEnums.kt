package com.codingpixel.undiscoveredarchives.utils

enum class BroadCastEnums(val type: String) {
    REFRESH_PUBLISHED_PRODUCTS("RefreshPublishedProducts"),
    REFRESH_TRASH_PRODUCTS("RefreshTrashProducts"),
    REFRESH_DRAFT_PRODUCTS("RefreshDraftProducts"),
}