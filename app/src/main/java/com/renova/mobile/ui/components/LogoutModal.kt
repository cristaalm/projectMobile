package com.renova.mobile.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.renova.mobile.R
import kotlinx.coroutines.delay

@Composable
fun LogoutModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(2000)
            onConfirm()
            isLoading = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Colores según tema
    val backgroundColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF333333)
    val secondaryTextColor = if (isDarkTheme) Color(0xFFAAAAAA) else Color(0xFF666666)
    val buttonColor = if (isDarkTheme) Color(0xFFFF7043) else Color(0xFFFF5722)

    if (isVisible) {
        Dialog(
            onDismissRequest = { if (!isLoading) onDismiss() },
            properties = DialogProperties(
                dismissOnBackPress = !isLoading,
                dismissOnClickOutside = !isLoading
            )
        ) {
            Card(
                modifier = Modifier
                    .wrapContentSize()
                    .scale(scale),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .widthIn(min = 200.dp, max = 280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = buttonColor,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Cerrando sesión...",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = textColor
                        )
                    } else {
                        Text(
                            "Cerrar sesión",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¿Seguro que deseas salir?",
                            fontSize = 14.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", color = buttonColor)
                            }
                            Button(
                                onClick = { isLoading = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                            ) {
                                Text("Salir", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
