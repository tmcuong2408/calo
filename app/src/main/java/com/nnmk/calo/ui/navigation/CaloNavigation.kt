package com.nnmk.calo.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Meals : Screen("meals", "Bữa ăn", Icons.Default.List)
    object Dishes : Screen("dishes", "Món ăn", Icons.Default.Menu)
    object Units : Screen("units", "Đơn vị", Icons.Default.Settings)
}

sealed class LeafScreen(val route: String) {
    object AddEditMeal : LeafScreen("add_edit_meal?mealId={mealId}") {
        fun createRoute(mealId: Long?) = if (mealId == null) "add_edit_meal" else "add_edit_meal?mealId=$mealId"
    }
}
