package com.gopal.runningappstats.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    var image: Bitmap? = null,
    var timestamp: Long = 0L, // WHen our run was
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeter: Int = 0,
    var timeInMillis: Long = 0L,// How long our run was
    var caloriesBurned: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}