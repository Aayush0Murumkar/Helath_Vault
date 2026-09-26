package com.example.data.repository

import com.example.backend.api.IHealthApiService
import com.example.backend.auth.IAuthRepository
import com.example.backend.firestore.IFirestoreRepository
import com.example.backend.functions.ICloudFunctionsService
import com.example.backend.notifications.INotificationService
import com.example.backend.storage.ICloudStorageService
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ConsultationNoteEntity
import com.example.data.entity.DoctorAccessRequestEntity
import com.example.data.entity.DoctorVerificationEntity
import com.example.data.entity.EncryptedRecordEntity
import com.example.data.entity.HospitalVerificationEntity
import com.example.data.entity.MedicalEventEntity
import com.example.data.entity.NomineeEntity
import com.example.data.entity.PatientProfileEntity
import com.example.data.entity.UserAccountEntity
import kotlinx.coroutines.flow.Flow

/**
 * Clean Repository Interface contract for Health Vault.
 * Provides unified access to local database, authentication, cloud sync,
 * encrypted file storage, cloud functions, push notifications, and Gemini APIs.
 */
interface IHealthVaultRepository {

    // Backend Sub-Service References
    val authRepository: IAuthRepository
    val firestoreRepository: IFirestoreRepository
    val cloudStorageService: ICloudStorageService
    val cloudFunctionsService: ICloudFunctionsService
    val notificationService: INotificationService
    val healthApiService: IHealthApiService

    // Patient Profile
    fun getPatientProfileFlow(patientId: String = "PAT-98421"): Flow<PatientProfileEntity?>
    suspend fun updatePatientProfile(profile: PatientProfileEntity)
    suspend fun searchPatientProfile(query: String): PatientProfileEntity?

    // Encrypted Records
    fun getEncryptedRecordsFlow(patientId: String = "PAT-98421"): Flow<List<EncryptedRecordEntity>>
    suspend fun uploadAndEncryptRecord(
        patientId: String = "PAT-98421",
        fileName: String,
        category: String,
        plaintext: String,
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
    ): EncryptedRecordEntity

    suspend fun updateRecordMetadata(record: EncryptedRecordEntity)
    suspend fun toggleFavorite(record: EncryptedRecordEntity)
    suspend fun toggleArchive(record: EncryptedRecordEntity)
    suspend fun moveToTrash(record: EncryptedRecordEntity)
    suspend fun restoreFromTrash(record: EncryptedRecordEntity)
    suspend fun emptyTrash(patientId: String = "PAT-98421")
    suspend fun decryptRecord(record: EncryptedRecordEntity, accessorId: String, accessorRole: String): String
    suspend fun deleteRecord(record: EncryptedRecordEntity, patientId: String = "PAT-98421")

    // Medical Events & Timeline
    fun getMedicalEventsFlow(patientId: String = "PAT-98421"): Flow<List<MedicalEventEntity>>
    suspend fun addMedicalEvent(event: MedicalEventEntity)
    suspend fun deleteMedicalEvent(eventId: String)

    // Doctor Access Requests
    fun getDoctorAccessRequestsFlow(patientId: String = "PAT-98421"): Flow<List<DoctorAccessRequestEntity>>
    suspend fun createDoctorAccessRequest(
        patientId: String = "PAT-98421",
        doctorId: String = "DOC-4481",
        doctorName: String,
        hospitalName: String,
        specialization: String,
        reason: String,
        isEmergencyRequest: Boolean = false,
        requestedScope: String = "All Vault Medical Records"
    ): DoctorAccessRequestEntity

    suspend fun approveDoctorAccess(requestId: String, durationMinutes: Int = 30, approvedBy: String = "Patient: Sarah Jenkins")
    suspend fun rejectDoctorAccess(requestId: String, rejectedBy: String = "Patient: Sarah Jenkins")
    suspend fun approveEmergencyAccessByNominee(requestId: String, durationMinutes: Int = 60, nomineeName: String = "John Jenkins")
    suspend fun rejectEmergencyAccessByNominee(requestId: String, nomineeName: String = "John Jenkins")
    suspend fun revokeDoctorAccess(requestId: String)
    suspend fun verifyEmergencyOtpAndGrantAccess(
        requestId: String,
        nomineeName: String,
        nomineePhone: String,
        permissionLevel: String,
        durationMinutes: Int = 60
    )

    // Audit Logs
    fun getAuditLogsFlow(): Flow<List<AuditLogEntity>>
    suspend fun addAuditLog(userId: String, userRole: String, action: String, details: String)

    // Emergency Nominees
    fun getNomineesFlow(patientId: String = "PAT-98421"): Flow<List<NomineeEntity>>
    suspend fun addNominee(
        name: String,
        relationship: String,
        phone: String,
        email: String,
        priority: Int = 1,
        emergencyPermissionLevel: String = "Full Access",
        isPrimary: Boolean = false,
        patientId: String = "PAT-98421"
    )
    suspend fun updateNominee(nominee: NomineeEntity)
    suspend fun setPrimaryNominee(nomineeId: String, patientId: String = "PAT-98421")
    suspend fun removeNominee(nomineeId: String, patientId: String = "PAT-98421")

    // Verifications & Admin
    fun getDoctorVerificationsFlow(): Flow<List<DoctorVerificationEntity>>
    suspend fun verifyDoctor(doctorId: String, isApproved: Boolean)
    fun getHospitalVerificationsFlow(): Flow<List<HospitalVerificationEntity>>
    suspend fun verifyHospital(hospitalId: String, isApproved: Boolean)
    fun getUserAccountsFlow(): Flow<List<UserAccountEntity>>
    suspend fun toggleUserSuspension(userId: String, currentSuspended: Boolean)

    // Consultation Notes
    fun getConsultationNotesFlow(patientId: String = "PAT-98421"): Flow<List<ConsultationNoteEntity>>
    suspend fun addConsultationNote(doctorId: String, patientId: String, noteText: String)
}
