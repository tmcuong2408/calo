package com.nnmk.calo.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nnmk.calo.data.entity.DishEntity
import com.nnmk.calo.data.entity.DishWithUnit
import com.nnmk.calo.data.entity.UnitEntity
import com.nnmk.calo.ui.viewmodel.DishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishScreen(viewModel: DishViewModel) {
    val dishes by viewModel.allDishes.collectAsState()
    val units by viewModel.allUnits.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var dishToEdit by remember { mutableStateOf<DishWithUnit?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Món ăn") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm món ăn")
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
                placeholder = { Text("Tìm kiếm món ăn...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dishes, key = { it.dish.id }) { dishWithUnit ->
                    DishItem(
                        dishWithUnit = dishWithUnit,
                        onEdit = { dishToEdit = dishWithUnit },
                        onDelete = { viewModel.deleteDish(dishWithUnit.dish) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        DishDialog(
            units = units,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cals, unitId ->
                viewModel.addDish(name, cals, unitId)
                showAddDialog = false
            }
        )
    }

    dishToEdit?.let { dishWithUnit ->
        DishDialog(
            initialName = dishWithUnit.dish.name,
            initialCalories = dishWithUnit.dish.calories,
            initialUnitId = dishWithUnit.dish.unitId,
            units = units,
            title = "Sửa món ăn",
            onDismiss = { dishToEdit = null },
            onConfirm = { name, cals, unitId ->
                viewModel.updateDish(dishWithUnit.dish.copy(name = name, calories = cals, unitId = unitId))
                dishToEdit = null
            }
        )
    }
}

@Composable
fun DishItem(dishWithUnit: DishWithUnit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dishWithUnit.dish.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${dishWithUnit.dish.calories} kcal / ${dishWithUnit.unit?.name ?: "Không có đơn vị"}",
                    style = MaterialTheme.typography.bodyMedium
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDialog(
    initialName: String = "",
    initialCalories: Double = 0.0,
    initialUnitId: Long? = null,
    units: List<UnitEntity>,
    title: String = "Thêm món ăn",
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Long?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var calories by remember { mutableStateOf(if (initialCalories == 0.0) "" else initialCalories.toString()) }
    var selectedUnitId by remember { mutableStateOf(initialUnitId) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Tên món ăn") })
                TextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Lượng calo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = units.find { it.id == selectedUnitId }?.name ?: "Chọn đơn vị",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Đơn vị") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.name) },
                                onClick = {
                                    selectedUnitId = unit.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val cals = calories.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) onConfirm(name, cals, selectedUnitId)
            }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
