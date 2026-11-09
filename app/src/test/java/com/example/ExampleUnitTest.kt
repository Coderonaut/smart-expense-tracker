package com.example

import com.example.data.model.Currency
import com.example.data.model.ExpenseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyConversion() {
        val usdAmount = 100.0
        val inEUR = Currency.convert(usdAmount, Currency.USD, Currency.EUR)
        assertEquals(92.0, inEUR, 0.01)

        val backToUSD = Currency.convert(inEUR, Currency.EUR, Currency.USD)
        assertEquals(100.0, backToUSD, 0.01)
    }

    @Test
    fun testCategoryAutoDetection() {
        assertEquals(ExpenseCategory.GROCERIES, ExpenseCategory.detectCategory("Trader Joe's Supermarket"))
        assertEquals(ExpenseCategory.DINING, ExpenseCategory.detectCategory("Starbucks Coffee"))
        assertEquals(ExpenseCategory.TRANSPORT, ExpenseCategory.detectCategory("Uber Airport Ride"))
        assertEquals(ExpenseCategory.SHOPPING, ExpenseCategory.detectCategory("Apple Store Purchase"))
        assertEquals(ExpenseCategory.BILLS, ExpenseCategory.detectCategory("Sonic Telecom Fiber"))
    }

    @Test
    fun testCurrencyFormatting() {
        val formattedUSD = Currency.formatAmount(42.50, Currency.USD)
        assertEquals("$42.50", formattedUSD)
    }
}
