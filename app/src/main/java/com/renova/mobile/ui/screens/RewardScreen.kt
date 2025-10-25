package com.renova.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
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
import com.renova.mobile.ui.theme.RenovaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardScreen(navController: NavController, allianceId: Int) {
    val viewModel: RewardViewModel = viewModel(factory = RewardViewModelFactory(allianceId))
    val uiState = viewModel.uiState
    val colors = LocalRenovaColors.current
    var selectedReward by remember { mutableStateOf<Reward?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box {
            SectionHeader(title = stringResource(id = R.string.reward_screen_title), hasNavigationIcon = true)

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.content_description_back),
                    tint = Color.White
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = RenovaColors.Primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.loading_rewards),
                            fontFamily = PoppinsFontFamily,
                            color = colors.textSecondary
                        )
                    }
                }
                uiState.error != null -> {
                    Text(
                        text = stringResource(id = R.string.error_prefix) + uiState.error,
                        modifier = Modifier.align(Alignment.Center),
                        fontFamily = PoppinsFontFamily,
                        color = colors.textPrimary
                    )
                }
                uiState.rewards.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.no_rewards_available),
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        fontFamily = PoppinsFontFamily,
                        color = colors.textPrimary
                    )
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
                            RewardCard(
                                reward = reward,
                                backgroundColor = cardBackgroundColor,
                                onClick = {
                                    selectedReward = reward
                                    showBottomSheet = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet && selectedReward != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            RewardDetailSheet(reward = selectedReward!!)
        }
    }
}

@Composable
private fun RewardDetailSheet(reward: Reward) {
    val colors = LocalRenovaColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Agregamos un padding inferior para que no se corte al final
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título del Sheet
        Text(
            text = stringResource(id = R.string.reward_detail_title),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Nombre de la recompensa
        Text(
            text = reward.name,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Descripción completa
        Text(
            text = reward.description,
            fontFamily = PoppinsFontFamily,
            fontSize = 15.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Puntos requeridos
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = reward.pointsRequired.toString(),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp,
                color = RenovaColors.Primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.pointsR),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun RewardCard(reward: Reward, backgroundColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
            )

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
                    text = stringResource(id = R.string.pointsR),
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.8f),
                    fontFamily = PoppinsFontFamily
                )
            }
        }
    }
}

class TicketShape(private val cornerRadius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Generic(path = drawTicketPath(size = size, cornerRadius = cornerRadius))
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
            rect = Rect(left = notchPosition - notchRadius, top = -notchRadius, right = notchPosition + notchRadius, bottom = notchRadius),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        lineTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(notchPosition + notchRadius, size.height)
        arcTo(
            rect = Rect(left = notchPosition - notchRadius, top = size.height - notchRadius, right = notchPosition + notchRadius, bottom = size.height + notchRadius),
            startAngleDegrees = 0f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        lineTo(0f, size.height)
        close()
    }
}