package com.billbao.app.data.model

data class Bill(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val billDate: String, // Format: YYYY-MM-DD
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val status: String = "PAID", // "PAID" or "PENDING"
    val notes: String = "",
    val imageUri: String? = null,
    val isTaxDeductible: Boolean = true,
    val taxShieldAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

data class LedgerSummary(
    val totalBalance: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val totalBillsCount: Int = 0,
    val totalTaxShield: Double = 0.0,
    val taxShieldPercentage: Float = 100f
)

data class CategorySpend(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val count: Int
)

data class MonthlyReport(
    val yearMonth: String, // "YYYY-MM"
    val displayTitle: String, // "October 2026"
    val totalAmount: Double,
    val totalCount: Int,
    val paidAmount: Double,
    val pendingAmount: Double,
    val totalTaxShield: Double = 0.0,
    val categoryBreakdown: List<CategorySpend>,
    val bills: List<Bill>
)

data class UserProfile(
    val name: String = "Restaurant Owner",
    val role: String = "Owner & Manager",
    val businessName: String = "My Restaurant",
    val taxId: String = "0105567089123",
    val branch: String = "Head Office (00000)",
    val email: String = "owner@restaurant.co",
    val phone: String = "+66 82 456 7890",
    val address: String = "Bangkok, Thailand",
    val currency: String = "THB",
    val currencySymbol: String = "฿",
    val taxShieldRate: Double = 0.07 // 7% VAT standard
)
