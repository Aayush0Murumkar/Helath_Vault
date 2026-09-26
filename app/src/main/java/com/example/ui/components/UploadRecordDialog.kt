package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.components.pressScale

@Composable
fun UploadRecordDialog(
    onDismiss: () -> Unit,
    onUpload: (fileName: String, category: String, content: String) -> Unit
) {
    val categories = listOf("Blood Report", "Prescription", "MRI / Scan", "Vaccination", "Lab Diagnostics")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var fileName by remember { mutableStateOf("Aug_2026_Lipid_Profile.pdf") }
    var reportContent by remember {
        mutableStateOf("""
            HEALTH VAULT LOCAL ENCRYPTED REPORT
            =========================================
            Patient: Sarah Jenkins (PAT-98421)
            Date: August 01, 2026
            Category: Blood Report - Lipid Profile
            
            RESULTS:
            - Fasting Triglycerides: 135 mg/dL [Normal < 150]
            - HDL Cholesterol: 58 mg/dL [Optimal]
            - LDL Cholesterol: 94 mg/dL [Optimal]
            
            NOTES:
            Patient demonstrates excellent cardiovascular markers.
        """.trimIndent())
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("upload_record_dialog"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1A0D1B1E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = Color(0xFF0F7A6B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add Medical Document",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D1B1E)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_upload_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0x990D1B1E))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE6F5F3))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF0F7A6B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUTOMATIC LOCAL AES-256 ENCRYPTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F7A6B),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Document Title / File Name", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color(0x990D1B1E))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("file_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF7F5F0),
                        unfocusedContainerColor = Color(0xFFF7F5F0),
                        focusedBorderColor = Color(0xFF0F7A6B),
                        unfocusedBorderColor = Color(0x1A0D1B1E),
                        focusedTextColor = Color(0xFF0D1B1E),
                        unfocusedTextColor = Color(0xFF0D1B1E)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color(0x990D1B1E))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        CategoryChip(
                            category = cat,
                            isSelected = selectedCategory == cat,
                            onSelect = { selectedCategory = cat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Report Content", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color(0x990D1B1E))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = reportContent,
                    onValueChange = { reportContent = it },
                    minLines = 4,
                    maxLines = 7,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_content_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF7F5F0),
                        unfocusedContainerColor = Color(0xFFF7F5F0),
                        focusedBorderColor = Color(0xFF0F7A6B),
                        unfocusedBorderColor = Color(0x1A0D1B1E),
                        focusedTextColor = Color(0xFF0D1B1E),
                        unfocusedTextColor = Color(0xFF0D1B1E)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                val buttonInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = {
                        if (fileName.isNotBlank() && reportContent.isNotBlank()) {
                            onUpload(fileName, selectedCategory, reportContent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F7A6B), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    interactionSource = buttonInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(buttonInteraction)
                        .testTag("save_encrypted_record_btn")
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Encrypt & Save File", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF0F7A6B) else Color(0xFFF7F5F0))
            .border(1.dp, if (isSelected) Color(0xFF0F7A6B) else Color(0x1A0D1B1E), RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color.White else Color(0x990D1B1E),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}
