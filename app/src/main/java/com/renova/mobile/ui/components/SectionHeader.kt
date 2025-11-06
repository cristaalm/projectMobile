package com.renova.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import android.content.Intent
import com.renova.mobile.ui.activities.ManualGeneralActivity
import com.renova.mobile.ui.activities.FaqActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    hasNavigationIcon: Boolean = false,
    textColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(RenovaColors.Primary)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(
                start = if (hasNavigationIcon) 56.dp else 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp
            ),
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFontFamily
        )
    }
}

@Composable
fun BusinessSectionHeader(
    title: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    onOpenManual: (() -> Unit)? = null,
    onOpenFAQ: (() -> Unit)? = null,
    hasNavigationIcon: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(RenovaColors.Primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (hasNavigationIcon) {
                Spacer(modifier = Modifier.width(20.dp))
            }
            Text(
                text = title,
                color = textColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFontFamily
            )
            // Ícono de cierre de sesión con modal de confirmación, consistente en todos los headers
            LogoutAction(onConfirm = onLogout)
        }
    }
}

@Composable
fun RightMenuAction(
    onOpenManual: () -> Unit,
    onOpenFAQ: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
    val primaryColor = Color(0xFF08b662)

    Box(
        modifier = modifier
            .size(72.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { showMenu = true },
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Filled.Dashboard,
                contentDescription = "Menú",
                tint = Color(0xFF05D16E),
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showMenu) {
        Dialog(onDismissRequest = { showMenu = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            var panelVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { panelVisible = true }
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                        .clickable { panelVisible = false }
                )
                AnimatedVisibility(
                    visible = panelVisible,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(280.dp),
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                        color = Color(0xFF05D16E)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Menú",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { panelVisible = false }) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                                }
                            }
                            TextButton(
                                onClick = {
                                    panelVisible = false
                                    onOpenManual()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Text("Manual general (PDF)")
                            }
                            TextButton(
                                onClick = {
                                    panelVisible = false
                                    onOpenFAQ()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Text("Preguntas frecuentes")
                            }
                            Divider(color = Color.White.copy(alpha = 0.3f))
                            Button(
                                onClick = {
                                    panelVisible = false
                                    onLogout()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF05D16E))
                            ) {
                                Text("Cerrar sesión")
                            }
                        }
                    }
                }
                LaunchedEffect(panelVisible) {
                    if (!panelVisible) {
                        delay(250)
                        showMenu = false
                    }
                }
            }
        }
    }
}
