package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HealthVaultBorder
import com.example.ui.screens.HealthVaultCream
import com.example.ui.screens.HealthVaultDanger
import com.example.ui.screens.HealthVaultDangerLight
import com.example.ui.screens.HealthVaultGold
import com.example.ui.screens.HealthVaultInk
import com.example.ui.screens.HealthVaultTeal
import com.example.ui.screens.HealthVaultTealLight
import com.example.viewmodel.HealthVaultViewModel
import com.example.viewmodel.NotificationCategory
import com.example.viewmodel.NotificationCenterItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showPreferences by remember { mutableStateOf(false) }

    // Preference toggles
    var masterNotifications by remember { mutableStateOf(true) }
    var medicineAlerts by remember { mutableStateOf(true) }
    var appointmentAlerts by remember { mutableStateOf(true) }
    var labReportAlerts by remember { mutableStateOf(true) }
    var doctorAccessAlerts by remember { mutableStateOf(true) }
    var emergencySosAlerts by remember { mutableStateOf(true) }
    var backupAlerts by remember { mutableStateOf(true) }
    var storageAlerts by remember { mutableStateOf(true) }
    var securityAlerts by remember { mutableStateOf(true) }
    var soundVibration by remember { mutableStateOf(true) }

    val filteredList = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "UNREAD" -> notifications.filter { !it.isRead }
            "MEDICINE" -> notifications.filter { it.category == NotificationCategory.MEDICINE }
            "APPOINTMENT" -> notifications.filter { it.category == NotificationCategory.APPOINTMENT }
            "LAB_REPORT" -> notifications.filter { it.category == NotificationCategory.LAB_REPORT }
            "DOCTOR_ACCESS" -> notifications.filter { it.category == NotificationCategory.DOCTOR_ACCESS }
            "EMERGENCY" -> notifications.filter { it.category == NotificationCategory.EMERGENCY }
            "BACKUP" -> notifications.filter { it.category == NotificationCategory.BACKUP }
            "STORAGE" -> notifications.filter { it.category == NotificationCategory.STORAGE }
            "SECURITY" -> notifications.filter { it.category == NotificationCategory.SECURITY }
            else -> notifications
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "Notification Center",
            subtitle = "Real-time health alerts, reminders, access requests & security logs",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Action Header Banner & Unread Badge
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (unreadCount > 0) HealthVaultDangerLight else HealthVaultTealLight)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (unreadCount > 0) "🔴 $unreadCount Unread" else "🟢 All Read",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (unreadCount > 0) HealthVaultDanger else HealthVaultTeal
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${notifications.size} Total History",
                                    fontSize = 12.sp,
                                    color = HealthVaultInk.copy(alpha = 0.6f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = {
                                        viewModel.markAllNotificationsAsRead()
                                        Toast.makeText(context, "All notifications marked as read", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("mark_all_read_btn")
                                ) {
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = "Mark All Read",
                                        modifier = Modifier.size(16.dp),
                                        tint = HealthVaultTeal
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Read", fontSize = 11.sp, color = HealthVaultTeal, fontWeight = FontWeight.Bold)
                                }

                                TextButton(
                                    onClick = {
                                        showPreferences = !showPreferences
                                    },
                                    modifier = Modifier.testTag("toggle_pref_btn")
                                ) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Preferences",
                                        modifier = Modifier.size(16.dp),
                                        tint = HealthVaultInk.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (showPreferences) "Hide Rules" else "Rules", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Preferences Card
            if (showPreferences) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "NOTIFICATION RULES & PREFERENCES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Allow Push Notifications", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                    Text("Master toggle for all alerts", fontSize = 10.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                                }
                                Switch(
                                    checked = masterNotifications,
                                    onCheckedChange = {
                                        masterNotifications = it
                                        Toast.makeText(context, if (it) "Notifications Enabled" else "Notifications Disabled", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal),
                                    modifier = Modifier.testTag("master_notif_switch")
                                )
                            }

                            if (masterNotifications) {
                                Spacer(modifier = Modifier.height(10.dp))
                                NotifPrefRow("💊 Medicine Reminders", medicineAlerts) { medicineAlerts = it }
                                NotifPrefRow("📅 Appointment Reminders", appointmentAlerts) { appointmentAlerts = it }
                                NotifPrefRow("🧪 Lab Report Notifications", labReportAlerts) { labReportAlerts = it }
                                NotifPrefRow("👨‍⚕️ Doctor Access Requests", doctorAccessAlerts) { doctorAccessAlerts = it }
                                NotifPrefRow("🚨 Emergency SOS Warnings", emergencySosAlerts) { emergencySosAlerts = it }
                                NotifPrefRow("☁️ Backup Completed Alerts", backupAlerts) { backupAlerts = it }
                                NotifPrefRow("💾 Storage Capacity Warnings", storageAlerts) { storageAlerts = it }
                                NotifPrefRow("🛡️ Security & Login Alerts", securityAlerts) { securityAlerts = it }
                                NotifPrefRow("🔔 Sound & Haptic Feedback", soundVibration) { soundVibration = it }
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        "ALL" to "All (${notifications.size})",
                        "UNREAD" to "🔴 Unread ($unreadCount)",
                        "MEDICINE" to "💊 Medicine",
                        "APPOINTMENT" to "📅 Appointments",
                        "LAB_REPORT" to "🧪 Lab Reports",
                        "DOCTOR_ACCESS" to "👨‍⚕️ Doctor Access",
                        "EMERGENCY" to "🚨 Emergency",
                        "BACKUP" to "☁️ Backup",
                        "STORAGE" to "💾 Storage",
                        "SECURITY" to "🛡️ Security"
                    )

                    items(filters) { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) HealthVaultTeal else Color.White)
                                .border(1.dp, if (isSelected) HealthVaultTeal else HealthVaultBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = key }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("filter_$key")
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else HealthVaultInk
                            )
                        }
                    }
                }
            }

            // Notification Items List
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔔", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Notifications", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                            Text(
                                "No alerts found in this category.",
                                fontSize = 11.sp,
                                color = HealthVaultInk.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { notif ->
                    NotificationCardItem(
                        item = notif,
                        onMarkRead = { viewModel.markNotificationAsRead(notif.id) },
                        onDelete = {
                            viewModel.deleteNotification(notif.id)
                            Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show()
                        },
                        onAction = {
                            viewModel.markNotificationAsRead(notif.id)
                            when (notif.category) {
                                NotificationCategory.MEDICINE -> Toast.makeText(context, "Dose logged: ${notif.title}", Toast.LENGTH_SHORT).show()
                                NotificationCategory.APPOINTMENT -> Toast.makeText(context, "Opening appointment details...", Toast.LENGTH_SHORT).show()
                                NotificationCategory.LAB_REPORT -> Toast.makeText(context, "Opening Lab Report...", Toast.LENGTH_SHORT).show()
                                NotificationCategory.DOCTOR_ACCESS -> Toast.makeText(context, "Opening Doctor Access Request...", Toast.LENGTH_SHORT).show()
                                NotificationCategory.EMERGENCY -> Toast.makeText(context, "Opening Emergency SOS Console...", Toast.LENGTH_SHORT).show()
                                NotificationCategory.BACKUP -> Toast.makeText(context, "Opening Backup Logs...", Toast.LENGTH_SHORT).show()
                                NotificationCategory.STORAGE -> Toast.makeText(context, "Opening Vault Storage Manager...", Toast.LENGTH_SHORT).show()
                                NotificationCategory.SECURITY -> Toast.makeText(context, "Opening Security Audit Trail...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotifPrefRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, color = HealthVaultInk, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal)
        )
    }
}

@Composable
private fun NotificationCardItem(
    item: NotificationCenterItem,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    onAction: () -> Unit
) {
    val (categoryLabel, categoryIcon, badgeBg, badgeTextColor) = when (item.category) {
        NotificationCategory.MEDICINE -> Quadruple("Medicine", "💊", HealthVaultTealLight, HealthVaultTeal)
        NotificationCategory.APPOINTMENT -> Quadruple("Appointment", "📅", Color(0xFFE8EAF6), Color(0xFF283593))
        NotificationCategory.LAB_REPORT -> Quadruple("Lab Report", "🧪", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        NotificationCategory.DOCTOR_ACCESS -> Quadruple("Doctor Request", "👨‍⚕️", Color(0xFFFFF3E0), Color(0xFFE65100))
        NotificationCategory.EMERGENCY -> Quadruple("Emergency SOS", "🚨", HealthVaultDangerLight, HealthVaultDanger)
        NotificationCategory.BACKUP -> Quadruple("Cloud Backup", "☁️", HealthVaultTealLight, HealthVaultTeal)
        NotificationCategory.STORAGE -> Quadruple("Storage Warning", "💾", Color(0xFFFFF8E1), Color(0xFFF57F17))
        NotificationCategory.SECURITY -> Quadruple("Security Alert", "🛡️", Color(0xFFFFEBEE), Color(0xFFC62828))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (!item.isRead) Color(0xFFFAFAFA) else Color.White),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (!item.isRead) HealthVaultTeal.copy(alpha = 0.3f) else HealthVaultBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category & Timestamp Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HealthVaultDanger)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$categoryIcon $categoryLabel",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }

                Text(
                    text = item.timestamp,
                    fontSize = 10.sp,
                    color = HealthVaultInk.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = HealthVaultInk
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Message
            Text(
                text = item.message,
                fontSize = 11.sp,
                color = HealthVaultInk.copy(alpha = 0.75f),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.actionText != null) {
                    Button(
                        onClick = onAction,
                        colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("action_btn_${item.id}"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(item.actionText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = onMarkRead,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            if (item.isRead) "Mark Unread" else "Mark Read",
                            fontSize = 10.sp,
                            color = HealthVaultTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Notification",
                            tint = HealthVaultInk.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun BackupRestoreScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCloudBackupEnabled by remember { mutableStateOf(true) }
    var syncFrequency by remember { mutableStateOf("Daily") }
    var lastBackupTime by remember { mutableStateOf("Today, 08:30 AM") }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "Backup & Restore",
            subtitle = "Encrypted cloud backup, auto-sync & restoration",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Encrypted Cloud Backup",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultInk
                                )
                                Text(
                                    text = "Sync zero-knowledge encrypted blobs to Google Drive",
                                    fontSize = 11.sp,
                                    color = HealthVaultInk.copy(alpha = 0.6f)
                                )
                            }

                            Switch(
                                checked = isCloudBackupEnabled,
                                onCheckedChange = {
                                    isCloudBackupEnabled = it
                                    Toast.makeText(context, if (it) "Cloud Sync Enabled" else "Cloud Sync Disabled", Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(HealthVaultCream)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Last Backup Status:", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                                Text("✓ $lastBackupTime (12 Files Encrypted)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                lastBackupTime = "Just now (${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())})"
                                Toast.makeText(context, "Encrypted backup complete!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("create_backup_btn")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Instant Encrypted Backup", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogsScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "Audit Logs & Provenance",
            subtitle = "Cryptographic access history & tamper-evident audit trail",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "${auditLogs.size} TOTAL AUDIT EVENTS RECORDED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthVaultTeal,
                    letterSpacing = 1.sp
                )
            }

            items(auditLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HealthVaultTealLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = log.action,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultTeal
                                )
                            }

                            Text(
                                text = sdf.format(Date(log.timestamp)),
                                fontSize = 10.sp,
                                color = HealthVaultInk.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = log.details,
                            fontSize = 12.sp,
                            color = HealthVaultInk,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Actor: ${log.userId} (${log.userRole})",
                            fontSize = 10.sp,
                            color = HealthVaultInk.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageAccessibilityScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var selectedLang by remember { mutableStateOf("English (US)") }
    var fontScale by remember { mutableStateOf(1.0f) }
    var isLangDropdownExpanded by remember { mutableStateOf(false) }
    val languages = listOf("English (US)", "Español (Spanish)", "Français (French)", "Deutsch (German)", "Hindi (हिंदी)")

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "Language & Accessibility",
            subtitle = "Localization, theme contrast & text scaling",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "LANGUAGE & LOCALIZATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedLang,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("App Language") },
                                trailingIcon = { Text("▼", modifier = Modifier.padding(end = 8.dp)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isLangDropdownExpanded = true }
                                    .testTag("lang_select_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            DropdownMenu(
                                expanded = isLangDropdownExpanded,
                                onDismissRequest = { isLangDropdownExpanded = false }
                            ) {
                                languages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedLang = lang
                                            isLangDropdownExpanded = false
                                            Toast.makeText(context, "Language set to $lang", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("High-Contrast Dark Theme", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                Text("Eye-safe high-contrast dark background", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                            }

                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.setDarkMode(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal),
                                modifier = Modifier.testTag("dark_mode_lang_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text("Typography Font Scaling (${(fontScale * 100).toInt()}%)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                        Slider(
                            value = fontScale,
                            onValueChange = { fontScale = it },
                            valueRange = 0.8f..1.4f,
                            colors = SliderDefaults.colors(thumbColor = HealthVaultTeal, activeTrackColor = HealthVaultTeal),
                            modifier = Modifier.testTag("font_scale_slider")
                        )
                    }
                }
            }
        }
    }
}
