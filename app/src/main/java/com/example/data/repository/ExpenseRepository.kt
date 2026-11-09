package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.BankAccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.model.Currency
import com.example.data.model.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ParsedReceipt(
    val merchant: String,
    val totalAmount: Double,
    val currency: Currency,
    val category: ExpenseCategory,
    val taxAmount: Double,
    val dateString: String,
    val itemsSummary: String,
    val rawText: String
)

class ExpenseRepository(private val database: AppDatabase) {
    private val expenseDao = database.expenseDao()
    private val bankAccountDao = database.bankAccountDao()
    private val budgetDao = database.budgetDao()

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allBankAccounts: Flow<List<BankAccountEntity>> = bankAccountDao.getAllBankAccounts()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingExpenses = allExpenses.firstOrNull() ?: emptyList()
        if (existingExpenses.isEmpty()) {
            val now = System.currentTimeMillis()
            val day = 86_400_000L

            val initialExpenses = listOf(
                ExpenseEntity(
                    title = "Trader Joe's Groceries",
                    amount = 84.50,
                    currency = "USD",
                    category = ExpenseCategory.GROCERIES.name,
                    timestamp = now - (day * 1),
                    merchantName = "Trader Joe's",
                    receiptTax = 6.20,
                    notes = "Organic fruits, sourdough, milk, almond butter",
                    receiptLineItems = "Almond Butter ($6.99), Organic Milk ($4.50), Apples ($5.20), Sourdough ($3.99)",
                    isBankSynced = true
                ),
                ExpenseEntity(
                    title = "Blue Bottle Espresso",
                    amount = 14.80,
                    currency = "USD",
                    category = ExpenseCategory.DINING.name,
                    timestamp = now - (day * 2),
                    merchantName = "Blue Bottle Coffee",
                    receiptTax = 1.10,
                    notes = "Oat flat white and almond croissant",
                    isBankSynced = true
                ),
                ExpenseEntity(
                    title = "Uber Airport Ride",
                    amount = 42.00,
                    currency = "USD",
                    category = ExpenseCategory.TRANSPORT.name,
                    timestamp = now - (day * 3),
                    merchantName = "Uber Technologies",
                    receiptTax = 3.50,
                    notes = "Ride to SFO Terminal 2",
                    isBankSynced = true
                ),
                ExpenseEntity(
                    title = "Eurostar Paris Ticket",
                    amount = 115.00,
                    currency = "EUR",
                    category = ExpenseCategory.TRANSPORT.name,
                    timestamp = now - (day * 4),
                    merchantName = "Eurostar International",
                    receiptTax = 12.00,
                    notes = "London St Pancras to Paris Gare du Nord",
                    isBankSynced = false
                ),
                ExpenseEntity(
                    title = "Apple Developer Sub",
                    amount = 99.00,
                    currency = "USD",
                    category = ExpenseCategory.SHOPPING.name,
                    timestamp = now - (day * 5),
                    merchantName = "Apple Services",
                    receiptTax = 8.25,
                    notes = "Annual iOS Developer Account Renewal",
                    isBankSynced = true
                ),
                ExpenseEntity(
                    title = "Mumbai Street Diner",
                    amount = 1250.00,
                    currency = "INR",
                    category = ExpenseCategory.DINING.name,
                    timestamp = now - (day * 6),
                    merchantName = "Bombay Canteen",
                    receiptTax = 62.50,
                    notes = "Team dinner celebration",
                    isBankSynced = true
                ),
                ExpenseEntity(
                    title = "High-speed Fiber Broadband",
                    amount = 65.00,
                    currency = "USD",
                    category = ExpenseCategory.BILLS.name,
                    timestamp = now - (day * 8),
                    merchantName = "Sonic Telecom",
                    receiptTax = 4.50,
                    notes = "Monthly gigabit internet bill",
                    isBankSynced = true
                ),
                ExpenseEntity(
                    title = "Cinema IMAX Tickets",
                    amount = 38.00,
                    currency = "USD",
                    category = ExpenseCategory.ENTERTAINMENT.name,
                    timestamp = now - (day * 10),
                    merchantName = "AMC Theatres",
                    receiptTax = 2.80,
                    notes = "Sci-fi blockbuster night with popcorn",
                    isBankSynced = false
                ),
                ExpenseEntity(
                    title = "Pharmacy Prescriptions",
                    amount = 26.40,
                    currency = "USD",
                    category = ExpenseCategory.HEALTH.name,
                    timestamp = now - (day * 12),
                    merchantName = "CVS Health",
                    receiptTax = 1.90,
                    notes = "Vitamins and allergy relief",
                    isBankSynced = true
                )
            )
            expenseDao.insertExpenses(initialExpenses)

            val initialAccounts = listOf(
                BankAccountEntity(
                    bankName = "Chase Sapphire Checking",
                    accountType = "Checking",
                    accountNumberMasked = "•••• 4921",
                    balance = 6840.50,
                    currency = "USD",
                    isConnected = true,
                    lastSyncedTimestamp = now - 1800000,
                    syncTokenHash = "enc_token_chase_a98f12d"
                ),
                BankAccountEntity(
                    bankName = "Revolut Multi-Currency",
                    accountType = "Savings & Travel",
                    accountNumberMasked = "•••• 8304",
                    balance = 2450.00,
                    currency = "EUR",
                    isConnected = true,
                    lastSyncedTimestamp = now - 3600000,
                    syncTokenHash = "enc_token_revolut_b41e88"
                ),
                BankAccountEntity(
                    bankName = "HDFC Priority Gold",
                    accountType = "Savings",
                    accountNumberMasked = "•••• 1982",
                    balance = 145000.00,
                    currency = "INR",
                    isConnected = true,
                    lastSyncedTimestamp = now - 7200000,
                    syncTokenHash = "enc_token_hdfc_c779df"
                )
            )
            bankAccountDao.insertAccounts(initialAccounts)

            val initialBudgets = listOf(
                BudgetEntity(category = ExpenseCategory.GROCERIES.name, monthlyLimit = 400.0, currency = "USD"),
                BudgetEntity(category = ExpenseCategory.DINING.name, monthlyLimit = 250.0, currency = "USD"),
                BudgetEntity(category = ExpenseCategory.TRANSPORT.name, monthlyLimit = 180.0, currency = "USD"),
                BudgetEntity(category = ExpenseCategory.SHOPPING.name, monthlyLimit = 200.0, currency = "USD"),
                BudgetEntity(category = ExpenseCategory.BILLS.name, monthlyLimit = 350.0, currency = "USD"),
                BudgetEntity(category = ExpenseCategory.ENTERTAINMENT.name, monthlyLimit = 150.0, currency = "USD"),
                BudgetEntity(category = ExpenseCategory.HEALTH.name, monthlyLimit = 100.0, currency = "USD")
            )
            budgetDao.insertBudgets(initialBudgets)
        }
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun addBankAccount(bankName: String, accountType: String, accountNumber: String, balance: Double, currency: String): Long = withContext(Dispatchers.IO) {
        val masked = if (accountNumber.length >= 4) "•••• " + accountNumber.takeLast(4) else "•••• $accountNumber"
        val account = BankAccountEntity(
            bankName = bankName,
            accountType = accountType,
            accountNumberMasked = masked,
            balance = balance,
            currency = currency,
            isConnected = true,
            lastSyncedTimestamp = System.currentTimeMillis(),
            syncTokenHash = "enc_token_" + UUID.randomUUID().toString().take(12)
        )
        bankAccountDao.insertAccount(account)
    }

    suspend fun syncBankAccounts(): Int = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val accounts = bankAccountDao.getAllBankAccounts().firstOrNull() ?: emptyList()
        var newTransactionsCount = 0

        for (acc in accounts) {
            bankAccountDao.updateLastSynced(acc.id, now)

            // Simulate pulling 1 recent transaction from bank feed
            val sampleMerchants = when (acc.currency) {
                "EUR" -> listOf(
                    Triple("Carrefour Express", 28.40, ExpenseCategory.GROCERIES),
                    Triple("Café de Flore", 19.50, ExpenseCategory.DINING),
                    Triple("SNCF Train", 45.00, ExpenseCategory.TRANSPORT)
                )
                "INR" -> listOf(
                    Triple("Swiggy Delivery", 480.00, ExpenseCategory.DINING),
                    Triple("D-Mart Supermarket", 1850.00, ExpenseCategory.GROCERIES),
                    Triple("Jio Fiber Prepaid", 799.00, ExpenseCategory.BILLS)
                )
                else -> listOf(
                    Triple("Whole Foods Market", 52.30, ExpenseCategory.GROCERIES),
                    Triple("Shell Fuel Station", 38.00, ExpenseCategory.TRANSPORT),
                    Triple("Starbucks Reserve", 8.75, ExpenseCategory.DINING)
                )
            }

            val picked = sampleMerchants.random()
            val newExpense = ExpenseEntity(
                title = picked.first,
                amount = picked.second,
                currency = acc.currency,
                category = picked.third.name,
                timestamp = now - (1000 * (10..3600).random()),
                notes = "Auto-synced from ${acc.bankName} (${acc.accountNumberMasked})",
                merchantName = picked.first,
                receiptTax = picked.second * 0.08,
                isBankSynced = true,
                bankAccountId = acc.id
            )
            expenseDao.insertExpense(newExpense)
            newTransactionsCount++
        }
        newTransactionsCount
    }

    suspend fun updateBudgetLimit(categoryName: String, newLimit: Double, currency: String) = withContext(Dispatchers.IO) {
        val budgets = budgetDao.getAllBudgets().firstOrNull() ?: emptyList()
        val existing = budgets.firstOrNull { it.category == categoryName }
        if (existing != null) {
            budgetDao.updateBudget(existing.copy(monthlyLimit = newLimit, currency = currency))
        } else {
            budgetDao.insertBudget(BudgetEntity(category = categoryName, monthlyLimit = newLimit, currency = currency))
        }
    }

    // Smart Receipt Parser: Heuristic and Regex NLP extraction
    fun parseReceipt(rawText: String): ParsedReceipt {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var detectedMerchant = "Receipt Store"
        var detectedTotal = 0.0
        var detectedTax = 0.0
        var detectedCurrency = Currency.USD
        var detectedCategory = ExpenseCategory.OTHER
        val itemsFound = mutableListOf<String>()

        val currencyRegex = Regex("""(\$|€|£|₹|¥|CAD|USD|EUR|GBP|INR)""")
        val priceRegex = Regex("""(?:Total|Amount Due|Balance Due|Grand Total|Net Total|Charge)?[\s:]*([$€£₹¥]?\s*\d+[.,]\d{2})""", RegexOption.IGNORE_CASE)
        val taxRegex = Regex("""(?:Tax|VAT|GST|Sales Tax)[\s:]*([$€£₹¥]?\s*\d+[.,]\d{2})""", RegexOption.IGNORE_CASE)

        if (lines.isNotEmpty()) {
            detectedMerchant = lines.first()
        }

        // Look for currency symbols
        for (line in lines) {
            when {
                line.contains("€") || line.contains("EUR", ignoreCase = true) -> detectedCurrency = Currency.EUR
                line.contains("£") || line.contains("GBP", ignoreCase = true) -> detectedCurrency = Currency.GBP
                line.contains("₹") || line.contains("INR", ignoreCase = true) -> detectedCurrency = Currency.INR
                line.contains("¥") || line.contains("JPY", ignoreCase = true) -> detectedCurrency = Currency.JPY
                line.contains("CA$") || line.contains("CAD", ignoreCase = true) -> detectedCurrency = Currency.CAD
                line.contains("$") || line.contains("USD", ignoreCase = true) -> detectedCurrency = Currency.USD
            }

            // Check for tax
            val taxMatch = taxRegex.find(line)
            if (taxMatch != null) {
                val numStr = taxMatch.groupValues[1].replace(Regex("[^0-9.]"), "")
                detectedTax = numStr.toDoubleOrNull() ?: 0.0
            }

            // Check for total
            if (line.contains("total", ignoreCase = true) || line.contains("amount", ignoreCase = true) || line.contains("balance", ignoreCase = true)) {
                val match = priceRegex.find(line)
                if (match != null) {
                    val numStr = match.groupValues[1].replace(Regex("[^0-9.]"), "")
                    val amt = numStr.toDoubleOrNull() ?: 0.0
                    if (amt > detectedTotal) detectedTotal = amt
                }
            } else if (line.matches(Regex(""".*\d+[.,]\d{2}.*"""))) {
                itemsFound.add(line)
            }
        }

        // Fallback total if no line had "total" explicitly
        if (detectedTotal == 0.0) {
            val allNumbers = Regex("""\d+[.,]\d{2}""").findAll(rawText).mapNotNull {
                it.value.replace(",", ".").toDoubleOrNull()
            }.toList()
            if (allNumbers.isNotEmpty()) {
                detectedTotal = allNumbers.maxOrNull() ?: 24.99
            } else {
                detectedTotal = 32.50
            }
        }

        // Smart Category Detection
        detectedCategory = ExpenseCategory.detectCategory("$detectedMerchant $rawText")

        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        val itemsSummary = if (itemsFound.isNotEmpty()) {
            itemsFound.take(3).joinToString(", ")
        } else {
            "Itemized receipt breakdown"
        }

        return ParsedReceipt(
            merchant = detectedMerchant,
            totalAmount = detectedTotal,
            currency = detectedCurrency,
            category = detectedCategory,
            taxAmount = detectedTax,
            dateString = dateStr,
            itemsSummary = itemsSummary,
            rawText = rawText
        )
    }
}
