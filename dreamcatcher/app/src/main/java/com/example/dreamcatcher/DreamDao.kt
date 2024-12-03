package com.example.dreamcatcher
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DreamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDream(dream: Dream): Long

    @Query("SELECT * FROM dreams ORDER BY createdAt DESC")
    fun getAllDreams(): LiveData<List<Dream>>

    @Query("SELECT * FROM dreams WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getDreamsByUser(userId: Int): List<Dream>

    @Query("SELECT * FROM dreams WHERE dreamId = :dreamId")
    suspend fun getDreamById(dreamId: Int): Dream?

    @Query("SELECT * FROM dreams WHERE DATE(createdAt / 1000, 'unixepoch') = :date")
    suspend fun getDreamsByDate(date: String): List<Dream>

    @Query("""
    SELECT * FROM dreams 
    WHERE userId = :userId AND DATE(createdAt / 1000, 'unixepoch') = :date
    ORDER BY createdAt DESC
""")
    fun getDreamsByUserAndDate(userId: Int, date: String): LiveData<List<Dream>>

    @Query("""
        SELECT * FROM dreams 
        WHERE DATE(createdAt / 1000, 'unixepoch') BETWEEN :startDate AND :endDate
    """)
    suspend fun getDreamsByDateRange(startDate: String, endDate: String): List<Dream>

    @Delete
    suspend fun deleteDream(dream: Dream)

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: Int): User?

}
