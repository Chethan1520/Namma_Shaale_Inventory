package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "institutions")
data class Institution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val location: String? = null,
    val userEmail: String = "", // Link to User
    val createdAt: Long = System.currentTimeMillis()
)
