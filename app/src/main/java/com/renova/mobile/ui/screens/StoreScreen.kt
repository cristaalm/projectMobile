package com.renova.mobile.ui.screens

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.renova.mobile.R
import com.renova.mobile.network.Alianza
import com.renova.mobile.network.TypeShop
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.screens.viewmodel.StoreViewModel
import com.renova.mobile.ui.theme.*

@Composable
fun StoreScreen(
    navController: NavController,
    viewModel: StoreViewModel = viewModel()
) {
    // ... (el código de remember, val activeCategories, etc., se mantiene igual)
    val colors = LocalRenovaColors.current
    var searchQuery by remember { mutableStateOf("") }
    val uiState = viewModel.uiState
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    val activeCategories = remember(uiState.categories, uiState.alianzas) {
        uiState.categories.filter { category ->
            uiState.alianzas.any { it.type_shop_id == category.id }
        }
    }

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SectionHeader(title = "Tienda")

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
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
            // ... (el resto del contenido de la LazyColumn se mantiene igual)
            item { DiscoverSection(colors = colors) }

            if (activeCategories.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.categories),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFontFamily,
                            color = colors.textPrimary
                        )
                        Text(
                            text = stringResource(R.string.store_subtitle),
                            fontSize = 13.sp,
                            fontFamily = PoppinsFontFamily,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.height(200.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val cardColor = RenovaColors.Primary
                            CategoryCard(
                                category = TypeShop(id = -1, name = stringResource(R.string.all_categories)),
                                isSelected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                cardColor = cardColor
                            )
                        }

                        items(activeCategories.take(7)) { category ->
                            val cardColor = colors.categoryCardBackgrounds[category.id % colors.categoryCardBackgrounds.size]
                            CategoryCard(
                                category = category,
                                isSelected = selectedCategoryId == category.id,
                                onClick = {
                                    selectedCategoryId = if (selectedCategoryId == category.id) null else category.id
                                },
                                cardColor = cardColor
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    Text(
                        text = stringResource(R.string.featured_stores),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily,
                        color = colors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.store_rewards_subtitle),
                        fontSize = 13.sp,
                        fontFamily = PoppinsFontFamily,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            when {
                uiState.isLoading -> item { LoadingSection(colors = colors) }
                uiState.error != null -> item { ErrorSection(message = uiState.error, onRetry = { viewModel.retryLoading() }, colors = colors) }
                filteredAlianzas.isEmpty() -> item { EmptySection(colors = colors) }
                else -> {
                    itemsIndexed(filteredAlianzas, key = { _, alianza -> alianza.id }) { index, alianza ->
                        val logoColor = colors.allianceLogoBackgrounds[alianza.id % colors.allianceLogoBackgrounds.size]
                        Column {
                            AlianzaCard(
                                alianza = alianza,
                                categories = uiState.categories,
                                colors = colors,
                                logoColor = logoColor,
                                onClick = { navController.navigate("reward_screen/${alianza.id}") }
                            )

                            if (index < filteredAlianzas.size - 1) {
                                Divider(
                                    color = RenovaColors.PrimaryColor,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, colors: RenovaColorScheme) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        // << CAMBIO 1: Añadimos un Modifier.border >>
        // Este dibujará nuestro nuevo borde más grueso por fuera.
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp, // Aquí puedes ajustar el grosor que quieras
                color = Color(0xFF07B460),
                shape = RoundedCornerShape(24.dp)
            ),
        placeholder = {
            Text(
                stringResource(R.string.search_allied_stores),
                color = colors.textSecondary,
                fontFamily = PoppinsFontFamily
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = stringResource(R.string.search_icon_content_description),
                tint = Color(0xFF07B460)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            // << CAMBIO 2: Ocultamos el borde original del componente >>
            // Al hacerlo transparente, solo se verá el borde que dibujamos con el Modifier.
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,

            // Mantenemos el resto de los colores
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = RenovaColors.PrimaryColor,
            // Hacemos el fondo transparente para que no haya colores extraños
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        )
    )
}


@Composable
private fun DiscoverSection(colors: RenovaColorScheme) {
    val textColor = Color.Black
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.6f)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.discover_products),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.discover_stores_description),
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontFamily = PoppinsFontFamily,
                        textAlign = TextAlign.Justify,
                        color = textColor.copy(alpha = 0.9f)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    GifPlayer(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun AlianzaCard(
    alianza: Alianza,
    categories: List<TypeShop>,
    colors: RenovaColorScheme,
    logoColor: Color,
    onClick: () -> Unit
) {
    val category = categories.find { it.id == alianza.type_shop_id }
    val categoryName = category?.name ?: stringResource(R.string.no_category)
    val categoryIcon = if (category != null) getIconForCategory(category.name) else R.drawable.ic_tienda

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(logoColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alianza.name.firstOrNull()?.uppercase() ?: "R",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = alianza.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = categoryIcon),
                        contentDescription = stringResource(R.string.category_icon_content_description),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = categoryName,
                        fontSize = 13.sp,
                        fontFamily = PoppinsFontFamily,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!alianza.address.isNullOrBlank()) {
                    // << CAMBIO PRINCIPAL AQUÍ >>
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = stringResource(R.string.address_icon_content_description),
                            tint = RenovaColors.PrimaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = alianza.address,
                            fontSize = 12.sp,
                            fontFamily = PoppinsFontFamily,
                            color = RenovaColors.PrimaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.next1),
                contentDescription = stringResource(R.string.view_details_content_description),
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
@Composable
private fun CategoryCard(
    category: TypeShop,
    isSelected: Boolean,
    onClick: () -> Unit,
    cardColor: Color
) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1f, label = "cardScale")
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .alpha(if (isSelected) 1f else 0.6f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        val iconResId = if (category.id == -1) R.drawable.ic_all else getIconForCategory(category.name)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = category.name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFontFamily,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 10.sp
            )
        }
    }
}


// ... (GifPlayer, getIconForCategory, LoadingSection, ErrorSection, EmptySection se mantienen igual)
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
        else -> R.drawable.ic_tienda
    }
}

@Composable
private fun GifPlayer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(R.raw.gif1)
            .build(),
        imageLoader = imageLoader,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
fun LoadingSection(colors: RenovaColorScheme) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = RenovaColors.Primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.loading_stores),
                color = colors.textSecondary,
                fontFamily = PoppinsFontFamily
            )
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
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.error_loading_stores),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFontFamily,
                color = RenovaColors.Error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                fontFamily = PoppinsFontFamily,
                color = RenovaColors.Error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = RenovaColors.Primary)
            ) {
                Text(
                    stringResource(R.string.retry),
                    color = Color.White,
                    fontFamily = PoppinsFontFamily
                )
            }
        }
    }
}

@Composable
fun EmptySection(colors: RenovaColorScheme) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.no_stores_available),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFontFamily,
                color = colors.textPrimary
            )
            Text(
                text = stringResource(R.string.no_stores_found),
                fontSize = 14.sp,
                fontFamily = PoppinsFontFamily,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}