package com.renova.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.ui.theme.LocalRenovaColors
import java.text.SimpleDateFormat
import java.util.*

// Modelo de datos
data class ActivityItem(
    val actividad: String,
    val tipo_reciclaje: String?,
    val puntos: Int,
    val fecha: String,
    val descripcion: String,
    val comercio: String? = null
)

// Modificador personalizado para sombra verde
fun Modifier.greenShadow(
    color: Color = Color(0xFF4CAF50),
    alpha: Float = 0.15f,
    borderRadius: Dp = 16.dp,
    shadowRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp
) = this.drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparent = color.copy(alpha = 0f).toArgb()

    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparent
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}

@Composable
fun ActivityScreen() {
    val renovaColors = LocalRenovaColors.current
    var currentPage by remember { mutableStateOf(0) }
    val itemsPerPage = 5
    val fakeData = getFakeActivityItems()
    val totalPages = (fakeData.size + itemsPerPage - 1) / itemsPerPage
    val totalPlastic = fakeData.count { it.tipo_reciclaje == "plastico" }
    val totalAluminum = fakeData.count { it.tipo_reciclaje == "aluminio" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(renovaColors.activityBackground)
    ) {
        // Encabezado
        Text(
            text = stringResource(R.string.recycling_materials),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = renovaColors.activityPrimary,
            modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
        )

        // Cuadros de materiales con sombras verdes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MaterialStatCard(
                title = stringResource(R.string.plastic),
                count = totalPlastic,
                icon = R.drawable.bottle,
                cardColor = renovaColors.plasticCardBackground,
                iconTint = renovaColors.activityPrimary,
                cardWidth = 120.dp,
                cardHeight = 150.dp
            )
            Spacer(modifier = Modifier.width(24.dp))
            MaterialStatCard(
                title = stringResource(R.string.aluminum),
                count = totalAluminum,
                icon = R.drawable.can,
                cardColor = renovaColors.aluminumCardBackground,
                iconTint = renovaColors.aluminumIconTint,
                cardWidth = 120.dp,
                cardHeight = 150.dp
            )
        }

        // Total de materiales
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.total_materials),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = renovaColors.activityPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${totalPlastic + totalAluminum}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = renovaColors.activityCardBackground,
                modifier = Modifier
                    .background(renovaColors.activityPrimary, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        // Historial
        Text(
            text = stringResource(R.string.history),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = renovaColors.activityPrimary,
            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
        )

        // Lista paginada
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val startIndex = currentPage * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, fakeData.size)
            items(endIndex - startIndex) { index ->
                val item = fakeData[startIndex + index]
                ActivityMockupCardFull(item = item)
            }
        }

        // Paginación con iconos y colores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (currentPage > 0) currentPage-- },
                enabled = currentPage > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPage > 0) renovaColors.buttonEnabled else renovaColors.buttonDisabled,
                    contentColor = renovaColors.activityCardBackground
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = stringResource(R.string.previous),
                    tint = renovaColors.activityCardBackground,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.previous), color = renovaColors.activityCardBackground)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${currentPage + 1} ${stringResource(R.string.of)} $totalPages",
                modifier = Modifier.align(Alignment.CenterVertically),
                color = renovaColors.activityPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { if (currentPage < totalPages - 1) currentPage++ },
                enabled = currentPage < totalPages - 1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPage < totalPages - 1) renovaColors.buttonEnabled else renovaColors.buttonDisabled,
                    contentColor = renovaColors.activityCardBackground
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.next), color = renovaColors.activityCardBackground)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.next),
                    contentDescription = stringResource(R.string.next),
                    tint = renovaColors.activityCardBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MaterialStatCard(
    title: String,
    count: Int,
    icon: Int,
    cardColor: Color,
    iconTint: Color,
    cardWidth: Dp = 100.dp,
    cardHeight: Dp = 140.dp
) {
    val renovaColors = LocalRenovaColors.current

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .greenShadow(
                color = renovaColors.shadowColor,
                alpha = 0.2f,
                shadowRadius = 6.dp,
                offsetY = 3.dp
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 18.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconTint)
            )
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = iconTint,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = count.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = iconTint,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ActivityMockupCardFull(item: ActivityItem) {
    val renovaColors = LocalRenovaColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .greenShadow(
                color = renovaColors.shadowColor,
                alpha = 0.15f,
                shadowRadius = 8.dp,
                offsetY = 4.dp
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = renovaColors.activityCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(45.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(50))
                        .background(renovaColors.plasticCardBackground)
                )
                val logoRes = when {
                    item.actividad == "canjeo_puntos" -> R.drawable.ic_launcher_foreground
                    item.tipo_reciclaje == "plastico" -> R.drawable.bottle
                    item.tipo_reciclaje == "aluminio" -> R.drawable.can
                    else -> R.drawable.ic_launcher_foreground
                }
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = item.comercio ?: "Logo",
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (item.actividad == "canjeo_puntos") {
                    Text(
                        text = "Canjeo de Puntos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = renovaColors.activityPrimary
                    )
                    val cleanDescription = item.descripcion.replace("Canjeo de Puntos\n", "").trim()
                    if (cleanDescription.isNotEmpty()) {
                        Text(
                            text = cleanDescription,
                            fontSize = 14.sp,
                            color = renovaColors.activitySecondary,
                            lineHeight = 18.sp
                        )
                    }
                    Text(
                        text = formatFriendlyDate(item.fecha),
                        fontSize = 13.sp,
                        color = renovaColors.activitySecondary
                    )
                } else {
                    Text(
                        text = item.descripcion,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = renovaColors.activityPrimary
                    )
                    Text(
                        text = formatFriendlyDate(item.fecha),
                        fontSize = 13.sp,
                        color = renovaColors.activitySecondary
                    )
                }
            }
            Text(
                text = "${if (item.puntos >= 0) "+" else ""}${item.puntos} pts",
                fontWeight = FontWeight.Bold,
                color = if (item.puntos >= 0) renovaColors.positivePoints else renovaColors.negativePoints,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

private fun formatFriendlyDate(isoString: String): String {
    val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val date = try { sdfInput.parse(isoString) } catch (_: Exception) { null }
    if (date == null) return isoString
    val calendar = Calendar.getInstance()
    val calDate = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    return when {
        calDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) ->
            "Hoy, ${timeFormat.format(date)}"
        calDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) ->
            "Ayer, ${timeFormat.format(date)}"
        else -> {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy, h:mm a", Locale.getDefault())
            dateFormat.format(date)
        }
    }
}

private fun getFakeActivityItems(): List<ActivityItem> {
    return listOf(
        ActivityItem(
            actividad = "suma_puntos",
            tipo_reciclaje = "plastico",
            puntos = 75,
            fecha = "2025-09-28T14:30:00Z",
            descripcion = "Botella PET"
        ),
        ActivityItem(
            actividad = "suma_puntos",
            tipo_reciclaje = "aluminio",
            puntos = 36,
            fecha = "2025-09-28T11:15:00Z",
            descripcion = "Lata Aluminio"
        ),
        ActivityItem(
            actividad = "canjeo_puntos",
            tipo_reciclaje = null,
            puntos = -250,
            fecha = "2025-09-28T12:00:00Z",
            descripcion = "Canjeo de Puntos\nOxxo - Recarga de saldo",
            comercio = "Oxxo"
        ),
        ActivityItem(
            actividad = "suma_puntos",
            tipo_reciclaje = "plastico",
            puntos = 5,
            fecha = "2025-09-27T09:45:00Z",
            descripcion = "Botella PET"
        ),
        ActivityItem(
            actividad = "suma_puntos",
            tipo_reciclaje = "aluminio",
            puntos = 28,
            fecha = "2025-09-26T16:20:00Z",
            descripcion = "Lata Aluminio"
        ),
        ActivityItem(
            actividad = "canjeo_puntos",
            tipo_reciclaje = null,
            puntos = -150,
            fecha = "2025-09-26T10:30:00Z",
            descripcion = "Canjeo de Puntos\n7-Eleven - Producto",
            comercio = "7-Eleven"
        )
    )
}