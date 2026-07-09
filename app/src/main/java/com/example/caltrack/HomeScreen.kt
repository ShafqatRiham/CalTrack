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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.caltrack.network.ActivityLogRequest
import com.example.caltrack.network.MealLogItem
import com.example.caltrack.network.MealLogRequest
import com.example.caltrack.network.RetrofitClient
import com.example.caltrack.network.SaveFoodRequest
import com.example.caltrack.ui.theme.Purple40
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import com.example.caltrack.network.SetGoalRequest
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.font.FontWeight

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
    val food: FoodItem,
    val mealType: String = "",
    val logTime: String = ""
)

fun getMealTypeFromTime(hour: Int): String {
    return when {
        hour < 10 -> "breakfast"
        hour < 14 -> "lunch"
        hour < 18 -> "dinner"
        else -> "snack"
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(userId: Int = 1,onNavigateToLeaderboard: () -> Unit = {}, onLogout: () -> Unit = {}) {
    val now = LocalTime.now()

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var addedMessage by remember { mutableStateOf("") }

    val loggedFoods = remember { mutableStateListOf<LoggedFoodItem>() }

    val totalCalories = loggedFoods.sumOf { it.food.calories ?: 0.0 }
    val totalProtein = loggedFoods.sumOf { it.food.protein ?: 0.0 }
    val totalCarbs = loggedFoods.sumOf { it.food.carbs ?: 0.0 }
    val totalFat = loggedFoods.sumOf { it.food.fat ?: 0.0 }

    // Steps state
    var showStepsDialog by remember { mutableStateOf(false) }
    var stepsInput by remember { mutableStateOf("") }
    var todaySteps by remember { mutableStateOf(0) }
    var caloriesBurned by remember { mutableStateOf(0.0) }
    var stepsMessage by remember { mutableStateOf("") }
    var isLoggingSteps by remember { mutableStateOf(false) }

    // Food log dialog state
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var showFoodDialog by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf(getMealTypeFromTime(now.hour)) }
    var mealTypeExpanded by remember { mutableStateOf(false) }
    var logTime by remember { mutableStateOf(now.format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var isLoggingFood by remember { mutableStateOf(false) }

    // Date state
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isFetchingMeals by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var calorieGoal by remember { mutableStateOf<Int?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }
    var isSettingGoal by remember { mutableStateOf(false) }
    var goalMessage by remember { mutableStateOf("") }

    val mealTypes = listOf("breakfast", "lunch", "dinner", "snack")
    val scope = rememberCoroutineScope()

    // today always reflects selected date
    val today = selectedDate.format(DateTimeFormatter.ISO_DATE)

    fun removeFromLog(id: String) {
        loggedFoods.removeAll { it.id == id }
    }

    fun fetchActivityForDate(date: LocalDate) {
        scope.launch {
            try {
                val dateStr = date.format(DateTimeFormatter.ISO_DATE)
                val response = RetrofitClient.authApi.getActivity(
                    userId = userId,
                    date = dateStr
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    todaySteps = body?.steps ?: 0
                    caloriesBurned = body?.calories_burned ?: 0.0
                } else {
                    // No activity for this date — reset to 0
                    todaySteps = 0
                    caloriesBurned = 0.0
                }
            } catch (e: Exception) {
                todaySteps = 0
                caloriesBurned = 0.0
                android.util.Log.e("HomeScreen", "Fetch activity error: ${e.message}")
            }
        }
    }

    fun fetchGoalForDate(date: LocalDate) {
        scope.launch {
            try {
                val dateStr = date.format(DateTimeFormatter.ISO_DATE)
                val response = RetrofitClient.authApi.getGoal(
                    userId = userId,
                    date = dateStr
                )
                if (response.isSuccessful) {
                    calorieGoal = response.body()?.calorie_goal
                } else {
                    calorieGoal = null
                }
            } catch (e: Exception) {
                calorieGoal = null
                android.util.Log.e("HomeScreen", "Fetch goal error: ${e.message}")
            }
        }
    }

    fun fetchMealsForDate(date: LocalDate) {
        scope.launch {
            isFetchingMeals = true
            loggedFoods.clear()
            try {
                val dateStr = date.format(DateTimeFormatter.ISO_DATE)
                val response = RetrofitClient.authApi.getMeals(
                    userId = userId,
                    date = dateStr
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.meals?.forEach { meal ->
                        meal.items.forEach { item ->
                            loggedFoods.add(
                                LoggedFoodItem(
                                    food = FoodItem(
                                        externalApiId = null,
                                        name = item.food_name,
                                        brand = null,
                                        calories = item.calories,
                                        protein = item.protein,
                                        carbs = item.carbs,
                                        fat = item.fat
                                    ),
                                    mealType = meal.meal_type,
                                    logTime = meal.log_time?.take(5) ?: ""
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Fetch meals error: ${e.message}")
            } finally {
                isFetchingMeals = false
            }
        }
    }

    // Fetch meals whenever selected date changes
    LaunchedEffect(selectedDate) {
        fetchMealsForDate(selectedDate)
        fetchActivityForDate(selectedDate)
        fetchGoalForDate(selectedDate)
    }

    // Steps Dialog
    if (showStepsDialog) {
        AlertDialog(
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
                    OutlinedTextField(
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
                                val response = RetrofitClient.authApi.logActivity(
                                    ActivityLogRequest(
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
                TextButton(onClick = { showStepsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Food Log Dialog
    if (showFoodDialog && selectedFood != null) {
        AlertDialog(
            onDismissRequest = { showFoodDialog = false },
            title = { Text("Log Food") },
            text = {
                Column {
                    Text(
                        text = selectedFood!!.name,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${selectedFood!!.calories?.toInt() ?: "?"} kcal per 100g",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text("Meal Type", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = mealTypeExpanded,
                        onExpandedChange = { mealTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedMealType.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealTypeExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
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
                    Spacer(Modifier.height(12.dp))
                    Text("Time eaten (HH:mm)", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = logTime,
                        onValueChange = { logTime = it },
                        label = { Text("e.g. 08:30") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val food = selectedFood ?: return@Button
                        val logTimeFormatted = "$logTime:00"
                        scope.launch {
                            isLoggingFood = true
                            try {
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
                                            log_time = logTimeFormatted,
                                            items = listOf(
                                                MealLogItem(
                                                    food_id = foodId,
                                                    quantity = 1.0,
                                                    unit = "100g",
                                                    calories = food.calories ?: 0.0,
                                                    protein = food.protein ?: 0.0,
                                                    carbs = food.carbs ?: 0.0,
                                                    fat = food.fat ?: 0.0
                                                )
                                            )
                                        )
                                    )
                                    if (logResponse.isSuccessful) {
                                        loggedFoods.add(
                                            LoggedFoodItem(
                                                food = food,
                                                mealType = selectedMealType,
                                                logTime = logTime
                                            )
                                        )
                                        addedMessage = "${food.name} logged as $selectedMealType"
                                        showFoodDialog = false
                                        selectedMealType = getMealTypeFromTime(LocalTime.now().hour)
                                        logTime = LocalTime.now()
                                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                                    } else {
                                        addedMessage = "Failed to log meal"
                                    }
                                } else {
                                    addedMessage = "Failed to save food"
                                }
                            } catch (e: Exception) {
                                addedMessage = "Could not connect to server"
                                android.util.Log.e("HomeScreen", "Log error: ${e.message}")
                            } finally {
                                isLoggingFood = false
                            }
                        }
                    },
                    enabled = !isLoggingFood
                ) {
                    Text(if (isLoggingFood) "Logging..." else "Log Food")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFoodDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant
                            .ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set Calorie Goal") },
            text = {
                Column {
                    Text(
                        text = "Set your calorie intake goal for ${
                            if (selectedDate == LocalDate.now()) "today"
                            else selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))
                        }",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = { Text("Calories (e.g. 2000)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (goalMessage.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = goalMessage,
                            fontSize = 12.sp,
                            color = if (goalMessage.startsWith("Error")) Color.Red else Color.Green
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val goal = goalInput.toIntOrNull()
                        if (goal == null || goal <= 0) {
                            goalMessage = "Please enter a valid calorie goal"
                            return@Button
                        }
                        scope.launch {
                            isSettingGoal = true
                            goalMessage = ""
                            try {
                                val response = RetrofitClient.authApi.setGoal(
                                    SetGoalRequest(
                                        user_id = userId,
                                        goal_date = today,
                                        calorie_goal = goal
                                    )
                                )
                                if (response.isSuccessful) {
                                    calorieGoal = goal
                                    goalMessage = "Goal set successfully"
                                    kotlinx.coroutines.delay(800)
                                    showGoalDialog = false
                                    goalInput = ""
                                    goalMessage = ""
                                } else {
                                    goalMessage = "Error: Failed to set goal"
                                }
                            } catch (e: Exception) {
                                goalMessage = "Error: Could not connect to server"
                            } finally {
                                isSettingGoal = false
                            }
                        }
                    },
                    enabled = !isSettingGoal
                ) {
                    Text(if (isSettingGoal) "Setting..." else "Set Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Date selector row — at top of screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedDate == LocalDate.now()) "Today"
                else selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                fontSize = 16.sp,
                color = Purple40
            )
            TextButton(onClick = { showDatePicker = true }) {
                Text("Change Date")
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) CircularProgressIndicator()

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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFood = food
                                selectedMealType = getMealTypeFromTime(LocalTime.now().hour)
                                logTime = LocalTime.now()
                                    .format(DateTimeFormatter.ofPattern("HH:mm"))
                                showFoodDialog = true
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedDate == LocalDate.now()) "Today's Totals"
                            else selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = { showGoalDialog = true }) {
                            Text(
                                text = if (calorieGoal != null) "Edit Goal" else "Set Goal",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isFetchingMeals) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        // Calories consumed
                        Text(
                            text = "${totalCalories.toInt()} kcal consumed",
                            color = Color.White,
                            fontSize = 22.sp
                        )

                        // Goal progress
                        if (calorieGoal != null) {
                            Spacer(modifier = Modifier.height(6.dp))

                            val progress = (totalCalories / calorieGoal!!).toFloat().coerceIn(0f, 1f)
                            val remaining = calorieGoal!! - totalCalories.toInt()
                            val goalColor = when {
                                totalCalories >= calorieGoal!! -> Color(0xFFFF6B6B) // over goal - red
                                totalCalories >= calorieGoal!! * 0.9 -> Color(0xFFFFD700) // near goal - yellow
                                else -> Color(0xFF90EE90) // under goal - light green
                            }

                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(goalColor)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (remaining > 0) "$remaining kcal remaining of ${calorieGoal!!} goal"
                                else "${-remaining} kcal over goal",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        } else {
                            TextButton(onClick = { showGoalDialog = true }) {
                                Text(
                                    text = "Tap to set a calorie goal for this day",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }

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
                        }

// Net calories always shows when there is any data
                        if (totalCalories > 0 || caloriesBurned > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val netCalories = (totalCalories - caloriesBurned).toInt()
                            val netColor = when {
                                netCalories < 0 -> Color(0xFF90EE90) // burned more than consumed - green
                                calorieGoal != null && netCalories > calorieGoal!! -> Color(0xFFFF6B6B) // over goal - red
                                else -> Color.White
                            }
                            Text(
                                text = "Net calories: $netCalories kcal",
                                color = netColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (loggedFoods.isEmpty() && !isFetchingMeals) {
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
                                if (logged.mealType.isNotEmpty() || logged.logTime.isNotEmpty()) {
                                    Text(
                                        text = buildString {
                                            if (logged.mealType.isNotEmpty()) {
                                                append(logged.mealType.replaceFirstChar { it.uppercase() })
                                            }
                                            if (logged.logTime.isNotEmpty()) {
                                                append(" · ${logged.logTime}")
                                            }
                                        },
                                        fontSize = 11.sp,
                                        color = Purple40
                                    )
                                }
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Purple40
                )
            ) {
                Text("Log Steps")
            }
            Button(
                onClick = onNavigateToLeaderboard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Purple40
                )
            ) {
                Text("Leaderboard")
            }

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text("Logout")
            }
        }
    }
}