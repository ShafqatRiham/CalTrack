package com.example.caltrack

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private object HomeColors {
    val Background = Color(0xFFF7F3EC)
    val Surface = Color(0xFFFFFFFF)
    val Border = Color(0xFFDDD2BE)
    val Primary = Color(0xFF3F6C51)
    val Secondary = Color(0xFFC1502E)
    val TextPrimary = Color(0xFF2B2620)
    val TextMuted = Color(0xFF8A8074)
}

data class FoodItem(
    val name: String,
    val calories: Int
)

//Placeholder list
private val placeholderFoods = listOf(
    FoodItem("Chicken Breast", 165),
    FoodItem("Brown Rice", 216),
    FoodItem("Banana", 105),
    FoodItem("Greek Yogurt", 100),
    FoodItem("Almonds", 164),
    FoodItem("Eggs", 78),
    FoodItem("Bread", 81),
    FoodItem("Avocado", 234)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(placeholderFoods) }

    fun runSearch() {
        // API INTEGRATION POINT
        results = if (query.isBlank()) {
            placeholderFoods
        } else {
            placeholderFoods.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeColors.Background)
            .padding(20.dp)
    ) {
        Text(
            text = "CalTrack",
            color = HomeColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Search the food catalog",
            color = HomeColors.TextMuted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                placeholder = {
                    Text("e.g. chicken breast", color = HomeColors.TextMuted, fontSize = 14.sp)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { runSearch() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = HomeColors.Surface,
                    unfocusedContainerColor = HomeColors.Surface,
                    focusedBorderColor = HomeColors.Primary,
                    unfocusedBorderColor = HomeColors.Border,
                    focusedTextColor = HomeColors.TextPrimary,
                    unfocusedTextColor = HomeColors.TextPrimary,
                    cursorColor = HomeColors.Primary
                )
            )

            Button(
                onClick = { runSearch() },
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomeColors.Primary,
                    contentColor = HomeColors.Surface
                )
            ) {
                Text("Search", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matches yet — try a different search",
                    color = HomeColors.TextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(results) { food ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HomeColors.Surface, RoundedCornerShape(14.dp))
                            .border(1.dp, HomeColors.Border, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = food.name,
                            color = HomeColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${food.calories} kcal",
                            color = HomeColors.Secondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}