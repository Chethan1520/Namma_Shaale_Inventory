package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtil {
    fun copyUriToInternalStorage(context: Context, uri: Uri, folderName: String): Uri? {
        val contentResolver = context.contentResolver
        val fileName = "IMG_GALLERY_${System.currentTimeMillis()}.jpg"
        val folder = File(context.filesDir, folderName)
        if (!folder.exists()) folder.mkdirs()
        
        val destFile = File(folder, fileName)
        
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
