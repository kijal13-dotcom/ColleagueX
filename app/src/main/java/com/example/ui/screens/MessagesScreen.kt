package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import com.example.ui.theme.colleagueXTextFieldColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    messages: List<MessageEntity>,
    currentUser: UserEntity?,
    allUsers: List<UserEntity>,
    connections: List<UserConnectionEntity>,
    activeRecipientId: Long?,
    isGuestMode: Boolean = false,
    onSelectConversation: (Long) -> Unit,
    onSendMessage: (recipientId: Long, body: String) -> Unit,
    onAcceptRequest: (Long) -> Unit,
    onDeclineRequest: (Long) -> Unit,
    onSendConnectionRequest: (targetUserId: Long, note: String, isMessageRequest: Boolean) -> Unit,
    onBackToList: () -> Unit,
    onLoginClick: () -> Unit = {},
    onFindProfessionals: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 1. Guest or Unauthenticated State
    if (currentUser == null || isGuestMode) {
        MessagesGuestGate(
            onLoginClick = onLoginClick,
            modifier = modifier
        )
        return
    }

    // 2. Active 1-on-1 Chat Conversation View
    val recipient = allUsers.find { it.id == activeRecipientId }
    if (recipient != null) {
        val existingConnection = connections.find { conn ->
            ((conn.requesterId == currentUser.id && conn.receiverId == recipient.id) ||
                (conn.requesterId == recipient.id && conn.receiverId == currentUser.id))
        }
        val isConnectedOrAccepted = existingConnection?.status == ConnectionStatus.ACCEPTED

        ChatConversationView(
            currentUser = currentUser,
            recipient = recipient,
            connection = existingConnection,
            isConnectedOrAccepted = isConnectedOrAccepted,
            messages = messages,
            onBackToList = onBackToList,
            onSendMessage = onSendMessage,
            onAcceptRequest = onAcceptRequest,
            onSendConnectionRequest = onSendConnectionRequest,
            modifier = modifier
        )
        return
    }

    // 3. Messages & Requests Overview
    MessagesOverviewView(
        currentUser = currentUser,
        allUsers = allUsers,
        messages = messages,
        connections = connections,
        onSelectConversation = onSelectConversation,
        onAcceptRequest = onAcceptRequest,
        onDeclineRequest = onDeclineRequest,
        onFindProfessionals = onFindProfessionals,
        modifier = modifier
    )
}

@Composable
private fun MessagesGuestGate(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .padding(24.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFEBF2FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = WorkCircleBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sign In to Access Messages",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = WorkCircleNavy
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Direct 1-on-1 chat and message requests are available for authenticated professionals. Connect with verified peers across companies to start safe, confidential conversations.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = WorkCircleSecondaryText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("messages_login_button")
                ) {
                    Text("Log In or Switch Profile", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatConversationView(
    currentUser: UserEntity,
    recipient: UserEntity,
    connection: UserConnectionEntity?,
    isConnectedOrAccepted: Boolean,
    messages: List<MessageEntity>,
    onBackToList: () -> Unit,
    onSendMessage: (Long, String) -> Unit,
    onAcceptRequest: (Long) -> Unit,
    onSendConnectionRequest: (Long, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageInput by remember { mutableStateOf("") }
    var introNoteInput by remember { mutableStateOf("") }

    val conversationMessages = messages.filter {
        (it.senderId == currentUser.id && it.recipientId == recipient.id) ||
            (it.senderId == recipient.id && it.recipientId == currentUser.id)
    }

    val quickReplies = listOf(
        "Thanks for connecting!",
        "Let's schedule 15 mins to chat",
        "Happy to share our framework memo",
        "How is your team handling this?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(
                        onClick = onBackToList,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WorkCircleNavy
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UserAvatar(initials = recipient.avatarInitials, sizeDp = 38)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = recipient.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = WorkCircleNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (recipient.isVerified) {
                                    VerifiedBadge(text = "Verified")
                                }
                            }
                            Text(
                                text = recipient.headline,
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isConnectedOrAccepted) {
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Text(
                                    text = if (connection?.isMessageRequest == true) "Accepted Request" else "Connected",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF047857),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isConnectedOrAccepted) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Quick replies
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(quickReplies) { reply ->
                                SuggestionChip(
                                    onClick = {
                                        onSendMessage(recipient.id, reply)
                                    },
                                    label = { Text(reply, fontSize = 11.sp, color = WorkCircleNavy) },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        // Message input row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = messageInput,
                                onValueChange = { messageInput = it },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                                placeholder = { Text("Write a message to ${recipient.fullName}...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                                colors = colleagueXTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("direct_message_input"),
                                shape = RoundedCornerShape(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (messageInput.isNotBlank()) {
                                        onSendMessage(recipient.id, messageInput.trim())
                                        messageInput = ""
                                    }
                                },
                                enabled = messageInput.isNotBlank(),
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (messageInput.isNotBlank()) WorkCircleBlue else Color(0xFFE2E8F0),
                                        shape = CircleShape
                                    )
                                    .testTag("send_direct_message_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = WorkCircleBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                // Safety banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = WorkCircleBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Direct peer communication. ColleagueX community safety rules and company confidentiality apply.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = WorkCircleNavy
                        )
                    }
                }
            }

            if (!isConnectedOrAccepted) {
                // Gated connection state
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            UserAvatar(initials = recipient.avatarInitials, sizeDp = 56)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = recipient.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = WorkCircleNavy
                            )
                            Text(
                                text = recipient.headline,
                                fontSize = 12.sp,
                                color = WorkCircleSecondaryText,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (connection?.status == ConnectionStatus.PENDING && connection.receiverId == currentUser.id) {
                                // Recipient can accept incoming request
                                Text(
                                    text = "${recipient.fullName} sent you a message request:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = WorkCircleNavy
                                )
                                if (connection.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "\"${connection.note}\"",
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = WorkCircleNavy,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onAcceptRequest(connection.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("accept_chat_request_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Accept Request to Start Chat", fontWeight = FontWeight.Bold)
                                }
                            } else if (connection?.status == ConnectionStatus.PENDING && connection.requesterId == currentUser.id) {
                                // Outgoing request is pending
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D))
                                ) {
                                    Text(
                                        text = "Your message request is waiting for ${recipient.fullName}'s approval. You can chat as soon as it is accepted.",
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                // No request yet
                                Text(
                                    text = "To maintain quality and privacy, direct messaging is available between connected peers or accepted message requests.",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = WorkCircleSecondaryText,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = introNoteInput,
                                    onValueChange = { introNoteInput = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp),
                                    placeholder = { Text("Include an intro note (e.g. why you'd like to chat)...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                                    colors = colleagueXTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth().testTag("intro_note_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        onSendConnectionRequest(
                                            recipient.id,
                                            introNoteInput.ifBlank { "Hi ${recipient.fullName}, I'd love to connect and chat on ColleagueX." },
                                            true
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("send_message_request_button")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send Message Request", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // Connected / Accepted Chat Messages
                if (conversationMessages.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                UserAvatar(initials = recipient.avatarInitials, sizeDp = 48)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "You are connected with ${recipient.fullName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Send a message or pick a quick suggestion below to start the conversation.",
                                    fontSize = 12.sp,
                                    color = WorkCircleSecondaryText,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(conversationMessages, key = { it.id }) { msg ->
                        val isMe = msg.senderId == currentUser.id
                        val timeFormatted = remember(msg.timestamp) {
                            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                            sdf.format(Date(msg.timestamp))
                        }

                        Row(
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!isMe) {
                                UserAvatar(initials = recipient.avatarInitials, sizeDp = 28)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                                Surface(
                                    color = if (isMe) WorkCircleBlue else Color.White,
                                    shape = RoundedCornerShape(
                                        topStart = 14.dp,
                                        topEnd = 14.dp,
                                        bottomStart = if (isMe) 14.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 14.dp
                                    ),
                                    border = if (isMe) null else androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.body,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = if (isMe) Color.White else WorkCircleNavy
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = timeFormatted,
                                        fontSize = 10.sp,
                                        color = WorkCircleSecondaryText
                                    )
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Delivered",
                                            tint = WorkCircleBlue,
                                            modifier = Modifier.size(12.dp)
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
}

@Composable
private fun MessagesOverviewView(
    currentUser: UserEntity,
    allUsers: List<UserEntity>,
    messages: List<MessageEntity>,
    connections: List<UserConnectionEntity>,
    onSelectConversation: (Long) -> Unit,
    onAcceptRequest: (Long) -> Unit,
    onDeclineRequest: (Long) -> Unit,
    onFindProfessionals: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Accepted connections involving currentUser
    val acceptedConnections = connections.filter { conn ->
        ((conn.requesterId == currentUser.id) || (conn.receiverId == currentUser.id)) &&
            conn.status == ConnectionStatus.ACCEPTED
    }

    val connectedPeerIds = acceptedConnections.map { conn ->
        if (conn.requesterId == currentUser.id) conn.receiverId else conn.requesterId
    }.toSet()

    val connectedPeers = allUsers.filter { it.id in connectedPeerIds }

    // Incoming requests for currentUser
    val incomingRequests = connections.filter { conn ->
        conn.receiverId == currentUser.id && conn.status == ConnectionStatus.PENDING
    }

    // Outgoing requests by currentUser
    val outgoingRequests = connections.filter { conn ->
        conn.requesterId == currentUser.id && conn.status == ConnectionStatus.PENDING
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
    ) {
        // Top Header
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Messages & Connections",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = WorkCircleNavy
                )
                Text(
                    text = "Chat with connected colleagues and manage message requests.",
                    fontSize = 12.sp,
                    color = WorkCircleSecondaryText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = WorkCircleBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Chats", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                                if (connectedPeers.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (selectedTab == 0) WorkCircleBlue else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${connectedPeers.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTab == 0) Color.White else WorkCircleNavy,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("messages_tab_chats")
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Requests", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                                if (incomingRequests.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFEF4444),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${incomingRequests.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("messages_tab_requests")
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // Connected Active Chats Tab
            if (connectedPeers.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFEFF6FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = WorkCircleBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Connected Chats Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = WorkCircleNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You can chat with users you are connected with or who have accepted your message request.",
                                fontSize = 12.sp,
                                color = WorkCircleSecondaryText,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onFindProfessionals,
                                colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Find Professionals to Connect", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize().testTag("active_chats_list")
                ) {
                    items(connectedPeers, key = { it.id }) { peer ->
                        val lastMessage = messages.lastOrNull {
                            (it.senderId == currentUser.id && it.recipientId == peer.id) ||
                                (it.senderId == peer.id && it.recipientId == currentUser.id)
                        }

                        val conn = acceptedConnections.find {
                            (it.requesterId == peer.id && it.receiverId == currentUser.id) ||
                                (it.requesterId == currentUser.id && it.receiverId == peer.id)
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectConversation(peer.id) }
                                .testTag("conversation_item_${peer.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                UserAvatar(initials = peer.avatarInitials, sizeDp = 46)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = peer.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = WorkCircleNavy
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (peer.isVerified) {
                                            VerifiedBadge(text = "Verified")
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Surface(
                                            color = Color(0xFFEFF6FF),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (conn?.isMessageRequest == true) "Accepted Request" else "Connected",
                                                fontSize = 9.sp,
                                                color = WorkCircleBlue,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = peer.headline,
                                        fontSize = 11.sp,
                                        color = WorkCircleSecondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = lastMessage?.body ?: "Tap to start conversation with ${peer.fullName}",
                                        fontSize = 12.sp,
                                        fontWeight = if (lastMessage != null) FontWeight.Normal else FontWeight.Medium,
                                        color = if (lastMessage != null) WorkCircleNavy.copy(alpha = 0.85f) else WorkCircleBlue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Requests Tab
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize().testTag("requests_list")
            ) {
                // 1. Incoming Requests
                item {
                    Text(
                        text = "Incoming Requests (${incomingRequests.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = WorkCircleNavy
                    )
                }

                if (incomingRequests.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "No pending requests from other professionals.",
                                    fontSize = 12.sp,
                                    color = WorkCircleSecondaryText
                                )
                            }
                        }
                    }
                } else {
                    items(incomingRequests, key = { it.id }) { req ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("incoming_request_${req.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(initials = req.requesterAvatar, sizeDp = 42)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = req.requesterName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = WorkCircleNavy
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFFEF3C7),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (req.isMessageRequest) "Message Request" else "Connect Request",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB45309),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = req.requesterHeadline,
                                            fontSize = 11.sp,
                                            color = WorkCircleSecondaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (req.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "\"${req.note}\"",
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = WorkCircleNavy,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { onDeclineRequest(req.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Decline", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { onAcceptRequest(req.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("accept_request_button_${req.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Outgoing Pending Requests
                if (outgoingRequests.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Sent Requests (${outgoingRequests.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = WorkCircleNavy
                        )
                    }

                    items(outgoingRequests, key = { it.id }) { req ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                UserAvatar(initials = req.receiverAvatar, sizeDp = 40)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = req.receiverName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = WorkCircleNavy
                                    )
                                    Text(
                                        text = req.receiverHeadline,
                                        fontSize = 11.sp,
                                        color = WorkCircleSecondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Pending",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = WorkCircleSecondaryText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
