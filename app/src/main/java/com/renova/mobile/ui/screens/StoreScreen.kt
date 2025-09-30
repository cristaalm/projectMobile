package com.renova.mobile.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.ColorFilter
import androidx.navigation.NavController
import com.renova.mobile.R
import com.renova.mobile.network.Alianza
import com.renova.mobile.network.TypeShop
import com.renova.mobile.ui.screens.viewmodel.StoreViewModel
import com.renova.mobile.ui.theme.*

@Composable
fun StoreScreen(
    navController: NavController,
    viewModel: StoreViewModel = viewModel()
) {
    val colors = LocalRenovaColors.current
    var searchQuery by remember { mutableStateOf("") }
    val uiState = viewModel.uiState
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    val filteredAlianzas = remember(selectedCategoryId, searchQuery, uiState.alianzas) {
        val categoryFiltered = if (selectedCategoryId == null) {
            uiState.alianzas
        } else {
            uiState.alianzas.filter { it.type_shop_id == selectedCategoryId }
        }
        if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { alianza ->
                alianza.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                colors = colors
            )
        }

        item { DiscoverSection(colors = colors) }

        item {
            Text(
                text = stringResource(R.string.featured_stores),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        when {
            uiState.isLoading -> {
                item { LoadingSection(colors = colors) }
            }
            uiState.error != null -> {
                item {
                    ErrorSection(
                        message = uiState.error,
                        onRetry = { viewModel.retryLoading() },
                        colors = colors
                    )
                }
            }
            filteredAlianzas.isEmpty() && !uiState.isLoading -> {
                item { EmptySection(colors = colors) }
            }
            else -> {
                items(filteredAlianzas, key = { it.id }) { alianza ->
                    AlianzaCard(
                        alianza = alianza,
                        colors = colors,
                        onClick ={
                                navController.navigate("reward_screen/${alianza.id}")
                            }
                    )
                }
            }
        }

        if (uiState.categories.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.categories),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        CategoryCard(
                            category = TypeShop(id = -1, name = "Todos"),
                            isSelected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null },
                            colors = colors
                        )
                    }
                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onClick = {
                                selectedCategoryId = if (selectedCategoryId == category.id) null else category.id
                            },
                            colors = colors
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun getIconForCategory(categoryName: String): Int {
    return when {
        categoryName.contains("restaurante", ignoreCase = true) -> R.drawable.ic_restaurant
        categoryName.contains("supermercado", ignoreCase = true) -> R.drawable.ic_supermercado
        categoryName.contains("farmacia", ignoreCase = true) -> R.drawable.ic_farmacia
        categoryName.contains("ropa", ignoreCase = true) -> R.drawable.ic_ropa
        categoryName.contains("restaurant", ignoreCase = true) -> R.drawable.ic_restaurant
        categoryName.contains("gasolinera", ignoreCase = true) -> R.drawable.ic_gasolinera
        categoryName.contains("bodega", ignoreCase = true) -> R.drawable.ic_bodega
        categoryName.contains("electrónica", ignoreCase = true) -> R.drawable.ic_electronica
        categoryName.contains("tienda de libros", ignoreCase = true) -> R.drawable.ic_libro
        categoryName.contains("juguetes", ignoreCase = true) -> R.drawable.ic_juguetes
        categoryName.contains("tienda", ignoreCase = true) -> R.drawable.ic_tienda

        else -> R.drawable.ic_tienda // Un ícono genérico por si no encuentra coincidencia
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, colors: RenovaColorScheme) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_allied_stores), color = colors.textSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = colors.iconTint) },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = RenovaComponentColors.textFieldColors()
    )
}

@Composable
private fun DiscoverSection(colors: RenovaColorScheme) {
    val isDarkTheme = isSystemInDarkTheme()

    val backgroundGradient = Brush.horizontalGradient(
        colors = listOf(colors.discoverGradientStart, colors.discoverGradientEnd)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundGradient)
        ) {
            val leafTintColor = colors.textSecondary
            val leafAlpha = 0.8f

            Image(
                painter = painterResource(id = R.drawable.ic_decorative_leaf),
                contentDescription = null,
                colorFilter = ColorFilter.tint(leafTintColor.copy(alpha = leafAlpha + 0.1f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = 8.dp)
                    .size(20.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.ic_decorative_leaf),
                contentDescription = null,
                colorFilter = ColorFilter.tint(leafTintColor.copy(alpha = leafAlpha - 0.1f)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 70.dp, y = (-8).dp)
                    .size(22.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.ic_decorative_leaf),
                contentDescription = null,
                colorFilter = ColorFilter.tint(leafTintColor.copy(alpha = leafAlpha)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-40).dp, y = (-15).dp)
                    .size(28.dp)
            )


            Image(
                painter = painterResource(id = R.drawable.ic_decorative_leaf),
                contentDescription = null,
                colorFilter = ColorFilter.tint(leafTintColor.copy(alpha = leafAlpha - 0.15f)),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 50.dp)
                    .size(18.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.ic_decorative_leaf),
                contentDescription = null,
                colorFilter = ColorFilter.tint(leafTintColor.copy(alpha = leafAlpha)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = 10.dp)
                    .size(35.dp)
            )

            Column(
                modifier = Modifier
                    .padding(vertical = 20.dp, horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.discover_products),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.discover_stores_description),
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun AlianzaCard(alianza: Alianza, colors: RenovaColorScheme, onClick: () -> Unit) {
    val logoColors = listOf(RenovaColors.Primary, Color(0xFFE1BEE7), RenovaColors.Info, RenovaColors.Warning, RenovaColors.Error)
    val logoColor = logoColors[alianza.id % logoColors.size]
    val isDarkTheme = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (isDarkTheme) {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) // Borde blanco muy sutil
        } else {
            null // Sin borde en modo claro
        }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(logoColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alianza.name.firstOrNull()?.uppercase() ?: "R",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alianza.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = alianza.contact_name,
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!alianza.address.isNullOrBlank()) {
                    Text(
                        text = alianza.address,
                        fontSize = 12.sp,
                        color = RenovaColors.Primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(">", fontSize = 18.sp, color = colors.textSecondary)
        }
    }
}

@Composable
private fun CategoryCard(
    category: TypeShop,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: RenovaColorScheme
) {
    val cardColors = listOf(
        Color(0xFFFF9800), Color(0xFFF44336), Color(0xFF00BCD4),
        Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFF3F51B5)
    )
    val cardColor = if (category.id == -1) RenovaColors.Primary else cardColors[category.id % cardColors.size]

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 80.dp)
            .scale(scale)
            .alpha(if (isSelected) 1f else 0.4f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        // Obtenemos el ID del recurso del icono
        val iconResId = if (category.id == -1) R.drawable.ic_all else getIconForCategory(category.name)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = category.name,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun LoadingSection(colors: RenovaColorScheme) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = RenovaColors.Primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.loading_stores), color = colors.textSecondary)
        }
    }
}

@Composable
fun ErrorSection(message: String, onRetry: () -> Unit, colors: RenovaColorScheme) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RenovaColors.Error.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.error_loading_stores),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = RenovaColors.Error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, fontSize = 14.sp, color = RenovaColors.Error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = RenovaColors.Primary)
            ) {
                Text(stringResource(R.string.retry), color = Color.White)
            }
        }
    }
}

@Composable
fun EmptySection(colors: RenovaColorScheme) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.no_stores_available),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = stringResource(R.string.no_stores_found),
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}