package com.renova.mobile.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.renova.mobile.R
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.RenovaColorScheme

enum class ManualViewMode {
    ONLINE,
    PDF
}

@Composable
fun ManualDialog(
    onDismiss: () -> Unit
) {
    val colors: RenovaColorScheme = LocalRenovaColors.current
    val context = LocalContext.current

    var selectedMode by remember { mutableStateOf<ManualViewMode?>(null) }
    var showOnlineExpanded by remember { mutableStateOf(false) }
    var showPdfExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBackground
        ) {
            Column(
                modifier = Modifier.wrapContentSize()
            ) {
                // Header
                Surface(
                    color = colors.primaryColor.copy(alpha = 0.125f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = colors.primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = stringResource(R.string.user_manual),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = colors.textPrimary
                            )
                        }
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Opción 1: Ver en línea
                    ManualOptionCard(
                        title = stringResource(R.string.view_online),
                        description = stringResource(R.string.view_online_description),
                        icon = Icons.Default.Public,
                        isExpanded = showOnlineExpanded,
                        isSelected = selectedMode == ManualViewMode.ONLINE,
                        onToggle = {
                            showOnlineExpanded = !showOnlineExpanded
                            if (showPdfExpanded) showPdfExpanded = false
                        },
                        colors = colors
                    ) {
                        ManualOnlineContent(
                            onOpenLink = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://issuu.com/renovaapp/docs/manual_de_usuario"))
                                context.startActivity(intent)
                                onDismiss()
                            },
                            colors = colors
                        )
                    }

                    // Opción 2: Ver PDF
                    ManualOptionCard(
                        title = stringResource(R.string.view_pdf),
                        description = stringResource(R.string.view_pdf_description),
                        icon = Icons.Default.PictureAsPdf,
                        isExpanded = showPdfExpanded,
                        isSelected = selectedMode == ManualViewMode.PDF,
                        onToggle = {
                            showPdfExpanded = !showPdfExpanded
                            if (showOnlineExpanded) showOnlineExpanded = false
                        },
                        colors = colors
                    ) {
                        ManualPdfContent(
                            onOpenPdf = {
                                selectedMode = ManualViewMode.PDF
                                // Aquí abrirías el visor de PDF
                            },
                            colors = colors
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManualOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isExpanded: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit,
    colors: RenovaColorScheme,
    content: @Composable (() -> Unit)
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colors.primaryColor.copy(alpha = 0.1f) else colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) colors.primaryColor else colors.textSecondary.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column {
            // Header clickeable
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) colors.primaryColor else colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = if (isSelected) colors.primaryColor else colors.textSecondary,
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(arrowRotation)
                    )
                }
            }

            // Contenido expandible
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(300)),
                exit = shrinkVertically(tween(300))
            ) {
                Column {
                    Divider(
                        color = colors.textSecondary.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Box(modifier = Modifier.padding(16.dp)) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun ManualOnlineContent(
    onOpenLink: () -> Unit,
    colors: RenovaColorScheme
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info card con animación
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colors.primaryColor.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = colors.primaryColor.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = colors.primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.online_manual_features),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary,
                    lineHeight = 18.sp
                )
            }
        }

        Button(
            onClick = onOpenLink,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.open_in_browser))
        }
    }
}

@Composable
fun ManualPdfContent(
    onOpenPdf: () -> Unit,
    colors: RenovaColorScheme
) {
    var showPdfViewer by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info card
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colors.primaryColor.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = colors.primaryColor.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.pdf_manual_features),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary,
                    lineHeight = 18.sp
                )
            }
        }

        Button(
            onClick = { showPdfViewer = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.view_pdf))
        }
    }

    if (showPdfViewer) {
        PdfViewerDialog(onDismiss = { showPdfViewer = false })
    }
}