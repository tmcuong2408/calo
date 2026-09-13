package com.nnmk.calo

import android.app.Application
import com.nnmk.calo.data.database.AppDatabase
import com.nnmk.calo.data.repository.CaloRepository

class CaloApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CaloRepository(database.unitDao(), database.dishDao(), database.mealDao()) }
}
