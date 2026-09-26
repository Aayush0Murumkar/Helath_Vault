package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Colors matching Health Vault
val HealthVaultTeal = Color(0xFF0F7A6B)
val HealthVaultTealMid = Color(0xFF1A9E8C)
val HealthVaultTealLight = Color(0xFFE6F5F3)
val HealthVaultInk = Color(0xFF0D1B1E)
val HealthVaultCream = Color(0xFFF7F5F0)
val HealthVaultDanger = Color(0xFFC0392B)
val HealthVaultDangerLight = Color(0xFFFDF0EF)
val HealthVaultGold = Color(0xFFC9A84C)
val HealthVaultBorder = Color(0x1A0D1B1E)

// -------------------------------------------------------------------
// 1. TACTILE PRESS FEEDBACK MODIFIER (Scale 0.97 on press)
// -------------------------------------------------------------------
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = 0.97f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "PressScaleAnim"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// -------------------------------------------------------------------
// 2. SHIMMER EFFECT MODIFIER & SKELETON BOX
// -------------------------------------------------------------------
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val shimmerColors = listOf(
        Color(0xFFE8E5DC),
        Color(0xFFFBF9F5),
        Color(0xFFE8E5DC)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )

    return this.background(brush)
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerEffect()
    )
}

// -------------------------------------------------------------------
// 3. STAGGERED ENTRY CARD WRAPPER
// -------------------------------------------------------------------
@Composable
fun StaggeredEntryCard(
    index: Int,
    modifier: Modifier = Modifier,
    delayPerItem: Int = 50,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * delayPerItem).toLong())
        visible = true
    }

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "StaggerOffsetY"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "StaggerAlpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY
                this.alpha = alpha
            }
    ) {
        content()
    }
}

// -------------------------------------------------------------------
// 4. SCREEN SKELETON LOADERS
// -------------------------------------------------------------------

@Composable
fun HomeScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome Banner Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            shape = RoundedCornerShape(18.dp)
        )

        // 2x2 Stat Grid Skeleton
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBox(modifier = Modifier.weight(1f).height(90.dp), shape = RoundedCornerShape(14.dp))
                SkeletonBox(modifier = Modifier.weight(1f).height(90.dp), shape = RoundedCornerShape(14.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBox(modifier = Modifier.weight(1f).height(90.dp), shape = RoundedCornerShape(14.dp))
                SkeletonBox(modifier = Modifier.weight(1f).height(90.dp), shape = RoundedCornerShape(14.dp))
            }
        }

        // Timeline Card Skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SkeletonBox(modifier = Modifier.width(140.dp).height(14.dp))
                Spacer(modifier = Modifier.height(16.dp))
                repeat(3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(modifier = Modifier.size(12.dp), shape = CircleShape)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            SkeletonBox(modifier = Modifier.width(90.dp).height(10.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordsScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                    SkeletonBox(modifier = Modifier.width(150.dp).height(14.dp))
                    SkeletonBox(modifier = Modifier.width(90.dp).height(24.dp), shape = RoundedCornerShape(8.dp))
                }
                Spacer(modifier = Modifier.height(14.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(10.dp))
                Spacer(modifier = Modifier.height(16.dp))
                repeat(4) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun UploadScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SkeletonBox(modifier = Modifier.width(160.dp).height(14.dp))
                Spacer(modifier = Modifier.height(16.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                SkeletonBox(modifier = Modifier.width(130.dp).height(12.dp))
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(10.dp))
                Spacer(modifier = Modifier.height(14.dp))
                SkeletonBox(modifier = Modifier.width(90.dp).height(12.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        SkeletonBox(modifier = Modifier.width(80.dp).height(32.dp), shape = RoundedCornerShape(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp))
            }
        }
    }
}

@Composable
fun EmergencyScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        SkeletonBox(modifier = Modifier.size(140.dp), shape = CircleShape)
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = HealthVaultDangerLight),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5C6C2))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                SkeletonBox(modifier = Modifier.width(180.dp).height(14.dp))
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SkeletonBox(modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(10.dp))
                    SkeletonBox(modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(10.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthVaultCream)
    ) {
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(70.dp), shape = RoundedCornerShape(0.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp))
            repeat(6) {
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(14.dp))
            }
        }
    }
}

@Composable
fun SubscreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthVaultCream)
    ) {
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(0.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) {
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(110.dp), shape = RoundedCornerShape(16.dp))
            }
        }
    }
}

// -------------------------------------------------------------------
// 5. EMPTY STATES (Requirements 14)
// -------------------------------------------------------------------
@Composable
fun EmptyRecordsState(
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(HealthVaultTealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = HealthVaultTeal,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No medical records yet.",
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HealthVaultInk
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your local enclave is ready. Upload lab reports, prescriptions, or scans with end-to-end encryption.",
                fontSize = 13.sp,
                color = HealthVaultInk.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            val interactionSource = remember { MutableInteractionSource() }
            Button(
                onClick = onUploadClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthVaultTeal,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                interactionSource = interactionSource,
                modifier = Modifier
                    .pressScale(interactionSource)
                    .testTag("empty_state_upload_btn")
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload First Record", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyNomineesState(
    onAddNomineeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(HealthVaultTealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = HealthVaultTeal,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No nominees added yet.",
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HealthVaultInk
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Assign trusted contacts or family members for emergency access or key inheritance.",
                fontSize = 13.sp,
                color = HealthVaultInk.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            val interactionSource = remember { MutableInteractionSource() }
            Button(
                onClick = onAddNomineeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthVaultTeal,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                interactionSource = interactionSource,
                modifier = Modifier
                    .pressScale(interactionSource)
                    .testTag("empty_state_add_nominee_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Nominee", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------------
// 6. MODAL & DIALOG SCALE + FADE ANIMATED WRAPPER (Requirement 9)
// -------------------------------------------------------------------
@Composable
fun AnimatedModalCard(
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(250)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthVaultBorder)
        ) {
            content()
        }
    }
}
