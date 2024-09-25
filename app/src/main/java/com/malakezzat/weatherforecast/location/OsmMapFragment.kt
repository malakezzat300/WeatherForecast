package com.malakezzat.weatherforecast.location

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View

import com.malakezzat.weatherforecast.R
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class OsmMapFragment(private val isHome : Boolean ) : Fragment(R.layout.fragment_maps) {
    companion object {
        const val TAG = "OsmMapFragment"
    }

    private lateinit var mapView: MapView
    private var isZooming = false
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(0))

        mapView = view.findViewById(R.id.map)

        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller
        mapController.setZoom(5.0)
        val startPoint = GeoPoint(27.18039285293778, 31.186714348461493)
        mapController.setCenter(startPoint)

        val startMarker = Marker(mapView)
        startMarker.position = startPoint
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)


        mapView.setOnTouchListener { _, event ->
            handleMapTouch(event)
        }
    }

    private fun handleMapTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isZooming = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount > 1) {
                    isZooming = true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isZooming) {
                    val projection = mapView.projection
                    val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint

                    val latitude = geoPoint.latitude
                    val longitude = geoPoint.longitude

                    addMarkerAtLocation(latitude, longitude)
                    val bottomSheet = MarkerBottomSheet(latitude, longitude,isHome)
                    bottomSheet.show(parentFragmentManager, "MarkerBottomSheet")

                    Log.d(TAG, "Tapped location: Latitude: $latitude, Longitude: $longitude")
                }
            }
        }
        return false
    }

    private fun addMarkerAtLocation(latitude: Double, longitude: Double) {
        val newMarker = Marker(mapView)
        newMarker.position = GeoPoint(latitude, longitude)
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        newMarker.title = "Marker at ($latitude, $longitude)"

        mapView.overlays.clear()

        mapView.overlays.add(newMarker)
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isHome) {
            activity?.finish()
        }
    }
}