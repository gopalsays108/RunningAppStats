package com.gopal.runningappstats.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Convertors {

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytArray, 0, bytArray.size)
    }
}