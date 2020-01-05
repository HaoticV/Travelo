package com.example.travelo.models

import com.example.travelo.R
import java.io.Serializable

data class Tour(
    var name: String = "",
    var routeId: String = "",
    var users: HashMap<String, String> = hashMapOf()
) : Serializable

