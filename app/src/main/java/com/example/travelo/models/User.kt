package com.example.travelo.models

import java.io.Serializable

data class User(
    val id: String = "",
    var name: String = "",
    var surname: String = "",
    var login: String = "",
    var email: String = "",
    var admin: Boolean = false
): Serializable

