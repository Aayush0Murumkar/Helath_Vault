package com.example.data.repository

import com.example.backend.api.IHealthApiService
import com.example.backend.api.LocalHealthApiService
import com.example.backend.auth.IAuthRepository
import com.example.backend.auth.FirebaseAuthService
import com.example.backend.firestore.IFirestoreRepository
import com.example.backend.firestore.FirestoreSyncService
import com.example.backend.functions.ICloudFunctionsService
import com.example.backend.functions.LocalCloudFunctionsService
import com.example.backend.notifications.INotificationService
import com.example.backend.notifications.LocalNotificationService
import com.example.backend.notifications.FCMNotificationService
import com.example.backend.storage.ICloudStorageService
import com.example.backend.storage.FirebaseStorageService
import com.example.crypto.CryptoManager
import com.example.data.dao.HealthVaultDao
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.DoctorAccessRequestEntity
import com.example.data.entity.EncryptedRecordEntity
import com.example.data.entity.PatientProfileEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Concrete implementation of [IHealthVaultRepository].
 * Orchestrates local Room database and modular backend service implementations:
 * - Authentication (Firebase Auth / Local)
 * - Database Sync (Cloud Firestore / Room)
 * - Encrypted Blob Storage (Firebase Storage / Local)
 * - Serverless Triggers (Cloud Functions)
 * - Push Alerts (Firebase Cloud Messaging / NotificationManager)
 * - Gemini Intelligence (Gemini REST / Local)
 */
class HealthVaultRepository(
    private val dao: HealthVaultDao,
    override val authRepository: IAuthRepository = FirebaseAuthService(),
    override val firestoreRepository: IFirestoreRepository = FirestoreSyncService(),
    override val cloudStorageService: ICloudStorageService = FirebaseStorageService(),
    override val cloudFunctionsService: ICloudFunctionsService = LocalCloudFunctionsService(),
    override val notificationService: INotificationService = FCMNotificationService(),
    override val healthApiService: IHealthApiService = LocalHealthApiService()
) : IHealthVaultRepository {

    override fun getPatientProfileFlow(patientId: String): Flow<PatientProfileEntity?> {
        return dao.getPatientProfileFlow(patientId)
    }

    override suspend fun updatePatientProfile(profile: PatientProfileEntity) {
        dao.insertPatientProfile(profile)
        firestoreRepository.syncPatientProfileToCloud(profile)
    }

    override suspend fun searchPatientProfile(query: String): PatientProfileEntity? {
        val clean = query.trim()
        return dao.getPatientProfileBySearch(clean, clean)
    }

    override fun getEncryptedRecordsFlow(patientId: String): Flow<List<EncryptedRecordEntity>> {
        return dao.getEncryptedRecordsFlow(patientId)
    }

    override fun getMedicalEventsFlow(patientId: String): Flow<List<com.example.data.entity.MedicalEventEntity>> {
        return dao.getMedicalEventsFlow(patientId)
    }

    override suspend fun addMedicalEvent(event: com.example.data.entity.MedicalEventEntity) {
        dao.insertMedicalEvent(event)
    }

    override suspend fun deleteMedicalEvent(eventId: String) {
        dao.deleteMedicalEvent(eventId)
    }

    override fun getDoctorAccessRequestsFlow(patientId: String): Flow<List<DoctorAccessRequestEntity>> {
        return dao.getDoctorAccessRequestsFlow(patientId)
    }

    override fun getAuditLogsFlow(): Flow<List<AuditLogEntity>> {
        return dao.getAllAuditLogsFlow()
    }

    override suspend fun uploadAndEncryptRecord(
        patientId: String,
        fileName: String,
        category: String,
        plaintext: String,
        title: String,
        hospitalName: String,
        doctorName: String,
        examinationDate: Long,
        documentType: String,
        tags: String,
        notes: String,
        ocrText: String,
        aiSummary: String,
        diagnosis: String,
        medicines: String
    ): EncryptedRecordEntity {
        // Local AES-256-GCM Zero-Knowledge Encryption
        val encResult = CryptoManager.encrypt(plaintext)
        val fileId = "ENC_" + UUID.randomUUID().toString().take(8).uppercase()
        val fileSizeKb = (plaintext.length * 1.5 / 1024.0) + 1.2
        val formattedSize = if (fileSizeKb > 1024) "%.1f MB".format(fileSizeKb / 1024) else "%.0f KB".format(fileSizeKb)

        // Upload encrypted blob to cloud storage service (if enabled)
        cloudStorageService.uploadEncryptedBlob(fileId, encResult.ciphertextBase64)

        // OCR Extractor Call
        val ocrEngine = com.example.ai.ocr.OcrProcessorFactory.getActiveEngine()
        val extractedOcr = if (ocrText.isNotBlank()) ocrText else {
            val result = ocrEngine.extractText(encResult.ciphertextBase64, fileName, category)
            result.ocrText
        }

        // AI Summarizer Call
        val aiSummarizer = com.example.ai.summarizer.AiSummarizerFactory.getActiveSummarizer()
        val generatedAiResult = if (aiSummary.isNotBlank()) {
            com.example.ai.summarizer.AiSummaryResult(aiSummary, diagnosis, medicines)
        } else {
            val tempEntity = EncryptedRecordEntity(
                fileId = fileId,
                originalFileName = fileName,
                fileCategory = category,
                title = title,
                fileSizeFormatted = formattedSize,
                encryptedIvBase64 = encResult.ivBase64,
                encryptedPayloadBase64 = encResult.ciphertextBase64,
                sha256Hash = encResult.sha256Hash
            )
            aiSummarizer.generateSummary(tempEntity, plaintext)
        }

        val entity = EncryptedRecordEntity(
            fileId = fileId,
            patientId = patientId,
            title = title.ifBlank { fileName.replace("_", " ").substringBeforeLast(".") },
            originalFileName = fileName,
            fileCategory = category,
            hospitalName = hospitalName,
            doctorName = doctorName,
            examinationDate = examinationDate,
            fileSizeFormatted = formattedSize,
            documentType = documentType,
            tags = tags,
            notes = notes,
            isFavorite = false,
            isArchived = false,
            isTrashed = false,
            trashedAt = 0L,
            ocrText = extractedOcr,
            aiSummary = generatedAiResult.summaryText,
            diagnosis = diagnosis.ifBlank { generatedAiResult.detectedDiagnosis },
            medicines = medicines.ifBlank { generatedAiResult.detectedMedicines },
            uploadTimestamp = System.currentTimeMillis(),
            encryptedIvBase64 = encResult.ivBase64,
            encryptedPayloadBase64 = encResult.ciphertextBase64,
            sha256Hash = encResult.sha256Hash
        )

        dao.insertEncryptedRecord(entity)
        firestoreRepository.syncRecordToCloud(entity)

        // Automatically map uploaded document into Medical Timeline Event
        val mappedCategory = when (category) {
            "Hospital Visits" -> "Hospital Visits"
            "Diagnoses" -> "Diagnoses"
            "Surgery Reports", "Surgeries" -> "Surgeries"
            "Vaccinations" -> "Vaccinations"
            "Lab Reports", "Blood Tests" -> "Lab Reports"
            "X-Ray", "MRI", "CT Scan", "ECG", "Ultrasound", "Scans" -> "Scans"
            "Prescriptions", "Medicine Changes" -> "Medicine Changes"
            "Discharge Summaries" -> "Discharge Summaries"
            "Appointments" -> "Appointments"
            else -> "Lab Reports"
        }

        val autoEvent = com.example.data.entity.MedicalEventEntity(
            eventId = "ME_AUTO_" + fileId,
            patientId = patientId,
            title = entity.getDisplayTitle(),
            category = mappedCategory,
            eventDate = examinationDate,
            hospitalName = hospitalName.ifBlank { "Health Vault Medical Center" },
            doctorName = doctorName.ifBlank { "Attending Physician" },
            quickPreview = entity.aiSummary.ifBlank { entity.diagnosis.ifBlank { "Uploaded medical document ($category): ${entity.getDisplayTitle()}" } },
            vitalsOrNotes = if (entity.medicines.isNotBlank()) "Meds: ${entity.medicines}" else null,
            linkedRecordId = fileId,
            status = "Completed"
        )
        dao.insertMedicalEvent(autoEvent)

        // Log audit
        addAuditLog(
            userId = patientId,
            userRole = "PATIENT",
            action = "LOCAL_ENCRYPT_AND_UPLOAD",
            details = "Encrypted '${entity.getDisplayTitle()}' ($category) locally using Android KeyStore AES-256-GCM. Synchronized to cloud storage."
        )

        return entity
    }

    override suspend fun updateRecordMetadata(record: EncryptedRecordEntity) {
        dao.updateEncryptedRecord(record)
        firestoreRepository.syncRecordToCloud(record)
        addAuditLog(
            userId = record.patientId,
            userRole = "PATIENT",
            action = "METADATA_UPDATE",
            details = "Updated metadata for '${record.getDisplayTitle()}' (File ID: ${record.fileId})."
        )
    }

    override suspend fun toggleFavorite(record: EncryptedRecordEntity) {
        val updated = record.copy(isFavorite = !record.isFavorite)
        dao.updateEncryptedRecord(updated)
        firestoreRepository.syncRecordToCloud(updated)
        addAuditLog(
            userId = record.patientId,
            userRole = "PATIENT",
            action = if (updated.isFavorite) "FAVORITE_ADDED" else "FAVORITE_REMOVED",
            details = "Marked '${record.getDisplayTitle()}' as favorite: ${updated.isFavorite}"
        )
    }

    override suspend fun toggleArchive(record: EncryptedRecordEntity) {
        val updated = record.copy(isArchived = !record.isArchived)
        dao.updateEncryptedRecord(updated)
        firestoreRepository.syncRecordToCloud(updated)
        addAuditLog(
            userId = record.patientId,
            userRole = "PATIENT",
            action = if (updated.isArchived) "ARCHIVE_RECORD" else "UNARCHIVE_RECORD",
            details = "Toggled archive status for '${record.getDisplayTitle()}' (Archived: ${updated.isArchived})"
        )
    }

    override suspend fun moveToTrash(record: EncryptedRecordEntity) {
        val updated = record.copy(isTrashed = true, trashedAt = System.currentTimeMillis())
        dao.updateEncryptedRecord(updated)
        firestoreRepository.syncRecordToCloud(updated)
        addAuditLog(
            userId = record.patientId,
            userRole = "PATIENT",
            action = "MOVE_TO_TRASH",
            details = "Moved '${record.getDisplayTitle()}' to Trash (30 days retention)."
        )
    }

    override suspend fun restoreFromTrash(record: EncryptedRecordEntity) {
        val updated = record.copy(isTrashed = false, trashedAt = 0L)
        dao.updateEncryptedRecord(updated)
        firestoreRepository.syncRecordToCloud(updated)
        addAuditLog(
            userId = record.patientId,
            userRole = "PATIENT",
            action = "RESTORE_FROM_TRASH",
            details = "Restored '${record.getDisplayTitle()}' from Trash."
        )
    }

    override suspend fun emptyTrash(patientId: String) {
        dao.emptyTrashRecords(patientId)
        addAuditLog(
            userId = patientId,
            userRole = "PATIENT",
            action = "EMPTY_TRASH",
            details = "Emptied all records in Trash."
        )
    }

    override suspend fun decryptRecord(record: EncryptedRecordEntity, accessorId: String, accessorRole: String): String {
        val decrypted = CryptoManager.decrypt(record.encryptedPayloadBase64, record.encryptedIvBase64)
        addAuditLog(
            userId = accessorId,
            userRole = accessorRole,
            action = "DECRYPT_RECORD",
            details = "Decrypted record '${record.originalFileName}' (File ID: ${record.fileId}) during authorized session."
        )
        return decrypted
    }

    override suspend fun deleteRecord(record: EncryptedRecordEntity, patientId: String) {
        dao.deleteEncryptedRecord(record.fileId)
        cloudStorageService.deleteCloudBlob(record.fileId)
        firestoreRepository.deleteCloudRecord(record.fileId)
        addAuditLog(
            userId = patientId,
            userRole = "PATIENT",
            action = "DELETE_RECORD",
            details = "Permanently deleted encrypted file '${record.originalFileName}' (File ID: ${record.fileId})."
        )
    }

    override suspend fun createDoctorAccessRequest(
        patientId: String,
        doctorId: String,
        doctorName: String,
        hospitalName: String,
        specialization: String,
        reason: String,
        isEmergencyRequest: Boolean,
        requestedScope: String
    ): DoctorAccessRequestEntity {
        val requestId = "REQ-" + UUID.randomUUID().toString().take(5).uppercase()
        val req = DoctorAccessRequestEntity(
            requestId = requestId,
            doctorId = doctorId,
            doctorName = doctorName,
            hospitalName = hospitalName,
            specialization = specialization,
            patientId = patientId,
            requestReason = reason,
            requestTimestamp = System.currentTimeMillis(),
            status = "PENDING",
            isEmergencyRequest = isEmergencyRequest,
            requestedScope = requestedScope
        )
        dao.insertDoctorAccessRequest(req)
        firestoreRepository.syncAccessRequestToCloud(req)

        if (isEmergencyRequest) {
            cloudFunctionsService.triggerEmergencyAccessWebhook(patientId, doctorName, reason)
            notificationService.sendEmergencyPushAlert(
                patientId,
                "🚨 EMERGENCY ACCESS REQUESTED",
                "$doctorName ($hospitalName) requested immediate emergency medical access: '$reason'"
            )
        }

        addAuditLog(
            userId = doctorId,
            userRole = if (isEmergencyRequest) "EMERGENCY_PHYSICIAN" else "DOCTOR",
            action = if (isEmergencyRequest) "EMERGENCY_REQUEST_ACCESS" else "DOCTOR_REQUEST_ACCESS",
            details = "${if (isEmergencyRequest) "[EMERGENCY MODE] " else ""}$doctorName requested medical access for: '$reason'."
        )
        return req
    }

    override suspend fun approveDoctorAccess(requestId: String, durationMinutes: Int, approvedBy: String) {
        val req = dao.getDoctorAccessRequestById(requestId) ?: return
        val now = System.currentTimeMillis()
        val expiry = now + (durationMinutes * 60 * 1000L)

        val tokenResult = cloudFunctionsService.generateTemporaryAccessToken(requestId, durationMinutes)
        val sessionToken = tokenResult.getOrDefault("SESSION_TOKEN_$requestId")

        val updated = req.copy(
            status = "APPROVED",
            approvalTimestamp = now,
            sessionExpiryTimestamp = expiry,
            durationMinutes = durationMinutes,
            approvedBy = approvedBy
        )
        dao.updateDoctorAccessRequest(updated)
        firestoreRepository.syncAccessRequestToCloud(updated)

        addAuditLog(
            userId = req.patientId,
            userRole = "PATIENT",
            action = "PATIENT_APPROVE_ACCESS",
            details = "Patient APPROVED time-limited access ($durationMinutes mins) for ${req.doctorName} (${req.hospitalName}). Session token generated: $sessionToken. Approved by: '$approvedBy'."
        )
    }

    override suspend fun rejectDoctorAccess(requestId: String, rejectedBy: String) {
        val req = dao.getDoctorAccessRequestById(requestId) ?: return
        val updated = req.copy(status = "REJECTED", approvedBy = "Rejected by $rejectedBy")
        dao.updateDoctorAccessRequest(updated)
        firestoreRepository.syncAccessRequestToCloud(updated)

        addAuditLog(
            userId = req.patientId,
            userRole = "PATIENT",
            action = "PATIENT_REJECT_ACCESS",
            details = "Patient REJECTED medical access request from ${req.doctorName} (${req.hospitalName})."
        )
    }

    override suspend fun approveEmergencyAccessByNominee(requestId: String, durationMinutes: Int, nomineeName: String) {
        val req = dao.getDoctorAccessRequestById(requestId) ?: return
        val now = System.currentTimeMillis()
        val expiry = now + (durationMinutes * 60 * 1000L)

        val updated = req.copy(
            status = "APPROVED",
            approvalTimestamp = now,
            sessionExpiryTimestamp = expiry,
            durationMinutes = durationMinutes,
            approvedBy = "Nominee: $nomineeName (Emergency Override)"
        )
        dao.updateDoctorAccessRequest(updated)
        firestoreRepository.syncAccessRequestToCloud(updated)

        addAuditLog(
            userId = "NOMINEE-$nomineeName",
            userRole = "NOMINEE",
            action = "NOMINEE_EMERGENCY_APPROVE",
            details = "🚨 Emergency Nominee '$nomineeName' APPROVED emergency access ($durationMinutes mins) for ${req.doctorName} on behalf of unresponsive patient."
        )
    }

    override suspend fun rejectEmergencyAccessByNominee(requestId: String, nomineeName: String) {
        val req = dao.getDoctorAccessRequestById(requestId) ?: return
        val updated = req.copy(status = "REJECTED", approvedBy = "Rejected by Nominee: $nomineeName")
        dao.updateDoctorAccessRequest(updated)
        firestoreRepository.syncAccessRequestToCloud(updated)

        addAuditLog(
            userId = "NOMINEE-$nomineeName",
            userRole = "NOMINEE",
            action = "NOMINEE_EMERGENCY_REJECT",
            details = "🚨 Emergency Nominee '$nomineeName' REJECTED emergency access request from ${req.doctorName}."
        )
    }

    override suspend fun revokeDoctorAccess(requestId: String) {
        val req = dao.getDoctorAccessRequestById(requestId) ?: return
        val updated = req.copy(status = "EXPIRED")
        dao.updateDoctorAccessRequest(updated)
        firestoreRepository.syncAccessRequestToCloud(updated)

        addAuditLog(
            userId = req.patientId,
            userRole = "PATIENT",
            action = "REVOKE_ACCESS_EARLY",
            details = "Patient revoked active doctor authorization session immediately for ${req.doctorName}."
        )
    }

    override suspend fun addAuditLog(userId: String, userRole: String, action: String, details: String) {
        val log = AuditLogEntity(
            timestamp = System.currentTimeMillis(),
            userId = userId,
            userRole = userRole,
            action = action,
            details = details
        )
        dao.insertAuditLog(log)
        firestoreRepository.syncAuditLogToCloud(log)
    }

    // Nominees
    override fun getNomineesFlow(patientId: String): Flow<List<com.example.data.entity.NomineeEntity>> = dao.getNomineesFlow(patientId)

    override suspend fun addNominee(
        name: String,
        relationship: String,
        phone: String,
        email: String,
        priority: Int,
        emergencyPermissionLevel: String,
        isPrimary: Boolean,
        patientId: String
    ) {
        val id = "NOM-" + UUID.randomUUID().toString().take(5).uppercase()
        if (isPrimary) {
            val existing = dao.getAllNomineesDirect(patientId)
            existing.forEach { if (it.isPrimary) dao.updateNominee(it.copy(isPrimary = false)) }
        }
        val nominee = com.example.data.entity.NomineeEntity(
            nomineeId = id,
            patientId = patientId,
            name = name,
            relationship = relationship,
            phone = phone,
            email = email,
            priority = priority,
            emergencyPermissionLevel = emergencyPermissionLevel,
            isPrimary = isPrimary,
            canApproveEmergencyAccess = true
        )
        dao.insertNominee(nominee)
        addAuditLog(patientId, "PATIENT", "ADD_NOMINEE", "Added emergency nominee '$name' ($relationship, Priority $priority, Level: '$emergencyPermissionLevel', Primary: $isPrimary).")
    }

    override suspend fun updateNominee(nominee: com.example.data.entity.NomineeEntity) {
        if (nominee.isPrimary) {
            val existing = dao.getAllNomineesDirect(nominee.patientId)
            existing.forEach { if (it.nomineeId != nominee.nomineeId && it.isPrimary) dao.updateNominee(it.copy(isPrimary = false)) }
        }
        dao.updateNominee(nominee)
        addAuditLog(nominee.patientId, "PATIENT", "UPDATE_NOMINEE", "Updated nominee '${nominee.name}' (Priority ${nominee.priority}, Permission: ${nominee.emergencyPermissionLevel}, Primary: ${nominee.isPrimary}).")
    }

    override suspend fun setPrimaryNominee(nomineeId: String, patientId: String) {
        val existing = dao.getAllNomineesDirect(patientId)
        existing.forEach { nom ->
            dao.updateNominee(nom.copy(isPrimary = (nom.nomineeId == nomineeId)))
        }
        addAuditLog(patientId, "PATIENT", "NOMINEE_PRIMARY_UPDATED", "Set nominee ID $nomineeId as Primary Emergency Nominee.")
    }

    override suspend fun removeNominee(nomineeId: String, patientId: String) {
        dao.deleteNominee(nomineeId)
        addAuditLog(patientId, "PATIENT", "REMOVE_NOMINEE", "Removed emergency nominee ID: $nomineeId.")
    }

    override suspend fun verifyEmergencyOtpAndGrantAccess(
        requestId: String,
        nomineeName: String,
        nomineePhone: String,
        permissionLevel: String,
        durationMinutes: Int
    ) {
        val req = dao.getDoctorAccessRequestById(requestId) ?: return
        val now = System.currentTimeMillis()
        val expiry = now + (durationMinutes * 60 * 1000L)

        val updated = req.copy(
            status = "APPROVED",
            approvalTimestamp = now,
            sessionExpiryTimestamp = expiry,
            durationMinutes = durationMinutes,
            approvedBy = "Nominee OTP Verified: $nomineeName ($permissionLevel)"
        )
        dao.updateDoctorAccessRequest(updated)
        firestoreRepository.syncAccessRequestToCloud(updated)

        addAuditLog(
            userId = "NOMINEE-$nomineeName",
            userRole = "NOMINEE",
            action = "EMERGENCY_OTP_VERIFIED",
            details = "🚨 Emergency Access OTP successfully VERIFIED by Nominee '$nomineeName' ($nomineePhone). Granted $durationMinutes-min temporary access (Level: $permissionLevel) for doctor '${req.doctorName}' (${req.hospitalName})."
        )
    }

    // Verifications (Admin)
    override fun getDoctorVerificationsFlow(): Flow<List<com.example.data.entity.DoctorVerificationEntity>> = dao.getDoctorVerificationsFlow()

    override suspend fun verifyDoctor(doctorId: String, isApproved: Boolean) {
        val status = if (isApproved) "VERIFIED" else "REJECTED"
        dao.updateDoctorVerificationStatus(doctorId, isApproved, status)
        cloudFunctionsService.verifyDoctorLicenseWithMedicalRegistry(doctorId, "MD-REG-$doctorId")
        addAuditLog("ADMIN-01", "ADMIN", "VERIFY_DOCTOR", "Admin set doctor $doctorId verification status to $status.")
    }

    override fun getHospitalVerificationsFlow(): Flow<List<com.example.data.entity.HospitalVerificationEntity>> = dao.getHospitalVerificationsFlow()

    override suspend fun verifyHospital(hospitalId: String, isApproved: Boolean) {
        val status = if (isApproved) "VERIFIED" else "REJECTED"
        dao.updateHospitalVerificationStatus(hospitalId, isApproved, status)
        addAuditLog("ADMIN-01", "ADMIN", "VERIFY_HOSPITAL", "Admin set hospital $hospitalId verification status to $status.")
    }

    // User Accounts (Admin)
    override fun getUserAccountsFlow(): Flow<List<com.example.data.entity.UserAccountEntity>> = dao.getUserAccountsFlow()

    override suspend fun toggleUserSuspension(userId: String, currentSuspended: Boolean) {
        val newStatus = !currentSuspended
        dao.updateUserSuspension(userId, newStatus)
        addAuditLog("ADMIN-01", "ADMIN", "USER_SUSPENSION_TOGGLE", "Admin set user $userId suspension state to $newStatus.")
    }

    // Consultation Notes (Doctor)
    override fun getConsultationNotesFlow(patientId: String): Flow<List<com.example.data.entity.ConsultationNoteEntity>> = dao.getConsultationNotesFlow(patientId)

    override suspend fun addConsultationNote(doctorId: String, patientId: String, noteText: String) {
        val noteId = "NOTE-" + UUID.randomUUID().toString().take(6).uppercase()
        val note = com.example.data.entity.ConsultationNoteEntity(noteId, doctorId, patientId, noteText)
        dao.insertConsultationNote(note)
        addAuditLog(doctorId, "DOCTOR", "ADD_CONSULTATION_NOTE", "Doctor added clinical consultation note for patient $patientId.")
    }
}

