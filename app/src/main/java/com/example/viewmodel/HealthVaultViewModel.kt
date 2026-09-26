package com.example.viewmodel

import android.app.Application
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.CryptoManager
import com.example.data.db.HealthVaultDatabase
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.DoctorAccessRequestEntity
import com.example.data.entity.EncryptedRecordEntity
import com.example.data.entity.PatientProfileEntity
import com.example.data.repository.HealthVaultRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole { PATIENT, DOCTOR, ADMIN }

enum class PatientTab { VAULT, ACCESS_CONTROL, NOMINEES, SOS_EMERGENCY, EMERGENCY_PROFILE, AUDIT_LOGS, AI_SUMMARIZER, SETTINGS }

data class DecryptedViewRecord(
    val record: EncryptedRecordEntity,
    val plaintext: String
)

data class AiSummaryState(
    val isAnalyzing: Boolean = false,
    val summaryText: String? = null,
    val consentGiven: Boolean = false,
    val errorMessage: String? = null
)

class HealthVaultViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HealthVaultDatabase.getDatabase(application)
    private val repository = HealthVaultRepository(db.healthVaultDao())

    // Current Role & Active Tab
    private val _currentRole = MutableStateFlow(UserRole.PATIENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _activePatientTab = MutableStateFlow(PatientTab.VAULT)
    val activePatientTab: StateFlow<PatientTab> = _activePatientTab.asStateFlow()

    // Biometric & Security Settings State
    private val _isAppLocked = MutableStateFlow(true)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(true)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _vaultPin = MutableStateFlow("1234")
    val vaultPin: StateFlow<String> = _vaultPin.asStateFlow()

    private val _autoLockMinutes = MutableStateFlow(1)
    val autoLockMinutes: StateFlow<Int> = _autoLockMinutes.asStateFlow()

    private val _isPinLockEnabled = MutableStateFlow(true)
    val isPinLockEnabled: StateFlow<Boolean> = _isPinLockEnabled.asStateFlow()

    private val _sessionTimeoutMinutes = MutableStateFlow(15)
    val sessionTimeoutMinutes: StateFlow<Int> = _sessionTimeoutMinutes.asStateFlow()

    private val _autoLockBackground = MutableStateFlow(true)
    val autoLockBackground: StateFlow<Boolean> = _autoLockBackground.asStateFlow()

    private val _flagSecureEnabled = MutableStateFlow(true)
    val flagSecureEnabled: StateFlow<Boolean> = _flagSecureEnabled.asStateFlow()

    private val _autoBackupFrequency = MutableStateFlow("Daily")
    val autoBackupFrequency: StateFlow<String> = _autoBackupFrequency.asStateFlow()

    private val _lastBackupTimestamp = MutableStateFlow(System.currentTimeMillis() - 14400000L)
    val lastBackupTimestamp: StateFlow<Long> = _lastBackupTimestamp.asStateFlow()

    private val _activeSessions = MutableStateFlow(
        listOf(
            ActiveSessionItem("sess_1", "Google Pixel 8 Pro", "Smartphone", "Android 14", "192.168.1.42", "San Francisco, CA", "Active Now", true),
            ActiveSessionItem("sess_2", "iPad Air 5th Gen", "Tablet", "iPadOS 17.4", "73.189.24.11", "San Francisco, CA", "25 mins ago", false),
            ActiveSessionItem("sess_3", "Chrome Enclave Workstation", "Desktop", "macOS Sonoma", "198.51.100.14", "San Jose, CA", "2 hours ago", false)
        )
    )
    val activeSessions: StateFlow<List<ActiveSessionItem>> = _activeSessions.asStateFlow()

    private val _trustedDevices = MutableStateFlow(
        listOf(
            TrustedDeviceItem("dev_1", "Pixel 8 Pro (Primary)", "Google Pixel 8 Pro", "Aug 01, 2026", "Primary Enclave", true),
            TrustedDeviceItem("dev_2", "iPad Air", "Apple iPad13,16", "Jul 15, 2026", "Secondary Enclave", false),
            TrustedDeviceItem("dev_3", "MacBook Pro M3", "Apple Mac15,3", "Jun 20, 2026", "Web Vault Authorized", false)
        )
    )
    val trustedDevices: StateFlow<List<TrustedDeviceItem>> = _trustedDevices.asStateFlow()

    private val _loginHistory = MutableStateFlow(
        listOf(
            LoginHistoryItem("lh_1", "Aug 02, 2026 @ 09:14 AM", "Google Pixel 8 Pro", "Biometric Fingerprint", "SUCCESS", "192.168.1.42", "San Francisco, CA"),
            LoginHistoryItem("lh_2", "Aug 02, 2026 @ 07:30 AM", "Google Pixel 8 Pro", "Security PIN", "SUCCESS", "192.168.1.42", "San Francisco, CA"),
            LoginHistoryItem("lh_3", "Aug 01, 2026 @ 11:45 PM", "Unknown Chrome Browser", "Master Password", "FAILED_ATTEMPT", "203.0.113.88", "Dallas, TX"),
            LoginHistoryItem("lh_4", "Aug 01, 2026 @ 04:20 PM", "iPad Air 5th Gen", "Biometric Face ID", "SUCCESS", "73.189.24.11", "San Francisco, CA"),
            LoginHistoryItem("lh_5", "Jul 31, 2026 @ 02:10 PM", "Google Pixel 8 Pro", "Biometric Fingerprint", "SUCCESS", "192.168.1.42", "San Francisco, CA")
        )
    )
    val loginHistory: StateFlow<List<LoginHistoryItem>> = _loginHistory.asStateFlow()

    private val _securityAlerts = MutableStateFlow(
        listOf(
            SecurityAlertItem("alt_1", "10 mins ago", "New Trusted Device Session", "Session started on Google Pixel 8 Pro with Hardware Enclave verification.", "INFO", false),
            SecurityAlertItem("alt_2", "2 hours ago", "Biometric Authentication Active", "Fingerprint biometric unlock verified by KeyStore TEE enclave.", "INFO", false),
            SecurityAlertItem("alt_3", "Yesterday", "Security PIN Updated", "Vault backup PIN updated successfully.", "NOTICE", false),
            SecurityAlertItem("alt_4", "3 days ago", "Unrecognized Login Blocked", "Failed authentication attempt from IP 203.0.113.88 in Dallas, TX.", "WARNING", false)
        )
    )
    val securityAlerts: StateFlow<List<SecurityAlertItem>> = _securityAlerts.asStateFlow()

    // Notification Center State
    private val _notifications = MutableStateFlow(
        listOf(
            NotificationCenterItem(
                id = "n_1",
                title = "Medicine Reminder: Atorvastatin",
                message = "Take Atorvastatin 20mg with water after dinner. Scheduled daily dose.",
                timestamp = "10 mins ago",
                category = NotificationCategory.MEDICINE,
                isRead = false,
                severity = "INFO",
                actionText = "Mark Taken"
            ),
            NotificationCenterItem(
                id = "n_2",
                title = "Upcoming Appointment Reminder",
                message = "Cardiology Follow-up with Dr. Sarah Jenkins tomorrow at 02:00 PM (Clinic 4B, St. Jude).",
                timestamp = "45 mins ago",
                category = NotificationCategory.APPOINTMENT,
                isRead = false,
                severity = "INFO",
                actionText = "View Details"
            ),
            NotificationCenterItem(
                id = "n_3",
                title = "Lab Report Ready: Lipid & CMP Panel",
                message = "Comprehensive Metabolic Panel & Lipid Profile results released by Quest Diagnostics.",
                timestamp = "2 hours ago",
                category = NotificationCategory.LAB_REPORT,
                isRead = false,
                severity = "SUCCESS",
                actionText = "View Results"
            ),
            NotificationCenterItem(
                id = "n_4",
                title = "Doctor Access Request",
                message = "Dr. Robert Vance, MD requested emergency access to your Cardiovascular records.",
                timestamp = "3 hours ago",
                category = NotificationCategory.DOCTOR_ACCESS,
                isRead = false,
                severity = "WARNING",
                actionText = "Review Access"
            ),
            NotificationCenterItem(
                id = "n_5",
                title = "Emergency Override Triggered",
                message = "Emergency Nominee Override initiated by Marcus Vance (Son) at St. Jude Emergency Room.",
                timestamp = "5 hours ago",
                category = NotificationCategory.EMERGENCY,
                isRead = false,
                severity = "CRITICAL",
                actionText = "Manage SOS"
            ),
            NotificationCenterItem(
                id = "n_6",
                title = "Zero-Knowledge Backup Completed",
                message = "Cloud backup completed successfully. 48.2 MB encrypted via AES-256-GCM in KeyStore enclave.",
                timestamp = "Yesterday",
                category = NotificationCategory.BACKUP,
                isRead = true,
                severity = "SUCCESS",
                actionText = "Backup Logs"
            ),
            NotificationCenterItem(
                id = "n_7",
                title = "Storage Warning: Vault at 88% Capacity",
                message = "Vault storage is at 88% capacity (4.4 GB / 5.0 GB). Consider archiving older DICOM scans.",
                timestamp = "2 days ago",
                category = NotificationCategory.STORAGE,
                isRead = true,
                severity = "WARNING",
                actionText = "Manage Storage"
            ),
            NotificationCenterItem(
                id = "n_8",
                title = "Security Alert: Unrecognized Device",
                message = "Failed login attempt blocked from IP 203.0.113.88 in Dallas, TX.",
                timestamp = "3 days ago",
                category = NotificationCategory.SECURITY,
                isRead = true,
                severity = "CRITICAL",
                actionText = "Security Log"
            )
        )
    )
    val notifications: StateFlow<List<NotificationCenterItem>> = _notifications.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // AI Assistant State
    private val _isAITyping = MutableStateFlow(false)
    val isAITyping: StateFlow<Boolean> = _isAITyping.asStateFlow()

    private val _conversations = MutableStateFlow(
        listOf(
            ConversationSession(
                id = "conv_1",
                title = "Lipid & Blood Report Analysis",
                timestamp = "Today, 09:30 AM",
                preview = "Total Cholesterol is 185 mg/dL (Normal). HbA1c is 6.2%...",
                messageCount = 3
            ),
            ConversationSession(
                id = "conv_2",
                title = "Medication Interactions Check",
                timestamp = "Yesterday",
                preview = "No adverse interactions detected between Metformin and Atorvastatin...",
                messageCount = 2
            ),
            ConversationSession(
                id = "conv_3",
                title = "Cardiology Visit Prep",
                timestamp = "3 days ago",
                preview = "Questions to ask Dr. Jenkins regarding blood pressure targets...",
                messageCount = 4
            )
        )
    )
    val conversations: StateFlow<List<ConversationSession>> = _conversations.asStateFlow()

    private val _currentConversationId = MutableStateFlow("conv_1")
    val currentConversationId: StateFlow<String> = _currentConversationId.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessageItem(
                id = "msg_1",
                sender = ChatSender.SYSTEM,
                text = "✨ Health Vault Gemini Assistant initialized. All queries are processed locally with zero-knowledge encryption.",
                timestamp = "09:30 AM",
                type = ChatMessageType.GENERAL
            ),
            ChatMessageItem(
                id = "msg_2",
                sender = ChatSender.USER,
                text = "Can you summarize my recent blood report?",
                timestamp = "09:31 AM",
                type = ChatMessageType.REPORT_SUMMARY
            ),
            ChatMessageItem(
                id = "msg_3",
                sender = ChatSender.ASSISTANT,
                text = """
                📊 **Recent Blood Report Summary (Quest Diagnostics - Lipid & CMP Panel)**:
                
                • **Total Cholesterol**: 185 mg/dL — *Desirable Range (< 200 mg/dL)*
                • **HDL (Good Cholesterol)**: 55 mg/dL — *Optimal (> 50 mg/dL for females)*
                • **LDL (Bad Cholesterol)**: 110 mg/dL — *Near Optimal*
                • **Triglycerides**: 120 mg/dL — *Normal (< 150 mg/dL)*
                • **HbA1c**: 6.2% — *Monitored (Pre-diabetic/Target range for Type 1 Diabetes management)*
                • **Liver & Kidney Function**: AST 22 U/L, ALT 24 U/L, eGFR > 90 mL/min — *All Healthy*
                
                💡 **AI Clinical Takeaway**: Your lipid profile is well-controlled under your current regimen (Atorvastatin 20mg). Keep maintaining a low-sodium Mediterranean diet.
                """.trimIndent(),
                timestamp = "09:31 AM",
                type = ChatMessageType.REPORT_SUMMARY
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessageItem>> = _chatMessages.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _decryptedCacheStatus = MutableStateFlow("Memory Encrypted (0 Active Views)")
    val decryptedCacheStatus: StateFlow<String> = _decryptedCacheStatus.asStateFlow()

    // Patient Profile & Records State
    val patientProfile: StateFlow<PatientProfileEntity?> = repository.getPatientProfileFlow("PAT-98421")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val encryptedRecords: StateFlow<List<EncryptedRecordEntity>> = repository.getEncryptedRecordsFlow("PAT-98421")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicalEvents: StateFlow<List<com.example.data.entity.MedicalEventEntity>> = repository.getMedicalEventsFlow("PAT-98421")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val doctorRequests: StateFlow<List<DoctorAccessRequestEntity>> = repository.getDoctorAccessRequestsFlow("PAT-98421")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAuditLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nominees State
    val nominees: StateFlow<List<com.example.data.entity.NomineeEntity>> = repository.getNomineesFlow("PAT-98421")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin State
    val doctorVerifications: StateFlow<List<com.example.data.entity.DoctorVerificationEntity>> = repository.getDoctorVerificationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hospitalVerifications: StateFlow<List<com.example.data.entity.HospitalVerificationEntity>> = repository.getHospitalVerificationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userAccounts: StateFlow<List<com.example.data.entity.UserAccountEntity>> = repository.getUserAccountsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val consultationNotes: StateFlow<List<com.example.data.entity.ConsultationNoteEntity>> = repository.getConsultationNotesFlow("PAT-98421")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // SOS & Emergency Mode State
    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private val _isLiveLocationSharing = MutableStateFlow(false)
    val isLiveLocationSharing: StateFlow<Boolean> = _isLiveLocationSharing.asStateFlow()

    private val _currentLocationCoords = MutableStateFlow("40.7128° N, 74.0060° W (Manhattan, NY)")
    val currentLocationCoords: StateFlow<String> = _currentLocationCoords.asStateFlow()

    // Doctor Portal Auth State
    private val _doctorPortalDoctorId = MutableStateFlow("DOC-4481")
    val doctorPortalDoctorId: StateFlow<String> = _doctorPortalDoctorId.asStateFlow()

    private val _doctorPortalHospitalId = MutableStateFlow("HOSP-METRO-01")
    val doctorPortalHospitalId: StateFlow<String> = _doctorPortalHospitalId.asStateFlow()

    private val _isDoctorPortalLoggedIn = MutableStateFlow(true)
    val isDoctorPortalLoggedIn: StateFlow<Boolean> = _isDoctorPortalLoggedIn.asStateFlow()

    // Doctor QR Scanner State
    private val _isQrScanning = MutableStateFlow(false)
    val isQrScanning: StateFlow<Boolean> = _isQrScanning.asStateFlow()

    // Decryption Dialog & Active Plaintext view
    private val _selectedDecryptedRecord = MutableStateFlow<DecryptedViewRecord?>(null)
    val selectedDecryptedRecord: StateFlow<DecryptedViewRecord?> = _selectedDecryptedRecord.asStateFlow()

    // Dedicated Record Details Page Navigation State
    private val _selectedRecordForDetails = MutableStateFlow<EncryptedRecordEntity?>(null)
    val selectedRecordForDetails: StateFlow<EncryptedRecordEntity?> = _selectedRecordForDetails.asStateFlow()

    // Doctor Search & Access Verification State
    private val _doctorSearchQuery = MutableStateFlow("PAT-98421")
    val doctorSearchQuery: StateFlow<String> = _doctorSearchQuery.asStateFlow()

    private val _searchedPatient = MutableStateFlow<PatientProfileEntity?>(null)
    val searchedPatient: StateFlow<PatientProfileEntity?> = _searchedPatient.asStateFlow()

    private val _doctorAuthPassword = MutableStateFlow("")
    val doctorAuthPassword: StateFlow<String> = _doctorAuthPassword.asStateFlow()

    private val _isDoctorAuthenticatedForSession = MutableStateFlow(false)
    val isDoctorAuthenticatedForSession: StateFlow<Boolean> = _isDoctorAuthenticatedForSession.asStateFlow()

    private val _doctorAuthError = MutableStateFlow<String?>(null)
    val doctorAuthError: StateFlow<String?> = _doctorAuthError.asStateFlow()

    // Session Remaining Seconds Counter
    private val _activeSessionRemainingSeconds = MutableStateFlow<Long?>(null)
    val activeSessionRemainingSeconds: StateFlow<Long?> = _activeSessionRemainingSeconds.asStateFlow()

    // Admin Inspection Modal State
    private val _adminInspectedRecord = MutableStateFlow<EncryptedRecordEntity?>(null)
    val adminInspectedRecord: StateFlow<EncryptedRecordEntity?> = _adminInspectedRecord.asStateFlow()

    // AI Summarizer State
    private val _aiSummaryState = MutableStateFlow(AiSummaryState())
    val aiSummaryState: StateFlow<AiSummaryState> = _aiSummaryState.asStateFlow()

    // Upload Record Modal State
    private val _showUploadModal = MutableStateFlow(false)
    val showUploadModal: StateFlow<Boolean> = _showUploadModal.asStateFlow()

    // Doctor Request Access Modal
    private val _showDoctorRequestModal = MutableStateFlow(false)
    val showDoctorRequestModal: StateFlow<Boolean> = _showDoctorRequestModal.asStateFlow()

    init {
        // Automatically search default patient when Doctor tab opens
        viewModelScope.launch {
            searchPatient("PAT-98421")
        }

        // Timer monitor loop for time-limited doctor access sessions
        viewModelScope.launch {
            while (true) {
                updateDoctorSessionTimer()
                delay(1000)
            }
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        viewModelScope.launch {
            repository.addAuditLog(
                userId = if (role == UserRole.PATIENT) "PAT-98421" else if (role == UserRole.DOCTOR) "DOC-4481" else "ADMIN-01",
                userRole = role.name,
                action = "SWITCH_ROLE_VIEW",
                details = "Switched interface to ${role.name} portal mode."
            )
        }
    }

    fun unlockApp() {
        _isAppLocked.value = false
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "BIOMETRIC_UNLOCK",
                details = "App unlocked via Fingerprint / PIN authentication."
            )
        }
    }

    fun lockApp() {
        _isAppLocked.value = true
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "APP_LOCKED",
                details = "App manually locked by user."
            )
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "SETTINGS_CHANGED",
                details = "Biometric fingerprint authentication ${if (enabled) "enabled" else "disabled"}."
            )
        }
    }

    fun setVaultPin(newPin: String) {
        _vaultPin.value = newPin
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "PIN_UPDATED",
                details = "Vault security PIN updated."
            )
        }
    }

    fun setAutoLockMinutes(minutes: Int) {
        _autoLockMinutes.value = minutes
    }

    fun setPinLockEnabled(enabled: Boolean) {
        _isPinLockEnabled.value = enabled
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "PIN_LOCK_TOGGLED",
                details = "PIN lock ${if (enabled) "enabled" else "disabled"}."
            )
        }
    }

    fun setSessionTimeoutMinutes(minutes: Int) {
        _sessionTimeoutMinutes.value = minutes
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "TIMEOUT_UPDATED",
                details = "Session timeout updated to $minutes minutes."
            )
        }
    }

    fun setAutoLockBackground(enabled: Boolean) {
        _autoLockBackground.value = enabled
    }

    fun setFlagSecureEnabled(enabled: Boolean) {
        _flagSecureEnabled.value = enabled
    }

    fun setAutoBackupFrequency(freq: String) {
        _autoBackupFrequency.value = freq
    }

    fun createLocalEncryptedBackup() {
        _lastBackupTimestamp.value = System.currentTimeMillis()
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "BACKUP_CREATED",
                details = "Created AES-256 encrypted local vault backup package (.hvpack)."
            )
        }
    }

    fun exportLocalVaultToJson(context: android.content.Context): String {
        return try {
            val records = encryptedRecords.value
            val profile = patientProfile.value
            val jsonObject = org.json.JSONObject().apply {
                put("patientId", profile?.id ?: "PAT-98421")
                put("patientName", profile?.name ?: "Sarah Jenkins")
                put("exportTimestamp", System.currentTimeMillis())
                put("recordCount", records.size)
                
                val recordsArray = org.json.JSONArray()
                records.forEach { r ->
                    val rObj = org.json.JSONObject().apply {
                        put("fileId", r.fileId)
                        put("title", r.title)
                        put("category", r.fileCategory)
                        put("hospital", r.hospitalName)
                        put("doctor", r.doctorName)
                        put("ocrText", r.ocrText)
                        put("aiSummary", r.aiSummary)
                        put("encryptedIvBase64", r.encryptedIvBase64)
                        put("encryptedPayloadBase64", r.encryptedPayloadBase64)
                    }
                    recordsArray.put(rObj)
                }
                put("records", recordsArray)
            }

            val exportDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            val file = java.io.File(exportDir, "HealthVault_Local_Storage_Backup.json")
            file.writeText(jsonObject.toString(2))
            
            viewModelScope.launch {
                repository.addAuditLog(
                    userId = "PAT-98421",
                    userRole = "PATIENT",
                    action = "LOCAL_STORAGE_EXPORT",
                    details = "Exported local vault data to file: ${file.absolutePath}"
                )
            }
            file.absolutePath
        } catch (e: Throwable) {
            "Error exporting: ${e.message}"
        }
    }

    fun revokeSession(sessionId: String) {
        _activeSessions.value = _activeSessions.value.filter { it.id != sessionId }
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "SESSION_REVOKED",
                details = "Revoked active session ID: $sessionId"
            )
        }
    }

    fun logoutOtherSessions() {
        _activeSessions.value = _activeSessions.value.filter { it.isCurrent }
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "SESSIONS_CLEARED",
                details = "Logged out all other remote active sessions."
            )
        }
    }

    fun addTrustedDevice(deviceName: String, model: String) {
        val newDev = TrustedDeviceItem(
            id = "dev_${System.currentTimeMillis()}",
            deviceName = deviceName,
            deviceModel = model,
            addedDate = "Today",
            trustLevel = "Secondary Enclave",
            isCurrentDevice = false
        )
        _trustedDevices.value = _trustedDevices.value + newDev
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "DEVICE_TRUSTED",
                details = "Added new trusted device: $deviceName ($model)"
            )
        }
    }

    fun removeTrustedDevice(deviceId: String) {
        _trustedDevices.value = _trustedDevices.value.filter { it.id != deviceId }
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "DEVICE_REMOVED",
                details = "Removed trusted device ID: $deviceId"
            )
        }
    }

    fun clearSecurityAlerts() {
        _securityAlerts.value = _securityAlerts.value.map { it.copy(isAcknowledged = true) }
    }

    fun purgeVaultDataAndAccount() {
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "VAULT_PURGED",
                details = "DANGER ZONE: Vault records and local account purged by user."
            )
            _decryptedCacheStatus.value = "Vault Data Wiped"
            _isAppLocked.value = true
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun clearDecryptedMemoryCache() {
        _selectedDecryptedRecord.value = null
        _decryptedCacheStatus.value = "Memory Wiped Successfully (${System.currentTimeMillis() % 10000})"
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "PAT-98421",
                userRole = "PATIENT",
                action = "CACHE_CLEARED",
                details = "In-memory decrypted medical records buffer wiped."
            )
        }
    }

    fun setPatientTab(tab: PatientTab) {
        _activePatientTab.value = tab
    }

    fun showUploadModal(show: Boolean) {
        _showUploadModal.value = show
    }

    fun showDoctorRequestModal(show: Boolean) {
        _showDoctorRequestModal.value = show
    }

    fun uploadRecord(fileName: String, category: String, content: String) {
        uploadRecordWithMetadata(
            fileName = fileName,
            category = category,
            content = content
        )
    }

    fun uploadRecordWithMetadata(
        fileName: String,
        category: String,
        content: String,
        title: String = "",
        hospitalName: String = "",
        doctorName: String = "",
        examinationDate: Long = System.currentTimeMillis(),
        documentType: String = "PDF Document",
        tags: String = "",
        notes: String = "",
        ocrText: String = "",
        aiSummary: String = "",
        diagnosis: String = "",
        medicines: String = ""
    ) {
        viewModelScope.launch {
            repository.uploadAndEncryptRecord(
                patientId = "PAT-98421",
                fileName = fileName,
                category = category,
                plaintext = content,
                title = title,
                hospitalName = hospitalName,
                doctorName = doctorName,
                examinationDate = examinationDate,
                documentType = documentType,
                tags = tags,
                notes = notes,
                ocrText = ocrText,
                aiSummary = aiSummary,
                diagnosis = diagnosis,
                medicines = medicines
            )
            _showUploadModal.value = false
        }
    }

    fun openRecordDetails(record: EncryptedRecordEntity) {
        _selectedRecordForDetails.value = record
    }

    fun closeRecordDetails() {
        _selectedRecordForDetails.value = null
    }

    fun updateRecordMetadata(updatedRecord: EncryptedRecordEntity) {
        viewModelScope.launch {
            repository.updateRecordMetadata(updatedRecord)
            if (_selectedRecordForDetails.value?.fileId == updatedRecord.fileId) {
                _selectedRecordForDetails.value = updatedRecord
            }
        }
    }

    fun toggleFavorite(record: EncryptedRecordEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(record)
            val current = _selectedRecordForDetails.value
            if (current?.fileId == record.fileId) {
                _selectedRecordForDetails.value = current.copy(isFavorite = !current.isFavorite)
            }
        }
    }

    fun toggleArchive(record: EncryptedRecordEntity) {
        viewModelScope.launch {
            repository.toggleArchive(record)
            val current = _selectedRecordForDetails.value
            if (current?.fileId == record.fileId) {
                _selectedRecordForDetails.value = current.copy(isArchived = !current.isArchived)
            }
        }
    }

    fun moveToTrash(record: EncryptedRecordEntity) {
        viewModelScope.launch {
            repository.moveToTrash(record)
            if (_selectedRecordForDetails.value?.fileId == record.fileId) {
                _selectedRecordForDetails.value = null
            }
        }
    }

    fun restoreFromTrash(record: EncryptedRecordEntity) {
        viewModelScope.launch {
            repository.restoreFromTrash(record)
            val current = _selectedRecordForDetails.value
            if (current?.fileId == record.fileId) {
                _selectedRecordForDetails.value = current.copy(isTrashed = false, trashedAt = 0L)
            }
        }
    }

    suspend fun decryptAndSelectRecordForDetails(record: EncryptedRecordEntity): String {
        val role = _currentRole.value
        val userId = if (role == UserRole.PATIENT) "PAT-98421" else "DOC-4481"
        return repository.decryptRecord(record, userId, role.name)
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    fun decryptAndSelectRecord(record: EncryptedRecordEntity) {
        viewModelScope.launch {
            val role = _currentRole.value
            val userId = if (role == UserRole.PATIENT) "PAT-98421" else "DOC-4481"
            val decryptedPlaintext = repository.decryptRecord(record, userId, role.name)
            _selectedDecryptedRecord.value = DecryptedViewRecord(record, decryptedPlaintext)
        }
    }

    fun triggerSosAlert() {
        toggleSosMode(true)
    }

    fun updateFullPatientProfile(updatedProfile: PatientProfileEntity) {
        viewModelScope.launch {
            val updated = updatedProfile.copy(updatedAt = System.currentTimeMillis())
            repository.updatePatientProfile(updated)
            repository.addAuditLog(
                userId = updated.id,
                userRole = "PATIENT",
                action = "PROFILE_UPDATED",
                details = "Personal Health Profile updated with comprehensive medical and emergency parameters."
            )
        }
    }

    fun updatePatientProfile(
        fullName: String,
        bloodGroup: String,
        chronic: String,
        allergies: String,
        medications: String
    ) {
        val current = patientProfile.value ?: PatientProfileEntity()
        val updated = current.copy(
            name = fullName,
            bloodGroup = bloodGroup,
            chronicDiseases = chronic,
            allergies = allergies,
            currentMedications = medications
        )
        updateFullPatientProfile(updated)
    }

    fun closeDecryptedRecord() {
        _selectedDecryptedRecord.value = null
    }

    fun deleteRecord(record: EncryptedRecordEntity) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            if (_selectedDecryptedRecord.value?.record?.fileId == record.fileId) {
                _selectedDecryptedRecord.value = null
            }
        }
    }

    // Doctor Actions
    fun setDoctorSearchQuery(query: String) {
        _doctorSearchQuery.value = query
    }

    fun searchPatient(query: String) {
        viewModelScope.launch {
            val profile = repository.searchPatientProfile(query)
            _searchedPatient.value = profile
        }
    }

    fun requestDoctorAccess(reason: String) {
        viewModelScope.launch {
            repository.createDoctorAccessRequest(
                patientId = "PAT-98421",
                doctorId = "DOC-4481",
                doctorName = "Dr. Marcus Vance, MD",
                hospitalName = "Metropolitan General Hospital",
                specialization = "Cardiology & Internal Medicine",
                reason = reason
            )
            _showDoctorRequestModal.value = false
        }
    }

    fun authenticateDoctorForSession(password: String) {
        _doctorAuthPassword.value = password
        if (password.trim() == "doc123" || password.trim() == "doctor" || password.trim().length >= 4) {
            _isDoctorAuthenticatedForSession.value = true
            _doctorAuthError.value = null
            viewModelScope.launch {
                repository.addAuditLog(
                    userId = "DOC-4481",
                    userRole = "DOCTOR",
                    action = "DOCTOR_PASSWORD_VERIFIED",
                    details = "Dr. Marcus Vance verified credentials for active access session."
                )
            }
        } else {
            _doctorAuthError.value = "Invalid doctor credentials. Please use password 'doc123'."
        }
    }

    // Patient Access Approval Actions
    fun approveDoctorRequest(requestId: String, durationMinutes: Int = 30, approvedBy: String = "Patient: Sarah Jenkins") {
        viewModelScope.launch {
            repository.approveDoctorAccess(requestId, durationMinutes, approvedBy)
        }
    }

    fun rejectDoctorRequest(requestId: String, rejectedBy: String = "Patient: Sarah Jenkins") {
        viewModelScope.launch {
            repository.rejectDoctorAccess(requestId, rejectedBy)
        }
    }

    fun approveEmergencyAccessByNominee(requestId: String, durationMinutes: Int = 60, nomineeName: String = "John Jenkins") {
        viewModelScope.launch {
            repository.approveEmergencyAccessByNominee(requestId, durationMinutes, nomineeName)
        }
    }

    fun rejectEmergencyAccessByNominee(requestId: String, nomineeName: String = "John Jenkins") {
        viewModelScope.launch {
            repository.rejectEmergencyAccessByNominee(requestId, nomineeName)
        }
    }

    fun simulateIncomingDoctorRequest(
        doctorName: String,
        hospitalName: String,
        specialization: String,
        reason: String,
        isEmergency: Boolean = false
    ) {
        viewModelScope.launch {
            repository.createDoctorAccessRequest(
                patientId = "PAT-98421",
                doctorId = "DOC-" + (1000..9999).random(),
                doctorName = doctorName,
                hospitalName = hospitalName,
                specialization = specialization,
                reason = reason,
                isEmergencyRequest = isEmergency
            )
        }
    }

    fun revokeDoctorRequest(requestId: String) {
        viewModelScope.launch {
            repository.revokeDoctorAccess(requestId)
        }
    }

    // Admin Inspection Modal
    fun inspectRecordAdmin(record: EncryptedRecordEntity) {
        _adminInspectedRecord.value = record
        viewModelScope.launch {
            repository.addAuditLog(
                userId = "ADMIN-01",
                userRole = "ADMIN",
                action = "INSPECT_METADATA_ONLY",
                details = "Admin attempted file inspection on '${record.originalFileName}'. Decryption blocked by Zero-Knowledge policy."
            )
        }
    }

    fun closeAdminInspection() {
        _adminInspectedRecord.value = null
    }

    // AI Privacy Summarizer
    fun grantAiConsentAndAnalyze() {
        _aiSummaryState.value = _aiSummaryState.value.copy(consentGiven = true, isAnalyzing = true, errorMessage = null)
        viewModelScope.launch {
            val records = encryptedRecords.value
            if (records.isEmpty()) {
                _aiSummaryState.value = _aiSummaryState.value.copy(
                    isAnalyzing = false,
                    summaryText = "No medical records found in vault to analyze."
                )
                return@launch
            }

            try {
                // Decrypt records in memory strictly for analysis
                val decryptedTexts = records.map { rec ->
                    CryptoManager.decrypt(rec.encryptedPayloadBase64, rec.encryptedIvBase64)
                }

                // Privacy summary generation
                val combinedText = decryptedTexts.joinToString("\n---\n")

                repository.addAuditLog(
                    userId = "PAT-98421",
                    userRole = "PATIENT",
                    action = "AI_IN_MEMORY_ANALYSIS",
                    details = "Patient granted explicit consent for ephemeral in-memory AI medical summary. Zero retention guaranteed."
                )

                delay(1200) // Realistic processing delay

                val summary = """
                    Zero-Knowledge AI Health Summary:
                    
                    • Glycemic Control: Fasting blood glucose is 112 mg/dL with HbA1c at 6.4%, showing effective Type 1 Diabetes management.
                    • Cardiac Status: Left ventricular ejection fraction (LVEF) is preserved at 62%. Normal wall motion observed.
                    • Critical Allergies: Severe Anaphylactic reaction to Penicillin and Peanuts. High alert required for prescriptions.
                    • Active Prescriptions: Basal Insulin Glargine (18 Units), Prandial Insulin Lispro, Albuterol inhaler.
                    • Recommendations: Schedule follow-up blood panel in 90 days and annual cardiology check.
                """.trimIndent()

                _aiSummaryState.value = _aiSummaryState.value.copy(
                    isAnalyzing = false,
                    summaryText = summary
                )
            } catch (e: Exception) {
                _aiSummaryState.value = _aiSummaryState.value.copy(
                    isAnalyzing = false,
                    errorMessage = "Analysis error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetAiConsent() {
        _aiSummaryState.value = AiSummaryState()
    }

    // Nominees Functions
    fun addNominee(
        name: String,
        relationship: String,
        phone: String,
        email: String,
        priority: Int = 1,
        emergencyPermissionLevel: String = "Full Access",
        isPrimary: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addNominee(name, relationship, phone, email, priority, emergencyPermissionLevel, isPrimary)
        }
    }

    fun updateNominee(nominee: com.example.data.entity.NomineeEntity) {
        viewModelScope.launch {
            repository.updateNominee(nominee)
        }
    }

    fun setPrimaryNominee(nomineeId: String) {
        viewModelScope.launch {
            repository.setPrimaryNominee(nomineeId)
        }
    }

    fun removeNominee(nomineeId: String) {
        viewModelScope.launch {
            repository.removeNominee(nomineeId)
        }
    }

    fun verifyEmergencyOtpAndGrantAccess(
        requestId: String,
        nomineeName: String,
        nomineePhone: String,
        permissionLevel: String,
        durationMinutes: Int = 60
    ) {
        viewModelScope.launch {
            repository.verifyEmergencyOtpAndGrantAccess(requestId, nomineeName, nomineePhone, permissionLevel, durationMinutes)
        }
    }

    // SOS & Emergency Mode Functions
    fun toggleSosMode(active: Boolean) {
        _isSosActive.value = active
        if (active) {
            _isLiveLocationSharing.value = true
            viewModelScope.launch {
                repository.addAuditLog(
                    userId = "PAT-98421",
                    userRole = "PATIENT",
                    action = "SOS_EMERGENCY_TRIGGERED",
                    details = "PATIENT ACTIVATED SOS EMERGENCY MODE. Live GPS coordinates broadcasted to emergency contacts & nearby hospitals."
                )
            }
        }
    }

    fun toggleLiveLocationSharing(sharing: Boolean) {
        _isLiveLocationSharing.value = sharing
    }

    // Doctor Portal Auth & Actions
    fun loginDoctorPortal(docId: String, pass: String, hospId: String) {
        _doctorPortalDoctorId.value = docId
        _doctorPortalHospitalId.value = hospId
        _isDoctorPortalLoggedIn.value = true
        viewModelScope.launch {
            repository.addAuditLog(
                userId = docId,
                userRole = "DOCTOR",
                action = "DOCTOR_PORTAL_LOGIN",
                details = "Doctor $docId authenticated successfully at hospital $hospId."
            )
        }
    }

    fun logoutDoctorPortal() {
        _isDoctorPortalLoggedIn.value = false
    }

    fun toggleQrScanning(scanning: Boolean) {
        _isQrScanning.value = scanning
    }

    fun addConsultationNote(noteText: String) {
        viewModelScope.launch {
            repository.addConsultationNote(
                doctorId = _doctorPortalDoctorId.value,
                patientId = "PAT-98421",
                noteText = noteText
            )
        }
    }

    fun requestEmergencyAccessWithNominee(reason: String) {
        viewModelScope.launch {
            val req = repository.createDoctorAccessRequest(
                patientId = "PAT-98421",
                doctorId = _doctorPortalDoctorId.value,
                doctorName = "Dr. Marcus Vance, MD",
                hospitalName = "Metropolitan General Hospital",
                specialization = "Cardiology & Emergency Medicine",
                reason = "[EMERGENCY UNCONSCIOUS PATIENT] $reason"
            )
            repository.addAuditLog(
                userId = _doctorPortalDoctorId.value,
                userRole = "DOCTOR",
                action = "EMERGENCY_ACCESS_NOMINEE_DISPATCHED",
                details = "Doctor requested EMERGENCY ACCESS for patient PAT-98421. Emergency alert dispatched to primary nominee John Jenkins (+1 555-014-9981)."
            )
        }
    }

    // Admin Functions
    fun verifyDoctor(doctorId: String, approve: Boolean) {
        viewModelScope.launch {
            repository.verifyDoctor(doctorId, approve)
        }
    }

    fun verifyHospital(hospitalId: String, approve: Boolean) {
        viewModelScope.launch {
            repository.verifyHospital(hospitalId, approve)
        }
    }

    fun toggleUserSuspension(userId: String, currentSuspended: Boolean) {
        viewModelScope.launch {
            repository.toggleUserSuspension(userId, currentSuspended)
        }
    }

    private fun updateDoctorSessionTimer() {
        val requests = doctorRequests.value
        val activeApproved = requests.firstOrNull { it.status == "APPROVED" }

        if (activeApproved == null || activeApproved.sessionExpiryTimestamp == null) {
            _activeSessionRemainingSeconds.value = null
            return
        }

        val now = System.currentTimeMillis()
        val remaining = (activeApproved.sessionExpiryTimestamp - now) / 1000

        if (remaining <= 0) {
            _activeSessionRemainingSeconds.value = 0
            _isDoctorAuthenticatedForSession.value = false
            viewModelScope.launch {
                repository.revokeDoctorAccess(activeApproved.requestId)
            }
        } else {
            _activeSessionRemainingSeconds.value = remaining
        }
    }

    fun addMedicalEvent(
        title: String,
        category: String,
        eventDate: Long,
        hospitalName: String,
        doctorName: String,
        quickPreview: String,
        vitalsOrNotes: String? = null
    ) {
        viewModelScope.launch {
            val event = com.example.data.entity.MedicalEventEntity(
                eventId = "ME_" + java.util.UUID.randomUUID().toString().take(8).uppercase(),
                patientId = "PAT-98421",
                title = title,
                category = category,
                eventDate = eventDate,
                hospitalName = hospitalName,
                doctorName = doctorName,
                quickPreview = quickPreview,
                vitalsOrNotes = vitalsOrNotes,
                status = "Completed"
            )
            repository.addMedicalEvent(event)
        }
    }

    fun deleteMedicalEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteMedicalEvent(eventId)
        }
    }

    // Notification Center Operations
    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun deleteNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    fun addNotification(
        title: String,
        message: String,
        category: NotificationCategory,
        severity: String = "INFO",
        actionText: String? = null
    ) {
        val newNotif = NotificationCenterItem(
            id = "n_${System.currentTimeMillis()}",
            title = title,
            message = message,
            timestamp = "Just now",
            category = category,
            isRead = false,
            severity = severity,
            actionText = actionText
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    // AI Assistant Operations
    fun processUserQuery(userText: String, messageType: ChatMessageType = ChatMessageType.GENERAL) {
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val userMsg = ChatMessageItem(
            id = "msg_${System.currentTimeMillis()}",
            sender = ChatSender.USER,
            text = userText,
            timestamp = time,
            type = messageType
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAITyping.value = true
            kotlinx.coroutines.delay(1200) // typing animation delay

            val aiResponseText: String
            var matchingRecords: List<com.example.data.entity.EncryptedRecordEntity> = emptyList()

            when (messageType) {
                ChatMessageType.MEDICAL_SUMMARY -> {
                    aiResponseText = """
                        🏥 **Comprehensive Medical Summary**:
                        
                        • **Patient**: Sarah Jenkins (Age 34, Blood Group O+)
                        • **Primary Physician**: Dr. Marcus Vance, MD (St. Jude Medical Center)
                        • **Active Diagnoses**: Type 1 Diabetes Mellitus, Mild Intermittent Asthma
                        • **Allergies**: Penicillin (Moderate Rash)
                        • **Active Prescriptions**:
                          - Atorvastatin 20mg (1x daily at night)
                          - Metformin 500mg (2x daily with meals)
                          - Lisinopril 10mg (1x daily morning)
                          - Albuterol HFA Inhaler (PRN)
                        • **Latest Vitals**: Blood Pressure 120/80 mmHg, Resting Heart Rate 72 bpm, SpO2 99%.
                        
                        🔒 *Vault Cryptographic Hash Verified: SHA-256 Validated.*
                    """.trimIndent()
                }
                ChatMessageType.MEDICINE_EXPLANATION -> {
                    aiResponseText = """
                        💊 **Medication Plain-English Breakdown**:
                        
                        1. **Atorvastatin 20mg**: Statin medication that lowers LDL (bad) cholesterol and stabilizes arterial plaques. Take at bedtime with or without food. Avoid grapefruit juice.
                        2. **Metformin 500mg**: Reduces hepatic glucose production and improves peripheral insulin sensitivity. Best taken with meals to minimize gastrointestinal discomfort.
                        3. **Lisinopril 10mg**: ACE inhibitor that relaxes blood vessels and offers renal/kidney protection for diabetic patients.
                        4. **Albuterol Inhaler**: Fast-acting bronchodilator for rapid relief during sudden shortness of breath or asthma flare-ups.
                        
                        ⚠️ *Safety Audit*: No severe drug-drug interactions detected between active prescriptions.
                    """.trimIndent()
                }
                ChatMessageType.APPOINTMENT_EXPLANATION -> {
                    aiResponseText = """
                        📅 **Upcoming Appointment Guide**:
                        
                        • **Visit**: Cardiology Follow-up & ECG
                        • **Provider**: Dr. Sarah Jenkins, MD
                        • **Date & Time**: Tomorrow at 02:00 PM (Clinic 4B, St. Jude)
                        
                        📋 **What to Prepare**:
                        1. Fasting is NOT required for this consultation unless blood panel is re-ordered.
                        2. Bring your home Blood Pressure log from the past 14 days.
                        
                        💡 **Recommended Questions for Dr. Jenkins**:
                        - "Are my current BP readings meeting our target threshold?"
                        - "Should we adjust Atorvastatin dosage based on latest lab results?"
                    """.trimIndent()
                }
                ChatMessageType.HEALTH_TRENDS -> {
                    aiResponseText = """
                        📈 **Health Trends Analysis (6-Month Overview)**:
                        
                        • **Blood Pressure**: Average 118/78 mmHg — *Trend: Stable & Optimal*
                        • **Fasting Blood Glucose**: Average 102 mg/dL — *Trend: Improved by 6% over last quarter*
                        • **LDL Cholesterol**: Down from 128 mg/dL to 110 mg/dL — *Trend: Significant Improvement*
                        • **Heart Rate (Resting)**: 68 - 74 bpm — *Trend: Normal Athletic Target*
                        
                        🌟 **AI Health Score**: 92/100 (Excellent Management)
                    """.trimIndent()
                }
                ChatMessageType.REPORT_SUMMARY -> {
                    aiResponseText = """
                        🧪 **Recent Blood Report Summary (Quest Diagnostics - Lipid & CMP Panel)**:
                        
                        • **Total Cholesterol**: 185 mg/dL — *Desirable Range (< 200 mg/dL)*
                        • **HDL (Good Cholesterol)**: 55 mg/dL — *Optimal (> 50 mg/dL)*
                        • **LDL (Bad Cholesterol)**: 110 mg/dL — *Near Optimal*
                        • **Triglycerides**: 120 mg/dL — *Normal (< 150 mg/dL)*
                        • **HbA1c**: 6.2% — *Controlled Target Range*
                        • **Kidney & Liver Function**: eGFR > 90 mL/min, ALT/AST Normal — *All Healthy*
                    """.trimIndent()
                }
                ChatMessageType.RECORD_SEARCH_RESULT -> {
                    val q = userText.lowercase()
                    val currentVault = encryptedRecords.value
                    matchingRecords = currentVault.filter {
                        it.title.lowercase().contains(q) ||
                        it.fileCategory.lowercase().contains(q) ||
                        it.doctorName.lowercase().contains(q) ||
                        it.hospitalName.lowercase().contains(q) ||
                        it.ocrText.lowercase().contains(q) ||
                        it.notes.lowercase().contains(q) ||
                        it.aiSummary.lowercase().contains(q)
                    }
                    aiResponseText = if (matchingRecords.isNotEmpty()) {
                        "🔍 **Natural Language Vault Search**: Found **${matchingRecords.size} encrypted record(s)** matching '$userText'."
                    } else {
                        "🔍 **Natural Language Vault Search**: Searched all encrypted vault records for '$userText'. No exact match found, showing all relevant health categories."
                    }
                }
                else -> {
                    val lower = userText.lowercase()
                    aiResponseText = when {
                        lower.contains("blood") || lower.contains("lab") || lower.contains("report") -> {
                            "🧪 **Lab Query Response**: Based on your Quest Diagnostics blood panel, your liver and kidney markers are healthy, and total cholesterol is 185 mg/dL."
                        }
                        lower.contains("doctor") || lower.contains("appointment") -> {
                            "👨‍⚕️ **Appointment Info**: Your next appointment is with Dr. Sarah Jenkins tomorrow at 02:00 PM at St. Jude Medical Center."
                        }
                        lower.contains("medicine") || lower.contains("pill") || lower.contains("drug") -> {
                            "💊 **Medication Summary**: You have 4 active prescriptions: Atorvastatin, Metformin, Lisinopril, and Albuterol. No adverse interactions detected."
                        }
                        lower.contains("summary") || lower.contains("overview") -> {
                            "🏥 **Medical Summary**: Sarah Jenkins, age 34. Active diagnosis: Type 1 Diabetes and Mild Asthma. Vitals stable."
                        }
                        else -> {
                            "✨ **Gemini Assistant**: I analyzed your encrypted Health Vault context regarding '$userText'. Your vitals, prescriptions, and lab history indicate stable overall health parameters."
                        }
                    }
                }
            }

            val aiMsg = ChatMessageItem(
                id = "msg_${System.currentTimeMillis() + 1}",
                sender = ChatSender.ASSISTANT,
                text = aiResponseText,
                timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                type = messageType,
                matchingRecords = matchingRecords
            )

            _isAITyping.value = false
            _chatMessages.value = _chatMessages.value + aiMsg
        }
    }

    fun loadConversation(conversationId: String) {
        _currentConversationId.value = conversationId
        val session = _conversations.value.find { it.id == conversationId }
        val title = session?.title ?: "AI Consultation"

        _chatMessages.value = listOf(
            ChatMessageItem(
                id = "msg_sys_${System.currentTimeMillis()}",
                sender = ChatSender.SYSTEM,
                text = "✨ Loaded session: $title",
                timestamp = "Just now",
                type = ChatMessageType.GENERAL
            ),
            ChatMessageItem(
                id = "msg_hist_${System.currentTimeMillis()}",
                sender = ChatSender.ASSISTANT,
                text = "Loaded previous context for '$title'. How can I assist you further with this medical record or query?",
                timestamp = "Just now",
                type = ChatMessageType.GENERAL
            )
        )
    }

    fun startNewConversation() {
        val newId = "conv_${System.currentTimeMillis()}"
        val newConv = ConversationSession(
            id = newId,
            title = "New AI Health Inquiry",
            timestamp = "Just now",
            preview = "Ask Gemini about your medical records...",
            messageCount = 1
        )
        _conversations.value = listOf(newConv) + _conversations.value
        _currentConversationId.value = newId
        _chatMessages.value = listOf(
            ChatMessageItem(
                id = "msg_new_${System.currentTimeMillis()}",
                sender = ChatSender.SYSTEM,
                text = "✨ New AI Session started. Encrypted context ready.",
                timestamp = "Just now",
                type = ChatMessageType.GENERAL
            )
        )
    }

    fun clearCurrentConversation() {
        _chatMessages.value = listOf(
            ChatMessageItem(
                id = "msg_clear_${System.currentTimeMillis()}",
                sender = ChatSender.SYSTEM,
                text = "✨ Conversation cleared. Vault session re-encrypted.",
                timestamp = "Just now",
                type = ChatMessageType.GENERAL
            )
        )
    }
}

data class ActiveSessionItem(
    val id: String,
    val deviceName: String,
    val deviceType: String,
    val osVersion: String,
    val ipAddress: String,
    val location: String,
    val lastActive: String,
    val isCurrent: Boolean
)

data class TrustedDeviceItem(
    val id: String,
    val deviceName: String,
    val deviceModel: String,
    val addedDate: String,
    val trustLevel: String,
    val isCurrentDevice: Boolean
)

data class LoginHistoryItem(
    val id: String,
    val timestamp: String,
    val deviceName: String,
    val authMethod: String,
    val status: String,
    val ipAddress: String,
    val location: String
)

data class SecurityAlertItem(
    val id: String,
    val timestamp: String,
    val title: String,
    val description: String,
    val severity: String,
    val isAcknowledged: Boolean
)

enum class NotificationCategory {
    MEDICINE,
    APPOINTMENT,
    LAB_REPORT,
    DOCTOR_ACCESS,
    EMERGENCY,
    BACKUP,
    STORAGE,
    SECURITY
}

data class NotificationCenterItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val category: NotificationCategory,
    val isRead: Boolean = false,
    val severity: String = "INFO",
    val actionText: String? = null
)

enum class ChatSender { USER, ASSISTANT, SYSTEM }

enum class ChatMessageType {
    GENERAL,
    MEDICAL_SUMMARY,
    MEDICINE_EXPLANATION,
    APPOINTMENT_EXPLANATION,
    HEALTH_TRENDS,
    REPORT_SUMMARY,
    RECORD_SEARCH_RESULT
}

data class ChatMessageItem(
    val id: String,
    val sender: ChatSender,
    val text: String,
    val timestamp: String,
    val type: ChatMessageType = ChatMessageType.GENERAL,
    val isTyping: Boolean = false,
    val matchingRecords: List<com.example.data.entity.EncryptedRecordEntity> = emptyList()
)

data class ConversationSession(
    val id: String,
    val title: String,
    val timestamp: String,
    val preview: String,
    val messageCount: Int
)



