package com.example.dreamcatcher.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamcatcher.MainViewModel
import com.example.dreamcatcher.MainViewModelFactory
import com.example.dreamcatcher.User

@Composable
fun SettingScreen(viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current.applicationContext as Application))) {
    val allUsers by viewModel.allUsers.observeAsState(emptyList()) // Observe users
    val userName = remember { mutableStateOf("") }
    val userEmail = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Input new user's name
        OutlinedTextField(
            value = userName.value,
            onValueChange = { userName.value = it },
            label = { Text("User Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input new user's email
        OutlinedTextField(
            value = userEmail.value,
            onValueChange = { userEmail.value = it },
            label = { Text("User Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add user button
        Button(
            onClick = {
                if (userName.value.isNotEmpty() && userEmail.value.isNotEmpty()) {
                    val newUser = User(
                        email = userEmail.value,
                        displayName = userName.value,
                        passwordHash = "",
                        preferences = ""
                    )
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

        // Display all users
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

                    // Delete button
                    Button(onClick = { viewModel.removeUser(user) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
