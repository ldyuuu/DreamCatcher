package com.example.dreamcatcher

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamcatcher.models.TherapyCenter
import com.example.dreamcatcher.network.RetrofitInstance
import kotlinx.coroutines.launch

open class MainViewModel(application: Application) : ViewModel() {
    private val repository: Repository
    open val allUsers: LiveData<List<User>>
    val allDreams: LiveData<List<Dream>>
    val searchUserResults: LiveData<User?>
    val searchDreamResults: LiveData<List<Dream>>
    val userAddress = MutableLiveData<String?>()

    private val _therapyCenters = MutableLiveData<List<TherapyCenter>>()
    open val therapyCenters: MutableLiveData<List<TherapyCenter>> get() = _therapyCenters

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

    fun getDreamsByUserAndDate(userId: Int, date: String): LiveData<List<Dream>> {
        return repository.getDreamsByUserAndDate(userId, date)
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


    fun fetchTherapyCenters(email: String, apiKey: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmailSync(email)
            val address = user?.address

            if (address != null) {
                val location = geocodeAddress(address, apiKey)
                if (location != null) {
                    val centers = findNearbyTherapies(location.latitude, location.longitude, apiKey)
                    _therapyCenters.postValue(centers)
                } else {
                    _therapyCenters.postValue(emptyList())
                }
            } else {
                _therapyCenters.postValue(emptyList())
            }
        }
    }


    suspend fun getUserByEmailSync(email: String): User? {
        return repository.getUserByEmailSync(email)
    }


    private suspend fun geocodeAddress(address: String, apiKey: String): Location? {
        val response = RetrofitInstance.geocodingAPI.geocode(address, apiKey)
        val locationData = response.results.firstOrNull()?.geometry?.location

        return if (locationData != null) {
            Location(
                latitude = locationData.lat,
                longitude = locationData.lng
            )
        } else {
            null
        }

    }

    private suspend fun findNearbyTherapies(lat: Double, lng: Double, apiKey: String): List<com.example.dreamcatcher.models.TherapyCenter> {
        return try {
            val location = "$lat,$lng"
            val response = RetrofitInstance.placesAPI.findPlaces(
                location,
                radius = 5000,
                type = "health",
                keyword = "therapist|psychiatrist",
                apiKey
            )
            if (response.status == "OK" && response.results.isNotEmpty()) {
                response.results.map { place ->
                    com.example.dreamcatcher.models.TherapyCenter(
                        name = place.name,
                        address = place.vicinity,
                        latitude = place.geometry.location.lat,
                        longitude = place.geometry.location.lng,
                        photoReference = place.photos?.firstOrNull()?.photoReference
                    )
                }
            } else {
                Log.e("Places", "Failed to find places: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("Places", "Error: ${e.message}")
            emptyList()
        }
    }

    fun fetchUserAddress(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            userAddress.postValue(user?.address)
        }
    }


}


data class Location(val latitude: Double, val longitude: Double)
data class TherapyCenter(val name: String, val address: String, val rating: Float)