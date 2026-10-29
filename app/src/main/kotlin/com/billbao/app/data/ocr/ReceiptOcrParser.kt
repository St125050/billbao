package com.billbao.app.data.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class ParsedReceipt(
    val vendor: String,
    val date: String, // YYYY-MM-DD
    val amount: Double,
    val taxId: String,
    val category: String,
    val candidates: List<Double>,
    val docType: String,
    val reason: String,
    val rawText: String
)

object ReceiptOcrParser {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Runs Google ML Kit Text Recognition on an in-memory Bitmap.
     */
    suspend fun recognizeText(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(bitmap, 0)
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { error ->
                    continuation.resume("")
                }
        }
    }

    /**
     * Runs Google ML Kit Text Recognition on a Uri.
     */
    suspend fun recognizeTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.Default) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            suspendCancellableCoroutine { continuation ->
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.text)
                    }
                    .addOnFailureListener {
                        continuation.resume("")
                    }
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Parses raw OCR text extracting Seller/Vendor Name, 13-digit Tax ID, Date,
     * Category, and Overall Total (in English and Thai keywords).
     */
    fun parse(rawText: String, defaultName: String = "Receipt"): ParsedReceipt {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        if (rawText.isBlank()) {
            return ParsedReceipt(
                vendor = defaultName,
                date = today,
                amount = 0.0,
                taxId = "",
                category = "Food Supplies (COGS)",
                candidates = emptyList(),
                docType = "Cash Bill (บิลเงินสด)",
                reason = "Please enter total amount manually",
                rawText = ""
            )
        }

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // 1. EXTRACT VENDOR NAME (First meaningful non-numeric line)
        var vendor = ""
        val skipWords = listOf(
            "tax invoice", "receipt", "cash bill", "ใบเสร็จ", "ใบกำกับภาษี",
            "บิลเงินสด", "welcome", "ยินดีต้อนรับ", "tel", "phone", "date", "เวลา", "order"
        )
        for (i in 0 until minOf(lines.size, 5)) {
            val line = lines[i]
            val lower = line.lowercase(Locale.ROOT)
            val isSkip = skipWords.any { lower.contains(it) }
            val isNumberOnly = line.all { it.isDigit() || it.isWhitespace() || it == '-' || it == '/' || it == '.' }
            if (!isSkip && !isNumberOnly && line.length > 2) {
                vendor = line.replace(Regex("[#*:=_]"), "").trim()
                break
            }
        }
        if (vendor.isBlank()) {
            vendor = defaultName
        }

        // 2. EXTRACT 13-DIGIT TAX ID (Thai Revenue Code standard)
        var taxId = ""
        val taxPattern = Pattern.compile("""(?:\b\d{13}\b|(?:Tax\s*ID|TIN|เลขประจำตัวผู้เสียภาษี)[:\s]*([0-9\s-]{13,17}))""", Pattern.CASE_INSENSITIVE)
        val taxMatcher = taxPattern.matcher(rawText)
        if (taxMatcher.find()) {
            val matched = taxMatcher.group(0)?.replace(Regex("[^0-9]"), "") ?: ""
            if (matched.length >= 13) {
                taxId = matched.take(13)
            }
        }

        // 3. EXTRACT DATE
        var billDate = today
        val datePattern = Pattern.compile("""(\d{1,2})[/\.-](\d{1,2})[/\.-](\d{2,4})""")
        val dateMatcher = datePattern.matcher(rawText)
        if (dateMatcher.find()) {
            val day = dateMatcher.group(1)?.padStart(2, '0') ?: "01"
            val month = dateMatcher.group(2)?.padStart(2, '0') ?: "01"
            var year = dateMatcher.group(3) ?: "2026"
            if (year.length == 2) year = "20$year"
            val yearInt = year.toIntOrNull() ?: 2026
            // Convert Buddhist Era (e.g. 2567 -> 2024, 2569 -> 2026)
            val finalYear = if (yearInt > 2500) (yearInt - 543).toString() else year
            billDate = "$finalYear-$month-$day"
        }

        // 4. EXTRACT TOTAL AMOUNT & CANDIDATES (English & Thai keywords)
        val candidates = mutableListOf<Double>()
        var totalAmount = 0.0
        var reason = "Extracted from receipt"

        // Collect all potential numbers
        val numberRegex = Regex("""(\d{1,3}(?:,\d{3})*\.\d{2})|(\b\d{2,6}\b\s*[-—])|(\b\d{2,6}\.\d{1,2}\b)""")
        numberRegex.findAll(rawText).forEach { match ->
            val clean = match.value.replace(Regex("[-—,]"), "").trim()
            val num = clean.toDoubleOrNull()
            if (num != null && num > 0.0 && num < 1_000_000.0 && !candidates.contains(num)) {
                candidates.add(num)
            }
        }

        // Priority 1: Lines containing explicit Total keywords (Thai & English)
        val totalKeywords = listOf(
            "total", "bill total", "grand total", "net total", "amount due", "balance",
            "ยอดรวม", "รวมทั้งสิ้น", "รวมเงิน", "จำนวนเงินทั้งสิ้น", "ยอดสุทธิ", "รวมเป็นเงิน"
        )

        for (line in lines) {
            val lower = line.lowercase(Locale.ROOT)
            if (totalKeywords.any { lower.contains(it) }) {
                val lineNums = Regex("""(\d{1,3}(?:,\d{3})*(?:\.\d{2})?|\d+)""").findAll(line)
                    .mapNotNull { it.value.replace(",", "").toDoubleOrNull() }
                    .filter { it > 0 }
                    .toList()
                if (lineNums.isNotEmpty()) {
                    totalAmount = lineNums.last()
                    reason = "✓ Match on keyword line: \"${line.take(30)}\""
                    break
                }
            }
        }

        // Priority 2: Thai Dash Total Notation (e.g. "160 -" or "720.-")
        if (totalAmount == 0.0) {
            val dashMatch = Regex("""(\d{2,6})\s*[-—]""").find(rawText)
            if (dashMatch != null) {
                val num = dashMatch.groupValues[1].toDoubleOrNull()
                if (num != null && num > 0) {
                    totalAmount = num
                    reason = "✓ Dash total notation recognized: \"${dashMatch.value}\" ➔ $totalAmount THB"
                }
            }
        }

        // Priority 3: Fallback to largest detected candidate number
        if (totalAmount == 0.0 && candidates.isNotEmpty()) {
            totalAmount = candidates.maxOrNull() ?: 0.0
            reason = "✓ Auto-selected highest candidate amount found on bill"
        }

        // 5. CATEGORY CLASSIFICATION
        val lowerText = rawText.lowercase(Locale.ROOT)
        val category = when {
            lowerText.contains("makro") || lowerText.contains("lotus") || lowerText.contains("food") ||
            lowerText.contains("vegetable") || lowerText.contains("meat") || lowerText.contains("ตลาด") ||
            lowerText.contains("ผัก") || lowerText.contains("หมู") || lowerText.contains("ไก่") ||
            lowerText.contains("สด") || lowerText.contains("กุ้ง") -> "Food Supplies (COGS)"

            lowerText.contains("gas") || lowerText.contains("lpg") || lowerText.contains("ptt") ||
            lowerText.contains("ก๊าซ") || lowerText.contains("ถังแก๊ส") || lowerText.contains("ไฟ") -> "Cooking Gas & Utilities"

            lowerText.contains("refrigerator") || lowerText.contains("freezer") || lowerText.contains("fryer") ||
            lowerText.contains("ตู้เย็น") || lowerText.contains("เตา") -> "Kitchen Equipment (Capex)"

            lowerText.contains("box") || lowerText.contains("bag") || lowerText.contains("cup") ||
            lowerText.contains("กล่อง") || lowerText.contains("ถุง") -> "Packaging & Disposables"

            lowerText.contains("repair") || lowerText.contains("service") || lowerText.contains("ซ่อม") ||
            lowerText.contains("ช่าง") -> "Repairs & Upkeep"

            else -> "Food Supplies (COGS)"
        }

        return ParsedReceipt(
            vendor = vendor,
            date = billDate,
            amount = totalAmount,
            taxId = taxId,
            category = category,
            candidates = candidates,
            docType = if (taxId.isNotBlank()) "Full Tax Invoice (ใบกำกับภาษีเต็มรูป)" else "Cash Bill (บิลเงินสด)",
            reason = reason,
            rawText = rawText
        )
    }

    /**
     * 1-Tap Presets from the reference web application
     */
    fun getPreset(id: String): ParsedReceipt {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return when (id) {
            "govindam" -> ParsedReceipt(
                vendor = "Govindam Pure Veg",
                date = today,
                amount = 720.00,
                taxId = "",
                category = "Food Supplies (COGS)",
                candidates = listOf(720.00, 240.00, 480.00),
                docType = "Handwritten Cash Bill (บิลเงินสด)",
                reason = "✓ Handwritten dash notation recognized: 720.-",
                rawText = "Govindam Pure Veg Restaurant\nDate: $today\nPaneer Butter Masala 240.-\nGarlic Naan 480.-\nTotal 720.-"
            )
            "sanguan" -> ParsedReceipt(
                vendor = "Thai Sanguan Wet Market",
                date = today,
                amount = 160.00,
                taxId = "",
                category = "Food Supplies (COGS)",
                candidates = listOf(160.00, 80.00),
                docType = "Cash Bill (บิลเงินสดตลาดสด)",
                reason = "✓ Dash format total (160 -) matched",
                rawText = "ไทยสงวน ตลาดสด\nผักกาดขาว 80\nต้นหอม 80\nรวม 160 -"
            )
            "dasaprakash" -> ParsedReceipt(
                vendor = "Dasaprakash Restaurant",
                date = today,
                amount = 929.50,
                taxId = "0105558123456",
                category = "Food Supplies (COGS)",
                candidates = listOf(929.50, 845.00, 84.50),
                docType = "Full Tax Invoice (ใบกำกับภาษีเต็มรูป)",
                reason = "✓ Subtotal 845.00 + VAT 84.50 ➔ Grand Total 929.50 THB",
                rawText = "Dasaprakash Indian Cuisine\nTAX ID: 0105558123456\nSubtotal: 845.00\nVAT 7%: 84.50\nGrand Total: 929.50"
            )
            "chokchai" -> ParsedReceipt(
                vendor = "Chok Chai Farm",
                date = today,
                amount = 2412.50,
                taxId = "0105532098765",
                category = "Food Supplies (COGS)",
                candidates = listOf(2412.50, 4825.00, 241.25),
                docType = "Tax Invoice / 50% Deposit",
                reason = "✓ Matched 50% Deposit invoice total: 2,412.50 THB",
                rawText = "ฟาร์มโชคชัย (Chokchai Farm)\nเลขประจำตัวผู้เสียภาษี 0105532098765\nยอดสุทธิ (Net Total) 2,412.50 บาท"
            )
            else -> parse("", "Receipt")
        }
    }
}
