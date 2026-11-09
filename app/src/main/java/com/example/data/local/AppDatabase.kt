package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BankAccountDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.entity.BankAccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ExpenseEntity

@Database(
    entities = [
        ExpenseEntity::class,
        BankAccountEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_expense.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
