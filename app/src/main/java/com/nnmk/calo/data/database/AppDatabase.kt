package com.nnmk.calo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nnmk.calo.data.dao.DishDao
import com.nnmk.calo.data.dao.MealDao
import com.nnmk.calo.data.dao.UnitDao
import com.nnmk.calo.data.entity.DishEntity
import com.nnmk.calo.data.entity.MealDishCrossRef
import com.nnmk.calo.data.entity.MealEntity
import com.nnmk.calo.data.entity.UnitEntity

@Database(
    entities = [UnitEntity::class, DishEntity::class, MealEntity::class, MealDishCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun unitDao(): UnitDao
    abstract fun dishDao(): DishDao
    abstract fun mealDao(): MealDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
