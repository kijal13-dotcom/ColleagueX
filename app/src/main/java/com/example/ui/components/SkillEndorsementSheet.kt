package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SkillEndorsementEntity
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText

/**
 * Material 3 Modal Bottom Sheet displaying skill endorsements,
 * peer recommendations, verification notes, and an offline Room DB persist action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillEndorsementSheet(
    skillName: String,
    skillIndex: Int,
    targetUserId: Long,
    currentUserId: Long,
    endorsements: List<SkillEndorsementEntity>,
    onDismiss: () -> Unit,
    onEndorse: (skillName: String, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = getSkillPillPalette(skillIndex)
    val skillIcon = getSkillIcon(skillName)

    val relevantEndorsements = endorsements.filter {
        it.skillName.equals(skillName, ignoreCase = true)
    }
    val hasCurrentUserEndorsed = relevantEndorsements.any { it.endorserUserId == currentUserId }
    val isSelfProfile = targetUserId == currentUserId

    var noteText by remember { mutableStateOf("") }
    var showNoteInput by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("user_profile_endorse_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
        ) {
            // Header bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.backgroundColor,
                        border = BorderStroke(1.dp, palette.borderColor),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = skillIcon,
                                contentDescription = null,
                                tint = palette.iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = skillName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy
                        )
                        Text(
                            text = "Professional Skill Endorsement",
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("user_profile_endorse_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close endorsement sheet",
                        tint = WorkCircleSecondaryText
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // Metrics & Persistence Status Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Endorsement Count Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = palette.backgroundColor,
                    border = BorderStroke(1.dp, palette.borderColor),
                    modifier = Modifier.testTag("user_profile_endorse_count_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = palette.iconColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${relevantEndorsements.size} ${if (relevantEndorsements.size == 1) "Endorsement" else "Endorsements"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textColor
                        )
                    }
                }

                // Offline Room DB Persistence Indicator
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.testTag("user_profile_offline_endorsement_indicator")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Room DB Offline-Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Endorse Action Card (if not self profile)
            if (!isSelfProfile) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (hasCurrentUserEndorsed) "You endorsed this skill" else "Validate this colleague",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = if (hasCurrentUserEndorsed) "Tap button to withdraw endorsement" else "Add peer credibility for their workplace expertise",
                                    fontSize = 11.sp,
                                    color = WorkCircleSecondaryText
                                )
                            }

                            Button(
                                onClick = {
                                    onEndorse(skillName, noteText.trim())
                                    noteText = ""
                                    showNoteInput = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasCurrentUserEndorsed) Color(0xFF059669) else WorkCircleBlue
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("user_profile_endorse_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (hasCurrentUserEndorsed) Icons.Default.Check else Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (hasCurrentUserEndorsed) "Endorsed" else "Endorse",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Optional Recommendation Note
                        if (!hasCurrentUserEndorsed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            if (!showNoteInput) {
                                OutlinedButton(
                                    onClick = { showNoteInput = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("+ Add peer recommendation note", fontSize = 11.sp, color = WorkCircleBlue)
                                }
                            } else {
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp),
                                    placeholder = {
                                        Text("e.g. Demonstrated exceptional technical leadership on our distributed platform...", fontSize = 12.sp, color = Color(0xFF64748B))
                                    },
                                    singleLine = false,
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = Color.Black,
                                        focusedBorderColor = WorkCircleBlue,
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_endorse_note_input")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Peer Endorsers List Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Peer Endorsers",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkCircleNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFE2E8F0),
                    shape = CircleShape
                ) {
                    Text(
                        text = relevantEndorsements.size.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                    )
                }
            }

            // Endorsers List
            if (relevantEndorsements.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = palette.backgroundColor,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = palette.iconColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No endorsements yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = WorkCircleNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Be the first verified colleague to endorse this skill!",
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(relevantEndorsements) { index, endorsement ->
                        EndorserCard(
                            endorsement = endorsement,
                            index = index,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card displaying an individual endorser with verification badge,
 * role details, and optional peer commendation quote.
 */
@Composable
private fun EndorserCard(
    endorsement: SkillEndorsementEntity,
    index: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.testTag("user_profile_endorser_item_$index")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar initials
                val initials = endorsement.endorserName
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                    .ifBlank { "PE" }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEEF2FF),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4338CA)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = endorsement.endorserName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy
                        )
                        if (endorsement.isEndorserVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Professional",
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    if (endorsement.endorserRole.isNotBlank()) {
                        Text(
                            text = endorsement.endorserRole,
                            fontSize = 11.sp,
                            color = WorkCircleSecondaryText,
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = formatRelativeTime(endorsement.timestamp),
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Optional Recommendation Quote
            if (endorsement.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = endorsement.note,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF334155),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
