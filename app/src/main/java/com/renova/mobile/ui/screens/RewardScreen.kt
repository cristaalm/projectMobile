package com.renova.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.renova.mobile.R
import com.renova.mobile.ui.screens.viewmodel.RewardViewModel
import com.renova.mobile.ui.screens.viewmodel.RewardViewModelFactory
import com.renova.mobile.network.Reward
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardScreen(navController: NavController, allianceId: Int) {
    val viewModel: RewardViewModel = viewModel(factory = RewardViewModelFactory(allianceId))
    val uiState = viewModel.uiState
    val colors = LocalRenovaColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Usamos un Box para poder poner el botón de regreso encima del encabezado
        Box {
            SectionHeader(title = "Recompensas", hasNavigationIcon = true)

            // IconButton con zIndex para asegurar que quede encima
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .zIndex(1f) // Esto fuerza que esté por encima del SectionHeader
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.reward_screen_subtitle),
                                fontSize = 14.sp,
                                fontFamily = PoppinsFontFamily,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        itemsIndexed(uiState.rewards, key = { _, reward -> reward.id }) { index, reward ->
                            val cardBackgroundColor = colors.rewardCardBackgrounds[index % colors.rewardCardBackgrounds.size]
                            RewardCard(reward = reward, backgroundColor = cardBackgroundColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardCard(reward: Reward, backgroundColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = TicketShape(cornerRadius = 16f),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna izquierda para el texto
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .padding(start = 24.dp, top = 20.dp, bottom = 20.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reward.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = PoppinsFontFamily
                )
                Text(
                    text = reward.description,
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = PoppinsFontFamily
                )
            }

            // Divisor visual
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
            )

            // Columna derecha para los puntos
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = reward.pointsRequired.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.Black,
                    fontFamily = PoppinsFontFamily
                )
                Text(
                    text = "puntos",
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.8f),
                    fontFamily = PoppinsFontFamily
                )
            }
        }
    }
}

class TicketShape(private val cornerRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            path = drawTicketPath(size = size, cornerRadius = cornerRadius)
        )
    }
}

private fun drawTicketPath(size: Size, cornerRadius: Float): Path {
    return Path().apply {
        reset()
        val notchPosition = size.width * 0.65f
        val notchRadius = cornerRadius
        moveTo(0f, 0f)
        lineTo(notchPosition - notchRadius, 0f)
        arcTo(
            rect = Rect(
                left = notchPosition - notchRadius,
                top = -notchRadius,
                right = notchPosition + notchRadius,
                bottom = notchRadius
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        lineTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(notchPosition + notchRadius, size.height)
        arcTo(
            rect = Rect(
                left = notchPosition - notchRadius,
                top = size.height - notchRadius,
                right = notchPosition + notchRadius,
                bottom = size.height + notchRadius
            ),
            startAngleDegrees = 0f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        lineTo(0f, size.height)
        close()
    }
}