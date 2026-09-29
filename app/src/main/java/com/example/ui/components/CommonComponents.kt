package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportReason
import com.example.data.model.UserRole
import com.example.ui.theme.WorkCircleAmberContainer
import com.example.ui.theme.WorkCircleAmberWarning
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleRedContainer
import com.example.ui.theme.WorkCircleRedDestructive
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen
import com.example.ui.theme.WorkCircleVerifiedGreenContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - timestamp).coerceAtLeast(0)
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

fun formatFullTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun UserAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 44,
    backgroundColor: Color = WorkCircleBlue,
    isAnonymous: Boolean = false
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(if (isAnonymous) Color(0xFF475569) else backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isAnonymous) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Anonymous",
                tint = Color.White,
                modifier = Modifier.size((sizeDp * 0.55).dp)
            )
        } else {
            Text(
                text = initials.take(2).uppercase(),
                color = Color.White,
                fontSize = (sizeDp * 0.38).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VerifiedBadge(
    text: String = "Verified",
    modifier: Modifier = Modifier
) {
    Surface(
        color = WorkCircleVerifiedGreenContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified badge",
                tint = WorkCircleVerifiedGreen,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = WorkCircleVerifiedGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ConfidentialityWarningBanner(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WorkCircleAmberContainer.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Confidentiality Warning",
                tint = WorkCircleAmberWarning,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Professional Confidentiality Reminder",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = WorkCircleNavy
                )
                Text(
                    text = "Never disclose employer trade secrets, non-public financials, internal source code, or customer PII. Anonymity is protected for candid professional discussion but subject to platform safety policies.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = WorkCircleNavy.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val bgColor = when (role) {
        UserRole.REGULAR_PROFESSIONAL -> Color(0xFFEBF2FF)
        UserRole.COMMUNITY_MODERATOR -> Color(0xFFEDE9FE)
        UserRole.COMPANY_REPRESENTATIVE -> Color(0xFFCCFBF1)
        UserRole.PLATFORM_ADMIN -> WorkCircleRedContainer
        UserRole.GUEST -> Color(0xFFF1F5F9)
    }
    val textColor = Color(role.badgeColorHex)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = role.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ReportDialog(
    entityTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(ReportReason.CONFIDENTIAL_INFO) }
    var detailsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = WorkCircleRedDestructive,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report Content",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WorkCircleNavy
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Item: \"${entityTitle.take(60)}\"",
                    fontSize = 12.sp,
                    color = WorkCircleSecondaryText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "Select violation reason:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = WorkCircleNavy
                )

                ReportReason.values().forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = WorkCircleBlue)
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = reason.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = WorkCircleNavy
                            )
                            Text(
                                text = reason.description,
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = detailsText,
                    onValueChange = { detailsText = it },
                    label = { Text("Additional context (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_details_input"),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(selectedReason.name, detailsText) },
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text("Submit Report", color = WorkCircleRedDestructive, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = WorkCircleSecondaryText)
            }
        }
    )
}
