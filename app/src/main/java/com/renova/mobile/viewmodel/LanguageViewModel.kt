package com.renova.mobile.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.utils.LocaleHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class LanguageViewModel(application: Application) : AndroidViewModel(application) {
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _currentLanguage = MutableStateFlow(
        LocaleHelper.getLanguage(application.applicationContext)
    )
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun resetUpdating() {
        _isUpdating.value = false
    }

    fun changeLanguage(language: String, onComplete: () -> Unit) {
        if (_isUpdating.value || _currentLanguage.value == language) return

        viewModelScope.launch {
            _isUpdating.value = true
            LocaleHelper.persist(getApplication(), language)
            LocaleHelper.setLocale(getApplication(), language)
            _currentLanguage.value = language
            //kotlinx.coroutines.delay(300)
            onComplete()
        }
    }
}