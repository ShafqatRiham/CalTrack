package com.example.caltrack

import android.widget.ListView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.caltrack.ui.theme.Purple40

var foodList = mutableListOf("Egg", "Bacon", "Bread")

@Composable
fun HomeScreen() {

    LazyColumn(modifier = Modifier, verticalArrangement = Arrangement.Center) {
        foodList
    }

    Column(
        modifier = Modifier.fillMaxSize(),
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