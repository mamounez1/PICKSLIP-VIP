package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "predictions",
    foreignKeys = [
        ForeignKey(
            entity = FixtureEntity::class,
            parentColumns = ["id"],
            childColumns = ["fixtureId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fixtureId")]
)
data class PredictionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val fixtureId: Long,
    val pick: String, // e.g. "Over 2.5 Buts", "BTTS (Les 2 marquent)", "Victoire Domicile (1)", "Double Chance (1X)"
    val pickType: String = "OVER_25", // "OVER_25", "UNDER_25", "BTTS_YES", "HOME_WIN", "AWAY_WIN", "DRAW", "DC_1X", "DC_X2"
    val confidence: Double, // 50.0 - 95.0 %
    val reasoning: String, // French reasoning
    val isFreePick: Boolean, // First daily pick is true, rest false (VIP)
    val result: String = "pending", // "pending", "win", "loss", "void"
    val oddsEst: Double = 1.85,
    val createdAt: Long = System.currentTimeMillis()
)

// Data class uniting Prediction with its associated Fixture for UI
data class PredictionWithFixture(
    val prediction: PredictionEntity,
    val fixture: FixtureEntity
)
