package com.example.trasex

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar
import com.example.trasex.Auth.SignInActivity
import com.example.trasex.Directionhelpers.FetchURL
import com.example.trasex.Directionhelpers.TaskLoadedCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.smarteist.autoimageslider.IndicatorAnimations
import com.smarteist.autoimageslider.SliderAnimations
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.navigation_drawer.view.*
import kotlinx.android.synthetic.main.sheet_map.*


class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, TaskLoadedCallback {

    private val PERMISSION_ID: Int = 42
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationPermissionGranted: Boolean = false
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private lateinit var currentPolyline: Polyline
    private lateinit var mLocation: LatLng
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

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
        mFusedLocationClient.lastLocation.addOnSuccessListener { mLocation = LatLng(it.latitude, it.longitude) }
        enableLocalization()
        updateUILayer()
        initToolbar()
        initNavigationMenu()
        initBottomSheet()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val bounds = LatLngBounds(LatLng(48.844458, 13.914181), LatLng(54.972622, 23.583997))
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.12).toInt()

        if (mLocationPermissionGranted) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
        }
        val hashMap: HashMap<String, Pair<String, LatLng>> = HashMap()
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data: DataSnapshot in dataSnapshot.children.filter { it.key == "routes" }.flatMap { it.children }) {
                    hashMap[data.key.toString()] = Pair(
                        data.child("type").value.toString(), LatLng(
                            data.child("origin").child("latitude").value.toString().toDouble(),
                            data.child("origin").child("longitude").value.toString().toDouble()
                        )
                    )
                }
                for (item in hashMap) {
                    when (item.value.first) {
                        "road" -> googleMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_road)).position(item.value.second))
                            .tag = item.key
                        "city" -> googleMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_city)).position(item.value.second))
                            .tag = item.key
                        "mountain" -> googleMap.addMarker(
                            MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_mountain)).position(
                                item.value.second
                            )
                        ).tag = item.key
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(this@MapsActivity.toString(), "loadPost:onCancelled", databaseError.toException())
            }
        }
        QApp.fData.reference.addValueEventListener(postListener)
        mMap.setOnMarkerClickListener(this)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding))
        mMap.setOnMapClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            currentPolyline.remove()
            mMap.setPadding(0, 0, 0, 0)
        }
    }


    override fun onMarkerClick(marker: Marker?): Boolean {
        val markerId = marker?.tag.toString()
        val markerListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val route = dataSnapshot.child("routes").child(markerId).getValue(Route::class.java)!!
                FetchURL(this@MapsActivity).execute(getUrl(route))
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        LatLngBounds(
                            LatLng(route.bounds[1].latitude, route.bounds[1].longitude),
                            LatLng(route.bounds[0].latitude, route.bounds[0].longitude)
                        ),
                        resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels,
                        100
                    )
                )
                route_name.text = route.name
                route_distance.text = route.distanceText
                route_time.text = route.timeText
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MapsActivity, "Wystąpił błąd podczas pobierania trasy", Toast.LENGTH_SHORT).show()
            }
        }
        QApp.fData.reference.addListenerForSingleValueEvent(markerListener)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        mMap.setPadding(0, 0, 0, 200)
        return true
    }

    override fun onTaskDone(vararg values: Any?) {
        currentPolyline = if (::currentPolyline.isInitialized) {
            currentPolyline.remove()
            mMap.addPolyline(values[0] as PolylineOptions?)
        } else {
            mMap.addPolyline(values[0] as PolylineOptions?)
        }
    }

    //region permissions
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    //endregion

    //region menu

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_basic, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            QApp.fAuth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initNavigationMenu() {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.inflateHeaderView(R.layout.navigation_drawer)
        val header: View = navigationView.getHeaderView(0)
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle: ActionBarDrawerToggle =
            object : ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            }
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener {
            drawer.closeDrawers()
            true
        }

        val seekBarSearchRadius: CrystalSeekbar = header.seekbar_search_radius
        seekBarSearchRadius.setOnSeekbarChangeListener { minValue -> header.search_radius.text = minValue.toString() + "km" }

        val seekBarSearchRouteLength: CrystalRangeSeekbar = header.seekbar_search_route_length
        seekBarSearchRouteLength.setOnRangeSeekbarChangeListener(object : OnRangeSeekbarChangeListener {
            override fun valueChanged(minValue: Number, maxValue: Number) {
                header.search_route_length.text = minValue.toString() + " - " + maxValue.toString() + "km"
            }
        })

        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val filteredListType: MutableList<DataSnapshot> = arrayListOf()
                val list: List<DataSnapshot> =
                    dataSnapshot.children.filter { it.key == "routes" }.flatMap { it.children } as MutableList<DataSnapshot>
                if (header.checkBoxRoad.isChecked) {
                    filteredListType.addAll(list.filter { it.child("type").value == "road" })
                }
                if (header.checkBoxCity.isChecked) {
                    filteredListType.addAll(list.filter { it.child("type").value == "city" })
                }
                if (header.checkBoxMountain.isChecked) {
                    filteredListType.addAll(list.filter { it.child("type").value == "mountain" })
                }
                val filteredListDistance: MutableList<DataSnapshot> = arrayListOf()
                filteredListDistance.addAll(filteredListType.filter {
                    it.child("distance").value.toString().toInt() / 1000 > header.seekbar_search_route_length.selectedMinValue.toInt()
                            && it.child("distance").value.toString().toInt() / 1000 < header.seekbar_search_route_length.selectedMaxValue.toInt()
                })
                val filteredListRadius: MutableList<DataSnapshot> = arrayListOf()
                for (item in filteredListDistance) {
                    val origin = LatLng(
                        item.child("origin").child("latitude").value.toString().toDouble(),
                        filteredListDistance[0].child("origin").child("longitude").value.toString().toDouble()
                    )
                    val pointLocation = Location("currentLocation")
                    pointLocation.latitude = origin.latitude
                    pointLocation.longitude = origin.longitude
                    val currentLocation = Location("currentLocation")
                    currentLocation.latitude = mLocation.latitude
                    currentLocation.longitude = mLocation.longitude
                    if (pointLocation.distanceTo(currentLocation) / 1000 < header.seekbar_search_radius.selectedMinValue.toInt()) {
                        filteredListRadius.add(item)
                    }
                }
                mMap.clear()
                for (item in filteredListRadius) {
                    when (item.child("type").value) {
                        "road" -> mMap.addMarker(
                            MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_road)).position(
                                LatLng(
                                    item.child("origin").child("latitude").value.toString().toDouble(),
                                    item.child("origin").child("longitude").value.toString().toDouble()
                                )
                            )
                        )
                            .tag = item.key
                        "city" -> mMap.addMarker(
                            MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_city)).position(
                                LatLng(
                                    item.child("origin").child("latitude").value.toString().toDouble(),
                                    item.child("origin").child("longitude").value.toString().toDouble()
                                )
                            )
                        )
                            .tag = item.key
                        "mountain" -> mMap.addMarker(
                            MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_mountain)).position(
                                LatLng(
                                    item.child("origin").child("latitude").value.toString().toDouble(),
                                    item.child("origin").child("longitude").value.toString().toDouble()
                                )
                            )
                        ).tag = item.key
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        header.search_button.setOnClickListener {
            QApp.fData.reference.addListenerForSingleValueEvent(eventListener)
            drawer.closeDrawers()
        }
    }

    private fun initBottomSheet() { // get the bottom sheet view
        val llBottomSheet = findViewById<LinearLayout>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(p0: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    BottomSheetBehavior.STATE_DRAGGING -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_DRAGGING
                    BottomSheetBehavior.PEEK_HEIGHT_AUTO -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    BottomSheetBehavior.STATE_SETTLING -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_SETTLING
                    BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
            }
        })
        imageSlider.sliderAdapter = SliderAdapter(this)
        imageSlider.setIndicatorAnimation(IndicatorAnimations.DROP)
        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
    }

    //endregion

    private fun getUrl(trasa: Route): String? { // Origin of route
        val strOrigin = "origin=" + trasa.origin.latitude + "," + trasa.origin.longitude
        // Destination of route
        val strDest = "destination=" + trasa.destination.latitude + "," + trasa.destination.longitude
        // Mode
        val mode = "mode=${trasa.mode}"
        // Building the parameters to the web service
        var strWaypoints = "waypoints=via:"
        for (point in trasa.waypoints) {
            strWaypoints += point.latitude.toString() + "," + point.longitude.toString() + "|"
        }
        val parameters = "$strOrigin&$strWaypoints&$strDest&$mode"
        // Output format
        val output = "json"
        // Building the url to the web service
        val final = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key)
        return final
    }

    //region lifecycle
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
    //endregion
}
