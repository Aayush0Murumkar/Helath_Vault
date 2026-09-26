package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.screens.HealthVaultBorder
import com.example.ui.screens.HealthVaultCream
import com.example.ui.screens.HealthVaultDanger
import com.example.ui.screens.HealthVaultGold
import com.example.ui.screens.HealthVaultInk
import com.example.ui.screens.HealthVaultTeal
import com.example.ui.screens.HealthVaultTealLight
import com.example.viewmodel.HealthVaultViewModel

data class ConnectedDeviceItem(val id: String, val deviceName: String, val location: String, val lastActive: String, val isCurrent: Boolean)

@Composable
fun ConnectedDevicesScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var devices by remember {
        mutableStateOf(
            listOf(
                ConnectedDeviceItem("DEV-1", "Pixel 8 Pro (Primary Phone)", "Mountain View, CA", "Active Now", true),
                ConnectedDeviceItem("DEV-2", "Galaxy Tab S9 Enclave Vault", "San Francisco, CA", "2 hours ago", false),
                ConnectedDeviceItem("DEV-3", "MacBook Pro Enclave Web Portal", "San Jose, CA", "Yesterday at 18:40", false)
            )
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "Connected Devices & Sessions",
            subtitle = "Active device authorizations & session revocation",
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
                            Column {
                                Text("ACTIVE SESSIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk, letterSpacing = 0.5.sp)
                                Text("${devices.size} Devices authenticated", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                            }

                            if (devices.size > 1) {
                                OutlinedButton(
                                    onClick = {
                                        devices = devices.filter { it.isCurrent }
                                        Toast.makeText(context, "Terminated all other device sessions", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthVaultDanger),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultDanger),
                                    modifier = Modifier.testTag("terminate_other_sessions_btn")
                                ) {
                                    Text("Log Out Others", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            items(devices) { dev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(HealthVaultTealLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Smartphone, contentDescription = null, tint = HealthVaultTeal)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(dev.deviceName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                    if (dev.isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(HealthVaultTeal)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("THIS DEVICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text("📍 ${dev.location} • Active: ${dev.lastActive}", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                            }
                        }

                        if (!dev.isCurrent) {
                            OutlinedButton(
                                onClick = {
                                    devices = devices.filter { it.id != dev.id }
                                    Toast.makeText(context, "Revoked session for ${dev.deviceName}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthVaultDanger),
                                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultDanger.copy(alpha = 0.4f)),
                                modifier = Modifier.testTag("revoke_dev_${dev.id}")
                            ) {
                                Text("Revoke", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencySettingsScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var shakeToSos by remember { mutableStateOf(true) }
    var powerTriplePress by remember { mutableStateOf(true) }
    var hospitalName by remember { mutableStateOf("St. Jude Central Hospital & ER Enclave") }
    var hospitalHotline by remember { mutableStateOf("+1 (800) 555-0199") }
    var primaryContact by remember { mutableStateOf("+1 (555) 987-6543 (John Connor)") }
    var customSosMessage by remember { mutableStateOf("MEDICAL SOS: Patient PAT-98421 requires immediate ER assistance. Blood Type: O+. Location broadcast active.") }
    var shareLiveGps by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "Emergency Settings",
            subtitle = "SOS configuration, preferred hospital & hotline settings",
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
                        Text("SOS TRIGGER METHODS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk, letterSpacing = 0.5.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Shake Device for SOS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                Text("Accelerometer gesture trigger", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                            }
                            Switch(checked = shakeToSos, onCheckedChange = { shakeToSos = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Power Button Triple Press", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                Text("Hardware key emergency combo", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                            }
                            Switch(checked = powerTriplePress, onCheckedChange = { powerTriplePress = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal))
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("PREFERRED HOSPITAL & CONTACTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk, letterSpacing = 0.5.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = hospitalName,
                            onValueChange = { hospitalName = it },
                            label = { Text("Preferred Hospital") },
                            modifier = Modifier.fillMaxWidth().testTag("sos_hospital_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = hospitalHotline,
                            onValueChange = { hospitalHotline = it },
                            label = { Text("ER Hotline") },
                            modifier = Modifier.fillMaxWidth().testTag("sos_hotline_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = primaryContact,
                            onValueChange = { primaryContact = it },
                            label = { Text("Primary Emergency Contact") },
                            modifier = Modifier.fillMaxWidth().testTag("sos_contact_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customSosMessage,
                            onValueChange = { customSosMessage = it },
                            label = { Text("Emergency Broadcast Message") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("sos_message_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Share Live GPS Location during SOS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                            Switch(checked = shareLiveGps, onCheckedChange = { shareLiveGps = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Emergency SOS Settings Saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("save_sos_settings_btn")
                        ) {
                            Text("Save Emergency Configuration", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppPreferencesScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAiCardOnHome by remember { mutableStateOf(true) }
    var reduceMotion by remember { mutableStateOf(false) }
    var hapticFeedback by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "App Preferences",
            subtitle = "Dashboard layout, visual animations & interaction style",
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
                        Text("DASHBOARD & INTERACTION PREFERENCES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk, letterSpacing = 0.5.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Show AI Clinical Summary Card", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                Text("Display Gemini medical analyzer on Home tab", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                            }
                            Switch(checked = showAiCardOnHome, onCheckedChange = { showAiCardOnHome = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reduce Motion / Animations", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                Text("Disable pulse animations for faster screen rendering", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                            }
                            Switch(checked = reduceMotion, onCheckedChange = { reduceMotion = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Haptic Touch Feedback", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                Text("Vibrate device gently on key button presses", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                            }
                            Switch(checked = hapticFeedback, onCheckedChange = { hapticFeedback = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HealthVaultTeal))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeDialog by remember { mutableStateOf<String?>(null) }
    var bugDescription by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "About Health Vault",
            subtitle = "Architecture, legal terms, support & feedback",
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
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(HealthVaultTealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = HealthVaultTeal, modifier = Modifier.size(36.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Health Vault Enclave", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                        Text("Version 1.2.0 • Build 2026.08", fontSize = 12.sp, color = HealthVaultTeal, fontWeight = FontWeight.SemiBold)
                        Text("Zero-Knowledge Local AES-256 Medical Vault", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("INFORMATION & SUPPORT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk, letterSpacing = 0.5.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = { activeDialog = "PRIVACY" },
                            modifier = Modifier.fillMaxWidth().testTag("privacy_policy_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("View Privacy Policy & HIPAA Compliance")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { activeDialog = "TERMS" },
                            modifier = Modifier.fillMaxWidth().testTag("terms_service_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Terms of Service & Usage Agreement")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { activeDialog = "FAQ" },
                            modifier = Modifier.fillMaxWidth().testTag("faqs_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Frequently Asked Questions (FAQs)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { activeDialog = "BUG" },
                            modifier = Modifier.fillMaxWidth().testTag("report_bug_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Report a Bug / Send Feedback", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal dialogs for Privacy, Terms, FAQ, Bug
    when (activeDialog) {
        "PRIVACY" -> {
            Dialog(onDismissRequest = { activeDialog = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("PRIVACY POLICY", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HealthVaultTeal)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Health Vault operates on a strictly Zero-Knowledge architecture. All medical files and records are encrypted locally on your device using hardware-backed AES-256-GCM keys prior to storage. No unencrypted patient data or raw medical files are ever transmitted or accessible to external servers without your explicit permission.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = HealthVaultInk
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal), modifier = Modifier.fillMaxWidth()) {
                            Text("Close")
                        }
                    }
                }
            }
        }

        "TERMS" -> {
            Dialog(onDismissRequest = { activeDialog = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("TERMS OF SERVICE", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HealthVaultTeal)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "By using Health Vault, you retain 100% ownership of your health records. You are solely responsible for maintaining your security PIN and nominee credentials. Emergency access features function according to your designated access rules.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = HealthVaultInk
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal), modifier = Modifier.fillMaxWidth()) {
                            Text("Close")
                        }
                    }
                }
            }
        }

        "FAQ" -> {
            Dialog(onDismissRequest = { activeDialog = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("FREQUENTLY ASKED QUESTIONS", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HealthVaultTeal)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Q: How does doctor access work?\nA: Doctors request access, which you can approve for 15, 30, or 60 minutes. You can revoke access at any time.", fontSize = 12.sp, lineHeight = 18.sp, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Q: What happens in an SOS emergency?\nA: Your designated nominees can receive emergency key access and custom broadcast alerts.", fontSize = 12.sp, lineHeight = 18.sp, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal), modifier = Modifier.fillMaxWidth()) {
                            Text("Close")
                        }
                    }
                }
            }
        }

        "BUG" -> {
            Dialog(onDismissRequest = { activeDialog = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("REPORT A BUG / FEEDBACK", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HealthVaultTeal)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = bugDescription,
                            onValueChange = { bugDescription = it },
                            placeholder = { Text("Describe the issue or suggestion...") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("bug_desc_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { activeDialog = null }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                            Button(
                                onClick = {
                                    if (bugDescription.isNotBlank()) {
                                        Toast.makeText(context, "Feedback submitted! Thank you.", Toast.LENGTH_SHORT).show()
                                        bugDescription = ""
                                        activeDialog = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                                modifier = Modifier.weight(1f).testTag("submit_bug_btn")
                            ) { Text("Submit") }
                        }
                    }
                }
            }
        }
    }
}
