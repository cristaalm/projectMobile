package com.renova.mobile.ui.components

import com.renova.mobile.ui.screens.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.renova.mobile.R
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.RenovaColorScheme

@Composable
fun BadgeDialogV2(
    badge: MonthlyBadge,
    currentMonthPoints: Int,
    isClaimingBadge: Boolean,
    onDismiss: () -> Unit,
    onClaim: () -> Unit
) {
    val backgroundColor = when {
        badge.isClaimed -> RenovaColors.PrimaryColor.copy(alpha = 0.7f)
        badge.isUnlocked -> RenovaColors.SecondaryColor
        else -> RenovaColors.SecondaryColor.copy(alpha = 0.5f)
    }

    val textColor = Color.White

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .padding(horizontal = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.45f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = badge.iconRes),
                            contentDescription = badge.name,
                            modifier = Modifier.size(55.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = PoppinsFontFamily
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            badge.isClaimed -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.claimed),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                            }

                            badge.isUnlocked -> {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.available_to_claim),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                            }

                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.locked),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.requirement),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontFamily = PoppinsFontFamily
                            )
                            Text(
                                text = stringResource(R.string.points_format, badge.pointsRequired),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                fontFamily = PoppinsFontFamily
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.2f),
                                thickness = 1.dp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(R.string.reward_bonus),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontFamily = PoppinsFontFamily
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+${badge.bonusPoints} ${stringResource(R.string.points)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                            }
                        }
                    }

                    if (!badge.isUnlocked && !badge.isClaimed) {
                        Spacer(modifier = Modifier.height(20.dp))

                        val progress =
                            (currentMonthPoints.toFloat() / badge.pointsRequired.toFloat()).coerceIn(
                                0f,
                                1f
                            )
                        val pointsNeeded =
                            (badge.pointsRequired - currentMonthPoints).coerceAtLeast(0)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.points_needed, pointsNeeded),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontFamily = PoppinsFontFamily
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .fillMaxHeight()
                                        .background(
                                            RenovaColors.PrimaryColor,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "$currentMonthPoints / ${badge.pointsRequired} pts (${(progress * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }

                    if (badge.isUnlocked && !badge.isClaimed) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onClaim,
                            enabled = !isClaimingBadge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RenovaColors.TertiaryColor,
                                disabledContainerColor = RenovaColors.TertiaryColor.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isClaimingBadge) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = RenovaColors.SecondaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.claim_reward),
                                        color = RenovaColors.SecondaryColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}