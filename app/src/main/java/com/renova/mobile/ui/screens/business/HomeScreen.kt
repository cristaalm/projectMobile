package com.renova.mobile.ui.screens.business

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.renova.mobile.R
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.components.BusinessSectionHeader

@Composable
fun BusinessHomeScreen(onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        BusinessSectionHeader(
            title = stringResource(id = R.string.bottom_nav_home),
            onLogout = onLogout,
            textColor = Color.White
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Home Business")
        }
    }
}

