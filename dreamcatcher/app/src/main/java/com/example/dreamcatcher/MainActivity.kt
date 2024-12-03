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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.dreamcatcher.screens.CalendarScreen
import com.example.dreamcatcher.screens.HomeScreen
import com.example.dreamcatcher.screens.NewsScreen
import com.example.dreamcatcher.screens.SettingScreen
import com.example.dreamcatcher.screens.TodayScreen
import com.example.dreamcatcher.screens.TodayViewModel
import com.example.dreamcatcher.screens.TodayViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DreamcatcherRoomDatabase.getInstance(applicationContext)
        val dreamDao = database.dreamDao()

        val todayViewModel: TodayViewModel = ViewModelProvider(
            this,
            TodayViewModelFactory(dreamDao)
        )[TodayViewModel::class.java]

        setContent {
            DreamcatcherTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val todayViewModel: TodayViewModel = viewModel()

    Scaffold(
        topBar = { TopBar(currentRoute = navController.currentBackStackEntry?.destination?.route) },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("today") { TodayScreen(todayViewModel = todayViewModel) }
            composable("calendar") { CalendarScreen() }
            composable("news") { NewsScreen() }
            composable("settings") { SettingScreen() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(currentRoute: String?) {
    val BAUSH = FontFamily(Font(R.font.baush))
    val screenTitles = mapOf(
        "home" to "Home",
        "today" to "Today",
        "calendar" to "Calendar",
        "news" to "News Feed",
        "settings" to "Settings"
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
            "news" to R.drawable.news,
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



class MainViewModelFactory(val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(application) as T
    }
}