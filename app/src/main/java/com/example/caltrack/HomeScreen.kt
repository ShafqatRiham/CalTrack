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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltrack.network.RetrofitClient
import com.example.caltrack.ui.theme.Purple40
import kotlinx.coroutines.launch
import java.util.UUID

data class FoodItem(
    val externalApiId: String?,
    val name: String,
    val brand: String?,
    val calories: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?
)

data class LoggedFoodItem(
    val id: String = UUID.randomUUID().toString(),
    val food: FoodItem
)

@Composable
fun HomeScreen(userId: Int = 1) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var addedMessage by remember { mutableStateOf("") }

    val loggedFoods = remember { mutableStateListOf<LoggedFoodItem>() }

    var showStepsDialog by remember { mutableStateOf(false) }
    var stepsInput by remember { mutableStateOf("") }
    var todaySteps by remember { mutableStateOf(0) }
    var caloriesBurned by remember { mutableStateOf(0.0) }
    var stepsMessage by remember { mutableStateOf("") }
    var isLoggingSteps by remember { mutableStateOf(false) }

    val totalCalories = loggedFoods.sumOf { it.food.calories ?: 0.0 }
    val totalProtein = loggedFoods.sumOf { it.food.protein ?: 0.0 }
    val totalCarbs = loggedFoods.sumOf { it.food.carbs ?: 0.0 }
    val totalFat = loggedFoods.sumOf { it.food.fat ?: 0.0 }

    val scope = rememberCoroutineScope()

    fun addToLog(food: FoodItem) {
        loggedFoods.add(LoggedFoodItem(food = food))
        addedMessage = "Added ${food.name}"
    }

    fun removeFromLog(id: String) {
        loggedFoods.removeAll { it.id == id }
    }

    if (showStepsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showStepsDialog = false },
            title = { Text("Log Steps") },
            text = {
                Column {
                    Text(
                        text = "Enter your step count for today",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = stepsInput,
                        onValueChange = { stepsInput = it },
                        label = { Text("Steps") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (stepsMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stepsMessage,
                            fontSize = 12.sp,
                            color = if (stepsMessage.startsWith("Error")) Color.Red else Color.Green
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val steps = stepsInput.toIntOrNull()
                        if (steps == null || steps < 0) {
                            stepsMessage = "Please enter a valid step count"
                            return@Button
                        }

                        scope.launch {
                            isLoggingSteps = true
                            stepsMessage = ""
                            try {
                                val today = java.time.LocalDate.now()
                                    .format(java.time.format.DateTimeFormatter.ISO_DATE)

                                val response = RetrofitClient.authApi.logActivity(
                                    com.example.caltrack.network.ActivityLogRequest(
                                        user_id = userId,
                                        steps = steps,
                                        log_date = today
                                    )
                                )

                                if (response.isSuccessful) {
                                    val body = response.body()
                                    todaySteps = steps
                                    caloriesBurned = body?.calories_burned ?: 0.0
                                    stepsMessage = "Logged successfully"
                                    kotlinx.coroutines.delay(1000)
                                    showStepsDialog = false
                                    stepsInput = ""
                                    stepsMessage = ""
                                } else {
                                    stepsMessage = "Error: Failed to log steps"
                                }
                            } catch (e: Exception) {
                                stepsMessage = "Error: Could not connect to server"
                                android.util.Log.e("HomeScreen", "Steps error: ${e.message}")
                            } finally {
                                isLoggingSteps = false
                            }
                        }
                    },
                    enabled = !isLoggingSteps
                ) {
                    Text(if (isLoggingSteps) "Logging..." else "Log Steps")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showStepsDialog = false }
                ) {
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
                        results = emptyList()
                        errorMessage = ""
                        addedMessage = ""
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        addedMessage = ""
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

            if (addedMessage.isNotEmpty()) {
                Text(
                    text = addedMessage,
                    color = Purple40,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (results.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${results.size} result${if (results.size == 1) "" else "s"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Clear results ✕",
                        fontSize = 13.sp,
                        color = Purple40,
                        modifier = Modifier.clickable {
                            results = emptyList()
                            errorMessage = ""
                            addedMessage = ""
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            LazyColumn {
                items(results) { food ->
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { addToLog(food) }
                        .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = food.name,
                            fontSize = 16.sp
                        )
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
                            text = "Tap to add",
                            fontSize = 11.sp,
                            color = Purple40
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Purple40)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Today's Totals",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${totalCalories.toInt()} kcal consumed",
                        color = Color.White,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Protein: ${totalProtein.toInt()}g   " +
                                "Carbs: ${totalCarbs.toInt()}g   " +
                                "Fat: ${totalFat.toInt()}g",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    if (todaySteps > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Steps: $todaySteps  |  Burned: ${caloriesBurned.toInt()} kcal",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Net calories: ${(totalCalories - caloriesBurned).toInt()} kcal",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (loggedFoods.isEmpty()) {
                Text(
                    text = "Nothing logged yet. Search above and tap a result to add it.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(loggedFoods, key = { it.id }) { logged ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = logged.food.name, fontSize = 14.sp)
                                Text(
                                    text = "${logged.food.calories?.toInt() ?: "?"} kcal | " +
                                            "P: ${logged.food.protein?.toInt() ?: "?"}g | " +
                                            "C: ${logged.food.carbs?.toInt() ?: "?"}g | " +
                                            "F: ${logged.food.fat?.toInt() ?: "?"}g",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "✕",
                                color = Color.Red,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .clickable { removeFromLog(logged.id) }
                                    .padding(start = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Purple40)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showStepsDialog = true },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Purple40
                )
            ) {
                Text("Log Steps")
            }
        }
    }
}