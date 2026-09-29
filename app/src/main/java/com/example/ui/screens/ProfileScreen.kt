package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.AuthState
import com.example.data.local.PostEntity
import com.example.data.local.SkillEndorsementEntity
import com.example.data.local.UserEntity
import com.example.data.repository.FirestoreCommentItem
import com.example.data.repository.FirestorePost
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.toFirestorePost
import com.example.ui.components.FirestorePostCard
import com.example.ui.components.SkillEndorsementSheet
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCirclePurpleAccent
import com.example.ui.theme.WorkCirclePurpleContainer
import com.example.ui.theme.WorkCircleRedDestructive
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen
import kotlinx.coroutines.launch

/**
 * Filter tab categories for authored posts on the profile screen.
 */
enum class ProfilePostsFilter(val label: String) {
    ALL("All Posts"),
    DISCUSSIONS("Discussions"),
    QUESTIONS("Questions"),
    POLLS("Polls")
}

/**
 * Profile screen that displays the current user's complete profile information,
 * professional statistics, verified credentials, and lists all posts they have
 * personally created (with real-time Firestore tracking, interactive Like and Comment buttons,
 * and post management).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    user: UserEntity?,
    userPosts: List<PostEntity> = emptyList(),
    isCurrentUser: Boolean = true,
    authState: AuthState = AuthState.Unauthenticated,
    endorsements: List<SkillEndorsementEntity> = emptyList(),
    currentUserId: Long = 1,
    firestoreRepository: FirestoreRepository = remember { FirestoreRepository() },
    onCreatePostClick: () -> Unit = {},
    onEndorseSkill: (skillName: String, note: String) -> Unit = { _, _ -> },
    onSaveProfile: (displayName: String, jobTitle: String, bio: String, headline: String, employer: String, location: String, skills: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateToVerification: () -> Unit = {},
    onToggleEmployerVisibility: (Boolean) -> Unit = {},
    onToggleLocationVisibility: (Boolean) -> Unit = {},
    onPostClick: (Long) -> Unit = {},
    onDeleteLocalPost: ((Long) -> Unit)? = null,
    onSignOut: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (user == null) {
        Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
            Text("User not found", color = WorkCircleSecondaryText, fontSize = 14.sp)
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()

    // Determine current user ID string representations for Firestore
    val userAuthorId = user.id.toString()
    val userFirebaseUid = user.firebaseUid
    val userFullName = user.fullName

    // Observe posts created by this user from Firestore in real-time
    val firestoreUserPosts by remember(userAuthorId, userFirebaseUid, userFullName) {
        firestoreRepository.observePostsByAuthor(
            authorId = userAuthorId,
            authorFirebaseUid = userFirebaseUid,
            authorName = userFullName
        )
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Convert local Room userPosts to FirestorePost format for unified list presentation
    val localConvertedPosts = remember(userPosts) {
        userPosts.map { it.toFirestorePost() }
    }

    // Merge and deduplicate posts so every post authored by the user is visible
    val allUserAuthoredPosts = remember(firestoreUserPosts, localConvertedPosts) {
        val firestoreMap = firestoreUserPosts.associateBy { it.id.ifBlank { "${it.createdAt}_${it.title.hashCode()}" } }
        val mergedList = firestoreUserPosts.toMutableList()

        for (localPost in localConvertedPosts) {
            val key = localPost.id.ifBlank { "${localPost.createdAt}_${localPost.title.hashCode()}" }
            if (!firestoreMap.containsKey(key) && !firestoreMap.containsKey(localPost.id)) {
                mergedList.add(localPost)
            }
        }
        mergedList.sortedByDescending { it.createdAt }
    }

    // UI state
    var selectedFilter by remember { mutableStateOf(ProfilePostsFilter.ALL) }
    var isEditingProfileDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<FirestorePost?>(null) }
    var activeEndorsementSkill by remember { mutableStateOf<Pair<String, Int>?>(null) }

    val skillsList = remember(user.skills) {
        user.skills.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    // Filter posts according to chosen category tab
    val filteredPosts = remember(allUserAuthoredPosts, selectedFilter) {
        when (selectedFilter) {
            ProfilePostsFilter.ALL -> allUserAuthoredPosts
            ProfilePostsFilter.DISCUSSIONS -> allUserAuthoredPosts.filter { it.postType == "DISCUSSION" }
            ProfilePostsFilter.QUESTIONS -> allUserAuthoredPosts.filter { it.postType == "QUERY" || it.title.contains("?", ignoreCase = true) }
            ProfilePostsFilter.POLLS -> allUserAuthoredPosts.filter { it.postType == "POLL" || it.pollOptions.isNotBlank() }
        }
    }

    // Statistics calculations
    val totalPostsCount = allUserAuthoredPosts.size
    val totalLikesReceived = remember(allUserAuthoredPosts) {
        allUserAuthoredPosts.sumOf { it.likesCount }
    }
    val totalCommentsReceived = remember(allUserAuthoredPosts) {
        allUserAuthoredPosts.sumOf { it.commentsCount }
    }

    Box(modifier = modifier.fillMaxSize().background(WorkCircleBackground)) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("user_profile_screen")
        ) {
            // ==========================================
            // 1. HERO PROFILE CARD WITH USER INFORMATION
            // ==========================================
            item(key = "profile_hero_card") {
                Card(
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, WorkCircleCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_hero_card")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Gradient Cover Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            WorkCircleNavy,
                                            Color(0xFF1E3A8A),
                                            WorkCircleBlue
                                        )
                                    )
                                )
                        ) {
                            if (isCurrentUser) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable { isEditingProfileDialog = true }
                                        .testTag("profile_edit_dialog_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Profile",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Profile Header Content (Avatar, Names, Badges)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Avatar with border
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, Color.White, CircleShape)
                                        .background(Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = user.avatarInitials.ifBlank {
                                        user.fullName.split(" ")
                                            .filter { it.isNotBlank() }
                                            .map { it.first() }
                                            .take(2)
                                            .joinToString("")
                                            .uppercase()
                                    }
                                    UserAvatar(
                                        initials = initials.ifBlank { "CX" },
                                        sizeDp = 76,
                                        modifier = Modifier.testTag("profile_user_avatar")
                                    )
                                }

                                // Quick Actions Row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCurrentUser) {
                                        OutlinedButton(
                                            onClick = { isEditingProfileDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, WorkCircleBlue),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WorkCircleBlue),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("edit_profile_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Profile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Button(
                                            onClick = onCreatePostClick,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("profile_create_post_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Create Post", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Name & Handle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = user.fullName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleNavy,
                                    modifier = Modifier.testTag("profile_user_fullname")
                                )

                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Employee",
                                        tint = WorkCircleVerifiedGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "@${user.username.ifBlank { user.email.substringBefore("@") }}",
                                fontSize = 13.sp,
                                color = WorkCircleSecondaryText,
                                modifier = Modifier.testTag("profile_user_username")
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Headline / Role
                            val headline = user.headline.ifBlank { user.role.ifBlank { "Professional Member" } }
                            Text(
                                text = headline,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = WorkCircleBlue,
                                modifier = Modifier.testTag("profile_user_headline")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Employer, Industry & Location Badges
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (user.employer.isNotBlank()) {
                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Business,
                                                contentDescription = null,
                                                tint = WorkCircleNavy,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = user.employer,
                                                fontSize = 12.sp,
                                                color = WorkCircleNavy,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (user.industry.isNotBlank()) {
                                    Surface(
                                        color = WorkCirclePurpleContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Work,
                                                contentDescription = null,
                                                tint = WorkCirclePurpleAccent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = user.industry,
                                                fontSize = 12.sp,
                                                color = WorkCirclePurpleAccent,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (user.location.isNotBlank()) {
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = user.location,
                                                fontSize = 12.sp,
                                                color = Color(0xFF92400E),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. PROFESSIONAL ACTIVITY STATS DASHBOARD
            // ==========================================
            item(key = "profile_stats_dashboard") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Professional Impact",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Metric 1: Authored Posts Count
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, WorkCircleCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_posts_created_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PostAdd,
                                    contentDescription = null,
                                    tint = WorkCircleBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$totalPostsCount",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Posts Created",
                                    fontSize = 11.sp,
                                    color = WorkCircleSecondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Metric 2: Total Likes Earned
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, WorkCircleCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_likes_received_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = WorkCircleRedDestructive,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$totalLikesReceived",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Likes Earned",
                                    fontSize = 11.sp,
                                    color = WorkCircleSecondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Metric 3: Comments Received
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, WorkCircleCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_comments_received_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = WorkCirclePurpleAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$totalCommentsReceived",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Comments",
                                    fontSize = 11.sp,
                                    color = WorkCircleSecondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Metric 4: Colleague Connections
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, WorkCircleCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_connections_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = WorkCircleVerifiedGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${user.connectionsCount}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Connections",
                                    fontSize = 11.sp,
                                    color = WorkCircleSecondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. ABOUT / BIOGRAPHY SECTION
            // ==========================================
            item(key = "profile_bio_card") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkCircleCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("profile_bio_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "About & Professional Summary",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleNavy
                            )

                            if (isCurrentUser) {
                                IconButton(
                                    onClick = { isEditingProfileDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Bio",
                                        tint = WorkCircleBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val displayBio = user.bio.ifBlank {
                            "Experienced professional focused on collaboration, industry insights, and engineering excellence across technology sectors."
                        }
                        Text(
                            text = displayBio,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = WorkCircleNavy.copy(alpha = 0.85f),
                            modifier = Modifier.testTag("profile_bio_text")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = WorkCircleCardBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Application Owners & Governance Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = WorkCircleBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Application Owners & Governance",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleNavy
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = "Official Info",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("application_owners_info_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Application Owner", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Kijal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkCircleNavy)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Owner",
                                            tint = WorkCircleVerifiedGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Owner Email", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                    Text(
                                        text = "kijal13@gmail.com",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = WorkCircleBlue
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Platform Role", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                    Text(
                                        text = "Product Owner & System Architect",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = WorkCircleNavy
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Organization", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                    Text(
                                        text = "ColleagueX Platform Team",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = WorkCircleNavy
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Application Build", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                    Text(
                                        text = "ColleagueX v1.0.0 (Production)",
                                        fontSize = 11.sp,
                                        color = WorkCircleSecondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. SKILLS & COMPETENCIES SECTION
            // ==========================================
            if (skillsList.isNotEmpty()) {
                item(key = "profile_skills_card") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, WorkCircleCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("profile_skills_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Skills & Endorsements",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "${skillsList.size} competencies",
                                    fontSize = 12.sp,
                                    color = WorkCircleSecondaryText
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                skillsList.forEachIndexed { index, skill ->
                                    val skillEndorsementCount = endorsements.count {
                                        it.skillName.equals(skill, ignoreCase = true)
                                    }

                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable {
                                                activeEndorsementSkill = skill to index
                                            }
                                            .testTag("profile_skill_chip_$index")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = skill,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = WorkCircleNavy
                                            )

                                            if (skillEndorsementCount > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = Color(0xFFDBEAFE),
                                                    shape = CircleShape
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = WorkCircleBlue,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text(
                                                            text = "$skillEndorsementCount",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = WorkCircleBlue
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
            }

            // ==========================================
            // 5. EMPLOYMENT VERIFICATION STATUS CARD
            // ==========================================
            if (isCurrentUser) {
                item(key = "profile_verification_card") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (user.isVerified) Color(0xFFECFDF5) else Color(0xFFEFF6FF)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (user.isVerified) Color(0xFFA7F3D0) else Color(0xFFBFDBFE)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("profile_verification_banner")
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
                                Icon(
                                    imageVector = if (user.isVerified) Icons.Default.Verified else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (user.isVerified) WorkCircleVerifiedGreen else WorkCircleBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (user.isVerified) "Employment Verified" else "Employment Verification",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = WorkCircleNavy
                                    )
                                    Text(
                                        text = if (user.isVerified) "Confirmed via official corporate email domain." else "Verify your corporate identity to earn trusted community badges.",
                                        fontSize = 11.sp,
                                        color = WorkCircleSecondaryText
                                    )
                                }
                            }

                            Button(
                                onClick = onNavigateToVerification,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (user.isVerified) WorkCircleVerifiedGreen else WorkCircleBlue
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (user.isVerified) "Status" else "Verify",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 6. CLOUD ACCOUNT & SECURITY CARD
            // ==========================================
            if (isCurrentUser) {
                item(key = "profile_cloud_account_card") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, WorkCircleCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("profile_cloud_account_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = WorkCircleVerifiedGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cloud Account & Synchronization",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = WorkCircleNavy
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFECFDF5)
                                ) {
                                    Text(
                                        text = "Firestore Synced",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkCircleVerifiedGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Registered Email", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                Text(
                                    text = user.email.ifBlank { "colleague@company.com" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = WorkCircleNavy
                                )
                            }

                            if (user.firebaseUid.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Firebase UID", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                    Text(
                                        text = user.firebaseUid.take(14) + "...",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = onNavigateToAuth,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Switch Account", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = onSignOut,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Log Out", fontSize = 11.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 7. POSTS PERSONALLY CREATED BY USER
            // ==========================================
            item(key = "authored_posts_section_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isCurrentUser) "Posts Personally Created by You" else "${user.fullName.substringBefore(" ")}'s Posts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = WorkCircleBlue
                            ) {
                                Text(
                                    text = "${allUserAuthoredPosts.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (isCurrentUser) {
                            TextButton(
                                onClick = onCreatePostClick,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("create_post_text_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = WorkCircleBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Post", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkCircleBlue)
                            }
                        }
                    }

                    Text(
                        text = "Real-time list of posts created by you and synchronized to Firestore.",
                        fontSize = 11.sp,
                        color = WorkCircleSecondaryText,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    // Post Filter Tabs
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfilePostsFilter.values().forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) WorkCircleBlue else Color.White,
                                border = if (isSelected) null else BorderStroke(1.dp, WorkCircleCardBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedFilter = filter }
                                    .testTag("profile_filter_${filter.name.lowercase()}")
                            ) {
                                Text(
                                    text = filter.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else WorkCircleNavy,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Empty State
            if (filteredPosts.isEmpty()) {
                item(key = "authored_posts_empty_state") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, WorkCircleCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("profile_no_posts_card")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 20.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PostAdd,
                                        contentDescription = null,
                                        tint = WorkCircleBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (selectedFilter == ProfilePostsFilter.ALL) {
                                    "You haven't created any posts yet"
                                } else {
                                    "No ${selectedFilter.label.lowercase()} found"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleNavy,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Share your workplace insights, salary benchmarks, questions, or company reviews with your peers on ColleagueX.",
                                fontSize = 12.sp,
                                color = WorkCircleSecondaryText,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            if (isCurrentUser) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onCreatePostClick,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                    modifier = Modifier.testTag("profile_create_first_post_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Create Your First Post",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // List of posts personally created by the user
                items(
                    items = filteredPosts,
                    key = { post -> post.id.ifBlank { "post_${post.createdAt}_${post.title.hashCode()}" } }
                ) { post ->
                    FirestorePostCard(
                        post = post,
                        currentUserId = userAuthorId,
                        onPostClick = {
                            val localId = post.id.toLongOrNull() ?: 1L
                            onPostClick(localId)
                        },
                        onAuthorClick = {
                            // User is already on their profile screen
                        },
                        onTagClick = {},
                        onLikeClick = { isLiked ->
                            coroutineScope.launch {
                                firestoreRepository.togglePostLike(
                                    postId = post.id,
                                    userId = userAuthorId,
                                    isLiked = isLiked
                                )
                            }
                        },
                        onAddComment = { commentText, isAnonymous ->
                            coroutineScope.launch {
                                val commentItem = FirestoreCommentItem(
                                    id = "cmt_${System.currentTimeMillis()}",
                                    postId = post.id,
                                    authorId = if (isAnonymous) "anon_${System.currentTimeMillis() % 10000}" else userAuthorId,
                                    authorName = if (isAnonymous) "Anonymous Professional" else user.fullName,
                                    authorHeadline = if (isAnonymous) "Verified Industry Peer" else user.headline.ifBlank { user.role },
                                    authorAvatar = if (isAnonymous) "AP" else user.avatarInitials,
                                    isAnonymous = isAnonymous,
                                    body = commentText,
                                    createdAt = System.currentTimeMillis()
                                )
                                firestoreRepository.addCommentToPost(post.id, commentItem)
                            }
                        },
                        onCommentClick = {
                            val localId = post.id.toLongOrNull() ?: 1L
                            onPostClick(localId)
                        },
                        onShareClick = {},
                        onReportClick = {},
                        onDeleteClick = {
                            postToDelete = post
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // Floating Action Button to author new post
        if (isCurrentUser) {
            FloatingActionButton(
                onClick = onCreatePostClick,
                containerColor = WorkCircleBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 20.dp)
                    .testTag("profile_fab_create_post")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create New Post",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // ==========================================
    // DELETE POST CONFIRMATION DIALOG
    // ==========================================
    postToDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = {
                Text(
                    text = "Delete Post?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WorkCircleNavy
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${post.title.take(50)}\"? This action will remove it from both your profile and the community feed.",
                    fontSize = 13.sp,
                    color = WorkCircleSecondaryText,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (post.id.isNotBlank()) {
                                firestoreRepository.deletePost(post.id)
                            }
                            post.id.toLongOrNull()?.let { localId ->
                                onDeleteLocalPost?.invoke(localId)
                            }
                            postToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleRedDestructive),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { postToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp, color = WorkCircleSecondaryText)
                }
            }
        )
    }

    // ==========================================
    // EDIT PROFILE DIALOG
    // ==========================================
    if (isEditingProfileDialog) {
        var draftFullName by remember { mutableStateOf(user.fullName) }
        var draftJobTitle by remember { mutableStateOf(user.role.ifBlank { user.headline }) }
        var draftHeadline by remember { mutableStateOf(user.headline) }
        var draftEmployer by remember { mutableStateOf(user.employer) }
        var draftLocation by remember { mutableStateOf(user.location) }
        var draftBio by remember { mutableStateOf(user.bio) }
        var draftSkills by remember { mutableStateOf(user.skills) }

        AlertDialog(
            onDismissRequest = { isEditingProfileDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Edit Profile Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = WorkCircleNavy
                    )
                    IconButton(
                        onClick = { isEditingProfileDialog = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = WorkCircleSecondaryText)
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = draftFullName,
                        onValueChange = { draftFullName = it },
                        label = { Text("Full Name", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = draftJobTitle,
                        onValueChange = { draftJobTitle = it },
                        label = { Text("Job Title / Role", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = draftHeadline,
                        onValueChange = { draftHeadline = it },
                        label = { Text("Professional Headline", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = draftEmployer,
                            onValueChange = { draftEmployer = it },
                            label = { Text("Company", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = draftLocation,
                            onValueChange = { draftLocation = it },
                            label = { Text("Location", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = draftBio,
                        onValueChange = { draftBio = it },
                        label = { Text("Biography", fontSize = 12.sp) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = draftSkills,
                        onValueChange = { draftSkills = it },
                        label = { Text("Skills (comma-separated)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveProfile(
                            draftFullName.trim(),
                            draftJobTitle.trim(),
                            draftBio.trim(),
                            draftHeadline.trim(),
                            draftEmployer.trim(),
                            draftLocation.trim(),
                            draftSkills.trim()
                        )
                        isEditingProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_profile_dialog_button")
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isEditingProfileDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp, color = WorkCircleSecondaryText)
                }
            }
        )
    }

    // ==========================================
    // SKILL ENDORSEMENT BOTTOM SHEET
    // ==========================================
    activeEndorsementSkill?.let { (skillName, skillIndex) ->
        SkillEndorsementSheet(
            skillName = skillName,
            skillIndex = skillIndex,
            targetUserId = user.id,
            currentUserId = currentUserId,
            endorsements = endorsements,
            onDismiss = { activeEndorsementSkill = null },
            onEndorse = { sName, note ->
                onEndorseSkill(sName, note)
            }
        )
    }
}
