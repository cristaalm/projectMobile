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

            // Obtenemos alianzas y categorías
            val alianzasResult = repository.getAllAlianzas()
            val categoriesResult = repository.getTypeShops()

            // Verificamos si ambos resultados fueron exitosos
            if (alianzasResult.isSuccess && categoriesResult.isSuccess) {
                uiState = uiState.copy(
                    isLoading = false,
                    alianzas = alianzasResult.getOrThrow(),
                    categories = categoriesResult.getOrThrow(), // <-- CAMBIO
                    error = null
                )
            } else {
                // Si alguno falla, mostramos el primer error que encontremos
                val errorMsg = alianzasResult.exceptionOrNull()?.message ?:
                categoriesResult.exceptionOrNull()?.message ?:
                "Error desconocido"
                uiState = uiState.copy(
                    isLoading = false,
                    error = errorMsg
                )
            }
        }
    }

    fun retryLoading() {
        loadStoreData()
    }
}

data class StoreUiState(
    val isLoading: Boolean = false,
    val alianzas: List<Alianza> = emptyList(),
    val categories: List<TypeShop> = emptyList(),
    val error: String? = null
)