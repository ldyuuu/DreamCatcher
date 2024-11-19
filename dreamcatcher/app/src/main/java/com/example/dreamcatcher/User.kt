package com.example.dreamcatcher
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val email: String,
    val displayName: String?,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val preferences: String?,
    val address: String? = null
)