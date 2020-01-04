package com.example.travelo.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar
import com.example.travelo.BaseActivity
import com.example.travelo.QApp
import com.example.travelo.R
import com.example.travelo.auth.SignInActivity
import com.example.travelo.database.DatabaseUtils
import com.example.travelo.directionHelpers.FetchURL
import com.example.travelo.directionHelpers.TaskLoadedCallback
import com.example.travelo.lib.ViewAnimation
import com.example.travelo.models.Route
import com.example.travelo.profile.ProfileActivity
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
import com.google.firebase.storage.UploadTask
import com.smarteist.autoimageslider.IndicatorAnimations
import com.smarteist.autoimageslider.SliderAnimations
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.add_photo_buttons.*
import kotlinx.android.synthetic.main.navigation_drawer.view.*
import kotlinx.android.synthetic.main.sheet_map.*


class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    TaskLoadedCallback {

    private val PERMISSION_ID: Int = 42
    private val REQUEST_IMAGE_CAPTURE = 1
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private val REQUEST_PICK_IMAGE = 2

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var currentPolyline: Polyline
    private lateinit var mLocation: LatLng
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var markerId: String
    private var mLocationPermissionGranted: Boolean = false
    private var rotate = false
    private var images = arrayListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (QApp.fUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        setContentView(R.layout.activity_map)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        enableLocalization()
        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.lastLocation.addOnSuccessListener {
            mLocation = LatLng(it.latitude, it.longitude)
        }
        updateUILayer()
        initToolbar()
        initNavigationMenu()
        initBottomSheet()

    }

    private fun drawMarker(drawable: Int): MarkerOptions {
        return MarkerOptions().icon(
            BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(
                        resources,
                        drawable
                    ), 130, 194, false
                )
            )
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val boundsPoland = LatLngBounds(LatLng(48.844458, 13.914181), LatLng(54.972622, 23.583997))
        val boundsLublin = LatLngBounds(LatLng(51.066020, 22.340719), LatLng(51.355511, 22.742954))
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
                        "road" -> googleMap.addMarker(
                            drawMarker(R.drawable.ic_marker_cyclist_road).position(
                                item.value.second
                            )
                        )
                            .tag = item.key
                        "city" -> googleMap.addMarker(
                            drawMarker(R.drawable.ic_marker_cyclist_city).position(item.value.second)
                        )
                            .tag = item.key
                        "mountain" -> googleMap.addMarker(
                            drawMarker(R.drawable.ic_marker_cyclist_mountain).position(
                                item.value.second
                            )
                        ).tag = item.key
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(
                    this@MapsActivity.toString(),
                    "loadPost:onCancelled",
                    databaseError.toException()
                )
            }
        }
        QApp.fData.reference.addValueEventListener(postListener)
        mMap.setOnMarkerClickListener(this)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsLublin, width, height, padding))
        mMap.setOnMapClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            if (::currentPolyline.isInitialized) {
                currentPolyline.remove()
            }
            mMap.setPadding(0, 0, 0, 0)
        }
    }


    override fun onMarkerClick(marker: Marker?): Boolean {
        fab_confirm.hide()
        fab_add.show()
        rotate = true
        toggleFabMode(fab_add)
        markerId = marker?.tag.toString()
        val markerListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val route =
                    dataSnapshot.child("routes").child(markerId).getValue(Route::class.java)!!
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
                Toast.makeText(
                    this@MapsActivity,
                    "Wystąpił błąd podczas pobierania trasy",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        QApp.fData.reference.addListenerForSingleValueEvent(markerListener)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        mMap.setPadding(0, 0, 0, 200)
        loadImages()
        return true
    }

    private fun loadImages() {
        imageSlider.sliderAdapter = SliderAdapter(images)
        val imagesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                images = arrayListOf()
                dataSnapshot.child("routes").child(markerId).child("images")
                    .children.forEach { images.add(it.value!!) }
                imageSlider.sliderAdapter = SliderAdapter(images)
                imageSlider.sliderAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(dataSnapshot: DatabaseError) {
                Toast.makeText(
                    this@MapsActivity,
                    "Wystąpił błąd podczas pobierania zdjęć",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        QApp.fData.reference.addValueEventListener(imagesListener)
    }

    private fun toggleFabMode(v: View) {
        val fromGalleryLayout: View = findViewById(R.id.lyt_mic)
        val fromCameraLayout: View = findViewById(R.id.lyt_call)

        rotate = ViewAnimation.rotateFab(v, !rotate)
        if (rotate) {
            ViewAnimation.showIn(fromGalleryLayout)
            ViewAnimation.showIn(fromCameraLayout)
        } else {
            ViewAnimation.showOut(fromGalleryLayout)
            ViewAnimation.showOut(fromCameraLayout)
        }
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

    private fun enableLocalization() {
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
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
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.title = getString(R.string.app_name)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_basic, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            QApp.fAuth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun initNavigationMenu() {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.inflateHeaderView(R.layout.navigation_drawer)
        val header: View = navigationView.getHeaderView(0)
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle: ActionBarDrawerToggle =
            object : ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            ) {
                override fun onDrawerStateChanged(newState: Int) {
                    super.onDrawerStateChanged(newState)
                    if (newState == DrawerLayout.STATE_SETTLING) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                }
            }
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener {
            true
        }

        val nameListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child(QApp.fAuth.currentUser?.uid!!).child("name").value
                val surname = dataSnapshot.child(QApp.fAuth.currentUser?.uid!!).child("surname").value
                header.navigation_user_name.text = name.toString()+ " "+surname.toString()
            }

        }
        QApp.fData.reference.child("users").addListenerForSingleValueEvent(nameListener)
        val seekBarSearchRadius: CrystalSeekbar = header.seekbar_search_radius
        seekBarSearchRadius.setOnSeekbarChangeListener { minValue ->
            header.search_radius.text = minValue.toString() + "km"
        }

        val seekBarSearchRouteLength: CrystalRangeSeekbar = header.seekbar_search_route_length
        seekBarSearchRouteLength.setOnRangeSeekbarChangeListener { minValue, maxValue ->
            header.search_route_length.text =
                minValue.toString() + " - " + maxValue.toString() + "km"
        }

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
                            drawMarker(R.drawable.ic_marker_cyclist_road).position(
                                LatLng(
                                    item.child("origin").child("latitude").value.toString().toDouble(),
                                    item.child("origin").child("longitude").value.toString().toDouble()
                                )
                            )
                        )
                            .tag = item.key
                        "city" -> mMap.addMarker(
                            drawMarker(R.drawable.ic_marker_cyclist_city).position(
                                LatLng(
                                    item.child("origin").child("latitude").value.toString().toDouble(),
                                    item.child("origin").child("longitude").value.toString().toDouble()
                                )
                            )
                        )
                            .tag = item.key
                        "mountain" -> mMap.addMarker(
                            drawMarker(R.drawable.ic_marker_cyclist_mountain).position(
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
        header.profile_header.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun initBottomSheet() { // get the bottom sheet view
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(p0: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_HIDDEN
                    BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_COLLAPSED
                    BottomSheetBehavior.STATE_DRAGGING -> bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_DRAGGING
                    BottomSheetBehavior.STATE_SETTLING -> bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_SETTLING
                    BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_EXPANDED
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_HALF_EXPANDED
                }
            }
        })

        imageSlider.setIndicatorAnimation(IndicatorAnimations.DROP)
        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)

        ViewAnimation.initShowOut(findViewById(R.id.lyt_mic))
        ViewAnimation.initShowOut(findViewById(R.id.lyt_call))

        fab_add.setOnClickListener { view -> toggleFabMode(view) }
        fab_add_with_camera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
        fab_add_from_gallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Wybierz zdjęcie"),
                REQUEST_PICK_IMAGE
            )

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            toggleFabMode(fab_add)
            fab_add.hide()
            fab_confirm.show()
            images.add(data?.data!!)
            imageSlider.sliderAdapter = SliderAdapter(images)
            imageSlider.sliderAdapter.notifyDataSetChanged()
            imageSlider.currentPagePosition = imageSlider.sliderAdapter.count - 1
            fab_confirm.setOnClickListener {
                fab_confirm.hide()
                progressBar.visibility = View.VISIBLE
                DatabaseUtils.addImageToStorage(markerId, data.data.toString())
                    .addOnSuccessListener {
                        addPhotoProcess(it)
                    }
            }

        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            toggleFabMode(fab_add)
            fab_add.hide()
            fab_confirm.show()
            images.add(data?.extras?.get("data")!!)
            imageSlider.sliderAdapter = SliderAdapter(images)
            imageSlider.sliderAdapter.notifyDataSetChanged()
            imageSlider.currentPagePosition = imageSlider.sliderAdapter.count - 1
            fab_confirm.setOnClickListener {
                fab_confirm.hide()
                progressBar.visibility = View.VISIBLE
                DatabaseUtils.addImageToStorage(markerId, data.extras!!.get("data") as Bitmap)
                    .addOnSuccessListener {
                        addPhotoProcess(it)
                    }
            }
        }
    }

    private fun addPhotoProcess(uploadTask: UploadTask.TaskSnapshot) {
        uploadTask.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
            QApp.fData.reference.child("routes").child(markerId).child("images").push()
                .setValue(uri.toString()).addOnSuccessListener {
                    Log.d("uri", uri.toString())
                    Toast.makeText(this, "Dodano nowe zdjęcie", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    fab_add.show()
                }
        }

    }

    //endregion

    private fun getUrl(trasa: Route): String? { // Origin of route
        val strOrigin = "origin=" + trasa.origin.latitude + "," + trasa.origin.longitude
        // Destination of route
        val strDest =
            "destination=" + trasa.destination.latitude + "," + trasa.destination.longitude
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
        val final =
            "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(
                R.string.google_maps_key
            )
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

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawers()
        }else if(::currentPolyline.isInitialized && currentPolyline.isVisible){
            currentPolyline.isVisible = false
            currentPolyline.remove()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mMap.setPadding(0, 0, 0, 0)
        }
        else super.onBackPressed()
    }

    //endregion
}
