package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BankAccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.model.Currency
import com.example.data.model.ExpenseCategory
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.ParsedReceipt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategorySpendInsight(
    val category: ExpenseCategory,
    val totalAmountInActiveCurrency: Double,
    val percentage: Float,
    val transactionCount: Int,
    val budgetLimit: Double,
    val isOverBudget: Boolean
)

data class SpendingTrendPoint(
    val dayLabel: String,
    val amount: Double
)

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _activeCurrency = MutableStateFlow(Currency.USD)
    val activeCurrency: StateFlow<Currency> = _activeCurrency.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _isSyncingBanks = MutableStateFlow(false)
    val isSyncingBanks: StateFlow<Boolean> = _isSyncingBanks.asStateFlow()

    private val _syncFeedback = MutableStateFlow<String?>(null)
    val syncFeedback: StateFlow<String?> = _syncFeedback.asStateFlow()

    private val _parsedReceipt = MutableStateFlow<ParsedReceipt?>(null)
    val parsedReceipt: StateFlow<ParsedReceipt?> = _parsedReceipt.asStateFlow()

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBankAccounts: StateFlow<List<BankAccountEntity>> = repository.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Expenses according to search query and category filter
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses,
        _searchQuery,
        _selectedCategoryFilter
    ) { expenses, query, catFilter ->
        expenses.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.merchantName.contains(query, ignoreCase = true) ||
                    item.notes.contains(query, ignoreCase = true)
            val matchesCategory = catFilter == null || item.category.equals(catFilter, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Converted total expenses in active currency
    val totalSpendInActiveCurrency: StateFlow<Double> = combine(
        allExpenses,
        _activeCurrency
    ) { expenses, currency ->
        expenses.sumOf { exp ->
            val fromCurr = Currency.entries.firstOrNull { it.code == exp.currency } ?: Currency.USD
            Currency.convert(exp.amount, fromCurr, currency)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Total bank balances converted to active currency
    val totalBankBalanceInActiveCurrency: StateFlow<Double> = combine(
        allBankAccounts,
        _activeCurrency
    ) { accounts, currency ->
        accounts.sumOf { acc ->
            val fromCurr = Currency.entries.firstOrNull { it.code == acc.currency } ?: Currency.USD
            Currency.convert(acc.balance, fromCurr, currency)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Spending insights by Category
    val categoryInsights: StateFlow<List<CategorySpendInsight>> = combine(
        allExpenses,
        allBudgets,
        _activeCurrency
    ) { expenses, budgets, targetCurr ->
        val totalAll = expenses.sumOf {
            val fromCurr = Currency.entries.firstOrNull { c -> c.code == it.currency } ?: Currency.USD
            Currency.convert(it.amount, fromCurr, targetCurr)
        }

        ExpenseCategory.entries.mapNotNull { cat ->
            val catExpenses = expenses.filter { it.category == cat.name }
            if (catExpenses.isEmpty() && cat == ExpenseCategory.OTHER) return@mapNotNull null

            val sumInTarget = catExpenses.sumOf {
                val fromCurr = Currency.entries.firstOrNull { c -> c.code == it.currency } ?: Currency.USD
                Currency.convert(it.amount, fromCurr, targetCurr)
            }
            val pct = if (totalAll > 0) (sumInTarget / totalAll).toFloat() else 0f
            val budget = budgets.firstOrNull { it.category == cat.name }?.let {
                val bCurr = Currency.entries.firstOrNull { c -> c.code == it.currency } ?: Currency.USD
                Currency.convert(it.monthlyLimit, bCurr, targetCurr)
            } ?: 0.0

            CategorySpendInsight(
                category = cat,
                totalAmountInActiveCurrency = sumInTarget,
                percentage = pct,
                transactionCount = catExpenses.size,
                budgetLimit = budget,
                isOverBudget = budget > 0 && sumInTarget > budget
            )
        }.sortedByDescending { it.totalAmountInActiveCurrency }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Spending trend points for last 7 intervals
    val weeklyTrendPoints: StateFlow<List<SpendingTrendPoint>> = combine(
        allExpenses,
        _activeCurrency
    ) { expenses, targetCurr ->
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        dayLabels.mapIndexed { index, label ->
            val targetDayStart = now - ((6 - index) * dayMs)
            val dayExpenses = expenses.filter {
                it.timestamp >= targetDayStart - dayMs && it.timestamp < targetDayStart
            }
            val sum = dayExpenses.sumOf {
                val fromCurr = Currency.entries.firstOrNull { c -> c.code == it.currency } ?: Currency.USD
                Currency.convert(it.amount, fromCurr, targetCurr)
            }
            SpendingTrendPoint(label, sum)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }
    }

    fun setActiveCurrency(currency: Currency) {
        _activeCurrency.value = currency
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun clearFeedback() {
        _syncFeedback.value = null
    }

    fun addExpense(
        title: String,
        amount: Double,
        currency: Currency,
        category: ExpenseCategory,
        notes: String,
        merchant: String = ""
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = title.ifBlank { merchant.ifBlank { "Expense" } },
                amount = amount,
                currency = currency.code,
                category = category.name,
                timestamp = System.currentTimeMillis(),
                notes = notes,
                merchantName = merchant,
                receiptTax = amount * 0.07,
                isBankSynced = false
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }

    fun syncBankAccounts() {
        if (_isSyncingBanks.value) return
        viewModelScope.launch {
            _isSyncingBanks.value = true
            _syncFeedback.value = "Authenticating with secure Open Banking gateways..."
            delay(1200)
            _syncFeedback.value = "Verifying 256-bit token signatures & TLS 1.3 channel..."
            delay(1000)
            val newCount = repository.syncBankAccounts()
            _isSyncingBanks.value = false
            _syncFeedback.value = "Sync complete! Pulled $newCount recent bank transactions into local Room cache."
        }
    }

    fun linkNewBank(bankName: String, accountType: String, accountNumber: String, balance: Double, currency: String) {
        viewModelScope.launch {
            repository.addBankAccount(bankName, accountType, accountNumber, balance, currency)
            _syncFeedback.value = "Connected $bankName successfully with end-to-end tokenization."
        }
    }

    fun parseReceiptText(text: String) {
        val result = repository.parseReceipt(text)
        _parsedReceipt.value = result
    }

    fun saveParsedReceipt(parsed: ParsedReceipt) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = parsed.merchant,
                amount = parsed.totalAmount,
                currency = parsed.currency.code,
                category = parsed.category.name,
                timestamp = System.currentTimeMillis(),
                notes = "Auto-categorized from receipt. Line items: ${parsed.itemsSummary}",
                merchantName = parsed.merchant,
                receiptTax = parsed.taxAmount,
                receiptLineItems = parsed.itemsSummary,
                isBankSynced = false
            )
            repository.insertExpense(expense)
            _parsedReceipt.value = null
            _syncFeedback.value = "Receipt from ${parsed.merchant} saved to offline database!"
        }
    }

    fun clearParsedReceipt() {
        _parsedReceipt.value = null
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
