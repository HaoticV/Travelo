package com.example.travelo.models

import java.io.Serializable

data class User(
    var name: String = "",
    var surname: String = "",
    var displayName: String ="",
    var email: String = "",
    var image: String = "https://firebasestorage.googleapis.com/v0/b/praca-inzynierska-a3c28.appspot.com/o/icon_user_default.png?alt=media&token=0a758c8d-bd45-4b75-b995-1d837bfbea1d",
    var ownRoutes: HashMap<String, String> = hashMapOf(),
    var likedRoutes: HashMap<String, String> = hashMapOf(),
    var admin: Boolean = false
): Serializable

