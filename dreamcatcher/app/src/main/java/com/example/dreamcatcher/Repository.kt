package com.example.dreamcatcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class Repository(private val userDao: UserDao, private val dreamDao: DreamDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    val allUsers: LiveData<List<User>> = userDao.getAllUsers()
    val searchUserResults = MutableLiveData<User?>()

    val allDreams: LiveData<List<Dream>> = dreamDao.getAllDreams()
    val searchDreamResults = MutableLiveData<List<Dream>>()

    fun insertUser(user: User) {
        coroutineScope.launch(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    fun deleteUser(user: User) {
        coroutineScope.launch(Dispatchers.IO) {
            userDao.deleteUser(user)
        }
    }

    fun findUserByEmail(email: String) {
        coroutineScope.launch(Dispatchers.Main) {
            searchUserResults.value = asyncFindUser(email).await()
        }
    }

    private fun asyncFindUser(email: String) = coroutineScope.async(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    fun insertDream(dream: Dream) {
        coroutineScope.launch(Dispatchers.IO) {
            dreamDao.insertDream(dream)
        }
    }

    fun deleteDream(dream: Dream) {
        coroutineScope.launch(Dispatchers.IO) {
            dreamDao.deleteDream(dream)
        }
    }

    fun findDreamsByDate(date: String) {
        coroutineScope.launch(Dispatchers.Main) {
            searchDreamResults.value = asyncFindDreamsByDate(date).await() ?: emptyList()
        }
    }

    private fun asyncFindDreamsByDate(date: String) = coroutineScope.async(Dispatchers.IO) {
        dreamDao.getDreamsByDate(date)
    }

}
