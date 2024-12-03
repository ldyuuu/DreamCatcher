package com.example.dreamcatcher
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(
    tableName = "dreams",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class Dream(
    @PrimaryKey(autoGenerate = true) val dreamId: Int = 0,
    val userId: Int,
    val title: String?,
    val content: String,
    val mood: String,
    val createdAt: Long = System.currentTimeMillis(),
    val aiImageURL: String?
)