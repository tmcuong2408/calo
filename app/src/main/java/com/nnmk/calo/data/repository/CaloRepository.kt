package com.nnmk.calo.data.repository

import com.nnmk.calo.data.dao.DishDao
import com.nnmk.calo.data.dao.MealDao
import com.nnmk.calo.data.dao.UnitDao
import com.nnmk.calo.data.entity.*
import kotlinx.coroutines.flow.Flow

class CaloRepository(
    private val unitDao: UnitDao,
    private val dishDao: DishDao,
    private val mealDao: MealDao
) {
    // Units
    val allUnits: Flow<List<UnitEntity>> = unitDao.getAllUnits()
    suspend fun insertUnit(unit: UnitEntity) = unitDao.insertUnit(unit)
    suspend fun updateUnit(unit: UnitEntity) = unitDao.updateUnit(unit)
    suspend fun deleteUnit(unit: UnitEntity) = unitDao.deleteUnit(unit)

    // Dishes
    val allDishes: Flow<List<DishWithUnit>> = dishDao.getAllDishes()
    suspend fun insertDish(dish: DishEntity) = dishDao.insertDish(dish)
    suspend fun updateDish(dish: DishEntity) = dishDao.updateDish(dish)
    suspend fun deleteDish(dish: DishEntity) = dishDao.deleteDish(dish)

    // Meals
    val allMeals: Flow<List<MealWithDishes>> = mealDao.getAllMeals()
    suspend fun upsertMealWithDishes(meal: MealEntity, dishes: List<Pair<DishEntity, Int>>) {
        val crossRefs = dishes.map { (dish, quantity) ->
            MealDishCrossRef(mealId = meal.id, dishId = dish.id, quantity = quantity)
        }
        mealDao.upsertMealWithDishes(meal, crossRefs)
    }
    suspend fun deleteMeal(meal: MealEntity) = mealDao.deleteMeal(meal)
}
