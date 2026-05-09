package com.numisproerp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.viewmodel.HistoryEntry
import com.numisproerp.ui.viewmodel.HistoryEntryType
import com.numisproerp.ui.viewmodel.HistorySort
import com.numisproerp.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val visible = remember(uiState) { viewModel.visibleEntries() }

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

        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            HistorySortMenu(
                current = uiState.sort,
                onSelect = { viewModel.setSort(it) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = tr("Історія", "History"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                textAlign = TextAlign.Center
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(HistoryEntryType.values().toList()) { type ->
                    val selected = type in uiState.selectedTypes
                    val typeLabel = when (type) {
                        HistoryEntryType.PURCHASE -> tr("Закупівля", "Purchase")
                        HistoryEntryType.SALE -> tr("Продаж", "Sale")
                        HistoryEntryType.WRITEOFF -> tr("Списання", "Writeoff")
                        HistoryEntryType.EXPENSE -> tr("Витрата", "Expense")
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.toggleType(type) },
                        label = { Text(typeLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HistorySummaryCard(visible)

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (visible.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = tr("Записів історії немає", "No history records"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visible) { entry ->
                        HistoryRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySummaryCard(entries: List<HistoryEntry>) {
    val income = entries.filter { it.sign > 0 }.sumOf { it.amount }
    val outflow = entries.filter { it.sign < 0 }.sumOf { it.amount }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(tr("Надходження", "Income"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(
                    "+${String.format("%,.2f", income)} ₴",
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(tr("Витрати", "Outflow"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(
                    "-${String.format("%,.2f", outflow)} ₴",
                    fontWeight = FontWeight.Bold,
                    color = AccentRed
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val (icon: ImageVector, color: Color) = when (entry.type) {
        HistoryEntryType.PURCHASE -> Icons.Outlined.ShoppingCart to AccentBlue
        HistoryEntryType.SALE -> Icons.Outlined.Sell to AccentGreen
        HistoryEntryType.WRITEOFF -> Icons.Outlined.RemoveCircle to AccentRed
        HistoryEntryType.EXPENSE -> Icons.Default.MoneyOff to AccentOrange
    }
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
            IOSIconChip(icon = icon, tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val localTypeLabel = when (entry.type) {
                    HistoryEntryType.PURCHASE -> tr("Закупівля", "Purchase")
                    HistoryEntryType.SALE -> tr("Продаж", "Sale")
                    HistoryEntryType.WRITEOFF -> tr("Списання", "Writeoff")
                    HistoryEntryType.EXPENSE -> tr("Витрата", "Expense")
                }
                Text(
                    text = localTypeLabel + if (entry.productName.isNotEmpty()) " • ${entry.productName}" else "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (entry.counterparty.isNotEmpty()) {
                    Text(
                        text = entry.counterparty + (entry.quantity?.let { " • $it ${tr("шт.", "pcs")}" } ?: ""),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (entry.comment.isNotEmpty()) {
                    Text(
                        text = entry.comment,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = dateFormat.format(Date(entry.date)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            val sign = if (entry.sign > 0) "+" else "-"
            Text(
                text = "$sign${String.format("%,.2f", entry.amount)} ₴",
                fontWeight = FontWeight.Bold,
                color = if (entry.sign > 0) AccentGreen else AccentRed
            )
        }
    }
}

@Composable
private fun HistorySortMenu(
    current: HistorySort,
    onSelect: (HistorySort) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(
            imageVector = Icons.Default.Sort,
            contentDescription = tr("Сортування", "Sort"),
            tint = MaterialTheme.colorScheme.primary
        )
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(
            text = { Text(tr("Дата ↓ (новіші зверху)", "Date ↓ (newer first)") + if (current == HistorySort.DATE_DESC) "  ✓" else "") },
            onClick = { onSelect(HistorySort.DATE_DESC); open = false }
        )
        DropdownMenuItem(
            text = { Text(tr("Дата ↑ (старіші зверху)", "Date ↑ (older first)") + if (current == HistorySort.DATE_ASC) "  ✓" else "") },
            onClick = { onSelect(HistorySort.DATE_ASC); open = false }
        )
    }
}
