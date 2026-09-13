package com.nnmk.calo.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nnmk.calo.data.entity.DishWithUnit
import com.nnmk.calo.data.entity.MealEntity
import com.nnmk.calo.ui.viewmodel.MealViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMealScreen(
    viewModel: MealViewModel,
    mealId: Long? = null,
    onNavigateBack: () -> Unit
) {
    val meals by viewModel.allMeals.collectAsState()
    val allDishesWithUnits by viewModel.allDishes.collectAsState()

    val mealWithDishes = remember(mealId, meals) {
        meals.find { it.meal.id == mealId }
    }

    // Fix: Use remember(mealWithDishes) to ensure it updates when data is loaded
    var name by remember(mealWithDishes) { mutableStateOf(mealWithDishes?.meal?.name ?: "") }
    
    val selectedDishes = remember {
        mutableStateListOf<Pair<DishWithUnit, Int>>()
    }

    // Pre-fill dishes when editing
    LaunchedEffect(mealWithDishes, allDishesWithUnits) {
        if (mealWithDishes != null && allDishesWithUnits.isNotEmpty() && selectedDishes.isEmpty()) {
            mealWithDishes.dishes.forEach { dishEntity ->
                val dishWithUnit = allDishesWithUnits.find { it.dish.id == dishEntity.id }
                if (dishWithUnit != null) {
                    val quantity = mealWithDishes.crossRefs.find { it.dishId == dishEntity.id }?.quantity ?: 0
                    selectedDishes.add(dishWithUnit to quantity)
                }
            }
        }
    }

    var showDishPicker by remember { mutableStateOf(false) }
    
    // Auto-complete suggestions logic
    val existingMealNames = remember(meals) {
        meals.map { it.meal.name }.distinct().filter { it.isNotBlank() }
    }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mealId == null) "Thêm bữa ăn" else "Sửa bữa ăn") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    Button(onClick = {
                        if (name.isNotBlank() && selectedDishes.isNotEmpty()) {
                            val meal = mealWithDishes?.meal?.copy(name = name) ?: MealEntity(name = name)
                            viewModel.saveMeal(meal, selectedDishes.map { it.first.dish to it.second })
                            onNavigateBack()
                        }
                    }) {
                        Text("Lưu")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Meal Name with Auto-complete (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expanded && existingMealNames.isNotEmpty(),
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        expanded = true 
                    },
                    label = { Text("Tên bữa ăn") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                
                val filteredSuggestions = existingMealNames.filter { 
                    it.contains(name, ignoreCase = true) && it != name 
                }
                
                if (filteredSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    name = suggestion
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Món ăn", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { showDishPicker = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Thêm món ăn")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedDishes, key = { it.first.dish.id }) { (dishWithUnit, quantity) ->
                    SelectedDishItem(
                        dishWithUnit = dishWithUnit,
                        quantity = quantity,
                        onQuantityChange = { newQty ->
                            val index = selectedDishes.indexOfFirst { it.first.dish.id == dishWithUnit.dish.id }
                            if (index != -1) {
                                selectedDishes[index] = dishWithUnit to newQty
                            }
                        },
                        onDelete = {
                            selectedDishes.removeAll { it.first.dish.id == dishWithUnit.dish.id }
                        }
                    )
                }
            }
            
            val totalCals = selectedDishes.sumOf { it.first.dish.calories * it.second }
            Text(
                "Tổng calo: $totalCals kcal",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }

    if (showDishPicker) {
        DishPickerDialog(
            dishes = allDishesWithUnits,
            onDismiss = { showDishPicker = false },
            onDishSelected = { dishWithUnit ->
                if (selectedDishes.none { it.first.dish.id == dishWithUnit.dish.id }) {
                    selectedDishes.add(dishWithUnit to 0)
                }
                showDishPicker = false
            }
        )
    }
}

@Composable
fun SelectedDishItem(
    dishWithUnit: DishWithUnit,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    // Chỉ khởi tạo giá trị text một lần dựa trên ID món ăn.
    // Việc này ngăn ô nhập liệu tự động nhảy về "0" khi bạn đang xóa hết sạch chữ.
    var textValue by remember(dishWithUnit.dish.id) { mutableStateOf(quantity.toString()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dishWithUnit.dish.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${dishWithUnit.dish.calories} kcal / ${dishWithUnit.unit?.name ?: "đơn vị"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextField(
                value = textValue,
                onValueChange = { newValue ->
                    // Chỉ cho phép nhập số
                    val digitsOnly = newValue.filter { it.isDigit() }
                    textValue = digitsOnly
                    // Cập nhật giá trị cho parent, nếu trống thì coi là 0 để tính calo
                    val newQty = digitsOnly.toIntOrNull() ?: 0
                    onQuantityChange(newQty)
                },
                label = { Text("SL") },
                modifier = Modifier.width(80.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa")
            }
        }
    }
}

@Composable
fun DishPickerDialog(
    dishes: List<DishWithUnit>,
    onDismiss: () -> Unit,
    onDishSelected: (DishWithUnit) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredDishes = remember(searchQuery, dishes) {
        dishes.filter { it.dish.name.contains(searchQuery, ignoreCase = true) }
            .take(5)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn món ăn") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm tên món ăn...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(filteredDishes, key = { it.dish.id }) { dishWithUnit ->
                        ListItem(
                            headlineContent = { Text(dishWithUnit.dish.name) },
                            supportingContent = { 
                                Text("${dishWithUnit.dish.calories} kcal / ${dishWithUnit.unit?.name ?: "đơn vị"}") 
                            },
                            modifier = Modifier.clickable { onDishSelected(dishWithUnit) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
