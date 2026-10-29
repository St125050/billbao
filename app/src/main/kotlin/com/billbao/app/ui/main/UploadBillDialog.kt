package com.billbao.app.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.billbao.app.data.model.Bill
import com.billbao.app.ui.theme.BillbaoGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val BILL_CATEGORIES = listOf(
    "Food & Restaurant",
    "Kitchen Supplies",
    "Utilities",
    "Rent & Lease",
    "Beverages",
    "Staff & Payroll",
    "Equipment & Maintenance",
    "Marketing",
    "Other Expense"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadBillDialog(
    currencySymbol: String = "฿",
    onDismiss: () -> Unit,
    onSave: (Bill) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var billDate by remember { mutableStateOf(today) }
    var selectedCategory by remember { mutableStateOf(BILL_CATEGORIES[0]) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("PAID") }
    var isTaxDeductible by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = BillbaoGreenPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Add Manual Bill",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Stored permanently in your local SQLite database. Sets earlier month reports dynamically when you record past dates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Vendor / Merchant Name *") },
                    placeholder = { Text("e.g. Makro Wholesale, Central Food Hall") },
                    leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    isError = titleError,
                    supportingText = { if (titleError) Text("Vendor title is required") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount with currency symbol
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = false
                    },
                    label = { Text("Amount ($currencySymbol) *") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError,
                    supportingText = { if (amountError) Text("Please enter a valid amount > 0") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Picker field (YYYY-MM-DD)
                OutlinedTextField(
                    value = billDate,
                    onValueChange = {
                        billDate = it
                        dateError = false
                    },
                    label = { Text("Bill Date (YYYY-MM-DD) *") },
                    supportingText = { Text("Can record earlier months (e.g. 2026-08-20)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    isError = dateError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = isCategoryExpanded,
                    onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false }
                    ) {
                        BILL_CATEGORIES.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    isCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Tax Shield toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = BillbaoGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Claim Tax Shield (100%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Add to business tax deductions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isTaxDeductible,
                            onCheckedChange = { isTaxDeductible = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = BillbaoGreenPrimary)
                        )
                    }
                }

                // Payment Status
                Text(
                    text = "Payment Status:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = status == "PAID",
                        onClick = { status = "PAID" },
                        label = { Text("Paid") },
                        leadingIcon = if (status == "PAID") {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                    FilterChip(
                        selected = status == "PENDING",
                        onClick = { status = "PENDING" },
                        label = { Text("Pending / Due") }
                    )
                }

                // Notes / Description
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Invoice Number (Optional)") },
                    placeholder = { Text("e.g. Inv #8839, paid via Bank Transfer") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull()
                    val isValidTitle = title.isNotBlank()
                    val isValidAmount = parsedAmount != null && parsedAmount > 0
                    val isValidDate = billDate.matches(Regex("""\d{4}-\d{2}-\d{2}"""))

                    if (!isValidTitle) titleError = true
                    if (!isValidAmount) amountError = true
                    if (!isValidDate) dateError = true

                    if (isValidTitle && isValidAmount && isValidDate) {
                        val newBill = Bill(
                            title = title.trim(),
                            amount = parsedAmount,
                            category = selectedCategory,
                            billDate = billDate.trim(),
                            type = "EXPENSE",
                            status = status,
                            notes = notes.trim(),
                            imageUri = null,
                            isTaxDeductible = isTaxDeductible,
                            taxShieldAmount = if (isTaxDeductible) parsedAmount else 0.0
                        )
                        onSave(newBill)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BillbaoGreenPrimary,
                    contentColor = Color(0xFF06140D)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Bill", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        }
    )
}
