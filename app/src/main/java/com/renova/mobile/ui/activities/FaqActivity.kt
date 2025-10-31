package com.renova.mobile.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.renova.mobile.R
import com.renova.mobile.ui.components.BusinessSectionHeader
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.RenovaTheme
import com.renova.mobile.utils.LocaleHelper

class FaqActivity : ComponentActivity() {

    // 🔹 Aplica el idioma guardado antes de crear la interfaz
    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext = LocaleHelper.setLocale(
            newBase,
            LocaleHelper.getLanguage(newBase)
        )
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Habilita edge-to-edge para que el contenido se extienda bajo las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            RenovaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    FaqScreen(
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }

    // 🔹 Si el sistema cambia idioma (configuración del dispositivo)
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}

data class FaqItem(
    val question: String,
    val answer: String,
    val category: String
)

@Composable
fun FaqScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val colors = LocalRenovaColors.current

    // 🔹 Si cambia el idioma o configuración, recompone toda la pantalla
    key(configuration.locales[0]) {
        FaqContent(onNavigateBack = onNavigateBack, colors = colors)
    }
}

@Composable
fun FaqContent(
    onNavigateBack: () -> Unit,
    colors: com.renova.mobile.ui.theme.RenovaColorScheme
) {
    // 🔹 Lista de preguntas frecuentes traducibles
    val faqList = listOf(
        // Cuenta y Registro
        FaqItem(
            question = stringResource(R.string.faq_q2),
            answer = stringResource(R.string.faq_a2),
            category = stringResource(R.string.faq_category_account)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q3),
            answer = stringResource(R.string.faq_a3),
            category = stringResource(R.string.faq_category_account)
        ),

        // Reciclaje y Puntos
        FaqItem(
            question = stringResource(R.string.faq_q4),
            answer = stringResource(R.string.faq_a4),
            category = stringResource(R.string.faq_category_recycling)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q5),
            answer = stringResource(R.string.faq_a5),
            category = stringResource(R.string.faq_category_recycling)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q7),
            answer = stringResource(R.string.faq_a7),
            category = stringResource(R.string.faq_category_recycling)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q8),
            answer = stringResource(R.string.faq_a8),
            category = stringResource(R.string.faq_category_recycling)
        ),

        // Recompensas y Canjes
        FaqItem(
            question = stringResource(R.string.faq_q9),
            answer = stringResource(R.string.faq_a9),
            category = stringResource(R.string.faq_category_rewards)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q10),
            answer = stringResource(R.string.faq_a10),
            category = stringResource(R.string.faq_category_rewards)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q11),
            answer = stringResource(R.string.faq_a11),
            category = stringResource(R.string.faq_category_rewards)
        ),

        // Para Negocios/Alianzas
        FaqItem(
            question = stringResource(R.string.faq_q12),
            answer = stringResource(R.string.faq_a12),
            category = stringResource(R.string.faq_category_business)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q13),
            answer = stringResource(R.string.faq_a13),
            category = stringResource(R.string.faq_category_business)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q14),
            answer = stringResource(R.string.faq_a14),
            category = stringResource(R.string.faq_category_business)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q15),
            answer = stringResource(R.string.faq_a15),
            category = stringResource(R.string.faq_category_business)
        ),

        // General
        FaqItem(
            question = stringResource(R.string.faq_q16),
            answer = stringResource(R.string.faq_a16),
            category = stringResource(R.string.faq_category_general)
        ),
        FaqItem(
            question = stringResource(R.string.faq_q17),
            answer = stringResource(R.string.faq_a17),
            category = stringResource(R.string.faq_category_general)
        ),
    )

    // 🔹 Agrupar FAQs por categoría
    val groupedFaqs = faqList.groupBy { it.category }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Box {
            BusinessSectionHeader(
                title = "     ${stringResource(R.string.faq_title)}",
                onLogout = { },
                textColor = Color.White
            )

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.faq_back),
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            groupedFaqs.forEach { (category, faqs) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = RenovaColors.Primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(faqs) { faq ->
                    FaqCard(faq = faq, colors = colors)
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqCard(
    faq: FaqItem,
    colors: com.renova.mobile.ui.theme.RenovaColorScheme
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "rotation"
    )

    CompositionLocalProvider(
        LocalRippleConfiguration provides null
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = RenovaColors.Light.ActivityShadowColor
                ),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(12.dp),
            onClick = { isExpanded = !isExpanded }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = faq.question,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) stringResource(R.string.faq_collapse) else stringResource(R.string.faq_expand),
                        tint = RenovaColors.Primary,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(300)) +
                            expandVertically(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)) +
                            shrinkVertically(animationSpec = tween(300))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(
                            color = colors.textSecondary.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = faq.answer,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily
                            ),
                            color = colors.textSecondary,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                        )
                    }
                }
            }
        }
    }}