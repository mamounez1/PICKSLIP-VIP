package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FixtureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FixtureDao {
    @Query("SELECT * FROM fixtures ORDER BY kickoff ASC")
    fun getAllFixtures(): Flow<List<FixtureEntity>>

    @Query("SELECT * FROM fixtures WHERE id = :id")
    suspend fun getFixtureById(id: Long): FixtureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixtures(fixtures: List<FixtureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixture(fixture: FixtureEntity)

    @Update
    suspend fun updateFixture(fixture: FixtureEntity)

    @Query("UPDATE fixtures SET status = :status, homeScore = :homeScore, awayScore = :awayScore WHERE id = :id")
    suspend fun updateFixtureResult(id: Long, status: String, homeScore: Int, awayScore: Int)

    @Query("DELETE FROM fixtures")
    suspend fun clearFixtures()
}
