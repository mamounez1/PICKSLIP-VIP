package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fixtures")
data class FixtureEntity(
    @PrimaryKey
    val id: Long, // API-Football fixture id
    val league: String,
    val leagueCountry: String = "Europe",
    val homeTeam: String,
    val awayTeam: String,
    val kickoff: String,
    val homeTeamId: Long,
    val awayTeamId: Long,
    val status: String = "scheduled", // "scheduled", "finished", "in_play"
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    // Statistical inputs snapshot
    val homeRecentForm: String = "WWDLW",
    val awayRecentForm: String = "WLDWD",
    val homeGoalsForAvg: Double = 1.9,
    val homeGoalsAgainstAvg: Double = 0.9,
    val awayGoalsForAvg: Double = 1.4,
    val awayGoalsAgainstAvg: Double = 1.5,
    val h2hBttsRate: Double = 0.65,
    val h2hOver25Rate: Double = 0.60
)
