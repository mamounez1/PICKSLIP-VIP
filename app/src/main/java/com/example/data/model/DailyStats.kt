package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stats_daily")
data class DailyStatsEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val picksCount: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double // e.g. 75.0%
)
