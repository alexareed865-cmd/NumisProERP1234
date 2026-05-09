package com.numisproerp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.text.style.TextAlign
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.viewmodel.ExpensesSort
import com.numisproerp.ui.viewmodel.ExpensesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    navController: NavHostController,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = tr("Назад", "Back"),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        FloatingActionButton(
            onClick = { viewModel.toggleAddDialog(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = AccentBlue
        ) {
            Icon(Icons.Default.Add, contentDescription = tr("Додати витрату", "Add expense"))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = tr("Витрати", "Expenses"),
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                textAlign = TextAlign.Center
            )

            val visible = viewModel.visibleExpenses()
            val totalExpenses = visible.sumOf { it.amount }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AccentRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.selectedCategory.isBlank()) tr("Загальні витрати", "Total expenses")
                        else "${tr("Витрати", "Expenses")}: ${uiState.selectedCategory}",
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%,.2f", totalExpenses)} ₴",
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("Сортування", "Sort"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                ExpensesSortMenu(
                    current = uiState.sort,
                    onSelected = { viewModel.setSort(it) }
                )
            }

            if (uiState.categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory.isBlank(),
                            onClick = { viewModel.setCategory("") },
                            label = { Text(tr("Усі категорії", "All categories")) }
                        )
                    }
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = {
                                if (uiState.selectedCategory == category) viewModel.setCategory("")
                                else viewModel.setCategory(category)
                            },
                            label = { Text(category) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (visible.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.expenses.isEmpty()) tr("Немає витрат.\nНатисніть + щоб додати", "No expenses.\nTap + to add")
                        else tr("Немає витрат за обраним фільтром", "No expenses for selected filter"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visible) { expense ->
                        ExpenseCard(
                            expense = expense,
                            formatDate = { viewModel.formatDate(it) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddDialog(false) },
            title = { Text(tr("Додати витрату", "Add expense")) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var category by remember { mutableStateOf("") }
                    var amount by remember { mutableStateOf("") }
                    var comment by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(tr("Категорія *", "Category *")) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(tr("Оренда, Реклама, Доставка...", "Rent, Ads, Delivery...")) },
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(tr("Сума *", "Amount *")) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("₴") },
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text(tr("Коментар", "Comment")) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(tr("Опис витрати", "Expense description")) },
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )

                    TextButton(
                        onClick = {
                            if (category.isNotBlank() && amount.isNotBlank()) {
                                viewModel.addExpense(category, amount, comment)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(tr("Зберегти", "Save"))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddDialog(false) }) {
                    Text(tr("Скасувати", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun ExpenseCard(
    expense: com.numisproerp.data.entities.OtherExpense,
    formatDate: (Long) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IOSIconChip(
                icon = Icons.Outlined.Category,
                tint = AccentOrange
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.category,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = formatDate(expense.date),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (expense.comment.isNotEmpty()) {
                    Text(
                        text = expense.comment,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                text = "-${String.format("%,.2f", expense.amount)} ₴",
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = AccentRed
            )
        }
    }
}

@Composable
private fun ExpensesSortMenu(
    current: ExpensesSort,
    onSelected: (ExpensesSort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (current) {
        ExpensesSort.DATE_DESC -> "${tr("Дата", "Date")} ↓"
        ExpensesSort.DATE_ASC -> "${tr("Дата", "Date")} ↑"
        ExpensesSort.AMOUNT_DESC -> "${tr("Сума", "Amount")} ↓"
        ExpensesSort.AMOUNT_ASC -> "${tr("Сума", "Amount")} ↑"
    }
    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Sort, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(tr("Дата (нові спочатку)", "Date (newest first)")) }, onClick = {
                onSelected(ExpensesSort.DATE_DESC); expanded = false
            })
            DropdownMenuItem(text = { Text(tr("Дата (старі спочатку)", "Date (oldest first)")) }, onClick = {
                onSelected(ExpensesSort.DATE_ASC); expanded = false
            })
            DropdownMenuItem(text = { Text(tr("Сума (більші спочатку)", "Amount (highest first)")) }, onClick = {
                onSelected(ExpensesSort.AMOUNT_DESC); expanded = false
            })
            DropdownMenuItem(text = { Text(tr("Сума (менші спочатку)", "Amount (lowest first)")) }, onClick = {
                onSelected(ExpensesSort.AMOUNT_ASC); expanded = false
            })
        }
    }
}
