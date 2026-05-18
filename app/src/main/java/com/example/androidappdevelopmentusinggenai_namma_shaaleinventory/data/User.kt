package com.example.androidappdevelopmentusinggenai_namma_shaaleinventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val fullName: String,
    val phone: String,
    val passwordHash: String,
    val securityQuestion: String,
    val securityAnswer: String
)
