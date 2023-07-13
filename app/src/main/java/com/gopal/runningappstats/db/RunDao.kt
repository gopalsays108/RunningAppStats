package com.gopal.runningappstats.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("Select * from running_table order by timestamp Desc")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMs(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeter DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMs(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeter) FROM running_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(distanceInMeter) FROM running_table")
    fun getTotalAvgSpeed(): LiveData<Float>
}