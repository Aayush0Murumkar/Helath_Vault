package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.EncryptedRecordEntity
import com.example.ui.screens.HealthVaultTeal
import com.example.ui.screens.HealthVaultInk
import com.example.ui.screens.HealthVaultCream
import com.example.ui.screens.HealthVaultBorder

val ALL_MEDICAL_CATEGORIES = listOf(
    "Prescriptions",
    "Lab Reports",
    "Blood Tests",
    "X-Ray",
    "MRI",
    "CT Scan",
    "ECG",
    "Ultrasound",
    "Vaccinations",
    "Surgery Reports",
    "Discharge Summaries",
    "Insurance Documents",
    "Bills",
    "Other"
)

val DOCUMENT_TYPES = listOf(
    "PDF Document",
    "Medical Image",
    "Diagnostic Report",
    "Prescription",
    "Lab Result",
    "Certificate",
    "Bill / Invoice"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordMetadataDialog(
    record: EncryptedRecordEntity,
    onDismiss: () -> Unit,
    onSave: (EncryptedRecordEntity) -> Unit
) {
    var title by remember { mutableStateOf(record.getDisplayTitle()) }
    var selectedCategory by remember { mutableStateOf(record.fileCategory.ifBlank { ALL_MEDICAL_CATEGORIES[0] }) }
    var hospitalName by remember { mutableStateOf(record.hospitalName) }
    var doctorName by remember { mutableStateOf(record.doctorName) }
    var selectedDocType by remember { mutableStateOf(record.documentType.ifBlank { DOCUMENT_TYPES[0] }) }
    var tags by remember { mutableStateOf(record.tags) }
    var diagnosis by remember { mutableStateOf(record.diagnosis) }
    var medicines by remember { mutableStateOf(record.medicines) }
    var notes by remember { mutableStateOf(record.notes) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var docTypeDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("edit_metadata_dialog"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE6F5F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Metadata",
                                tint = HealthVaultTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Edit Document Metadata",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultInk
                            )
                            Text(
                                text = record.originalFileName,
                                style = MaterialTheme.typography.labelSmall,
                                color = HealthVaultInk.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_edit_metadata_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = HealthVaultInk.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title
                    Column {
                        Text("Document Title", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("e.g. Annual Blood Test", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = HealthVaultCream,
                                unfocusedContainerColor = HealthVaultCream,
                                focusedBorderColor = HealthVaultTeal,
                                unfocusedBorderColor = HealthVaultBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Category Dropdown
                    Column {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            val catInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HealthVaultCream)
                                    .border(1.dp, HealthVaultBorder, RoundedCornerShape(10.dp))
                                    .pressScale(catInteraction)
                                    .clickable(interactionSource = catInteraction, indication = null) {
                                        categoryDropdownExpanded = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedCategory, fontSize = 13.sp, color = HealthVaultInk)
                                    Text("▾", fontSize = 14.sp, color = HealthVaultTeal)
                                }
                            }

                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                ALL_MEDICAL_CATEGORIES.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontSize = 13.sp) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Hospital Name
                    Column {
                        Text("Hospital / Clinic Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = hospitalName,
                            onValueChange = { hospitalName = it },
                            placeholder = { Text("e.g. City General Hospital", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_hospital_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = HealthVaultCream,
                                unfocusedContainerColor = HealthVaultCream,
                                focusedBorderColor = HealthVaultTeal,
                                unfocusedBorderColor = HealthVaultBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Doctor Name
                    Column {
                        Text("Doctor's Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = doctorName,
                            onValueChange = { doctorName = it },
                            placeholder = { Text("e.g. Dr. Sarah Connor, MD", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_doctor_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = HealthVaultCream,
                                unfocusedContainerColor = HealthVaultCream,
                                focusedBorderColor = HealthVaultTeal,
                                unfocusedBorderColor = HealthVaultBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Document Type Dropdown
                    Column {
                        Text("Document Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            val docTypeInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HealthVaultCream)
                                    .border(1.dp, HealthVaultBorder, RoundedCornerShape(10.dp))
                                    .pressScale(docTypeInteraction)
                                    .clickable(interactionSource = docTypeInteraction, indication = null) {
                                        docTypeDropdownExpanded = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedDocType, fontSize = 13.sp, color = HealthVaultInk)
                                    Text("▾", fontSize = 14.sp, color = HealthVaultTeal)
                                }
                            }

                            DropdownMenu(
                                expanded = docTypeDropdownExpanded,
                                onDismissRequest = { docTypeDropdownExpanded = false }
                            ) {
                                DOCUMENT_TYPES.forEach { docType ->
                                    DropdownMenuItem(
                                        text = { Text(docType, fontSize = 13.sp) },
                                        onClick = {
                                            selectedDocType = docType
                                            docTypeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Tags
                    Column {
                        Text("Tags (comma separated)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            placeholder = { Text("e.g. Diabetes, Routine, Annual", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_tags_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = HealthVaultCream,
                                unfocusedContainerColor = HealthVaultCream,
                                focusedBorderColor = HealthVaultTeal,
                                unfocusedBorderColor = HealthVaultBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Diagnosis / Disease
                    Column {
                        Text("Diagnosis / Disease Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = diagnosis,
                            onValueChange = { diagnosis = it },
                            placeholder = { Text("e.g. Type 1 Diabetes, Asthma", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_diagnosis_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = HealthVaultCream,
                                unfocusedContainerColor = HealthVaultCream,
                                focusedBorderColor = HealthVaultTeal,
                                unfocusedBorderColor = HealthVaultBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Medicines
                    Column {
                        Text("Prescribed Medicines", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = medicines,
                            onValueChange = { medicines = it },
                            placeholder = { Text("e.g. Insulin Glargine, Albuterol", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_medicines_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = HealthVaultCream,
                                unfocusedContainerColor = HealthVaultCream,
                                focusedBorderColor = HealthVaultTeal,
                                unfocusedBorderColor = HealthVaultBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Notes
                    Column {
                        Text("Notes & Remarks", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Additional notes or observations...", fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth().height(90.dp).testTag("edit_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = HealthVaultCream,
                                unfocusedContainerColor = HealthVaultCream,
                                focusedBorderColor = HealthVaultTeal,
                                unfocusedBorderColor = HealthVaultBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = HealthVaultInk)
                    }

                    val saveInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            val updated = record.copy(
                                title = title.trim(),
                                fileCategory = selectedCategory,
                                hospitalName = hospitalName.trim(),
                                doctorName = doctorName.trim(),
                                documentType = selectedDocType,
                                tags = tags.trim(),
                                diagnosis = diagnosis.trim(),
                                medicines = medicines.trim(),
                                notes = notes.trim()
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        interactionSource = saveInteraction,
                        modifier = Modifier
                            .weight(1f)
                            .pressScale(saveInteraction)
                            .testTag("save_metadata_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
