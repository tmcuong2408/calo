package com.nnmk.calo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nnmk.calo.data.entity.DishEntity
import com.nnmk.calo.data.entity.DishWithUnit
import com.nnmk.calo.data.entity.UnitEntity
import com.nnmk.calo.data.repository.CaloRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DishViewModel(private val repository: CaloRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allDishes: StateFlow<List<DishWithUnit>> = combine(
        repository.allDishes,
        _searchQuery
    ) { dishes, query ->
        val filtered = if (query.isBlank()) dishes
        else dishes.filter { it.dish.name.contains(query, ignoreCase = true) }
        filtered.take(5)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allUnits: StateFlow<List<UnitEntity>> = repository.allUnits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addDish(name: String, calories: Double, unitId: Long?) {
        viewModelScope.launch {
            repository.insertDish(DishEntity(name = name, calories = calories, unitId = unitId))
        }
    }

    fun updateDish(dish: DishEntity) {
        viewModelScope.launch {
            repository.updateDish(dish)
        }
    }

    fun deleteDish(dish: DishEntity) {
        viewModelScope.launch {
            repository.deleteDish(dish)
        }
    }
}

class DishViewModelFactory(private val repository: CaloRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
