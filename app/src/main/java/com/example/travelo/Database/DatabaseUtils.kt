package com.example.travelo.Database

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.travelo.QApp
import com.example.travelo.R
import java.util.*

class DatabaseUtils {
    companion object {
        fun addImageToStorage(context: Context, markerId: String) {
            val uploadTask =
                QApp.fStorage.reference.child("image/" + markerId + "/" + UUID.randomUUID().toString())
                    .putFile(Uri.parse("android.resource://com.example.trasex/" + R.drawable.header_background_green))
            uploadTask.addOnFailureListener { Toast.makeText(context, "Nie udało się", Toast.LENGTH_SHORT).show() }
                .addOnSuccessListener { Toast.makeText(context, "Udało się", Toast.LENGTH_SHORT).show() }
        }
    }
}