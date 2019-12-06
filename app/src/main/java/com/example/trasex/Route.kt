package com.example.trasex

import com.google.android.gms.maps.model.LatLng

data class Route(val origin: LatLng, val waypoints: List<LatLng>, val destination: LatLng, val mode: String = "bicycling")