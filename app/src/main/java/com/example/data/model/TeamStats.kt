package com.example.data.model

data class TeamStatsInput(
    val teamName: String,
    val recentForm: String, // e.g. "WWDLW"
    val goalsForAvg: Double,
    val goalsAgainstAvg: Double
)

data class H2HStatsInput(
    val bttsRate: Double,
    val over25Rate: Double,
    val homeWins: Int,
    val draws: Int,
    val awayWins: Int
)

data class AlgorithmOutput(
    val pick: String,
    val pickType: String,
    val confidence: Double,
    val reasoning: String,
    val estimatedOdds: Double,
    val metrics: AlgorithmMetrics
)

data class AlgorithmMetrics(
    val expectedGoalsHome: Double,
    val expectedGoalsAway: Double,
    val totalExpectedGoals: Double,
    val homeFormIndex: Double,
    val awayFormIndex: Double,
    val formGap: Double,
    val bttsProbability: Double,
    val over25Probability: Double
)
