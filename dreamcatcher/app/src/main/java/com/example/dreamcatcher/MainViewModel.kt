package com.example.dreamcatcher

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.dreamcatcher.models.TherapyCenter
import com.example.dreamcatcher.network.RetrofitInstance
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class MainViewModel(application: Application,private val dataStoreManager: DataStoreManager) : ViewModel() {
    private val repository: Repository
    open val allUsers: LiveData<List<User>>
    val allDreams: LiveData<List<Dream>>
    val searchUserResults: LiveData<User?>
    val searchDreamResults: LiveData<List<Dream>>
    val userAddress = MutableLiveData<String?>()

    private val _settings = MutableLiveData(
        mapOf(
            "Show Today's Dream" to true,
            "Show Log Dream" to true,
            "Show Dream Calendar" to true,
            "Show Nearby Therapists" to true,
            "Show Trend Analysis" to true
        )
    )
    val settings: LiveData<Map<String, Boolean>> get() = _settings

    fun updateSetting(key: String, value: Boolean) {
        val updatedSettings = _settings.value?.toMutableMap()?.apply {
            put(key, value)
        } ?: mapOf(key to value)
        _settings.value = updatedSettings

        // Save updated settings to DataStore
        viewModelScope.launch {
            dataStoreManager.saveHomeScreenSettings(updatedSettings)
        }
    }

    private fun saveSettings(settings: Map<String, Boolean>) {
        viewModelScope.launch {
            dataStoreManager.saveHomeScreenSettings(settings)
        }
    }

    private val _dreams = MutableStateFlow<List<Dream>>(emptyList())
    val dreams: StateFlow<List<Dream>> get() = _dreams

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val firebaseUser: StateFlow<FirebaseUser?> get() = _firebaseUser

    private val _isDarkModeEnabled = MutableStateFlow(false)
    val isDarkModeEnabled: StateFlow<Boolean> get() = _isDarkModeEnabled

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> get() = _loggedInUser

    fun updateDreams(dreams: List<Dream>) {
        _dreams.value = dreams
    }

    fun updateFirebaseUser(user: FirebaseUser?) {
        _firebaseUser.value = user
    }
    private val _therapyCenters = MutableLiveData<List<TherapyCenter>>()
    open val therapyCenters: MutableLiveData<List<TherapyCenter>> get() = _therapyCenters

    init {
        val database = DreamcatcherRoomDatabase.getInstance(application)
        val userDao = database.userDao()
        val dreamDao = database.dreamDao()

        viewModelScope.launch {
            dataStoreManager.isDarkModeEnabled.collect{
                isEnabled -> _isDarkModeEnabled.value = isEnabled
            }
        }

        repository = Repository(userDao, dreamDao)

        allUsers = repository.allUsers
        allDreams = repository.allDreams
        searchUserResults = repository.searchUserResults
        searchDreamResults = repository.searchDreamResults

        viewModelScope.launch {
            allDreams.asFlow().collect { dreamList ->
                Log.d("MainViewModel", "Updated Dreams: $dreamList")
                _dreams.value = dreamList
            }
        }

        viewModelScope.launch {
            dataStoreManager.homeScreenSettings.collect { savedSettings ->
                _settings.value = savedSettings
            }
        }
    }




    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setDarkModeEnabled(enabled)
        }
    }

    fun syncFirebaseUserWithLocalData(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            val email = firebaseUser.email ?: return@launch
            val localUser = repository.getUserByEmailSync(email)

            val user = if (localUser == null) {
                val newUser = User(
                    displayName = firebaseUser.displayName ?: "Unknown",
                    email = email,
                    createdAt = System.currentTimeMillis(),
                    preferences = null,
                    address = null
                )
                repository.insertUser(newUser)
                newUser
                //repository.getUserByEmailSync(email) // 获取插入后的用户
            } else {
                localUser
            }
            _loggedInUser.value = user // 更新当前登录用户
        }
    }

    fun updateLoggedInUser(user: User?) {
        _loggedInUser.value = user
    }

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

    fun saveUserToDatabase(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
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

    fun getDreamsForLoggedInUser(): LiveData<List<Dream>> {
        val userId = loggedInUser.value?.userId ?: return MutableLiveData(emptyList())
        return repository.getDreamsByUserId(userId)
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


    fun fetchUserLocation(address: String, apiKey: String, onResult: (Location?) -> Unit) {
        viewModelScope.launch {
            val location = geocodeAddress(address, apiKey)
            onResult(location)
        }
    }

}


data class Location(val latitude: Double, val longitude: Double)
data class TherapyCenter(val name: String, val address: String, val rating: Float)