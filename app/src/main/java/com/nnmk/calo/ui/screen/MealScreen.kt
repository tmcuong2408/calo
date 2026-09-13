package com.nnmk.calo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnmk.calo.data.entity.MealWithDishes
import com.nnmk.calo.ui.viewmodel.MealViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.nnmk.calo.data.entity.DishWithUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScreen(viewModel: MealViewModel, onAddMeal: () -> Unit, onEditMeal: (Long) -> Unit) {
    val meals by viewModel.allMeals.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedMealForDetail by remember { mutableStateOf<MealWithDishes?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bữa ăn") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMeal) {
                Icon(Icons.Default.Add, contentDescription = "Thêm bữa ăn")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm kiếm bữa ăn...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(meals, key = { it.meal.id }) { mealWithDishes ->
                    MealItem(
                        mealWithDishes = mealWithDishes,
                        onEdit = { onEditMeal(mealWithDishes.meal.id) },
                        onDelete = { viewModel.deleteMeal(mealWithDishes.meal) },
                        onClick = { selectedMealForDetail = mealWithDishes }
                    )
                }
            }
        }
    }

    val allDishesWithUnits by viewModel.allDishes.collectAsState()

    selectedMealForDetail?.let { meal ->
        MealDetailDialog(
            mealWithDishes = meal,
            allDishesWithUnits = allDishesWithUnits,
            onDismiss = { selectedMealForDetail = null }
        )
    }
}

@Composable
fun MealItem(
    mealWithDishes: MealWithDishes,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mealWithDishes.meal.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    dateFormat.format(Date(mealWithDishes.meal.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Tổng calo: ${mealWithDishes.totalCalories} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa")
                }
            }
        }
    }
}

@Composable
fun MealDetailDialog(
    mealWithDishes: MealWithDishes,
    allDishesWithUnits: List<DishWithUnit>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mealWithDishes.meal.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Danh sách món ăn",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mealWithDishes.dishes) { dish ->
                        val crossRef = mealWithDishes.crossRefs.find { it.dishId == dish.id }
                        val quantity = crossRef?.quantity ?: 0
                        val dishWithUnit = allDishesWithUnits.find { it.dish.id == dish.id }
                        val unitName = dishWithUnit?.unit?.name ?: "đơn vị"
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            ListItem(
                                headlineContent = { 
                                    Text(
                                        text = "${dish.name} ($unitName)", 
                                        fontWeight = FontWeight.Bold
                                    ) 
                                },
                                supportingContent = { 
                                    Text("${dish.calories} kcal x $quantity = ${dish.calories * quantity} kcal") 
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tổng cộng", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${mealWithDishes.totalCalories} kcal",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}
