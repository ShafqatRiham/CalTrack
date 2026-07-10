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

    @POST("api/activity/log")
    suspend fun logActivity(@Body request: ActivityLogRequest): Response<ActivityLogResponse>

    @GET("api/meals")
    suspend fun getMeals(
        @Query("user_id") userId: Int,
        @Query("date") date: String
    ): Response<MealsResponse>

    @GET("api/leaderboard")
    suspend fun getLeaderboard(): Response<LeaderboardResponse>

    @GET("api/activity")
    suspend fun getActivity(
        @Query("user_id") userId: Int,
        @Query("date") date: String
    ): Response<ActivityResponse>

    @POST("api/goals")
    suspend fun setGoal(@Body request: SetGoalRequest): Response<SetGoalResponse>

    @GET("api/goals")
    suspend fun getGoal(
        @Query("user_id") userId: Int,
        @Query("date") date: String
    ): Response<GetGoalResponse>

    @POST("api/foods/custom")
    suspend fun saveCustomFood(@Body request: SaveCustomFoodRequest): Response<SaveFoodResponse>

}