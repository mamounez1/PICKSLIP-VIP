package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailyStatsEntity
import com.example.data.model.FixtureEntity
import com.example.data.model.PredictionEntity
import com.example.data.model.SubscriberEntity

@Database(
    entities = [
        SubscriberEntity::class,
        FixtureEntity::class,
        PredictionEntity::class,
        DailyStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PickSlipDatabase : RoomDatabase() {
    abstract fun subscriberDao(): SubscriberDao
    abstract fun fixtureDao(): FixtureDao
    abstract fun predictionDao(): PredictionDao
    abstract fun dailyStatsDao(): DailyStatsDao

    companion object {
        @Volatile
        private var INSTANCE: PickSlipDatabase? = null

        fun getInstance(context: Context): PickSlipDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PickSlipDatabase::class.java,
                    "pickslip_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
