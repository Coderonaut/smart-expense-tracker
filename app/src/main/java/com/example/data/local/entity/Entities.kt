package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val currency: String, // USD, EUR, INR, etc.
    val category: String, // GROCERIES, DINING, etc.
    val timestamp: Long,
    val notes: String = "",
    val merchantName: String = "",
    val receiptTax: Double = 0.0,
    val isBankSynced: Boolean = false,
    val bankAccountId: Long? = null,
    val receiptLineItems: String = ""
)

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,
    val accountType: String, // Checking, Savings, Credit Card
    val accountNumberMasked: String, // e.g. "•••• 4821"
    val balance: Double,
    val currency: String,
    val isConnected: Boolean = true,
    val lastSyncedTimestamp: Long = System.currentTimeMillis(),
    val syncTokenHash: String = "sha256_mock_token_oauth2"
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val currency: String
)
