package com.renova.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.renova.mobile.R
import com.renova.mobile.ui.theme.*

// Definir la fuente Poppins
val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun TermsAndConditionsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.renovaColors

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header (icono a la izquierda, título a la derecha)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = stringResource(id = R.string.terms_dialog_close),
                                    tint = Color.Unspecified
                                )
                            }

                            Text(
                                text = stringResource(id = R.string.terms_dialog_title),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PoppinsFontFamily,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = colors.textSecondary.copy(alpha = 0.3f)
                    )

                    // Contenido
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = stringResource(id = R.string.terms_section_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFontFamily,
                            color = RenovaColors.Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = stringResource(id = R.string.terms_content),
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily,
                            color = colors.textPrimary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(id = R.string.privacy_section_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFontFamily,
                            color = RenovaColors.Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = stringResource(id = R.string.privacy_content),
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily,
                            color = colors.textPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
