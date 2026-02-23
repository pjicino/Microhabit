package com.microhabit.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert
    suspend fun insert(record: HabitRecord)

    /**
     * 查询指定时间之后的所有记录，用于熔断机制判断
     */
    @Query("SELECT * FROM habit_records WHERE timestamp > :since ORDER BY timestamp DESC")
    fun getRecordsSince(since: Long): Flow<List<HabitRecord>>

    @Query("SELECT * FROM habit_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<HabitRecord>>
}
