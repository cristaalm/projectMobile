package com.renova.mobile.ui.screens.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.Alianza
import com.renova.mobile.network.TypeShop
import com.renova.mobile.repository.AlianzasRepository
import kotlinx.coroutines.launch

// propiedades para la paginación
data class StoreUiState(
    val isLoading: Boolean = false,
    val alianzas: List<Alianza> = emptyList(),
    val categories: List<TypeShop> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val itemsPerPage: Int = 5 // mostrar 5 alianzas por página
)

class StoreViewModel : ViewModel() {

    private val repository = AlianzasRepository()

    var uiState by mutableStateOf(StoreUiState())
        private set

    init {
        loadStoreData()
    }

    fun loadStoreData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val alianzasResult = repository.getAllAlianzas()
            val categoriesResult = repository.getTypeShops()

            if (alianzasResult.isSuccess && categoriesResult.isSuccess) {
                uiState = uiState.copy(
                    isLoading = false,
                    alianzas = alianzasResult.getOrThrow(),
                    categories = categoriesResult.getOrThrow(),
                    error = null,
                    currentPage = 1
                )
            } else {
                val errorMsg = alianzasResult.exceptionOrNull()?.message ?:
                categoriesResult.exceptionOrNull()?.message ?:
                "Error desconocido"
                uiState = uiState.copy(isLoading = false, error = errorMsg)
            }
        }
    }

    fun retryLoading() {
        loadStoreData()
    }

    // funciones para controlar la paginación
    fun nextPage() {
        val totalPages = (uiState.alianzas.size + uiState.itemsPerPage - 1) / uiState.itemsPerPage
        if (uiState.currentPage < totalPages) {
            uiState = uiState.copy(currentPage = uiState.currentPage + 1)
        }
    }

    fun previousPage() {
        if (uiState.currentPage > 1) {
            uiState = uiState.copy(currentPage = uiState.currentPage - 1)
        }
    }
}