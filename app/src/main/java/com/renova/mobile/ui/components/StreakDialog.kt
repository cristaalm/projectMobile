package com.renova.mobile.ui.components

import com.renova.mobile.ui.screens.*
import androidx.compose.animation.core.*
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

// Dialog del Challenge - VERSIÓN CORREGIDA
/*@Composable
fun WeeklyChallengeDialog(
    challenge: WeeklyChallenge,
    isAccepted: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    renovaColors: RenovaColorScheme
) {
    val progress = if (challenge.targetProgress > 0) {
        (challenge.currentProgress.toFloat() / challenge.targetProgress.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val daysRemaining = 4

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = RenovaColors.SecondaryColor
            ),
            elevation = cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Theaters,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = PoppinsFontFamily
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = challenge.titleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = PoppinsFontFamily
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.description),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontFamily = PoppinsFontFamily
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.goal),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = null,
                                        tint = RenovaColors.TertiaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = challenge.goal,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            RenovaColors.TertiaryColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = RenovaColors.TertiaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.reward),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontFamily = PoppinsFontFamily
                            )
                            Text(
                                text = challenge.rewardText,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }
                }

                if (isAccepted) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stringResource(R.string.progress)}:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                fontFamily = PoppinsFontFamily
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RenovaColors.TertiaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(5.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(10.dp)
                                    .background(
                                        RenovaColors.TertiaryColor,
                                        RoundedCornerShape(5.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${challenge.currentProgress} / ${challenge.targetProgress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAccepted) {
                            "${stringResource(R.string.expires_in)} $daysRemaining ${stringResource(R.string.days)}"
                        } else {
                            "$daysRemaining ${stringResource(R.string.days_remaining)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = PoppinsFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!isAccepted) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RenovaColors.TertiaryColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.accept_challenge),
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
}*/

// BadgeDialog - VERSIÓN CORREGIDA
@Composable
fun BadgeDialog(
    badge: MonthlyBadge,
    currentMonthPoints: Int,
    isClaimingBadge: Boolean,
    onDismiss: () -> Unit,
    onClaim: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (badge.isUnlocked) badge.backgroundColor else Color(0xFFE0E0E0)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icono del badge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = if (badge.isUnlocked) {
                                Color.White.copy(alpha = 0.25f)
                            } else {
                                Color(0xFFF5F5F5)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = badge.iconRes),
                        contentDescription = badge.title,
                        modifier = Modifier.size(55.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            if (badge.isUnlocked) Color.White else Color(0xFF9E9E9E)
                        ),
                        alpha = if (badge.isUnlocked) 1f else 0.5f
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Título
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (badge.isUnlocked) badge.color else Color(0xFF616161),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = PoppinsFontFamily
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = badge.titleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (badge.isUnlocked) badge.color.copy(alpha = 0.7f) else Color(0xFF9E9E9E),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = PoppinsFontFamily
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Estado del badge
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (badge.isClaimed) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = badge.color,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.claimed),
                            style = MaterialTheme.typography.labelLarge,
                            color = badge.color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily
                        )
                    } else if (badge.isUnlocked) {
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
                            color = badge.color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.locked),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Información del badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (badge.isUnlocked) {
                                Color.White.copy(alpha = 0.15f)
                            } else {
                                Color(0xFFF5F5F5)
                            },
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
                            color = if (badge.isUnlocked) badge.color.copy(alpha = 0.7f) else Color(0xFF757575),
                            fontSize = 12.sp,
                            fontFamily = PoppinsFontFamily
                        )
                        Text(
                            text = stringResource(R.string.points_format, badge.requiredPoints),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (badge.isUnlocked) badge.color else Color(0xFF616161),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            fontFamily = PoppinsFontFamily
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(
                            color = if (badge.isUnlocked) {
                                Color.White.copy(alpha = 0.2f)
                            } else {
                                Color(0xFFE0E0E0)
                            },
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.reward_bonus),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (badge.isUnlocked) badge.color.copy(alpha = 0.7f) else Color(0xFF757575),
                            fontSize = 12.sp,
                            fontFamily = PoppinsFontFamily
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (badge.isUnlocked) Color(0xFFFFD700) else Color(0xFFBDBDBD),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+${badge.bonusPoints} ${stringResource(R.string.points)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (badge.isUnlocked) badge.color else Color(0xFF616161),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }
                }

                // Barra de progreso para badges no desbloqueados
                if (!badge.isUnlocked) {
                    Spacer(modifier = Modifier.height(20.dp))

                    val progress = (currentMonthPoints.toFloat() / badge.requiredPoints.toFloat()).coerceIn(0f, 1f)
                    val pointsNeeded = (badge.requiredPoints - currentMonthPoints).coerceAtLeast(0)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.points_needed, pointsNeeded),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575),
                            fontSize = 13.sp,
                            fontFamily = PoppinsFontFamily
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .background(RenovaColors.PrimaryColor, RoundedCornerShape(4.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "$currentMonthPoints / ${badge.requiredPoints} pts (${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E),
                            fontSize = 11.sp,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }

                // Botón de reclamar (solo si está desbloqueado y no reclamado)
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
                                    tint = badge.backgroundColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.claim_reward),
                                    color = badge.backgroundColor,
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