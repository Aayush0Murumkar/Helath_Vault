package com.example.backend.firestore

import com.example.data.entity.AuditLogEntity
import com.example.data.entity.DoctorAccessRequestEntity
import com.example.data.entity.EncryptedRecordEntity
import com.example.data.entity.PatientProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface for Cloud Firestore Database Synchronization.
 * Supports zero-knowledge encrypted document sync, doctor access requests, and audit trails.
 */
interface IFirestoreRepository {
    suspend fun syncRecordToCloud(record: EncryptedRecordEntity): Result<String>
    suspend fun fetchCloudRecords(patientId: String): Result<List<EncryptedRecordEntity>>
    suspend fun deleteCloudRecord(fileId: String): Result<Boolean>
    suspend fun syncAccessRequestToCloud(request: DoctorAccessRequestEntity): Result<Boolean>
    suspend fun fetchAccessRequestsFromCloud(patientId: String): Result<List<DoctorAccessRequestEntity>>
    fun listenToLiveAccessRequests(patientId: String): Flow<List<DoctorAccessRequestEntity>>
    suspend fun syncAuditLogToCloud(log: AuditLogEntity): Result<Boolean>
    suspend fun syncPatientProfileToCloud(profile: PatientProfileEntity): Result<Boolean>
}
