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

// Food search response models
data class FoodSearchResult(
    val external_api_id: String?,
    val name: String,
    val brand: String?,
    val calories: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?,
    val fiber: Double?,
    val sugar: Double?,
    val sodium: Double?,
    val serving_size: String?,
    val image_url: String?
)

data class FoodSearchResponse(
    val results: List<FoodSearchResult>
)