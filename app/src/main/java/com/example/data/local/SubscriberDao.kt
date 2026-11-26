package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SubscriberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriberDao {
    @Query("SELECT * FROM subscribers ORDER BY id DESC LIMIT 1")
    fun getCurrentSubscriber(): Flow<SubscriberEntity?>

    @Query("SELECT * FROM subscribers WHERE email = :email LIMIT 1")
    suspend fun getSubscriberByEmail(email: String): SubscriberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(subscriber: SubscriberEntity): Long

    @Query("UPDATE subscribers SET subscriptionStatus = :status, currentPeriodEnd = :periodEnd WHERE email = :email")
    suspend fun updateStatus(email: String, status: String, periodEnd: Long)

    @Query("DELETE FROM subscribers")
    suspend fun clearSubscribers()
}
