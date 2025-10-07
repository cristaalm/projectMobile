package com.renova.mobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.renova.mobile.R
import com.renova.mobile.network.TypeShop

/**
 * Devuelve el nombre traducido de una categoría.
 */
@Composable
fun getTranslatedCategoryName(category: TypeShop): String {
    // Maneja el caso especial de la categoría "Todos".
    if (category.id == -1) {
        return stringResource(R.string.all_categories)
    }

    // Mapea el nombre de la API al ID del recurso de string correspondiente.
    val stringId = when {
        category.name.contains("restaurant", ignoreCase = true) -> R.string.category_restaurant
        category.name.contains("supermercado", ignoreCase = true) -> R.string.category_supermarket
        category.name.contains("farmacia", ignoreCase = true) -> R.string.category_pharmacy
        category.name.contains("ropa", ignoreCase = true) -> R.string.category_clothing
        category.name.contains("gasolinera", ignoreCase = true) -> R.string.category_gas_station
        category.name.contains("bodega", ignoreCase = true) -> R.string.category_cellar
        category.name.contains("electrónica", ignoreCase = true) -> R.string.category_electronics
        category.name.contains("libros", ignoreCase = true) -> R.string.category_bookstore
        category.name.contains("juguetes", ignoreCase = true) -> R.string.category_toys
        category.name.contains("tienda", ignoreCase = true) -> R.string.category_store
        // Si no hay coincidencia, devuelve el nombre original.
        else -> return category.name
    }
    return stringResource(id = stringId)
}

/**
 * Devuelve el ícono correspondiente para una categoría.
 */
fun getIconForCategory(categoryName: String): Int {
    return when {
        categoryName.contains("restaurant", ignoreCase = true) -> R.drawable.ic_restaurant
        categoryName.contains("supermercado", ignoreCase = true) -> R.drawable.ic_supermercado
        categoryName.contains("farmacia", ignoreCase = true) -> R.drawable.ic_farmacia
        categoryName.contains("ropa", ignoreCase = true) -> R.drawable.ic_ropa
        categoryName.contains("gasolinera", ignoreCase = true) -> R.drawable.ic_gasolinera
        categoryName.contains("bodega", ignoreCase = true) -> R.drawable.ic_bodega
        categoryName.contains("electrónica", ignoreCase = true) -> R.drawable.ic_electronica
        categoryName.contains("tienda de libros", ignoreCase = true) -> R.drawable.ic_libro
        categoryName.contains("juguetes", ignoreCase = true) -> R.drawable.ic_juguetes
        categoryName.contains("tienda", ignoreCase = true) -> R.drawable.ic_tienda
        else -> R.drawable.ic_tienda
    }
}