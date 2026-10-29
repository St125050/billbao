package com.billbao.app.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.billbao.app.data.model.Bill
import com.billbao.app.data.model.CategorySpend
import com.billbao.app.data.model.LedgerSummary
import com.billbao.app.data.model.MonthlyReport
import com.billbao.app.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Locale

class BillDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "billbao.db"
        const val DATABASE_VERSION = 3

        const val TABLE_BILLS = "bills"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_BILL_DATE = "bill_date" // YYYY-MM-DD
        const val COLUMN_TYPE = "type" // EXPENSE or INCOME
        const val COLUMN_STATUS = "status" // PAID or PENDING
        const val COLUMN_NOTES = "notes"
        const val COLUMN_IMAGE_URI = "image_uri"
        const val COLUMN_IS_TAX_DEDUCTIBLE = "is_tax_deductible"
        const val COLUMN_TAX_SHIELD_AMOUNT = "tax_shield_amount"
        const val COLUMN_CREATED_AT = "created_at"

        const val TABLE_PROFILE = "profile"
        const val COLUMN_PROFILE_NAME = "name"
        const val COLUMN_PROFILE_ROLE = "role"
        const val COLUMN_PROFILE_BIZ_NAME = "business_name"
        const val COLUMN_PROFILE_TAX_ID = "tax_id"
        const val COLUMN_PROFILE_BRANCH = "branch"
        const val COLUMN_PROFILE_EMAIL = "email"
        const val COLUMN_PROFILE_PHONE = "phone"
        const val COLUMN_PROFILE_ADDRESS = "address"
        const val COLUMN_PROFILE_CURRENCY = "currency"
        const val COLUMN_PROFILE_SYMBOL = "currency_symbol"
        const val COLUMN_PROFILE_TAX_RATE = "tax_shield_rate"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createBillsTable = """
            CREATE TABLE $TABLE_BILLS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_BILL_DATE TEXT NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL DEFAULT 'EXPENSE',
                $COLUMN_STATUS TEXT NOT NULL DEFAULT 'PAID',
                $COLUMN_NOTES TEXT,
                $COLUMN_IMAGE_URI TEXT,
                $COLUMN_IS_TAX_DEDUCTIBLE INTEGER NOT NULL DEFAULT 1,
                $COLUMN_TAX_SHIELD_AMOUNT REAL NOT NULL DEFAULT 0.0,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createBillsTable)

        val createProfileTable = """
            CREATE TABLE $TABLE_PROFILE (
                id INTEGER PRIMARY KEY,
                $COLUMN_PROFILE_NAME TEXT,
                $COLUMN_PROFILE_ROLE TEXT,
                $COLUMN_PROFILE_BIZ_NAME TEXT,
                $COLUMN_PROFILE_TAX_ID TEXT,
                $COLUMN_PROFILE_BRANCH TEXT,
                $COLUMN_PROFILE_EMAIL TEXT,
                $COLUMN_PROFILE_PHONE TEXT,
                $COLUMN_PROFILE_ADDRESS TEXT,
                $COLUMN_PROFILE_CURRENCY TEXT,
                $COLUMN_PROFILE_SYMBOL TEXT,
                $COLUMN_PROFILE_TAX_RATE REAL
            )
        """.trimIndent()
        db.execSQL(createProfileTable)

        // Seed default initial profile
        val initialProfile = UserProfile()
        val profileValues = ContentValues().apply {
            put("id", 1)
            put(COLUMN_PROFILE_NAME, initialProfile.name)
            put(COLUMN_PROFILE_ROLE, initialProfile.role)
            put(COLUMN_PROFILE_BIZ_NAME, initialProfile.businessName)
            put(COLUMN_PROFILE_TAX_ID, initialProfile.taxId)
            put(COLUMN_PROFILE_BRANCH, initialProfile.branch)
            put(COLUMN_PROFILE_EMAIL, initialProfile.email)
            put(COLUMN_PROFILE_PHONE, initialProfile.phone)
            put(COLUMN_PROFILE_ADDRESS, initialProfile.address)
            put(COLUMN_PROFILE_CURRENCY, initialProfile.currency)
            put(COLUMN_PROFILE_SYMBOL, initialProfile.currencySymbol)
            put(COLUMN_PROFILE_TAX_RATE, initialProfile.taxShieldRate)
        }
        db.insert(TABLE_PROFILE, null, profileValues)
        // Clean start: bills table has zero rows.
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BILLS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        onCreate(db)
    }

    fun insertBill(bill: Bill): Long {
        val db = writableDatabase
        val calculatedTaxShield = if (bill.isTaxDeductible) {
            if (bill.taxShieldAmount > 0) bill.taxShieldAmount else bill.amount
        } else 0.0

        val values = ContentValues().apply {
            put(COLUMN_TITLE, bill.title)
            put(COLUMN_AMOUNT, bill.amount)
            put(COLUMN_CATEGORY, bill.category)
            put(COLUMN_BILL_DATE, bill.billDate)
            put(COLUMN_TYPE, bill.type)
            put(COLUMN_STATUS, bill.status)
            put(COLUMN_NOTES, bill.notes)
            put(COLUMN_IMAGE_URI, bill.imageUri)
            put(COLUMN_IS_TAX_DEDUCTIBLE, if (bill.isTaxDeductible) 1 else 0)
            put(COLUMN_TAX_SHIELD_AMOUNT, calculatedTaxShield)
            put(COLUMN_CREATED_AT, bill.createdAt)
        }
        return db.insert(TABLE_BILLS, null, values)
    }

    fun updateBill(bill: Bill): Int {
        val db = writableDatabase
        val calculatedTaxShield = if (bill.isTaxDeductible) {
            if (bill.taxShieldAmount > 0) bill.taxShieldAmount else bill.amount
        } else 0.0

        val values = ContentValues().apply {
            put(COLUMN_TITLE, bill.title)
            put(COLUMN_AMOUNT, bill.amount)
            put(COLUMN_CATEGORY, bill.category)
            put(COLUMN_BILL_DATE, bill.billDate)
            put(COLUMN_TYPE, bill.type)
            put(COLUMN_STATUS, bill.status)
            put(COLUMN_NOTES, bill.notes)
            put(COLUMN_IMAGE_URI, bill.imageUri)
            put(COLUMN_IS_TAX_DEDUCTIBLE, if (bill.isTaxDeductible) 1 else 0)
            put(COLUMN_TAX_SHIELD_AMOUNT, calculatedTaxShield)
        }
        return db.update(TABLE_BILLS, values, "$COLUMN_ID = ?", arrayOf(bill.id.toString()))
    }

    fun deleteBill(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_BILLS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun getAllBills(): List<Bill> {
        val bills = mutableListOf<Bill>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BILLS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_BILL_DATE DESC, $COLUMN_ID DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                bills.add(cursorToBill(it))
            }
        }
        return bills
    }

    fun getBillsForMonth(yearMonth: String): List<Bill> {
        val bills = mutableListOf<Bill>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BILLS,
            null,
            "$COLUMN_BILL_DATE LIKE ?",
            arrayOf("$yearMonth%"),
            null,
            null,
            "$COLUMN_BILL_DATE DESC, $COLUMN_ID DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                bills.add(cursorToBill(it))
            }
        }
        return bills
    }

    fun getDistinctMonths(): List<String> {
        val months = mutableListOf<String>()
        val db = readableDatabase
        val query = """
            SELECT DISTINCT substr($COLUMN_BILL_DATE, 1, 7) AS ym
            FROM $TABLE_BILLS
            ORDER BY ym DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val ym = it.getString(0)
                if (!ym.isNullOrBlank()) {
                    months.add(ym)
                }
            }
        }
        return months
    }

    fun getLedgerSummary(): LedgerSummary {
        val db = readableDatabase
        var totalBalance = 0.0
        var totalPaid = 0.0
        var totalPending = 0.0
        var totalBills = 0
        var totalTaxShield = 0.0

        val cursor = db.rawQuery(
            "SELECT $COLUMN_AMOUNT, $COLUMN_TYPE, $COLUMN_STATUS, $COLUMN_TAX_SHIELD_AMOUNT FROM $TABLE_BILLS",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val amount = it.getDouble(0)
                val type = it.getString(1)
                val status = it.getString(2)
                val taxShield = it.getDouble(3)

                val signedAmount = if (type == "INCOME") -amount else amount
                totalBalance += signedAmount
                totalBills++
                totalTaxShield += taxShield

                if (status == "PAID") {
                    totalPaid += amount
                } else {
                    totalPending += amount
                }
            }
        }

        val percentage = if (totalBalance > 0) ((totalTaxShield / totalBalance) * 100).toFloat() else 100f

        return LedgerSummary(
            totalBalance = totalBalance,
            totalPaid = totalPaid,
            totalPending = totalPending,
            totalBillsCount = totalBills,
            totalTaxShield = totalTaxShield,
            taxShieldPercentage = percentage
        )
    }

    fun getMonthlyReport(yearMonth: String): MonthlyReport {
        val bills = getBillsForMonth(yearMonth)
        var totalAmount = 0.0
        var paidAmount = 0.0
        var pendingAmount = 0.0
        var totalTaxShield = 0.0
        val categoryMap = mutableMapOf<String, Pair<Double, Int>>()

        for (bill in bills) {
            val amt = bill.amount
            totalAmount += amt
            totalTaxShield += bill.taxShieldAmount
            if (bill.status == "PAID") {
                paidAmount += amt
            } else {
                pendingAmount += amt
            }

            val existing = categoryMap.getOrDefault(bill.category, Pair(0.0, 0))
            categoryMap[bill.category] = Pair(existing.first + amt, existing.second + 1)
        }

        val categoryBreakdown = categoryMap.map { (cat, data) ->
            val catTotal = data.first
            val count = data.second
            val pct = if (totalAmount > 0) ((catTotal / totalAmount) * 100).toFloat() else 0f
            CategorySpend(
                category = cat,
                amount = catTotal,
                percentage = pct,
                count = count
            )
        }.sortedByDescending { it.amount }

        val displayTitle = try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.US)
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.US)
            val date = parser.parse(yearMonth)
            if (date != null) formatter.format(date) else yearMonth
        } catch (e: Exception) {
            yearMonth
        }

        return MonthlyReport(
            yearMonth = yearMonth,
            displayTitle = displayTitle,
            totalAmount = totalAmount,
            totalCount = bills.size,
            paidAmount = paidAmount,
            pendingAmount = pendingAmount,
            totalTaxShield = totalTaxShield,
            categoryBreakdown = categoryBreakdown,
            bills = bills
        )
    }

    fun getUserProfile(): UserProfile {
        val db = readableDatabase
        val cursor = db.query(TABLE_PROFILE, null, "id = 1", null, null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return UserProfile(
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_NAME)) ?: "Restaurant Owner",
                    role = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_ROLE)) ?: "Owner & Manager",
                    businessName = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_BIZ_NAME)) ?: "My Restaurant",
                    taxId = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_TAX_ID)) ?: "0105567089123",
                    branch = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_BRANCH)) ?: "Head Office (00000)",
                    email = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_EMAIL)) ?: "owner@restaurant.co",
                    phone = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_PHONE)) ?: "+66 82 456 7890",
                    address = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_ADDRESS)) ?: "Bangkok, Thailand",
                    currency = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_CURRENCY)) ?: "THB",
                    currencySymbol = it.getString(it.getColumnIndexOrThrow(COLUMN_PROFILE_SYMBOL)) ?: "฿",
                    taxShieldRate = it.getDouble(it.getColumnIndexOrThrow(COLUMN_PROFILE_TAX_RATE)).takeIf { r -> r > 0 } ?: 0.07
                )
            }
        }
        return UserProfile()
    }

    fun updateUserProfile(profile: UserProfile): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PROFILE_NAME, profile.name)
            put(COLUMN_PROFILE_ROLE, profile.role)
            put(COLUMN_PROFILE_BIZ_NAME, profile.businessName)
            put(COLUMN_PROFILE_TAX_ID, profile.taxId)
            put(COLUMN_PROFILE_BRANCH, profile.branch)
            put(COLUMN_PROFILE_EMAIL, profile.email)
            put(COLUMN_PROFILE_PHONE, profile.phone)
            put(COLUMN_PROFILE_ADDRESS, profile.address)
            put(COLUMN_PROFILE_CURRENCY, profile.currency)
            put(COLUMN_PROFILE_SYMBOL, profile.currencySymbol)
            put(COLUMN_PROFILE_TAX_RATE, profile.taxShieldRate)
        }
        return db.update(TABLE_PROFILE, values, "id = 1", null)
    }

    fun clearAllData() {
        val db = writableDatabase
        db.delete(TABLE_BILLS, null, null)
    }

    private fun cursorToBill(cursor: Cursor): Bill {
        val isTaxDeductibleInt = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_TAX_DEDUCTIBLE))
        return Bill(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
            billDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BILL_DATE)),
            type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
            status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
            notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)) ?: "",
            imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
            isTaxDeductible = isTaxDeductibleInt == 1,
            taxShieldAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TAX_SHIELD_AMOUNT)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
        )
    }
}
