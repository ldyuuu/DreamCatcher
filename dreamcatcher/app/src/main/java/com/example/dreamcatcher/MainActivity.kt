package com.example.dreamcatcher

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dreamcatcher.ui.theme.DreamcatcherTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.dreamcatcher.screens.CalendarScreen
import com.example.dreamcatcher.screens.DreamDetailScreen
import com.example.dreamcatcher.screens.HomeScreen
import com.example.dreamcatcher.screens.MapScreen
import com.example.dreamcatcher.screens.SettingScreen
import com.example.dreamcatcher.screens.TodayScreen
import com.example.dreamcatcher.screens.TodayViewModel
import com.example.dreamcatcher.screens.TodayViewModelFactory
import com.example.dreamcatcher.tools.DatabaseTest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DreamcatcherRoomDatabase.getInstance(applicationContext)
        val dreamDao = database.dreamDao()
        val viewModel: MainViewModel
        val todayViewModel: TodayViewModel = ViewModelProvider(
            this,
            TodayViewModelFactory(dreamDao)
        )[TodayViewModel::class.java]
        val factory = MainViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]


        val email = "test@gmail.com"
        val apiKey = BuildConfig.GOOGLE_MAP_API_KEY

        setContent {
            DreamcatcherTheme {
                MainApp(viewModel = viewModel, userId = 1,apiKey = apiKey,email = email)//user Id comes from other page
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel, userId: Int, apiKey:String, email:String) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val todayViewModel: TodayViewModel = viewModel()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = { TopBar(currentRoute = currentRoute) },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("today") { TodayScreen(todayViewModel = todayViewModel) }
            composable("calendar") {
                CalendarScreen(
                    viewModel = viewModel,
                    userId = userId,
                    onDateSelected = { selectedDate ->
                        // 跳转到梦境详情页面，传递选中的日期
                        navController.navigate("dreamDetail/$selectedDate")
                    }
                )
            }
            composable("dreamDetail/{selectedDate}") { backStackEntry ->
                // 获取传递的日期参数
                val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
                DreamDetailScreen(
                    viewModel = viewModel,
                    userId = userId,
                    date = selectedDate,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("map") { MapScreen(email = email, apiKey = apiKey, viewModel = viewModel) }
            composable("settings") { SettingScreen(navController = navController) }
            composable("database_testing") {
                val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current.applicationContext as Application))
                DatabaseTest(navController = navController,viewModel = mainViewModel)
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
        "database_testing" to "Database Testing"
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
fun BottomNavigationBar(
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
                    if (!selected) {
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
                        painter = painterResource(id=iconRes),
                        contentDescription = route,
                        modifier = Modifier.size(if (selected) 30.dp else 24.dp),
                        tint = Color.Unspecified
                    )
                },
                label = if (selected) null else {
                    { Text(text = route.capitalize(), fontSize = 12.sp, fontFamily = BAUSH) }
                },
                selectedContentColor = MaterialTheme.colorScheme.secondary,
                unselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)

            )
        }
    }
}


class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 验证 ViewModel 类型
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application) as T
        }
        // 抛出异常以防止错误类型
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

