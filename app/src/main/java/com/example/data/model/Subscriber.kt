package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscribers")
data class SubscriberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val email: String = "",
    val stripeCustomerId: String = "",
    val stripeSubscriptionId: String = "",
    val subscriptionStatus: String = "free", // "free", "active", "canceled", "past_due"
    val currentPeriodEnd: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
