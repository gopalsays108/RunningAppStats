package com.gopal.runningappstats.repositories

import androidx.lifecycle.LiveData
import com.gopal.runningappstats.db.Run
import com.gopal.runningappstats.db.RunDao
import javax.inject.Inject


class MainRepository @Inject constructor(
    val runDao: RunDao
) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate(): LiveData<List<Run>> = runDao.getAllRunsSortedByDate()

    fun getAllRunSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMS() = runDao.getAllRunsSortedByTimeInMs()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurn() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalTimeInMs() = runDao.getTotalTimeInMs()
}