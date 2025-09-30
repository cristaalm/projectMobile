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
import com.renova.mobile.ui.theme.RenovaColors

@Composable
fun QRScreen() {
    val currentPoints = 2450
    val nameUser = "Brisa Medina"
    val id = 24

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
                    spotColor = RenovaColors.Light.ShadowColor
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
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "$currentPoints ${stringResource(id = R.string.points_unit)}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = stringResource(id = R.string.current_points),
                                color = Color.White,
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
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(id = R.string.share_code),
                                tint = Color.White
                            )
                        }
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.5f),
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
                            id.toString(),
                            null,
                            QRGContents.Type.TEXT,
                            500
                        )
                        qrEncoder.colorBlack = RenovaColors.Primary.hashCode()
                        qrEncoder.colorWhite = android.graphics.Color.WHITE // transparente

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
                            containerColor = Color.White,
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
                    spotColor = RenovaColors.Light.ShadowColor
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = RenovaColors.Light.Surface,
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

                // 4. Indicador de desplazamiento SUPERIOR (Fading out to the top)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp) // Altura de la sombra
                        .align(Alignment.TopCenter) // Se coloca en la parte superior
                        .alpha(topAlpha) // Aplicamos la opacidad animada
                        // Degradado de la superficie al transparente (para ocultar el inicio)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    RenovaColors.Light.Surface, // Color de la superficie
                                    RenovaColors.Light.Surface.copy(alpha = 0f) // Transparente
                                ),
                                // Invertimos el rango para que el opaco esté en la parte superior
                                startY = 0f,
                                endY = 100f
                            )
                        )
                )

                // 5. Indicador de desplazamiento INFERIOR (Flecha animada apuntando hacia abajo)
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
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQRScreen() {
    QRScreen()
}