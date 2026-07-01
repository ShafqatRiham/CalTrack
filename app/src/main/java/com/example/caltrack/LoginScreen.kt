package com.example.caltrack

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private object LoginColors {
    val Background = Color(0xFFF7F3EC)
    val Surface = Color(0xFFFFFFFF)
    val Border = Color(0xFFDDD2BE)
    val Primary = Color(0xFF3F6C51)
    val Secondary = Color(0xFFC1502E)
    val TextPrimary = Color(0xFF2B2620)
    val TextMuted = Color(0xFF8A8074)
    val Error = Color(0xFFB3261E)
}

private const val PLACEHOLDER_USER = "admin"
private const val PLACEHOLDER_PASS = "1234"

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
        }
    }
}