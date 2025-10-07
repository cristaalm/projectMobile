package com.renova.mobile.ui.screens.business

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.renova.mobile.ui.components.BusinessHeader

@Composable
fun BusinessHomeScreen(onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        BusinessHeader(title = "Inicio", onLogout = onLogout)

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Home Business")
        }
    }
}

