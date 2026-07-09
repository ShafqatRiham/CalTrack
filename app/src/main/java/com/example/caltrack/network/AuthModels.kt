package com.example.caltrack.network

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val weight: Double? = null,
    val height: Double? = null,
    val weight_unit: String = "kg",
    val height_unit: String = "cm",
    val gender: String? = null
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

data class MealLogItem(
    val food_id: Int,
    val quantity: Double,
    val unit: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

data class MealLogRequest(
    val user_id: Int,
    val meal_type: String,
    val log_date: String,
    val log_time: String?,
    val items: List<MealLogItem>
)

data class MealLogResponse(
    val message: String,
    val log_id: Int
)

data class SaveFoodResponse(
    val food_id: Int,
    val message: String
)

data class SaveFoodRequest(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val external_api_id: String
)

data class ActivityLogRequest(
    val user_id: Int,
    val steps: Int,
    val log_date: String
)

data class ActivityLogResponse(
    val message: String,
    val steps: Int,
    val calories_burned: Double,
    val note: String
)