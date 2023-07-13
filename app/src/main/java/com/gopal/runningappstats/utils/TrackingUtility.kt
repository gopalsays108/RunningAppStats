package com.gopal.runningappstats.utils

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import pub.devrel.easypermissions.EasyPermissions
import java.sql.Time
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasBGLocationPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
        }

     fun    getFormattedStopWatchTime(ms: Long, includeMillis: Boolean): String {
        var milliseconds = ms
        val hour = TimeUnit.MILLISECONDS.toHours(ms)
        milliseconds -= TimeUnit.HOURS.toMillis(hour)

        val min = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(min)

        val sec = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        if (!includeMillis) {
            return "${if (hour < 10) "0" else ""}$hour:" +
                    "${if (min < 10) "0" else ""}$min:" +
                    "${if (sec < 10) "0" else ""}$sec"
        }

        milliseconds -= TimeUnit.SECONDS.toMillis(sec)
        milliseconds /= 10
        return "${if (hour < 10) "0" else ""}$hour:" +
                "${if (min < 10) "0" else ""}$min:" +
                "${if (sec < 10) "0" else ""}$sec:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds"

    }
}