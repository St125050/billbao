package com.billbao.app.ui.main

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.billbao.app.data.model.Bill
import com.billbao.app.data.ocr.ParsedReceipt
import com.billbao.app.data.ocr.ReceiptOcrParser
import com.billbao.app.ui.theme.BillbaoBorderDark
import com.billbao.app.ui.theme.BillbaoCardDark
import com.billbao.app.ui.theme.BillbaoCardElevatedDark
import com.billbao.app.ui.theme.BillbaoGreenContainer
import com.billbao.app.ui.theme.BillbaoGreenPrimary
import com.billbao.app.ui.theme.BillbaoShieldBlue
import com.billbao.app.ui.theme.BillbaoWarningOrange
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val SCANNER_CATEGORIES = listOf(
    "Food Supplies (COGS)",
    "Kitchen Equipment (Capex)",
    "Cooking Gas & Utilities",
    "Packaging & Disposables",
    "Repairs & Upkeep",
    "Staff Meals & Shifts",
    "Other Expense"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanBillDialog(
    currencySymbol: String = "฿",
    onDismiss: () -> Unit,
    onSaveBill: (Bill) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isOcrRunning by remember { mutableStateOf(false) }

    // Form fields extracted from OCR
    var vendorName by remember { mutableStateOf("") }
    var billDate by remember { mutableStateOf(today) }
    var selectedCategory by remember { mutableStateOf(SCANNER_CATEGORIES[0]) }
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }
    var totalAmountText by remember { mutableStateOf("") }
    var extractionReason by remember { mutableStateOf("Ready to scan bill or enter manually") }
    var candidateNumbers by remember { mutableStateOf<List<Double>>(emptyList()) }
    var taxId by remember { mutableStateOf("") }
    var lineItemsText by remember { mutableStateOf("") }
    var isClaimTaxChecked by remember { mutableStateOf(true) }
    var docTypeBadge by remember { mutableStateOf("Verified Bill") }

    fun populateFromParsedReceipt(parsed: ParsedReceipt) {
        vendorName = parsed.vendor
        billDate = parsed.date
        selectedCategory = if (SCANNER_CATEGORIES.contains(parsed.category)) parsed.category else SCANNER_CATEGORIES[0]
        totalAmountText = if (parsed.amount > 0) String.format(Locale.US, "%.2f", parsed.amount) else ""
        extractionReason = parsed.reason
        candidateNumbers = parsed.candidates
        taxId = parsed.taxId
        lineItemsText = parsed.rawText
        docTypeBadge = parsed.docType
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            isOcrRunning = true
            coroutineScope.launch {
                val ocrText = ReceiptOcrParser.recognizeText(bitmap)
                val parsed = ReceiptOcrParser.parse(ocrText, "Camera Receipt")
                populateFromParsedReceipt(parsed)
                isOcrRunning = false
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            isOcrRunning = true
            coroutineScope.launch {
                val ocrText = ReceiptOcrParser.recognizeTextFromUri(context, uri)
                val parsed = ReceiptOcrParser.parse(ocrText, "Photo Slip")
                populateFromParsedReceipt(parsed)
                isOcrRunning = false
            }
        }
    }

    // Laser Animation for Viewfinder
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 160f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserSweep"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BillbaoGreenPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = BillbaoGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Scan Bill / Receipt",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header card: CAPTURE OR UPLOAD RECEIPT
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BillbaoGreenPrimary.copy(alpha = 0.3f))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CAPTURE OR UPLOAD RECEIPT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = BillbaoGreenPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BillbaoGreenContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI OCR & Auto-Extract",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BillbaoGreenPrimary
                                )
                            }
                        }

                        // Take Photo & Upload Photo Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BillbaoGreenPrimary,
                                    contentColor = Color(0xFF06140D)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📸", style = MaterialTheme.typography.titleMedium)
                                    Text("Take Photo", fontWeight = FontWeight.Bold)
                                    Text("Auto Camera", style = MaterialTheme.typography.labelSmall, color = Color(0xFF06140D).copy(alpha = 0.8f))
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🖼️", style = MaterialTheme.typography.titleMedium)
                                    Text("Upload Photo", fontWeight = FontWeight.Bold)
                                    Text("Gallery / Slip", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Viewfinder Frame with laser sweep animation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0A121E))
                                .border(1.dp, BillbaoGreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (capturedBitmap != null) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "Captured Bill",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "📷 Point camera at receipt or select slip",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Animated Laser Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .offset(y = laserOffset.dp)
                                    .background(BillbaoGreenPrimary)
                            )

                            // HUD Labels
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isOcrRunning) "● SCANNING OCR..." else "● READY TO SCAN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BillbaoGreenPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("OCR Engine Active", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Auto Text Detect", style = MaterialTheme.typography.labelSmall, color = BillbaoGreenPrimary)
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Ready", style = MaterialTheme.typography.labelSmall, color = BillbaoWarningOrange)
                                }
                            }
                        }

                        if (isOcrRunning) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BillbaoGreenPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reading bill text with on-device AI OCR...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // 1-Tap Presets Section (Matching Reference Screen)
                Text(
                    text = "Or Try With Your Exact Uploaded Bills: (1-Tap Preset)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Preset 1: Govindam
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                populateFromParsedReceipt(ReceiptOcrParser.getPreset("govindam"))
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🍛 Govindam Cash", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("Handwritten (ลายมือ)", style = MaterialTheme.typography.labelSmall, color = BillbaoWarningOrange)
                            Text("฿720.00", fontWeight = FontWeight.ExtraBold, color = BillbaoGreenPrimary)
                        }
                    }

                    // Preset 2: Thai Sanguan
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                populateFromParsedReceipt(ReceiptOcrParser.getPreset("sanguan"))
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📑 Thai Sanguan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("Dash format (160 -)", style = MaterialTheme.typography.labelSmall, color = BillbaoWarningOrange)
                            Text("฿160.00", fontWeight = FontWeight.ExtraBold, color = BillbaoWarningOrange)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Preset 3: Dasaprakash
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                populateFromParsedReceipt(ReceiptOcrParser.getPreset("dasaprakash"))
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🇮🇳 Dasaprakash", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("Sub 845 vs Bill 929", style = MaterialTheme.typography.labelSmall, color = BillbaoGreenPrimary)
                            Text("฿929.50", fontWeight = FontWeight.ExtraBold, color = BillbaoGreenPrimary)
                        }
                    }

                    // Preset 4: Chok Chai Farm
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                populateFromParsedReceipt(ReceiptOcrParser.getPreset("chokchai"))
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🚜 Chok Chai Farm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("50% Deposit Match", style = MaterialTheme.typography.labelSmall, color = BillbaoShieldBlue)
                            Text("฿2,412.50", fontWeight = FontWeight.ExtraBold, color = BillbaoShieldBlue)
                        }
                    }
                }

                // Review & Edit Scanned Bill Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BillbaoGreenPrimary.copy(alpha = 0.35f))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Review & Edit Scanned Bill", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("Tap any field to edit before saving", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BillbaoGreenContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(docTypeBadge, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BillbaoGreenPrimary)
                            }
                        }

                        // Shop / Vendor Name
                        OutlinedTextField(
                            value = vendorName,
                            onValueChange = { vendorName = it },
                            label = { Text("Shop / Vendor Name *") },
                            placeholder = { Text("e.g. Siam Makro, Govindam, 7-Eleven") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Bill Date & Category
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = billDate,
                                onValueChange = { billDate = it },
                                label = { Text("Bill Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            // Category Dropdown
                            ExposedDropdownMenuBox(
                                expanded = isCategoryDropdownOpen,
                                onExpandedChange = { isCategoryDropdownOpen = !isCategoryDropdownOpen },
                                modifier = Modifier.weight(1.2f)
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownOpen) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = isCategoryDropdownOpen,
                                    onDismissRequest = { isCategoryDropdownOpen = false }
                                ) {
                                    SCANNER_CATEGORIES.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                selectedCategory = cat
                                                isCategoryDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // TOTAL BILL AMOUNT (Hero Input Box)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = BillbaoGreenContainer.copy(alpha = 0.5f)),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(BillbaoGreenPrimary.copy(alpha = 0.4f))
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOTAL BILL AMOUNT (THB):", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall, color = BillbaoGreenPrimary)
                                    Text("Editable", style = MaterialTheme.typography.labelSmall, color = BillbaoGreenPrimary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currencySymbol,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BillbaoGreenPrimary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    OutlinedTextField(
                                        value = totalAmountText,
                                        onValueChange = { totalAmountText = it },
                                        placeholder = { Text("0.00") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Text(extractionReason, style = MaterialTheme.typography.labelSmall, color = BillbaoGreenPrimary)
                            }
                        }

                        // Alternative Numbers Chips
                        if (candidateNumbers.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Alternative Numbers Found on Bill (Tap to pick):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    candidateNumbers.take(4).forEach { num ->
                                        val formatted = String.format(Locale.US, "%.2f", num)
                                        FilterChip(
                                            selected = totalAmountText == formatted,
                                            onClick = { totalAmountText = formatted },
                                            label = { Text("฿$formatted") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BillbaoGreenPrimary,
                                                selectedLabelColor = Color(0xFF06140D)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Tax ID / TIN (Optional - 13 Digits)
                        OutlinedTextField(
                            value = taxId,
                            onValueChange = { taxId = it },
                            label = { Text("Tax ID / TIN (Optional - 13 Digits)") },
                            placeholder = { Text("e.g. 0105558123456 (leave empty if cash bill)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Line Items & Raw Text
                        OutlinedTextField(
                            value = lineItemsText,
                            onValueChange = { lineItemsText = it },
                            label = { Text("Line Items & Raw Text (Saved Separately)") },
                            placeholder = { Text("Line items or description from receipt") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Add to "Claim Tax" Section Toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = BillbaoGreenPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Add to \"Claim Tax\" Section", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Qualifies as business deduction under Revenue Code", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Checkbox(
                                    checked = isClaimTaxChecked,
                                    onCheckedChange = { isClaimTaxChecked = it },
                                    colors = CheckboxDefaults.colors(checkedColor = BillbaoGreenPrimary)
                                )
                            }
                        }

                        // Final Save Button: Add Bill to Expenses & Claim Tax
                        Button(
                            onClick = {
                                val amount = totalAmountText.toDoubleOrNull() ?: 0.0
                                val title = vendorName.trim().ifEmpty { "Scanned Bill" }
                                if (amount > 0) {
                                    val newBill = Bill(
                                        title = title,
                                        amount = amount,
                                        category = selectedCategory,
                                        billDate = billDate.trim().ifEmpty { today },
                                        type = "EXPENSE",
                                        status = "PAID",
                                        notes = if (taxId.isNotBlank()) "Tax ID: $taxId\n$lineItemsText" else lineItemsText,
                                        imageUri = selectedUri?.toString() ?: "scanned_receipt",
                                        isTaxDeductible = isClaimTaxChecked,
                                        taxShieldAmount = if (isClaimTaxChecked) amount else 0.0
                                    )
                                    onSaveBill(newBill)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BillbaoGreenPrimary,
                                contentColor = Color(0xFF06140D)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✓ Add Bill to Expenses & Claim Tax", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        }
    )
}
