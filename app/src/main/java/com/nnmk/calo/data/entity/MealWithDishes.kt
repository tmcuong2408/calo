package com.nnmk.calo.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MealWithDishes(
    @Embedded val meal: MealEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MealDishCrossRef::class,
            parentColumn = "mealId",
            entityColumn = "dishId"
        )
    )
    val dishes: List<DishEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val crossRefs: List<MealDishCrossRef>
) {
    val totalCalories: Double
        get() = dishes.sumOf { dish ->
            val quantity = crossRefs.find { it.dishId == dish.id }?.quantity ?: 0
            dish.calories * quantity
        }
}
