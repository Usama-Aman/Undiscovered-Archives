package com.codingpixel.undiscoveredarchives.home.notifications

data class NotificationsModel(
    val count: Int,
    val `data`: List<NotificationsData>,
    val message: String,
    val status: Boolean
)

data class NotificationsData(
    val created_at: String,
    val from_id: Int,
    val from_type: String,
    val id: Int,
    val is_read: Int,
    val notification: String,
    val notification_type: String,
    val time: String,
    val title: String,
    val user_id: Int
)