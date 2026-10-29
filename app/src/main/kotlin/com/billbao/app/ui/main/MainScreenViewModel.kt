package com.billbao.app.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.billbao.app.data.export.ExcelCsvExporter
import com.billbao.app.data.export.PdfReportGenerator
import com.billbao.app.data.model.Bill
import com.billbao.app.data.model.LedgerSummary
import com.billbao.app.data.model.MonthlyReport
import com.billbao.app.data.model.UserProfile
import com.billbao.app.data.repository.BillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {
    val repository = BillRepository(application)

    private val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    private val _selectedMonth = MutableStateFlow(currentMonth)
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true) // Default to sleek dark mode like screenshot
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("EN")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val _monthlyReport = MutableStateFlow(
        MonthlyReport(
            yearMonth = currentMonth,
            displayTitle = formatMonthTitle(currentMonth),
            totalAmount = 0.0,
            totalCount = 0,
            paidAmount = 0.0,
            pendingAmount = 0.0,
            totalTaxShield = 0.0,
            categoryBreakdown = emptyList(),
            bills = emptyList()
        )
    )
    val monthlyReport: StateFlow<MonthlyReport> = _monthlyReport.asStateFlow()

    val ledgerSummary: StateFlow<LedgerSummary> = repository.ledgerSummary
    val distinctMonths: StateFlow<List<String>> = repository.distinctMonths
    val userProfile: StateFlow<UserProfile> = repository.userProfile

    val filteredBills: StateFlow<List<Bill>> = combine(
        repository.bills,
        _searchQuery,
        _categoryFilter
    ) { allBills, query, category ->
        allBills.filter { bill ->
            val matchesQuery = query.isBlank() ||
                bill.title.contains(query, ignoreCase = true) ||
                bill.category.contains(query, ignoreCase = true) ||
                bill.notes.contains(query, ignoreCase = true)
            val matchesCategory = category == null || bill.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.refresh()
            loadMonthlyReport(_selectedMonth.value)
        }
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleLanguage() {
        _selectedLanguage.value = if (_selectedLanguage.value == "EN") "TH" else "EN"
    }

    fun selectMonth(yearMonth: String) {
        _selectedMonth.value = yearMonth
        loadMonthlyReport(yearMonth)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun addBill(bill: Bill) {
        viewModelScope.launch {
            repository.addBill(bill)
            loadMonthlyReport(_selectedMonth.value)
        }
    }

    fun updateBill(bill: Bill) {
        viewModelScope.launch {
            repository.updateBill(bill)
            loadMonthlyReport(_selectedMonth.value)
        }
    }

    fun deleteBill(id: Long) {
        viewModelScope.launch {
            repository.deleteBill(id)
            loadMonthlyReport(_selectedMonth.value)
        }
    }

    fun toggleBillStatus(bill: Bill) {
        val newStatus = if (bill.status == "PAID") "PENDING" else "PAID"
        updateBill(bill.copy(status = newStatus))
    }

    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun resetLedgerToZero() {
        viewModelScope.launch {
            repository.clearAllData()
            loadMonthlyReport(_selectedMonth.value)
        }
    }

    fun getPreviousMonth(yearMonth: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            val cal = Calendar.getInstance().apply {
                time = sdf.parse(yearMonth) ?: Date()
                add(Calendar.MONTH, -1)
            }
            sdf.format(cal.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun exportPdfReport(context: Context, yearMonth: String) {
        viewModelScope.launch {
            val currentRep = repository.getMonthlyReport(yearMonth)
            val prevMonthStr = getPreviousMonth(yearMonth)
            val prevRep = if (prevMonthStr.isNotBlank()) repository.getMonthlyReport(prevMonthStr) else null
            PdfReportGenerator.generateAndDownloadReport(
                context = context,
                currentReport = currentRep,
                previousReport = prevRep,
                profile = userProfile.value
            )
        }
    }

    fun exportExcel(context: Context, monthFilter: String? = null) {
        viewModelScope.launch {
            val billsToExport = if (monthFilter != null) {
                repository.getMonthlyReport(monthFilter).bills
            } else {
                repository.bills.value
            }
            ExcelCsvExporter.exportAndDownloadLedger(
                context = context,
                bills = billsToExport,
                monthFilter = monthFilter
            )
        }
    }

    private fun loadMonthlyReport(yearMonth: String) {
        viewModelScope.launch {
            _monthlyReport.value = repository.getMonthlyReport(yearMonth)
        }
    }

    companion object {
        fun formatMonthTitle(yearMonth: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM", Locale.US)
                val formatter = SimpleDateFormat("MMMM yyyy", Locale.US)
                val date = parser.parse(yearMonth)
                if (date != null) formatter.format(date) else yearMonth
            } catch (e: Exception) {
                yearMonth
            }
        }
    }
}
