package com.example.travelo.models

import java.io.Serializable

data class Tour(
    var name: String = "",
    var host: String = "",
    var date: String = "",
    var dateTime: String = "",
    var routeId: String = "",
    var users: HashMap<String, String> = hashMapOf()
) : Serializable

