package com.espert.reeporteciudadano.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class AppViewModel : ViewModel() {
    private val _stack = MutableStateFlow<List<NavDestination>>(listOf(NavDestination.MainShell))
    val currentDestination: StateFlow<NavDestination> = _stack
        .map { it.last() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, NavDestination.MainShell)

    private val _selectedTab = MutableStateFlow(BottomTab.REPORT)
    val selectedTab: StateFlow<BottomTab> = _selectedTab.asStateFlow()

    fun navigate(dest: NavDestination) { _stack.update { it + dest } }

    fun back(): Boolean {
        if (_stack.value.size <= 1) return false
        _stack.update { it.dropLast(1) }
        return true
    }

    fun backToMain() {
        _stack.value = listOf(NavDestination.MainShell)
        _selectedTab.value = BottomTab.REPORT
    }

    fun selectTab(tab: BottomTab) { _selectedTab.value = tab }
}
