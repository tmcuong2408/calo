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
import com.nnmk.calo.data.entity.UnitEntity
import com.nnmk.calo.ui.viewmodel.UnitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitScreen(viewModel: UnitViewModel) {
    val units by viewModel.allUnits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var unitToEdit by remember { mutableStateOf<UnitEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Đơn vị") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm đơn vị")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(units, key = { it.id }) { unit ->
                UnitItem(
                    unit = unit,
                    onEdit = { unitToEdit = unit },
                    onDelete = { viewModel.deleteUnit(unit) }
                )
            }
        }
    }

    if (showAddDialog) {
        UnitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addUnit(name)
                showAddDialog = false
            }
        )
    }

    unitToEdit?.let { unit ->
        UnitDialog(
            initialName = unit.name,
            title = "Sửa đơn vị",
            onDismiss = { unitToEdit = null },
            onConfirm = { name ->
                viewModel.updateUnit(unit.copy(name = name))
                unitToEdit = null
            }
        )
    }
}

@Composable
fun UnitItem(unit: UnitEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(unit.name, style = MaterialTheme.typography.bodyLarge)
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
fun UnitDialog(
    initialName: String = "",
    title: String = "Thêm đơn vị",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên đơn vị") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
