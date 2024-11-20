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
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.dreamcatcher.ui.theme.DreamcatcherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    // Navigation setup
    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") { MainMenu(navController) }
        composable("mic_test") { MicTestScreen(navController) }
        composable("db_test") { DatabaseTestScreen(navController) }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DreamcatcherTheme {

    }
}


@Composable
fun MainScreen(viewModel: MainViewModel) {
    val allUsers by viewModel.allUsers.observeAsState(emptyList()) // 观察所有用户
    val userName = remember { mutableStateOf("") }
    val userEmail = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 输入新用户的名字
        OutlinedTextField(
            value = userName.value,
            onValueChange = { userName.value = it },
            label = { Text("User Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 输入新用户的邮箱
        OutlinedTextField(
            value = userEmail.value,
            onValueChange = { userEmail.value = it },
            label = { Text("User Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 添加用户按钮
        Button(
            onClick = {
                if (userName.value.isNotEmpty() && userEmail.value.isNotEmpty()) {
                    val newUser = User(email = userEmail.value, displayName = userName.value
                    , passwordHash = "", preferences = "")
                    viewModel.addUser(newUser)
                    userName.value = ""
                    userEmail.value = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add User")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 显示所有用户
        Text("All Users:", style = MaterialTheme.typography.bodyLarge)
        LazyColumn {
            items(allUsers) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Name: ${user.displayName}")
                        Text(text = "Email: ${user.email}")
                    }

                    // 删除按钮
                    Button(onClick = { viewModel.removeUser(user) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
class MainViewModelFactory(val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(application) as T
    }
}