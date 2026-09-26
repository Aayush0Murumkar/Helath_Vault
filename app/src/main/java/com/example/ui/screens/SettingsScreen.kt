package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.settings.AboutScreen
import com.example.ui.screens.settings.AccountScreen
import com.example.ui.screens.settings.AppPreferencesScreen
import com.example.ui.screens.settings.AuditLogsScreen
import com.example.ui.screens.settings.BackupRestoreScreen
import com.example.ui.screens.settings.ConnectedDevicesScreen
import com.example.ui.screens.settings.DataSharingScreen
import com.example.ui.screens.settings.EmergencySettingsScreen
import com.example.ui.screens.settings.LanguageAccessibilityScreen
import com.example.ui.screens.settings.MedicalProfileScreen
import com.example.ui.screens.settings.NomineesScreen
import com.example.ui.screens.settings.NotificationsScreen
import com.example.ui.screens.settings.RecordsStorageScreen
import com.example.ui.screens.settings.SecurityPrivacyScreen
import com.example.ui.screens.settings.SettingsSubScreen
import com.example.viewmodel.HealthVaultViewModel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.ui.components.StaggeredEntryCard
import com.example.ui.components.pressScale

data class SettingsMenuItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val target: SettingsSubScreen,
    val category: String
)

@Composable
fun SettingsScreen(
    viewModel: HealthVaultViewModel,
    modifier: Modifier = Modifier
) {
    var subScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }
    var searchQuery by remember { mutableStateOf("") }

    val menuItems = remember {
        listOf(
            SettingsMenuItem("item_account", "Account", "Personal details, contact info, role & ID", Icons.Default.Person, SettingsSubScreen.ACCOUNT, "Identity"),
            SettingsMenuItem("item_medical", "Medical Profile", "Blood group, allergies, chronic diseases, meds, insurance", Icons.Default.Healing, SettingsSubScreen.MEDICAL_PROFILE, "Clinical"),
            SettingsMenuItem("item_nominees", "Nominees & Emergency Access", "Emergency contacts, nominee management & key inheritance", Icons.Default.People, SettingsSubScreen.NOMINEES, "Emergency"),
            SettingsMenuItem("item_security", "Security & Privacy", "Password, PIN, biometrics, 2FA, sessions & encryption status", Icons.Default.Security, SettingsSubScreen.SECURITY_PRIVACY, "Security"),
            SettingsMenuItem("item_records", "Records & Storage", "Record preferences, storage usage, export/import & cache", Icons.Default.Folder, SettingsSubScreen.RECORDS_STORAGE, "Data"),
            SettingsMenuItem("item_sharing", "Data Sharing", "Sharing permissions, QR code sharing & temporary access", Icons.Default.Share, SettingsSubScreen.DATA_SHARING, "Permissions"),
            SettingsMenuItem("item_notifications", "Notification Center", "Medicine & appointment reminders, lab reports, doctor access, emergency alerts & history", Icons.Default.Notifications, SettingsSubScreen.NOTIFICATIONS, "Alerts"),
            SettingsMenuItem("item_backup", "Backup & Restore", "Encrypted cloud backup, sync schedule & manual restore", Icons.Default.Backup, SettingsSubScreen.BACKUP_RESTORE, "Storage"),
            SettingsMenuItem("item_audit", "Audit Logs", "Cryptographic activity trail & tamper-evident log", Icons.Default.ReceiptLong, SettingsSubScreen.AUDIT_LOGS, "Compliance"),
            SettingsMenuItem("item_language", "Language & Accessibility", "Language selection, dark mode, font scaling & accessibility", Icons.Default.Accessibility, SettingsSubScreen.LANGUAGE_ACCESSIBILITY, "Preferences"),
            SettingsMenuItem("item_devices", "Connected Devices", "Logged-in active devices, sessions & remote logout", Icons.Default.Devices, SettingsSubScreen.CONNECTED_DEVICES, "Security"),
            SettingsMenuItem("item_emergency", "Emergency Settings", "SOS triggers, preferred hospital & hotline configuration", Icons.Default.Emergency, SettingsSubScreen.EMERGENCY_SETTINGS, "Emergency"),
            SettingsMenuItem("item_preferences", "App Preferences", "Theme palette, dashboard cards, animations & layout", Icons.Default.Palette, SettingsSubScreen.APP_PREFERENCES, "Customization"),
            SettingsMenuItem("item_about", "About", "Version info, privacy policy, terms, FAQs & support", Icons.Default.Info, SettingsSubScreen.ABOUT, "Info")
        )
    }

    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) menuItems
        else menuItems.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subtitle.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    if (subScreen != SettingsSubScreen.MAIN) {
        BackHandler {
            subScreen = SettingsSubScreen.MAIN
        }
    }

    AnimatedContent(
        targetState = subScreen,
        transitionSpec = {
            if (targetState == SettingsSubScreen.MAIN) {
                // Navigating back to main list
                (slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(280)))
                    .togetherWith(slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(280)))
            } else {
                // Navigating into subscreen
                (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(280)))
                    .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(280)))
            }
        },
        label = "SettingsSubScreenTransition"
    ) { currentSubScreen ->
        when (currentSubScreen) {
            SettingsSubScreen.MAIN -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(HealthVaultCream)
                ) {
                    // Header Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HealthVaultTeal)
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Column {
                            Text(
                                text = "Settings & Preferences",
                                fontFamily = FontFamily.Serif,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "HEALTH VAULT ENCLAVE NAVIGATION HUB",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search settings, security, profiles...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_search_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = HealthVaultTeal,
                                    unfocusedBorderColor = HealthVaultBorder
                                )
                            )
                        }

                        itemsIndexed(filteredItems, key = { _, item -> item.id }) { index, item ->
                            StaggeredEntryCard(index = index) {
                                val itemInteraction = remember { MutableInteractionSource() }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pressScale(itemInteraction)
                                        .clickable(interactionSource = itemInteraction, indication = null) { subScreen = item.target }
                                        .testTag(item.id),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(HealthVaultTealLight),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.title,
                                                    tint = HealthVaultTeal,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = item.title,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HealthVaultInk
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(HealthVaultTealLight)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = item.category.uppercase(),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = HealthVaultTeal
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(2.dp))

                                                Text(
                                                    text = item.subtitle,
                                                    fontSize = 11.sp,
                                                    color = HealthVaultInk.copy(alpha = 0.65f),
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = "Navigate to ${item.title}",
                                            tint = HealthVaultInk.copy(alpha = 0.4f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SettingsSubScreen.ACCOUNT -> AccountScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.MEDICAL_PROFILE -> MedicalProfileScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.NOMINEES -> NomineesScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.SECURITY_PRIVACY -> SecurityPrivacyScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.RECORDS_STORAGE -> RecordsStorageScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.DATA_SHARING -> DataSharingScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.NOTIFICATIONS -> NotificationsScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.BACKUP_RESTORE -> BackupRestoreScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.AUDIT_LOGS -> AuditLogsScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.LANGUAGE_ACCESSIBILITY -> LanguageAccessibilityScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.CONNECTED_DEVICES -> ConnectedDevicesScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.EMERGENCY_SETTINGS -> EmergencySettingsScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.APP_PREFERENCES -> AppPreferencesScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )

            SettingsSubScreen.ABOUT -> AboutScreen(
                viewModel = viewModel,
                onBack = { subScreen = SettingsSubScreen.MAIN },
                modifier = modifier
            )
        }
    }
}
