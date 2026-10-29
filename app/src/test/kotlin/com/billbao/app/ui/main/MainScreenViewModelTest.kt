package com.billbao.app.ui.main

import com.billbao.app.data.model.Bill
import com.billbao.app.data.model.CategorySpend
import com.billbao.app.data.model.LedgerSummary
import com.billbao.app.data.model.MonthlyReport
import junit.framework.TestCase.assertEquals
import org.junit.Test

class BillbaoLogicTest {

    @Test
    fun testFreshStartLedgerIsZero() {
        // Fresh start test: Ledger with no bills must have 0.00 total
        val summary = LedgerSummary()
        assertEquals(0.0, summary.totalBalance)
        assertEquals(0.0, summary.totalPaid)
        assertEquals(0.0, summary.totalPending)
        assertEquals(0, summary.totalBillsCount)
    }

    @Test
    fun testDynamicMonthlyReportComputation() {
        val testBills = listOf(
            Bill(id = 1, title = "Electricity", amount = 120.0, category = "Utilities", billDate = "2026-08-10", status = "PAID"),
            Bill(id = 2, title = "Water", amount = 50.0, category = "Utilities", billDate = "2026-08-18", status = "PAID"),
            Bill(id = 3, title = "Groceries", amount = 200.0, category = "Groceries", billDate = "2026-08-25", status = "PENDING"),
            Bill(id = 4, title = "September Rent", amount = 1500.0, category = "Rent", billDate = "2026-09-01", status = "PAID")
        )

        // Filter for August 2026
        val augustBills = testBills.filter { it.billDate.startsWith("2026-08") }
        val augustTotal = augustBills.sumOf { it.amount }
        val augustPaid = augustBills.filter { it.status == "PAID" }.sumOf { it.amount }
        val augustPending = augustBills.filter { it.status == "PENDING" }.sumOf { it.amount }

        assertEquals(3, augustBills.size)
        assertEquals(370.0, augustTotal)
        assertEquals(170.0, augustPaid)
        assertEquals(200.0, augustPending)

        // Empty month test (e.g. July 2026 where no bills were uploaded)
        val julyBills = testBills.filter { it.billDate.startsWith("2026-07") }
        val julyTotal = julyBills.sumOf { it.amount }
        assertEquals(0, julyBills.size)
        assertEquals(0.0, julyTotal)
    }

    @Test
    fun testMonthTitleFormatting() {
        val title = MainScreenViewModel.formatMonthTitle("2026-08")
        assertEquals("August 2026", title)
    }
}
