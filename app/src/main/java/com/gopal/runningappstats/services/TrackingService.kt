package com.gopal.runningappstats.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.gopal.runningappstats.R
import com.gopal.runningappstats.ui.MainActivity
import com.gopal.runningappstats.utils.Constant.ACTION_PAUSE_SERVICE
import com.gopal.runningappstats.utils.Constant.ACTION_SHOW_TRACKING_FRAGMENT
import com.gopal.runningappstats.utils.Constant.ACTION_START_OR_RESUME_SERVICE
import com.gopal.runningappstats.utils.Constant.ACTION_STOP_SERVICE
import com.gopal.runningappstats.utils.Constant.NOTIFICATION_CHANNEL_ID
import com.gopal.runningappstats.utils.Constant.NOTIFICATION_CHANNEL_NAME
import com.gopal.runningappstats.utils.Constant.NOTIFICATION_ID
import com.gopal.runningappstats.utils.TrackingUtility
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.gopal.runningappstats.utils.Constant.FASTEST_LOCATION_INTERVAL
import com.gopal.runningappstats.utils.Constant.LOCATION_UPDATE_INTERVAL
import com.gopal.runningappstats.utils.Constant.TIMER_UPDATE_INTERVAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    companion object {
        //        val pathPoint = MutableLiveData<MutableLiveData<MutableList<LatLng>>>()
        val pathPoint = MutableLiveData<Polylines>()
        val isTracking = MutableLiveData<Boolean>()
        val timeRunInMills = MutableLiveData<Long>()
    }

    private fun postInitialValue() {
        isTracking.postValue(true)
        pathPoint.postValue(mutableListOf())
        timeRunInMills.postValue(0)
        timeRunInSec.postValue(0L)
    }

    var isFirstRun = true
    private val timeRunInSec = MutableLiveData<Long>()
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var totalTimeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L


    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        postInitialValue()
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun startTimer(){
        addEmptyPolylines()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        lifecycleScope.launch(Dispatchers.Main) {
            while (isTracking.value!!){
                // Time difference between now and time started
                lapTime  = System.currentTimeMillis() - timeStarted

                //Post new Lap time
                timeRunInMills.postValue(totalTimeRun + lapTime)


                if(timeRunInMills.value!! >=lastSecondTimeStamp + 1000L){
                    timeRunInSec.postValue(timeRunInSec.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }

            totalTimeRun += lapTime
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForeGroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                        Timber.d("resume service")
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    Timber.d("Serice paused")
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Serice Stop")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasBGLocationPermission(this)) {
                val re = LocationRequest.Builder(LOCATION_UPDATE_INTERVAL).apply {
                    setIntervalMillis(LOCATION_UPDATE_INTERVAL)
                    setPriority(PRIORITY_HIGH_ACCURACY)
                    setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                }.build()

                fusedLocationProviderClient.requestLocationUpdates(
                    re,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(results: LocationResult) {
            super.onLocationResult(results)
            if (isTracking.value!!) {
                results?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("New location: ${location.latitude} and ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoint.value?.apply {
                last().add(pos)
                pathPoint.postValue(this)
            }
        }
    }

    private fun addEmptyPolylines() = pathPoint.value?.apply {
        add(mutableListOf())
        pathPoint.postValue(this)
    } ?: pathPoint.postValue(mutableListOf(mutableListOf()))

    private fun startForeGroundService() {
        startTimer()
        isTracking.postValue(true)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentText("00:00:00")
            .setContentTitle("Running App")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )
}