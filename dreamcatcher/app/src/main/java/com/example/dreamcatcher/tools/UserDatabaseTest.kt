package com.example.dreamcatcher.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dreamcatcher.MainViewModel
import com.example.dreamcatcher.R
import com.example.dreamcatcher.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseTest(navController: NavController, viewModel: MainViewModel) {
    val allUsers by viewModel.allUsers.observeAsState(emptyList())
    val userName = remember { mutableStateOf("") }
    val userEmail = remember { mutableStateOf("") }
    val allDreams by viewModel.allDreams.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("Database Testing") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.back), // Replace with your back icon resource
                        contentDescription = "Back"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userName.value,
            onValueChange = { userName.value = it },
            label = { Text("User Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userEmail.value,
            onValueChange = { userEmail.value = it },
            label = { Text("User Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        LazyColumn {
            items(allUsers) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Name: ${user.displayName}")
                        Text(text = "Email: ${user.email}")
                    }

                    Button(onClick = { viewModel.removeUser(user) }) {
                        Text("Delete")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Display dreams
        Text(
            text = "Dreams",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn {
            items(allDreams) { dream ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                        Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                            Text(text = "Content: ${dream.content.take(50)}...") // Shorten content
                            Text(text = "Mood: ${dream.mood}")
                            Text(text = "URL: ${dream.aiImageURL}")
                        }


                    Button(onClick = { viewModel.removeDream(dream) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}




