package com.example.caltrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }
    var showLeaderboard by remember { mutableStateOf(false) }
    var loggedInUserId by remember { mutableStateOf(1) }

    when {
        !isLoggedIn && !showRegister -> LoginScreen(
            onLoginSuccess = { userId ->
                loggedInUserId = userId
                isLoggedIn = true
            },
            onNavigateToRegister = { showRegister = true }
        )
        showRegister -> RegisterScreen(
            onRegisterSuccess = { showRegister = false },
            onBackToLogin = { showRegister = false }
        )
        showLeaderboard -> LeaderboardScreen(
            currentUserId = loggedInUserId,
            onNavigateToHome = { showLeaderboard = false }
        )
        else -> HomeScreen(
            userId = loggedInUserId,
            onNavigateToLeaderboard = { showLeaderboard = true },
            onLogout = {
                isLoggedIn = false
                showLeaderboard = false
                loggedInUserId = 1
            }
        )
    }
}