package com.example.dreamcatcher

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

open class MainViewModel(application: Application) : ViewModel() {
    private val repository: Repository
    open val allUsers: LiveData<List<User>>
    val allDreams: LiveData<List<Dream>>
    val searchUserResults: LiveData<User?>
    val searchDreamResults: LiveData<List<Dream>>

    init {

        val database = DreamcatcherRoomDatabase.getInstance(application)
        val userDao = database.userDao()
        val dreamDao = database.dreamDao()

        repository = Repository(userDao, dreamDao)

        allUsers = repository.allUsers
        allDreams = repository.allDreams
        searchUserResults = repository.searchUserResults
        searchDreamResults = repository.searchDreamResults
    }

    // 用户操作方法
    open fun addUser(user: User) {
        repository.insertUser(user)
    }

    open fun removeUser(user: User) {
        repository.deleteUser(user)
    }

    fun findUser(email: String) {
        repository.findUserByEmail(email)
    }

    fun addDream(dream: Dream) {
        repository.insertDream(dream)
    }

    fun removeDream(dream: Dream) {
        repository.deleteDream(dream)
    }

    fun findDreamsByDate(date: String) {
        repository.findDreamsByDate(date)
    }
}
