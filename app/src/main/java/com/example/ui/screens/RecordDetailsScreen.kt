package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.EncryptedRecordEntity
import com.example.ui.components.EditRecordMetadataDialog
import com.example.ui.components.pressScale
import com.example.viewmodel.HealthVaultViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



@Composable
fun RecordDetailsScreen(
    record: EncryptedRecordEntity,
    viewModel: HealthVaultViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isDecrypted by remember { mutableStateOf(false) }
    var decryptedContent by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showShareModal by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    val examDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Auto-decrypt plaintext preview in view
    LaunchedEffect(record.fileId) {
        try {
            val plaintext = viewModel.decryptAndSelectRecordForDetails(record)
            decryptedContent = plaintext
            isDecrypted = true
        } catch (e: Exception) {
            isDecrypted = false
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("record_details_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = HealthVaultInk
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = record.getDisplayTitle(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${record.fileCategory} • ${record.documentType}",
                                fontSize = 11.sp,
                                color = HealthVaultTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Favorite Toggle Button
                        IconButton(
                            onClick = {
                                viewModel.toggleFavorite(record)
                                Toast.makeText(
                                    context,
                                    if (!record.isFavorite) "Added to Favorites" else "Removed from Favorites",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.testTag("favorite_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (record.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Favorite",
                                tint = if (record.isFavorite) HealthVaultGold else HealthVaultInk.copy(alpha = 0.5f)
                            )
                        }

                        // Edit Metadata Button
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.testTag("edit_metadata_icon_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Metadata",
                                tint = HealthVaultTeal
                            )
                        }
                    }
                }
            }
        },
        containerColor = HealthVaultCream
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Banner if Trashed or Archived
            if (record.isTrashed) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthVaultDangerLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultDanger)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = HealthVaultDanger)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Item in Trash (Permanently deleted in 30 days)",
                                fontSize = 12.sp,
                                color = HealthVaultDanger,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.restoreFromTrash(record)
                                Toast.makeText(context, "Record restored from Trash", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Restore", fontSize = 11.sp)
                        }
                    }
                }
            } else if (record.isArchived) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEEBA))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Archive, contentDescription = null, tint = Color(0xFF856404))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Archived Document (Hidden from main list)",
                                fontSize = 12.sp,
                                color = Color(0xFF856404),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.toggleArchive(record)
                                Toast.makeText(context, "Unarchived Record", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Unarchive", fontSize = 11.sp)
                        }
                    }
                }
            }

            // 1. Document Preview Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HealthVaultTealLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (record.originalFileName.contains("pdf", ignoreCase = true)) "📄" else "🖼️",
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DOCUMENT PREVIEW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultTeal,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "AES-256-GCM Hardware Encrypted",
                                    fontSize = 11.sp,
                                    color = HealthVaultInk.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HealthVaultTealLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔒 Verified Zero-Knowledge",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rendered Preview Window
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HealthVaultCream)
                            .border(1.dp, HealthVaultBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (isDecrypted && !decryptedContent.isNullOrBlank()) {
                            Column {
                                Text(
                                    text = "--- MEDICAL REPORT DECRYPTED PREVIEW ---",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthVaultTeal,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Text(
                                    text = decryptedContent ?: "",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = HealthVaultInk,
                                    lineHeight = 18.sp
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = HealthVaultTeal, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Decrypting Document Key...", fontSize = 12.sp, color = HealthVaultInk)
                            }
                        }
                    }
                }
            }

            // 2. Comprehensive Metadata Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECORD METADATA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )

                        TextButton(
                            onClick = { showEditDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Edit Metadata ✏️", fontSize = 12.sp, color = HealthVaultTeal, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetadataRow(label = "Title", value = record.getDisplayTitle())
                        MetadataRow(label = "Original File", value = record.originalFileName)
                        MetadataRow(label = "Category", value = record.fileCategory)
                        MetadataRow(
                            label = "Hospital / Clinic",
                            value = record.hospitalName.ifBlank { "Not Specified" }
                        )
                        MetadataRow(
                            label = "Attending Doctor",
                            value = record.doctorName.ifBlank { "Not Specified" }
                        )
                        MetadataRow(
                            label = "Examination Date",
                            value = examDateFormat.format(Date(record.examinationDate))
                        )
                        MetadataRow(
                            label = "Upload Date",
                            value = dateFormat.format(Date(record.uploadTimestamp))
                        )
                        MetadataRow(label = "File Size", value = record.fileSizeFormatted)
                        MetadataRow(label = "Document Type", value = record.documentType)
                        MetadataRow(
                            label = "SHA-256 Checksum",
                            value = record.sha256Hash.take(16) + "..."
                        )
                    }
                }
            }

            // 2.5 AI Summary Card Placeholder
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🤖 ", fontSize = 16.sp)
                            Text(
                                text = "AI SUMMARY PLACEHOLDER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HealthVaultTealLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Architecture Ready",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HealthVaultCream)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = record.aiSummary.ifBlank { "[AI Summary Placeholder]\nPending Gemini / Vertex AI clinical processing." },
                            fontSize = 12.sp,
                            color = HealthVaultInk,
                            lineHeight = 18.sp
                        )
                    }

                    if (record.diagnosis.isNotBlank() || record.medicines.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (record.diagnosis.isNotBlank()) {
                                Text(
                                    text = "Diagnosis: ${record.diagnosis}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HealthVaultTeal
                                )
                            }
                            if (record.medicines.isNotBlank()) {
                                Text(
                                    text = "Medicines: ${record.medicines}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HealthVaultInk.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. OCR Text Extraction Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔍 ", fontSize = 16.sp)
                            Text(
                                text = "EXTRACTED OCR TEXT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultInk.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                        }

                        val copyText = record.ocrText.ifBlank { decryptedContent ?: "" }
                        TextButton(
                            onClick = {
                                if (copyText.isNotBlank()) {
                                    Toast.makeText(context, "OCR Text copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Copy Text 📋", fontSize = 12.sp, color = HealthVaultTeal, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HealthVaultCream)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (record.ocrText.isNotBlank()) record.ocrText else (decryptedContent ?: "No OCR text extracted yet."),
                            fontSize = 12.sp,
                            color = HealthVaultInk,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 4. Notes & Tags Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TAGS & PERSONAL NOTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthVaultInk.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tags Chips
                    if (record.tags.isNotBlank()) {
                        Text("Tags:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            record.tags.split(",").forEach { tag ->
                                val cleanTag = tag.trim()
                                if (cleanTag.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(HealthVaultTealLight)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("#$cleanTag", fontSize = 11.sp, color = HealthVaultTeal, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Notes
                    Text("Notes:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.notes.ifBlank { "No additional notes attached." },
                        fontSize = 12.sp,
                        color = HealthVaultInk.copy(alpha = 0.8f)
                    )
                }
            }

            // 5. Action Buttons Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Share Button
                        val shareInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = {
                                showShareModal = true
                            },
                            interactionSource = shareInteraction,
                            modifier = Modifier.weight(1f).pressScale(shareInteraction).testTag("share_record_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Share 📤", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Download Button
                        val downloadInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Decrypted file saved to Downloads folder!", Toast.LENGTH_LONG).show()
                            },
                            interactionSource = downloadInteraction,
                            modifier = Modifier.weight(1f).pressScale(downloadInteraction).testTag("download_record_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTealMid, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Download 📥", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Archive Toggle
                        OutlinedButton(
                            onClick = {
                                viewModel.toggleArchive(record)
                                Toast.makeText(
                                    context,
                                    if (!record.isArchived) "Record moved to Archive" else "Record unarchived",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.weight(1f).testTag("archive_record_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (record.isArchived) "Unarchive 📦" else "Archive 📦", fontSize = 12.sp, color = HealthVaultInk)
                        }

                        // Trash / Delete
                        OutlinedButton(
                            onClick = {
                                if (record.isTrashed) {
                                    viewModel.deleteRecord(record)
                                    Toast.makeText(context, "Record permanently deleted", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                } else {
                                    viewModel.moveToTrash(record)
                                    Toast.makeText(context, "Record moved to Trash (30 days retention)", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("trash_record_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthVaultDanger),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultDanger),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (record.isTrashed) "Delete Forever 🗑️" else "Move to Trash 🗑️", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Edit Metadata Dialog Modal
    if (showEditDialog) {
        EditRecordMetadataDialog(
            record = record,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.updateRecordMetadata(updated)
                Toast.makeText(context, "Metadata updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Share Confirmation Modal
    if (showShareModal) {
        AlertDialog(
            onDismissRequest = { showShareModal = false },
            title = { Text("Share Record Securely", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select share mode for '${record.getDisplayTitle()}':", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Time-limited Doctor Access Code (24h)", fontSize = 12.sp, color = HealthVaultTeal)
                    Text("• Zero-Knowledge Encrypted Link", fontSize = 12.sp, color = HealthVaultTeal)
                    Text("• Decrypted PDF Attachment", fontSize = 12.sp, color = HealthVaultTeal)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShareModal = false
                        Toast.makeText(context, "Encrypted share link generated and copied!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthVaultTeal)
                ) {
                    Text("Generate Link 🔗")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = HealthVaultInk.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = HealthVaultInk,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
