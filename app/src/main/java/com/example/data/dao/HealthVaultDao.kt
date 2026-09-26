package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.DoctorAccessRequestEntity
import com.example.data.entity.EncryptedRecordEntity
import com.example.data.entity.PatientProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthVaultDao {

    // Patient Profile
    @Query("SELECT * FROM patient_profiles WHERE id = :patientId LIMIT 1")
    fun getPatientProfileFlow(patientId: String): Flow<PatientProfileEntity?>

    @Query("SELECT * FROM patient_profiles WHERE id = :patientId OR mobileNumber = :query LIMIT 1")
    suspend fun getPatientProfileBySearch(patientId: String, query: String): PatientProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatientProfile(profile: PatientProfileEntity)

    // Encrypted Records
    @Query("SELECT * FROM encrypted_records WHERE patientId = :patientId ORDER BY uploadTimestamp DESC")
    fun getEncryptedRecordsFlow(patientId: String): Flow<List<EncryptedRecordEntity>>

    @Query("SELECT * FROM encrypted_records WHERE patientId = :patientId ORDER BY uploadTimestamp DESC")
    suspend fun getEncryptedRecordsDirect(patientId: String = "PAT-98421"): List<EncryptedRecordEntity>

    @Query("SELECT * FROM encrypted_records WHERE fileId = :fileId LIMIT 1")
    suspend fun getEncryptedRecordById(fileId: String): EncryptedRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncryptedRecord(record: EncryptedRecordEntity)

    @Update
    suspend fun updateEncryptedRecord(record: EncryptedRecordEntity)

    @Query("DELETE FROM encrypted_records WHERE fileId = :fileId")
    suspend fun deleteEncryptedRecord(fileId: String)

    @Query("DELETE FROM encrypted_records WHERE isTrashed = 1 AND patientId = :patientId")
    suspend fun emptyTrashRecords(patientId: String = "PAT-98421")

    // Doctor Access Requests
    @Query("SELECT * FROM doctor_access_requests WHERE patientId = :patientId ORDER BY requestTimestamp DESC")
    fun getDoctorAccessRequestsFlow(patientId: String): Flow<List<DoctorAccessRequestEntity>>

    @Query("SELECT * FROM doctor_access_requests WHERE patientId = :patientId ORDER BY requestTimestamp DESC")
    suspend fun getDoctorAccessRequestsDirect(patientId: String = "PAT-98421"): List<DoctorAccessRequestEntity>

    @Query("SELECT * FROM doctor_access_requests WHERE requestId = :requestId LIMIT 1")
    suspend fun getDoctorAccessRequestById(requestId: String): DoctorAccessRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorAccessRequest(request: DoctorAccessRequestEntity)

    @Update
    suspend fun updateDoctorAccessRequest(request: DoctorAccessRequestEntity)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Nominees
    @Query("SELECT * FROM nominees WHERE patientId = :patientId ORDER BY priority ASC")
    fun getNomineesFlow(patientId: String): Flow<List<com.example.data.entity.NomineeEntity>>

    @Query("SELECT * FROM nominees WHERE patientId = :patientId ORDER BY priority ASC")
    suspend fun getAllNomineesDirect(patientId: String = "PAT-98421"): List<com.example.data.entity.NomineeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNominee(nominee: com.example.data.entity.NomineeEntity)

    @Update
    suspend fun updateNominee(nominee: com.example.data.entity.NomineeEntity)

    @Query("DELETE FROM nominees WHERE nomineeId = :nomineeId")
    suspend fun deleteNominee(nomineeId: String)

    // Doctor Verifications
    @Query("SELECT * FROM doctor_verifications ORDER BY isVerified ASC")
    fun getDoctorVerificationsFlow(): Flow<List<com.example.data.entity.DoctorVerificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorVerification(doc: com.example.data.entity.DoctorVerificationEntity)

    @Query("UPDATE doctor_verifications SET isVerified = :verified, status = :status WHERE doctorId = :doctorId")
    suspend fun updateDoctorVerificationStatus(doctorId: String, verified: Boolean, status: String)

    // Hospital Verifications
    @Query("SELECT * FROM hospital_verifications ORDER BY isVerified ASC")
    fun getHospitalVerificationsFlow(): Flow<List<com.example.data.entity.HospitalVerificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospitalVerification(hosp: com.example.data.entity.HospitalVerificationEntity)

    @Query("UPDATE hospital_verifications SET isVerified = :verified, status = :status WHERE hospitalId = :hospitalId")
    suspend fun updateHospitalVerificationStatus(hospitalId: String, verified: Boolean, status: String)

    // User Accounts
    @Query("SELECT * FROM user_accounts ORDER BY registeredAt DESC")
    fun getUserAccountsFlow(): Flow<List<com.example.data.entity.UserAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: com.example.data.entity.UserAccountEntity)

    @Query("UPDATE user_accounts SET isSuspended = :suspended WHERE userId = :userId")
    suspend fun updateUserSuspension(userId: String, suspended: Boolean)

    // Consultation Notes
    @Query("SELECT * FROM consultation_notes WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getConsultationNotesFlow(patientId: String): Flow<List<com.example.data.entity.ConsultationNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultationNote(note: com.example.data.entity.ConsultationNoteEntity)

    // Medical Timeline Events
    @Query("SELECT * FROM medical_timeline_events WHERE patientId = :patientId ORDER BY eventDate DESC")
    fun getMedicalEventsFlow(patientId: String = "PAT-98421"): Flow<List<com.example.data.entity.MedicalEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalEvent(event: com.example.data.entity.MedicalEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalEvents(events: List<com.example.data.entity.MedicalEventEntity>)

    @Query("DELETE FROM medical_timeline_events WHERE eventId = :eventId")
    suspend fun deleteMedicalEvent(eventId: String)
}

