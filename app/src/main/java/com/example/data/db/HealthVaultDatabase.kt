package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.crypto.CryptoManager
import com.example.data.dao.HealthVaultDao
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PatientProfileEntity::class,
        EncryptedRecordEntity::class,
        DoctorAccessRequestEntity::class,
        AuditLogEntity::class,
        NomineeEntity::class,
        DoctorVerificationEntity::class,
        HospitalVerificationEntity::class,
        UserAccountEntity::class,
        ConsultationNoteEntity::class,
        MedicalEventEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class HealthVaultDatabase : RoomDatabase() {

    abstract fun healthVaultDao(): HealthVaultDao

    companion object {
        @Volatile
        private var INSTANCE: HealthVaultDatabase? = null

        fun getDatabase(context: Context): HealthVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthVaultDatabase::class.java,
                    "health_vault_zero_knowledge_v7.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        prepopulateDatabase(database.healthVaultDao())
                    }
                }
            }
        }

        private suspend fun prepopulateDatabase(dao: HealthVaultDao) {
            // Patient Profile
            val defaultPatient = PatientProfileEntity(
                id = "PAT-98421",
                name = "Sarah Jenkins",
                dateOfBirth = "1992-05-14",
                age = 34,
                gender = "Female",
                bloodGroup = "O+",
                heightCm = "168 cm",
                weightKg = "62 kg",
                bmi = "22.0 (Normal)",
                mobileNumber = "+1 555-019-2831",
                email = "sarah.jenkins@example.com",
                address = "742 Evergreen Terrace, Springfield, OR 97477",
                aadhaarNumber = "XXXX-XXXX-9842",
                emergencyNotes = "Patient carries Epipen for severe peanut allergy. T1D Insulin dependent.",
                isOrganDonor = true,
                disabilityStatus = "None",
                insuranceProvider = "Blue Cross Blue Shield Health",
                insurancePolicyNumber = "POL-88392019",
                insuranceCoverageDetails = "Comprehensive Inpatient & Outpatient ($500,000 limit)",
                insuranceExpiryDate = "2027-12-31",
                allergies = "Penicillin, Peanuts (Severe)",
                chronicDiseases = "Type 1 Diabetes, Mild Asthma",
                currentMedications = "Lantus Solostar 20U Daily, Ventolin HFA Inhaler PRN",
                previousSurgeries = "Appendectomy (2018), Wisdom Teeth Removal (2014)",
                familyMedicalHistory = "Father: Hypertension; Mother: Type 2 Diabetes",
                vaccinationHistory = "COVID-19 Booster (2025), Tdap (2023), Influenza (2025)",
                lifestyleHabits = "Non-smoker, Social Alcohol (1-2 drinks/wk), Moderate Exercise",
                primaryPhysician = "Dr. Marcus Vance, MD (Metropolitan General Hospital)",
                emergencyContact = "John Jenkins (+1 555-014-9981)",
                nomineeName = "John Jenkins",
                nomineeContact = "+1 555-014-9981",
                emergencyInstructions = "In case of severe hypoglycemia, administer Oral Glucose Gel / Glucagon. Contact John Jenkins immediately.",
                preferredHospital = "Metropolitan General Emergency Center, City Center"
            )
            dao.insertPatientProfile(defaultPatient)

            // Pre-Encrypt 3 realistic medical documents locally with AES-256-GCM
            val now = System.currentTimeMillis()

            val doc1Plaintext = """
                HEALTH VAULT MEDICAL LAB REPORT
                =========================================
                Patient: Sarah Jenkins (PAT-98421)
                Date: June 15, 2026
                Facility: Bay Diagnostic Center
                Test: Complete Blood Count & Metabolic Panel
                
                RESULTS:
                - Hemoglobin: 13.8 g/dL [Normal 12.0-15.5]
                - Fasting Blood Glucose: 112 mg/dL [Slightly Elevated - Monitored]
                - HbA1c: 6.4% [Controlled Type 1 Diabetes]
                - Total Cholesterol: 182 mg/dL [Optimal]
                - Serum Creatinine: 0.9 mg/dL [Normal]
                
                PHYSICIAN SUMMARY:
                Metabolic panel indicates excellent glycemic control under current insulin therapy.
                Maintain current dietary regimen. Re-test recommended in 90 days.
            """.trimIndent()

            val doc2Plaintext = """
                CARDIOLOGY ECHOCARDIOGRAM REPORT
                =========================================
                Patient: Sarah Jenkins (PAT-98421)
                Date: May 02, 2026
                Facility: Heart Health Clinic
                Attending: Dr. Marcus Vance, MD
                
                FINDINGS:
                - LVEF: 62% (Preserved Systolic Function)
                - Valvular Function: Mild mitral valve regurgitation, hemodynamically insignificant.
                - Wall Motion: Normal global left ventricular contractility.
                
                RECOMMENDATIONS:
                No acute intervention required. Follow-up echocardiogram in 12 months.
            """.trimIndent()

            val doc3Plaintext = """
                SPECIALIST PRESCRIPTION & CARE PLAN
                =========================================
                Patient: Sarah Jenkins (PAT-98421)
                Date: July 10, 2026
                Rx Ref: RX-2026-99081
                
                MEDICATIONS:
                1. Insulin Glargine (Basal) - 18 Units SC daily at bedtime.
                2. Insulin Lispro (Prandial) - 4-6 Units SC before meals as per carb ratio.
                3. Albuterol HFA Inhaler - 2 puffs as needed for exercise-induced bronchospasm.
                
                CONTRAINDICATIONS:
                - SEVERE ALLERGY: Penicillin derivatives (Anaphylaxis risk)
            """.trimIndent()

            val doc4Plaintext = """
                COVID-19 & INFLUENZA VACCINATION CERTIFICATE
                =========================================
                Patient: Sarah Jenkins (PAT-98421)
                Date: October 12, 2025
                Facility: City Health Clinic & Vaccine Center
                Provider: Nurse Rebecca Torres, RN
                
                VACCINES ADMINISTERED:
                1. Bivalent mRNA Covid-19 Booster - Lot #CV-90218
                2. Quadrivalent Influenza Vaccine - Lot #FL-44019
                
                NEXT DUE: October 2026
            """.trimIndent()

            val enc1 = CryptoManager.encrypt(doc1Plaintext)
            val enc2 = CryptoManager.encrypt(doc2Plaintext)
            val enc3 = CryptoManager.encrypt(doc3Plaintext)
            val enc4 = CryptoManager.encrypt(doc4Plaintext)

            val rec1 = EncryptedRecordEntity(
                fileId = "ENC_9FA7C21D",
                patientId = "PAT-98421",
                title = "Comprehensive Blood Panel",
                originalFileName = "Comprehensive_Blood_Panel_June2026.pdf",
                fileCategory = "Blood Tests",
                hospitalName = "Bay Diagnostic Center",
                doctorName = "Dr. Elena Rostova, MD",
                examinationDate = now - 86400000 * 5,
                fileSizeFormatted = "1.4 MB",
                documentType = "Lab Report",
                tags = "Blood, CBC, HbA1c, Diabetes",
                notes = "Routine quarterly metabolic check. Fasting glucose slightly elevated at 112 mg/dL.",
                isFavorite = true,
                ocrText = "Hemoglobin 13.8 g/dL, Fasting Blood Glucose 112 mg/dL, HbA1c 6.4%, Total Cholesterol 182 mg/dL, Serum Creatinine 0.9 mg/dL. Metabolic panel indicates excellent glycemic control under current insulin therapy.",
                aiSummary = "[AI Clinical Summary]\n• Glycemic Control: Fasting blood glucose is 112 mg/dL with HbA1c at 6.4%, showing effective Type 1 Diabetes management.\n• Lipid Profile: Total cholesterol 182 mg/dL (Optimal).\n• Next Action: Re-test metabolic panel in 90 days.",
                diagnosis = "Type 1 Diabetes, Hyperglycemia (Mild)",
                medicines = "Insulin Glargine, Insulin Lispro",
                uploadTimestamp = now - 86400000 * 4,
                encryptedIvBase64 = enc1.ivBase64,
                encryptedPayloadBase64 = enc1.ciphertextBase64,
                sha256Hash = enc1.sha256Hash
            )

            val rec2 = EncryptedRecordEntity(
                fileId = "ENC_8B2A10FF",
                patientId = "PAT-98421",
                title = "Cardiology Echocardiogram Scan",
                originalFileName = "Echocardiogram_Diagnostics_May2026.pdf",
                fileCategory = "MRI",
                hospitalName = "Heart Health Clinic",
                doctorName = "Dr. Marcus Vance, MD",
                examinationDate = now - 86400000 * 20,
                fileSizeFormatted = "4.2 MB",
                documentType = "Diagnostic Imaging",
                tags = "Cardiology, Echo, Heart, LVEF",
                notes = "Annual cardiac ultrasound. LVEF 62% with preserved systolic function.",
                isFavorite = false,
                ocrText = "LVEF 62% Preserved Systolic Function. Valvular Function: Mild mitral valve regurgitation, hemodynamically insignificant. Wall Motion: Normal global left ventricular contractility.",
                aiSummary = "[AI Clinical Summary]\n• Cardiac Function: LVEF 62% with preserved systolic function.\n• Valvular Assessment: Mild mitral regurgitation, clinically insignificant.\n• Plan: Follow-up echo in 12 months.",
                diagnosis = "Mitral Regurgitation (Mild)",
                medicines = "None",
                uploadTimestamp = now - 86400000 * 18,
                encryptedIvBase64 = enc2.ivBase64,
                encryptedPayloadBase64 = enc2.ciphertextBase64,
                sha256Hash = enc2.sha256Hash
            )

            val rec3 = EncryptedRecordEntity(
                fileId = "ENC_3C91EE04",
                patientId = "PAT-98421",
                title = "Endocrinology Rx Care Plan",
                originalFileName = "Endocrinology_Prescription_July2026.pdf",
                fileCategory = "Prescriptions",
                hospitalName = "Metropolitan General Hospital",
                doctorName = "Dr. Marcus Vance, MD",
                examinationDate = now - 86400000 * 2,
                fileSizeFormatted = "680 KB",
                documentType = "Prescription",
                tags = "Prescription, Insulin, Diabetes, Asthma",
                notes = "Refill for Insulin Glargine and Lispro. Penicillin allergy highlighted.",
                isFavorite = true,
                ocrText = "Insulin Glargine 18 Units SC daily at bedtime. Insulin Lispro 4-6 Units SC before meals. Albuterol HFA Inhaler 2 puffs PRN. Severe Penicillin Allergy.",
                aiSummary = "[AI Clinical Summary]\n• Active Prescriptions: Basal Insulin Glargine (18 Units SC), Prandial Insulin Lispro (4-6 Units SC), Albuterol HFA Inhaler (2 puffs PRN).\n• High Alert: Severe Penicillin Anaphylaxis risk.",
                diagnosis = "Type 1 Diabetes, Asthma",
                medicines = "Insulin Glargine, Insulin Lispro, Albuterol HFA",
                uploadTimestamp = now - 86400000 * 1,
                encryptedIvBase64 = enc3.ivBase64,
                encryptedPayloadBase64 = enc3.ciphertextBase64,
                sha256Hash = enc3.sha256Hash
            )

            val rec4 = EncryptedRecordEntity(
                fileId = "ENC_4D02BB12",
                patientId = "PAT-98421",
                title = "Annual Vaccination Record",
                originalFileName = "Vaccination_Certificate_2025.pdf",
                fileCategory = "Vaccinations",
                hospitalName = "City Health Clinic",
                doctorName = "Nurse Rebecca Torres, RN",
                examinationDate = now - 86400000 * 290,
                fileSizeFormatted = "920 KB",
                documentType = "Immunization Record",
                tags = "Vaccine, Covid-19, Influenza, Prevention",
                notes = "Boosters administered. No adverse reactions observed.",
                isFavorite = false,
                ocrText = "COVID-19 mRNA Booster Lot CV-90218, Quadrivalent Influenza Vaccine Lot FL-44019. Next booster due October 2026.",
                aiSummary = "[AI Clinical Summary]\n• Immunization Status: Up-to-date for COVID-19 mRNA Booster & Quadrivalent Influenza Vaccine.\n• Reminders: Next annual booster due in October 2026.",
                diagnosis = "Immunization / Prophylaxis",
                medicines = "COVID-19 Vaccine, Influenza Vaccine",
                uploadTimestamp = now - 86400000 * 280,
                encryptedIvBase64 = enc4.ivBase64,
                encryptedPayloadBase64 = enc4.ciphertextBase64,
                sha256Hash = enc4.sha256Hash
            )

            dao.insertEncryptedRecord(rec1)
            dao.insertEncryptedRecord(rec2)
            dao.insertEncryptedRecord(rec3)
            dao.insertEncryptedRecord(rec4)

            // Pending Doctor Request
            val docRequest = DoctorAccessRequestEntity(
                requestId = "REQ-88021",
                doctorId = "DOC-4481",
                doctorName = "Dr. Marcus Vance, MD",
                hospitalName = "Metropolitan General Hospital",
                specialization = "Cardiology & Internal Medicine",
                patientId = "PAT-98421",
                requestReason = "Quarterly Diabetic & Cardiac Diagnostic Review",
                requestTimestamp = now - 3600000,
                status = "PENDING"
            )
            dao.insertDoctorAccessRequest(docRequest)

            // Audit Logs
            val initialLogs = listOf(
                AuditLogEntity(
                    timestamp = now - 86400000 * 4,
                    userId = "PAT-98421",
                    userRole = "PATIENT",
                    action = "LOCAL_ENCRYPT_AND_UPLOAD",
                    details = "Encrypted 'Comprehensive_Blood_Panel_June2026.pdf' using Android KeyStore AES-256-GCM. Ciphertext stored."
                ),
                AuditLogEntity(
                    timestamp = now - 3600000,
                    userId = "DOC-4481",
                    userRole = "DOCTOR",
                    action = "DOCTOR_REQUEST_ACCESS",
                    details = "Dr. Marcus Vance requested 30-min time-limited access for 'Quarterly Diabetic & Cardiac Diagnostic Review'."
                ),
                AuditLogEntity(
                    timestamp = now - 1800000,
                    userId = "ADMIN-01",
                    userRole = "ADMIN",
                    action = "INSPECT_METADATA_ONLY",
                    details = "System Admin inspected technical metadata for file 'ENC_9FA7C21D'. Payload content blocked by Zero-Knowledge policy."
                )
            )

            initialLogs.forEach { dao.insertAuditLog(it) }

            // Prepopulate Nominees
            val defaultNominee = com.example.data.entity.NomineeEntity(
                nomineeId = "NOM-001",
                patientId = "PAT-98421",
                name = "John Jenkins",
                relationship = "Spouse",
                phone = "+1 555-014-9981",
                email = "john.jenkins@email.com",
                priority = 1,
                emergencyPermissionLevel = "Full Access",
                isPrimary = true,
                canApproveEmergencyAccess = true
            )
            val secondaryNominee = com.example.data.entity.NomineeEntity(
                nomineeId = "NOM-002",
                patientId = "PAT-98421",
                name = "Emily Jenkins",
                relationship = "Sister",
                phone = "+1 555-019-3321",
                email = "emily.jenkins@email.com",
                priority = 2,
                emergencyPermissionLevel = "Emergency Summary Only",
                isPrimary = false,
                canApproveEmergencyAccess = true
            )
            dao.insertNominee(defaultNominee)
            dao.insertNominee(secondaryNominee)

            // Prepopulate Doctor Verifications
            val docVerif1 = com.example.data.entity.DoctorVerificationEntity(
                doctorId = "DOC-4481",
                doctorName = "Dr. Marcus Vance, MD",
                licenseNumber = "MD-NY-982310",
                hospitalName = "Metropolitan General Hospital",
                specialization = "Cardiology & Internal Medicine",
                isVerified = true,
                status = "VERIFIED"
            )
            val docVerif2 = com.example.data.entity.DoctorVerificationEntity(
                doctorId = "DOC-7712",
                doctorName = "Dr. Elena Rostova, MD",
                licenseNumber = "MD-CA-441290",
                hospitalName = "St. Jude Children & Family Hospital",
                specialization = "Endocrinology & Diabetology",
                isVerified = false,
                status = "PENDING"
            )
            dao.insertDoctorVerification(docVerif1)
            dao.insertDoctorVerification(docVerif2)

            // Prepopulate Hospital Verifications
            val hospVerif1 = com.example.data.entity.HospitalVerificationEntity(
                hospitalId = "HOSP-METRO-01",
                name = "Metropolitan General Hospital",
                registrationNo = "REG-HOSP-99120",
                location = "New York, NY",
                isVerified = true,
                status = "VERIFIED"
            )
            val hospVerif2 = com.example.data.entity.HospitalVerificationEntity(
                hospitalId = "HOSP-BAY-02",
                name = "Bay Area Medical Center",
                registrationNo = "REG-HOSP-10492",
                location = "San Francisco, CA",
                isVerified = false,
                status = "PENDING"
            )
            dao.insertHospitalVerification(hospVerif1)
            dao.insertHospitalVerification(hospVerif2)

            // Prepopulate User Accounts for Admin Management
            val users = listOf(
                com.example.data.entity.UserAccountEntity("PAT-98421", "Sarah Jenkins", "PATIENT", "sarah.j@vault.io", false),
                com.example.data.entity.UserAccountEntity("DOC-4481", "Dr. Marcus Vance", "DOCTOR", "m.vance@metrogene.org", false),
                com.example.data.entity.UserAccountEntity("DOC-7712", "Dr. Elena Rostova", "DOCTOR", "e.rostova@stjude.org", false),
                com.example.data.entity.UserAccountEntity("ADMIN-01", "Security Admin - Root", "ADMIN", "admin@healthvault.io", false)
            )
            users.forEach { dao.insertUserAccount(it) }

            // Prepopulate Comprehensive Medical Timeline Events across 9 Categories
            val preseededEvents = listOf(
                MedicalEventEntity(
                    eventId = "ME-001",
                    title = "Cardiology & T1D Emergency Evaluation",
                    category = "Hospital Visits",
                    eventDate = 1784380800000L, // July 18, 2026
                    hospitalName = "Metropolitan General Emergency Center",
                    doctorName = "Dr. Marcus Vance, MD",
                    quickPreview = "Emergency evaluation for acute hypoglycemia. Blood glucose restored with IV Dextrose. Discharged with adjusted insulin sliding scale.",
                    vitalsOrNotes = "BP: 118/76 mmHg | HR: 78 bpm | Glucose: 52 mg/dL",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-002",
                    title = "Inpatient Hypoglycemia Recovery",
                    category = "Discharge Summaries",
                    eventDate = 1784467200000L, // July 19, 2026
                    hospitalName = "Metropolitan General Emergency Center",
                    doctorName = "Dr. Marcus Vance, MD",
                    quickPreview = "Discharged in stable condition following 24-hour observation for nocturnal hypoglycemia. Primary care follow-up scheduled in 30 days.",
                    vitalsOrNotes = "Discharge BP: 120/78 mmHg | Glucose: 110 mg/dL",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-003",
                    title = "Lantus Solostar Dosage Adjustment",
                    category = "Medicine Changes",
                    eventDate = 1783689600000L, // July 10, 2026
                    hospitalName = "Metropolitan General Hospital",
                    doctorName = "Dr. Marcus Vance, MD",
                    quickPreview = "Increased Lantus Solostar insulin from 18U to 20U daily at bedtime. Ventolin HFA inhaler maintained 2 puffs PRN.",
                    vitalsOrNotes = "Target HbA1c < 6.5%",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-004",
                    title = "Complete Blood Count & Metabolic Panel",
                    category = "Lab Reports",
                    eventDate = 1781529600000L, // June 15, 2026
                    hospitalName = "Bay Diagnostic Center",
                    doctorName = "Dr. Marcus Vance, MD",
                    quickPreview = "HbA1c: 6.4% (T1D Controlled), Fasting Glucose: 112 mg/dL, Total Cholesterol: 182 mg/dL, Creatinine: 0.9 mg/dL.",
                    vitalsOrNotes = "Hemoglobin: 13.8 g/dL | WBC: 6.5 k/uL",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-005",
                    title = "Allergic Rhinitis & Seasonal Flare-up",
                    category = "Diagnoses",
                    eventDate = 1780665600000L, // June 05, 2026
                    hospitalName = "City Care Allergy & Immunology Center",
                    doctorName = "Dr. Elena Rostova",
                    quickPreview = "Confirmed Allergic Rhinitis and Mild Asthma exacerbation due to high pollen count. Commenced daily Cetirizine 10mg.",
                    vitalsOrNotes = "SpO2: 98% on room air",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-006",
                    title = "Transthoracic Echocardiogram (TTE)",
                    category = "Scans",
                    eventDate = 1777728000000L, // May 02, 2026
                    hospitalName = "Heart Health & Imaging Clinic",
                    doctorName = "Dr. Marcus Vance, MD",
                    quickPreview = "Transthoracic Echocardiogram: Preserved LVEF 62%, mild mitral regurgitation without hemodynamically significant stenosis.",
                    vitalsOrNotes = "Normal global left ventricular contractility",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-007",
                    title = "Upcoming T1D Quarterly Follow-up",
                    category = "Appointments",
                    eventDate = 1786713600000L, // August 15, 2026
                    hospitalName = "Metropolitan General Outpatient Pavilion",
                    doctorName = "Dr. Marcus Vance, MD",
                    quickPreview = "Upcoming T1D Endocrinology 90-Day Comprehensive Review & Continuous Glucose Monitor (CGM) calibration.",
                    vitalsOrNotes = "Bring 14-day CGM log and blood pressure diary.",
                    status = "Upcoming"
                ),
                MedicalEventEntity(
                    eventId = "ME-008",
                    title = "Annual Influenza & COVID Booster",
                    category = "Vaccinations",
                    eventDate = 1760448000000L, // October 14, 2025
                    hospitalName = "Springfield Community Health Clinic",
                    doctorName = "Dr. Sarah Lin, MD",
                    quickPreview = "Administered Annual Influenza Quadrivalent Vaccine & COVID-19 mRNA Booster in right deltoid. Patient tolerated well.",
                    vitalsOrNotes = "No adverse post-vaccination reactions observed.",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-009",
                    title = "Lumbar Spine MRI Scan",
                    category = "Scans",
                    eventDate = 1757078400000L, // September 05, 2025
                    hospitalName = "Advanced MRI & Diagnostic Pavilion",
                    doctorName = "Dr. Rebecca Thorne",
                    quickPreview = "Lumbar Spine MRI: Minimal L4-L5 disc protrusion without significant neural foraminal narrowing or spinal cord compression.",
                    vitalsOrNotes = "Physical therapy recommended if symptoms recur.",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-010",
                    title = "Acute Bronchitis Evaluation",
                    category = "Hospital Visits",
                    eventDate = 1740057600000L, // February 20, 2025
                    hospitalName = "City General Urgent Care",
                    doctorName = "Dr. David Hwang",
                    quickPreview = "Outpatient visit for acute upper respiratory bronchitis and cough. Prescribed 5-day course of Amoxicillin 500mg.",
                    vitalsOrNotes = "Temp: 99.2°F | BP: 116/74 mmHg",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-011",
                    title = "Laparoscopic Appendectomy",
                    category = "Surgeries",
                    eventDate = 1731417600000L, // November 12, 2024
                    hospitalName = "St. Jude Surgical Hospital",
                    doctorName = "Dr. Arthur Pendelton",
                    quickPreview = "Laparoscopic Appendectomy performed under general anesthesia for non-perforated acute appendicitis. Successful recovery.",
                    vitalsOrNotes = "Surgical incisions well healed. No pathology abnormalities.",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-012",
                    title = "Baseline Type 1 Diabetes Diagnosis",
                    category = "Diagnoses",
                    eventDate = 1704892800000L, // January 10, 2024
                    hospitalName = "Metropolitan Endocrine Specialty Clinic",
                    doctorName = "Dr. Marcus Vance, MD",
                    quickPreview = "Type 1 Diabetes Mellitus routine baseline evaluation. Autoantibody screening positive for anti-GAD. Initiated insulin therapy.",
                    vitalsOrNotes = "Fasting Blood Glucose: 210 mg/dL | HbA1c: 8.9%",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-013",
                    title = "Tdap Booster Vaccination",
                    category = "Vaccinations",
                    eventDate = 1681824000000L, // April 18, 2023
                    hospitalName = "Springfield Health Pavilion",
                    doctorName = "Dr. Sarah Lin, MD",
                    quickPreview = "Tdap (Tetanus, Diphtheria, Acellular Pertussis) Booster administered in left arm. Next booster scheduled for 2033.",
                    vitalsOrNotes = "Left arm mild soreness expected 24-48 hours.",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-014",
                    title = "Prandial Insulin Lispro Transition",
                    category = "Medicine Changes",
                    eventDate = 1741785600000L, // March 12, 2025
                    hospitalName = "Metropolitan General Hospital",
                    doctorName = "Dr. Marcus Vance, MD",
                    quickPreview = "Switched rapid-acting insulin from Regular Humulin to Humalog (Insulin Lispro) 5U pre-meals for better postprandial glucose control.",
                    vitalsOrNotes = "Target postprandial glucose < 140 mg/dL",
                    status = "Completed"
                ),
                MedicalEventEntity(
                    eventId = "ME-015",
                    title = "Comprehensive Fasting Lipid Profile",
                    category = "Lab Reports",
                    eventDate = 1764681600000L, // December 02, 2025
                    hospitalName = "Bay Diagnostic Center",
                    doctorName = "Dr. Marcus Vance, MD",
                    quickPreview = "Triglycerides: 110 mg/dL, HDL: 58 mg/dL, LDL: 102 mg/dL. Normal renal & hepatic function indicators.",
                    vitalsOrNotes = "Lipid panel within goal range for diabetes management.",
                    status = "Completed"
                )
            )
            dao.insertMedicalEvents(preseededEvents)
        }
    }
}

