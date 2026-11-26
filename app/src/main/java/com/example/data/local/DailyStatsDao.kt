package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM stats_daily ORDER BY date DESC")
    fun getAllDailyStats(): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM stats_daily WHERE date = :date LIMIT 1")
    suspend fun getDailyStatsForDate(date: String): DailyStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyStats(stats: DailyStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<DailyStatsEntity>)

    @Query("DELETE FROM stats_daily")
    suspend fun clearDailyStats()
}
