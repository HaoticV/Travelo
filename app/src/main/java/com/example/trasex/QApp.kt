package com.example.trasex

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

        fUser = fAuth.currentUser
    }

    companion object {
        lateinit var ctx: Context
        lateinit var res: Resources

        lateinit var fData: FirebaseDatabase
        lateinit var fAuth: FirebaseAuth
        lateinit var fStorage: FirebaseStorage

        var fUser: FirebaseUser? = null
    }
}