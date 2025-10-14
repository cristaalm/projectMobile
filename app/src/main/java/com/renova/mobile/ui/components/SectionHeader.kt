package com.renova.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import androidx.compose.foundation.layout.Arrangement

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
    textColor: Color = Color.White
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
            Text(
                text = title,
                color = textColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFontFamily
            )
            LogoutAction(onConfirm = onLogout)
        }
    }
}
