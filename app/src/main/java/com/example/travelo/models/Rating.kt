package com.example.travelo.models

import java.io.Serializable

data class Rating(
    var userid: String = "",
    var text: String = "",
    var rating: Double = 5.0,
    var username: String = "",
    var userimage: String = ""
) : Serializable