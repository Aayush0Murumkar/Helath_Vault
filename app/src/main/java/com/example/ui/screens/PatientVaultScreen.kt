package com.example.ui.screens

import com.example.ui.theme.*
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.ui.screens.assistant.AIAssistantScreen
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.EncryptedRecordEntity
import com.example.data.entity.NomineeEntity
import com.example.data.entity.MedicalEventEntity
import com.example.ui.components.EncryptedRecordCard
import com.example.ui.components.FingerprintLockScreen
import com.example.viewmodel.HealthVaultViewModel
import com.example.viewmodel.UserRole
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.components.AnimatedModalCard
import com.example.ui.components.EmptyNomineesState
import com.example.ui.components.EmptyRecordsState
import com.example.ui.components.EmergencyScreenSkeleton
import com.example.ui.components.HomeScreenSkeleton
import com.example.ui.components.RecordsScreenSkeleton
import com.example.ui.components.SettingsScreenSkeleton
import com.example.ui.components.StaggeredEntryCard
import com.example.ui.components.UploadScreenSkeleton
import com.example.ui.components.pressScale
import kotlinx.coroutines.delay

// Reference Colors
val HealthVaultTeal = Color(0xFF0F7A6B)
val HealthVaultTealMid = Color(0xFF1A9E8C)
val HealthVaultTealLight = Color(0xFFE6F5F3)
val HealthVaultInk = Color(0xFF0D1B1E)
val HealthVaultCream = Color(0xFFF7F5F0)
val HealthVaultDanger = Color(0xFFC0392B)
val HealthVaultDangerLight = Color(0xFFFDF0EF)
val HealthVaultGold = Color(0xFFC9A84C)
val HealthVaultBorder = Color(0x1A0D1B1E)

enum class ScreenTab { HOME, ASSISTANT, RECORDS, REQUESTS, UPLOAD, EMERGENCY, SETTINGS }

@Composable
fun PatientVaultScreen(
    viewModel: HealthVaultViewModel,
    modifier: Modifier = Modifier
) {
    val patientProfile by viewModel.patientProfile.collectAsStateWithLifecycle()
    val records by viewModel.encryptedRecords.collectAsStateWithLifecycle()
    val medicalEvents by viewModel.medicalEvents.collectAsStateWithLifecycle()
    val nominees by viewModel.nominees.collectAsStateWithLifecycle()

    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val selectedRecordForDetails by viewModel.selectedRecordForDetails.collectAsStateWithLifecycle()

    var activeScreen by remember { mutableStateOf(ScreenTab.HOME) }
    var showProfileDrawer by remember { mutableStateOf(false) }
    var showAddNomineeModal by remember { mutableStateOf(false) }
    var nomineeToEdit by remember { mutableStateOf<NomineeEntity?>(null) }

    if (selectedRecordForDetails != null) {
        RecordDetailsScreen(
            record = selectedRecordForDetails!!,
            viewModel = viewModel,
            onBackClick = { viewModel.closeRecordDetails() }
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(HealthVaultCream)
        ) {
        if (isAppLocked && isBiometricEnabled) {
            FingerprintLockScreen(viewModel = viewModel)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header (Matching HTML Spec)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Primary)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar
                            val profileInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer)
                                    .border(2.dp, PrimaryFixed, CircleShape)
                                    .pressScale(profileInteraction)
                                    .clickable(interactionSource = profileInteraction, indication = null) { showProfileDrawer = true }
                                    .testTag("header_profile_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👩🏻‍⚕️", fontSize = 20.sp)
                            }

                            Column {
                                Text(
                                    text = "Health Vault",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimary
                                )
                                Text(
                                    text = "SECURE MEDICAL RECORDS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = PrimaryFixed.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Settings Button
                            val settingsInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer)
                                    .pressScale(settingsInteraction)
                                    .clickable(interactionSource = settingsInteraction, indication = null) { activeScreen = ScreenTab.SETTINGS }
                                    .testTag("header_settings_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚙️", fontSize = 18.sp)
                            }

                            // Lock App Button
                            val lockInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer)
                                    .pressScale(lockInteraction)
                                    .clickable(interactionSource = lockInteraction, indication = null) { viewModel.lockApp() }
                                    .testTag("header_lock_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔒", fontSize = 18.sp)
                            }
                        }
                    }
                }

                // Main Content Area with AnimatedContent Navigation Transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = activeScreen,
                        transitionSpec = {
                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> direction * 35 },
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(300)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(300)
                            )).togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -direction * 35 },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(300)) + scaleOut(
                                    targetScale = 0.98f,
                                    animationSpec = tween(300)
                                )
                            )
                        },
                        label = "ScreenNavigationTransition"
                    ) { screen ->
                        var isTabLoading by remember(screen) { mutableStateOf(true) }

                        LaunchedEffect(screen) {
                            delay(300)
                            isTabLoading = false
                        }

                        if (isTabLoading) {
                            when (screen) {
                                ScreenTab.HOME -> HomeScreenSkeleton()
                                ScreenTab.ASSISTANT -> RecordsScreenSkeleton()
                                ScreenTab.RECORDS -> RecordsScreenSkeleton()
                                ScreenTab.REQUESTS -> RecordsScreenSkeleton()
                                ScreenTab.UPLOAD -> UploadScreenSkeleton()
                                ScreenTab.EMERGENCY -> EmergencyScreenSkeleton()
                                ScreenTab.SETTINGS -> SettingsScreenSkeleton()
                            }
                        } else {
                            when (screen) {
                                ScreenTab.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    patientProfile = patientProfile,
                                    records = records,
                                    medicalEvents = medicalEvents,
                                    nominees = nominees,
                                    onNavigateToAssistant = { activeScreen = ScreenTab.ASSISTANT },
                                    onNavigateToRequests = { activeScreen = ScreenTab.REQUESTS },
                                    onNavigateToTimeline = { activeScreen = ScreenTab.RECORDS },
                                    onNavigateToUpload = { activeScreen = ScreenTab.UPLOAD },
                                    onNavigateToEmergency = { activeScreen = ScreenTab.EMERGENCY },
                                    onNavigateToNotifications = { activeScreen = ScreenTab.SETTINGS }
                                )
                                ScreenTab.ASSISTANT -> AIAssistantScreen(
                                    viewModel = viewModel,
                                    onNavigateToRecordDetails = { record ->
                                        viewModel.openRecordDetails(record)
                                    }
                                )
                                ScreenTab.RECORDS -> RecordsScreen(
                                    viewModel = viewModel,
                                    records = records,
                                    medicalEvents = medicalEvents,
                                    onNavigateToUpload = { activeScreen = ScreenTab.UPLOAD }
                                )

                                ScreenTab.REQUESTS -> AccessRequestsScreen(
                                    viewModel = viewModel
                                )
                                ScreenTab.UPLOAD -> UploadScreen(
                                    viewModel = viewModel,
                                    onUploaded = { activeScreen = ScreenTab.RECORDS }
                                )
                                ScreenTab.EMERGENCY -> EmergencyScreen(
                                    viewModel = viewModel,
                                    patientProfile = patientProfile,
                                    nominees = nominees,
                                    onAddNomineeClick = { showAddNomineeModal = true },
                                    onEditNomineeClick = { nomineeToEdit = it }
                                )
                                ScreenTab.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }

                // Bottom Nav Bar with animated icon scale and container indicators
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(Color.White)
                        .border(width = 1.dp, color = HealthVaultBorder)
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavItem(
                            icon = "🏠",
                            label = "Home",
                            isActive = activeScreen == ScreenTab.HOME,
                            onClick = { activeScreen = ScreenTab.HOME },
                            modifier = Modifier.testTag("nav_home")
                        )
                        NavItem(
                            icon = "✨",
                            label = "AI Assistant",
                            isActive = activeScreen == ScreenTab.ASSISTANT,
                            onClick = { activeScreen = ScreenTab.ASSISTANT },
                            modifier = Modifier.testTag("nav_assistant")
                        )
                        NavItem(
                            icon = "📂",
                            label = "Records",
                            isActive = activeScreen == ScreenTab.RECORDS,
                            onClick = { activeScreen = ScreenTab.RECORDS },
                            modifier = Modifier.testTag("nav_records")
                        )
                        NavItem(
                            icon = "🔐",
                            label = "Requests",
                            isActive = activeScreen == ScreenTab.REQUESTS,
                            onClick = { activeScreen = ScreenTab.REQUESTS },
                            modifier = Modifier.testTag("nav_requests")
                        )
                        NavItem(
                            icon = "⬆️",
                            label = "Upload",
                            isActive = activeScreen == ScreenTab.UPLOAD,
                            onClick = { activeScreen = ScreenTab.UPLOAD },
                            modifier = Modifier.testTag("nav_upload")
                        )
                        NavItem(
                            icon = "🚑",
                            label = "SOS",
                            isActive = activeScreen == ScreenTab.EMERGENCY,
                            onClick = { activeScreen = ScreenTab.EMERGENCY },
                            modifier = Modifier.testTag("nav_emergency")
                        )
                        NavItem(
                            icon = "⚙️",
                            label = "Settings",
                            isActive = activeScreen == ScreenTab.SETTINGS,
                            onClick = { activeScreen = ScreenTab.SETTINGS },
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            }
        }

        // Profile / Drawer Modal Overlay
        AnimatedVisibility(
            visible = showProfileDrawer,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            ProfileDrawerModal(
                viewModel = viewModel,
                patientProfile = patientProfile,
                nominees = nominees,
                onDismiss = { showProfileDrawer = false },
                onOpenAddNominee = { showAddNomineeModal = true }
            )
        }

        // Add / Edit Nominee Modal
        if (showAddNomineeModal || nomineeToEdit != null) {
            AddNomineeModal(
                viewModel = viewModel,
                existingNominee = nomineeToEdit,
                onDismiss = {
                    showAddNomineeModal = false
                    nomineeToEdit = null
                }
            )
        }
    }
}
}

@Composable
private fun NavItem(
    icon: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navInteraction = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "NavIconScaleAnim"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isActive) HealthVaultTeal else HealthVaultInk.copy(alpha = 0.45f),
        animationSpec = tween(200),
        label = "NavLabelColorAnim"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isActive) HealthVaultTealLight else Color.Transparent,
        animationSpec = tween(200),
        label = "NavContainerColorAnim"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .pressScale(navInteraction)
            .clickable(interactionSource = navInteraction, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 20.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = labelColor
            )
        }
    }
}

// ----------------------------------------------------
// 1. HOME SCREEN
// ----------------------------------------------------
@Composable
private fun HomeScreen(
    viewModel: HealthVaultViewModel,
    patientProfile: com.example.data.entity.PatientProfileEntity?,
    records: List<EncryptedRecordEntity>,
    medicalEvents: List<MedicalEventEntity>,
    nominees: List<NomineeEntity>,
    onNavigateToAssistant: (() -> Unit)? = null,
    onNavigateToRequests: (() -> Unit)? = null,
    onNavigateToTimeline: (() -> Unit)? = null,
    onNavigateToUpload: (() -> Unit)? = null,
    onNavigateToEmergency: (() -> Unit)? = null,
    onNavigateToNotifications: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val userName = patientProfile?.name ?: "Sarah Jenkins"
    val bloodGroup = patientProfile?.bloodGroup ?: "O+"
    val age = patientProfile?.age ?: 34
    val gender = patientProfile?.gender ?: "Female"
    val height = patientProfile?.heightCm ?: "168 cm"
    val weight = patientProfile?.weightKg ?: "62 kg"
    val bmi = patientProfile?.bmi ?: "22.0 (Normal)"
    val isOrganDonor = patientProfile?.isOrganDonor ?: true
    val physician = patientProfile?.primaryPhysician ?: "Dr. Marcus Vance, MD"
    val chronic = patientProfile?.chronicDiseases ?: "Type 1 Diabetes, Mild Asthma"

    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Gradient Welcome Banner (Matching HTML File Layout)
        item {
            StaggeredEntryCard(index = 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(HealthVaultTeal, HealthVaultTealMid)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back,",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userName,
                                fontFamily = FontFamily.Serif,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$age Yrs • $gender • PAT-98421",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .clickable { onNavigateToNotifications?.invoke() }
                                        .padding(8.dp)
                                        .testTag("home_notif_bell"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box {
                                        Text("🔔", fontSize = 16.sp)
                                        if (unreadCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(HealthVaultDanger)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$bloodGroup Blood",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                            if (isOrganDonor) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(HealthVaultGold)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "🫀 Organ Donor",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HealthVaultInk
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2x2 Stat Grid (Total Records, Nominees, Blood Group, Chronic) - Matching HTML File
        item {
            StaggeredEntryCard(index = 1) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBox(
                            icon = "📂",
                            value = "${records.size}",
                            label = "Total Records",
                            valueColor = HealthVaultTeal,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            icon = "👥",
                            value = "${nominees.size}",
                            label = "Nominees",
                            valueColor = HealthVaultGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBox(
                            icon = "🩸",
                            value = bloodGroup,
                            label = "Blood Group",
                            valueColor = Color(0xFFC0392B),
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            icon = "🩺",
                            value = if (chronic.isNotBlank() && !chronic.contains("None", true)) chronic.substringBefore(",") else "None",
                            label = "Chronic",
                            valueColor = HealthVaultInk,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Gemini AI Assistant Quick Launch Card
        item {
            StaggeredEntryCard(index = 2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAssistant?.invoke() }
                        .testTag("home_ai_assistant_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthVaultCream),
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(HealthVaultTeal),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✨", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Gemini Health AI Assistant", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                    Text("Ask questions, summarize lab reports & search records", fontSize = 10.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(HealthVaultTeal)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Ask AI >", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, HealthVaultBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.processUserQuery("Generate my Medical Summary", com.example.viewmodel.ChatMessageType.MEDICAL_SUMMARY)
                                        onNavigateToAssistant?.invoke()
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏥 Medical Summary", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, HealthVaultBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.processUserQuery("Summarize my recent blood report", com.example.viewmodel.ChatMessageType.REPORT_SUMMARY)
                                        onNavigateToAssistant?.invoke()
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🩸 Blood Report", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid (Apple Health style)
        item {
            StaggeredEntryCard(index = 1) {
                QuickActionsCard(
                    onUpload = onNavigateToUpload,
                    onTimeline = onNavigateToTimeline,
                    onShareAccess = onNavigateToRequests,
                    onEmergency = onNavigateToEmergency
                )
            }
        }

        // Notification Center Preview Widget on HomeScreen
        item {
            StaggeredEntryCard(index = 1) {
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
                                Text("🔔 Notification Center", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (unreadCount > 0) HealthVaultDangerLight else HealthVaultTealLight)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (unreadCount > 0) "$unreadCount Unread" else "Up to date",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (unreadCount > 0) HealthVaultDanger else HealthVaultTeal
                                    )
                                }
                            }

                            TextButton(
                                onClick = { onNavigateToNotifications?.invoke() },
                                modifier = Modifier.testTag("home_view_all_notifs_btn")
                            ) {
                                Text("View All", fontSize = 12.sp, color = HealthVaultTeal, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val previewList = notifications.take(3)
                        if (previewList.isEmpty()) {
                            Text("No notifications", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.5f))
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                previewList.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (!item.isRead) HealthVaultCream else Color(0xFFFAFAFA))
                                            .border(1.dp, HealthVaultBorder, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (!item.isRead) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(CircleShape)
                                                                .background(HealthVaultDanger)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    Text(
                                                        text = item.title,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HealthVaultInk
                                                    )
                                                }
                                                Text(
                                                    text = item.message,
                                                    fontSize = 10.sp,
                                                    color = HealthVaultInk.copy(alpha = 0.7f),
                                                    maxLines = 1
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            TextButton(
                                                onClick = {
                                                    viewModel.markNotificationAsRead(item.id)
                                                    Toast.makeText(context, "Opened: ${item.title}", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.height(28.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                            ) {
                                                Text(item.actionText ?: "View", fontSize = 10.sp, color = HealthVaultTeal, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Apple Health Intelligent Health Insights Section
        item {
            StaggeredEntryCard(index = 2) {
                IntelligentHealthInsightsSection(
                    records = records,
                    medicalEvents = medicalEvents,
                    onNavigateToUpload = onNavigateToUpload,
                    onNavigateToTimeline = onNavigateToTimeline,
                    onNavigateToRequests = onNavigateToRequests
                )
            }
        }

        // Vitals Grid (Height, Weight, BMI, Records)
        item {
            StaggeredEntryCard(index = 3) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBox(
                            icon = "📐",
                            value = height,
                            label = "Height",
                            valueColor = HealthVaultTeal,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            icon = "⚖️",
                            value = weight,
                            label = "Weight",
                            valueColor = HealthVaultTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBox(
                            icon = "📊",
                            value = bmi,
                            label = "BMI Ratio",
                            valueColor = HealthVaultInk,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            icon = "📂",
                            value = "${records.size} Files",
                            label = "Medical Vault",
                            valueColor = HealthVaultTealMid,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Primary Physician & Insurance Card
        item {
            StaggeredEntryCard(index = 4) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PRIMARY CARE & INSURANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultInk.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Verified",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(HealthVaultTealLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🩺", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = physician,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultInk
                                )
                                Text(
                                    text = "Primary Care Physician",
                                    fontSize = 11.sp,
                                    color = HealthVaultInk.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(HealthVaultCream)
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = patientProfile?.insuranceProvider ?: "Blue Cross Blue Shield",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HealthVaultInk
                                    )
                                    Text(
                                        text = "Expires: ${patientProfile?.insuranceExpiryDate ?: "2027"}",
                                        fontSize = 11.sp,
                                        color = HealthVaultInk.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Policy: ${patientProfile?.insurancePolicyNumber ?: "POL-88392019"}",
                                    fontSize = 11.sp,
                                    color = HealthVaultInk.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Medical Timeline Card
        item {
            StaggeredEntryCard(index = 5) {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🩺 ", fontSize = 15.sp)
                                Text(
                                    text = "MEDICAL TIMELINE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultInk.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                            }
                            if (onNavigateToTimeline != null) {
                                TextButton(
                                    onClick = onNavigateToTimeline,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("View Full Timeline →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (medicalEvents.isEmpty()) {
                            Text(
                                text = "No medical timeline events recorded.",
                                fontSize = 13.sp,
                                color = HealthVaultInk.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                            val sortedEvents = medicalEvents.sortedByDescending { it.eventDate }
                            sortedEvents.take(4).forEachIndexed { index, event ->
                                val dateStr = dateFormat.format(Date(event.eventDate))
                                RealMedicalTimelineRow(
                                    event = event,
                                    dateStr = dateStr,
                                    isLast = index == sortedEvents.take(4).lastIndex
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// APPLE HEALTH STYLE QUICK ACTIONS & INTELLIGENT INSIGHTS
// ----------------------------------------------------

@Composable
private fun QuickActionsCard(
    onUpload: (() -> Unit)?,
    onTimeline: (() -> Unit)?,
    onShareAccess: (() -> Unit)?,
    onEmergency: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "QUICK ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HealthVaultInk.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = "📤",
                    label = "Upload Record",
                    onClick = { onUpload?.invoke() },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = "🩺",
                    label = "Timeline",
                    onClick = { onTimeline?.invoke() },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = "🔐",
                    label = "Share Access",
                    onClick = { onShareAccess?.invoke() },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = "🆘",
                    label = "Emergency",
                    onClick = { onEmergency?.invoke() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pressScale(interactionSource)
            .clip(RoundedCornerShape(12.dp))
            .background(HealthVaultCream)
            .border(1.dp, HealthVaultBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = HealthVaultInk,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IntelligentHealthInsightsSection(
    records: List<EncryptedRecordEntity>,
    medicalEvents: List<MedicalEventEntity>,
    onNavigateToUpload: (() -> Unit)?,
    onNavigateToTimeline: (() -> Unit)?,
    onNavigateToRequests: (() -> Unit)?
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val lastUploadedRecord = remember(records) {
        records.maxByOrNull { it.uploadTimestamp }
    }

    val lastHospitalVisit = remember(medicalEvents) {
        medicalEvents.filter { it.hospitalName.isNotBlank() || it.category.contains("Visit", true) }
            .maxByOrNull { it.eventDate }
    }

    val favoriteRecords = remember(records) {
        records.filter { it.isFavorite }
    }

    val totalStorageBytes = remember(records) {
        if (records.isEmpty()) 48_200_000L else (records.size * 4_800_000L)
    }

    // Medicine Reminders State
    var med1Checked by remember { mutableStateOf(true) }
    var med2Checked by remember { mutableStateOf(false) }
    var med3Checked by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header (Apple Health Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🧠 ", fontSize = 16.sp)
                Text(
                    text = "INTELLIGENT HEALTH INSIGHTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthVaultInk.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(HealthVaultTealLight)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Apple Health Sync • Live 💚",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthVaultTeal
                )
            }
        }

        // 1. Health Score Placeholder & AI Summary Placeholder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Health Score Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HEALTH SCORE & SYNC INDEX",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "96",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal
                            )
                            Text(
                                text = "/ 100",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HealthVaultInk.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(HealthVaultCream)
                            .border(1.dp, HealthVaultBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Optimal Balance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                            Text("Updated 2 hrs ago", fontSize = 10.sp, color = HealthVaultInk.copy(0.5f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Score Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(HealthVaultCream)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.96f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(HealthVaultTeal, HealthVaultTealMid, HealthVaultGold)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // AI Summary Placeholder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(HealthVaultCream, Color.White)
                            )
                        )
                        .border(1.dp, HealthVaultTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✨", fontSize = 16.sp)
                            Text(
                                text = "AI Health Copilot Summary",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Vault analysis complete across ${records.size} records & ${medicalEvents.size} timeline events. Lipid profile shows cholesterol reduced 8% since Q1. Blood pressure baseline stable at 118/76 mmHg. No active drug interactions flagged.",
                            fontSize = 12.sp,
                            color = HealthVaultInk.copy(alpha = 0.85f),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // 2. Last Uploaded Report Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📄 ", fontSize = 15.sp)
                        Text(
                            text = "LAST UPLOADED REPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                    if (onNavigateToUpload != null) {
                        TextButton(onClick = onNavigateToUpload, contentPadding = PaddingValues(0.dp)) {
                            Text("Upload New +", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (lastUploadedRecord != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(HealthVaultCream)
                            .border(1.dp, HealthVaultBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lastUploadedRecord.getDisplayTitle(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultInk,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${lastUploadedRecord.fileCategory} • ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = HealthVaultTeal
                                    )
                                    Text(
                                        text = dateFormat.format(Date(lastUploadedRecord.uploadTimestamp)),
                                        fontSize = 11.sp,
                                        color = HealthVaultInk.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HealthVaultTealLight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("AES-256 🔒", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Comprehensive Lipid & Metabolic Panel.pdf (Uploaded Aug 02, 2026 • AES-256 Encrypted)",
                        fontSize = 12.sp,
                        color = HealthVaultInk.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 3. Last Hospital Visit Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏥 ", fontSize = 15.sp)
                        Text(
                            text = "LAST HOSPITAL VISIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = if (lastHospitalVisit != null) dateFormat.format(Date(lastHospitalVisit.eventDate)) else "Jul 18, 2026",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultTeal
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val visitHospital = lastHospitalVisit?.hospitalName ?: "Metropolitan General Hospital"
                val visitDoctor = lastHospitalVisit?.doctorName ?: "Dr. Marcus Vance, MD"
                val visitPreview = lastHospitalVisit?.quickPreview ?: "Annual Cardiology Assessment & Routine ECG Review. All parameters within normal limits."

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HealthVaultCream)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = visitHospital,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk
                        )
                        Text(
                            text = "Attending: $visitDoctor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = HealthVaultTeal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = visitPreview,
                            fontSize = 12.sp,
                            color = HealthVaultInk.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. Upcoming Appointment Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📅 ", fontSize = 15.sp)
                        Text(
                            text = "UPCOMING APPOINTMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("In 13 Days ⏳", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HealthVaultCream)
                        .border(1.dp, HealthVaultBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Cardiology Routine Review",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultInk
                            )
                            Text(
                                text = "Dr. Marcus Vance, MD • St. Jude Heart Center",
                                fontSize = 11.sp,
                                color = HealthVaultInk.copy(0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🗓️ Aug 15, 2026 @ 10:30 AM",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal
                            )
                        }
                    }
                }
            }
        }

        // 5. Medicine Reminder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💊 ", fontSize = 15.sp)
                        Text(
                            text = "MEDICINE REMINDER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Daily Schedule",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultTeal
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedicineReminderRow(
                        name = "Metformin 500mg",
                        dosage = "1 tablet with Breakfast (08:00 AM)",
                        isChecked = med1Checked,
                        onCheckedChange = { med1Checked = it }
                    )
                    MedicineReminderRow(
                        name = "Atorvastatin 10mg",
                        dosage = "1 tablet at Bedtime (09:00 PM)",
                        isChecked = med2Checked,
                        onCheckedChange = { med2Checked = it }
                    )
                    MedicineReminderRow(
                        name = "Albuterol Inhaler",
                        dosage = "2 puffs as needed before workout",
                        isChecked = med3Checked,
                        onCheckedChange = { med3Checked = it }
                    )
                }
            }
        }

        // 6. Storage Used Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                val mbUsed = (totalStorageBytes / (1024.0 * 1024.0)).coerceAtLeast(42.8)
                val totalGb = 10.0
                val percentage = ((mbUsed / (totalGb * 1024.0)) * 100).coerceAtLeast(0.48)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💾 ", fontSize = 15.sp)
                        Text(
                            text = "STORAGE USED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", mbUsed)} MB / 10 GB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultTeal
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(HealthVaultCream)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((percentage / 100f).toFloat().coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(4.dp))
                            .background(HealthVaultTeal)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${records.size} Encrypted Files Stored",
                        fontSize = 11.sp,
                        color = HealthVaultInk.copy(0.6f)
                    )
                    Text(
                        text = "AES-256 Multi-Cloud Sync 🔒",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultTeal
                    )
                }
            }
        }

        // 7. Favorite Records Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐ ", fontSize = 15.sp)
                        Text(
                            text = "FAVORITE RECORDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "${favoriteRecords.size} Starred",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultTeal
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (favoriteRecords.isEmpty()) {
                    Text(
                        text = "Star important medical documents to access them instantly here.",
                        fontSize = 12.sp,
                        color = HealthVaultInk.copy(0.5f)
                    )
                } else {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favoriteRecords.size) { idx ->
                            val fav = favoriteRecords[idx]
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HealthVaultCream)
                                    .border(1.dp, HealthVaultBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⭐ ", fontSize = 12.sp)
                                    Column {
                                        Text(
                                            text = fav.getDisplayTitle(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = HealthVaultInk
                                        )
                                        Text(
                                            text = fav.fileCategory,
                                            fontSize = 10.sp,
                                            color = HealthVaultTeal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Recently Viewed Records Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👁️ ", fontSize = 15.sp)
                        Text(
                            text = "RECENTLY VIEWED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                    Text("Vault History", fontSize = 10.sp, color = HealthVaultInk.copy(0.5f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                val recentList = records.take(3)
                if (recentList.isEmpty()) {
                    Text("No recently viewed documents.", fontSize = 12.sp, color = HealthVaultInk.copy(0.5f))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        recentList.forEachIndexed { index, rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HealthVaultCream)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📂 ", fontSize = 12.sp)
                                    Text(
                                        text = rec.getDisplayTitle(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HealthVaultInk
                                    )
                                }
                                Text(
                                    text = if (index == 0) "Just now" else if (index == 1) "2 hrs ago" else "Yesterday",
                                    fontSize = 10.sp,
                                    color = HealthVaultInk.copy(0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 9. Recent Vault Activity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡ ", fontSize = 15.sp)
                        Text(
                            text = "RECENT ACTIVITY LOG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                    Text("Audited 🛡️", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActivityItemRow(
                        icon = "🔒",
                        action = "Document Encrypted & Saved",
                        time = "10 mins ago"
                    )
                    ActivityItemRow(
                        icon = "🩺",
                        action = "Medical Timeline Event Recorded",
                        time = "1 hour ago"
                    )
                    ActivityItemRow(
                        icon = "🛡️",
                        action = "Biometric Lock Authentication Verified",
                        time = "2 hours ago"
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicineReminderRow(
    name: String,
    dosage: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isChecked) HealthVaultTealLight.copy(0.5f) else HealthVaultCream)
            .border(
                1.dp,
                if (isChecked) HealthVaultTeal.copy(alpha = 0.4f) else HealthVaultBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onCheckedChange(!isChecked) }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isChecked) HealthVaultTeal else HealthVaultInk
            )
            Text(
                text = dosage,
                fontSize = 11.sp,
                color = HealthVaultInk.copy(alpha = 0.7f)
            )
        }

        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = HealthVaultTeal,
                uncheckedColor = HealthVaultInk.copy(0.4f)
            )
        )
    }
}

@Composable
private fun ActivityItemRow(
    icon: String,
    action: String,
    time: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = action,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = HealthVaultInk
            )
        }
        Text(
            text = time,
            fontSize = 10.sp,
            color = HealthVaultInk.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun StatBox(
    icon: String,
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = modifier.pressScale(interactionSource),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HealthVaultCream),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = HealthVaultInk.copy(alpha = 0.6f)
            )
        }
    }
}

fun getMedicalCategoryIcon(category: String): String = when (category) {
    "Hospital Visits", "Hospital Visit" -> "🏥"
    "Diagnoses", "Diagnosis" -> "🩺"
    "Surgeries", "Surgery" -> "✂️"
    "Vaccinations", "Vaccination" -> "💉"
    "Lab Reports", "Lab Report" -> "🧪"
    "Scans", "Scan" -> "🩻"
    "Medicine Changes", "Medicine Change" -> "💊"
    "Discharge Summaries", "Discharge Summary" -> "📋"
    "Appointments", "Appointment" -> "📅"
    else -> "🩺"
}

fun getMedicalCategoryColor(category: String): Color = when (category) {
    "Hospital Visits", "Hospital Visit" -> Color(0xFFD97706)
    "Diagnoses", "Diagnosis" -> Color(0xFF2563EB)
    "Surgeries", "Surgery" -> Color(0xFFDC2626)
    "Vaccinations", "Vaccination" -> Color(0xFF059669)
    "Lab Reports", "Lab Report" -> Color(0xFF7C3AED)
    "Scans", "Scan" -> Color(0xFF0284C7)
    "Medicine Changes", "Medicine Change" -> Color(0xFFEA580C)
    "Discharge Summaries", "Discharge Summary" -> Color(0xFF4F46E5)
    "Appointments", "Appointment" -> HealthVaultTeal
    else -> HealthVaultTeal
}

@Composable
private fun RealMedicalTimelineRow(
    event: MedicalEventEntity,
    dateStr: String,
    isLast: Boolean
) {
    val categoryIcon = getMedicalCategoryIcon(event.category)
    val categoryColor = getMedicalCategoryColor(event.category)

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.5.dp, categoryColor, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(84.dp)
                        .background(categoryColor.copy(alpha = 0.3f))
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthVaultInk.copy(alpha = 0.6f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(categoryColor.copy(alpha = 0.12f))
                        .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$categoryIcon ${event.category}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = event.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HealthVaultInk
            )

            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏛️ ${event.hospitalName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = HealthVaultTeal
                )
                if (event.doctorName.isNotBlank()) {
                    Text(
                        text = " • 🩺 ${event.doctorName}",
                        fontSize = 11.sp,
                        color = HealthVaultInk.copy(alpha = 0.7f)
                    )
                }
            }

            if (event.quickPreview.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HealthVaultCream)
                        .border(1.dp, HealthVaultBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = event.quickPreview,
                        fontSize = 11.sp,
                        color = HealthVaultInk.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun MedicalTimelineSection(
    viewModel: HealthVaultViewModel,
    events: List<MedicalEventEntity>,
    records: List<EncryptedRecordEntity>
) {
    var viewMode by remember { mutableStateOf("CHRONOLOGICAL") } // CHRONOLOGICAL, MONTHLY, YEARLY
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrderAscending by remember { mutableStateOf(false) } // false = Newest First
    var selectedEventForDetails by remember { mutableStateOf<MedicalEventEntity?>(null) }
    var showAddEventModal by remember { mutableStateOf(false) }

    val categories = listOf(
        "All",
        "Hospital Visits",
        "Diagnoses",
        "Surgeries",
        "Vaccinations",
        "Lab Reports",
        "Scans",
        "Medicine Changes",
        "Discharge Summaries",
        "Appointments"
    )

    val filteredEvents = remember(events, selectedCategory, searchQuery, sortOrderAscending) {
        events.filter { event ->
            val matchesCategory = (selectedCategory == "All") || event.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.hospitalName.contains(searchQuery, ignoreCase = true) ||
                    event.doctorName.contains(searchQuery, ignoreCase = true) ||
                    event.category.contains(searchQuery, ignoreCase = true) ||
                    event.quickPreview.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }.let { list ->
            if (sortOrderAscending) list.sortedBy { it.eventDate }
            else list.sortedByDescending { it.eventDate }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Controls Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Title & Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MEDICAL TIMELINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Comprehensive Health History",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk
                        )
                    }

                    Button(
                        onClick = { showAddEventModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("＋ Add Event", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // View Mode Toggle (Monthly | Yearly | Chronological)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(HealthVaultCream)
                        .border(1.dp, HealthVaultBorder, RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val modes = listOf(
                        "CHRONOLOGICAL" to "⏱️ Chronological",
                        "MONTHLY" to "📅 Monthly",
                        "YEARLY" to "📆 Yearly"
                    )
                    modes.forEach { (modeKey, label) ->
                        val isSelected = viewMode == modeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) HealthVaultTeal else Color.Transparent)
                                .clickable { viewMode = modeKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else HealthVaultInk.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search & Sort Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search timeline events...", fontSize = 11.sp, color = HealthVaultInk.copy(0.5f)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("timeline_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HealthVaultCream,
                            unfocusedContainerColor = HealthVaultCream,
                            focusedBorderColor = HealthVaultTeal,
                            unfocusedBorderColor = HealthVaultBorder
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(HealthVaultCream)
                            .border(1.dp, HealthVaultBorder, RoundedCornerShape(8.dp))
                            .clickable { sortOrderAscending = !sortOrderAscending }
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (sortOrderAscending) "Oldest ⬆️" else "Newest ⬇️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultTeal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.size) { idx ->
                        val cat = categories[idx]
                        val isSel = selectedCategory == cat
                        val icon = getMedicalCategoryIcon(cat)
                        val chipText = if (cat == "All") "All Events (${events.size})" else "$icon $cat"

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSel) HealthVaultTeal else HealthVaultCream)
                                .border(1.dp, if (isSel) HealthVaultTeal else HealthVaultBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipText,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else HealthVaultInk.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Render Events based on selected viewMode
        if (filteredEvents.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No medical timeline events found.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultInk
                    )
                    Text(
                        text = "Try clearing filters or search query.",
                        fontSize = 12.sp,
                        color = HealthVaultInk.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            when (viewMode) {
                "CHRONOLOGICAL" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                            filteredEvents.forEachIndexed { index, event ->
                                val dateStr = dateFormat.format(Date(event.eventDate))
                                Box(modifier = Modifier.clickable { selectedEventForDetails = event }) {
                                    RealMedicalTimelineRow(
                                        event = event,
                                        dateStr = dateStr,
                                        isLast = index == filteredEvents.lastIndex
                                    )
                                }
                            }
                        }
                    }
                }

                "MONTHLY" -> {
                    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
                    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

                    val monthGroups = remember(filteredEvents) {
                        filteredEvents.groupBy { monthFormat.format(Date(it.eventDate)) }
                    }

                    monthGroups.forEach { (monthName, monthEvents) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HealthVaultCream)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📅 ", fontSize = 14.sp)
                                        Text(
                                            text = monthName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = HealthVaultInk
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(HealthVaultTeal)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${monthEvents.size} events",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                monthEvents.forEachIndexed { idx, event ->
                                    val dateStr = dateFormat.format(Date(event.eventDate))
                                    Box(modifier = Modifier.clickable { selectedEventForDetails = event }) {
                                        RealMedicalTimelineRow(
                                            event = event,
                                            dateStr = dateStr,
                                            isLast = idx == monthEvents.lastIndex
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "YEARLY" -> {
                    val yearFormat = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }
                    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

                    val yearGroups = remember(filteredEvents) {
                        filteredEvents.groupBy { yearFormat.format(Date(it.eventDate)) }
                    }

                    yearGroups.forEach { (year, yearEvents) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(HealthVaultTeal, HealthVaultTealMid)
                                            )
                                        )
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "YEAR $year",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${yearEvents.size} Medical Timeline Records",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }

                                    val categoriesCount = yearEvents.map { it.category }.distinct().size
                                    Text(
                                        text = "$categoriesCount Categories",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HealthVaultGold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                yearEvents.forEachIndexed { idx, event ->
                                    val dateStr = dateFormat.format(Date(event.eventDate))
                                    Box(modifier = Modifier.clickable { selectedEventForDetails = event }) {
                                        RealMedicalTimelineRow(
                                            event = event,
                                            dateStr = dateStr,
                                            isLast = idx == yearEvents.lastIndex
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEventModal) {
        AddMedicalEventModal(
            onDismiss = { showAddEventModal = false },
            onAddEvent = { title, category, dateMs, hospital, doctor, preview, vitals ->
                viewModel.addMedicalEvent(title, category, dateMs, hospital, doctor, preview, vitals)
                showAddEventModal = false
            }
        )
    }

    if (selectedEventForDetails != null) {
        MedicalEventDetailsModal(
            event = selectedEventForDetails!!,
            records = records,
            onDismiss = { selectedEventForDetails = null },
            onDelete = {
                viewModel.deleteMedicalEvent(selectedEventForDetails!!.eventId)
                selectedEventForDetails = null
            }
        )
    }
}

@Composable
private fun AddMedicalEventModal(
    onDismiss: () -> Unit,
    onAddEvent: (
        title: String,
        category: String,
        eventDateMs: Long,
        hospitalName: String,
        doctorName: String,
        quickPreview: String,
        vitalsOrNotes: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Hospital Visits") }
    var hospitalName by remember { mutableStateOf("Metropolitan General Hospital") }
    var doctorName by remember { mutableStateOf("Dr. Marcus Vance, MD") }
    var quickPreview by remember { mutableStateOf("") }
    var vitals by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Hospital Visits",
        "Diagnoses",
        "Surgeries",
        "Vaccinations",
        "Lab Reports",
        "Scans",
        "Medicine Changes",
        "Discharge Summaries",
        "Appointments"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Medical Event",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultInk
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text("Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk.copy(0.6f))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = "${getMedicalCategoryIcon(selectedCategory)} $selectedCategory",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryDropdownExpanded = true },
                        trailingIcon = { Text("▾") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HealthVaultCream,
                            unfocusedContainerColor = HealthVaultCream
                        )
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${getMedicalCategoryIcon(cat)} $cat", fontSize = 13.sp) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("Event Title", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk.copy(0.6f))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g., Annual Cardiology Review", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream
                    )
                )

                Text("Hospital / Facility Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk.copy(0.6f))
                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    placeholder = { Text("e.g., Metropolitan General Hospital", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream
                    )
                )

                Text("Attending Doctor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk.copy(0.6f))
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    placeholder = { Text("e.g., Dr. Marcus Vance, MD", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream
                    )
                )

                Text("Quick Preview / Summary Findings", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk.copy(0.6f))
                OutlinedTextField(
                    value = quickPreview,
                    onValueChange = { quickPreview = it },
                    placeholder = { Text("Enter clinical notes, test findings, or doctor recommendations...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream
                    )
                )

                Text("Vitals / Specific Measurements (Optional)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk.copy(0.6f))
                OutlinedTextField(
                    value = vitals,
                    onValueChange = { vitals = it },
                    placeholder = { Text("e.g., BP: 120/80 mmHg | Fasting Glucose: 110 mg/dL", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddEvent(
                                title,
                                selectedCategory,
                                System.currentTimeMillis(),
                                hospitalName,
                                doctorName,
                                quickPreview.ifBlank { "Recorded $selectedCategory at $hospitalName." },
                                vitals.ifBlank { null }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Medical Event", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun MedicalEventDetailsModal(
    event: MedicalEventEntity,
    records: List<EncryptedRecordEntity>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()) }
    val categoryIcon = getMedicalCategoryIcon(event.category)
    val categoryColor = getMedicalCategoryColor(event.category)

    val linkedRecord = remember(event, records) {
        event.linkedRecordId?.let { linkId -> records.firstOrNull { it.fileId == linkId } }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$categoryIcon ${event.category}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = event.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthVaultInk
                )

                Text(
                    text = "🗓️ " + dateFormat.format(Date(event.eventDate)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthVaultInk.copy(alpha = 0.6f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(HealthVaultCream)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text("🏛️ Hospital: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                            Text(event.hospitalName, fontSize = 12.sp, color = HealthVaultInk)
                        }
                        if (event.doctorName.isNotBlank()) {
                            Row {
                                Text("🩺 Attending: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                Text(event.doctorName, fontSize = 12.sp, color = HealthVaultInk)
                            }
                        }
                        Row {
                            Text("⚡ Status: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                            Text(event.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                        }
                    }
                }

                Text(
                    text = "Quick Preview & Clinical Findings",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthVaultInk.copy(alpha = 0.7f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, HealthVaultBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = event.quickPreview,
                        fontSize = 13.sp,
                        color = HealthVaultInk
                    )
                }

                if (!event.vitalsOrNotes.isNullOrBlank()) {
                    Text(
                        text = "Vitals & Measurements",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultInk.copy(alpha = 0.7f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HealthVaultTealLight.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = event.vitalsOrNotes,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HealthVaultTeal
                        )
                    }
                }

                if (linkedRecord != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HealthVaultCream)
                            .border(1.dp, HealthVaultBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("📁 Linked Document", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk.copy(0.6f))
                                Text(linkedRecord.getDisplayTitle(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                            }
                            Text("AES-256 Encrypted 🔒", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDelete) {
                        Text("Delete Event 🗑️", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. RECORDS SCREEN (STRUCTURED DOCUMENT MANAGEMENT)
// ----------------------------------------------------
val ALL_CATEGORIES_LIST = listOf(
    "Prescriptions",
    "Lab Reports",
    "Blood Tests",
    "X-Ray",
    "MRI",
    "CT Scan",
    "ECG",
    "Ultrasound",
    "Vaccinations",
    "Surgery Reports",
    "Discharge Summaries",
    "Insurance Documents",
    "Bills",
    "Other"
)

fun getCategoryEmoji(cat: String): String = when (cat) {
    "Prescriptions" -> "💊"
    "Lab Reports" -> "🧪"
    "Blood Tests" -> "🩸"
    "X-Ray" -> "🩻"
    "MRI" -> "🧲"
    "CT Scan" -> "🖥️"
    "ECG" -> "📈"
    "Ultrasound" -> "🔊"
    "Vaccinations" -> "💉"
    "Surgery Reports" -> "🩺"
    "Discharge Summaries" -> "🏥"
    "Insurance Documents" -> "🛡️"
    "Bills" -> "🧾"
    else -> "📑"
}

@Composable
private fun RecordsScreen(
    viewModel: HealthVaultViewModel,
    records: List<EncryptedRecordEntity>,
    medicalEvents: List<MedicalEventEntity>,
    onNavigateToUpload: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf("TIMELINE") } // TIMELINE, CATEGORIES, ALL, FAVORITES, ARCHIVE, TRASH
    var searchQuery by remember { mutableStateOf("") }
    var selectedSortOption by remember { mutableStateOf("Newest First") }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    // Category expansion map
    var expandedCategories by remember { mutableStateOf(mapOf<String, Boolean>()) }

    val context = LocalContext.current

    val activeRecords = records.filter { !it.isTrashed && !it.isArchived }
    val favoriteRecords = records.filter { it.isFavorite && !it.isTrashed }
    val archivedRecords = records.filter { it.isArchived && !it.isTrashed }
    val trashedRecords = records.filter { it.isTrashed }

    val sourceList = when (activeSubTab) {
        "FAVORITES" -> favoriteRecords
        "ARCHIVE" -> archivedRecords
        "TRASH" -> trashedRecords
        else -> activeRecords
    }

    val searchDateFormat1 = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val searchDateFormat2 = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Instant Search across metadata (filename, OCR text, doctor, medicine, diagnosis, hospital, tags, notes, document dates)
    val searchedRecords = sourceList.filter { record ->
        if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.lowercase()
            val examDate1 = searchDateFormat1.format(Date(record.examinationDate)).lowercase()
            val examDate2 = searchDateFormat2.format(Date(record.examinationDate)).lowercase()
            val uploadDate = searchDateFormat1.format(Date(record.uploadTimestamp)).lowercase()

            record.getDisplayTitle().lowercase().contains(q) ||
                    record.originalFileName.lowercase().contains(q) ||
                    record.hospitalName.lowercase().contains(q) ||
                    record.doctorName.lowercase().contains(q) ||
                    record.fileCategory.lowercase().contains(q) ||
                    record.documentType.lowercase().contains(q) ||
                    record.tags.lowercase().contains(q) ||
                    record.notes.lowercase().contains(q) ||
                    record.ocrText.lowercase().contains(q) ||
                    record.aiSummary.lowercase().contains(q) ||
                    record.diagnosis.lowercase().contains(q) ||
                    record.medicines.lowercase().contains(q) ||
                    examDate1.contains(q) ||
                    examDate2.contains(q) ||
                    uploadDate.contains(q)
        }
    }

    // Sorting
    val sortedRecords = when (selectedSortOption) {
        "Oldest First" -> searchedRecords.sortedBy { it.examinationDate.takeIf { d -> d > 0 } ?: it.uploadTimestamp }
        "Title (A-Z)" -> searchedRecords.sortedBy { it.getDisplayTitle() }
        "Hospital" -> searchedRecords.sortedBy { it.hospitalName }
        "Category" -> searchedRecords.sortedBy { it.fileCategory }
        "Favorites First" -> searchedRecords.sortedByDescending { it.isFavorite }
        else -> searchedRecords.sortedByDescending { it.examinationDate.takeIf { d -> d > 0 } ?: it.uploadTimestamp }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header & Sub-Nav Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MEDICAL DOCUMENTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )

                        // Sort Dropdown Button
                        Box {
                            val sortInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HealthVaultCream)
                                    .border(1.dp, HealthVaultBorder, RoundedCornerShape(8.dp))
                                    .pressScale(sortInteraction)
                                    .clickable(interactionSource = sortInteraction, indication = null) { sortMenuExpanded = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Sort: $selectedSortOption ▾",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HealthVaultTeal
                                )
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                listOf("Newest First", "Oldest First", "Title (A-Z)", "Hospital", "Category", "Favorites First").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, fontSize = 12.sp) },
                                        onClick = {
                                            selectedSortOption = option
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sub-Nav View Chips: Categories, All Files, Favorites, Archive, Trash
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf(
                            "TIMELINE" to "Timeline 🩺",
                            "CATEGORIES" to "Categories 📁",
                            "ALL" to "All Files 📄",
                            "FAVORITES" to "Favorites ⭐",
                            "ARCHIVE" to "Archive 📦",
                            "TRASH" to "Trash 🗑️ (${trashedRecords.size})"
                        )

                        tabs.forEach { (key, label) ->
                            val isSelected = activeSubTab == key
                            val tabInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) HealthVaultTeal else HealthVaultCream)
                                    .border(1.dp, if (isSelected) HealthVaultTeal else HealthVaultBorder, RoundedCornerShape(8.dp))
                                    .pressScale(tabInteraction)
                                    .clickable(interactionSource = tabInteraction, indication = null) { activeSubTab = key }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else HealthVaultInk.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Instant Search TextField
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("🔍 Search by title, hospital, doctor, tags, OCR...", color = HealthVaultInk.copy(alpha = 0.5f), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_records_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HealthVaultCream,
                            unfocusedContainerColor = HealthVaultCream,
                            focusedBorderColor = HealthVaultTeal,
                            unfocusedBorderColor = HealthVaultBorder,
                            focusedTextColor = HealthVaultInk,
                            unfocusedTextColor = HealthVaultInk
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Trash Header Notice & Empty Trash Button
                    if (activeSubTab == "TRASH") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HealthVaultDangerLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultDanger)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Trash Retention Policy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultDanger)
                                    Text("Records in trash are permanently purged after 30 days.", fontSize = 10.sp, color = HealthVaultInk.copy(alpha = 0.7f))
                                }

                                if (trashedRecords.isNotEmpty()) {
                                    Button(
                                        onClick = { showEmptyTrashDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = HealthVaultDanger),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Empty Trash 🗑️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Main Document Content based on SubTab
        if (activeSubTab == "TIMELINE") {
            item {
                MedicalTimelineSection(
                    viewModel = viewModel,
                    events = medicalEvents,
                    records = records
                )
            }
        } else if (activeSubTab == "CATEGORIES") {
            // Render 14 Categories Accordion List
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

                    ALL_CATEGORIES_LIST.forEach { catName ->
                        val catRecords = sortedRecords.filter { r ->
                            r.fileCategory.equals(catName, ignoreCase = true) ||
                                    (catName == "Other" && ALL_CATEGORIES_LIST.none { c -> c.equals(r.fileCategory, ignoreCase = true) })
                        }

                        val count = catRecords.size
                        val latestTimestamp = catRecords.maxOfOrNull { it.examinationDate.takeIf { d -> d > 0 } ?: it.uploadTimestamp }
                        val latestDateStr = if (latestTimestamp != null) "Latest: ${dateFormat.format(Date(latestTimestamp))}" else "No files"
                        val isExpanded = expandedCategories[catName] ?: (count > 0 && searchQuery.isNotBlank())

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_accordion_$catName"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedCategories = expandedCategories.toMutableMap().apply {
                                                put(catName, !isExpanded)
                                            }
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(HealthVaultCream),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(getCategoryEmoji(catName), fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = catName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HealthVaultInk
                                            )
                                            Text(
                                                text = latestDateStr,
                                                fontSize = 11.sp,
                                                color = HealthVaultInk.copy(alpha = 0.5f)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (count > 0) HealthVaultTealLight else HealthVaultCream)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "$count ${if (count == 1) "file" else "files"}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (count > 0) HealthVaultTeal else HealthVaultInk.copy(alpha = 0.4f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isExpanded) "▲" else "▼", fontSize = 12.sp, color = HealthVaultInk.copy(alpha = 0.5f))
                                    }
                                }

                                // Expanded Category File Cards
                                AnimatedVisibility(visible = isExpanded && count > 0) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        catRecords.forEachIndexed { index, record ->
                                            StaggeredEntryCard(index = index) {
                                                EncryptedRecordCard(
                                                    record = record,
                                                    userRole = UserRole.PATIENT,
                                                    onDecryptAndOpen = { viewModel.openRecordDetails(it) },
                                                    onAdminInspect = { viewModel.inspectRecordAdmin(it) },
                                                    onDelete = {
                                                        viewModel.moveToTrash(it)
                                                        Toast.makeText(context, "Moved to Trash", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Render Flat List for ALL, FAVORITES, ARCHIVE, TRASH
            item {
                if (sortedRecords.isEmpty()) {
                    EmptyRecordsState(onUploadClick = onNavigateToUpload)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        sortedRecords.forEachIndexed { index, record ->
                            StaggeredEntryCard(index = index) {
                                EncryptedRecordCard(
                                    record = record,
                                    userRole = UserRole.PATIENT,
                                    onDecryptAndOpen = { viewModel.openRecordDetails(it) },
                                    onAdminInspect = { viewModel.inspectRecordAdmin(it) },
                                    onDelete = {
                                        if (record.isTrashed) {
                                            viewModel.deleteRecord(record)
                                            Toast.makeText(context, "Permanently deleted", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.moveToTrash(record)
                                            Toast.makeText(context, "Moved to Trash", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onRestore = {
                                        viewModel.restoreFromTrash(it)
                                        Toast.makeText(context, "Record restored", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Empty Trash Alert Dialog
    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty Trash?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete all ${trashedRecords.size} items in the Trash? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyTrashDialog = false
                        Toast.makeText(context, "Trash emptied", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthVaultDanger)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ----------------------------------------------------
// 3. ENHANCED UPLOAD SCREEN
// ----------------------------------------------------
@Composable
private fun UploadScreen(
    viewModel: HealthVaultViewModel,
    onUploaded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ALL_CATEGORIES_LIST[0]) }
    var hospitalName by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var documentType by remember { mutableStateOf("PDF Document") }
    var tags by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reportText by remember { mutableStateOf("") }
    var isFileSelected by remember { mutableStateOf(false) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgressTarget by remember { mutableFloatStateOf(0f) }
    var isUploadSuccess by remember { mutableStateOf(false) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = uploadProgressTarget,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "UploadProgressAnimation"
    )

    LaunchedEffect(isUploading) {
        if (isUploading) {
            uploadProgressTarget = 1.0f
            delay(850)
            isUploading = false
            isUploadSuccess = true
            delay(600)
            val fileNameToSave = if (title.isNotBlank()) "${title.replace(" ", "_")}.pdf" else "Medical_Report_${System.currentTimeMillis().toString().takeLast(4)}.pdf"
            val contentToSave = if (reportText.isNotBlank()) reportText else "HEALTH VAULT ENCRYPTED MEDICAL RECORD\nTitle: ${title.ifBlank { "Diagnostic Report" }}\nCategory: $selectedCategory\nHospital: ${hospitalName.ifBlank { "City General Hospital" }}\nDoctor: ${doctorName.ifBlank { "Dr. Sarah Connor" }}"

            viewModel.uploadRecordWithMetadata(
                fileName = fileNameToSave,
                category = selectedCategory,
                content = contentToSave,
                title = title.ifBlank { fileNameToSave.substringBeforeLast(".") },
                hospitalName = hospitalName,
                doctorName = doctorName,
                examinationDate = System.currentTimeMillis(),
                documentType = documentType,
                tags = tags,
                notes = notes,
                ocrText = "OCR EXTRACTED SUMMARY:\n$contentToSave"
            )
            onUploaded()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        text = "UPLOAD NEW MEDICAL RECORD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultInk.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val dropInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(HealthVaultTealLight)
                            .border(
                                width = 2.dp,
                                color = HealthVaultTealMid,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .pressScale(dropInteraction)
                            .clickable(interactionSource = dropInteraction, indication = null) {
                                if (!isFileSelected) {
                                    isFileSelected = true
                                    title = "Blood Test Results - Aug 2026"
                                    hospitalName = "Apollo Medical Center"
                                    doctorName = "Dr. Robert Vance, MD"
                                    tags = "Blood, Routine, Cholesterol"
                                    notes = "Fast 12 hours before test. Everything normal."
                                    reportText = "COMPLETE BLOOD COUNT (CBC)\nHemoglobin: 14.5 g/dL (Normal)\nWBC Count: 6.8 x 10^3 / µL (Normal)\nPlatelets: 250 x 10^3 / µL\nTriglycerides: 120 mg/dL"
                                }
                            }
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⬆️", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isFileSelected) "Selected: ${title.ifBlank { "Medical_Report.pdf" }}" else "Tap to Select / Scan File",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal
                            )
                            Text(
                                text = "Supports PDF, JPG, PNG, DICOM (Max 15MB)",
                                fontSize = 11.sp,
                                color = HealthVaultInk.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (isUploading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Encrypting with AES-256-GCM...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                                Text("${(animatedProgress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = HealthVaultTeal,
                                trackColor = HealthVaultTealLight,
                            )
                        }
                    }

                    if (isUploadSuccess) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(HealthVaultTealLight)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✅", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Record Encrypted & Saved Successfully!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultTeal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text("Document Title", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            isFileSelected = true
                        },
                        placeholder = { Text("e.g. Annual Blood Panel 2026", color = HealthVaultInk.copy(alpha = 0.4f), fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_file_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HealthVaultCream,
                            unfocusedContainerColor = HealthVaultCream,
                            focusedBorderColor = HealthVaultTeal,
                            unfocusedBorderColor = HealthVaultBorder,
                            focusedTextColor = HealthVaultInk,
                            unfocusedTextColor = HealthVaultInk
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selection Dropdown
                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        val catInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(HealthVaultCream)
                                .border(1.dp, HealthVaultBorder, RoundedCornerShape(10.dp))
                                .pressScale(catInteraction)
                                .clickable(interactionSource = catInteraction, indication = null) {
                                    categoryDropdownExpanded = true
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${getCategoryEmoji(selectedCategory)} $selectedCategory", fontSize = 13.sp, color = HealthVaultInk, fontWeight = FontWeight.SemiBold)
                                Text("▾", fontSize = 14.sp, color = HealthVaultTeal)
                            }
                        }

                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            ALL_CATEGORIES_LIST.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${getCategoryEmoji(cat)} $cat", fontSize = 12.sp) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hospital & Doctor Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hospital / Clinic", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = hospitalName,
                                onValueChange = { hospitalName = it },
                                placeholder = { Text("Hospital Name", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("upload_hospital_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = HealthVaultCream,
                                    unfocusedContainerColor = HealthVaultCream,
                                    focusedBorderColor = HealthVaultTeal,
                                    unfocusedBorderColor = HealthVaultBorder
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Doctor's Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = doctorName,
                                onValueChange = { doctorName = it },
                                placeholder = { Text("Dr. Name", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("upload_doctor_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = HealthVaultCream,
                                    unfocusedContainerColor = HealthVaultCream,
                                    focusedBorderColor = HealthVaultTeal,
                                    unfocusedBorderColor = HealthVaultBorder
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tags
                    Text("Tags (comma separated)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        placeholder = { Text("e.g. Annual, Fasting, Routine", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("upload_tags_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HealthVaultCream,
                            unfocusedContainerColor = HealthVaultCream,
                            focusedBorderColor = HealthVaultTeal,
                            unfocusedBorderColor = HealthVaultBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Report Plaintext Content
                    Text("Report Plaintext Content (To Encrypt)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = reportText,
                        onValueChange = { reportText = it },
                        placeholder = { Text("Enter medical details, diagnoses, or notes...", color = HealthVaultInk.copy(alpha = 0.4f), fontSize = 13.sp) },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_report_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HealthVaultCream,
                            unfocusedContainerColor = HealthVaultCream,
                            focusedBorderColor = HealthVaultTeal,
                            unfocusedBorderColor = HealthVaultBorder,
                            focusedTextColor = HealthVaultInk,
                            unfocusedTextColor = HealthVaultInk
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val uploadBtnInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            if (!isUploading && !isUploadSuccess) {
                                isUploading = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HealthVaultTeal,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        interactionSource = uploadBtnInteraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pressScale(uploadBtnInteraction)
                            .testTag("confirm_upload_btn")
                    ) {
                        Text(if (isUploading) "Encrypting & Storing..." else "Upload Encrypted Record", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 4. EMERGENCY SCREEN
// ----------------------------------------------------
@Composable
private fun EmergencyScreen(
    viewModel: HealthVaultViewModel,
    patientProfile: com.example.data.entity.PatientProfileEntity?,
    nominees: List<NomineeEntity>,
    onAddNomineeClick: () -> Unit,
    onEditNomineeClick: (NomineeEntity) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SOS Button Section
        item {
            StaggeredEntryCard(index = 0) {
                val sosInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFE84040), HealthVaultDanger)
                            )
                        )
                        .border(8.dp, HealthVaultDanger.copy(alpha = 0.2f), CircleShape)
                        .pressScale(sosInteraction)
                        .clickable(interactionSource = sosInteraction, indication = null) {
                            viewModel.triggerSosAlert()
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
                            context.startActivity(intent)
                        }
                        .testTag("sos_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚑", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SOS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Critical Medical Info Card
        item {
            StaggeredEntryCard(index = 1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthVaultDangerLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5C6C2))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚨 CRITICAL MEDICAL INFO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultDanger,
                                letterSpacing = 1.sp
                            )
                            if (patientProfile?.isOrganDonor == true) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HealthVaultGold)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("🫀 Organ Donor", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                EmergencyGridBox(
                                    label = "Blood Group",
                                    value = patientProfile?.bloodGroup ?: "O+",
                                    modifier = Modifier.weight(1f)
                                )
                                EmergencyGridBox(
                                    label = "Allergies",
                                    value = patientProfile?.allergies?.ifBlank { "None" } ?: "Penicillin",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                EmergencyGridBox(
                                    label = "Chronic Diseases",
                                    value = patientProfile?.chronicDiseases?.ifBlank { "None" } ?: "Asthma",
                                    modifier = Modifier.weight(1f)
                                )
                                EmergencyGridBox(
                                    label = "Medications",
                                    value = patientProfile?.currentMedications?.ifBlank { "None" } ?: "Insulin",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (!patientProfile?.emergencyNotes.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text("⚠️ Emergency Medical Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultDanger)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(patientProfile!!.emergencyNotes, fontSize = 12.sp, color = HealthVaultInk)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Emergency Instructions & Preferred Hospital Card
        item {
            StaggeredEntryCard(index = 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "🏥 PREFERRED HOSPITAL & INSTRUCTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = patientProfile?.preferredHospital ?: "Metropolitan General Emergency Center",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultTeal
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(HealthVaultCream)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = patientProfile?.emergencyInstructions ?: "In case of severe emergency, contact primary nominee immediately.",
                                fontSize = 12.sp,
                                color = HealthVaultInk
                            )
                        }
                    }
                }
            }
        }

        // Emergency Contacts Card
        item {
            StaggeredEntryCard(index = 3) {
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
                            Text(
                                text = "EMERGENCY CONTACTS & NOMINEES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultInk.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )

                            Button(
                                onClick = onAddNomineeClick,
                                colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTealLight, contentColor = HealthVaultTeal),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_nominee_emergency_screen_btn")
                            ) {
                                Text("+ Add Nominee", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (nominees.isEmpty()) {
                            EmptyNomineesState(onAddNomineeClick = onAddNomineeClick)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                nominees.forEach { nominee ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (nominee.isPrimary) HealthVaultGold.copy(alpha = 0.08f) else HealthVaultCream
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = if (nominee.isPrimary) HealthVaultGold else HealthVaultBorder
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = nominee.name,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HealthVaultInk
                                                    )
                                                    Text(
                                                        text = "(${nominee.relationship})",
                                                        fontSize = 12.sp,
                                                        color = HealthVaultInk.copy(alpha = 0.6f)
                                                    )
                                                }

                                                if (nominee.isPrimary) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(HealthVaultGold)
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("⭐ PRIMARY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(HealthVaultTealLight)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Priority #${nominee.priority}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White)
                                                        .border(1.dp, HealthVaultBorder, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Level: ${nominee.emergencyPermissionLevel}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = "📞 ${nominee.phone}  •  ✉️ ${nominee.email}",
                                                fontSize = 11.sp,
                                                color = HealthVaultInk.copy(alpha = 0.7f)
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    if (!nominee.isPrimary) {
                                                        TextButton(
                                                            onClick = { viewModel.setPrimaryNominee(nominee.nomineeId) },
                                                            contentPadding = PaddingValues(0.dp)
                                                        ) {
                                                            Text("⭐ Set Primary", fontSize = 11.sp, color = HealthVaultGold, fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    TextButton(
                                                        onClick = { onEditNomineeClick(nominee) },
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("✏️ Edit", fontSize = 11.sp, color = HealthVaultTeal, fontWeight = FontWeight.Bold)
                                                    }

                                                    TextButton(
                                                        onClick = { viewModel.removeNominee(nominee.nomineeId) },
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("🗑️ Delete", fontSize = 11.sp, color = HealthVaultDanger, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(HealthVaultTeal)
                                                            .clickable {
                                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${nominee.phone}"))
                                                                context.startActivity(intent)
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("📞 Call", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(HealthVaultGold)
                                                            .clickable {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${nominee.phone}"))
                                                                context.startActivity(intent)
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("📩 SMS", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyGridBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = HealthVaultInk.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HealthVaultInk
            )
        }
    }
}

// ----------------------------------------------------
// 5. PROFILE / DRAWER MODAL (Personal Health Profile Manager)
// ----------------------------------------------------
@Composable
private fun ProfileDrawerModal(
    viewModel: HealthVaultViewModel,
    patientProfile: com.example.data.entity.PatientProfileEntity?,
    nominees: List<NomineeEntity>,
    onDismiss: () -> Unit,
    onOpenAddNominee: () -> Unit
) {
    val context = LocalContext.current
    var selectedSectionTab by remember { mutableStateOf(0) } // 0: Personal, 1: Medical, 2: Emergency & Insurance

    // Personal Info
    var editName by remember(patientProfile) { mutableStateOf(patientProfile?.name ?: "Sarah Jenkins") }
    var editDob by remember(patientProfile) { mutableStateOf(patientProfile?.dateOfBirth ?: "1992-05-14") }
    var editAge by remember(patientProfile) { mutableStateOf(patientProfile?.age?.toString() ?: "34") }
    var editGender by remember(patientProfile) { mutableStateOf(patientProfile?.gender ?: "Female") }
    var editBlood by remember(patientProfile) { mutableStateOf(patientProfile?.bloodGroup ?: "O+") }
    var editHeight by remember(patientProfile) { mutableStateOf(patientProfile?.heightCm ?: "168 cm") }
    var editWeight by remember(patientProfile) { mutableStateOf(patientProfile?.weightKg ?: "62 kg") }
    var editBmi by remember(patientProfile) { mutableStateOf(patientProfile?.bmi ?: "22.0 (Normal)") }
    var editMobile by remember(patientProfile) { mutableStateOf(patientProfile?.mobileNumber ?: "+1 555-019-2831") }
    var editEmail by remember(patientProfile) { mutableStateOf(patientProfile?.email ?: "sarah.jenkins@example.com") }
    var editAddress by remember(patientProfile) { mutableStateOf(patientProfile?.address ?: "742 Evergreen Terrace, Springfield, OR 97477") }
    var editAadhaar by remember(patientProfile) { mutableStateOf(patientProfile?.aadhaarNumber ?: "XXXX-XXXX-9842") }
    var editEmergencyNotes by remember(patientProfile) { mutableStateOf(patientProfile?.emergencyNotes ?: "Carries Epipen for severe peanut allergy.") }
    var editOrganDonor by remember(patientProfile) { mutableStateOf(patientProfile?.isOrganDonor ?: true) }
    var editDisability by remember(patientProfile) { mutableStateOf(patientProfile?.disabilityStatus ?: "None") }

    // Insurance Info
    var editInsuranceProvider by remember(patientProfile) { mutableStateOf(patientProfile?.insuranceProvider ?: "Blue Cross Blue Shield Health") }
    var editInsurancePolicy by remember(patientProfile) { mutableStateOf(patientProfile?.insurancePolicyNumber ?: "POL-88392019") }
    var editInsuranceCoverage by remember(patientProfile) { mutableStateOf(patientProfile?.insuranceCoverageDetails ?: "Comprehensive Inpatient & Outpatient ($500,000 limit)") }
    var editInsuranceExpiry by remember(patientProfile) { mutableStateOf(patientProfile?.insuranceExpiryDate ?: "2027-12-31") }

    // Medical Info
    var editAllergies by remember(patientProfile) { mutableStateOf(patientProfile?.allergies ?: "Penicillin, Peanuts (Severe)") }
    var editChronic by remember(patientProfile) { mutableStateOf(patientProfile?.chronicDiseases ?: "Type 1 Diabetes, Mild Asthma") }
    var editMedication by remember(patientProfile) { mutableStateOf(patientProfile?.currentMedications ?: "Lantus Solostar 20U Daily, Ventolin HFA Inhaler PRN") }
    var editSurgeries by remember(patientProfile) { mutableStateOf(patientProfile?.previousSurgeries ?: "Appendectomy (2018), Wisdom Teeth Removal (2014)") }
    var editFamilyHistory by remember(patientProfile) { mutableStateOf(patientProfile?.familyMedicalHistory ?: "Father: Hypertension; Mother: Type 2 Diabetes") }
    var editVaccinationHistory by remember(patientProfile) { mutableStateOf(patientProfile?.vaccinationHistory ?: "COVID-19 Booster (2025), Tdap (2023), Influenza (2025)") }
    var editLifestyle by remember(patientProfile) { mutableStateOf(patientProfile?.lifestyleHabits ?: "Non-smoker, Social Alcohol (1-2 drinks/wk), Moderate Exercise") }
    var editPrimaryPhysician by remember(patientProfile) { mutableStateOf(patientProfile?.primaryPhysician ?: "Dr. Marcus Vance, MD (Metropolitan General Hospital)") }

    // Emergency Info
    var editEmergencyContact by remember(patientProfile) { mutableStateOf(patientProfile?.emergencyContact ?: "John Jenkins (+1 555-014-9981)") }
    var editEmergencyInstructions by remember(patientProfile) { mutableStateOf(patientProfile?.emergencyInstructions ?: "In case of severe hypoglycemia, administer Oral Glucose Gel / Glucagon. Contact John Jenkins immediately.") }
    var editPreferredHospital by remember(patientProfile) { mutableStateOf(patientProfile?.preferredHospital ?: "Metropolitan General Emergency Center, City Center") }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedModalCard(onDismiss = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header / Avatar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(HealthVaultTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = editName.take(1).uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = editName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultInk
                            )
                            Text(
                                text = "Personal Health Profile • PAT-98421",
                                fontSize = 11.sp,
                                color = HealthVaultInk.copy(alpha = 0.6f)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = HealthVaultInk)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section Tabs Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HealthVaultCream)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Personal", "Medical", "Emergency").forEachIndexed { idx, title ->
                        val isSel = selectedSectionTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) HealthVaultTeal else Color.Transparent)
                                .clickable { selectedSectionTab = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else HealthVaultInk.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedSectionTab) {
                    0 -> { // Personal Info
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileTextField(label = "Full Name", value = editName, onValueChange = { editName = it })
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProfileTextField(label = "Date of Birth", value = editDob, onValueChange = { editDob = it }, modifier = Modifier.weight(1f))
                                ProfileTextField(label = "Age", value = editAge, onValueChange = { editAge = it }, modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProfileTextField(label = "Gender", value = editGender, onValueChange = { editGender = it }, modifier = Modifier.weight(1f))
                                ProfileTextField(label = "Blood Group", value = editBlood, onValueChange = { editBlood = it }, modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProfileTextField(label = "Height (cm)", value = editHeight, onValueChange = { editHeight = it }, modifier = Modifier.weight(1f))
                                ProfileTextField(label = "Weight (kg)", value = editWeight, onValueChange = { editWeight = it }, modifier = Modifier.weight(1f))
                            }
                            ProfileTextField(label = "BMI", value = editBmi, onValueChange = { editBmi = it })
                            ProfileTextField(label = "Mobile Number", value = editMobile, onValueChange = { editMobile = it })
                            ProfileTextField(label = "Email Address", value = editEmail, onValueChange = { editEmail = it })
                            ProfileTextField(label = "Home Address", value = editAddress, onValueChange = { editAddress = it })
                            ProfileTextField(label = "Aadhaar / National ID (Optional)", value = editAadhaar, onValueChange = { editAadhaar = it })
                            
                            // Organ Donor Status Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HealthVaultCream)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Organ Donor Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (editOrganDonor) HealthVaultTeal else Color.LightGray.copy(alpha = 0.3f))
                                            .clickable { editOrganDonor = true }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Yes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (editOrganDonor) Color.White else HealthVaultInk)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (!editOrganDonor) HealthVaultDanger else Color.LightGray.copy(alpha = 0.3f))
                                            .clickable { editOrganDonor = false }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("No", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!editOrganDonor) Color.White else HealthVaultInk)
                                    }
                                }
                            }

                            ProfileTextField(label = "Disability Status", value = editDisability, onValueChange = { editDisability = it })
                            ProfileTextField(label = "Emergency Medical Notes", value = editEmergencyNotes, onValueChange = { editEmergencyNotes = it })
                        }
                    }

                    1 -> { // Medical Info
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileTextField(label = "Allergies", value = editAllergies, onValueChange = { editAllergies = it })
                            ProfileTextField(label = "Chronic Diseases", value = editChronic, onValueChange = { editChronic = it })
                            ProfileTextField(label = "Current Medications", value = editMedication, onValueChange = { editMedication = it })
                            ProfileTextField(label = "Previous Surgeries", value = editSurgeries, onValueChange = { editSurgeries = it })
                            ProfileTextField(label = "Family Medical History", value = editFamilyHistory, onValueChange = { editFamilyHistory = it })
                            ProfileTextField(label = "Vaccination History", value = editVaccinationHistory, onValueChange = { editVaccinationHistory = it })
                            ProfileTextField(label = "Lifestyle (Smoking / Alcohol)", value = editLifestyle, onValueChange = { editLifestyle = it })
                            ProfileTextField(label = "Primary Physician", value = editPrimaryPhysician, onValueChange = { editPrimaryPhysician = it })
                        }
                    }

                    2 -> { // Emergency & Insurance
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("INSURANCE DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                            ProfileTextField(label = "Insurance Provider", value = editInsuranceProvider, onValueChange = { editInsuranceProvider = it })
                            ProfileTextField(label = "Policy Number", value = editInsurancePolicy, onValueChange = { editInsurancePolicy = it })
                            ProfileTextField(label = "Coverage Details", value = editInsuranceCoverage, onValueChange = { editInsuranceCoverage = it })
                            ProfileTextField(label = "Expiry Date", value = editInsuranceExpiry, onValueChange = { editInsuranceExpiry = it })

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("EMERGENCY DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultDanger)
                            ProfileTextField(label = "Emergency Contact Phone", value = editEmergencyContact, onValueChange = { editEmergencyContact = it })
                            ProfileTextField(label = "Preferred Hospital", value = editPreferredHospital, onValueChange = { editPreferredHospital = it })
                            ProfileTextField(label = "Emergency Instructions", value = editEmergencyInstructions, onValueChange = { editEmergencyInstructions = it })

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MANAGE NOMINEES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultInk.copy(alpha = 0.6f)
                                )

                                val addBtnInteraction = remember { MutableInteractionSource() }
                                Button(
                                    onClick = onOpenAddNominee,
                                    colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTealLight, contentColor = HealthVaultTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    interactionSource = addBtnInteraction,
                                    modifier = Modifier.pressScale(addBtnInteraction)
                                ) {
                                    Text("+ Add Nominee", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (nominees.isEmpty()) {
                                EmptyNomineesState(onAddNomineeClick = onOpenAddNominee)
                            } else {
                                nominees.forEach { nom ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(HealthVaultCream)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(nom.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                            Text("${nom.relationship} • ${nom.phone}", fontSize = 11.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                                        }

                                        val removeInteraction = remember { MutableInteractionSource() }
                                        Text(
                                            text = "Remove",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = HealthVaultDanger,
                                            modifier = Modifier
                                                .pressScale(removeInteraction)
                                                .clickable(interactionSource = removeInteraction, indication = null) { viewModel.removeNominee(nom.nomineeId) }
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val saveBtnInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = {
                        val updated = (patientProfile ?: com.example.data.entity.PatientProfileEntity()).copy(
                            name = editName,
                            dateOfBirth = editDob,
                            age = editAge.toIntOrNull() ?: 34,
                            gender = editGender,
                            bloodGroup = editBlood,
                            heightCm = editHeight,
                            weightKg = editWeight,
                            bmi = editBmi,
                            mobileNumber = editMobile,
                            email = editEmail,
                            address = editAddress,
                            aadhaarNumber = editAadhaar,
                            emergencyNotes = editEmergencyNotes,
                            isOrganDonor = editOrganDonor,
                            disabilityStatus = editDisability,
                            insuranceProvider = editInsuranceProvider,
                            insurancePolicyNumber = editInsurancePolicy,
                            insuranceCoverageDetails = editInsuranceCoverage,
                            insuranceExpiryDate = editInsuranceExpiry,
                            allergies = editAllergies,
                            chronicDiseases = editChronic,
                            currentMedications = editMedication,
                            previousSurgeries = editSurgeries,
                            familyMedicalHistory = editFamilyHistory,
                            vaccinationHistory = editVaccinationHistory,
                            lifestyleHabits = editLifestyle,
                            primaryPhysician = editPrimaryPhysician,
                            emergencyContact = editEmergencyContact,
                            emergencyInstructions = editEmergencyInstructions,
                            preferredHospital = editPreferredHospital,
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.updateFullPatientProfile(updated)
                        Toast.makeText(context, "Personal Health Profile updated!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    interactionSource = saveBtnInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(saveBtnInteraction)
                        .testTag("save_profile_button")
                ) {
                    Text("Save Personal Health Profile", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = HealthVaultCream,
            unfocusedContainerColor = HealthVaultCream,
            focusedBorderColor = HealthVaultTeal,
            unfocusedBorderColor = HealthVaultBorder,
            focusedTextColor = HealthVaultInk,
            unfocusedTextColor = HealthVaultInk
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

// ----------------------------------------------------
// 6. ADD / EDIT NOMINEE MODAL
// ----------------------------------------------------
@Composable
private fun AddNomineeModal(
    viewModel: HealthVaultViewModel,
    existingNominee: NomineeEntity? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existingNominee?.name ?: "") }
    var rel by remember { mutableStateOf(existingNominee?.relationship ?: "") }
    var phone by remember { mutableStateOf(existingNominee?.phone ?: "") }
    var email by remember { mutableStateOf(existingNominee?.email ?: "") }
    var priority by remember { mutableStateOf(existingNominee?.priority ?: 1) }
    var emergencyPermissionLevel by remember { mutableStateOf(existingNominee?.emergencyPermissionLevel ?: "Full Access") }
    var isPrimary by remember { mutableStateOf(existingNominee?.isPrimary ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedModalCard(onDismiss = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (existingNominee != null) "Edit Emergency Nominee" else "Add Emergency Nominee",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthVaultInk
                )

                Text(
                    text = "Trusted contacts authorize emergency access when you are unavailable.",
                    fontSize = 11.sp,
                    color = HealthVaultInk.copy(alpha = 0.6f)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("nominee_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream,
                        focusedBorderColor = HealthVaultTeal,
                        unfocusedBorderColor = HealthVaultBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = rel,
                    onValueChange = { rel = it },
                    label = { Text("Relationship (e.g. Spouse, Brother, Doctor)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("nominee_rel_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream,
                        focusedBorderColor = HealthVaultTeal,
                        unfocusedBorderColor = HealthVaultBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (for SMS OTP)", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("nominee_phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream,
                        focusedBorderColor = HealthVaultTeal,
                        unfocusedBorderColor = HealthVaultBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("nominee_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream,
                        focusedBorderColor = HealthVaultTeal,
                        unfocusedBorderColor = HealthVaultBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Priority Selection
                Column {
                    Text("Nominee Priority Rank:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 4).forEach { p ->
                            val isSel = priority == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) HealthVaultTeal else HealthVaultCream)
                                    .clickable { priority = p }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#$p Priority", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else HealthVaultInk)
                            }
                        }
                    }
                }

                // Emergency Permission Level
                Column {
                    Text("Emergency Permission Level:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf("Full Access", "Emergency Summary Only", "Critical Vitals & Allergies").forEach { lvl ->
                        val isSel = emergencyPermissionLevel == lvl
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSel) HealthVaultTeal else HealthVaultBorder, RoundedCornerShape(8.dp))
                                .background(if (isSel) HealthVaultTealLight else Color.White)
                                .clickable { emergencyPermissionLevel = lvl }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lvl, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = HealthVaultInk)
                            if (isSel) {
                                Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                            }
                        }
                    }
                }

                // Primary Nominee Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPrimary) HealthVaultGold.copy(alpha = 0.15f) else HealthVaultCream)
                        .clickable { isPrimary = !isPrimary }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("⭐ Primary Emergency Nominee", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                        Text("First contact alerted when Emergency Mode is triggered.", fontSize = 10.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        modifier = Modifier.testTag("primary_nominee_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = HealthVaultCream, contentColor = HealthVaultTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                if (existingNominee != null) {
                                    viewModel.updateNominee(
                                        existingNominee.copy(
                                            name = name,
                                            relationship = rel.ifBlank { "Family" },
                                            phone = phone,
                                            email = email.ifBlank { "nominee@vault.io" },
                                            priority = priority,
                                            emergencyPermissionLevel = emergencyPermissionLevel,
                                            isPrimary = isPrimary
                                        )
                                    )
                                } else {
                                    viewModel.addNominee(
                                        name = name,
                                        relationship = rel.ifBlank { "Family" },
                                        phone = phone,
                                        email = email.ifBlank { "nominee@vault.io" },
                                        priority = priority,
                                        emergencyPermissionLevel = emergencyPermissionLevel,
                                        isPrimary = isPrimary
                                    )
                                }
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("save_nominee_btn")
                    ) {
                        Text("Save Nominee", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
