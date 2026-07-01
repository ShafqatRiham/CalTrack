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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.caltrack.ui.theme.Purple40


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


@Composable
fun HomeScreen() {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(placeholderFoods) }


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
                    //Real API call Riham needs to add
                    results = if (query.isBlank()) {
                        placeholderFoods
                    } else {
                        placeholderFoods.filter { it.name.contains(query, ignoreCase = true) }
                    }
                }) {
                    Text("Search")
                }
            }


            Spacer(modifier = Modifier.height(16.dp))


            LazyColumn {
                items(results) { food ->
                    Text("${food.name} - ${food.calories} kcal")
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }


        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Gnome")
        }


        Column(
            modifier = Modifier.fillMaxWidth().background(color = Purple40),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Buttons and stuff")
        }
    }
}
