package com.example.travelo.database

import android.graphics.Bitmap
import android.net.Uri
import com.example.travelo.QApp
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.*

class DatabaseUtils {
    companion object {
        fun addImageToStorage(markerId: String, uri: String): UploadTask {
            return QApp.fStorage.reference.child("image/" + markerId + "/" + UUID.randomUUID().toString())
                .putFile(Uri.parse(uri))

        }

        fun addImageToStorage(markerId: String, bitmap: Bitmap): UploadTask {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            return QApp.fStorage.reference.child("image/" + markerId + "/" + UUID.randomUUID().toString())
                .putBytes(data)

        }
    }
}