package com.renova.mobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renova.mobile.R
import com.renova.mobile.network.UserData
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.viewmodels.BusinessProfileViewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.network.Reward
import androidx.compose.ui.res.stringResource
import com.renova.mobile.ui.screens.RewardCard
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate

// Función helper para traducir categorías
@Composable
fun getCategoryTranslation(categoryName: String?, isSpanish: Boolean): String {
    if (categoryName == null) return "-"

    // Normalizar el nombre para comparación
    val normalized = categoryName.lowercase().trim()

    return when {
        // Restaurante
        normalized.contains("restaurante") || normalized.contains("restaurant") || normalized.contains("comida") || normalized.contains("food") ->
            if (isSpanish) "Restaurante" else "Restaurant"

        // Cafetería (antes que tienda/store para evitar conflictos)
        normalized.contains("cafetería") || normalized.contains("cafeteria") || normalized.contains("coffee") || normalized.contains("café") || normalized.contains("cafe") ->
            if (isSpanish) "Cafetería" else "Coffee Shop"

        // Panadería
        normalized.contains("panadería") || normalized.contains("panaderia") || normalized.contains("bakery") || normalized.contains("pan") || normalized.contains("bread") ->
            if (isSpanish) "Panadería" else "Bakery"

        // Farmacia
        normalized.contains("farmacia") || normalized.contains("pharmacy") || normalized.contains("drug") ->
            if (isSpanish) "Farmacia" else "Pharmacy"

        // Supermercado
        normalized.contains("supermercado") || normalized.contains("supermarket") || normalized.contains("abarrotes") || normalized.contains("grocery") ->
            if (isSpanish) "Supermercado" else "Supermarket"

        // Ropa
        normalized.contains("ropa") || normalized.contains("clothing") || normalized.contains("fashion") || normalized.contains("moda") || normalized.contains("vestir") ->
            if (isSpanish) "Ropa" else "Clothing"

        // Electrónica
        normalized.contains("electrónica") || normalized.contains("electronica") || normalized.contains("electronics") || normalized.contains("tecnología") || normalized.contains("tecnologia") ->
            if (isSpanish) "Electrónica" else "Electronics"

        // Librería
        normalized.contains("librería") || normalized.contains("libreria") || normalized.contains("bookstore") || normalized.contains("book") || normalized.contains("libros") ->
            if (isSpanish) "Librería" else "Bookstore"

        // Ferretería
        normalized.contains("ferretería") || normalized.contains("ferreteria") || normalized.contains("hardware") ->
            if (isSpanish) "Ferretería" else "Hardware Store"

        // Salud/Hospital
        normalized.contains("salud") || normalized.contains("health") || normalized.contains("hospital") || normalized.contains("clínica") || normalized.contains("clinica") ->
            if (isSpanish) "Salud" else "Health"

        // Belleza/Spa
        normalized.contains("belleza") || normalized.contains("beauty") || normalized.contains("spa") || normalized.contains("estética") || normalized.contains("estetica") ->
            if (isSpanish) "Belleza" else "Beauty"

        // Deportes/Gimnasio
        normalized.contains("deportes") || normalized.contains("sports") || normalized.contains("gym") || normalized.contains("gimnasio") || normalized.contains("fitness") ->
            if (isSpanish) "Deportes" else "Sports"

        // Mascotas
        normalized.contains("mascotas") || normalized.contains("pets") || normalized.contains("pet") || normalized.contains("veterinaria") || normalized.contains("veterinary") ->
            if (isSpanish) "Mascotas" else "Pets"

        // Juguetería
        normalized.contains("juguetería") || normalized.contains("jugueteria") || normalized.contains("toys") || normalized.contains("toy") || normalized.contains("juguetes") ->
            if (isSpanish) "Juguetería" else "Toy Store"

        // Muebles
        normalized.contains("muebles") || normalized.contains("furniture") || normalized.contains("mueblería") || normalized.contains("muebleria") ->
            if (isSpanish) "Muebles" else "Furniture"

        // Joyería
        normalized.contains("joyería") || normalized.contains("joyeria") || normalized.contains("jewelry") || normalized.contains("joyas") ->
            if (isSpanish) "Joyería" else "Jewelry"

        // Óptica
        normalized.contains("óptica") || normalized.contains("optica") || normalized.contains("optics") || normalized.contains("optical") || normalized.contains("lentes") ->
            if (isSpanish) "Óptica" else "Optical"

        // Floristería
        normalized.contains("floristería") || normalized.contains("floristeria") || normalized.contains("flowers") || normalized.contains("florist") || normalized.contains("flores") ->
            if (isSpanish) "Floristería" else "Florist"

        // Gasolinera
        normalized.contains("gasolinera") || normalized.contains("gas station") || normalized.contains("fuel") || normalized.contains("combustible") ->
            if (isSpanish) "Gasolinera" else "Gas Station"

        // Hotel
        normalized.contains("hotel") || normalized.contains("hospedaje") || normalized.contains("lodging") ->
            if (isSpanish) "Hotel" else "Hotel"

        // Bar
        normalized.contains("bar") || normalized.contains("cantina") ->
            if (isSpanish) "Bar" else "Bar"

        // Lavandería
        normalized.contains("lavandería") || normalized.contains("lavanderia") || normalized.contains("laundry") || normalized.contains("tintorería") || normalized.contains("tintoreria") ->
            if (isSpanish) "Lavandería" else "Laundry"

        // Zapatería
        normalized.contains("zapatería") || normalized.contains("zapateria") || normalized.contains("shoes") || normalized.contains("zapatos") || normalized.contains("calzado") ->
            if (isSpanish) "Zapatería" else "Shoe Store"

        // Papelería
        normalized.contains("papelería") || normalized.contains("papeleria") || normalized.contains("stationery") || normalized.contains("útiles") || normalized.contains("utiles") ->
            if (isSpanish) "Papelería" else "Stationery"

        // Banco
        normalized.contains("banco") || normalized.contains("bank") ->
            if (isSpanish) "Banco" else "Bank"

        // Pizzería
        normalized.contains("pizzería") || normalized.contains("pizzeria") || normalized.contains("pizza") ->
            if (isSpanish) "Pizzería" else "Pizzeria"

        // Carnicería
        normalized.contains("carnicería") || normalized.contains("carniceria") || normalized.contains("butcher") || normalized.contains("carne") || normalized.contains("meat") ->
            if (isSpanish) "Carnicería" else "Butcher"

        // Peluquería/Barbería
        normalized.contains("peluquería") || normalized.contains("peluqueria") || normalized.contains("barbería") || normalized.contains("barberia") || normalized.contains("salon") || normalized.contains("barber") || normalized.contains("hair") ->
            if (isSpanish) "Peluquería" else "Hair Salon"

        // Taller/Mecánico
        normalized.contains("taller") || normalized.contains("mecánico") || normalized.contains("mecanico") || normalized.contains("mechanic") || normalized.contains("auto") ->
            if (isSpanish) "Taller Mecánico" else "Auto Repair"

        // Tienda general (solo si contiene "tienda" o "store" genérico)
        normalized == "tienda" || normalized == "store" || normalized == "shop" ->
            if (isSpanish) "Tienda" else "Store"

        // Si no hay coincidencia exacta pero contiene "tienda" o "store", intentar retornar el original
        else -> categoryName // Retornar el valor original del backend
    }
}

// Función helper para obtener el icono según la categoría
@Composable
fun getCategoryIcon(categoryName: String?): ImageVector {
    if (categoryName == null) return Icons.Default.StoreMallDirectory

    // Normalizar el nombre para comparación
    val normalized = categoryName.lowercase().trim()

    return when {
        // Restaurante
        normalized.contains("restaurante") || normalized.contains("restaurant") || normalized.contains("comida") || normalized.contains("food") ->
            Icons.Default.Restaurant

        // Cafetería
        normalized.contains("cafetería") || normalized.contains("cafeteria") || normalized.contains("coffee") || normalized.contains("café") || normalized.contains("cafe") ->
            Icons.Default.LocalCafe

        // Panadería
        normalized.contains("panadería") || normalized.contains("panaderia") || normalized.contains("bakery") || normalized.contains("pan") ->
            Icons.Default.Cake

        // Farmacia
        normalized.contains("farmacia") || normalized.contains("pharmacy") ->
            Icons.Default.LocalPharmacy

        // Supermercado
        normalized.contains("supermercado") || normalized.contains("supermarket") || normalized.contains("abarrotes") || normalized.contains("grocery") ->
            Icons.Default.ShoppingCart

        // Ropa
        normalized.contains("ropa") || normalized.contains("clothing") || normalized.contains("fashion") || normalized.contains("moda") ->
            Icons.Default.Checkroom

        // Electrónica
        normalized.contains("electrónica") || normalized.contains("electronica") || normalized.contains("electronics") || normalized.contains("tecnología") ->
            Icons.Default.Devices

        // Librería
        normalized.contains("librería") || normalized.contains("libreria") || normalized.contains("bookstore") || normalized.contains("book") || normalized.contains("libros") ->
            Icons.Default.MenuBook

        // Ferretería
        normalized.contains("ferretería") || normalized.contains("ferreteria") || normalized.contains("hardware") ->
            Icons.Default.Build

        // Salud/Hospital
        normalized.contains("salud") || normalized.contains("health") || normalized.contains("hospital") || normalized.contains("clínica") ->
            Icons.Default.LocalHospital

        // Belleza/Spa
        normalized.contains("belleza") || normalized.contains("beauty") || normalized.contains("spa") ->
            Icons.Default.Spa

        // Deportes
        normalized.contains("deportes") || normalized.contains("sports") || normalized.contains("gym") || normalized.contains("gimnasio") || normalized.contains("fitness") ->
            Icons.Default.FitnessCenter

        // Mascotas
        normalized.contains("mascotas") || normalized.contains("pets") || normalized.contains("pet") || normalized.contains("veterinaria") ->
            Icons.Default.Pets

        // Juguetería
        normalized.contains("juguetería") || normalized.contains("jugueteria") || normalized.contains("toys") || normalized.contains("toy") || normalized.contains("juguetes") ->
            Icons.Default.Toys

        // Muebles
        normalized.contains("muebles") || normalized.contains("furniture") ->
            Icons.Default.Chair

        // Joyería
        normalized.contains("joyería") || normalized.contains("joyeria") || normalized.contains("jewelry") ->
            Icons.Default.Diamond

        // Óptica
        normalized.contains("óptica") || normalized.contains("optica") || normalized.contains("optics") || normalized.contains("lentes") ->
            Icons.Default.Visibility

        // Floristería
        normalized.contains("floristería") || normalized.contains("floristeria") || normalized.contains("flowers") || normalized.contains("flores") ->
            Icons.Default.LocalFlorist

        // Gasolinera
        normalized.contains("gasolinera") || normalized.contains("gas station") || normalized.contains("fuel") ->
            Icons.Default.LocalGasStation

        // Hotel
        normalized.contains("hotel") ->
            Icons.Default.Hotel

        // Bar
        normalized.contains("bar") || normalized.contains("cantina") ->
            Icons.Default.LocalBar

        // Lavandería
        normalized.contains("lavandería") || normalized.contains("lavanderia") || normalized.contains("laundry") ->
            Icons.Default.LocalLaundryService

        // Zapatería
        normalized.contains("zapatería") || normalized.contains("zapateria") || normalized.contains("shoes") || normalized.contains("zapatos") ->
            Icons.Default.ShoppingBag

        // Papelería
        normalized.contains("papelería") || normalized.contains("papeleria") || normalized.contains("stationery") ->
            Icons.Default.Edit

        // Banco
        normalized.contains("banco") || normalized.contains("bank") ->
            Icons.Default.AccountBalance

        // Pizzería
        normalized.contains("pizzería") || normalized.contains("pizzeria") || normalized.contains("pizza") ->
            Icons.Default.LocalPizza

        // Carnicería
        normalized.contains("carnicería") || normalized.contains("carniceria") || normalized.contains("butcher") || normalized.contains("carne") ->
            Icons.Default.Restaurant

        // Peluquería
        normalized.contains("peluquería") || normalized.contains("peluqueria") || normalized.contains("barbería") || normalized.contains("barberia") || normalized.contains("salon") || normalized.contains("hair") ->
            Icons.Default.ContentCut

        // Taller mecánico
        normalized.contains("taller") || normalized.contains("mecánico") || normalized.contains("mecanico") || normalized.contains("mechanic") || normalized.contains("auto") ->
            Icons.Default.CarRepair

        // Tienda genérica
        normalized.contains("tienda") || normalized.contains("store") || normalized.contains("shop") ->
            Icons.Default.ShoppingBag

        else -> Icons.Default.StoreMallDirectory
    }
}

@Composable
fun BusinessInfoCard(
    user: UserData,
    isSpanish: Boolean,
    viewModel: BusinessProfileViewModel
) {
    val colors = LocalRenovaColors.current
    val categoryIcon = getCategoryIcon(user.alliance?.type_shop?.name)
    val translatedCategory = getCategoryTranslation(user.alliance?.type_shop?.name, isSpanish)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = colors.primaryColor
        )
    ) {
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.primaryColor.copy(alpha = 0.125f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = colors.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = stringResource(R.string.business_info),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BusinessProfileField(
                    label = stringResource(R.string.name),
                    value = user.alliance?.name,
                    icon = Icons.Default.Store
                )

                BusinessProfileField(
                    label = stringResource(R.string.email),
                    value = user.email,
                    icon = Icons.Default.Email
                )

                BusinessProfileField(
                    label = stringResource(R.string.phone_number),
                    value = user.alliance?.phone,
                    icon = Icons.Default.Phone
                )

                BusinessProfileField(
                    label = stringResource(R.string.address),
                    value = user.alliance?.address,
                    icon = Icons.Default.LocationOn
                )

                BusinessProfileField(
                    label = stringResource(R.string.category),
                    value = translatedCategory,
                    icon = categoryIcon
                )
            }
        }
    }
}

@Composable
fun RewardsSection(
    rewards: List<Reward>,
    onRewardClick: (Reward) -> Unit
) {
    val colors = LocalRenovaColors.current
    var currentPage by remember { mutableStateOf(1) }
    val rewardsPerPage = 5
    val totalPages = (rewards.size + rewardsPerPage - 1) / rewardsPerPage

    val currentPageRewards = remember(currentPage, rewards) {
        val startIndex = (currentPage - 1) * rewardsPerPage
        val endIndex = minOf(startIndex + rewardsPerPage, rewards.size)
        if (startIndex < rewards.size) {
            rewards.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }
    var isExpanded by remember { mutableStateOf(false) }

    // Animación de la flecha
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = colors.primaryColor
        )
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                color = colors.primaryColor.copy(alpha = 0.125f)
            ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = colors.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.rewards_available),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = colors.primaryColor,
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(tween(300)),
                    exit = shrinkVertically(tween(300))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        currentPageRewards.forEachIndexed { index, reward ->
                            val globalIndex = (currentPage - 1) * rewardsPerPage + index
                            val cardBackgroundColor =
                                colors.rewardCardBackgrounds[globalIndex % colors.rewardCardBackgrounds.size]
                            RewardCard(
                                reward = reward,
                                backgroundColor = cardBackgroundColor,
                                isBusiness = true,
                                onClick = { onRewardClick(reward) }
                            )
                        }
                        if (totalPages > 1) {
                            Pagination(
                                currentPage = currentPage,
                                totalPages = totalPages,
                                isLoading = false,
                                RenovaColors = colors,
                                onPreviousPage = { currentPage = maxOf(1, currentPage - 1) },
                                onNextPage = { currentPage = minOf(totalPages, currentPage + 1) }
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
        }
    }
}

@Composable
fun BusinessProfileField(
    label: String,
    value: String?,
    icon: ImageVector
) {
    val colors = LocalRenovaColors.current

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Pagination(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    RenovaColors: RenovaColorScheme,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentPage > 1) MaterialTheme.colorScheme.secondary else RenovaColors.buttonDisabled,
                contentColor = RenovaColors.textSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = stringResource(R.string.previous),
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(stringResource(R.string.previous), color = MaterialTheme.colorScheme.background)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$currentPage ${stringResource(R.string.of)} $totalPages",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.CenterVertically),
            color = RenovaColors.textPrimary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentPage < totalPages) MaterialTheme.colorScheme.secondary else RenovaColors.buttonDisabled,
                contentColor = RenovaColors.textSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
        ) {
            Text(stringResource(R.string.next), color = MaterialTheme.colorScheme.background)
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                painter = painterResource(id = R.drawable.next),
                contentDescription = stringResource(R.string.next),
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}