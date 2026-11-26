package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.PredictionEntity
import com.example.data.model.PredictionWithFixture
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Transaction
    @Query("""
        SELECT predictions.*, fixtures.id as f_id, fixtures.league as f_league,
               fixtures.leagueCountry as f_leagueCountry, fixtures.homeTeam as f_homeTeam,
               fixtures.awayTeam as f_awayTeam, fixtures.kickoff as f_kickoff,
               fixtures.homeTeamId as f_homeTeamId, fixtures.awayTeamId as f_awayTeamId,
               fixtures.status as f_status, fixtures.homeScore as f_homeScore,
               fixtures.awayScore as f_awayScore, fixtures.homeRecentForm as f_homeRecentForm,
               fixtures.awayRecentForm as f_awayRecentForm, fixtures.homeGoalsForAvg as f_homeGoalsForAvg,
               fixtures.homeGoalsAgainstAvg as f_homeGoalsAgainstAvg, fixtures.awayGoalsForAvg as f_awayGoalsForAvg,
               fixtures.awayGoalsAgainstAvg as f_awayGoalsAgainstAvg, fixtures.h2hBttsRate as f_h2hBttsRate,
               fixtures.h2hOver25Rate as f_h2hOver25Rate
        FROM predictions
        INNER JOIN fixtures ON predictions.fixtureId = fixtures.id
        ORDER BY predictions.createdAt DESC
    """)
    fun getAllPredictionsRaw(): Flow<List<PredictionWithFixtureRaw>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPredictions(predictions: List<PredictionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionEntity)

    @Query("UPDATE predictions SET result = :result WHERE id = :id")
    suspend fun updatePredictionResult(id: String, result: String)

    @Query("SELECT * FROM predictions WHERE result = 'pending'")
    suspend fun getPendingPredictions(): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE result IN ('win', 'loss')")
    suspend fun getResolvedPredictions(): List<PredictionEntity>

    @Query("SELECT * FROM predictions")
    suspend fun getAllPredictionsDirect(): List<PredictionEntity>

    @Query("DELETE FROM predictions")
    suspend fun clearPredictions()
}

// Helper class for Raw Join
data class PredictionWithFixtureRaw(
    // Prediction fields
    val id: String,
    val fixtureId: Long,
    val pick: String,
    val pickType: String,
    val confidence: Double,
    val reasoning: String,
    val isFreePick: Boolean,
    val result: String,
    val oddsEst: Double,
    val createdAt: Long,
    // Fixture fields
    val f_id: Long,
    val f_league: String,
    val f_leagueCountry: String,
    val f_homeTeam: String,
    val f_awayTeam: String,
    val f_kickoff: String,
    val f_homeTeamId: Long,
    val f_awayTeamId: Long,
    val f_status: String,
    val f_homeScore: Int?,
    val f_awayScore: Int?,
    val f_homeRecentForm: String,
    val f_awayRecentForm: String,
    val f_homeGoalsForAvg: Double,
    val f_homeGoalsAgainstAvg: Double,
    val f_awayGoalsForAvg: Double,
    val f_awayGoalsAgainstAvg: Double,
    val f_h2hBttsRate: Double,
    val f_h2hOver25Rate: Double
) {
    fun toDomain(): PredictionWithFixture {
        return PredictionWithFixture(
            prediction = PredictionEntity(
                id = id,
                fixtureId = fixtureId,
                pick = pick,
                pickType = pickType,
                confidence = confidence,
                reasoning = reasoning,
                isFreePick = isFreePick,
                result = result,
                oddsEst = oddsEst,
                createdAt = createdAt
            ),
            fixture = com.example.data.model.FixtureEntity(
                id = f_id,
                league = f_league,
                leagueCountry = f_leagueCountry,
                homeTeam = f_homeTeam,
                awayTeam = f_awayTeam,
                kickoff = f_kickoff,
                homeTeamId = f_homeTeamId,
                awayTeamId = f_awayTeamId,
                status = f_status,
                homeScore = f_homeScore,
                awayScore = f_awayScore,
                homeRecentForm = f_homeRecentForm,
                awayRecentForm = f_awayRecentForm,
                homeGoalsForAvg = f_homeGoalsForAvg,
                homeGoalsAgainstAvg = f_homeGoalsAgainstAvg,
                awayGoalsForAvg = f_awayGoalsForAvg,
                awayGoalsAgainstAvg = f_awayGoalsAgainstAvg,
                h2hBttsRate = f_h2hBttsRate,
                h2hOver25Rate = f_h2hOver25Rate
            )
        )
    }
}
