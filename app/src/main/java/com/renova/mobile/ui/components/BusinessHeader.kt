package com.renova.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.ui.theme.PoppinsFontFamily

@Composable
fun BusinessHeader(
    title: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF08b662)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(primaryColor)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = PoppinsFontFamily,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        LogoutAction(onConfirm = onLogout)
    }
}

@Composable
fun LogoutAction(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutModal by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val primaryColor = Color(0xFF08b662)

    Box(
        modifier = modifier
            .size(72.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isPressed) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { showLogoutModal = true },
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                painter = painterResource(id = R.drawable.salida_blanco),
                contentDescription = "Logout",
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    LogoutModal(
        isVisible = showLogoutModal,
        onDismiss = { showLogoutModal = false },
        onConfirm = {
            showLogoutModal = false
            onConfirm()
        }
    )
}