package com.example.dreamcatcher.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import com.example.dreamcatcher.R


@Composable
fun DisplaySettingsScreen(
    isDarkModeEnabled: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onCustomizeHomePage: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(onClick = { onBack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Display Settings",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dark Mode Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Enable Dark Mode",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isDarkModeEnabled,
                onCheckedChange = {onDarkModeToggle(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onBackground,
                    checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f)
                )

            )
        }

        Log.d("MainApp", "Dark mode is enabled: $isDarkModeEnabled")

        // Customize Home Page Button
        Button(
            onClick = { onCustomizeHomePage() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Customize Home Page")
        }

    }
}

//
//@Preview(showBackground = true)
//@Composable
//fun DisplaySettingsScreenPreview() {
//    DisplaySettingsScreen(
//        isDarkModeEnabled = false,
//        onDarkModeToggle = {},
//        onCustomizeHomePage = {},
//        onBack = {}
//    )
//}
