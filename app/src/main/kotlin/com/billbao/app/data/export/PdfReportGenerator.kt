package com.billbao.app.data.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.billbao.app.R
import com.billbao.app.data.model.Bill
import com.billbao.app.data.model.MonthlyReport
import com.billbao.app.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 standard height in points (72 dpi)
    private const val MARGIN = 36f

    fun generateAndDownloadReport(
        context: Context,
        currentReport: MonthlyReport,
        previousReport: MonthlyReport?,
        profile: UserProfile
    ) {
        try {
            val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val cleanMonth = currentReport.yearMonth.replace("-", "_")
            val filename = "BillBao_Report_${cleanMonth}_$dateStamp.pdf"

            val pdfDocument = PdfDocument()

            // Calculate MoM metrics
            val thisMonthTotal = currentReport.totalAmount
            val lastMonthTotal = previousReport?.totalAmount ?: 0.0
            val delta = thisMonthTotal - lastMonthTotal
            val percentChange = when {
                lastMonthTotal > 0.0 -> (delta / lastMonthTotal) * 100.0
                thisMonthTotal > 0.0 -> 100.0
                else -> 0.0
            }

            // Paints
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
            }

            // Decode App Logo for PDF Header
            val rawLogo = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
            val logoBitmap = rawLogo?.let {
                Bitmap.createScaledBitmap(it, 44, 44, true)
            }

            // Split bills across pages if needed
            val bills = currentReport.bills
            val itemsPerPageFirst = 7
            val itemsPerPageSubsequent = 16

            val firstPageBills = bills.take(itemsPerPageFirst)
            val remainingBills = if (bills.size > itemsPerPageFirst) bills.drop(itemsPerPageFirst) else emptyList()
            val totalPages = 1 + if (remainingBills.isNotEmpty()) {
                (remainingBills.size + itemsPerPageSubsequent - 1) / itemsPerPageSubsequent
            } else 0

            // ================= PAGE 1 =================
            val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page1 = pdfDocument.startPage(pageInfo1)
            val canvas1 = page1.canvas

            drawPage1(
                canvas = canvas1,
                logoBitmap = logoBitmap,
                currentReport = currentReport,
                previousReport = previousReport,
                profile = profile,
                thisMonthTotal = thisMonthTotal,
                lastMonthTotal = lastMonthTotal,
                delta = delta,
                percentChange = percentChange,
                pageBills = firstPageBills,
                pageNumber = 1,
                totalPages = totalPages,
                textPaint = textPaint,
                fillPaint = fillPaint,
                strokePaint = strokePaint
            )

            pdfDocument.finishPage(page1)

            // ================= SUBSEQUENT PAGES (IF ANY) =================
            if (remainingBills.isNotEmpty()) {
                val chunks = remainingBills.chunked(itemsPerPageSubsequent)
                chunks.forEachIndexed { index, chunk ->
                    val pageNum = index + 2
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                    val page = pdfDocument.startPage(pageInfo)
                    drawContinuationPage(
                        canvas = page.canvas,
                        currentReport = currentReport,
                        profile = profile,
                        pageBills = chunk,
                        startRowIndex = itemsPerPageFirst + (index * itemsPerPageSubsequent) + 1,
                        pageNumber = pageNum,
                        totalPages = totalPages,
                        textPaint = textPaint,
                        fillPaint = fillPaint,
                        strokePaint = strokePaint
                    )
                    pdfDocument.finishPage(page)
                }
            }

            // Write to App Cache for FileProvider
            val cacheFile = File(context.cacheDir, filename)
            FileOutputStream(cacheFile).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()

            // Save to Public Downloads Folder
            savePdfToPublicDownloads(context, filename, cacheFile.readBytes())

            // Launch View / Share Intent
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "Open Monthly PDF Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Toast.makeText(
                context,
                "✓ Downloaded: Downloads/$filename",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF Report error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawPage1(
        canvas: Canvas,
        logoBitmap: Bitmap?,
        currentReport: MonthlyReport,
        previousReport: MonthlyReport?,
        profile: UserProfile,
        thisMonthTotal: Double,
        lastMonthTotal: Double,
        delta: Double,
        percentChange: Double,
        pageBills: List<Bill>,
        pageNumber: Int,
        totalPages: Int,
        textPaint: Paint,
        fillPaint: Paint,
        strokePaint: Paint
    ) {
        var y = MARGIN

        // --- 1. HEADER WITH LOGO ---
        if (logoBitmap != null) {
            canvas.drawBitmap(logoBitmap, MARGIN, y, null)
        }

        // Header Title
        textPaint.color = Color.rgb(11, 23, 27) // Dark obsidian
        textPaint.textSize = 17f
        textPaint.isFakeBoldText = true
        canvas.drawText("BillBao (บิลเบา)", MARGIN + 52f, y + 18f, textPaint)

        textPaint.textSize = 8.5f
        textPaint.color = Color.rgb(71, 85, 105) // Slate 600
        textPaint.isFakeBoldText = false
        canvas.drawText("RESTAURANT EXPENSE & TAX LEDGER • OFFICIAL AUDIT REPORT", MARGIN + 52f, y + 32f, textPaint)

        // Pill Badge on top right
        fillPaint.color = Color.rgb(236, 253, 245) // Mint container
        val badgeRect = RectF(PAGE_WIDTH - MARGIN - 140f, y + 4f, PAGE_WIDTH - MARGIN, y + 26f)
        canvas.drawRoundRect(badgeRect, 6f, 6f, fillPaint)

        textPaint.color = Color.rgb(5, 150, 105) // Emerald
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = true
        canvas.drawText("SEC 65 TER VERIFIED", PAGE_WIDTH - MARGIN - 128f, y + 19f, textPaint)

        y += 54f

        // Teal Divider
        strokePaint.color = Color.rgb(0, 229, 153) // Neon Teal
        strokePaint.strokeWidth = 2f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, strokePaint)

        y += 14f

        // --- 2. BUSINESS & REPORT INFO CARD ---
        fillPaint.color = Color.rgb(248, 250, 252) // Light Slate
        val infoRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 58f)
        canvas.drawRoundRect(infoRect, 8f, 8f, fillPaint)
        strokePaint.color = Color.rgb(226, 232, 240)
        strokePaint.strokeWidth = 1f
        canvas.drawRoundRect(infoRect, 8f, 8f, strokePaint)

        // Column 1: Restaurant Info
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 9.5f
        textPaint.isFakeBoldText = true
        canvas.drawText("Restaurant: ${profile.businessName.ifEmpty { "Restaurant Business" }}", MARGIN + 12f, y + 16f, textPaint)

        textPaint.textSize = 8f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(71, 85, 105)
        val taxIdStr = if (profile.taxId.isNotBlank()) profile.taxId else "Not registered"
        canvas.drawText("Tax ID (TIN): $taxIdStr  |  Branch: ${profile.branch.ifEmpty { "00000" }}", MARGIN + 12f, y + 30f, textPaint)
        canvas.drawText("Owner: ${profile.name}  |  Address: ${profile.address.ifEmpty { "Bangkok, Thailand" }}", MARGIN + 12f, y + 44f, textPaint)

        // Column 2: Audit Period Context
        val col2X = PAGE_WIDTH - MARGIN - 190f
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 9.5f
        textPaint.isFakeBoldText = true
        canvas.drawText("Report Month: ${currentReport.displayTitle}", col2X, y + 16f, textPaint)

        textPaint.textSize = 8f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(71, 85, 105)
        val prevTitle = previousReport?.displayTitle ?: "None (Fresh Start)"
        canvas.drawText("Prior Period: $prevTitle", col2X, y + 30f, textPaint)
        canvas.drawText("Audit Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}", col2X, y + 44f, textPaint)

        y += 70f

        // --- 3. MONTH-OVER-MONTH (MoM) COMPARISON CARD ---
        val momCardHeight = 84f
        fillPaint.color = Color.rgb(11, 23, 27) // Obsidian card
        val momRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + momCardHeight)
        canvas.drawRoundRect(momRect, 8f, 8f, fillPaint)

        // Metric 1: This Month Total
        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 7.5f
        textPaint.isFakeBoldText = true
        canvas.drawText("THIS MONTH (${currentReport.yearMonth})", MARGIN + 14f, y + 18f, textPaint)

        textPaint.color = Color.rgb(0, 229, 153) // Neon Green
        textPaint.textSize = 15f
        textPaint.isFakeBoldText = true
        canvas.drawText("฿${String.format(Locale.US, "%,.2f", thisMonthTotal)}", MARGIN + 14f, y + 38f, textPaint)

        textPaint.color = Color.rgb(203, 213, 225)
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = false
        canvas.drawText("${currentReport.totalCount} Bills Recorded", MARGIN + 14f, y + 52f, textPaint)

        // Metric 2: Last Month Total
        val metric2X = MARGIN + 180f
        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 7.5f
        textPaint.isFakeBoldText = true
        canvas.drawText("LAST MONTH (${previousReport?.yearMonth ?: "PREV"})", metric2X, y + 18f, textPaint)

        textPaint.color = Color.rgb(241, 245, 249)
        textPaint.textSize = 15f
        textPaint.isFakeBoldText = true
        canvas.drawText("฿${String.format(Locale.US, "%,.2f", lastMonthTotal)}", metric2X, y + 38f, textPaint)

        textPaint.color = Color.rgb(203, 213, 225)
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = false
        canvas.drawText("${previousReport?.totalCount ?: 0} Bills Recorded", metric2X, y + 52f, textPaint)

        // Metric 3: Expense Increase / Decrease Pill
        val metric3X = PAGE_WIDTH - MARGIN - 170f
        val isIncrease = delta > 0
        val isDecrease = delta < 0
        val isZero = delta == 0.0

        val varianceBadgeColor = when {
            isIncrease -> Color.rgb(239, 68, 68) // Red (Increase)
            isDecrease -> Color.rgb(16, 185, 129) // Green (Savings)
            else -> Color.rgb(100, 116, 139) // Slate
        }
        val varianceBgColor = when {
            isIncrease -> Color.rgb(69, 10, 10)
            isDecrease -> Color.rgb(6, 78, 59)
            else -> Color.rgb(30, 41, 59)
        }

        fillPaint.color = varianceBgColor
        val varianceRect = RectF(metric3X - 6f, y + 12f, PAGE_WIDTH - MARGIN - 10f, y + 62f)
        canvas.drawRoundRect(varianceRect, 6f, 6f, fillPaint)

        strokePaint.color = varianceBadgeColor
        strokePaint.strokeWidth = 1f
        canvas.drawRoundRect(varianceRect, 6f, 6f, strokePaint)

        textPaint.color = varianceBadgeColor
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = true
        val statusTitle = when {
            isIncrease -> "▲ EXPENSES INCREASED"
            isDecrease -> "▼ EXPENSES DECREASED"
            else -> "• NO CHANGE (฿0.00)"
        }
        canvas.drawText(statusTitle, metric3X, y + 27f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 11f
        textPaint.isFakeBoldText = true
        val deltaSign = if (delta > 0) "+" else ""
        canvas.drawText("$deltaSign฿${String.format(Locale.US, "%,.2f", delta)}", metric3X, y + 43f, textPaint)

        textPaint.textSize = 8f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(226, 232, 240)
        canvas.drawText("(${String.format(Locale.US, "%+.1f%%", percentChange)} vs last month)", metric3X, y + 56f, textPaint)

        // Card Sub-footer (Tax Shield + VAT Reclaim)
        strokePaint.color = Color.rgb(30, 41, 59)
        strokePaint.strokeWidth = 1f
        canvas.drawLine(MARGIN + 10f, y + 67f, PAGE_WIDTH - MARGIN - 10f, y + 67f, strokePaint)

        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 7.5f
        textPaint.isFakeBoldText = false
        val vat7 = currentReport.totalTaxShield * 0.07
        canvas.drawText(
            "🛡️ Claimable Tax Shield: ฿${String.format(Locale.US, "%,.2f", currentReport.totalTaxShield)}  •  7% VAT Reclaim (PP.30): ฿${String.format(Locale.US, "%,.2f", vat7)}",
            MARGIN + 14f,
            y + 78f,
            textPaint
        )

        y += momCardHeight + 16f

        // --- 4. BASIC GRAPHS SECTION ---
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = true
        canvas.drawText("MONTHLY EXPENSE COMPARISON & BREAKDOWN GRAPHS", MARGIN, y, textPaint)

        y += 12f

        // GRAPH 1: Month-over-Month Comparison Bar Chart
        val graphCardHeight = 84f
        fillPaint.color = Color.rgb(248, 250, 252)
        val graphCardRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + graphCardHeight)
        canvas.drawRoundRect(graphCardRect, 8f, 8f, fillPaint)
        strokePaint.color = Color.rgb(226, 232, 240)
        strokePaint.strokeWidth = 1f
        canvas.drawRoundRect(graphCardRect, 8f, 8f, strokePaint)

        // Draw MoM Horizontal Bars
        val barMaxAmount = maxOf(thisMonthTotal, lastMonthTotal, 1.0)
        val barAreaLeft = MARGIN + 120f
        val barAreaWidth = 240f

        // Last Month Bar
        textPaint.color = Color.rgb(71, 85, 105)
        textPaint.textSize = 7.5f
        textPaint.isFakeBoldText = true
        canvas.drawText("Last Month:", MARGIN + 14f, y + 24f, textPaint)

        fillPaint.color = Color.rgb(203, 213, 225) // Slate 300
        val lastBarWidth = ((lastMonthTotal / barMaxAmount) * barAreaWidth).toFloat().coerceIn(4f, barAreaWidth)
        val lastBarRect = RectF(barAreaLeft, y + 14f, barAreaLeft + lastBarWidth, y + 26f)
        canvas.drawRoundRect(lastBarRect, 3f, 3f, fillPaint)

        textPaint.color = Color.rgb(30, 41, 59)
        textPaint.textSize = 7.5f
        canvas.drawText("฿${String.format(Locale.US, "%,.2f", lastMonthTotal)}", barAreaLeft + lastBarWidth + 6f, y + 23f, textPaint)

        // This Month Bar
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 7.5f
        textPaint.isFakeBoldText = true
        canvas.drawText("This Month:", MARGIN + 14f, y + 42f, textPaint)

        fillPaint.color = Color.rgb(0, 229, 153) // Neon Green
        val thisBarWidth = ((thisMonthTotal / barMaxAmount) * barAreaWidth).toFloat().coerceIn(4f, barAreaWidth)
        val thisBarRect = RectF(barAreaLeft, y + 32f, barAreaLeft + thisBarWidth, y + 44f)
        canvas.drawRoundRect(thisBarRect, 3f, 3f, fillPaint)

        textPaint.color = Color.rgb(5, 150, 105)
        textPaint.textSize = 7.5f
        textPaint.isFakeBoldText = true
        canvas.drawText("฿${String.format(Locale.US, "%,.2f", thisMonthTotal)}", barAreaLeft + thisBarWidth + 6f, y + 41f, textPaint)

        // Mini Category Breakdown Bars (Food vs Gas vs Equip vs Other)
        val categories = currentReport.categoryBreakdown.take(3)
        var catY = y + 58f
        textPaint.color = Color.rgb(71, 85, 105)
        textPaint.textSize = 7f
        textPaint.isFakeBoldText = true
        canvas.drawText("Top Categories:", MARGIN + 14f, catY + 8f, textPaint)

        val catColors = listOf(
            Color.rgb(16, 185, 129), // Emerald
            Color.rgb(59, 130, 246), // Blue
            Color.rgb(245, 158, 11)  // Amber
        )

        var catBarX = MARGIN + 90f
        if (categories.isNotEmpty()) {
            categories.forEachIndexed { i, cat ->
                val color = catColors.getOrElse(i) { Color.rgb(139, 92, 246) }
                fillPaint.color = color
                val dotRect = RectF(catBarX, catY + 2f, catBarX + 7f, catY + 9f)
                canvas.drawRoundRect(dotRect, 2f, 2f, fillPaint)

                textPaint.color = Color.rgb(51, 65, 85)
                textPaint.textSize = 6.8f
                textPaint.isFakeBoldText = false
                val label = "${cat.category.take(16)}: ฿${String.format(Locale.US, "%,.0f", cat.amount)} (${String.format(Locale.US, "%.0f%%", cat.percentage)})"
                canvas.drawText(label, catBarX + 11f, catY + 8f, textPaint)
                catBarX += 135f
            }
        } else {
            textPaint.color = Color.rgb(148, 163, 184)
            textPaint.textSize = 7f
            canvas.drawText("No category data recorded yet.", MARGIN + 90f, catY + 8f, textPaint)
        }

        y += graphCardHeight + 16f

        // --- 5. ITEMIZED BILLS TABLE ---
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = true
        canvas.drawText("ITEMIZED EXPENSE & TAX AUDIT LEDGER (${currentReport.bills.size} Records)", MARGIN, y, textPaint)

        y += 10f

        // Table Header
        val rowHeight = 18f
        fillPaint.color = Color.rgb(30, 41, 59) // Slate 800
        val tableHeaderRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight)
        canvas.drawRoundRect(tableHeaderRect, 4f, 4f, fillPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 7f
        textPaint.isFakeBoldText = true
        canvas.drawText("DATE", MARGIN + 8f, y + 12f, textPaint)
        canvas.drawText("MERCHANT / VENDOR", MARGIN + 74f, y + 12f, textPaint)
        canvas.drawText("CATEGORY", MARGIN + 230f, y + 12f, textPaint)
        canvas.drawText("TAX ID / TIN", MARGIN + 355f, y + 12f, textPaint)
        canvas.drawText("AMOUNT (THB)", PAGE_WIDTH - MARGIN - 90f, y + 12f, textPaint)
        canvas.drawText("SHIELD", PAGE_WIDTH - MARGIN - 30f, y + 12f, textPaint)

        y += rowHeight + 3f

        // Table Rows
        if (pageBills.isEmpty()) {
            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textSize = 8.5f
            textPaint.isFakeBoldText = false
            canvas.drawText("🌱 Clean Fresh Slate: No expenses recorded for this month. Ledger total is ฿0.00 THB.", MARGIN + 12f, y + 20f, textPaint)
            y += 40f
        } else {
            pageBills.forEachIndexed { i, bill ->
                val bgCol = if (i % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
                fillPaint.color = bgCol
                val rRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight)
                canvas.drawRect(rRect, fillPaint)

                // Date
                textPaint.color = Color.rgb(71, 85, 105)
                textPaint.textSize = 6.8f
                textPaint.isFakeBoldText = false
                canvas.drawText(bill.billDate, MARGIN + 8f, y + 12f, textPaint)

                // Merchant
                textPaint.color = Color.rgb(15, 23, 42)
                textPaint.textSize = 7f
                textPaint.isFakeBoldText = true
                val cleanTitle = if (bill.title.length > 28) bill.title.take(26) + ".." else bill.title
                canvas.drawText(cleanTitle, MARGIN + 74f, y + 12f, textPaint)

                // Category
                textPaint.color = Color.rgb(71, 85, 105)
                textPaint.textSize = 6.8f
                textPaint.isFakeBoldText = false
                val cleanCat = if (bill.category.length > 20) bill.category.take(18) + ".." else bill.category
                canvas.drawText(cleanCat, MARGIN + 230f, y + 12f, textPaint)

                // Tax ID extracted
                val extractedTaxId = extractTaxIdFromNotes(bill.notes)
                textPaint.color = Color.rgb(100, 116, 139)
                textPaint.textSize = 6.5f
                canvas.drawText(extractedTaxId, MARGIN + 355f, y + 12f, textPaint)

                // Amount
                textPaint.color = Color.rgb(15, 23, 42)
                textPaint.textSize = 7.2f
                textPaint.isFakeBoldText = true
                canvas.drawText(String.format(Locale.US, "฿%,.2f", bill.amount), PAGE_WIDTH - MARGIN - 90f, y + 12f, textPaint)

                // Tax Shield Status
                textPaint.color = if (bill.isTaxDeductible) Color.rgb(5, 150, 105) else Color.rgb(148, 163, 184)
                textPaint.textSize = 6.8f
                canvas.drawText(if (bill.isTaxDeductible) "✓ Claim" else "—", PAGE_WIDTH - MARGIN - 28f, y + 12f, textPaint)

                // Bottom line
                strokePaint.color = Color.rgb(241, 245, 249)
                strokePaint.strokeWidth = 0.5f
                canvas.drawLine(MARGIN, y + rowHeight, PAGE_WIDTH - MARGIN, y + rowHeight, strokePaint)

                y += rowHeight
            }
        }

        // --- FOOTER ---
        drawFooter(canvas, pageNumber, totalPages, textPaint, strokePaint)
    }

    private fun drawContinuationPage(
        canvas: Canvas,
        currentReport: MonthlyReport,
        profile: UserProfile,
        pageBills: List<Bill>,
        startRowIndex: Int,
        pageNumber: Int,
        totalPages: Int,
        textPaint: Paint,
        fillPaint: Paint,
        strokePaint: Paint
    ) {
        var y = MARGIN

        // Continuation Header
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        canvas.drawText("BillBao (บิลเบา) — ${currentReport.displayTitle} (Continued)", MARGIN, y + 14f, textPaint)

        textPaint.color = Color.rgb(100, 116, 139)
        textPaint.textSize = 8f
        textPaint.isFakeBoldText = false
        canvas.drawText("Restaurant: ${profile.businessName}  |  Tax ID: ${profile.taxId}", MARGIN, y + 28f, textPaint)

        y += 40f

        // Table Header
        val rowHeight = 18f
        fillPaint.color = Color.rgb(30, 41, 59)
        val tableHeaderRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight)
        canvas.drawRoundRect(tableHeaderRect, 4f, 4f, fillPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 7f
        textPaint.isFakeBoldText = true
        canvas.drawText("DATE", MARGIN + 8f, y + 12f, textPaint)
        canvas.drawText("MERCHANT / VENDOR", MARGIN + 74f, y + 12f, textPaint)
        canvas.drawText("CATEGORY", MARGIN + 230f, y + 12f, textPaint)
        canvas.drawText("TAX ID / TIN", MARGIN + 355f, y + 12f, textPaint)
        canvas.drawText("AMOUNT (THB)", PAGE_WIDTH - MARGIN - 90f, y + 12f, textPaint)
        canvas.drawText("SHIELD", PAGE_WIDTH - MARGIN - 30f, y + 12f, textPaint)

        y += rowHeight + 3f

        // Rows
        pageBills.forEachIndexed { i, bill ->
            val bgCol = if (i % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
            fillPaint.color = bgCol
            val rRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight)
            canvas.drawRect(rRect, fillPaint)

            // Date
            textPaint.color = Color.rgb(71, 85, 105)
            textPaint.textSize = 6.8f
            textPaint.isFakeBoldText = false
            canvas.drawText(bill.billDate, MARGIN + 8f, y + 12f, textPaint)

            // Merchant
            textPaint.color = Color.rgb(15, 23, 42)
            textPaint.textSize = 7f
            textPaint.isFakeBoldText = true
            val cleanTitle = if (bill.title.length > 28) bill.title.take(26) + ".." else bill.title
            canvas.drawText(cleanTitle, MARGIN + 74f, y + 12f, textPaint)

            // Category
            textPaint.color = Color.rgb(71, 85, 105)
            textPaint.textSize = 6.8f
            textPaint.isFakeBoldText = false
            val cleanCat = if (bill.category.length > 20) bill.category.take(18) + ".." else bill.category
            canvas.drawText(cleanCat, MARGIN + 230f, y + 12f, textPaint)

            // Tax ID
            val extractedTaxId = extractTaxIdFromNotes(bill.notes)
            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textSize = 6.5f
            canvas.drawText(extractedTaxId, MARGIN + 355f, y + 12f, textPaint)

            // Amount
            textPaint.color = Color.rgb(15, 23, 42)
            textPaint.textSize = 7.2f
            textPaint.isFakeBoldText = true
            canvas.drawText(String.format(Locale.US, "฿%,.2f", bill.amount), PAGE_WIDTH - MARGIN - 90f, y + 12f, textPaint)

            // Tax Shield
            textPaint.color = if (bill.isTaxDeductible) Color.rgb(5, 150, 105) else Color.rgb(148, 163, 184)
            textPaint.textSize = 6.8f
            canvas.drawText(if (bill.isTaxDeductible) "✓ Claim" else "—", PAGE_WIDTH - MARGIN - 28f, y + 12f, textPaint)

            strokePaint.color = Color.rgb(241, 245, 249)
            strokePaint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN, y + rowHeight, PAGE_WIDTH - MARGIN, y + rowHeight, strokePaint)

            y += rowHeight
        }

        drawFooter(canvas, pageNumber, totalPages, textPaint, strokePaint)
    }

    private fun drawFooter(
        canvas: Canvas,
        pageNumber: Int,
        totalPages: Int,
        textPaint: Paint,
        strokePaint: Paint
    ) {
        val footerY = PAGE_HEIGHT - MARGIN + 10f
        strokePaint.color = Color.rgb(226, 232, 240)
        strokePaint.strokeWidth = 0.75f
        canvas.drawLine(MARGIN, footerY - 14f, PAGE_WIDTH - MARGIN, footerY - 14f, strokePaint)

        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 7f
        textPaint.isFakeBoldText = false
        canvas.drawText(
            "Confidential Restaurant Expense Ledger • Generated by BillBao for Thai Revenue Code Sec 65 Ter",
            MARGIN,
            footerY,
            textPaint
        )

        val pageStr = "Page $pageNumber of $totalPages"
        val pageTextWidth = textPaint.measureText(pageStr)
        canvas.drawText(pageStr, PAGE_WIDTH - MARGIN - pageTextWidth, footerY, textPaint)
    }

    private fun extractTaxIdFromNotes(notes: String): String {
        if (notes.isBlank()) return "—"
        val regex = Regex("""(?i)Tax\s*ID:\s*([0-9]{13})""")
        val match = regex.find(notes)
        return match?.groupValues?.get(1) ?: "—"
    }

    private fun savePdfToPublicDownloads(context: Context, filename: String, bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { os ->
                    os.write(bytes)
                }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetFile = File(downloadsDir, filename)
            FileOutputStream(targetFile).use { fos ->
                fos.write(bytes)
            }
        }
    }
}
