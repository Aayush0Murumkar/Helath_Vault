package com.example.ui.screens.assistant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.EncryptedRecordEntity
import com.example.ui.screens.HealthVaultBorder
import com.example.ui.screens.HealthVaultCream
import com.example.ui.screens.HealthVaultDanger
import com.example.ui.screens.HealthVaultDangerLight
import com.example.ui.screens.HealthVaultInk
import com.example.ui.screens.HealthVaultTeal
import com.example.ui.screens.HealthVaultTealLight
import com.example.viewmodel.ChatMessageItem
import com.example.viewmodel.ChatMessageType
import com.example.viewmodel.ChatSender
import com.example.viewmodel.ConversationSession
import com.example.viewmodel.HealthVaultViewModel
import kotlinx.coroutines.launch

@Composable
fun AIAssistantScreen(
    viewModel: HealthVaultViewModel,
    onNavigateToRecordDetails: (EncryptedRecordEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAITyping by viewModel.isAITyping.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val currentConvId by viewModel.currentConversationId.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showHistoryDrawer by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new chat message
    LaunchedEffect(chatMessages.size, isAITyping) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            AssistantHeader(
                onToggleHistory = { showHistoryDrawer = !showHistoryDrawer },
                onNewChat = {
                    viewModel.startNewConversation()
                    Toast.makeText(context, "Started new AI conversation session", Toast.LENGTH_SHORT).show()
                },
                onClearChat = {
                    viewModel.clearCurrentConversation()
                    Toast.makeText(context, "Conversation cleared", Toast.LENGTH_SHORT).show()
                }
            )

            // Suggested Prompts Row
            SuggestedPromptsRow(
                onSelectPrompt = { promptText, type ->
                    viewModel.processUserQuery(promptText, type)
                }
            )

            // Main Chat Stream
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        ChatMessageBubble(
                            message = msg,
                            onRecordClick = { record ->
                                viewModel.openRecordDetails(record)
                                onNavigateToRecordDetails(record)
                            }
                        )
                    }

                    if (isAITyping) {
                        item {
                            AITypingIndicatorBubble()
                        }
                    }
                }
            }

            // Input Dock
            AssistantInputDock(
                inputText = inputText,
                onInputTextChange = { inputText = it },
                isSearchMode = isSearchMode,
                onToggleSearchMode = { isSearchMode = !isSearchMode },
                onSend = {
                    if (inputText.isNotBlank()) {
                        val type = if (isSearchMode) ChatMessageType.RECORD_SEARCH_RESULT else ChatMessageType.GENERAL
                        viewModel.processUserQuery(inputText, type)
                        inputText = ""
                    }
                }
            )
        }

        // Conversation History Overlay Drawer
        AnimatedVisibility(
            visible = showHistoryDrawer,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            ConversationHistoryOverlay(
                conversations = conversations,
                currentConvId = currentConvId,
                onSelectSession = { convId ->
                    viewModel.loadConversation(convId)
                    showHistoryDrawer = false
                },
                onClose = { showHistoryDrawer = false }
            )
        }
    }
}

@Composable
private fun AssistantHeader(
    onToggleHistory: () -> Unit,
    onNewChat: () -> Unit,
    onClearChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = HealthVaultCream),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(HealthVaultTeal)
                        .testTag("ai_assistant_icon"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Gemini AI",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Gemini Health Assistant",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthVaultInk
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HealthVaultTealLight)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text("Enclave AI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HealthVaultTeal)
                        }
                    }

                    Text(
                        text = "Zero-Knowledge Medical Intelligence & Record Analysis",
                        fontSize = 10.sp,
                        color = HealthVaultInk.copy(alpha = 0.6f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(
                    onClick = onToggleHistory,
                    modifier = Modifier.testTag("btn_history_drawer")
                ) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = HealthVaultInk)
                }

                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier.testTag("btn_new_chat")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat", tint = HealthVaultTeal)
                }

                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.testTag("btn_clear_chat")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = HealthVaultDanger)
                }
            }
        }
    }
}

@Composable
private fun SuggestedPromptsRow(
    onSelectPrompt: (String, ChatMessageType) -> Unit
) {
    val prompts = listOf(
        Triple("🏥 Medical Summary", "Generate my full Medical Summary", ChatMessageType.MEDICAL_SUMMARY),
        Triple("💊 Medicine Explanation", "Explain my active prescribed medicines", ChatMessageType.MEDICINE_EXPLANATION),
        Triple("📅 Appointment Guide", "Explain my upcoming Cardiology appointment", ChatMessageType.APPOINTMENT_EXPLANATION),
        Triple("📈 Health Trends", "Analyze my 6-month blood pressure & vitals trends", ChatMessageType.HEALTH_TRENDS),
        Triple("🧪 Recent Blood Report", "Summarize my recent Quest Diagnostics blood test", ChatMessageType.REPORT_SUMMARY),
        Triple("🔍 Natural Language Search", "Search records for 'Cholesterol' and 'Metformin'", ChatMessageType.RECORD_SEARCH_RESULT)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(prompts) { (label, promptText, type) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, HealthVaultBorder, RoundedCornerShape(18.dp))
                    .clickable { onSelectPrompt(promptText, type) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("prompt_${type.name}")
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthVaultInk
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageItem,
    onRecordClick: (EncryptedRecordEntity) -> Unit
) {
    val context = LocalContext.current

    when (message.sender) {
        ChatSender.SYSTEM -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(HealthVaultCream)
                        .border(1.dp, HealthVaultBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = message.text,
                        fontSize = 10.sp,
                        color = HealthVaultInk.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        ChatSender.USER -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthVaultTeal),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = message.text,
                            fontSize = 13.sp,
                            color = Color.White,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.timestamp,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        ChatSender.ASSISTANT -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(HealthVaultTealLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Card(
                    shape = RoundedCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder),
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = message.text,
                            fontSize = 12.sp,
                            color = HealthVaultInk,
                            lineHeight = 17.sp
                        )

                        // Render Natural Language Search Results if available
                        if (message.matchingRecords.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "MATCHING VAULT RECORDS (${message.matchingRecords.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthVaultTeal
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                message.matchingRecords.forEach { record ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HealthVaultCream)
                                            .border(1.dp, HealthVaultBorder, RoundedCornerShape(8.dp))
                                            .clickable { onRecordClick(record) }
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Description,
                                                    contentDescription = "Record",
                                                    tint = HealthVaultTeal,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(record.getDisplayTitle(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                                                    Text("${record.fileCategory} • ${record.hospitalName}", fontSize = 9.sp, color = HealthVaultInk.copy(alpha = 0.6f))
                                                }
                                            }
                                            Text("View >", fontSize = 10.sp, color = HealthVaultTeal, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Footer with Copy & Speak actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.timestamp,
                                fontSize = 9.sp,
                                color = HealthVaultInk.copy(alpha = 0.4f)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("AI Health Assistant", message.text)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = HealthVaultInk.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "🔊 Audio summary playback initiated", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Read Aloud",
                                        tint = HealthVaultTeal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AITypingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "TypingAnim")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(400, delayMillis = 150, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(400, delayMillis = 300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(HealthVaultTealLight),
            contentAlignment = Alignment.Center
        ) {
            Text("✨", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            shape = RoundedCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Gemini AI is analyzing health records",
                    fontSize = 11.sp,
                    color = HealthVaultInk.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                Box(modifier = Modifier.size(6.dp).scale(dot1Scale).clip(CircleShape).background(HealthVaultTeal))
                Box(modifier = Modifier.size(6.dp).scale(dot2Scale).clip(CircleShape).background(HealthVaultTeal))
                Box(modifier = Modifier.size(6.dp).scale(dot3Scale).clip(CircleShape).background(HealthVaultTeal))
            }
        }
    }
}

@Composable
private fun AssistantInputDock(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    isSearchMode: Boolean,
    onToggleSearchMode: () -> Unit,
    onSend: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle Mode Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSearchMode) HealthVaultTealLight else HealthVaultCream)
                        .clickable { onToggleSearchMode() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("toggle_search_mode")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSearchMode) Icons.Default.Search else Icons.Default.AutoAwesome,
                            contentDescription = "Mode",
                            tint = if (isSearchMode) HealthVaultTeal else HealthVaultInk.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSearchMode) "Vault Search" else "Ask AI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSearchMode) HealthVaultTeal else HealthVaultInk
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    placeholder = {
                        Text(
                            text = if (isSearchMode) "Search records using natural language..." else "Ask about your health, lab reports, pills...",
                            fontSize = 11.sp,
                            color = HealthVaultInk.copy(alpha = 0.45f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_input_field"),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HealthVaultTeal,
                        unfocusedBorderColor = HealthVaultBorder,
                        focusedContainerColor = HealthVaultCream,
                        unfocusedContainerColor = HealthVaultCream
                    ),
                    singleLine = true,
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { onInputTextChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = HealthVaultInk.copy(alpha = 0.5f))
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "🎤 Voice dictation ready. Speak your question...", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = HealthVaultTeal)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) HealthVaultTeal else HealthVaultBorder)
                        .testTag("btn_send_ai")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.White else HealthVaultInk.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationHistoryOverlay(
    conversations: List<ConversationSession>,
    currentConvId: String,
    onSelectSession: (String) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📜 Conversation History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HealthVaultInk)
                    }

                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Clear, contentDescription = "Close Drawer", tint = HealthVaultInk)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        val isSelected = conv.id == currentConvId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) HealthVaultTealLight else HealthVaultCream)
                                .border(1.dp, if (isSelected) HealthVaultTeal else HealthVaultBorder, RoundedCornerShape(12.dp))
                                .clickable { onSelectSession(conv.id) }
                                .padding(12.dp)
                                .testTag("session_${conv.id}")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = conv.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) HealthVaultTeal else HealthVaultInk
                                    )
                                    Text(
                                        text = conv.timestamp,
                                        fontSize = 9.sp,
                                        color = HealthVaultInk.copy(alpha = 0.5f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = conv.preview,
                                    fontSize = 10.sp,
                                    color = HealthVaultInk.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
