package com.nnmk.calo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.nnmk.calo.ui.navigation.LeafScreen
import com.nnmk.calo.ui.navigation.Screen
import com.nnmk.calo.ui.screen.AddEditMealScreen
import com.nnmk.calo.ui.screen.DishScreen
import com.nnmk.calo.ui.screen.MealScreen
import com.nnmk.calo.ui.screen.UnitScreen
import com.nnmk.calo.ui.theme.CaloTheme
import com.nnmk.calo.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaloTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val repository = (LocalContext.current.applicationContext as CaloApplication).repository

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(Screen.Meals, Screen.Dishes, Screen.Units)
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Meals.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Meals.route) {
                val mealViewModel: MealViewModel = viewModel(factory = MealViewModelFactory(repository))
                MealScreen(
                    viewModel = mealViewModel,
                    onAddMeal = { navController.navigate(LeafScreen.AddEditMeal.createRoute(null)) },
                    onEditMeal = { mealId -> navController.navigate(LeafScreen.AddEditMeal.createRoute(mealId)) }
                )
            }
            composable(Screen.Dishes.route) {
                val dishViewModel: DishViewModel = viewModel(factory = DishViewModelFactory(repository))
                DishScreen(viewModel = dishViewModel)
            }
            composable(Screen.Units.route) {
                val unitViewModel: UnitViewModel = viewModel(factory = UnitViewModelFactory(repository))
                UnitScreen(viewModel = unitViewModel)
            }
            composable(
                route = LeafScreen.AddEditMeal.route,
                arguments = listOf(navArgument("mealId") { 
                    type = NavType.LongType
                    defaultValue = -1L 
                })
            ) { backStackEntry ->
                val mealId = backStackEntry.arguments?.getLong("mealId")?.takeIf { it != -1L }
                val mealViewModel: MealViewModel = viewModel(factory = MealViewModelFactory(repository))
                AddEditMealScreen(
                    viewModel = mealViewModel,
                    mealId = mealId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
