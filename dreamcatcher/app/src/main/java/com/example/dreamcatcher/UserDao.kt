package com.example.dreamcatcher

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users ORDER BY userId ASC")
    fun getAllUsers(): LiveData<List<User>>


}