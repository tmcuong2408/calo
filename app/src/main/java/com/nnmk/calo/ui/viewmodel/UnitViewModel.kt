package com.nnmk.calo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nnmk.calo.data.entity.UnitEntity
import com.nnmk.calo.data.repository.CaloRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnitViewModel(private val repository: CaloRepository) : ViewModel() {
    val allUnits: StateFlow<List<UnitEntity>> = repository.allUnits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addUnit(name: String) {
        viewModelScope.launch {
            repository.insertUnit(UnitEntity(name = name))
        }
    }

    fun updateUnit(unit: UnitEntity) {
        viewModelScope.launch {
            repository.updateUnit(unit)
        }
    }

    fun deleteUnit(unit: UnitEntity) {
        viewModelScope.launch {
            repository.deleteUnit(unit)
        }
    }
}

class UnitViewModelFactory(private val repository: CaloRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UnitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UnitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
