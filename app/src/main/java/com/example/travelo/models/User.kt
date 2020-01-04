package com.example.travelo.models

import com.example.travelo.R
import java.io.Serializable

data class User(
    var name: String = "",
    var surname: String = "",
    var displayName: String ="",
    var email: String = "",
    var image: String = "android.resource://com.example.travelo/" + R.drawable.icon_user_default,
    var admin: Boolean = false
): Serializable

