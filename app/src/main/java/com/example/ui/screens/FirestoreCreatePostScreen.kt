package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommunityEntity
import com.example.data.local.UserEntity
import com.example.data.model.PostType
import com.example.data.repository.FirestorePost
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.ConfidentialityWarningBanner
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleNavyLight
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen
import kotlinx.coroutines.launch

/**
 * Screen that allows users to write new text-based posts and save them
 * directly to the Firebase Firestore database with comprehensive author metadata.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FirestoreCreatePostScreen(
    communities: List<CommunityEntity>,
    currentUser: UserEntity? = null,
    firestoreRepository: FirestoreRepository = remember { FirestoreRepository() },
    onBack: () -> Unit,
    onPostPublished: (FirestorePost) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var selectedCommunity by remember {
        mutableStateOf(
            communities.firstOrNull() ?: CommunityEntity(
                id = 1,
                name = "Tech & AI Engineers",
                slug = "tech",
                description = "Engineering insights",
                category = "Technology",
                memberCount = 2800
            )
        )
    }
    var communityDropdownExpanded by remember { mutableStateOf(false) }

    var postType by remember { mutableStateOf(PostType.DISCUSSION) }
    var pillarScope by remember { mutableStateOf("INDIA") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }

    var isPublishing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var publishSuccess by remember { mutableStateOf(false) }

    val quickSuggestedTags = listOf(
        "CareerAdvice", "TechTips", "Leadership", "Productivity", "Compensation", "HybridWork", "SystemDesign", "InterviewPrep"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("firestore_create_post_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WorkCircleNavy
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Create Cloud Post",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = WorkCircleNavy
                        )
                        Text(
                            text = "Syncs with Firebase Firestore",
                            fontSize = 11.sp,
                            color = WorkCircleBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please enter a title for your post"
                                return@Button
                            }
                            if (body.isBlank()) {
                                errorMessage = "Please provide content in the body of your post"
                                return@Button
                            }

                            isPublishing = true
                            errorMessage = null

                            // Build author metadata
                            val authorUid = if (isAnonymous) {
                                "anon_${System.currentTimeMillis() % 10000}"
                            } else {
                                currentUser?.firebaseUid?.ifBlank { currentUser.id.toString() } ?: "user_${System.currentTimeMillis()}"
                            }

                            val authorName = if (isAnonymous) {
                                "Anonymous Professional"
                            } else {
                                currentUser?.fullName ?: "Verified Professional"
                            }

                            val authorHeadline = if (isAnonymous) {
                                "Verified ${currentUser?.role?.ifBlank { "Professional" } ?: "Professional"} @ ${currentUser?.industry?.ifBlank { "Technology" } ?: "Technology"}"
                            } else {
                                currentUser?.headline?.ifBlank { "${currentUser.role} • ${currentUser.employer}" } ?: "Verified Colleague"
                            }

                            val authorAvatar = if (isAnonymous) {
                                "AP"
                            } else {
                                currentUser?.avatarInitials ?: "CX"
                            }

                            val anonymousBadge = if (isAnonymous) {
                                "Verified ${currentUser?.role?.ifBlank { "Peer" } ?: "Peer"} @ ${currentUser?.industry?.ifBlank { "Tech" } ?: "Tech"}"
                            } else {
                                ""
                            }

                            val newPost = FirestorePost(
                                id = "", // Will be assigned by Firestore
                                authorId = authorUid,
                                authorName = authorName,
                                authorHeadline = authorHeadline,
                                authorAvatar = authorAvatar,
                                isAnonymous = isAnonymous,
                                anonymousBadge = anonymousBadge,
                                communityId = selectedCommunity.id,
                                communityName = selectedCommunity.name,
                                postType = postType.name,
                                title = title.trim(),
                                body = body.trim(),
                                tags = tagsInput.trim(),
                                pollOptions = "",
                                pollVotes = "",
                                likesCount = 0,
                                commentsCount = 0,
                                likedByUsers = emptyList(),
                                comments = emptyList(),
                                pillarScope = pillarScope,
                                city = currentUser?.location?.ifBlank { "Bengaluru" } ?: "Bengaluru",
                                moderationStatus = "PUBLISHED",
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )

                            coroutineScope.launch {
                                val result = firestoreRepository.createPost(newPost)
                                isPublishing = false
                                if (result.isSuccess) {
                                    publishSuccess = true
                                    val assignedId = result.getOrNull() ?: ""
                                    onPostPublished(newPost.copy(id = assignedId))
                                } else {
                                    errorMessage = "Error publishing to Firestore: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
                                }
                            }
                        },
                        enabled = !isPublishing && title.isNotBlank() && body.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("firestore_publish_top_button")
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("Publish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            )
        },
        containerColor = WorkCircleBackground,
        modifier = modifier.testTag("firestore_create_post_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Firestore Cloud Storage Indicator Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firestore_storage_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WorkCircleBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Target: Firebase Firestore Document Storage",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = WorkCircleNavy
                        )
                        Text(
                            text = "Posts are saved to the cloud 'posts' collection with author metadata & real-time updates.",
                            fontSize = 11.sp,
                            color = WorkCircleSecondaryText
                        )
                    }
                }
            }

            ConfidentialityWarningBanner()

            // ==========================================
            // AUTHOR METADATA PREVIEW CARD
            // ==========================================
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("author_metadata_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Author Metadata Preview",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleSecondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            initials = if (isAnonymous) "AP" else (currentUser?.avatarInitials ?: "CX"),
                            isAnonymous = isAnonymous,
                            sizeDp = 44
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAnonymous) "Anonymous Professional" else (currentUser?.fullName ?: "Verified Professional"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = WorkCircleNavy
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                if (isAnonymous) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Anonymous",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = WorkCircleVerifiedGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isAnonymous) {
                                    "Verified ${currentUser?.role ?: "Peer"} @ ${currentUser?.industry ?: "Tech"}"
                                } else {
                                    currentUser?.headline?.ifBlank { "${currentUser.role} • ${currentUser.employer}" } ?: "Verified Colleague"
                                },
                                fontSize = 12.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }
                }
            }

            // ==========================================
            // COMMUNITY PICKER
            // ==========================================
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Publish to Community",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { communityDropdownExpanded = true }
                                .testTag("community_selector_dropdown")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = selectedCommunity.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = WorkCircleNavy
                                    )
                                    Text(
                                        text = "${selectedCommunity.category} • ${selectedCommunity.memberCount} members",
                                        fontSize = 11.sp,
                                        color = WorkCircleSecondaryText
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select community",
                                    tint = WorkCircleNavy
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = communityDropdownExpanded,
                            onDismissRequest = { communityDropdownExpanded = false }
                        ) {
                            communities.forEach { comm ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(comm.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text(comm.category, fontSize = 11.sp, color = WorkCircleSecondaryText)
                                        }
                                    },
                                    onClick = {
                                        selectedCommunity = comm
                                        communityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // POST TYPE SELECTOR
            // ==========================================
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Post Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val types = listOf(
                        PostType.DISCUSSION to "Discussion",
                        PostType.QUESTION to "Question",
                        PostType.KNOWLEDGE_INSIGHT to "Insight",
                        PostType.WORKPLACE_STORY to "Story"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        types.forEach { (type, label) ->
                            val isSelected = postType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { postType = type },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WorkCircleNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // ==========================================
            // POST TITLE & BODY TEXT INPUTS
            // ==========================================
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Post Content",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Post Headline or Question *") },
                        placeholder = { Text("e.g. What are the best practices for system design interviews?") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("firestore_post_title_input")
                            .testTag("post_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Body Field
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Body Text *") },
                        placeholder = { Text("Write your thoughts, knowledge, background context, or question details...") },
                        minLines = 5,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("firestore_post_body_input")
                            .testTag("post_body_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tags Field
                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text("Hashtags / Topics (comma separated)") },
                        placeholder = { Text("e.g. #CareerAdvice, #TechTips") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("firestore_post_tags_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Suggested Tag Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickSuggestedTags.forEach { tag ->
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    tagsInput = if (tagsInput.isBlank()) {
                                        "#$tag"
                                    } else {
                                        "$tagsInput, #$tag"
                                    }
                                }
                            ) {
                                Text(
                                    text = "+ #$tag",
                                    fontSize = 11.sp,
                                    color = WorkCircleBlue,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // ANONYMITY & CONFIDENTIALITY TOGGLE
            // ==========================================
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (isAnonymous) Color(0xFFF8FAFC) else Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isAnonymous) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isAnonymous) Color.White else Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Post Anonymously",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = WorkCircleNavy
                            )
                            Text(
                                text = "Hides personal identity while keeping your role credentials visible.",
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = WorkCircleBlue
                        ),
                        modifier = Modifier.testTag("firestore_anonymous_toggle")
                    )
                }
            }

            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Success Confirmation
            AnimatedVisibility(visible = publishSuccess) {
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = WorkCircleVerifiedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Post successfully published and stored in Firestore!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            // ==========================================
            // PUBLISH BUTTON
            // ==========================================
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a title for your post"
                        return@Button
                    }
                    if (body.isBlank()) {
                        errorMessage = "Please provide content in the body of your post"
                        return@Button
                    }

                    isPublishing = true
                    errorMessage = null

                    val authorUid = if (isAnonymous) {
                        "anon_${System.currentTimeMillis() % 10000}"
                    } else {
                        currentUser?.firebaseUid?.ifBlank { currentUser.id.toString() } ?: "user_${System.currentTimeMillis()}"
                    }

                    val authorName = if (isAnonymous) {
                        "Anonymous Professional"
                    } else {
                        currentUser?.fullName ?: "Verified Professional"
                    }

                    val authorHeadline = if (isAnonymous) {
                        "Verified ${currentUser?.role?.ifBlank { "Professional" } ?: "Professional"} @ ${currentUser?.industry?.ifBlank { "Technology" } ?: "Technology"}"
                    } else {
                        currentUser?.headline?.ifBlank { "${currentUser.role} • ${currentUser.employer}" } ?: "Verified Colleague"
                    }

                    val authorAvatar = if (isAnonymous) {
                        "AP"
                    } else {
                        currentUser?.avatarInitials ?: "CX"
                    }

                    val anonymousBadge = if (isAnonymous) {
                        "Verified ${currentUser?.role?.ifBlank { "Peer" } ?: "Peer"} @ ${currentUser?.industry?.ifBlank { "Tech" } ?: "Tech"}"
                    } else {
                        ""
                    }

                    val newPost = FirestorePost(
                        id = "",
                        authorId = authorUid,
                        authorName = authorName,
                        authorHeadline = authorHeadline,
                        authorAvatar = authorAvatar,
                        isAnonymous = isAnonymous,
                        anonymousBadge = anonymousBadge,
                        communityId = selectedCommunity.id,
                        communityName = selectedCommunity.name,
                        postType = postType.name,
                        title = title.trim(),
                        body = body.trim(),
                        tags = tagsInput.trim(),
                        pollOptions = "",
                        pollVotes = "",
                        likesCount = 0,
                        commentsCount = 0,
                        likedByUsers = emptyList(),
                        comments = emptyList(),
                        pillarScope = pillarScope,
                        city = currentUser?.location?.ifBlank { "Bengaluru" } ?: "Bengaluru",
                        moderationStatus = "PUBLISHED",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    coroutineScope.launch {
                        val result = firestoreRepository.createPost(newPost)
                        isPublishing = false
                        if (result.isSuccess) {
                            publishSuccess = true
                            val assignedId = result.getOrNull() ?: ""
                            onPostPublished(newPost.copy(id = assignedId))
                        } else {
                            errorMessage = "Error publishing to Firestore: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
                        }
                    }
                },
                enabled = !isPublishing && title.isNotBlank() && body.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("firestore_publish_post_button")
                    .testTag("submit_post_button")
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Saving to Firestore...", fontSize = 14.sp, color = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAnonymous) "Publish to Firestore Anonymously" else "Publish to Firestore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
