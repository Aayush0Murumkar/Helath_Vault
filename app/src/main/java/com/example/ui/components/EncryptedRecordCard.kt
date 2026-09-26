package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.EncryptedRecordEntity
import com.example.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import com.example.ui.components.pressScale

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline

@Composable
fun EncryptedRecordCard(
    record: EncryptedRecordEntity,
    userRole: UserRole,
    onDecryptAndOpen: (EncryptedRecordEntity) -> Unit,
    onAdminInspect: (EncryptedRecordEntity) -> Unit,
    onDelete: (EncryptedRecordEntity) -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavorite: ((EncryptedRecordEntity) -> Unit)? = null,
    onRestore: ((EncryptedRecordEntity) -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(record.examinationDate.takeIf { it > 0 } ?: record.uploadTimestamp))
    val cardInteraction = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(cardInteraction)
            .clickable(interactionSource = cardInteraction, indication = null) {
                onDecryptAndOpen(record)
            }
            .testTag("encrypted_record_card_${record.fileId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1A0D1B1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE6F5F3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (record.fileCategory) {
                                "Prescriptions" -> "💊"
                                "Blood Tests", "Lab Reports" -> "🧪"
                                "X-Ray", "MRI", "CT Scan", "ECG", "Ultrasound" -> "🩻"
                                "Vaccinations" -> "💉"
                                "Surgery Reports", "Discharge Summaries" -> "🏥"
                                "Insurance Documents", "Bills" -> "📄"
                                else -> if (record.originalFileName.contains("PDF", ignoreCase = true)) "📄" else "🖼️"
                            },
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = record.getDisplayTitle(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D1B1E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (record.isFavorite) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("⭐", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${record.fileCategory} • $formattedDate • ${record.fileSizeFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF0F7A6B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (record.hospitalName.isNotBlank() || record.doctorName.isNotBlank()) {
                            Text(
                                text = listOfNotNull(record.hospitalName.takeIf { it.isNotBlank() }, record.doctorName.takeIf { it.isNotBlank() }).joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0x990D1B1E),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onToggleFavorite != null) {
                        IconButton(
                            onClick = { onToggleFavorite(record) },
                            modifier = Modifier.size(32.dp).testTag("card_fav_btn_${record.fileId}")
                        ) {
                            Icon(
                                imageVector = if (record.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Favorite",
                                tint = if (record.isFavorite) Color(0xFFC9A84C) else Color(0x660D1B1E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (record.isTrashed && onRestore != null) {
                        Button(
                            onClick = { onRestore(record) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F7A6B)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Restore", fontSize = 11.sp)
                        }
                    } else if (userRole == UserRole.PATIENT) {
                        val deleteInteraction = remember { MutableInteractionSource() }
                        IconButton(
                            onClick = { onDelete(record) },
                            interactionSource = deleteInteraction,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFDF0EF))
                                .pressScale(deleteInteraction)
                                .testTag("delete_record_btn_${record.fileId}")
                        ) {
                            Text("🗑️", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
