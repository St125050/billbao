package com.billbao.app.data.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.billbao.app.data.model.Bill
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelCsvExporter {

    /**
     * Exports bill ledger data as a proper RFC 4180 CSV / Excel spreadsheet file,
     * saves it directly into the device's public Downloads directory,
     * and prompts the user to Open or Share the downloaded file.
     */
    fun exportAndDownloadLedger(
        context: Context,
        bills: List<Bill>,
        monthFilter: String? = null
    ) {
        try {
            val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = if (!monthFilter.isNullOrBlank()) {
                "BillBao_Ledger_${monthFilter}_$dateStamp.csv"
            } else {
                "BillBao_Restaurant_Ledger_$dateStamp.csv"
            }

            // Generate RFC 4180 CSV with UTF-8 BOM (\uFEFF) for Microsoft Excel compatibility
            val csvContent = buildCsvContent(bills, monthFilter)

            // 1. Save to App Cache for immediate FileProvider intent sharing
            val cacheFile = File(context.cacheDir, filename)
            FileOutputStream(cacheFile).use { fos ->
                fos.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            // 2. Save directly into public Downloads folder so it is persistently downloaded
            saveToPublicDownloads(context, filename, csvContent.toByteArray(Charsets.UTF_8))

            // 3. Launch Open/Share Intent via FileProvider
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "BillBao Restaurant Expense Ledger (.csv / Excel)")
                putExtra(Intent.EXTRA_TEXT, "Here is the BillBao Restaurant Expense & Tax Ledger export.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Open / Share Excel & CSV File").apply {
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
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildCsvContent(bills: List<Bill>, monthFilter: String?): String {
        val sb = StringBuilder()
        // UTF-8 Byte Order Mark for Excel
        sb.append("\uFEFF")

        // Metadata Header
        sb.append("BillBao Restaurant Expense & Tax Ledger (บิลเบา)\n")
        sb.append("Export Date,\"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\"\n")
        if (!monthFilter.isNullOrBlank()) {
            sb.append("Filter Period,\"$monthFilter\"\n")
        }
        sb.append("Total Records,${bills.size}\n\n")

        // Table Column Headers
        sb.append("ID,Date,Merchant / Company,Category,Amount (THB),Status,Tax Deductible,Tax Shield (THB),Notes / Tax ID\n")

        var totalAmount = 0.0
        var totalShield = 0.0

        for (b in bills) {
            totalAmount += b.amount
            totalShield += b.taxShieldAmount

            val escapedTitle = escapeCsv(b.title)
            val escapedCategory = escapeCsv(b.category)
            val escapedNotes = escapeCsv(b.notes)
            val deductibleText = if (b.isTaxDeductible) "YES (Sec 65 Ter)" else "NO"

            sb.append("${b.id},")
            sb.append("\"${b.billDate}\",")
            sb.append("$escapedTitle,")
            sb.append("$escapedCategory,")
            sb.append(String.format(Locale.US, "%.2f,", b.amount))
            sb.append("\"${b.status}\",")
            sb.append("\"$deductibleText\",")
            sb.append(String.format(Locale.US, "%.2f,", b.taxShieldAmount))
            sb.append("$escapedNotes\n")
        }

        // Summary Rows
        val vatEstimate = totalShield * 0.07
        sb.append("\n")
        sb.append("--- SUMMARY TOTALS ---\n")
        sb.append("TOTAL EXPENSES (THB),${String.format(Locale.US, "%.2f", totalAmount)}\n")
        sb.append("TOTAL TAX SHIELD CLAIMABLE (THB),${String.format(Locale.US, "%.2f", totalShield)}\n")
        sb.append("ESTIMATED 7% INPUT VAT RECLAIM (THB),${String.format(Locale.US, "%.2f", vatEstimate)}\n")

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return "\"$clean\""
    }

    private fun saveToPublicDownloads(context: Context, filename: String, bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
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
