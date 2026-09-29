package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.theme.colleagueXTextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserConnectionEntity
import com.example.data.local.UserEntity
import com.example.data.model.ConnectionStatus
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText

@Composable
fun ProfessionalsScreen(
    professionals: List<UserEntity>,
    connections: List<UserConnectionEntity> = emptyList(),
    currentUserId: Long,
    onProfessionalClick: (Long) -> Unit,
    onMessageClick: (Long) -> Unit,
    onConnectClick: (Long, String, Boolean) -> Unit,
    onAcceptRequest: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedIndustry by remember { mutableStateOf("All") }
    var requestTargetUser by remember { mutableStateOf<UserEntity?>(null) }
    var requestNote by remember { mutableStateOf("") }
    var isDirectMessageRequest by remember { mutableStateOf(false) }

    val industries = listOf("All", "Technology & AI", "Consulting", "Banking and Finance", "Human Resources")

    val otherProfessionals = professionals.filter { it.id != currentUserId }
    val filtered = otherProfessionals.filter { user ->
        val matchesIndustry = selectedIndustry == "All" || user.industry.equals(selectedIndustry, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
            user.fullName.contains(searchQuery, ignoreCase = true) ||
            user.headline.contains(searchQuery, ignoreCase = true) ||
            user.employer.contains(searchQuery, ignoreCase = true)
        matchesIndustry && matchesSearch
    }

    if (requestTargetUser != null) {
        val target = requestTargetUser!!
        AlertDialog(
            onDismissRequest = { requestTargetUser = null },
            title = {
                Text(
                    text = if (isDirectMessageRequest) "Send Message Request" else "Connect with ${target.fullName}",
                    fontWeight = FontWeight.Bold,
                    color = WorkCircleNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isDirectMessageRequest)
                            "Introduce yourself to start a 1-on-1 chat with ${target.fullName} once accepted."
                        else
                            "Add ${target.fullName} to your ColleagueX professional network.",
                        fontSize = 12.sp,
                        color = WorkCircleSecondaryText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = requestNote,
                        onValueChange = { requestNote = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp),
                        placeholder = { Text("Add an intro note (optional)...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("connect_request_note_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConnectClick(target.id, requestNote.trim(), isDirectMessageRequest)
                        requestTargetUser = null
                        requestNote = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                    modifier = Modifier.testTag("submit_connect_request_button")
                ) {
                    Text("Send Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { requestTargetUser = null }) {
                    Text("Cancel", color = WorkCircleSecondaryText)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
    ) {
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Professional Directory",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = WorkCircleNavy
                )
                Text(
                    text = "Connect with peers, managers, and mentors across industries.",
                    fontSize = 12.sp,
                    color = WorkCircleSecondaryText,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                    placeholder = { Text("Search by name, role, or employer...", color = Color(0xFF64748B)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkCircleSecondaryText) },
                    shape = RoundedCornerShape(12.dp),
                    colors = colleagueXTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("professionals_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(industries) { ind ->
                        val isSelected = selectedIndustry == ind
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedIndustry = ind },
                            label = { Text(ind, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WorkCircleBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = WorkCircleNavy
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("professionals_list")
        ) {
            items(filtered, key = { it.id }) { user ->
                val conn = connections.find {
                    (it.requesterId == currentUserId && it.receiverId == user.id) ||
                        (it.requesterId == user.id && it.receiverId == currentUserId)
                }
                val isConnected = conn?.status == ConnectionStatus.ACCEPTED
                val isPendingOutgoing = conn?.status == ConnectionStatus.PENDING && conn.requesterId == currentUserId
                val isPendingIncoming = conn?.status == ConnectionStatus.PENDING && conn.receiverId == currentUserId

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProfessionalClick(user.id) }
                        .testTag("professional_card_${user.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserAvatar(initials = user.avatarInitials, sizeDp = 46)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = WorkCircleNavy
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (user.isVerified) {
                                        VerifiedBadge(text = user.verifiedBadgeText.ifEmpty { "Verified" })
                                    }
                                }
                                Text(
                                    text = user.headline,
                                    fontSize = 12.sp,
                                    color = WorkCircleNavy.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = if (user.employerVisibility) "${user.employer} • ${user.location}" else user.industry,
                                    fontSize = 11.sp,
                                    color = WorkCircleSecondaryText
                                )
                            }

                            if (isConnected) {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Connected",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkCircleBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = user.bio,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = WorkCircleNavy.copy(alpha = 0.9f),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when {
                                isConnected -> {
                                    Button(
                                        onClick = { onMessageClick(user.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("chat_button_${user.id}")
                                    ) {
                                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chat", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                isPendingIncoming -> {
                                    Button(
                                        onClick = { onAcceptRequest(conn.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("accept_button_${user.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Accept Request", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                isPendingOutgoing -> {
                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = WorkCircleSecondaryText, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Request Pending", fontSize = 11.sp, color = WorkCircleSecondaryText, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                                else -> {
                                    OutlinedButton(
                                        onClick = {
                                            requestTargetUser = user
                                            isDirectMessageRequest = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("message_request_button_${user.id}")
                                    ) {
                                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = WorkCircleNavy)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Message", fontSize = 12.sp, color = WorkCircleNavy)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            requestTargetUser = user
                                            isDirectMessageRequest = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("connect_button_${user.id}")
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Connect", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
