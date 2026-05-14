package com.numisproerp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.numisproerp.data.database.AppDatabase
import com.numisproerp.di.AppDatabaseEntryPoint
import dagger.hilt.android.EntryPointAccessors
import com.numisproerp.data.entities.Purchase
import com.numisproerp.data.entities.Sale
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.viewmodel.MonthlyStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Універсальний екран деталей, на який потрапляє користувач, тапнувши
 * по інформаційній картці на дашборді.
 *
 *  - `type == "balance"` → екран з агрегованою статистикою (як «Звіти»):
 *    плитки KPI, графік динаміки за місяцями і помісячний розклад.
 *  - `type == "purchases"` → перелік закупівель у стилі «Історії» з
 *    можливістю фільтрації за постачальниками і сортування.
 *  - `type == "profit"` → перелік продажів у стилі «Історії» з фільтром
 *    за клієнтами і сортуванням.
 */
@Composable
fun DetailsScreen(
    navController: NavHostController,
    type: String,
    title: String
) {
    when (type) {
        "balance" -> BalanceDetailsView(navController = navController, title = title)
        "purchases" -> PurchasesDetailsView(navController = navController, title = title)
        "profit" -> SalesDetailsView(navController = navController, title = title)
        else -> BalanceDetailsView(navController = navController, title = title)
    }
}

// ============================================================================
// "Загальний баланс" — KPI-плитки + графік за місяцями + помісячний список,
// як в екрані «Звіти» зі скріншоту.
// ============================================================================

@Composable
private fun BalanceDetailsView(
    navController: NavHostController,
    title: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val database: AppDatabase = remember {
        EntryPointAccessors
            .fromApplication(context, AppDatabaseEntryPoint::class.java)
            .appDatabase()
    }
    var isLoading by remember { mutableStateOf(true) }
    var totalRevenue by remember { mutableStateOf(0.0) }
    var totalExpenses by remember { mutableStateOf(0.0) }
    var netProfit by remember { mutableStateOf(0.0) }
    var stockValue by remember { mutableStateOf(0.0) }
    var monthlyData by remember { mutableStateOf<List<MonthlyStats>>(emptyList()) }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val purchases = database.purchaseDao().getAllPurchases()
            val sales = database.saleDao().getAllSales()
            val writeoffs = runCatching { database.writeoffDao().getAll() }.getOrDefault(emptyList())
            val expenses = runCatching { database.otherExpenseDao().getAllExpensesSync() }.getOrDefault(emptyList())

            val revenue = sales.sumOf { it.totalAmount }
            val purchasesSum = purchases.sumOf { it.totalAmount }
            val writeoffsSum = writeoffs.sumOf { it.totalAmount }
            val expensesSum = expenses.sumOf { it.amount }
            val expensesTotal = purchasesSum + writeoffsSum + expensesSum
            val profit = revenue - expensesTotal

            // Залишки на складі (Σ stock × avgPurchasePrice). Якщо метод недоступний — 0.
            val stockTotal = runCatching {
                val products = database.productDao().getProductsInStock().first()
                products.sumOf { it.currentStock * it.avgPurchasePrice }
            }.getOrDefault(0.0)

            val months = buildMonthlyStats(purchases, sales, writeoffs.map { it.date to it.totalAmount })

            totalRevenue = revenue
            totalExpenses = expensesTotal
            netProfit = profit
            stockValue = stockTotal
            monthlyData = months
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = tr("Назад", "Back"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BalanceStatCard(
                                modifier = Modifier.weight(1f),
                                title = tr("Дохід", "Revenue"),
                                value = String.format("%,.2f", totalRevenue),
                                icon = Icons.Filled.ShoppingCart,
                                iconColor = AccentGreen,
                                valueColor = AccentGreen
                            )
                            BalanceStatCard(
                                modifier = Modifier.weight(1f),
                                title = tr("Витрати", "Expenses"),
                                value = String.format("%,.2f", totalExpenses),
                                icon = Icons.Filled.Warning,
                                iconColor = AccentRed,
                                valueColor = AccentRed
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BalanceStatCard(
                                modifier = Modifier.weight(1f),
                                title = tr("Чистий прибуток", "Net profit"),
                                value = String.format("%,.2f", netProfit),
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                iconColor = if (netProfit >= 0) AccentGreen else AccentRed,
                                valueColor = if (netProfit >= 0) AccentGreen else AccentRed
                            )
                            BalanceStatCard(
                                modifier = Modifier.weight(1f),
                                title = tr("Залишки на складі", "Stock balance"),
                                value = String.format("%,.2f", stockValue),
                                icon = Icons.Filled.Store,
                                iconColor = AccentBlue,
                                valueColor = AccentBlue
                            )
                        }
                    }
                    item {
                        Text(
                            text = tr("Динаміка за місяцями (поточний зверху)", "Monthly dynamics (current on top)"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    item {
                        MonthlyDynamicsChartCard(monthlyData = monthlyData)
                    }
                    items(monthlyData) { monthData ->
                        BalanceMonthRow(monthData = monthData)
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    valueColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IOSIconChip(icon = icon, tint = iconColor)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$value ₴",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun BalanceMonthRow(monthData: MonthlyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(text = monthData.month, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(tr("Дохід", "Revenue"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("${String.format("%,.2f", monthData.revenue)} ₴", fontSize = 13.sp, color = AccentGreen)
                }
                Column {
                    Text(tr("Витрати", "Expenses"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("${String.format("%,.2f", monthData.expenses)} ₴", fontSize = 13.sp, color = AccentRed)
                }
                Column {
                    Text(tr("Прибуток", "Profit"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        "${String.format("%,.2f", monthData.profit)} ₴",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (monthData.profit >= 0) AccentGreen else AccentRed
                    )
                }
            }
        }
    }
}

/**
 * Простий лінійний графік за місяцями для тристрічкового відображення
 * дохід / витрати / прибуток. Малюється Canvas-ом, як на референсі.
 *
 * Порядок місяців на осі X — від найдавнішого зліва до поточного справа,
 * відповідно [monthlyData] подається з топ-поточним; всередині розгортаємо.
 */
@Composable
private fun MonthlyDynamicsChartCard(monthlyData: List<MonthlyStats>) {
    if (monthlyData.isEmpty()) return
    val chronological = remember(monthlyData) { monthlyData.asReversed() }
    val maxValue = remember(monthlyData) {
        val values = monthlyData.flatMap { listOf(it.revenue, it.expenses, it.profit) }
        val absMax = values.maxOf { kotlin.math.abs(it) }
        // Округлюємо вгору до зручного числа для осі (15k / 30k / 45k / 60k)
        when {
            absMax <= 0.0 -> 60000.0
            absMax < 1500 -> 1500.0
            absMax < 15000 -> kotlin.math.ceil(absMax / 1500.0) * 1500.0
            else -> kotlin.math.ceil(absMax / 15000.0) * 15000.0
        }
    }
    val firstLabel = chronological.firstOrNull()?.month ?: ""
    val lastLabel = chronological.lastOrNull()?.month ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthlyData.firstOrNull()?.month ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                LegendDot(color = AccentGreen, label = tr("Дохід", "Revenue"))
                Spacer(modifier = Modifier.width(8.dp))
                LegendDot(color = AccentRed, label = tr("Витрати", "Expenses"))
                Spacer(modifier = Modifier.width(8.dp))
                LegendDot(color = AccentOrange, label = tr("Прибуток", "Profit"))
            }
            Spacer(modifier = Modifier.height(12.dp))
            LineChart(
                months = chronological,
                maxValue = maxValue,
                firstMonthLabel = firstLabel,
                lastMonthLabel = lastLabel
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

/**
 * Малює лінійний графік 3-х серій (дохід/витрати/прибуток) + осі.
 */
@Composable
private fun LineChart(
    months: List<MonthlyStats>,
    maxValue: Double,
    firstMonthLabel: String,
    lastMonthLabel: String
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val gridColor = onSurface.copy(alpha = 0.10f)
    val axisColor = onSurface.copy(alpha = 0.45f)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = axisColor
    )
    val density = LocalDensity.current
    val axisLabelPxLeft = with(density) { 56.dp.toPx() }
    val axisLabelPxBottom = with(density) { 22.dp.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val plotLeft = axisLabelPxLeft
        val plotTop = 8f
        val plotRight = size.width - 8f
        val plotBottom = size.height - axisLabelPxBottom
        val plotW = plotRight - plotLeft
        val plotH = plotBottom - plotTop
        if (plotW <= 0f || plotH <= 0f) return@Canvas

        // 4 ліній сітки + 0 ₴
        val steps = 4
        for (i in 0..steps) {
            val y = plotTop + plotH * (i / steps.toFloat())
            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1f
            )
            val gridValue = maxValue * (1f - i / steps.toFloat())
            val labelText = AnnotatedString(formatAxisValue(gridValue))
            val measured = textMeasurer.measure(text = labelText, style = labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = labelText,
                style = labelStyle,
                topLeft = Offset(
                    x = plotLeft - measured.size.width - 4f,
                    y = y - measured.size.height / 2f
                )
            )
        }

        val n = months.size
        if (n == 0) return@Canvas
        val stepX = if (n > 1) plotW / (n - 1).toFloat() else plotW

        fun toY(value: Double): Float {
            val ratio = (value / maxValue).coerceIn(-1.0, 1.0)
            return plotBottom - (plotH * ratio).toFloat()
        }

        fun drawSeries(getValue: (MonthlyStats) -> Double, color: Color) {
            val path = Path()
            months.forEachIndexed { idx, m ->
                val x = plotLeft + stepX * idx
                val y = toY(getValue(m))
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 4f)
            )
            months.forEachIndexed { idx, m ->
                val x = plotLeft + stepX * idx
                val y = toY(getValue(m))
                drawCircle(color = color, radius = 5f, center = Offset(x, y))
            }
        }

        drawSeries({ it.expenses }, AccentRed)
        drawSeries({ it.revenue }, AccentGreen)
        drawSeries({ it.profit }, AccentOrange)

        // Підписи місяців: показуємо до 6 підписів, щоб не перекривались
        val labelsToShow = if (n <= 6) (0 until n).toList() else (0 until n).filter { it % 2 == 0 }
        labelsToShow.forEach { idx ->
            val label = months[idx].month
            val short = shortMonth(label)
            val measured = textMeasurer.measure(text = AnnotatedString(short), style = labelStyle)
            val x = (plotLeft + stepX * idx) - measured.size.width / 2f
            drawText(
                textMeasurer = textMeasurer,
                text = AnnotatedString(short),
                style = labelStyle,
                topLeft = Offset(x = x.coerceIn(0f, size.width - measured.size.width), y = plotBottom + 4f)
            )
        }

        // Тонка нульова лінія
        val zeroY = toY(0.0)
        if (zeroY in plotTop..plotBottom) {
            drawLine(
                color = axisColor.copy(alpha = 0.4f),
                start = Offset(plotLeft, zeroY),
                end = Offset(plotRight, zeroY),
                strokeWidth = 1f
            )
        }
    }
}

private fun formatAxisValue(value: Double): String {
    val v = kotlin.math.abs(value)
    val sign = if (value < 0) "-" else ""
    return when {
        v >= 1000 -> "$sign${String.format("%,d", v.toInt())} ₴"
        else -> "$sign${String.format("%.0f", v)} ₴"
    }
}

private fun shortMonth(full: String): String {
    // Очікуємо щось на кшталт "Травень 2026" — повертаємо "Трав 2026"
    val parts = full.trim().split(" ")
    if (parts.size < 2) return full
    val name = parts[0]
    val year = parts[1]
    val short = name.take(3)
    return "$short $year"
}

private fun buildMonthlyStats(
    purchases: List<Purchase>,
    sales: List<Sale>,
    writeoffs: List<Pair<Long, Double>>
): List<MonthlyStats> {
    val result = mutableListOf<MonthlyStats>()
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("LLLL yyyy", Locale("uk"))
    for (offset in 0 until 6) {
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MONTH, -offset)
        val start = startOfMonth(calendar.timeInMillis)
        val end = endOfMonth(calendar.timeInMillis)
        val revenue = sales.filter { it.date in start..end }.sumOf { it.totalAmount }
        val purchaseSum = purchases.filter { it.date in start..end }.sumOf { it.totalAmount }
        val writeoffSum = writeoffs.filter { it.first in start..end }.sumOf { it.second }
        val expenses = purchaseSum + writeoffSum
        result.add(
            MonthlyStats(
                month = dateFormat.format(Date(calendar.timeInMillis))
                    .replaceFirstChar { it.titlecase(Locale("uk")) },
                revenue = revenue,
                expenses = expenses,
                profit = revenue - expenses
            )
        )
    }
    return result
}

private fun startOfMonth(timestamp: Long): Long {
    val c = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return c.timeInMillis
}

private fun endOfMonth(timestamp: Long): Long {
    val c = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }
    return c.timeInMillis
}

// ============================================================================
// Внутрішня модель та сортування для покупок / продажів
// ============================================================================

private data class TxRecord(
    val id: String,
    val date: Long,
    val totalAmount: Double,
    val counterpartyId: String,
    val counterpartyName: String,
    val productName: String
)

private enum class TxSort {
    DATE_DESC,
    DATE_ASC,
    COUNTERPARTY_ASC,
    COUNTERPARTY_DESC,
    AMOUNT_DESC,
    AMOUNT_ASC
}

// ============================================================================
// "Закупівлі" — список закупівель з фільтром і сортуванням за постачальниками
// ============================================================================

@Composable
private fun PurchasesDetailsView(
    navController: NavHostController,
    title: String
) {
    TxListView(
        navController = navController,
        title = title,
        counterpartyFilterTitle = tr("Фільтр по постачальнику", "Filter by supplier"),
        unknownCounterpartyText = tr("Невідомий постачальник", "Unknown supplier"),
        transactionLabel = tr("Закупівля", "Purchase"),
        counterpartySortAsc = tr("Постачальник A→Я", "Supplier A→Z"),
        counterpartySortDesc = tr("Постачальник Я→A", "Supplier Z→A"),
        rowIcon = Icons.Outlined.ShoppingCart,
        rowIconTint = AccentBlue,
        sign = -1,
        emptyText = tr("Немає даних про закупівлі", "No purchase data"),
        loadData = { db ->
            val purchases = db.purchaseDao().getAllPurchases()
            val suppMap = mutableMapOf<String, String>()
            purchases.map { it.supplierId }.distinct().forEach { sid ->
                val s = db.supplierDao().getSupplierById(sid)
                suppMap[sid] = s?.name ?: ""
            }
            // Імена товарів — для відображення в рядках (best effort)
            val productMap = mutableMapOf<String, String>()
            runCatching {
                db.productDao().getProductsForSelection().forEach { p ->
                    productMap[p.catalogId] = p.name
                }
            }
            val records = purchases.map { p ->
                TxRecord(
                    id = p.purchaseId,
                    date = p.date,
                    totalAmount = p.totalAmount,
                    counterpartyId = p.supplierId,
                    counterpartyName = suppMap[p.supplierId]?.ifBlank { null } ?: "",
                    productName = productMap[p.catalogId] ?: ""
                )
            }
            records to suppMap
        }
    )
}

// ============================================================================
// "Прибуток" — список продажів з фільтром і сортуванням за клієнтами
// ============================================================================

@Composable
private fun SalesDetailsView(
    navController: NavHostController,
    title: String
) {
    TxListView(
        navController = navController,
        title = title,
        counterpartyFilterTitle = tr("Фільтр по клієнту", "Filter by client"),
        unknownCounterpartyText = tr("Невідомий клієнт", "Unknown client"),
        transactionLabel = tr("Продаж", "Sale"),
        counterpartySortAsc = tr("Клієнт A→Я", "Client A→Z"),
        counterpartySortDesc = tr("Клієнт Я→A", "Client Z→A"),
        rowIcon = Icons.Outlined.Sell,
        rowIconTint = AccentGreen,
        sign = +1,
        emptyText = tr("Немає даних про продажі", "No sales data"),
        loadData = { db ->
            val sales = db.saleDao().getAllSales()
            val cliMap = mutableMapOf<String, String>()
            sales.map { it.clientId }.distinct().forEach { cid ->
                val c = db.clientDao().getClientById(cid)
                cliMap[cid] = c?.name ?: ""
            }
            val productMap = mutableMapOf<String, String>()
            runCatching {
                db.productDao().getProductsForSelection().forEach { p ->
                    productMap[p.catalogId] = p.name
                }
            }
            val records = sales.map { s ->
                TxRecord(
                    id = s.saleId,
                    date = s.date,
                    totalAmount = s.totalAmount,
                    counterpartyId = s.clientId,
                    counterpartyName = cliMap[s.clientId]?.ifBlank { null } ?: "",
                    productName = productMap[s.catalogId] ?: ""
                )
            }
            records to cliMap
        }
    )
}

@Composable
private fun TxListView(
    navController: NavHostController,
    title: String,
    counterpartyFilterTitle: String,
    unknownCounterpartyText: String,
    transactionLabel: String,
    counterpartySortAsc: String,
    counterpartySortDesc: String,
    rowIcon: ImageVector,
    rowIconTint: Color,
    sign: Int,
    emptyText: String,
    loadData: suspend (AppDatabase) -> Pair<List<TxRecord>, Map<String, String>>
) {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val database: AppDatabase = remember {
        EntryPointAccessors
            .fromApplication(context, AppDatabaseEntryPoint::class.java)
            .appDatabase()
    }
    var isLoading by remember { mutableStateOf(true) }
    var records by remember { mutableStateOf<List<TxRecord>>(emptyList()) }
    var counterpartyNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var sort by remember { mutableStateOf(TxSort.DATE_DESC) }
    var showFilter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val (loaded, names) = loadData(database)
            records = loaded
            counterpartyNames = names
            isLoading = false
        }
    }

    val filtered by remember(records, selectedIds, sort) {
        derivedStateOf {
            val filteredList = if (selectedIds.isEmpty()) records
            else records.filter { it.counterpartyId in selectedIds }
            when (sort) {
                TxSort.DATE_DESC -> filteredList.sortedByDescending { it.date }
                TxSort.DATE_ASC -> filteredList.sortedBy { it.date }
                TxSort.COUNTERPARTY_ASC -> filteredList.sortedWith(
                    compareBy(
                        { (counterpartyNames[it.counterpartyId] ?: "").lowercase(Locale.getDefault()) },
                        { -it.date }
                    )
                )
                TxSort.COUNTERPARTY_DESC -> filteredList.sortedWith(
                    compareByDescending<TxRecord> { (counterpartyNames[it.counterpartyId] ?: "").lowercase(Locale.getDefault()) }
                        .thenByDescending { it.date }
                )
                TxSort.AMOUNT_DESC -> filteredList.sortedByDescending { it.totalAmount }
                TxSort.AMOUNT_ASC -> filteredList.sortedBy { it.totalAmount }
            }
        }
    }

    val totalAmount by remember(filtered) {
        derivedStateOf { filtered.sumOf { it.totalAmount } }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = tr("Назад", "Back"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showFilter = !showFilter }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = tr("Фільтр", "Filter"),
                            tint = if (selectedIds.isNotEmpty()) AccentOrange else MaterialTheme.colorScheme.primary
                        )
                    }
                    TxSortMenu(
                        current = sort,
                        onSelect = { sort = it },
                        counterpartySortAsc = counterpartySortAsc,
                        counterpartySortDesc = counterpartySortDesc
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Фільтр-чіпи по контрагентам (горизонтальний скрол)
                if (counterpartyNames.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        val sortedCounterparties = counterpartyNames.entries.sortedBy {
                            (it.value.ifBlank { unknownCounterpartyText }).lowercase(Locale.getDefault())
                        }
                        items(sortedCounterparties) { entry ->
                            val displayName = entry.value.ifBlank { unknownCounterpartyText }
                            val selected = entry.key in selectedIds
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedIds = if (selected) selectedIds - entry.key else selectedIds + entry.key
                                },
                                label = { Text(displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                AnimatedVisibility(visible = showFilter) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = counterpartyFilterTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (selectedIds.isNotEmpty()) {
                                    TextButton(onClick = { selectedIds = emptySet() }) {
                                        Text(tr("Скинути", "Reset"), fontSize = 12.sp, color = AccentOrange)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            counterpartyNames.entries.sortedBy {
                                (it.value.ifBlank { unknownCounterpartyText }).lowercase(Locale.getDefault())
                            }.forEach { (id, name) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = id in selectedIds,
                                        onCheckedChange = { checked ->
                                            selectedIds = if (checked) selectedIds + id else selectedIds - id
                                        }
                                    )
                                    Text(text = name.ifBlank { unknownCounterpartyText }, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                // Підсумкова сума
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${tr("Загальна сума", "Total amount")}:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${if (sign < 0) "-" else "+"}${String.format("%,.2f", totalAmount)} ₴",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sign < 0) AccentOrange else AccentGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = emptyText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered) { rec ->
                            TxRow(
                                record = rec,
                                counterpartyDisplay = (counterpartyNames[rec.counterpartyId] ?: "").ifBlank { unknownCounterpartyText },
                                transactionLabel = transactionLabel,
                                icon = rowIcon,
                                iconTint = rowIconTint,
                                sign = sign
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TxSortMenu(
    current: TxSort,
    onSelect: (TxSort) -> Unit,
    counterpartySortAsc: String,
    counterpartySortDesc: String
) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Sort,
            contentDescription = tr("Сортування", "Sort"),
            tint = MaterialTheme.colorScheme.primary
        )
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(
            text = { Text(tr("Дата ↓ (новіші зверху)", "Date ↓ (newer first)") + if (current == TxSort.DATE_DESC) "  ✓" else "") },
            onClick = { onSelect(TxSort.DATE_DESC); open = false }
        )
        DropdownMenuItem(
            text = { Text(tr("Дата ↑ (старіші зверху)", "Date ↑ (older first)") + if (current == TxSort.DATE_ASC) "  ✓" else "") },
            onClick = { onSelect(TxSort.DATE_ASC); open = false }
        )
        DropdownMenuItem(
            text = { Text(counterpartySortAsc + if (current == TxSort.COUNTERPARTY_ASC) "  ✓" else "") },
            onClick = { onSelect(TxSort.COUNTERPARTY_ASC); open = false }
        )
        DropdownMenuItem(
            text = { Text(counterpartySortDesc + if (current == TxSort.COUNTERPARTY_DESC) "  ✓" else "") },
            onClick = { onSelect(TxSort.COUNTERPARTY_DESC); open = false }
        )
        DropdownMenuItem(
            text = { Text(tr("Сума ↓", "Amount ↓") + if (current == TxSort.AMOUNT_DESC) "  ✓" else "") },
            onClick = { onSelect(TxSort.AMOUNT_DESC); open = false }
        )
        DropdownMenuItem(
            text = { Text(tr("Сума ↑", "Amount ↑") + if (current == TxSort.AMOUNT_ASC) "  ✓" else "") },
            onClick = { onSelect(TxSort.AMOUNT_ASC); open = false }
        )
    }
}

@Composable
private fun TxRow(
    record: TxRecord,
    counterpartyDisplay: String,
    transactionLabel: String,
    icon: ImageVector,
    iconTint: Color,
    sign: Int
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IOSIconChip(icon = icon, tint = iconTint)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transactionLabel + if (record.productName.isNotEmpty()) " • ${record.productName}" else "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = counterpartyDisplay,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = dateFormat.format(Date(record.date)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "${if (sign < 0) "-" else "+"}${String.format("%,.2f", record.totalAmount)} ₴",
                fontWeight = FontWeight.Bold,
                color = if (sign < 0) AccentOrange else AccentGreen
            )
        }
    }
}
