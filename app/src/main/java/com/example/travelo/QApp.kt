package com.example.travelo

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.example.travelo.models.User
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class QApp : Application() {

    override fun onCreate() {
        super.onCreate()

        res = resources
        ctx = applicationContext

        fData = FirebaseDatabase.getInstance()
        fAuth = FirebaseAuth.getInstance()
        fStorage = FirebaseStorage.getInstance()
        Places.initialize(applicationContext, res.getString(R.string.google_maps_key))
        val placesClient = Places.createClient(this)
    }

    companion object {
        lateinit var ctx: Context
        lateinit var res: Resources

        lateinit var fData: FirebaseDatabase
        lateinit var fAuth: FirebaseAuth
        lateinit var fStorage: FirebaseStorage

        var currentUser: User? = null
    }
}