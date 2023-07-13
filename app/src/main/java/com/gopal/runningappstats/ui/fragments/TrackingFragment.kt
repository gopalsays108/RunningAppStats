package com.gopal.runningappstats.ui.fragments

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.gopal.runningappstats.R
import com.gopal.runningappstats.databinding.FragmentTrackingBinding
import com.gopal.runningappstats.services.Polyline
import com.gopal.runningappstats.services.TrackingService
import com.gopal.runningappstats.ui.viewmodels.MainViewModel
import com.gopal.runningappstats.utils.Constant.ACTION_PAUSE_SERVICE
import com.gopal.runningappstats.utils.Constant.ACTION_START_OR_RESUME_SERVICE
import com.gopal.runningappstats.utils.Constant.ACTION_STOP_SERVICE
import com.gopal.runningappstats.utils.Constant.POLYLINES_COLOR
import com.gopal.runningappstats.utils.Constant.POLYLINES_WIDTH
import com.gopal.runningappstats.utils.Constant.ZOOM_LEVEL
import com.gopal.runningappstats.utils.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackingFragment : Fragment() {
    private lateinit var trackingBinding: FragmentTrackingBinding
    private val mainViewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoint = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        trackingBinding = FragmentTrackingBinding.inflate(inflater)
        return trackingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackingBinding.mapView.onCreate(savedInstanceState)
        trackingBinding.mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }

        trackingBinding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        subscribeToObserver()
    }

    private fun subscribeToObserver() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        TrackingService.pathPoint.observe(viewLifecycleOwner) {
            pathPoint = it
            addLatestPolyLine()
            funMoveCameraToUser()
        }

        TrackingService.timeRunInMills.observe(viewLifecycleOwner){
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis,true)
            trackingBinding.tvTimer.text = formattedTime
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            trackingBinding.btnToggleRun.text = "Start"
            trackingBinding.btnFinishRun.visibility = View.VISIBLE
        } else {
            trackingBinding.btnToggleRun.text = "Stop"
            trackingBinding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun funMoveCameraToUser() {
        if (pathPoint.isNotEmpty() && pathPoint.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoint.last().last(),
                    ZOOM_LEVEL
                )
            )
        }
    }

    private fun addAllPolyLines() {
        for (polyline in pathPoint) {
            val polylineOption = PolylineOptions()
                .color(POLYLINES_COLOR)
                .width(POLYLINES_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOption)
        }
    }

    private fun addLatestPolyLine() {
        if (pathPoint.isNotEmpty() && pathPoint.last().size > 1) {
            val preLastLng = pathPoint.last()[pathPoint.last().size - 2]
            val lastLng = pathPoint.last().last()
            val polyLinesOption = PolylineOptions()
                .color(POLYLINES_COLOR)
                .width(POLYLINES_WIDTH)
                .add(preLastLng)
                .add(lastLng)

            map?.addPolyline(polyLinesOption)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onStart() {
        super.onStart()
        trackingBinding.mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        trackingBinding.mapView?.onResume()
    }

    override fun onStop() {
        super.onStop()
        trackingBinding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        trackingBinding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        trackingBinding.mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        trackingBinding.mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        trackingBinding.mapView.onSaveInstanceState(outState)
    }
}