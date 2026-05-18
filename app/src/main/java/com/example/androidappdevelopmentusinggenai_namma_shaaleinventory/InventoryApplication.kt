package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory

import android.app.Application
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data.AppDatabase
import com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data.AssetRepository

class InventoryApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AssetRepository(database.assetDao(), database.userDao()) }
}
