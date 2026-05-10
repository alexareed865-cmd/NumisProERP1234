package com.numisproerp.di

import android.content.Context
import androidx.room.Room
import com.numisproerp.data.dao.CatalogDao
import com.numisproerp.data.dao.ClientDao
import com.numisproerp.data.dao.OtherExpenseDao
import com.numisproerp.data.dao.ProductDao
import com.numisproerp.data.dao.PurchaseDao
import com.numisproerp.data.dao.NoteDao
import com.numisproerp.data.dao.SaleDao
import com.numisproerp.data.dao.SupplierDao
import com.numisproerp.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides Room database, DAOs та інфраструктурні залежності.
 *
 * Міграції:
 *  - Усі майбутні Migration'и додаємо у [AppDatabase.MIGRATIONS] і реєструємо тут через
 *    [Room.databaseBuilder.addMigrations]. Користувач, що вже має базу, отримує реальну
 *    міграцію без втрати даних.
 *  - [Room.databaseBuilder.fallbackToDestructiveMigrationFrom] — компроміс ТІЛЬКИ для
 *    дуже старих версій (1..10), у яких історично використовувалось
 *    `fallbackToDestructiveMigration()` без збережених схем; більше нікуди ми
 *    дані не зносимо.
 *  - [Room.databaseBuilder.fallbackToDestructiveMigrationOnDowngrade] — на випадок
 *    випадкового пониження версії БД.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "numisproerp_database"
        )
            .addMigrations(*AppDatabase.MIGRATIONS)
            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideClientDao(db: AppDatabase): ClientDao = db.clientDao()

    @Provides
    fun provideSupplierDao(db: AppDatabase): SupplierDao = db.supplierDao()

    @Provides
    fun providePurchaseDao(db: AppDatabase): PurchaseDao = db.purchaseDao()

    @Provides
    fun provideSaleDao(db: AppDatabase): SaleDao = db.saleDao()

    @Provides
    fun provideOtherExpenseDao(db: AppDatabase): OtherExpenseDao = db.otherExpenseDao()

    @Provides
    fun provideCatalogDao(db: AppDatabase): CatalogDao = db.catalogDao()

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
}
