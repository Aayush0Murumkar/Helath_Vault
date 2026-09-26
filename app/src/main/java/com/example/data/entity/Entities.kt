package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_profiles")
data class PatientProfileEntity(
    @PrimaryKey val id: String = "PAT-98421",
    // 1. Personal Information
    val name: String = "Sarah Jenkins",
    val dateOfBirth: String = "1992-05-14",
    val age: Int = 34,
    val gender: String = "Female",
    val bloodGroup: String = "O+",
    val heightCm: String = "168 cm",
    val weightKg: String = "62 kg",
    val bmi: String = "22.0 (Normal)",
    val mobileNumber: String = "+1 555-019-2831",
    val email: String = "sarah.jenkins@example.com",
    val address: String = "742 Evergreen Terrace, Springfield, OR 97477",
    val aadhaarNumber: String = "XXXX-XXXX-9842",
    val emergencyNotes: String = "Patient carries Epipen for severe peanut allergy. T1D Insulin dependent.",
    val isOrganDonor: Boolean = true,
    val disabilityStatus: String = "None",

    // Insurance Information
    val insuranceProvider: String = "Blue Cross Blue Shield Health",
    val insurancePolicyNumber: String = "POL-88392019",
    val insuranceCoverageDetails: String = "Comprehensive Inpatient & Outpatient ($500,000 limit)",
    val insuranceExpiryDate: String = "2027-12-31",

    // 2. Medical Information
    val allergies: String = "Penicillin, Peanuts (Severe)",
    val chronicDiseases: String = "Type 1 Diabetes, Mild Asthma",
    val currentMedications: String = "Lantus Solostar 20U Daily, Ventolin HFA Inhaler PRN",
    val previousSurgeries: String = "Appendectomy (2018), Wisdom Teeth Removal (2014)",
    val familyMedicalHistory: String = "Father: Hypertension; Mother: Type 2 Diabetes",
    val vaccinationHistory: String = "COVID-19 Booster (2025), Tdap (2023), Influenza (2025)",
    val lifestyleHabits: String = "Non-smoker, Social Alcohol (1-2 drinks/wk), Moderate Exercise",
    val primaryPhysician: String = "Dr. Marcus Vance, MD (Metropolitan General Hospital)",

    // 3. Emergency Information
    val emergencyContact: String = "John Jenkins (+1 555-014-9981)",
    val nomineeName: String = "John Jenkins",
    val nomineeContact: String = "+1 555-014-9981",
    val emergencyInstructions: String = "In case of severe hypoglycemia, administer Oral Glucose Gel / Glucagon. Contact John Jenkins immediately.",
    val preferredHospital: String = "Metropolitan General Emergency Center, City Center",

    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "encrypted_records")
data class EncryptedRecordEntity(
    @PrimaryKey val fileId: String,
    val patientId: String = "PAT-98421",
    val title: String = "",
    val originalFileName: String,
    val fileCategory: String, // "Prescriptions", "Lab Reports", "Blood Tests", "X-Ray", "MRI", "CT Scan", "ECG", "Ultrasound", "Vaccinations", "Surgery Reports", "Discharge Summaries", "Insurance Documents", "Bills", "Other"
    val hospitalName: String = "",
    val doctorName: String = "",
    val examinationDate: Long = System.currentTimeMillis(),
    val fileSizeFormatted: String,
    val documentType: String = "PDF Document",
    val tags: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAt: Long = 0L,
    val ocrText: String = "",
    val aiSummary: String = "",
    val diagnosis: String = "",
    val medicines: String = "",
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val encryptedIvBase64: String,
    val encryptedPayloadBase64: String,
    val sha256Hash: String,
    val notesEncryptedBase64: String? = null,
    val notesIvBase64: String? = null
) {
    fun getDisplayTitle(): String = if (title.isNotBlank()) title else originalFileName.replace("_", " ").substringBeforeLast(".")
}

@Entity(tableName = "doctor_access_requests")
data class DoctorAccessRequestEntity(
    @PrimaryKey val requestId: String,
    val doctorId: String = "DOC-4481",
    val doctorName: String = "Dr. Marcus Vance, MD",
    val hospitalName: String = "Metropolitan General Hospital",
    val specialization: String = "Cardiology & Internal Medicine",
    val patientId: String = "PAT-98421",
    val requestReason: String = "Comprehensive Diagnostics & Lab Review",
    val requestTimestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, EXPIRED
    val approvalTimestamp: Long? = null,
    val sessionExpiryTimestamp: Long? = null,
    val durationMinutes: Int = 30,
    val isEmergencyRequest: Boolean = false,
    val approvedBy: String = "",
    val requestedScope: String = "All Vault Medical Records"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val userRole: String, // PATIENT, DOCTOR, ADMIN
    val action: String,
    val details: String,
    val ipAddress: String = "192.168.1.104",
    val deviceId: String = "Pixel 8 Pro (Hardware Key)"
)

@Entity(tableName = "nominees")
data class NomineeEntity(
    @PrimaryKey val nomineeId: String,
    val patientId: String = "PAT-98421",
    val name: String,
    val relationship: String,
    val phone: String,
    val email: String,
    val priority: Int = 1,
    val emergencyPermissionLevel: String = "Full Access", // "Full Access", "Emergency Summary Only", "Critical Vitals & Allergies"
    val isPrimary: Boolean = false,
    val canApproveEmergencyAccess: Boolean = true
)

@Entity(tableName = "doctor_verifications")
data class DoctorVerificationEntity(
    @PrimaryKey val doctorId: String,
    val doctorName: String,
    val licenseNumber: String,
    val hospitalName: String,
    val specialization: String,
    val isVerified: Boolean = false,
    val status: String = "PENDING" // PENDING, VERIFIED, REJECTED
)

@Entity(tableName = "hospital_verifications")
data class HospitalVerificationEntity(
    @PrimaryKey val hospitalId: String,
    val name: String,
    val registrationNo: String,
    val location: String,
    val isVerified: Boolean = false,
    val status: String = "PENDING" // PENDING, VERIFIED, REJECTED
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val role: String, // PATIENT, DOCTOR, ADMIN
    val email: String,
    val isSuspended: Boolean = false,
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "consultation_notes")
data class ConsultationNoteEntity(
    @PrimaryKey val noteId: String,
    val doctorId: String,
    val patientId: String,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medical_timeline_events")
data class MedicalEventEntity(
    @PrimaryKey val eventId: String,
    val patientId: String = "PAT-98421",
    val title: String,
    val category: String, // "Hospital Visits", "Diagnoses", "Surgeries", "Vaccinations", "Lab Reports", "Scans", "Medicine Changes", "Discharge Summaries", "Appointments"
    val eventDate: Long,
    val hospitalName: String,
    val doctorName: String,
    val quickPreview: String,
    val vitalsOrNotes: String? = null,
    val linkedRecordId: String? = null,
    val status: String = "Completed", // "Completed", "Upcoming", "Follow-up Scheduled"
    val timestampAdded: Long = System.currentTimeMillis()
)

