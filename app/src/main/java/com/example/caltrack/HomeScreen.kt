package com.example.caltrack

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltrack.network.MealLogItem
import com.example.caltrack.network.MealLogRequest
import com.example.caltrack.network.RetrofitClient
import com.example.caltrack.ui.theme.Purple40
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.caltrack.network.SaveFoodRequest

data class FoodItem(
    val externalApiId: String?,
    val name: String,
    val brand: String?,
    val calories: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(userId: Int = 1) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // Dialog state
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var showLogDialog by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf("breakfast") }
    var quantity by remember { mutableStateOf("1") }
    var mealTypeExpanded by remember { mutableStateOf(false) }
    var isLoggingMeal by remember { mutableStateOf(false) }

    val mealTypes = listOf("breakfast", "lunch", "dinner", "snack")
    val scope = rememberCoroutineScope()

    // Log meal dialog
    if (showLogDialog && selectedFood != null) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("Log Food") },
            text = {
                Column {
                    Text(
                        text = selectedFood!!.name,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text("Meal Type", fontSize = 12.sp, color = Color.Gray)
                    ExposedDropdownMenuBox(
                        expanded = mealTypeExpanded,
                        onExpandedChange = { mealTypeExpanded = it }
                    ) {
                        TextField(
                            value = selectedMealType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealTypeExpanded)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = mealTypeExpanded,
                            onDismissRequest = { mealTypeExpanded = false }
                        ) {
                            mealTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        selectedMealType = type
                                        mealTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Quantity (servings)", fontSize = 12.sp, color = Color.Gray)
                    TextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val cal = ((selectedFood!!.calories ?: 0.0) * (quantity.toDoubleOrNull() ?: 1.0)).toInt()
                    Text(
                        text = "Total calories: $cal kcal",
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantity.toDoubleOrNull() ?: 1.0
                        val food = selectedFood ?: return@Button
                        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

                        scope.launch {
                            isLoggingMeal = true
                            try {
                                // First save the food to our database if not already saved
                                val saveResponse = RetrofitClient.authApi.saveFood(
                                    SaveFoodRequest(
                                        name = food.name,
                                        calories = food.calories ?: 0.0,
                                        protein = food.protein ?: 0.0,
                                        carbs = food.carbs ?: 0.0,
                                        fat = food.fat ?: 0.0,
                                        external_api_id = food.externalApiId ?: ""
                                    )
                                )

                                val foodId = saveResponse.body()?.food_id

                                if (foodId != null) {
                                    val logResponse = RetrofitClient.authApi.logMeal(
                                        MealLogRequest(
                                            user_id = userId,
                                            meal_type = selectedMealType,
                                            log_date = today,
                                            log_time = now,
                                            items = listOf(
                                                MealLogItem(
                                                    food_id = foodId,
                                                    quantity = qty,
                                                    unit = "100g",
                                                    calories = (food.calories ?: 0.0) * qty,
                                                    protein = (food.protein ?: 0.0) * qty,
                                                    carbs = (food.carbs ?: 0.0) * qty,
                                                    fat = (food.fat ?: 0.0) * qty
                                                )
                                            )
                                        )
                                    )

                                    if (logResponse.isSuccessful) {
                                        successMessage = "${food.name} logged as $selectedMealType"
                                        showLogDialog = false
                                    } else {
                                        errorMessage = "Failed to log meal"
                                    }
                                } else {
                                    errorMessage = "Failed to save food"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Could not connect to server"
                                android.util.Log.e("HomeScreen", "Log error: ${e.message}")
                            } finally {
                                isLoggingMeal = false
                            }
                        }
                    },
                    enabled = !isLoggingMeal
                ) {
                    Text(if (isLoggingMeal) "Logging..." else "Log Food")
                }
            },
            dismissButton = {
                Button(onClick = { showLogDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search food") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    if (query.isBlank()) {
                        errorMessage = "Please enter a food to search"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        successMessage = ""
                        results = emptyList()

                        try {
                            val response = RetrofitClient.authApi.searchFoods(query)
                            if (response.isSuccessful) {
                                val body = response.body()
                                results = body?.results?.map { food ->
                                    FoodItem(
                                        externalApiId = food.external_api_id,
                                        name = food.name,
                                        brand = food.brand,
                                        calories = food.calories,
                                        protein = food.protein,
                                        carbs = food.carbs,
                                        fat = food.fat
                                    )
                                } ?: emptyList()

                                if (results.isEmpty()) {
                                    errorMessage = "No results found for \"$query\""
                                }
                            } else {
                                errorMessage = "Search failed. Please try again"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Could not connect to server"
                            android.util.Log.e("HomeScreen", "Search error: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Text("Search")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (successMessage.isNotEmpty()) {
                Text(
                    text = successMessage,
                    color = Color.Green,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            LazyColumn {
                items(results) { food ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFood = food
                                quantity = "1"
                                selectedMealType = "breakfast"
                                showLogDialog = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = food.name, fontSize = 16.sp)
                        if (!food.brand.isNullOrBlank()) {
                            Text(
                                text = food.brand,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "${food.calories?.toInt() ?: "?"} kcal | " +
                                    "P: ${food.protein?.toInt() ?: "?"}g | " +
                                    "C: ${food.carbs?.toInt() ?: "?"}g | " +
                                    "F: ${food.fat?.toInt() ?: "?"}g",
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Tap to log",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Gnome")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Purple40),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Buttons and stuff")
        }
    }
}