package com.billbao.app.data.repository

import android.content.Context
import com.billbao.app.data.database.BillDatabaseHelper
import com.billbao.app.data.model.Bill
import com.billbao.app.data.model.LedgerSummary
import com.billbao.app.data.model.MonthlyReport
import com.billbao.app.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class BillRepository(context: Context) {
    private val dbHelper = BillDatabaseHelper(context.applicationContext)

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _ledgerSummary = MutableStateFlow(LedgerSummary())
    val ledgerSummary: StateFlow<LedgerSummary> = _ledgerSummary.asStateFlow()

    private val _distinctMonths = MutableStateFlow<List<String>>(emptyList())
    val distinctMonths: StateFlow<List<String>> = _distinctMonths.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val allBills = dbHelper.getAllBills()
        val summary = dbHelper.getLedgerSummary()
        val months = dbHelper.getDistinctMonths()
        val profile = dbHelper.getUserProfile()

        _bills.value = allBills
        _ledgerSummary.value = summary
        _distinctMonths.value = months
        _userProfile.value = profile
    }

    suspend fun addBill(bill: Bill): Long = withContext(Dispatchers.IO) {
        val id = dbHelper.insertBill(bill)
        refresh()
        id
    }

    suspend fun updateBill(bill: Bill): Int = withContext(Dispatchers.IO) {
        val rows = dbHelper.updateBill(bill)
        refresh()
        rows
    }

    suspend fun deleteBill(id: Long): Int = withContext(Dispatchers.IO) {
        val rows = dbHelper.deleteBill(id)
        refresh()
        rows
    }

    suspend fun getMonthlyReport(yearMonth: String): MonthlyReport = withContext(Dispatchers.IO) {
        dbHelper.getMonthlyReport(yearMonth)
    }

    suspend fun updateProfile(profile: UserProfile): Int = withContext(Dispatchers.IO) {
        val rows = dbHelper.updateUserProfile(profile)
        refresh()
        rows
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dbHelper.clearAllData()
        refresh()
    }
}
