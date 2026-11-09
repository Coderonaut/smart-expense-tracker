package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val rateToUSD: Double // 1 USD = rateToUSD * Currency
) {
    USD("USD", "$", "US Dollar", 1.0),
    EUR("EUR", "€", "Euro", 0.92),
    GBP("GBP", "£", "British Pound", 0.79),
    INR("INR", "₹", "Indian Rupee", 83.25),
    JPY("JPY", "¥", "Japanese Yen", 154.50),
    CAD("CAD", "CA$", "Canadian Dollar", 1.36),
    AUD("AUD", "A$", "Australian Dollar", 1.52);

    companion object {
        fun convert(amount: Double, from: Currency, to: Currency): Double {
            if (from == to) return amount
            // Convert to base USD first, then to target
            val inUSD = amount / from.rateToUSD
            return inUSD * to.rateToUSD
        }

        fun formatAmount(amount: Double, currency: Currency): String {
            return when (currency) {
                JPY -> String.format("%s%,.0f", currency.symbol, amount)
                INR -> String.format("%s%,.2f", currency.symbol, amount)
                else -> String.format("%s%,.2f", currency.symbol, amount)
            }
        }
    }
}

enum class ExpenseCategory(
    val id: String,
    val title: String,
    val color: Color,
    val keywords: List<String>
) {
    GROCERIES(
        "groceries",
        "Groceries",
        CatGroceries,
        listOf("supermarket", "walmart", "whole foods", "trader joe", "kroger", "safeway", "target", "market", "grocery", "aldi", "costco")
    ),
    DINING(
        "dining",
        "Food & Dining",
        CatFood,
        listOf("starbucks", "mcdonald", "subway", "burger", "pizza", "coffee", "restaurant", "cafe", "taco", "diner", "bakery", "chipotle")
    ),
    TRANSPORT(
        "transport",
        "Transportation",
        CatTransport,
        listOf("uber", "lyft", "shell", "chevron", "gas", "fuel", "metro", "transit", "train", "airline", "delta", "parking", "flight")
    ),
    BILLS(
        "bills",
        "Bills & Utilities",
        CatBills,
        listOf("electric", "water", "internet", "wifi", "verizon", "at&t", "utility", "lease", "rent", "recharge", "t-mobile", "telecom", "fiber")
    ),
    SHOPPING(
        "shopping",
        "Shopping & Tech",
        CatShopping,
        listOf("amazon", "apple", "best buy", "ebay", "clothing", "electronics", "zara", "nike", "gadget", "store")
    ),
    HEALTH(
        "health",
        "Health & Medical",
        CatHealth,
        listOf("pharmacy", "cvs", "walgreens", "hospital", "clinic", "dental", "doctor", "medicine", "gym", "fitness")
    ),
    ENTERTAINMENT(
        "entertainment",
        "Entertainment",
        CatEntertainment,
        listOf("netflix", "spotify", "hulu", "disney", "cinema", "movie", "theater", "game", "steam", "concert")
    ),
    OTHER(
        "other",
        "Miscellaneous",
        CatOther,
        listOf()
    );

    companion object {
        fun detectCategory(text: String): ExpenseCategory {
            val lower = text.lowercase()
            for (category in entries) {
                if (category.keywords.any { lower.contains(it) }) {
                    return category
                }
            }
            return OTHER
        }

        fun fromId(id: String): ExpenseCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: OTHER
        }
    }
}
