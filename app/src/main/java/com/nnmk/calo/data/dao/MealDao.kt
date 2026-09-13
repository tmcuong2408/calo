package com.nnmk.calo.data.dao

import androidx.room.*
import com.nnmk.calo.data.entity.MealDishCrossRef
import com.nnmk.calo.data.entity.MealEntity
import com.nnmk.calo.data.entity.MealWithDishes
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Transaction
    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealWithDishes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealDishCrossRefs(crossRefs: List<MealDishCrossRef>)

    @Query("DELETE FROM meal_dish_cross_ref WHERE mealId = :mealId")
    suspend fun deleteCrossRefsForMeal(mealId: Long)

    @Transaction
    suspend fun upsertMealWithDishes(meal: MealEntity, crossRefs: List<MealDishCrossRef>) {
        val mealId = if (meal.id == 0L) {
            insertMeal(meal)
        } else {
            updateMeal(meal)
            meal.id
        }
        deleteCrossRefsForMeal(mealId)
        insertMealDishCrossRefs(crossRefs.map { it.copy(mealId = mealId) })
    }
}
