package com.example.caltrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.caltrack.network.LoginRequest
import com.example.caltrack.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun attemptLogin() {
        errorMessage = if (username == PLACEHOLDER_USER && password == PLACEHOLDER_PASS) {
            null
        } else {
            "Incorrect username or password"
        }
        if (errorMessage == null) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LoginColors.Surface, RoundedCornerShape(20.dp))
                .border(1.dp, LoginColors.Border, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CalTrack",
                color = LoginColors.Primary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Log in to keep tracking",
                color = LoginColors.TextMuted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    errorMessage = null
                },
                label = { Text("Username", color = LoginColors.TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LoginColors.Background,
                    unfocusedContainerColor = LoginColors.Background,
                    focusedBorderColor = LoginColors.Primary,
                    unfocusedBorderColor = LoginColors.Border,
                    focusedTextColor = LoginColors.TextPrimary,
                    unfocusedTextColor = LoginColors.TextPrimary,
                    cursorColor = LoginColors.Primary
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password", color = LoginColors.TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            color = LoginColors.TextMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LoginColors.Background,
                    unfocusedContainerColor = LoginColors.Background,
                    focusedBorderColor = LoginColors.Primary,
                    unfocusedBorderColor = LoginColors.Border,
                    focusedTextColor = LoginColors.TextPrimary,
                    unfocusedTextColor = LoginColors.TextPrimary,
                    cursorColor = LoginColors.Primary
                )
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = LoginColors.Error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { attemptLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoginColors.Primary,
                    contentColor = LoginColors.Surface
                )
            ) {
                Text("Log In", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login")

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    message = "Please fill in all fields"
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    message = ""
                    try {
                        val response = RetrofitClient.authApi.login(
                            LoginRequest(username = username, password = password)
                        )
                        if (response.isSuccessful) {
                            message = "Login successful"
                            onLoginSuccess()
                        } else {
                            message = when (response.code()) {
                                401 -> "Invalid username or password"
                                400 -> "Please fill in all fields"
                                else -> "Login failed. Please try again"
                            }
                        }
                    } catch (e: Exception) {
                        message = "Could not connect to server. Check your connection"
                        android.util.Log.e("LoginDebug", "Error: ${e.javaClass.name}: ${e.message}")
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Logging in..." else "Login")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (message == "Login successful") Color.Green else Color.Red,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}