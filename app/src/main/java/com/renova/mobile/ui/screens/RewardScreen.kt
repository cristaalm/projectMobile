package com.renova.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.renova.mobile.ui.screens.viewmodel.RewardViewModel
import com.renova.mobile.ui.screens.viewmodel.RewardViewModelFactory
import com.renova.mobile.network.Reward
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.RenovaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardScreen(navController: NavController, allianceId: Int) {
    val viewModel: RewardViewModel = viewModel(factory = RewardViewModelFactory(allianceId))
    val uiState = viewModel.uiState
    val colors = LocalRenovaColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.allianceName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.cardBackground,
                    titleContentColor = colors.textPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
                }
                uiState.rewards.isEmpty() -> {
                    Text("Este comercio no tiene recompensas disponibles.", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.rewards, key = { it.id }) { reward ->
                            RewardCard(reward = reward)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardCard(reward: Reward) {
    val colors = LocalRenovaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = reward.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reward.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                // CAMBIO: Usamos el nombre correcto del campo de puntos
                text = "${reward.pointsRequired} Puntos",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = RenovaColors.Primary
            )
        }
    }
}