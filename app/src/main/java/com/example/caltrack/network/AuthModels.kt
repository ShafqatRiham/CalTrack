package com.example.caltrack.network

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class UserResponse(
    val user_id: Int,
    val username: String,
    val email: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class MessageResponse(
    val message: String
)