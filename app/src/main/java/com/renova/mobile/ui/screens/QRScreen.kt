package com.renova.mobile.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.utils.SessionManager
import com.renova.mobile.repository.LoginRepository

@Composable
fun QRScreen() {
    val currentPoints = 2450
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val loginRepository = remember { LoginRepository() }
    
    // Obtener datos del usuario logueado
    val user = sessionManager.getUser()
    val accessToken = sessionManager.getAccessToken()
    
    // Generar código único para QR
    val qrCode = remember(user, accessToken) {
        if (user != null && !accessToken.isNullOrBlank()) {
            loginRepository.generateUniqueQRCode(accessToken, user.id)
        } else {
            "0" // Fallback al ID hardcodeado si no hay datos de sesión
        }
    }
    
    val nameUser = user?.name ?: "Usuario no identificado"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Encabezado and Ticket principal are unchanged) ...

        // ---- Encabezado ----
        Text(
            text = stringResource(id = R.string.mycode),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                fontWeight = FontWeight.ExtraBold
            ),
            color = RenovaColors.Primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 15.dp)
        )

        // ---- Ticket principal ----
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 6f)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = RenovaColors.Light.ActivityShadowColor
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Transparent) // mantiene bordes redondeados
                    .paint(
                        painter = painterResource(id = R.drawable.fondo_chico),
                        contentScale = ContentScale.Crop
                    )
                    .padding(15.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ---- Fila 1: datos de usuario ----
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = nameUser,
                                color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else Color.White,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "$currentPoints ${stringResource(id = R.string.points_unit)}",
                                color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else Color.White,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = stringResource(id = R.string.current_points),
                                color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        IconButton(
                            onClick = { /* handle share */ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(id = R.string.share_code),
                                //tint = Color.White
                                tint = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else Color.White

                            )
                        }
                    }

                    Divider(
                        color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // ---- Fila 2: QR ----
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val qrEncoder = QRGEncoder(
                            qrCode,
                            null,
                            QRGContents.Type.TEXT,
                            500
                        )
                        qrEncoder.colorBlack = RenovaColors.Primary.hashCode()
                        qrEncoder.colorWhite = if (androidx.compose.foundation.isSystemInDarkTheme()) android.graphics.Color.BLACK else android.graphics.Color.WHITE // transparente

                        val qrBitmap: Bitmap = qrEncoder.bitmap

                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }

                    // ---- Fila 3: botón compartir ----
                    Button(
                        onClick = { /* handle share */ },
                        modifier = Modifier
                            .fillMaxWidth() // ahora ocupa todo el ancho
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else Color.White,
                            contentColor = RenovaColors.Primary
                        ),
                        shape = RoundedCornerShape(25.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = RenovaColors.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.share_code),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---- Instrucciones ----
        // 1. Crear y recordar el estado del desplazamiento
        val scrollState = rememberScrollState()

        // 2. Determinar si el desplazamiento ha llegado al inicio y al final
        val isScrolledToStart by remember {
            derivedStateOf {
                scrollState.value == 0
            }
        }
        val isScrolledToEnd by remember {
            derivedStateOf {
                // If max is 0, it's not scrollable, so consider it "ended"
                scrollState.maxValue == 0 || scrollState.value >= scrollState.maxValue
            }
        }

        // 3. Animar la opacidad para los indicadores de desplazamiento (sombra degradada)
        // El alpha del top gradient será 0.0 si está al inicio, y 1.0 en caso contrario.
        val topAlpha by animateFloatAsState(
            targetValue = if (isScrolledToStart) 0f else 1f,
            label = "topScrollIndicatorAlpha"
        )

        // El alpha del bottom gradient será 0.0 si está al final, y 1.0 en caso contrario.
        val bottomAlpha by animateFloatAsState(
            targetValue = if (isScrolledToEnd) 0f else 1f,
            label = "bottomScrollIndicatorAlpha"
        )

        // Animación para el movimiento de la flecha hacia abajo
        val arrowOffset by animateFloatAsState(
            targetValue = if (isScrolledToEnd) 0f else 1f,
            animationSpec = if (isScrolledToEnd) tween(300) else infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "arrowMovement"
        )

        // Usamos Card y Box para aplicar el fondo, el contenido scrollable y superponer los gradientes.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                // Limitar la altura para que pueda scrollear
                .heightIn(max = 200.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else RenovaColors.Light.ActivityShadowColor
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else RenovaColors.Light.Surface,
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Contenido scrollable
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        // Aplicamos el estado de scroll
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)

                ) {
                    Text(
                        text = stringResource(id = R.string.instructions_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = RenovaColors.Primary,
                    )

                    InstructionItem(
                        text = stringResource(id = R.string.instruction_1),
                        icon = Icons.Default.CameraAlt
                    )
                    InstructionItem(
                        text = stringResource(id = R.string.instruction_2),
                        icon = Icons.Default.Info
                    )
                    InstructionItem(
                        text = stringResource(id = R.string.instruction_3),
                        icon = Icons.Default.Info
                    )
                    // **NOTA**: Agrega más InstructionItem si necesitas que la columna sea
                    // lo suficientemente larga para que se pueda scrollear.
                    // InstructionItem(text = "Instrucción extra para forzar scroll", icon = Icons.Default.Info)
                    // InstructionItem(text = "Última instrucción", icon = Icons.Default.Info)
                }

                // Indicador de desplazamiento INFERIOR (Flecha animada apuntando hacia abajo)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .alpha(bottomAlpha)
                        .padding(bottom = 8.dp, end = 16.dp)
                        .offset(y = (arrowOffset * 5).dp) // Movimiento vertical de la flecha
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Scroll hacia abajo",
                        tint = RenovaColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InstructionItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RenovaColors.Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                fontWeight = FontWeight.Medium
            ),
            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White else Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQRScreen() {
    QRScreen()
}