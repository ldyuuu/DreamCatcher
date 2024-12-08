package com.example.dreamcatcher

import android.app.Application
import android.provider.ContactsContract.Data
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun DatabaseTestScreen(navController: NavHostController) {
    val owner = LocalViewModelStoreOwner.current
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)

    owner?.let {
        val viewModel: MainViewModel = viewModel(
            it,
            "MainViewModel",
            MainViewModelFactory(
                LocalContext.current.applicationContext as Application,
                dataStoreManager = dataStoreManager
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            DatabaseUserScreen(viewModel,navController)

        }
    }
}


@Composable
fun DatabaseUserScreen(viewModel: MainViewModel,navController: NavHostController) {
    val allUsers by viewModel.allUsers.observeAsState(emptyList())
    val userName = remember { mutableStateOf("") }
    val userEmail = remember { mutableStateOf("") }
    val userAddress = remember { mutableStateOf("")}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // User input for name
        OutlinedTextField(
            value = userName.value,
            onValueChange = { userName.value = it },
            label = { Text("User Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // User input for email
        OutlinedTextField(
            value = userEmail.value,
            onValueChange = { userEmail.value = it },
            label = { Text("User Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userAddress.value,
            onValueChange = { userAddress.value = it },
            label = { Text("User Address") },
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
                        //passwordHash = "",
                        preferences = "",
                        address = userAddress.value

                    )
                    viewModel.addUser(newUser)
                    userName.value = ""
                    userEmail.value = ""
                    userAddress.value = ""

                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add User")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display all users
        Text("All Users:", style = MaterialTheme.typography.bodyLarge)

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn {
                items(allUsers) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column {
                            Text(text = "Name: ${user.displayName}")
                            Text(text = "Email: ${user.email}")
                            Text(text = "Address: ${user.address ?: "N/A"}")
                        }

                        Button(onClick = { viewModel.removeUser(user) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("main_menu") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Back to Main Menu", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
