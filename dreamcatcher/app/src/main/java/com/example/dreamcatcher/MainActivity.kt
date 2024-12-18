package com.example.dreamcatcher

import LoginScreen
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dreamcatcher.ui.theme.DreamcatcherTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.dreamcatcher.screens.AccountScreen
import com.example.dreamcatcher.screens.CalendarScreen
import com.example.dreamcatcher.screens.DisplaySettingsScreen
import com.example.dreamcatcher.screens.DreamDetailScreen
import com.example.dreamcatcher.screens.HomeScreen
import com.example.dreamcatcher.screens.MapScreen
import com.example.dreamcatcher.screens.NotificationSettingScreen

import com.example.dreamcatcher.screens.SettingScreen
import com.example.dreamcatcher.screens.TodayScreen
import com.example.dreamcatcher.screens.TodayViewModel
import com.example.dreamcatcher.screens.TodayViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch


object AuthManager {
    val isLoggedIn = mutableStateOf(false)
}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: MainViewModel
    private val CHANNEL_ID = "daily_reminder"
    private val NOTIFICATION_ID = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        FirebaseApp.initializeApp(this)

        val database = DreamcatcherRoomDatabase.getInstance(applicationContext)// 允许清除旧数据
        val dreamDao = database.dreamDao()
        val factory = MainViewModelFactory(application, DataStoreManager(applicationContext))
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        checkNotificationPermission()
        val todayViewModel: TodayViewModel = ViewModelProvider(
            this,
            TodayViewModelFactory(dreamDao)
        )[TodayViewModel::class.java]
        lifecycleScope.launch {
            viewModel.loginState.collect { (isLoggedIn, userId) ->
                setContent {

                    val navController = rememberNavController()
                    DreamcatcherTheme {
                        MainApp(
                            viewModel = viewModel,
                            todayViewModel=todayViewModel,
                            navController = navController,
                            startDestination = if (isLoggedIn) "home" else "login"
                        )
                    }
                }
            }
        }

    }


    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission is required.", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!) {
                    // 登录成功后调用回调
                    AuthManager.isLoggedIn.value = true
                }

            } catch (e: ApiException) {
                // Handle sign-in error
                Log.w("GoogleSignIn", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, onSuccess: () -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.w("GoogleSignIn", "signInWithCredential:success", task.exception)
                    val currentUser = auth.currentUser
                    viewModel.updateFirebaseUser(currentUser)
                    currentUser?.let { user ->
                        viewModel.syncFirebaseUserWithLocalData(user)
                        val localUser = viewModel.loggedInUser.value
                        localUser?.let { user ->
                            viewModel.setLoginState(isLoggedIn = true, userId = user.userId.toString())
                        }
                    }
                    onSuccess()
                } else {
                    // 登录失败
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                }
            }
    }


    companion object {
        const val RC_SIGN_IN = 9001
    }

}


@Composable
fun MainApp(
    viewModel: MainViewModel,
    todayViewModel: TodayViewModel,
    navController: NavHostController,
    startDestination: String,
) {

    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val isLoggedIn by AuthManager.isLoggedIn
    val isDarkModeEnabled by viewModel.isDarkModeEnabled.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
    LaunchedEffect(loggedInUser) {
        if (loggedInUser == null) {

        }
    }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    MaterialTheme(colorScheme = if (isDarkModeEnabled) darkColorScheme() else lightColorScheme()) {

        Scaffold(
            topBar = {
                if (currentRoute != "login") {
                    TopBar(currentRoute = currentRoute)
                }
            },
            bottomBar = {
                if (currentRoute != "login") {
                    BottomBar(navController = navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            AuthManager.isLoggedIn.value = true
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }, viewModel = viewModel
                    )
                }
                composable("home") {
                    val settings = viewModel.settings.observeAsState(
                        mapOf(
                            "Show Today's Dream" to true,
                            "Show Log Dream" to true,
                            "Show Dream Calendar" to true,
                            "Show Nearby Therapists" to true,
                            "Show Trend Analysis" to true
                        )
                    ).value
                    HomeScreen(viewModel = viewModel,navController = navController, settings = settings)
                }
                composable("today") {
                    TodayScreen(todayViewModel = todayViewModel, mainViewModel = viewModel)
                }
                composable("calendar") {
                    loggedInUser?.let { user ->
                        CalendarScreen(
                            viewModel = viewModel,
                            userId = user.userId,
                            onDateSelected = { selectedDate ->
                                navController.navigate("dreamDetail/$selectedDate")
                            }
                        )
                    }
                }
                composable("dreamDetail/{selectedDate}") { backStackEntry ->
                    val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
                    loggedInUser?.let { user ->
                        DreamDetailScreen(
                            viewModel = viewModel,
                            userId = user.userId,
                            date = selectedDate,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable("map") {
                    MapScreen(
                        apiKey = BuildConfig.GOOGLE_MAP_API_KEY,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
                composable("settings") {
                    SettingScreen(navController = navController,viewModel=viewModel)
                }

//                composable("database_testing") {
//                    val context = LocalContext.current.applicationContext as Application
//                    val dataStoreManager = remember { DataStoreManager(context) }
//                    val mainViewModel: MainViewModel =
//                        viewModel(factory = MainViewModelFactory(context, dataStoreManager))
//
//                    DatabaseTest(navController = navController, viewModel = mainViewModel)
//                }

                composable("display_settings") {
                    DisplaySettingsScreen(
                        isDarkModeEnabled = isDarkModeEnabled,
                        onDarkModeToggle = { viewModel.setDarkModeEnabled(it) },
                        settings = viewModel.settings.observeAsState(emptyMap()).value,
                        onToggleChange = { key, value -> viewModel.updateSetting(key, value) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("notification") {
                    NotificationSettingScreen(
                        viewModel=viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }


                composable("account_screen") {
                    AccountScreen(
                        viewModel = viewModel,
                        onBack={  navController.popBackStack() }
                        //onLogout = { currentScreen = "Login" }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DreamcatcherTheme {

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(currentRoute: String?) {
    val BAUSH = FontFamily(Font(R.font.baush))
    val screenTitles = mapOf(
        "home" to "Home",
        "today" to "Today",
        "calendar" to "Calendar",
        "map" to "Map",
        "settings" to "Settings",
        "dreamDetail/{selectedDate}" to "Dream Detail",
//        "database_testing" to "Database Testing",
        "account_screen" to "Account",
        "display_settings" to "Display",
        "notification" to "Notification"
    )
    val screenTitle = screenTitles[currentRoute] ?: "Home"

    TopAppBar(
        title = {
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = BAUSH,
                fontSize = 36.sp
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}


@Composable
fun BottomBar(
    navController: NavHostController
) {
    val BAUSH = FontFamily(Font(R.font.baush))
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val screens = listOf(
            "home" to R.drawable.home,
            "today" to R.drawable.today,
            "calendar" to R.drawable.calendar,
            "map" to R.drawable.map,
            "settings" to R.drawable.setting
        )
        screens.forEach { (route, iconRes) ->
            val selected = currentRoute == route
            BottomNavigationItem(
                selected = selected,
                onClick = {
                    if (route == "home") {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true // remove from back stack
                            }
                            launchSingleTop = true // prevent duplicate screen
                            restoreState = false
                        }
//                    } else if (route == "settings") {
//                        navController.navigate("settings") {
//                            popUpTo("settings") {
//                                inclusive = true
//                            }
//                            launchSingleTop = true
//                            restoreState = false
//                        }
                    } else if (!selected) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = route,
                        modifier = Modifier.size(if (selected) 30.dp else 24.dp),
                        tint = Color.Unspecified
                    )
                },
                label = {
                    BoxWithConstraints {
                        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                        val dynamicFontSize = if (maxWidthPx < 400) 10.sp else 12.sp

                        Text(
                            text = route.replaceFirstChar { it.uppercase() },
                            fontSize = dynamicFontSize,
                            fontFamily = BAUSH,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.secondary,
                unselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        }
    }
}


class MainViewModelFactory(
    private val application: Application,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 验证 ViewModel 类型
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application, dataStoreManager) as T
        }
        // 抛出异常以防止错误类型
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

