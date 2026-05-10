package com.numisproerp.data.repository

import com.numisproerp.data.dao.ClientForSelection
import com.numisproerp.data.dao.ClientWithBalance
import com.numisproerp.data.dao.ProductForSelection
import com.numisproerp.data.dao.ProductInStock
import com.numisproerp.data.dao.ProductStockInfo
import com.numisproerp.data.dao.ProductWithStock
import com.numisproerp.data.dao.PurchaseWithProductName
import com.numisproerp.data.dao.SaleWithClientName
import com.numisproerp.data.dao.SaleWithProductName
import com.numisproerp.data.dao.SupplierForSelection
import com.numisproerp.data.dao.SupplierWithBalance
import com.numisproerp.data.dao.WriteoffWithProductName
import com.numisproerp.data.database.AppDatabase
import com.numisproerp.data.entities.Client
import com.numisproerp.data.entities.OtherExpense
import com.numisproerp.data.entities.Product
import com.numisproerp.data.entities.Purchase
import com.numisproerp.data.entities.Sale
import com.numisproerp.data.entities.Supplier
import com.numisproerp.data.entities.Writeoff
import com.numisproerp.data.entities.CollectionItem
import com.numisproerp.data.entities.Note
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
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

    suspend fun getDistinctMaterials(): List<String> {
        return withContext(Dispatchers.IO) {
            database.productDao().getDistinctMaterials()
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

    suspend fun getCatalogImageMap(): Map<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                val catalogItems = database.catalogDao().getAllItemsSync()
                catalogItems.filter { it.imageUrlFront.isNotEmpty() }
                    .associate { it.name to it.imageUrlFront }
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    suspend fun getCatalogImagePairMap(): Map<String, Pair<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val catalogItems = database.catalogDao().getAllItemsSync()
                catalogItems.filter { it.imageUrlFront.isNotEmpty() || it.imageUrlBack.isNotEmpty() }
                    .associate { it.name to Pair(it.imageUrlFront, it.imageUrlBack) }
            } catch (e: Exception) {
                emptyMap()
            }
        }
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

    suspend fun getAllExpensesSync(): List<OtherExpense> {
        return withContext(Dispatchers.IO) {
            database.otherExpenseDao().getAllExpensesSync()
        }
    }

    // ==================== WRITEOFFS ====================

    suspend fun insertWriteoff(writeoff: Writeoff) {
        return withContext(Dispatchers.IO) {
            database.writeoffDao().insert(writeoff)
        }
    }

    suspend fun getAllWriteoffs(): List<Writeoff> {
        return withContext(Dispatchers.IO) {
            database.writeoffDao().getAll()
        }
    }

    suspend fun getAllWriteoffsWithProductName(): List<WriteoffWithProductName> {
        return withContext(Dispatchers.IO) {
            database.writeoffDao().getAllWithProductName()
        }
    }

    suspend fun getTotalWriteoffsSum(): Double = withContext(Dispatchers.IO) {
        database.writeoffDao().getTotalSum() ?: 0.0
    }

    suspend fun getWriteoffsSumByDateRange(startDate: Long, endDate: Long): Double =
        withContext(Dispatchers.IO) {
            database.writeoffDao().getSumByDateRange(startDate, endDate) ?: 0.0
        }

    // ==================== COLLECTION (МОЯ КОЛЕКЦІЯ) ====================

    /**
     * Додає товар до «Моєї колекції» (п. 12-13 ТЗ). Створюється:
     *  - запис у `collection_items` (з фото, описом, оціночною вартістю);
     *  - дублюючий `Product` з тим самим ID, щоб товар з'являвся у каталозі та
     *    був доступний у вибірках Sale/Stock.
     * Не створюється запис у `purchases`, тому грошовий баланс не зменшується,
     * а середня закупочна ціна = 0 → весь дохід при продажі = чистий прибуток.
     */
    suspend fun addCollectionItem(item: CollectionItem) {
        withContext(Dispatchers.IO) {
            val productEquivalent = Product(
                catalogId = item.collectionId,
                name = item.name,
                series = item.series,
                material = item.material,
                nominal = item.nominal,
                category = item.category,
                quality = item.quality,
                photoPath = item.photoPath
            )
            database.productDao().insert(productEquivalent)
            database.collectionItemDao().insert(item)
        }
    }

    suspend fun updateCollectionItem(item: CollectionItem) {
        withContext(Dispatchers.IO) {
            database.collectionItemDao().update(item)
            // Підтримуємо синхронну метадату на стороні products (назва, фото, серія, тощо).
            val product = database.productDao().getProductById(item.collectionId)
            if (product != null) {
                database.productDao().insert(
                    product.copy(
                        name = item.name,
                        series = item.series,
                        material = item.material,
                        nominal = item.nominal,
                        category = item.category,
                        quality = item.quality,
                        photoPath = item.photoPath
                    )
                )
            }
        }
    }

    suspend fun deleteCollectionItem(item: CollectionItem) {
        withContext(Dispatchers.IO) {
            database.collectionItemDao().delete(item)
            // Окремо не видаляємо Product, бо може бути пов'язана історія
            // продажів (FOREIGN KEY-подібні залежності) — товар просто
            // перестане числитися у складі (collection_items.quantity = 0).
        }
    }

    /**
     * Додає існуючий товар до колекції БЕЗ перезапису Product-запису.
     * Повертає false якщо товар вже в колекції.
     */
    suspend fun addExistingProductToCollection(item: CollectionItem): Boolean {
        return withContext(Dispatchers.IO) {
            val existing = database.collectionItemDao().getById(item.collectionId)
            if (existing != null) return@withContext false
            database.collectionItemDao().insert(item)
            true
        }
    }

    fun getAllCollectionItems(): Flow<List<CollectionItem>> =
        database.collectionItemDao().getAll()

    suspend fun getCollectionItemById(id: String): CollectionItem? =
        withContext(Dispatchers.IO) {
            database.collectionItemDao().getById(id)
        }

    suspend fun getCollectionTotalEstimatedValue(): Double = withContext(Dispatchers.IO) {
        database.collectionItemDao().getTotalEstimatedValue() ?: 0.0
    }

    suspend fun getCollectionCount(): Int = withContext(Dispatchers.IO) {
        database.collectionItemDao().getCount()
    }

    // ==================== RECENT TRANSACTIONS ====================

    suspend fun getAllPurchasesWithDetails() = withContext(Dispatchers.IO) {
        database.purchaseDao().getRecentWithDetails(Int.MAX_VALUE)
    }

    suspend fun getAllSalesWithDetails() = withContext(Dispatchers.IO) {
        database.saleDao().getRecentWithDetails(Int.MAX_VALUE)
    }

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

    /**
     * Повне очищення всіх таблиць (товари, контрагенти, операції, колекція).
     * Каталог НБУ не очищується — він імпортується окремою кнопкою з Excel
     * і не вважається "робочими даними". Викликається з Налаштувань після
     * подвійного підтвердження.
     *
     * Атомарність: усі 8 DELETE виконуються всередині `database.withTransaction`,
     * тобто або всі викочуються, або жоден. `NonCancellable` гарантує, що навіть
     * якщо користувач вийде зі Settings (composition-scope cancel) — операція
     * добіжить до кінця, а не лишить БД у частково очищеному стані.
     */
    // ==================== NOTES (МОЇ ЗАМІТКИ) ====================

    fun getAllNotes(): Flow<List<Note>> = database.noteDao().getAll()

    suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) { database.noteDao().insert(note) }
    }

    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) { database.noteDao().update(note) }
    }

    suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) { database.noteDao().delete(note) }
    }

    suspend fun getDueReminders(timestamp: Long): List<Note> {
        return withContext(Dispatchers.IO) { database.noteDao().getDueReminders(timestamp) }
    }

    // ==================== SALES HISTORY (ALL) ====================

    suspend fun getAllSalesWithClientName(): List<SaleWithClientName> {
        return withContext(Dispatchers.IO) {
            database.saleDao().getAllSalesWithClientName()
        }
    }

    // ==================== CLEAR ALL DATA ====================

    suspend fun clearAllData() = withContext(NonCancellable + Dispatchers.IO) {
        database.withTransaction {
            // Спочатку дочірні таблиці (operations), потім довідники
            database.writeoffDao().deleteAll()
            database.saleDao().deleteAll()
            database.purchaseDao().deleteAll()
            database.otherExpenseDao().deleteAll()
            database.noteDao().deleteAll()
            database.collectionItemDao().deleteAll()
            database.productDao().deleteAll()
            database.clientDao().deleteAll()
            database.supplierDao().deleteAll()
        }
    }
}
