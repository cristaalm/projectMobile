package com.renova.mobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.renova.mobile.R
import com.renova.mobile.ui.theme.RenovaColors

@Composable
fun ErrorModal(
    isVisible: Boolean,
    errorTitle: String = stringResource(R.string.error_title),
    errorMessage: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    isDarkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme()
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Colores según tema
    val backgroundColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF333333)
    val secondaryTextColor = if (isDarkTheme) Color(0xFFAAAAAA) else Color(0xFF666666)

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
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
                    // Icono de error
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = RenovaColors.Error,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Título
                    Text(
                        text = errorTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mensaje de error
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones
                    if (onRetry != null) {
                        // Si hay función de reintentar, mostrar dos botones
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.modal_logout_cancel),
                                    color = secondaryTextColor
                                )
                            }
                            Button(
                                onClick = {
                                    onRetry()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RenovaColors.Error
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.retry),
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // Si no hay función de reintentar, solo mostrar botón de aceptar
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RenovaColors.Error
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.accept),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Variante simplificada para errores rápidos
@Composable
fun QuickErrorModal(
    isVisible: Boolean,
    errorMessage: String,
    onDismiss: () -> Unit
) {
    ErrorModal(
        isVisible = isVisible,
        errorMessage = errorMessage,
        onDismiss = onDismiss,
        onRetry = null
    )
}

// Variante para errores con opción de reintentar
@Composable
fun RetryableErrorModal(
    isVisible: Boolean,
    errorTitle: String = stringResource(R.string.error_title),
    errorMessage: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    ErrorModal(
        isVisible = isVisible,
        errorTitle = errorTitle,
        errorMessage = errorMessage,
        onDismiss = onDismiss,
        onRetry = onRetry
    )
}