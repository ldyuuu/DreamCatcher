package com.example.dreamcatcher.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dreamcatcher.MainViewModel
import com.example.dreamcatcher.R

@Composable
fun AccountScreen(viewModel: MainViewModel,
                  onBack: () -> Unit) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val isDarkModeEnabled by viewModel.isDarkModeEnabled.collectAsState()
    val userAddress by viewModel.userAddress.observeAsState()

    var displayName by remember { mutableStateOf(loggedInUser?.displayName ?: "") }
    var email by remember { mutableStateOf(loggedInUser?.email ?: "") }
    var address by remember { mutableStateOf(loggedInUser?.address ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Account Settings", style = androidx.compose.material.MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false // Email remains read-only
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Dark Mode")
            Switch(
                checked = isDarkModeEnabled,
                onCheckedChange = { viewModel.setDarkModeEnabled(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (displayName.isBlank() || address.isBlank()) {
                    errorMessage = "Display name and address cannot be empty"
                    return@Button
                }

                // Update user information in ViewModel
                val updatedUser = loggedInUser?.copy(
                    displayName = displayName,
                    address = address
                )
                if (updatedUser != null) {
                    viewModel.updateLoggedInUser(updatedUser)
                    viewModel.addUser(updatedUser) // Update in database
                }
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }

        Button(
            onClick = onBack
            ,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }


    }
}