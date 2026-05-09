package com.numisproerp.data.repository

import com.numisproerp.data.dao.ClientForSelection
import com.numisproerp.data.dao.ClientWithBalance
import com.numisproerp.data.dao.ProductForSelection
import com.numisproerp.data.dao.ProductInStock
import com.numisproerp.data.dao.ProductStockInfo
import com.numisproerp.data.dao.ProductWithStock
import com.numisproerp.data.dao.PurchaseWithProductName
import com.numisproerp.data.dao.SaleWithProductName
import com.numisproerp.data.dao.SupplierForSelection
import com.numisproerp.data.dao.SupplierWithBalance
import com.numisproerp.data.database.AppDatabase
import com.numisproerp.data.entities.Client
import com.numisproerp.data.entities.OtherExpense
import com.numisproerp.data.entities.Product
import com.numisproerp.data.entities.Purchase
import com.numisproerp.data.entities.Sale
import com.numisproerp.data.entities.Supplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class TransactionSummary(
    val id: String,
    val type: String,
    val amount: Double,
    val date: Long,
    val productName: String,
    val counterpartyName: String
)

@Singleton
class Repository @Inject constructor(
    private val database: AppDatabase
) {

    // ==================== PRODUCTS ====================

    fun getProductsWithStock(searchQuery: String): Flow<List<ProductWithStock>> {
        return database.productDao().getProductsWithStock(searchQuery)
    }

    fun getProductsWithStockByCategory(category: String): Flow<List<ProductWithStock>> {
        return database.productDao().getProductsWithStockByCategory(category)
    }

    suspend fun getDistinctCategories(): List<String> {
        return withContext(Dispatchers.IO) {
            database.productDao().getDistinctCategories()
        }
    }

    suspend fun insertProduct(product: Product) {
        return withContext(Dispatchers.IO) {
            database.productDao().insert(product)
        }
    }

    suspend fun getProductsForSelection(): List<ProductForSelection> {
        return withContext(Dispatchers.IO) {
            database.productDao().getProductsForSelection()
        }
    }

    fun getProductsInStock(): Flow<List<ProductInStock>> {
        return database.productDao().getProductsInStock()
    }

    fun getAllProducts(): Flow<List<Product>> {
        return database.productDao().getAllProducts()
    }

    suspend fun getProductById(catalogId: String): Product? {
        return withContext(Dispatchers.IO) {
            database.productDao().getProductById(catalogId)
        }
    }

    // ==================== CLIENTS ====================

    fun getClientsWithBalance(): Flow<List<ClientWithBalance>> {
        return database.clientDao().getClientsWithBalance()
    }

    suspend fun getClientsForSelection(): List<ClientForSelection> {
        return withContext(Dispatchers.IO) {
            database.clientDao().getClientsForSelection()
        }
    }

    suspend fun insertClient(client: Client) {
        return withContext(Dispatchers.IO) {
            database.clientDao().insert(client)
        }
    }

    suspend fun updateClient(client: Client) {
        return withContext(Dispatchers.IO) {
            database.clientDao().update(client)
        }
    }

    suspend fun deleteClient(clientId: String) {
        return withContext(Dispatchers.IO) {
            database.clientDao().deleteById(clientId)
        }
    }

    suspend fun getSalesHistoryForClient(clientId: String): List<SaleWithProductName> {
        return withContext(Dispatchers.IO) {
            database.saleDao().getSalesByClient(clientId)
        }
    }

    // ==================== SUPPLIERS ====================

    fun getSuppliersWithBalance(): Flow<List<SupplierWithBalance>> {
        return database.supplierDao().getSuppliersWithBalance()
    }

    suspend fun getSuppliersForSelection(): List<SupplierForSelection> {
        return withContext(Dispatchers.IO) {
            database.supplierDao().getSuppliersForSelection()
        }
    }

    suspend fun insertSupplier(supplier: Supplier) {
        return withContext(Dispatchers.IO) {
            database.supplierDao().insert(supplier)
        }
    }

    suspend fun updateSupplier(supplier: Supplier) {
        return withContext(Dispatchers.IO) {
            database.supplierDao().update(supplier)
        }
    }

    suspend fun deleteSupplier(supplierId: String) {
        return withContext(Dispatchers.IO) {
            database.supplierDao().deleteById(supplierId)
        }
    }

    // ==================== PURCHASES ====================

    suspend fun insertPurchase(purchase: Purchase) {
        return withContext(Dispatchers.IO) {
            database.purchaseDao().insert(purchase)
        }
    }

    suspend fun getTotalPurchasesSum(): Double = withContext(Dispatchers.IO) {
        database.purchaseDao().getTotalSum() ?: 0.0
    }

    suspend fun getPurchasesSumByDateRange(startDate: Long, endDate: Long): Double = withContext(Dispatchers.IO) {
        database.purchaseDao().getSumByDateRange(startDate, endDate) ?: 0.0
    }

    suspend fun getPurchaseHistoryForSupplier(supplierId: String): List<PurchaseWithProductName> {
        return withContext(Dispatchers.IO) {
            database.purchaseDao().getPurchasesBySupplier(supplierId)
        }
    }

    // ==================== SALES ====================

    suspend fun insertSale(sale: Sale) {
        return withContext(Dispatchers.IO) {
            database.saleDao().insert(sale)
        }
    }

    suspend fun getTotalSalesSum(): Double = withContext(Dispatchers.IO) {
        database.saleDao().getTotalSum() ?: 0.0
    }

    suspend fun getSalesSumByDateRange(startDate: Long, endDate: Long): Double = withContext(Dispatchers.IO) {
        database.saleDao().getSumByDateRange(startDate, endDate) ?: 0.0
    }

    suspend fun getProductStockInfo(catalogId: String): ProductStockInfo? {
        return withContext(Dispatchers.IO) {
            database.saleDao().getProductStockInfo(catalogId)
        }
    }

    suspend fun getAllSales(): List<Sale> {
        return withContext(Dispatchers.IO) {
            database.saleDao().getAllSales()
        }
    }

    // ==================== OTHER EXPENSES ====================

    fun getAllExpenses(): Flow<List<OtherExpense>> {
        return database.otherExpenseDao().getAllExpenses()
    }

    suspend fun getTotalOtherExpensesSum(): Double = withContext(Dispatchers.IO) {
        database.otherExpenseDao().getTotalSum() ?: 0.0
    }

    suspend fun insertOtherExpense(expense: OtherExpense) {
        return withContext(Dispatchers.IO) {
            database.otherExpenseDao().insert(expense)
        }
    }

    suspend fun getRecentExpenses(limit: Int): List<OtherExpense> {
        return withContext(Dispatchers.IO) {
            database.otherExpenseDao().getRecentExpenses(limit)
        }
    }

    // ==================== RECENT TRANSACTIONS ====================

    suspend fun getRecentTransactions(limit: Int): List<TransactionSummary> = withContext(Dispatchers.IO) {
        val recentPurchases = database.purchaseDao().getRecentWithDetails(limit)
        val recentSales = database.saleDao().getRecentWithDetails(limit)

        val allTransactions = mutableListOf<TransactionSummary>()

        recentPurchases.forEach { purchase ->
            allTransactions.add(
                TransactionSummary(
                    id = purchase.purchaseId,
                    type = "Покупка",
                    amount = purchase.totalAmount,
                    date = purchase.date,
                    productName = purchase.productName,
                    counterpartyName = purchase.supplierName
                )
            )
        }

        recentSales.forEach { sale ->
            allTransactions.add(
                TransactionSummary(
                    id = sale.saleId,
                    type = "Продаж",
                    amount = sale.totalAmount,
                    date = sale.date,
                    productName = sale.productName,
                    counterpartyName = sale.clientName
                )
            )
        }

        allTransactions.sortedByDescending { it.date }.take(limit)
    }
}
