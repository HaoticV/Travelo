package com.example.travelo.models

import java.io.Serializable

data class Route(
    var name: String = "",
    var origin: LatLang = LatLang(0.0,0.0),
    var waypoints: List<LatLang> = listOf(),
    var destination: LatLang = LatLang(0.0,0.0),
    var bounds: List<LatLang> = listOf(),
    var mode: String = "bicycling",
    var type: String = "",
    var distance: Long = 0,
    var distanceText: String = "",
    var time: Long = 0,
    var timeText: String = "",
    val images: HashMap<String, String> = hashMapOf()
) : Serializable