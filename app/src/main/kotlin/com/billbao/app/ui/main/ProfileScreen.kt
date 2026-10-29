package com.billbao.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.billbao.app.data.model.UserProfile
import com.billbao.app.ui.theme.BillbaoGreenPrimary

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf(false) }

    // Edit fields state
    var editName by remember(profile) { mutableStateOf(profile.name) }
    var editRole by remember(profile) { mutableStateOf(profile.role) }
    var editBizName by remember(profile) { mutableStateOf(profile.businessName) }
    var editTaxId by remember(profile) { mutableStateOf(profile.taxId) }
    var editBranch by remember(profile) { mutableStateOf(profile.branch) }
    var editEmail by remember(profile) { mutableStateOf(profile.email) }
    var editPhone by remember(profile) { mutableStateOf(profile.phone) }
    var editAddress by remember(profile) { mutableStateOf(profile.address) }
    var editCurrency by remember(profile) { mutableStateOf(profile.currency) }
    var editSymbol by remember(profile) { mutableStateOf(profile.currencySymbol) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card (matching BillBao.html)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar with Chef emoji & Camera badge
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(BillbaoGreenPrimary.copy(alpha = 0.2f))
                            .border(2.dp, BillbaoGreenPrimary, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👨‍🍳",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = profile.role,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BillbaoGreenPrimary
                        )
                    }

                    // License Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BillbaoGreenPrimary.copy(alpha = 0.15f))
                            .border(1.dp, BillbaoGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BillBao Pro • Active License",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BillbaoGreenPrimary
                            )
                        }
                    }

                    // Action Buttons (Edit Profile / New User)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isEditing) {
                                    val updated = profile.copy(
                                        name = editName.trim(),
                                        role = editRole.trim(),
                                        businessName = editBizName.trim(),
                                        taxId = editTaxId.trim(),
                                        branch = editBranch.trim(),
                                        email = editEmail.trim(),
                                        phone = editPhone.trim(),
                                        address = editAddress.trim(),
                                        currency = editCurrency.trim(),
                                        currencySymbol = editSymbol.trim()
                                    )
                                    onSaveProfile(updated)
                                    isEditing = false
                                    saveSuccessMessage = true
                                } else {
                                    isEditing = true
                                    saveSuccessMessage = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEditing) BillbaoGreenPrimary else MaterialTheme.colorScheme.primary,
                                contentColor = if (isEditing) Color(0xFF06140D) else MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEditing) "Save Profile" else "Edit Profile", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                editName = "New Owner"
                                editRole = "Owner & Manager"
                                editBizName = "New Restaurant"
                                editTaxId = "-"
                                editBranch = "Head Office (00000)"
                                editEmail = "owner@newrestaurant.co"
                                editPhone = "-"
                                editAddress = "Thailand"
                                isEditing = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("New User Setup")
                        }
                    }

                    AnimatedVisibility(visible = saveSuccessMessage) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = BillbaoGreenPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Profile updated in local database!",
                                style = MaterialTheme.typography.labelMedium,
                                color = BillbaoGreenPrimary
                            )
                        }
                    }
                }
            }
        }

        // Contact Information Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📞 Owner Contact Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isEditing) {
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BillbaoGreenPrimary
                            )
                        }
                    }

                    if (!isEditing) {
                        ProfileInfoRow(icon = Icons.Default.Phone, label = "Phone Number", value = profile.phone)
                        ProfileInfoRow(icon = Icons.Default.Email, label = "Email ID", value = profile.email)
                    } else {
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email ID") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Registered Business Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏢 Registered Business Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isEditing) {
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BillbaoGreenPrimary
                            )
                        }
                    }

                    if (!isEditing) {
                        ProfileInfoRow(icon = Icons.Default.Person, label = "Owner Name", value = profile.name)
                        ProfileInfoRow(icon = Icons.Default.Badge, label = "Role / Title", value = profile.role)
                        ProfileInfoRow(icon = Icons.Default.Business, label = "Business Name", value = profile.businessName)
                        ProfileInfoRow(icon = Icons.Default.Shield, label = "13-Digit Tax ID (TIN)", value = profile.taxId)
                        ProfileInfoRow(icon = Icons.Default.LocationOn, label = "Branch", value = profile.branch)
                        ProfileInfoRow(icon = Icons.Default.LocationOn, label = "Registered Address", value = profile.address)
                        ProfileInfoRow(icon = Icons.Default.CurrencyExchange, label = "Currency", value = "${profile.currency} (${profile.currencySymbol})")
                    } else {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Owner Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editRole,
                            onValueChange = { editRole = it },
                            label = { Text("Role / Title *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editBizName,
                            onValueChange = { editBizName = it },
                            label = { Text("Business Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editTaxId,
                            onValueChange = { editTaxId = it },
                            label = { Text("13-Digit Tax ID (TIN) *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editBranch,
                            onValueChange = { editBranch = it },
                            label = { Text("Branch (e.g. Head Office 00000)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text("Registered Address") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editCurrency,
                                onValueChange = { editCurrency = it },
                                label = { Text("Currency") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editSymbol,
                                onValueChange = { editSymbol = it },
                                label = { Text("Symbol") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value.ifEmpty { "Not set yet" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
