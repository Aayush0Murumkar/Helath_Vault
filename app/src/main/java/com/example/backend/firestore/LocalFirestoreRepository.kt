package com.example.backend.firestore

import com.example.data.dao.HealthVaultDao
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.DoctorAccessRequestEntity
import com.example.data.entity.EncryptedRecordEntity
import com.example.data.entity.PatientProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Local offline implementation of [IFirestoreRepository].
 * Delegates to local Room DAO as local cache / database while app is offline.
 */
class LocalFirestoreRepository(private val dao: HealthVaultDao) : IFirestoreRepository {

    override suspend fun syncRecordToCloud(record: EncryptedRecordEntity): Result<String> {
        dao.insertEncryptedRecord(record)
        return Result.success("LOCAL_DB_SYNC_OK_${record.fileId}")
    }

    override suspend fun fetchCloudRecords(patientId: String): Result<List<EncryptedRecordEntity>> {
        val list = dao.getEncryptedRecordsDirect(patientId)
        return Result.success(list)
    }

    override suspend fun deleteCloudRecord(fileId: String): Result<Boolean> {
        dao.deleteEncryptedRecord(fileId)
        return Result.success(true)
    }

    override suspend fun syncAccessRequestToCloud(request: DoctorAccessRequestEntity): Result<Boolean> {
        dao.insertDoctorAccessRequest(request)
        return Result.success(true)
    }

    override suspend fun fetchAccessRequestsFromCloud(patientId: String): Result<List<DoctorAccessRequestEntity>> {
        val list = dao.getDoctorAccessRequestsDirect(patientId)
        return Result.success(list)
    }

    override fun listenToLiveAccessRequests(patientId: String): Flow<List<DoctorAccessRequestEntity>> {
        return dao.getDoctorAccessRequestsFlow(patientId)
    }

    override suspend fun syncAuditLogToCloud(log: AuditLogEntity): Result<Boolean> {
        dao.insertAuditLog(log)
        return Result.success(true)
    }

    override suspend fun syncPatientProfileToCloud(profile: PatientProfileEntity): Result<Boolean> {
        dao.insertPatientProfile(profile)
        return Result.success(true)
    }
}
