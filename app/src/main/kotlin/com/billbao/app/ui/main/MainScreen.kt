package com.billbao.app.ui.main

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.billbao.app.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.billbao.app.data.model.Bill
import com.billbao.app.data.model.CategorySpend
import com.billbao.app.data.model.LedgerSummary
import com.billbao.app.data.model.MonthlyReport
import com.billbao.app.data.model.UserProfile
import com.billbao.app.ui.theme.BillbaoGreenContainer
import com.billbao.app.ui.theme.BillbaoGreenPrimary
import com.billbao.app.ui.theme.BillbaoShieldBlue
import com.billbao.app.ui.theme.BillbaoTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Expenses, 1: Claim Tax, 2: Scan, 3: Settings, 4: Profile
    var showManualAddDialog by remember { mutableStateOf(false) }
    var showScanDialog by remember { mutableStateOf(false) }
    var showMonthlyReportModal by remember { mutableStateOf(false) }
    var selectedBillForDetail by remember { mutableStateOf<Bill?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAppInfoDialog by remember { mutableStateOf(false) }

    val bills by viewModel.filteredBills.collectAsStateWithLifecycle()
    val ledgerSummary by viewModel.ledgerSummary.collectAsStateWithLifecycle()
    val monthlyReport by viewModel.monthlyReport.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val distinctMonths by viewModel.distinctMonths.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()

    BillbaoTheme(darkTheme = isDarkTheme) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Official BillBao App Logo
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "BillBao Logo",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, BillbaoGreenPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "BillBao",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(บิลเบา)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // CLEAN LEDGER badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BillbaoGreenContainer)
                                            .border(1.dp, BillbaoGreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "CLEAN LEDGER",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BillbaoGreenPrimary
                                        )
                                    }
                                }
                                Text(
                                    text = "Scan Bills. Save Tax. Clean Ledger.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        // Dark/Light Theme toggle
                        IconButton(onClick = { viewModel.toggleDarkTheme() }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle Theme",
                                tint = BillbaoGreenPrimary
                            )
                        }
                        // Language toggle (EN / TH)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.toggleLanguage() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = selectedLanguage,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BillbaoGreenPrimary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Tab 1: Expenses
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Expenses") },
                        label = { Text("Expenses", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF06140D),
                            selectedTextColor = BillbaoGreenPrimary,
                            indicatorColor = BillbaoGreenPrimary
                        )
                    )
                    // Tab 2: Claim Tax
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.Shield, contentDescription = "Claim Tax") },
                        label = { Text("Claim Tax", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF06140D),
                            selectedTextColor = BillbaoShieldBlue,
                            indicatorColor = BillbaoShieldBlue
                        )
                    )
                    // Tab 3: Scan
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { showScanDialog = true },
                        icon = { Icon(Icons.Default.DocumentScanner, contentDescription = "Scan") },
                        label = { Text("Scan", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF06140D),
                            selectedTextColor = BillbaoGreenPrimary,
                            indicatorColor = BillbaoGreenPrimary
                        )
                    )
                    // Tab 4: Settings
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF06140D),
                            selectedTextColor = BillbaoGreenPrimary,
                            indicatorColor = BillbaoGreenPrimary
                        )
                    )
                    // Tab 5: Profile
                    NavigationBarItem(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF06140D),
                            selectedTextColor = BillbaoGreenPrimary,
                            indicatorColor = BillbaoGreenPrimary
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    0 -> {
                        // Expenses Dashboard (Identical to User's Target Screenshot)
                        ExpensesDashboardContent(
                            bills = bills,
                            ledgerSummary = ledgerSummary,
                            userProfile = userProfile,
                            onOpenScan = { showScanDialog = true },
                            onOpenManualAdd = { showManualAddDialog = true },
                            onOpenMonthlyReport = { showMonthlyReportModal = true },
                            onBillClick = { selectedBillForDetail = it },
                            onInstallInfoClick = { showAppInfoDialog = true },
                            onExportPdf = { viewModel.exportPdfReport(context, selectedMonth) },
                            onExportExcel = { viewModel.exportExcel(context, null) }
                        )
                    }
                    1 -> {
                        TaxShieldScreen(
                            bills = bills,
                            ledgerSummary = ledgerSummary,
                            currencySymbol = userProfile.currencySymbol
                        )
                    }
                    3 -> {
                        SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onToggleDarkTheme = viewModel::toggleDarkTheme,
                            selectedLanguage = selectedLanguage,
                            onToggleLanguage = viewModel::toggleLanguage,
                            ledgerSummary = ledgerSummary,
                            bills = bills,
                            onResetLedger = { showResetDialog = true },
                            onExportExcel = { viewModel.exportExcel(context, null) },
                            onExportPdf = { viewModel.exportPdfReport(context, selectedMonth) }
                        )
                    }
                    4 -> {
                        ProfileScreen(
                            profile = userProfile,
                            onSaveProfile = viewModel::updateUserProfile
                        )
                    }
                }
            }
        }

        // Add Manual Dialog
        if (showManualAddDialog) {
            UploadBillDialog(
                currencySymbol = userProfile.currencySymbol,
                onDismiss = { showManualAddDialog = false },
                onSave = { newBill ->
                    viewModel.addBill(newBill)
                    showManualAddDialog = false
                }
            )
        }

        // Scan Bill Dialog
        if (showScanDialog) {
            ScanBillDialog(
                currencySymbol = userProfile.currencySymbol,
                onDismiss = { showScanDialog = false },
                onSaveBill = { scannedBill ->
                    viewModel.addBill(scannedBill)
                    showScanDialog = false
                }
            )
        }

        // Monthly Report View Modal
        if (showMonthlyReportModal) {
            MonthlyReportModal(
                report = monthlyReport,
                selectedMonth = selectedMonth,
                distinctMonths = distinctMonths,
                currencySymbol = userProfile.currencySymbol,
                onMonthChange = viewModel::selectMonth,
                onDownloadPdf = { viewModel.exportPdfReport(context, selectedMonth) },
                onDismiss = { showMonthlyReportModal = false }
            )
        }

        // Bill Detail Dialog
        selectedBillForDetail?.let { bill ->
            BillDetailDialog(
                bill = bill,
                currencySymbol = userProfile.currencySymbol,
                onDismiss = { selectedBillForDetail = null },
                onToggleStatus = { viewModel.toggleBillStatus(it) },
                onDelete = { viewModel.deleteBill(it) }
            )
        }

        // Reset to Zero Confirmation
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                icon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Reset Ledger to ฿0.00?") },
                text = {
                    Text("This will wipe all uploaded and scanned bills from the SQLite database. Your ledger will reset to a clean zero start.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetLedgerToZero()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset to ฿0.00")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // App Independent Banner Info Dialog
        if (showAppInfoDialog) {
            AlertDialog(
                onDismissRequest = { showAppInfoDialog = false },
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "BillBao Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, BillbaoGreenPrimary, CircleShape)
                    )
                },
                title = { Text("Native BillBao (บิลเบา) App", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("You are running the standalone BillBao Android APK!")
                        Text("• 100% offline with local SQLite database (billbao.db).")
                        Text("• On-device Google ML Kit OCR bill scanning.")
                        Text("• Monthly A4 PDF audit reports with MoM comparison graphs.")
                        Text("• Direct Excel/CSV spreadsheet export to Downloads.")
                        Text("• Zero laptop or cloud server required.")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAppInfoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BillbaoGreenPrimary, contentColor = Color(0xFF06140D))
                    ) {
                        Text("Got it")
                    }
                }
            )
        }
    }
}

@Composable
private fun ExpensesDashboardContent(
    bills: List<Bill>,
    ledgerSummary: LedgerSummary,
    userProfile: UserProfile,
    onOpenScan: () -> Unit,
    onOpenManualAdd: () -> Unit,
    onOpenMonthlyReport: () -> Unit,
    onBillClick: (Bill) -> Unit,
    onInstallInfoClick: () -> Unit,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner: Install BillBao on This Phone
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BillbaoGreenPrimary.copy(alpha = 0.3f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📲", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Install BillBao on This Phone",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BillbaoGreenPrimary
                            )
                            Text(
                                text = "Runs 100% independently • Zero laptop required",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = onInstallInfoClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BillbaoGreenPrimary,
                            contentColor = Color(0xFF06140D)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Installed", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Hero Card: TOTAL RESTAURANT EXPENSES (Matching Screenshot)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BillbaoGreenPrimary.copy(alpha = 0.35f))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL RESTAURANT EXPENSES",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(BillbaoGreenContainer)
                                .border(1.dp, BillbaoGreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${ledgerSummary.totalBillsCount} Bills",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = BillbaoGreenPrimary
                            )
                        }
                    }

                    // Main Amount in Neon Emerald Green
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%s%.2f", userProfile.currencySymbol, ledgerSummary.totalBalance),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = BillbaoGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = userProfile.currency,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Footer: Claim Tax Shield
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Claim Tax Shield:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BillbaoShieldBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%s%.2f %s (100%%)", userProfile.currencySymbol, ledgerSummary.totalTaxShield, userProfile.currency),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = BillbaoGreenPrimary
                            )
                        }
                    }
                }
            }
        }

        // Month 1 Ledger • In Progress Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BillbaoGreenPrimary.copy(alpha = 0.25f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BillbaoGreenPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📊", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Month 1 Ledger • In Progress",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BillbaoGreenContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🌱 Fresh Start",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BillbaoGreenPrimary
                                    )
                                }
                            }
                            Text(
                                text = "First MoM Report & PDF on 1st @ 10:00 AM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = onOpenMonthlyReport,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BillbaoGreenPrimary,
                            contentColor = Color(0xFF06140D)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View PDF ➔", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Logged Bills & Receipts Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Logged Bills & Receipts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // + Add Manual
                    OutlinedButton(
                        onClick = onOpenManualAdd,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Manual")
                    }
                    // Scan Bill
                    Button(
                        onClick = onOpenScan,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BillbaoGreenPrimary,
                            contentColor = Color(0xFF06140D)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Bill", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Empty State or Bills Feed
        if (bills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BillbaoGreenPrimary.copy(alpha = 0.2f))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("🌱", style = MaterialTheme.typography.displayMedium)
                        Text(
                            text = "Fresh Clean Ledger Ready",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "0 bills recorded. You are starting with a clean slate. Tap \"Scan Bill\" or \"Add Manual\" to log your first restaurant expense.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onOpenScan,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BillbaoGreenPrimary,
                                    contentColor = Color(0xFF06140D)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan First Bill", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onOpenManualAdd,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Manual")
                            }
                        }
                    }
                }
            }
        } else {
            items(bills, key = { it.id }) { bill ->
                BillItemCard(
                    bill = bill,
                    currencySymbol = userProfile.currencySymbol,
                    onClick = { onBillClick(bill) }
                )
            }
        }

        // Dual Export Actions: PDF & Excel
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BillbaoGreenPrimary,
                        contentColor = Color(0xFF06140D)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Monthly PDF Report", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExportExcel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Excel (.xlsx)")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BillItemCard(
    bill: Bill,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val isPaid = bill.status == "PAID"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BillbaoGreenPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = BillbaoGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${bill.category} • ${bill.billDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "%s%.2f", currencySymbol, bill.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BillbaoGreenPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPaid) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isPaid) "PAID" else "PENDING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyReportModal(
    report: MonthlyReport,
    selectedMonth: String,
    distinctMonths: List<String>,
    currencySymbol: String,
    onMonthChange: (String) -> Unit,
    onDownloadPdf: () -> Unit = {},
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PieChart, contentDescription = null, tint = BillbaoGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Monthly Report", fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Clear, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val cal = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
                        sdf.parse(selectedMonth)?.let {
                            cal.time = it
                            cal.add(Calendar.MONTH, -1)
                            onMonthChange(sdf.format(cal.time))
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Prev")
                    }
                    Text(report.displayTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        val cal = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
                        sdf.parse(selectedMonth)?.let {
                            cal.time = it
                            cal.add(Calendar.MONTH, 1)
                            onMonthChange(sdf.format(cal.time))
                        }
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }

                // Month Metrics Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("TOTAL SPEND FOR MONTH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format(Locale.US, "%s%.2f", currencySymbol, report.totalAmount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = BillbaoGreenPrimary
                        )
                        Text("${report.totalCount} Bills Uploaded • Tax Shield: $currencySymbol${String.format(Locale.US, "%.2f", report.totalTaxShield)}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (report.categoryBreakdown.isEmpty()) {
                    Text(
                        "No bills uploaded for ${report.displayTitle}. Ledger total is $currencySymbol 0.00.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text("Category Breakdown:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    report.categoryBreakdown.forEach { cat ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat.category, style = MaterialTheme.typography.bodySmall)
                                Text("$currencySymbol${String.format(Locale.US, "%.2f", cat.amount)} (${String.format(Locale.US, "%.1f", cat.percentage)}%)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            LinearProgressIndicator(
                                progress = { cat.percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = BillbaoGreenPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onDownloadPdf() },
                    colors = ButtonDefaults.buttonColors(containerColor = BillbaoGreenPrimary, contentColor = Color(0xFF06140D)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download PDF Report", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            }
        }
    )
}
