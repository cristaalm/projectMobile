package com.renova.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    hasNavigationIcon: Boolean = false, // Nuevo parámetro
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
                start = if (hasNavigationIcon) 56.dp else 16.dp, // Más espacio si hay icono
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
