package com.example.travelo.add

import android.app.Dialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelo.BaseActivity
import com.example.travelo.QApp
import com.example.travelo.R
import com.example.travelo.auth.SignInActivity
import com.example.travelo.directionHelpers.FetchURL
import com.example.travelo.directionHelpers.TaskLoadedCallback
import com.example.travelo.models.LatLang
import com.example.travelo.models.Route
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_add.*
import java.util.*


class AddRouteActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, TaskLoadedCallback {
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private val AUTOCOMPLETE_REQUEST_CODE = 3
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var recyclerView: RecyclerView
    private lateinit var currentPolyline: Polyline
    private val routePoints = arrayListOf<Place>()
    private val markers = arrayListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_route_activity)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initToolbar()
        initBottomSheet()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.points_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        val adapter = AddRoutePointsAdapter(this, routePoints)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : AddRoutePointsAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: Place?, position: Int) {
                routePoints.removeAt(position)
                markers[position].remove()
                markers.removeAt(position)
                (recyclerView.adapter as AddRoutePointsAdapter).notifyDataSetChanged()
                drawRoute()
            }
        })
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbarAdd)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.title = "Stwórz własną trasę"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_basic, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            QApp.fAuth.signOut()
            QApp.currentUser = null
            startActivity(Intent(this, SignInActivity::class.java))
        }
        if (item.itemId == R.id.action_search) {
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setLocationBias(RectangularBounds.newInstance(LatLngBounds(LatLng(51.066020, 22.340719), LatLng(51.355511, 22.742954))))
                .setCountry("PL")
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initBottomSheet() { // get the bottom sheet view
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_add)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
        imageSave.setOnClickListener { saveRoute() }
    }

    private fun saveRoute() {
        if (routePoints.size >= 2) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before

            dialog.setContentView(R.layout.dialog_add_review)
            dialog.setCancelable(true)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT

            val et_post = dialog.findViewById<View>(R.id.et_post) as EditText
            (dialog.findViewById<View>(R.id.bt_cancel) as AppCompatButton).setOnClickListener { dialog.dismiss() }

            (dialog.findViewById<View>(R.id.bt_submit) as AppCompatButton).setOnClickListener {
                val name = et_post.text.toString().trim { it <= ' ' }
                if (name.isEmpty()) {
                    Toast.makeText(applicationContext, "Podaj nazwę", Toast.LENGTH_SHORT).show()
                } else {
                    dialog.dismiss()
                    Toast.makeText(applicationContext, "Dodałeś trasę: $name", Toast.LENGTH_SHORT).show()
                    addRouteToDatabase(name)
                }
            }
            dialog.show()
            dialog.window!!.attributes = lp
        } else Toast.makeText(applicationContext, "Trasa musi posiadać początek i koniec", Toast.LENGTH_SHORT).show()
    }

    private fun addRouteToDatabase(name: String) {
        val key = QApp.fData.reference.child("routes").push().key!!
        var wayPoints = routePoints.drop(0)
        wayPoints = wayPoints.dropLast(1)
        QApp.fData.reference.child("routes").setValue(
            Route(
                id = key,
                name = name,
                origin = LatLang(routePoints[0].latLng?.latitude!!, routePoints[0].latLng?.latitude!!),
                waypoints = wayPoints.map { place -> LatLang(place.latLng?.latitude!!, routePoints[0].latLng?.latitude!!) },
                destination = LatLang(routePoints.last().latLng?.latitude!!, routePoints.last().latLng?.longitude!!),
                type = "city"
            )
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.setOnMapLongClickListener(this)

        mFusedLocationClient.lastLocation.addOnSuccessListener {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 12.0f))
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

    override fun onMapLongClick(coordinates: LatLng?) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val getAddress = Geocoder(this, Locale("PL"))
        val address = getAddress.getFromLocation(coordinates?.latitude!!, coordinates.longitude, 1)[0].getAddressLine(0)
        addPointToRoute(Place.builder().setLatLng(coordinates).setName(address).build())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                addPointToRoute(place)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.i("Status", status.statusMessage)
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private fun addPointToRoute(place: Place) {
        markers.add(mMap.addMarker(MarkerOptions().position(place.latLng!!).title(place.name)))
        routePoints.add(place)
        recyclerView.adapter?.notifyDataSetChanged()
        animateCamera()
    }


    private fun animateCamera() {
        when (routePoints.size) {
            1 -> mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(routePoints[0].latLng?.latitude!!, routePoints[0].latLng?.longitude!!), 15.0f))
            else -> drawRoute()
        }
    }

    private fun drawRoute() {
        if (routePoints.isNotEmpty()) {
            val url = getUrl(parsePointsIntoRoute())
            FetchURL(this).execute(url)
        }
    }

    private fun parsePointsIntoRoute(): Route {
        val firstAndLast = listOf(routePoints[0], routePoints[routePoints.lastIndex])
        val wayPointsPlaces = routePoints.subtract(firstAndLast).toList()
        val wayPoints = wayPointsPlaces.map { LatLang(it.latLng!!.latitude, it.latLng!!.longitude) }
        return Route(
            origin = LatLang(firstAndLast[0].latLng!!.latitude, firstAndLast[0].latLng!!.longitude),
            destination = LatLang(firstAndLast[1].latLng!!.latitude, firstAndLast[1].latLng!!.longitude),
            waypoints = wayPoints
        )
    }

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

    override fun onTaskDone(polylineOptions: PolylineOptions?) {
        currentPolyline = if (::currentPolyline.isInitialized) {
            currentPolyline.remove()
            mMap.addPolyline(polylineOptions)
        } else {
            mMap.addPolyline(polylineOptions)
        }
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
