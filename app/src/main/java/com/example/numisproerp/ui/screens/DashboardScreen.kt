package com.numisproerp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.numisproerp.R
import com.numisproerp.data.settings.AppTheme
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.navigation.Screen
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.theme.LocalAppTheme
import com.numisproerp.ui.viewmodel.DashboardViewModel
import com.numisproerp.ui.viewmodel.DashboardData
import com.numisproerp.ui.viewmodel.RecentTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardData by viewModel.dashboardData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        DashboardContent(
            data = dashboardData,
            onNavigateToStock = { navController.navigate(Screen.Stock.route) },
            onNavigateToClients = { navController.navigate(Screen.Clients.route) },
            onNavigateToReports = { navController.navigate(Screen.Reports.route) },
            onNavigateToPurchase = { navController.navigate(Screen.Purchase.route) },
            onNavigateToSale = { navController.navigate(Screen.Sale.route) },
            onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
            onNavigateToDocuments = { navController.navigate(Screen.MyCollection.route) },
            onNavigateToSuppliers = { navController.navigate(Screen.Suppliers.route) },
            onNavigateToDetails = { type, title ->
                navController.navigate("details/$type/$title")
            }
        )
    }
}

@Composable
fun DashboardContent(
    data: DashboardData,
    onNavigateToStock: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToPurchase: () -> Unit,
    onNavigateToSale: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToDetails: (String, String) -> Unit
) {
    val currentDate = SimpleDateFormat("LLLL yyyy", Locale("uk")).format(Date())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DashboardHeader(currentDate = currentDate)
        }

        item {
            val balanceTitle = tr("Загальний баланс", "Total balance")
            StatsCardClickable(
                title = balanceTitle,
                value = String.format("%,.2f ₴", data.totalBalance),
                valueColor = if (data.totalBalance >= 0) AccentGreen else AccentRed,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onNavigateToDetails("balance", balanceTitle) }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val purchasesTitle = tr("Закупівлі", "Purchases")
                val profitTitle = tr("Прибуток", "Profit")
                MonthlyStatCardClickable(
                    modifier = Modifier.weight(1f),
                    title = purchasesTitle,
                    value = String.format("%,.2f ₴", data.monthlyPurchases),
                    icon = Icons.Outlined.LocalAtm,
                    iconColor = AccentOrange,
                    onClick = { onNavigateToDetails("purchases", purchasesTitle) }
                )
                MonthlyStatCardClickable(
                    modifier = Modifier.weight(1f),
                    title = profitTitle,
                    value = String.format("%,.2f ₴", data.monthlyProfit),
                    icon = Icons.Outlined.BarChart,
                    iconColor = AccentGreen,
                    onClick = { onNavigateToDetails("profit", profitTitle) }
                )
            }
        }

        item {
            SectionHeader(title = tr("Швидкий доступ", "Quick access"))
        }

        item {
            QuickAccessRow(
                onPurchaseClick = onNavigateToPurchase,
                onSaleClick = onNavigateToSale,
                onStockClick = onNavigateToStock,
                onClientsClick = onNavigateToClients
            )
        }

        item {
            QuickAccessRow2(
                onReportsClick = onNavigateToReports,
                onSuppliersClick = onNavigateToSuppliers,
                onExpensesClick = onNavigateToExpenses,
                onDocumentsClick = onNavigateToDocuments
            )
        }

        item {
            SectionHeader(title = tr("Останні операції", "Recent operations"))
        }

        items(data.recentTransactions) { transaction ->
            RecentTransactionItem(transaction = transaction)
        }
    }
}

@Composable
private fun DashboardHeader(currentDate: String) {
    val theme = LocalAppTheme.current
    if (theme == AppTheme.OLEG_SMILE) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.oleg_smile_emblem),
                contentDescription = "OlegSmile",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(36.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "OlegSmile",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = tr("NumisProERP — облік та каталогізація", "NumisProERP — accounting & catalog"),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = currentDate,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    } else {
        Column {
            Text(
                text = "NumisProERP",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tr("Облік та каталогізація", "Accounting & catalog"),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = currentDate,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun StatsCardClickable(
    title: String,
    value: String,
    valueColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun MonthlyStatCardClickable(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
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
                icon = icon,
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun QuickAccessRow(
    onPurchaseClick: () -> Unit,
    onSaleClick: () -> Unit,
    onStockClick: () -> Unit,
    onClientsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickAccessButton(
            icon = Icons.Outlined.LocalAtm,
            tileRes = R.drawable.tile_purchase,
            label = tr("Закупівля", "Purchase"),
            onClick = onPurchaseClick
        )
        QuickAccessButton(
            icon = Icons.Filled.ShoppingCart,
            tileRes = R.drawable.tile_sale,
            label = tr("Продаж", "Sale"),
            onClick = onSaleClick
        )
        QuickAccessButton(
            icon = Icons.Filled.Store,
            tileRes = R.drawable.tile_stock,
            label = tr("Склад", "Stock"),
            onClick = onStockClick
        )
        QuickAccessButton(
            icon = Icons.Filled.People,
            tileRes = R.drawable.tile_clients,
            label = tr("Клієнти", "Clients"),
            onClick = onClientsClick
        )
    }
}

@Composable
fun QuickAccessRow2(
    onReportsClick: () -> Unit,
    onSuppliersClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onDocumentsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickAccessButton(
            icon = Icons.Outlined.BarChart,
            tileRes = R.drawable.tile_reports,
            label = tr("Звіти", "Reports"),
            onClick = onReportsClick
        )
        QuickAccessButton(
            icon = Icons.Filled.People,
            tileRes = R.drawable.tile_suppliers,
            label = tr("Постачальники", "Suppliers"),
            onClick = onSuppliersClick
        )
        QuickAccessButton(
            icon = Icons.Outlined.Receipt,
            tileRes = R.drawable.tile_expenses,
            label = tr("Витрати", "Expenses"),
            onClick = onExpensesClick
        )
        QuickAccessButton(
            icon = Icons.Outlined.BarChart,
            tileRes = R.drawable.tile_collection,
            label = tr("Моя колекція", "Collection"),
            onClick = onDocumentsClick
        )
    }
}

@Composable
fun QuickAccessButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tileRes: Int,
    label: String,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current
    val tileSize = 76.dp
    val tileCorner = 18.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(tileSize)
                .clip(RoundedCornerShape(tileCorner))
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (theme == AppTheme.OLEG_SMILE) {
                Image(
                    painter = painterResource(id = tileRes),
                    contentDescription = label,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                IOSIconChip(
                    icon = icon,
                    tint = MaterialTheme.colorScheme.primary,
                    chipSize = 64.dp,
                    iconSize = 32.dp,
                    cornerRadius = 14.dp,
                    backgroundAlpha = 0.12f,
                    contentDescription = label
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun RecentTransactionItem(transaction: RecentTransaction) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(transaction.date))
    val isPurchase = transaction.type == "Покупка"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IOSIconChip(
                icon = if (isPurchase) Icons.Outlined.LocalAtm else Icons.Filled.ShoppingCart,
                tint = if (isPurchase) AccentOrange else AccentGreen
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.productName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "${transaction.counterpartyName} • $formattedDate",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = if (isPurchase) "-${String.format("%,.2f", transaction.amount)} ₴" else "+${String.format("%,.2f", transaction.amount)} ₴",
                fontWeight = FontWeight.Bold,
                color = if (isPurchase) AccentOrange else AccentGreen
            )
        }
    }
}
