package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "assets",
    foreignKeys = [
        ForeignKey(
            entity = Institution::class,
            parentColumns = ["id"],
            childColumns = ["institutionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Asset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val serialNumber: String,
    val category: String,
    val condition: String,
    val note: String? = null,
    val photoUri: String? = null,
    val institutionId: Int = 0, // Link to Institution
    val lastChecked: Long = System.currentTimeMillis()
)
