package com.example.trasex

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.trasex.Auth.SignInActivity
import com.example.trasex.Directionhelpers.FetchURL
import com.example.trasex.Directionhelpers.TaskLoadedCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_map.*


class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnPoiClickListener, TaskLoadedCallback {

    private val PERMISSION_ID: Int = 42
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationPermissionGranted: Boolean = false
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private lateinit var currentPolyline: Polyline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        if (QApp.fUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableLocalization()
        updateUILayer()

        buttonDirections.setOnClickListener {
            FetchURL(this).execute(
                getUrl(
                    LatLng(51.2368439, 22.5484569),
                    LatLng(51.2380664, 22.5302608),
                    mutableListOf(LatLng(51.2346674, 22.5423844)),
                    "cycling"
                )
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnPoiClickListener(this)

        if (mLocationPermissionGranted)
            mMap.isMyLocationEnabled = true
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                mLocationPermissionGranted = true
                mMap.isMyLocationEnabled = true
                updateUILayer()
            }
        }
    }

    private fun updateUILayer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun enableLocalization() {
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onPoiClick(poi: PointOfInterest) {
        Toast.makeText(
            applicationContext, "Clicked: " +
                    poi.name + "\nPlace ID:" + poi.placeId +
                    "\nLatitude:" + poi.latLng.latitude +
                    " Longitude:" + poi.latLng.longitude,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    private fun getUrl(origin: LatLng, dest: LatLng, waypoints: MutableList<LatLng>, directionMode: String): String? { // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
        // Mode
        val mode = "mode=$directionMode"
        // Building the parameters to the web service
        var str_waypoints = "waypoints="
        for (point in waypoints) {
            str_waypoints = "via:" + point.longitude + "," + point.longitude
        }
        val parameters = "$str_origin&$str_dest&$str_waypoints&$mode"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key)
    }

    override fun onTaskDone(vararg values: Any?) {
        currentPolyline = mMap.addPolyline(values[0] as PolylineOptions?)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}
