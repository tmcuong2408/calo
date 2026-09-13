package com.nnmk.calo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nnmk.calo.data.entity.*
import com.nnmk.calo.data.repository.CaloRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MealViewModel(private val repository: CaloRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allMeals: StateFlow<List<MealWithDishes>> = combine(
        repository.allMeals,
        _searchQuery
    ) { meals, query ->
        val filtered = if (query.isBlank()) meals
        else meals.filter { it.meal.name.contains(query, ignoreCase = true) }
        filtered.take(5)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDishes: StateFlow<List<DishWithUnit>> = repository.allDishes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveMeal(meal: MealEntity, dishesWithQuantity: List<Pair<DishEntity, Int>>) {
        viewModelScope.launch {
            repository.upsertMealWithDishes(meal, dishesWithQuantity)
        }
    }

    fun deleteMeal(meal: MealEntity) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
        }
    }
}

class MealViewModelFactory(private val repository: CaloRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
