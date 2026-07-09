package com.example.caltrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltrack.network.LeaderboardEntry
import com.example.caltrack.network.RetrofitClient
import com.example.caltrack.ui.theme.Purple40
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(
    currentUserId: Int = 1,
    onNavigateToHome: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showTop3 by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    fun loadLeaderboard() {
        scope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val response = RetrofitClient.authApi.getLeaderboard()
                if (response.isSuccessful) {
                    leaderboard = response.body()?.leaderboard ?: emptyList()
                } else {
                    errorMessage = "Failed to load leaderboard"
                }
            } catch (e: Exception) {
                errorMessage = "Could not connect to server"
                android.util.Log.e("Leaderboard", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadLeaderboard()
    }

    // Find current user's entry in full leaderboard
    val currentUserEntry = leaderboard.find { it.user_id == currentUserId }

    // Filter leaderboard based on selected view
    val limit = if (showTop3) 3 else 10
    val visibleEntries = leaderboard.take(limit)

    // Check if current user is already in the visible list
    val currentUserInVisible = visibleEntries.any { it.user_id == currentUserId }

    Column(modifier = Modifier.fillMaxSize()) {

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Purple40)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏆 Leaderboard",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Top 3 / Top 10 toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { showTop3 = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showTop3) Purple40 else Color.LightGray,
                    contentColor = if (showTop3) Color.White else Color.DarkGray
                ),
                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
            ) {
                Text("Top 3")
            }
            Button(
                onClick = { showTop3 = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showTop3) Purple40 else Color.LightGray,
                    contentColor = if (!showTop3) Color.White else Color.DarkGray
                ),
                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
            ) {
                Text("Top 10")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ranked by longest streak 🔥",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage, color = Color.Red)
                }
            }

            leaderboard.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No streaks yet — start logging meals to appear here!",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    // Show current user's placement at top if not in visible list
                    if (!currentUserInVisible && currentUserEntry != null) {
                        item {
                            Text(
                                text = "Your placement",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            LeaderboardCard(
                                entry = currentUserEntry,
                                isCurrentUser = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (showTop3) "Top 3" else "Top 10",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Show visible leaderboard entries
                    items(visibleEntries) { entry ->
                        LeaderboardCard(
                            entry = entry,
                            isCurrentUser = entry.user_id == currentUserId
                        )
                    }
                }
            }
        }

        // Bottom navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Purple40)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateToHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Purple40
                )
            ) {
                Text("Home")
            }
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.5f),
                    contentColor = Purple40
                )
            ) {
                Text("Leaderboard")
            }
            Button(
                onClick = onNavigateToProfile,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Purple40
                )
            ) {
                Text("Profile")
            }
        }
    }
}

@Composable
fun LeaderboardCard(
    entry: LeaderboardEntry,
    isCurrentUser: Boolean
) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.Gray
    }
    val rankEmoji = when (entry.rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#${entry.rank}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser)
                Purple40.copy(alpha = 0.15f)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(rankColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rankEmoji,
                        fontSize = if (entry.rank <= 3) 18.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankColor
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = entry.username + if (isCurrentUser) " (You)" else "",
                        fontSize = 15.sp,
                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "Logging since streak began",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "🔥 ${entry.streak}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple40
                )
                Text(
                    text = "day streak",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}