package com.example.dreamcatcher.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dreamcatcher.AuthManager
import com.example.dreamcatcher.R
import com.example.dreamcatcher.ui.theme.DreamcatcherTheme
import com.google.firebase.auth.FirebaseAuth


sealed class SettingAction {
    object NavigateToAccount : SettingAction()
    object ToggleTheme : SettingAction()
    object OpenNotificationSettings : SettingAction()
    object OpenTestingDialog : SettingAction()
    object SignOut : SettingAction() // 新增 Sign Out 动作
}


@Composable
fun SettingScreen(navController: NavController) {
    val settings = listOf(
        Triple("Account", R.drawable.account, SettingAction.NavigateToAccount),
        Triple("Display", R.drawable.display, SettingAction.ToggleTheme),
        Triple("Notification", R.drawable.notification, SettingAction.OpenNotificationSettings),
        Triple("Testing", R.drawable.testing, SettingAction.OpenTestingDialog),
        Triple("Sign Out", R.drawable.back, SettingAction.SignOut) // 新增 Sign Out
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(settings) { setting ->
            val (title, iconRes, action) = setting

            SettingRow(
                iconRes = iconRes,
                label = title,
                onClick = {
                    when (action) {
                        is SettingAction.NavigateToAccount -> navController.navigate("account_screen")
                        is SettingAction.ToggleTheme -> navController.navigate("display_settings")
                        is SettingAction.OpenNotificationSettings -> {/* Open settings */}
                        is SettingAction.OpenTestingDialog -> navController.navigate("database_testing")
                        is SettingAction.SignOut -> {
                            FirebaseAuth.getInstance().signOut() // 执行退出登录
                            navController.navigate("login") {
                                AuthManager.isLoggedIn.value = false
                                popUpTo(0) { inclusive = true } // 清除导航历史，防止回退
                            }
                        }
                    }
                }
            )
        }
    }
}



@Composable
fun SettingRow(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    val mockNavController = rememberNavController()
    DreamcatcherTheme {
        SettingScreen(navController = mockNavController)
    }
}
