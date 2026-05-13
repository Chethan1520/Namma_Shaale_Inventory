package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_checks",
    foreignKeys = [
        ForeignKey(
            entity = Asset::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HealthCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetId: Int,
    val condition: String,
    val note: String?,
    val timestamp: Long = System.currentTimeMillis()
)
