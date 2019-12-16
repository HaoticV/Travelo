package com.example.travelo.database

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
            uploadTask.addOnFailureListener { Toast.makeText(context, "Coś poszło nie tak", Toast.LENGTH_SHORT).show() }
                .addOnSuccessListener { Toast.makeText(context, "Dodano nowe zdjęcie", Toast.LENGTH_SHORT).show() }
        }
    }
}