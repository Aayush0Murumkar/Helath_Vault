package com.example.ui.screens.settings

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HealthVaultBorder
import com.example.ui.screens.HealthVaultCream
import com.example.ui.screens.HealthVaultDanger
import com.example.ui.screens.HealthVaultGold
import com.example.ui.screens.HealthVaultInk
import com.example.ui.screens.HealthVaultTeal
import com.example.ui.screens.HealthVaultTealLight
import com.example.viewmodel.HealthVaultViewModel

@Composable
fun AccountScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val patientProfile by viewModel.patientProfile.collectAsStateWithLifecycle()

    var name by remember(patientProfile) { mutableStateOf(patientProfile?.name ?: "Sarah Connor") }
    var email by remember { mutableStateOf("sarah.c@healthvault.io") }
    var phone by remember { mutableStateOf("+1 (555) 234-5678") }
    var isEditing by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "Account & Profile",
            subtitle = "Personal identity & authentication credentials",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(HealthVaultTeal)
                                .border(3.dp, HealthVaultGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk
                        )

                        Text(
                            text = "Patient ID: PAT-98421 • Role: PATIENT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HealthVaultTeal
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HealthVaultTealLight)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🔒 AES-256 ENCLAVE VERIFIED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "PERSONAL INFORMATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("account_name_input"),
                            enabled = isEditing,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth().testTag("account_email_input"),
                            enabled = isEditing,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth().testTag("account_phone_input"),
                            enabled = isEditing,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!isEditing) {
                            Button(
                                onClick = { isEditing = true },
                                modifier = Modifier.fillMaxWidth().testTag("edit_account_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Account Details")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { isEditing = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        viewModel.updatePatientProfile(
                                            fullName = name,
                                            bloodGroup = patientProfile?.bloodGroup ?: "O+",
                                            chronic = patientProfile?.chronicDiseases ?: "None",
                                            allergies = patientProfile?.allergies ?: "Penicillin",
                                            medications = "Lisinopril 10mg"
                                        )
                                        isEditing = false
                                        Toast.makeText(context, "Account profile updated!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).testTag("save_account_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Save Changes")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "ACCOUNT ACTIONS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.lockApp()
                                Toast.makeText(context, "Logged out of session", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("logout_account_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthVaultDanger),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultDanger),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Lock / Log Out of Session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalProfileScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val patientProfile by viewModel.patientProfile.collectAsStateWithLifecycle()

    var bloodGroup by remember(patientProfile) { mutableStateOf(patientProfile?.bloodGroup ?: "O+") }
    var allergies by remember(patientProfile) { mutableStateOf(patientProfile?.allergies ?: "Penicillin, Peanuts") }
    var chronicDiseases by remember(patientProfile) { mutableStateOf(patientProfile?.chronicDiseases ?: "Mild Asthma, Hypertension") }
    var medications by remember { mutableStateOf("Lisinopril 10mg daily, Albuterol Inhaler") }
    var heightCm by remember { mutableStateOf("172 cm") }
    var weightKg by remember { mutableStateOf("68 kg") }
    var insuranceProvider by remember { mutableStateOf("Aetna Health Plan") }
    var insurancePolicyNo by remember { mutableStateOf("POL-HV998231") }
    var emergencyNotes by remember { mutableStateOf("Anaphylactic reaction to penicillin. Keep Epipen in emergency kit.") }

    var isBloodDropdownExpanded by remember { mutableStateOf(false) }
    val bloodTypes = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "Medical Profile",
            subtitle = "Vital clinical parameters, allergies & emergency data",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            text = "BLOOD & VITAL STATS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = bloodGroup,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Blood Group") },
                                trailingIcon = { Text("▼", modifier = Modifier.padding(end = 8.dp)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isBloodDropdownExpanded = true }
                                    .testTag("blood_group_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            DropdownMenu(
                                expanded = isBloodDropdownExpanded,
                                onDismissRequest = { isBloodDropdownExpanded = false }
                            ) {
                                bloodTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            bloodGroup = type
                                            isBloodDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = heightCm,
                                onValueChange = { heightCm = it },
                                label = { Text("Height") },
                                modifier = Modifier.weight(1f).testTag("height_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = weightKg,
                                onValueChange = { weightKg = it },
                                label = { Text("Weight") },
                                modifier = Modifier.weight(1f).testTag("weight_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "CLINICAL CONDITIONS & ALLERGIES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = allergies,
                            onValueChange = { allergies = it },
                            label = { Text("Known Allergies") },
                            modifier = Modifier.fillMaxWidth().testTag("allergies_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = chronicDiseases,
                            onValueChange = { chronicDiseases = it },
                            label = { Text("Chronic Diseases / Conditions") },
                            modifier = Modifier.fillMaxWidth().testTag("chronic_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = medications,
                            onValueChange = { medications = it },
                            label = { Text("Current Active Medications") },
                            modifier = Modifier.fillMaxWidth().testTag("medications_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "INSURANCE & EMERGENCY NOTES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = insuranceProvider,
                            onValueChange = { insuranceProvider = it },
                            label = { Text("Insurance Provider") },
                            modifier = Modifier.fillMaxWidth().testTag("insurance_provider_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = insurancePolicyNo,
                            onValueChange = { insurancePolicyNo = it },
                            label = { Text("Policy Number") },
                            modifier = Modifier.fillMaxWidth().testTag("insurance_policy_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = emergencyNotes,
                            onValueChange = { emergencyNotes = it },
                            label = { Text("Emergency Medical Notes") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("emergency_notes_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.updatePatientProfile(
                                    fullName = patientProfile?.name ?: "Sarah Connor",
                                    bloodGroup = bloodGroup,
                                    chronic = chronicDiseases,
                                    allergies = allergies,
                                    medications = medications
                                )
                                Toast.makeText(context, "Medical Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_medical_profile_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Medical Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NomineesScreen(
    viewModel: HealthVaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nominees by viewModel.nominees.collectAsStateWithLifecycle()

    var showAddForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newRelation by remember { mutableStateOf("Spouse") }
    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            title = "Nominees & Emergency Access",
            subtitle = "Designated emergency contacts & key inheritance",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
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
                            Column {
                                Text(
                                    text = "TRUSTED NOMINEES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultInk,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${nominees.size} Nominees registered for emergency key inheritance",
                                    fontSize = 11.sp,
                                    color = HealthVaultInk.copy(alpha = 0.6f)
                                )
                            }

                            Button(
                                onClick = { showAddForm = !showAddForm },
                                colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("toggle_add_nominee_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (showAddForm) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(HealthVaultCream)
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "NEW NOMINEE DETAILS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HealthVaultTeal
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = newName,
                                        onValueChange = { newName = it },
                                        placeholder = { Text("Full Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("nominee_name_input"),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newRelation,
                                        onValueChange = { newRelation = it },
                                        placeholder = { Text("Relationship (e.g. Spouse, Child, Physician)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("nominee_relation_input"),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newPhone,
                                        onValueChange = { newPhone = it },
                                        placeholder = { Text("Phone Number") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("nominee_phone_input"),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newEmail,
                                        onValueChange = { newEmail = it },
                                        placeholder = { Text("Email Address") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("nominee_email_input"),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (newName.isNotBlank()) {
                                                viewModel.addNominee(
                                                    name = newName,
                                                    relationship = newRelation,
                                                    phone = newPhone.ifBlank { "+1 555-0199" },
                                                    email = newEmail.ifBlank { "nominee@example.com" }
                                                )
                                                newName = ""
                                                newPhone = ""
                                                newEmail = ""
                                                showAddForm = false
                                                Toast.makeText(context, "Nominee added successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("submit_nominee_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Save Nominee", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            items(nominees) { nominee ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = nominee.name,
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
                                        text = nominee.relationship.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HealthVaultTeal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "📞 ${nominee.phone} • ✉️ ${nominee.email}",
                                fontSize = 11.sp,
                                color = HealthVaultInk.copy(alpha = 0.6f)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.removeNominee(nominee.nomineeId)
                                Toast.makeText(context, "Removed nominee ${nominee.name}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthVaultDanger),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultDanger.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("remove_nominee_${nominee.nomineeId}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
