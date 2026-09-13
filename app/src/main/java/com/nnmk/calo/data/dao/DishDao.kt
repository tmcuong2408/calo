package com.nnmk.calo.data.dao

import androidx.room.*
import com.nnmk.calo.data.entity.DishEntity
import com.nnmk.calo.data.entity.DishWithUnit
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Transaction
    @Query("SELECT * FROM dishes ORDER BY id DESC")
    fun getAllDishes(): Flow<List<DishWithUnit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: DishEntity)

    @Update
    suspend fun updateDish(dish: DishEntity)

    @Delete
    suspend fun deleteDish(dish: DishEntity)

    @Query("SELECT * FROM dishes WHERE id = :id")
    suspend fun getDishById(id: Long): DishEntity?
}
