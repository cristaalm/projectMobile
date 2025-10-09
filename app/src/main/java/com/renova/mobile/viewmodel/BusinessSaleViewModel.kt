package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.IdentifyUserRequest
import com.renova.mobile.network.User
import com.renova.mobile.network.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Maneja el estado del cliente escaneado para el flujo de venta del comerciante
class BusinessSaleViewModel : ViewModel() {
    private val _scannedUser = MutableStateFlow<UserData?>(null)
    val scannedUser: StateFlow<UserData?> = _scannedUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clear() {
        _scannedUser.value = null
        _error.value = null
        _isLoading.value = false
    }

    fun identifyUserByToken(scannedToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Validaciones del token escaneado
                val token = scannedToken.trim()
                if (token.isEmpty()) {
                    _error.value = "No es un escaneo válido"
                    _scannedUser.value = null
                    return@launch
                }

                // Limpiar el token si tiene el prefijo "Bearer "
                val cleanToken = token.removePrefix("Bearer ").trim()

                val response = ApiClient.apiService.identifyUser(
                    IdentifyUserRequest(token = cleanToken)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data?.user != null) {
                        _scannedUser.value = body.data.user
                    } else {
                        // Si el backend indica que no encontró usuario o token inválido
                        val message = body?.message
                        _error.value = message ?: "Usuario no encontrado"
                        _scannedUser.value = null
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                    _error.value = when (response.code()) {
                        401 -> "No es un escaneo válido"
                        404 -> "Usuario no encontrado"
                        else -> "Error ${response.code()}: ${errorBody ?: response.message()}"
                    }
                    _scannedUser.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ocurrió un error"
                _scannedUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}