package com.example.caltrack

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltrack.network.RetrofitClient
import com.example.caltrack.ui.theme.Purple40
import kotlinx.coroutines.launch

data class FoodItem(
    val externalApiId: String?,
    val name: String,
    val brand: String?,
    val calories: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?
)

@Composable
fun HomeScreen() {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

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
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            LazyColumn {
                items(results) { food ->
                    Column(modifier = Modifier
                        .fillMaxWidth()
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
                                color = androidx.compose.ui.graphics.Color.Gray
                            )
                        }
                        Text(
                            text = "${food.calories?.toInt() ?: "?"} kcal | " +
                                    "P: ${food.protein?.toInt() ?: "?"}g | " +
                                    "C: ${food.carbs?.toInt() ?: "?"}g | " +
                                    "F: ${food.fat?.toInt() ?: "?"}g",
                            fontSize = 12.sp
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