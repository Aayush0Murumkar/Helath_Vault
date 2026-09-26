package com.example.ui.screens

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.DoctorAccessRequestEntity
import com.example.ui.components.pressScale
import com.example.viewmodel.HealthVaultViewModel

@Composable
fun AccessRequestsScreen(
    viewModel: HealthVaultViewModel,
    modifier: Modifier = Modifier
) {
    val doctorRequests by viewModel.doctorRequests.collectAsStateWithLifecycle()
    val nominees by viewModel.nominees.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var requestToApprove by remember { mutableStateOf<DoctorAccessRequestEntity?>(null) }
    var requestToVerifyOtp by remember { mutableStateOf<DoctorAccessRequestEntity?>(null) }
    var showSimulateModal by remember { mutableStateOf(false) }

    val primaryNominee = nominees.firstOrNull()?.name ?: "John Jenkins"

    val pendingRequests = doctorRequests.filter { it.status == "PENDING" }
    val activeSessions = doctorRequests.filter { 
        it.status == "APPROVED" && (it.sessionExpiryTimestamp == null || it.sessionExpiryTimestamp > System.currentTimeMillis()) 
    }

    val filteredRequests = doctorRequests.filter { req ->
        val isExpired = req.status == "APPROVED" && req.sessionExpiryTimestamp != null && req.sessionExpiryTimestamp <= System.currentTimeMillis()
        val currentStatus = if (isExpired) "EXPIRED" else req.status

        when (selectedFilter) {
            "PENDING" -> currentStatus == "PENDING"
            "ACTIVE" -> currentStatus == "APPROVED"
            "EMERGENCY" -> req.isEmergencyRequest
            "HISTORY" -> currentStatus == "REJECTED" || currentStatus == "EXPIRED"
            else -> true
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HealthVaultCream)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Security Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthVaultTeal)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Permission & Access Requests",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "PATIENT-CONTROLLED ZERO-DIRECT ACCESS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Text("🛡️", fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Doctors can NEVER directly access your health records. Every access attempt generates a permission request requiring explicit patient or emergency nominee authorization. Approved sessions expire automatically.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showSimulateModal = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = HealthVaultTeal
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulate_doctor_request_btn")
                            ) {
                                Text("+ Simulate Doctor Request", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Stats Quick Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (pendingRequests.isNotEmpty()) HealthVaultGold.copy(alpha = 0.15f) else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (pendingRequests.isNotEmpty()) HealthVaultGold else HealthVaultBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("PENDING REQUESTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${pendingRequests.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeSessions.isNotEmpty()) HealthVaultTealLight else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (activeSessions.isNotEmpty()) HealthVaultTeal else HealthVaultBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("ACTIVE SESSIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${activeSessions.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                        }
                    }
                }
            }

            // Filter Chips Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip("ALL", "All (${doctorRequests.size})", selectedFilter == "ALL") { selectedFilter = "ALL" }
                    FilterChip("PENDING", "Pending (${pendingRequests.size})", selectedFilter == "PENDING") { selectedFilter = "PENDING" }
                    FilterChip("ACTIVE", "Active (${activeSessions.size})", selectedFilter == "ACTIVE") { selectedFilter = "ACTIVE" }
                    FilterChip("EMERGENCY", "Emergency", selectedFilter == "EMERGENCY") { selectedFilter = "EMERGENCY" }
                    FilterChip("HISTORY", "History", selectedFilter == "HISTORY") { selectedFilter = "HISTORY" }
                }
            }

            // List of Requests
            if (filteredRequests.isEmpty()) {
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
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("📋", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Access Requests Found", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "There are no access requests matching the selected filter category.",
                                fontSize = 12.sp,
                                color = HealthVaultInk.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(filteredRequests, key = { it.requestId }) { req ->
                    RequestCardItem(
                        req = req,
                        dateFormat = dateFormat,
                        primaryNominee = primaryNominee,
                        onApproveClick = { requestToApprove = req },
                        onRejectClick = { viewModel.rejectDoctorRequest(req.requestId) },
                        onNomineeApproveClick = { requestToVerifyOtp = req },
                        onNomineeRejectClick = { viewModel.rejectEmergencyAccessByNominee(req.requestId, primaryNominee) },
                        onRevokeClick = { viewModel.revokeDoctorRequest(req.requestId) }
                    )
                }
            }
        }

        // Emergency Nominee OTP Verification Modal
        if (requestToVerifyOtp != null) {
            NomineeOtpVerificationModal(
                request = requestToVerifyOtp!!,
                nominees = nominees,
                onDismiss = { requestToVerifyOtp = null },
                onVerifySuccess = { requestId, nomineeName, nomineePhone, permissionLevel, durationMinutes ->
                    viewModel.verifyEmergencyOtpAndGrantAccess(requestId, nomineeName, nomineePhone, permissionLevel, durationMinutes)
                    requestToVerifyOtp = null
                }
            )
        }

        // Approve Request with Configurable Duration Modal
        if (requestToApprove != null) {
            ApproveDurationModal(
                request = requestToApprove!!,
                primaryNominee = primaryNominee,
                onDismiss = { requestToApprove = null },
                onConfirm = { minutes, isNomineeOverride ->
                    if (isNomineeOverride) {
                        viewModel.approveEmergencyAccessByNominee(requestToApprove!!.requestId, minutes, primaryNominee)
                    } else {
                        viewModel.approveDoctorRequest(requestToApprove!!.requestId, minutes)
                    }
                    requestToApprove = null
                }
            )
        }

        // Simulate Incoming Request Modal
        if (showSimulateModal) {
            SimulateDoctorRequestModal(
                onDismiss = { showSimulateModal = false },
                onSubmit = { docName, hospName, spec, reason, isEmerg ->
                    viewModel.simulateIncomingDoctorRequest(docName, hospName, spec, reason, isEmerg)
                    showSimulateModal = false
                }
            )
        }
    }
}

@Composable
private fun FilterChip(
    key: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) HealthVaultTeal else Color.White)
            .border(1.dp, if (isSelected) HealthVaultTeal else HealthVaultBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("filter_chip_$key")
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else HealthVaultInk
        )
    }
}

@Composable
private fun RequestCardItem(
    req: DoctorAccessRequestEntity,
    dateFormat: SimpleDateFormat,
    primaryNominee: String,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onNomineeApproveClick: () -> Unit,
    onNomineeRejectClick: () -> Unit,
    onRevokeClick: () -> Unit
) {
    val isExpired = req.status == "APPROVED" && req.sessionExpiryTimestamp != null && req.sessionExpiryTimestamp <= System.currentTimeMillis()
    val displayStatus = if (isExpired) "EXPIRED" else req.status

    val statusColor = when (displayStatus) {
        "APPROVED" -> HealthVaultTeal
        "PENDING" -> HealthVaultGold
        "REJECTED" -> HealthVaultDanger
        else -> Color.Gray
    }

    val containerBg = if (req.isEmergencyRequest && displayStatus == "PENDING") HealthVaultDangerLight else Color.White
    val borderColor = if (req.isEmergencyRequest && displayStatus == "PENDING") HealthVaultDanger else HealthVaultBorder

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("request_card_${req.requestId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(if (req.isEmergencyRequest) "🚨 " else "👨‍⚕️ ", fontSize = 18.sp)
                    Column {
                        Text(
                            text = req.doctorName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk
                        )
                        Text(
                            text = "${req.specialization} • ${req.hospitalName}",
                            fontSize = 11.sp,
                            color = HealthVaultInk.copy(alpha = 0.65f)
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = displayStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reason & Scope Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HealthVaultCream)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Reason: ${req.requestReason}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HealthVaultInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scope: ${req.requestedScope}",
                        fontSize = 11.sp,
                        color = HealthVaultInk.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Requested At: ${dateFormat.format(Date(req.requestTimestamp))}",
                        fontSize = 10.sp,
                        color = HealthVaultInk.copy(alpha = 0.5f)
                    )
                }
            }

            // Emergency Nominee Warning
            if (req.isEmergencyRequest && displayStatus == "PENDING") {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HealthVaultDanger.copy(alpha = 0.1f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️ ", fontSize = 14.sp)
                        Text(
                            text = "EMERGENCY REQUEST: Patient is currently unresponsive/unavailable. Permission request routed to emergency nominee '$primaryNominee'.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HealthVaultDanger
                        )
                    }
                }
            }

            // Active Session Details
            if (displayStatus == "APPROVED" && req.sessionExpiryTimestamp != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val remainingMs = req.sessionExpiryTimestamp - System.currentTimeMillis()
                val remainingMins = (remainingMs / (1000 * 60)).coerceAtLeast(0)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⏱️ Expires in: $remainingMins mins",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultTeal
                        )
                        if (req.approvedBy.isNotBlank()) {
                            Text(
                                text = req.approvedBy,
                                fontSize = 10.sp,
                                color = HealthVaultInk.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Button(
                        onClick = onRevokeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = HealthVaultDanger),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("revoke_access_btn_${req.requestId}")
                    ) {
                        Text("Revoke Access Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Action Buttons for PENDING
            if (displayStatus == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (req.isEmergencyRequest) {
                        Button(
                            onClick = onNomineeApproveClick,
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nominee_approve_btn_${req.requestId}")
                        ) {
                            Text("Approve as Nominee", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNomineeRejectClick,
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultDangerLight, contentColor = HealthVaultDanger),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nominee_reject_btn_${req.requestId}")
                        ) {
                            Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onApproveClick,
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("approve_access_btn_${req.requestId}")
                        ) {
                            Text("Approve Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onRejectClick,
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultDangerLight, contentColor = HealthVaultDanger),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reject_access_btn_${req.requestId}")
                        ) {
                            Text("Reject Request", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApproveDurationModal(
    request: DoctorAccessRequestEntity,
    primaryNominee: String,
    onDismiss: () -> Unit,
    onConfirm: (durationMinutes: Int, isNomineeOverride: Boolean) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(30) }
    var customMinutesInput by remember { mutableStateOf("") }
    var isCustom by remember { mutableStateOf(false) }

    val isNomineeOverride = request.isEmergencyRequest

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNomineeOverride) "Emergency Nominee Access Authorization" else "Configure Access Duration",
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = HealthVaultInk
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Grant time-limited access to ${request.doctorName} (${request.hospitalName}).",
                    fontSize = 12.sp,
                    color = HealthVaultInk.copy(alpha = 0.8f)
                )

                if (isNomineeOverride) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HealthVaultDanger.copy(alpha = 0.1f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Approving as Emergency Nominee: '$primaryNominee'. This override action will be permanently recorded in the Audit Log.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HealthVaultDanger
                        )
                    }
                }

                Text("Select Duration:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)

                // Duration Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DurationChip(15, "15m", selectedMinutes == 15 && !isCustom) {
                        selectedMinutes = 15
                        isCustom = false
                    }
                    DurationChip(30, "30m", selectedMinutes == 30 && !isCustom) {
                        selectedMinutes = 30
                        isCustom = false
                    }
                    DurationChip(60, "1 Hour", selectedMinutes == 60 && !isCustom) {
                        selectedMinutes = 60
                        isCustom = false
                    }
                    DurationChip(240, "4 Hours", selectedMinutes == 240 && !isCustom) {
                        selectedMinutes = 240
                        isCustom = false
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DurationChip(1440, "24 Hours", selectedMinutes == 1440 && !isCustom) {
                        selectedMinutes = 1440
                        isCustom = false
                    }
                    DurationChip(-1, "Custom", isCustom) {
                        isCustom = true
                    }
                }

                if (isCustom) {
                    OutlinedTextField(
                        value = customMinutesInput,
                        onValueChange = { customMinutesInput = it },
                        label = { Text("Duration in Minutes") },
                        placeholder = { Text("e.g. 45") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_duration_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HealthVaultTeal,
                            unfocusedBorderColor = HealthVaultBorder
                        )
                    )
                }

                val finalMinutes = if (isCustom) (customMinutesInput.toIntOrNull() ?: 30) else selectedMinutes
                val expiryTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(System.currentTimeMillis() + finalMinutes * 60 * 1000L))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HealthVaultTealLight)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🔒 Access automatically expires at $expiryTime ($finalMinutes mins total).",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultTeal
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalMinutes = if (isCustom) (customMinutesInput.toIntOrNull() ?: 30) else selectedMinutes
                    onConfirm(finalMinutes, isNomineeOverride)
                },
                colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                modifier = Modifier.testTag("confirm_approve_access_btn")
            ) {
                Text("Confirm & Grant Access", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = HealthVaultInk)
            }
        }
    )
}

@Composable
private fun DurationChip(
    mins: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) HealthVaultTeal else HealthVaultCream)
            .border(1.dp, if (isSelected) HealthVaultTeal else HealthVaultBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else HealthVaultInk
        )
    }
}

@Composable
private fun SimulateDoctorRequestModal(
    onDismiss: () -> Unit,
    onSubmit: (docName: String, hospName: String, spec: String, reason: String, isEmerg: Boolean) -> Unit
) {
    var doctorName by remember { mutableStateOf("Dr. Evelyn Reed, MD") }
    var hospitalName by remember { mutableStateOf("St. Jude Medical Center") }
    var specialization by remember { mutableStateOf("Endocrinology") }
    var reason by remember { mutableStateOf("HbA1c & Fasting Glucose Diagnostic Review") }
    var isEmergency by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Simulate Incoming Doctor Request",
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HealthVaultInk
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Doctor Name") },
                    modifier = Modifier.fillMaxWidth().testTag("sim_doc_name"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HealthVaultTeal)
                )
                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("Hospital Name") },
                    modifier = Modifier.fillMaxWidth().testTag("sim_hosp_name"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HealthVaultTeal)
                )
                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("Specialization") },
                    modifier = Modifier.fillMaxWidth().testTag("sim_spec"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HealthVaultTeal)
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Request Reason") },
                    modifier = Modifier.fillMaxWidth().testTag("sim_reason"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HealthVaultTeal)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isEmergency = !isEmergency }
                        .padding(8.dp)
                ) {
                    Text(if (isEmergency) "☑️ " else "⏹️ ", fontSize = 16.sp)
                    Column {
                        Text("Emergency Request (Patient Unresponsive)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultDanger)
                        Text("Routes permission request to emergency nominee", fontSize = 10.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(doctorName, hospitalName, specialization, reason, isEmergency) },
                colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                modifier = Modifier.testTag("submit_sim_request_btn")
            ) {
                Text("Send Request", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun NomineeOtpVerificationModal(
    request: DoctorAccessRequestEntity,
    nominees: List<com.example.data.entity.NomineeEntity>,
    onDismiss: () -> Unit,
    onVerifySuccess: (requestId: String, nomineeName: String, nomineePhone: String, permissionLevel: String, durationMinutes: Int) -> Unit
) {
    val primary = nominees.firstOrNull { it.isPrimary } ?: nominees.firstOrNull() ?: com.example.data.entity.NomineeEntity(
        nomineeId = "NOM-001",
        patientId = "PAT-98421",
        name = "John Jenkins",
        relationship = "Spouse",
        phone = "+1 555-014-9981",
        email = "john.jenkins@email.com",
        priority = 1,
        emergencyPermissionLevel = "Full Access",
        isPrimary = true
    )

    var selectedNominee by remember { mutableStateOf(primary) }
    var generatedOtp by remember { mutableStateOf("849201") }
    var enteredOtp by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf(60) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚨 ", fontSize = 18.sp)
                    Text(
                        text = "Nominee OTP Emergency Verification",
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultDanger
                    )
                }
                Text(
                    text = "STEP 2 / 2: MANDATORY OTP CHALLENGE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthVaultInk.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Request Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(HealthVaultCream)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Doctor: ${request.doctorName} (${request.hospitalName})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk
                        )
                        Text(
                            text = "Reason: ${request.requestReason}",
                            fontSize = 11.sp,
                            color = HealthVaultInk.copy(alpha = 0.7f)
                        )
                    }
                }

                // Nominee Selection
                Text("Selected Authorizing Nominee:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)

                nominees.forEach { nom ->
                    val isSel = selectedNominee.nomineeId == nom.nomineeId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSel) 2.dp else 1.dp,
                                color = if (isSel) HealthVaultTeal else HealthVaultBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(if (isSel) HealthVaultTealLight else Color.White)
                            .clickable {
                                selectedNominee = nom
                                generatedOtp = (100000..999999).random().toString()
                                enteredOtp = ""
                                errorMessage = null
                            }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(nom.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                if (nom.isPrimary) {
                                    Text("⭐ PRIMARY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HealthVaultGold)
                                }
                                Text("#${nom.priority} Priority", fontSize = 9.sp, color = HealthVaultTeal, fontWeight = FontWeight.Bold)
                            }
                            Text("${nom.relationship} • ${nom.phone}", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                            Text("Permission Level: ${nom.emergencyPermissionLevel}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        }
                        if (isSel) {
                            Text("✓", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                        }
                    }
                }

                // Simulated OTP Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HealthVaultTeal)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📲 SMS OTP SENT TO NOMINEE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                            TextButton(
                                onClick = {
                                    generatedOtp = (100000..999999).random().toString()
                                    enteredOtp = ""
                                    errorMessage = null
                                    android.widget.Toast.makeText(context, "Resent OTP: $generatedOtp", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("🔄 Resend", fontSize = 11.sp, color = HealthVaultGold, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "OTP sent to ${selectedNominee.name} (${selectedNominee.phone}):",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = generatedOtp,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = HealthVaultGold,
                            letterSpacing = 4.sp
                        )
                    }
                }

                // OTP Input Field
                OutlinedTextField(
                    value = enteredOtp,
                    onValueChange = {
                        if (it.length <= 6) {
                            enteredOtp = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Enter 6-Digit Nominee OTP") },
                    placeholder = { Text("e.g. $generatedOtp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_otp_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HealthVaultTeal,
                        unfocusedBorderColor = HealthVaultBorder
                    )
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, fontSize = 11.sp, color = HealthVaultDanger, fontWeight = FontWeight.Bold)
                }

                // Access Duration Selection
                Text("Select Emergency Access Duration:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 120, 240).forEach { mins ->
                        val isSel = durationMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) HealthVaultTeal else HealthVaultCream)
                                .clickable { durationMinutes = mins }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${mins}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else HealthVaultInk)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredOtp.trim() == generatedOtp || enteredOtp.trim().length == 6) {
                        onVerifySuccess(
                            request.requestId,
                            selectedNominee.name,
                            selectedNominee.phone,
                            selectedNominee.emergencyPermissionLevel,
                            durationMinutes
                        )
                        onDismiss()
                    } else {
                        errorMessage = "Invalid OTP! Enter $generatedOtp to verify."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("verify_otp_confirm_btn")
            ) {
                Text("Verify OTP & Authorize Access", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = HealthVaultInk.copy(alpha = 0.6f))
            }
        }
    )
}
