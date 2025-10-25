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
import androidx.compose.ui.draw.shadow
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
import com.renova.mobile.navigation.StoreGraph
import com.renova.mobile.network.Alianza
import com.renova.mobile.network.TypeShop
import com.renova.mobile.ui.components.AlianzaCard
import com.renova.mobile.ui.components.CategoryCard
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.screens.viewmodel.StoreViewModel
import com.renova.mobile.ui.theme.*
import kotlin.math.min
import com.renova.mobile.ui.tour.LocalTourState
import com.renova.mobile.ui.tour.HandleTourOnError
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


@Composable
fun StoreScreen(
    navController: NavController,
    viewModel: StoreViewModel = viewModel()
) {
    val colors = LocalRenovaColors.current
    var searchQuery by remember { mutableStateOf("") }
    val uiState = viewModel.uiState
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    val tourState = LocalTourState.current
    val isTourActive by tourState.isTourActive.collectAsState()

    HandleTourOnError(
        error = uiState.error, // El mensaje de error del ViewModel
        isTourActiveFlow = tourState.isTourActive, // El StateFlow del TourState
        endTour = tourState::endTour // La función para terminar el tour
    )

    // Estado para controlar la LazyColumn y el Scope para lanzar el scroll
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val activeCategories = remember(uiState.categories, uiState.alianzas) {
        uiState.categories.filter { category ->
            uiState.alianzas.any { it.type_shop_id == category.id }
        }
    }

    val filteredAlianzas = remember(selectedCategoryId, searchQuery, uiState.alianzas, uiState.categories) {
        // ... (lógica de filtro sin cambios)
        val categoryFiltered = if (selectedCategoryId == null) {
            uiState.alianzas
        } else {
            uiState.alianzas.filter { it.type_shop_id == selectedCategoryId }
        }

        if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { alianza ->
                val categoryName = uiState.categories
                    .find { it.id == alianza.type_shop_id }?.name ?: ""
                val nameMatches = alianza.name.contains(searchQuery, ignoreCase = true)
                val categoryMatches = categoryName.contains(searchQuery, ignoreCase = true)
                val addressMatches = alianza.address?.contains(searchQuery, ignoreCase = true) ?: false
                nameMatches || categoryMatches || addressMatches
            }
        }
    }

    // ... (lógica de paginación sin cambios)
    val totalPages = (filteredAlianzas.size + uiState.itemsPerPage - 1) / uiState.itemsPerPage
    val currentPage = uiState.currentPage.coerceIn(1, if (totalPages > 0) totalPages else 1)
    val startIndex = (currentPage - 1) * uiState.itemsPerPage
    val endIndex = min(startIndex + uiState.itemsPerPage, filteredAlianzas.size)
    val paginatedAlianzas = if (filteredAlianzas.isNotEmpty()) {
        filteredAlianzas.subList(startIndex, endIndex)
    } else {
        emptyList()
    }


    val currentStepTargetId by remember(tourState) {
        derivedStateOf { tourState.currentStep?.targetId }
    }

    LaunchedEffect(isTourActive, currentStepTargetId, activeCategories.isNotEmpty(), paginatedAlianzas.isNotEmpty()) {        // Estos números de índice dependen del orden de tus 'item' en la LazyColumn.
        // Si añades o quitas items, tendrás que ajustar estos índices.
        if (isTourActive) {
            // Cálculo de índices:
            // 0: Spacer
            // 1: SearchBar
            // 2: DiscoverSection
            var categoriesGridIndex = -1
            var alliancesTitleIndex = 3 // Por defecto (si no hay categorías)
            var firstAllianceIndex = 4  // Por defecto

            if (activeCategories.isNotEmpty()) {
                // 3: Título Categorías
                categoriesGridIndex = 4 // 4: Grid Categorías
                alliancesTitleIndex = 5 // 5: Título Alianzas
                firstAllianceIndex = 6  // 6: Inicio de Alianzas (el 'when')
            }

            // Lanza la corrutina de scroll
            scope.launch {
                when (currentStepTargetId) {
                    "store_categories" -> {
                        if (categoriesGridIndex != -1) {
                            // Hacemos scroll al grid de categorías
                            lazyListState.animateScrollToItem(
                                index = categoriesGridIndex,
                                scrollOffset = -50
                            )
                        }
                    }

                    "store_alliances_title" -> {
                        // Hacemos scroll al título de "Comercios Destacados"
                        lazyListState.animateScrollToItem(
                            index = alliancesTitleIndex,
                            scrollOffset = -50
                        )
                    }

                    "store_first_alliance" -> {
                        if (paginatedAlianzas.isNotEmpty()) {
                            // Hacemos scroll a la primera alianza de la lista
                            lazyListState.animateScrollToItem(
                                index = firstAllianceIndex,
                                scrollOffset = -50
                            )
                        }
                    }
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SectionHeader(title = stringResource(id = R.string.store_screen_title))

        LazyColumn(
            state = lazyListState, // <-- MODIFICADO: Asigna el estado a la LazyColumn
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) } // Índice 0

            item { // Índice 1
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    colors = colors
                )
            }
            item { DiscoverSection(colors = colors) } // Índice 2

            if (activeCategories.isNotEmpty()) {
                item { // Índice 3
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

                item { // Índice 4
                    Box(modifier = Modifier.onGloballyPositioned { coords ->
                        tourState.registerTarget("store_categories", coords)
                    }) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .height(200.dp),
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
                    DisposableEffect(Unit) {
                        onDispose {
                            tourState.unregisterTarget("store_categories")
                        }
                    }
                }
            }

            item { // Índice 5 (o 3 si no hay categorías)
                Column (
                    modifier = Modifier.onGloballyPositioned { coords ->
                        tourState.registerTarget("store_alliances_title", coords)
                    }
                ){
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
                DisposableEffect(Unit) {
                    onDispose {
                        tourState.unregisterTarget("store_alliances_title")
                    }
                }
            }

            // Índice 6 (o 4 si no hay categorías)
            when {
                uiState.isLoading -> item { LoadingSection(colors = colors) }
                uiState.error != null -> item { ErrorSection(message = uiState.error, onRetry = { viewModel.retryLoading() }, colors = colors) }
                filteredAlianzas.isEmpty() -> item { EmptySection(colors = colors) }
                else -> {
                    itemsIndexed(paginatedAlianzas, key = { _, alianza -> alianza.id }) { index, alianza ->
                        val logoColor = colors.allianceLogoBackgrounds[alianza.id % colors.allianceLogoBackgrounds.size]

                        val itemModifier = if (index == 0) {
                            Modifier.onGloballyPositioned { coords ->
                                tourState.registerTarget("store_first_alliance", coords)
                            }
                        } else {
                            Modifier
                        }

                        // Limpiar el target si el item se va de la composición (scroll)
                        if (index == 0) {
                            DisposableEffect(Unit) {
                                onDispose {
                                    tourState.unregisterTarget("store_first_alliance")
                                }
                            }
                        }
                        Column (modifier = itemModifier){
                            AlianzaCard(
                                alianza = alianza,
                                categories = uiState.categories,
                                colors = colors,
                                logoColor = logoColor,
                                onClick = { navController.navigate("reward_screen/${alianza.id}") }
                            )

                            // El divisor ahora se basa en el tamaño de la lista paginada
                            if (index < paginatedAlianzas.size - 1) {
                                Divider(
                                    color = RenovaColors.PrimaryColor,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (totalPages > 1) {
                        item {
                            PaginationControls(
                                currentPage = currentPage,
                                totalPages = totalPages,
                                isLoading = uiState.isLoading,
                                onPreviousPage = { viewModel.previousPage() },
                                onNextPage = { viewModel.nextPage() }
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PaginationControls(
    // ... (sin cambios)
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    val renovaColors = LocalRenovaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Anterior
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentPage > 1) RenovaColors.SecondaryColor else renovaColors.buttonDisabled,
                contentColor = renovaColors.activityCardBackground
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
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

        // Indicador de página
        Text(
            text = "$currentPage ${stringResource(R.string.of)} $totalPages",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.CenterVertically),
            color = renovaColors.textPrimary
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Botón Siguiente
        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentPage < totalPages) RenovaColors.SecondaryColor else renovaColors.buttonDisabled,
                contentColor = renovaColors.activityCardBackground
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
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

@Composable
private fun SearchBar(
    // ... (sin cambios)
    query: String,
    onQueryChange: (String) -> Unit,
    colors: RenovaColorScheme
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp, // grosor de borde search
                color = Color(0xFF07B460),
                shape = RoundedCornerShape(16.dp)
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
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = RenovaColors.PrimaryColor,
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        )
    )
}


@Composable
private fun DiscoverSection(
    colors: RenovaColorScheme
) {
    val textColor = Color.Black
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = RenovaColors.Light.ActivityShadowColor
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                        .weight(1.8f)
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
                        fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                        textAlign = TextAlign.Justify,
                        color = textColor
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1.2f)
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
private fun GifPlayer(
    modifier: Modifier = Modifier
) {
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
fun LoadingSection(
    // ... (sin cambios)
    colors: RenovaColorScheme
) {
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
fun ErrorSection(
    // ... (sin cambios)
    message: String,
    onRetry: () -> Unit,
    colors: RenovaColorScheme
) {
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
fun EmptySection(
    // ... (sin cambios)
    colors: RenovaColorScheme
) {
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