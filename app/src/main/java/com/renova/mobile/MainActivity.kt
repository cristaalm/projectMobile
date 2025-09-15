package com.renova.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.renova.mobile.ui.screens.LoginScreen
import com.renova.mobile.ui.theme.RENOVAMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RENOVAMobileTheme {
                LoginScreen()
            }
        }
    }
}