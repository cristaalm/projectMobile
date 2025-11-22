package com.renova.mobile.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.renova.mobile.ui.screens.business.statistics.BusinessStatisticsScreen
import com.renova.mobile.ui.theme.RenovaTheme
import com.renova.mobile.utils.LocaleHelper
import androidx.core.view.WindowCompat
import com.renova.mobile.utils.SessionManager
import com.renova.mobile.MainActivity

class BusinessStatisticsActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_ALLIANCE_ID = "EXTRA_ALLIANCE_ID"

        // Método helper para iniciar esta activity
        fun start(context: Context, allianceId: Int) {
            val intent = Intent(context, BusinessStatisticsActivity::class.java).apply {
                putExtra(EXTRA_ALLIANCE_ID, allianceId)
            }
            context.startActivity(intent)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext = LocaleHelper.setLocale(
            newBase,
            LocaleHelper.getLanguage(newBase)
        )
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener el allianceId del Intent
        val allianceId = intent.getIntExtra(EXTRA_ALLIANCE_ID, -1)

        // Si no hay allianceId válido, cerrar la actividad
        if (allianceId == -1) {
            finish()
            return
        }

        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            RenovaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BusinessStatisticsScreen(
                        allianceId = allianceId, // ✅ Ahora se pasa el parámetro
                        onLogout = {
                            val sessionManager = SessionManager(this@BusinessStatisticsActivity)
                            sessionManager.clearFcmToken()
                            sessionManager.logout()
                            finishAffinity()
                            startActivity(Intent(this@BusinessStatisticsActivity, MainActivity::class.java))
                        },
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}