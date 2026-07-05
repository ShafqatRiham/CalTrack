package com.example.caltrack.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<MessageResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/foods/search")
    suspend fun searchFoods(@Query("query") query: String): Response<FoodSearchResponse>

    @POST("api/meals/log")
    suspend fun logMeal(@Body request: MealLogRequest): Response<MealLogResponse>

    @POST("api/foods/save")
    suspend fun saveFood(@Body request: SaveFoodRequest): Response<SaveFoodResponse>
}