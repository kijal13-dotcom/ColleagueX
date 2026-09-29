package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.FirestoreCommentItem
import com.example.data.repository.FirestorePost
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleNavyLight
import com.example.ui.theme.WorkCirclePurpleAccent
import com.example.ui.theme.WorkCirclePurpleContainer
import com.example.ui.theme.WorkCircleRedDestructive
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen
import com.example.ui.theme.WorkCircleVerifiedGreenContainer
import kotlinx.coroutines.launch

/**
 * Composable function that displays a list of community posts fetched from the Firestore repository,
 * including complete support for post content, rich media/poll elements, and author details.
 *
 * @param firestoreRepository Repository responsible for fetching Firestore documents and listening to updates.
 * @param communityId Optional community filter ID.
 * @param pillarScope Optional pillar scope filter (e.g., "LOCAL", "INDIA", "GLOBAL").
 * @param onPostClick Callback invoked when a user taps a post card.
 * @param onAuthorClick Callback invoked when a user taps an author avatar or profile link.
 * @param onTagClick Callback invoked when a user selects a hashtag or category chip.
 * @param onCommentClick Callback invoked when opening comments for a post.
 * @param onShareClick Callback invoked when sharing a post.
 * @param onReportClick Callback invoked when flagging or reporting a post for community safety.
 */
@Composable
fun FirestoreCommunityPostsList(
    firestoreRepository: FirestoreRepository = remember { FirestoreRepository() },
    communityId: Long? = null,
    pillarScope: String? = null,
    currentUserId: String = "current_user",
    currentUserName: String = "You",
    currentUserHeadline: String = "Verified Professional",
    currentUserAvatar: String = "CX",
    onPostClick: (FirestorePost) -> Unit = {},
    onAuthorClick: (FirestorePost) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onCommentClick: ((FirestorePost) -> Unit)? = null,
    onShareClick: (FirestorePost) -> Unit = {},
    onReportClick: (FirestorePost) -> Unit = {},
    onCreatePostClick: () -> Unit = {},
    showCreateFab: Boolean = true,
    headerContent: (@Composable () -> Unit)? = null,
    emptyStateContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Observe community posts from Firestore in real-time
    val postsFlow = remember(firestoreRepository, communityId, pillarScope) {
        firestoreRepository.observeCommunityPosts(
            communityId = communityId,
            pillarScope = pillarScope
        )
    }

    val postsState by postsFlow.collectAsStateWithLifecycle(initialValue = null)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("firestore_community_posts_list")
    ) {
        when (val posts = postsState) {
            null -> {
                // Loading State
                FirestoreFeedLoadingState(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                if (posts.isEmpty()) {
                    // Empty State
                    if (emptyStateContent != null) {
                        emptyStateContent()
                    } else {
                        FirestoreFeedEmptyState(
                            communityId = communityId,
                            pillarScope = pillarScope,
                            onCreatePostClick = onCreatePostClick,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp)
                        )
                    }
                } else {
                    // Feed List
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Optional header
                        if (headerContent != null) {
                            item(key = "firestore_feed_header") {
                                headerContent()
                            }
                        }

                        // Firestore Live Sync Status Banner
                        item(key = "firestore_sync_status_badge") {
                            FirestoreSyncBanner(
                                postCount = posts.size,
                                pillarScope = pillarScope,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        // Community Posts
                        items(
                            items = posts,
                            key = { it.id.ifBlank { "post_${it.createdAt}_${it.title.hashCode()}" } }
                        ) { post ->
                            FirestorePostCard(
                                post = post,
                                currentUserId = currentUserId,
                                onPostClick = { onPostClick(post) },
                                onAuthorClick = { onAuthorClick(post) },
                                onTagClick = onTagClick,
                                onLikeClick = { isLiked ->
                                    coroutineScope.launch {
                                        firestoreRepository.togglePostLike(
                                            postId = post.id,
                                            userId = currentUserId,
                                            isLiked = isLiked
                                        )
                                    }
                                },
                                onAddComment = { commentText, isAnonymous ->
                                    coroutineScope.launch {
                                        val newComment = FirestoreCommentItem(
                                            id = "cmt_${System.currentTimeMillis()}",
                                            postId = post.id,
                                            authorId = if (isAnonymous) "anon_${System.currentTimeMillis() % 10000}" else currentUserId,
                                            authorName = if (isAnonymous) "Anonymous Professional" else currentUserName,
                                            authorHeadline = if (isAnonymous) "Verified Industry Peer" else currentUserHeadline,
                                            authorAvatar = if (isAnonymous) "AP" else currentUserAvatar,
                                            isAnonymous = isAnonymous,
                                            body = commentText,
                                            createdAt = System.currentTimeMillis()
                                        )
                                        firestoreRepository.addCommentToPost(post.id, newComment)
                                    }
                                },
                                onCommentClick = { onCommentClick?.invoke(post) },
                                onShareClick = { onShareClick(post) },
                                onReportClick = { onReportClick(post) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button to write new post and save to Firestore
        if (showCreateFab) {
            FirestoreCreatePostFab(
                onClick = onCreatePostClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            )
        }
    }
}

/**
 * Card component displaying an individual Firestore community post with author details,
 * verified status, post content, polls, tags, and action buttons.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirestorePostCard(
    post: FirestorePost,
    currentUserId: String = "",
    onPostClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onTagClick: (String) -> Unit,
    onLikeClick: (Boolean) -> Unit,
    onAddComment: ((String, Boolean) -> Unit)? = null,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onReportClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isLiked by remember(post.id, post.likedByUsers, currentUserId) {
        mutableStateOf(if (currentUserId.isNotBlank()) post.likedByUsers.contains(currentUserId) else false)
    }
    var likesCount by remember(post.id, post.likesCount) { mutableIntStateOf(post.likesCount) }
    var isSaved by remember(post.id) { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var userVotedIndex by remember { mutableIntStateOf(-1) }
    var showInlineComments by remember { mutableStateOf(false) }
    var inlineCommentText by remember { mutableStateOf("") }
    var isInlineCommentAnonymous by remember { mutableStateOf(false) }

    val likeTint by animateColorAsState(
        targetValue = if (isLiked) WorkCircleRedDestructive else WorkCircleSecondaryText,
        label = "like_tint"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(WorkCircleCardBorder)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick() }
            .testTag("firestore_post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ==========================================
            // AUTHOR DETAILS HEADER
            // ==========================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Author Avatar
                val avatarInitials = if (post.authorAvatar.isNotBlank()) {
                    post.authorAvatar
                } else {
                    post.authorName.split(" ").filter { it.isNotBlank() }
                        .map { it.first() }.take(2).joinToString("").uppercase()
                }

                UserAvatar(
                    initials = avatarInitials.ifBlank { "CX" },
                    isAnonymous = post.isAnonymous,
                    sizeDp = 44,
                    modifier = Modifier
                        .clickable(enabled = !post.isAnonymous) { onAuthorClick() }
                        .testTag("firestore_post_avatar_${post.id}")
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Author Name, Title & Post Timestamp
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (post.isAnonymous) "Anonymous Professional" else post.authorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = WorkCircleNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("firestore_post_author_name_${post.id}")
                        )

                        if (post.isAnonymous) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Anonymous Post",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Protected",
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Professional",
                                tint = WorkCircleVerifiedGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = WorkCircleSecondaryText,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Relative timestamp
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Timestamp",
                                tint = WorkCircleSecondaryText,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = formatRelativeTime(post.createdAt),
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }

                    // Author Headline or Anonymous Badge
                    Text(
                        text = if (post.isAnonymous && post.anonymousBadge.isNotBlank()) {
                            post.anonymousBadge
                        } else if (post.authorHeadline.isNotBlank()) {
                            post.authorHeadline
                        } else {
                            "Verified Colleague • ${post.communityName}"
                        },
                        fontSize = 12.sp,
                        color = WorkCircleSecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("firestore_post_author_headline_${post.id}")
                    )
                }

                // Overflow / Options Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("firestore_post_menu_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Post options",
                            tint = WorkCircleSecondaryText
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Report Post") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = "Report",
                                    tint = WorkCircleRedDestructive
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onReportClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Link") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = WorkCircleBlue
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onShareClick()
                            }
                        )
                        if (onDeleteClick != null) {
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = WorkCircleRedDestructive) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = WorkCircleRedDestructive
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // COMMUNITY & POST TYPE BADGES
            // ==========================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (post.communityName.isNotBlank()) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = post.communityName,
                            color = WorkCircleBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    color = WorkCirclePurpleContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = post.postType.replace("_", " "),
                        color = WorkCirclePurpleAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (post.city.isNotBlank()) {
                    Text(
                        text = post.city,
                        color = WorkCircleSecondaryText,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // POST CONTENT: TITLE & BODY
            // ==========================================
            Text(
                text = post.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WorkCircleNavy,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firestore_post_title_${post.id}")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = post.body,
                fontSize = 14.sp,
                color = WorkCircleNavyLight,
                lineHeight = 20.sp,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firestore_post_body_${post.id}")
            )

            // ==========================================
            // POLL SECTION (IF APPLICABLE)
            // ==========================================
            if (post.pollOptions.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                FirestorePollSection(
                    optionsString = post.pollOptions,
                    votesString = post.pollVotes,
                    userVotedIndex = userVotedIndex,
                    onVote = { index ->
                        userVotedIndex = index
                    }
                )
            }

            // ==========================================
            // HASHTAGS / TAGS CHIPS
            // ==========================================
            val tagsList = remember(post.tags) {
                post.tags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }

            if (tagsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagsList.forEach { rawTag ->
                        val formattedTag = if (rawTag.startsWith("#")) rawTag else "#$rawTag"
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.clickable { onTagClick(formattedTag) }
                        ) {
                            Text(
                                text = formattedTag,
                                fontSize = 11.sp,
                                color = WorkCircleBlue,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // ENGAGEMENT & ACTIONS BAR
            // ==========================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val newLiked = !isLiked
                            isLiked = newLiked
                            likesCount += if (newLiked) 1 else -1
                            onLikeClick(newLiked)
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("firestore_post_like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like post",
                        tint = likeTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (likesCount > 0) likesCount.toString() else "Like",
                        fontSize = 12.sp,
                        fontWeight = if (isLiked) FontWeight.Bold else FontWeight.Medium,
                        color = if (isLiked) WorkCircleRedDestructive else WorkCircleSecondaryText
                    )
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showInlineComments = !showInlineComments
                            onCommentClick()
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("firestore_post_comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment on post",
                        tint = if (showInlineComments) WorkCircleBlue else WorkCircleSecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (post.commentsCount > 0) post.commentsCount.toString() else "Comment",
                        fontSize = 12.sp,
                        fontWeight = if (showInlineComments) FontWeight.Bold else FontWeight.Medium,
                        color = if (showInlineComments) WorkCircleBlue else WorkCircleSecondaryText
                    )
                }

                // Share Button
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("firestore_post_share_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share post",
                        tint = WorkCircleSecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Save / Bookmark Button
                IconButton(
                    onClick = { isSaved = !isSaved },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("firestore_post_bookmark_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark post",
                        tint = if (isSaved) WorkCircleBlue else WorkCircleSecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ==========================================
            // INLINE COMMENTS & DISCUSSION SECTION
            // ==========================================
            AnimatedVisibility(visible = showInlineComments) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Discussion & Comments (${post.comments.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = WorkCircleNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (post.comments.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            post.comments.forEach { comment ->
                                FirestoreCommentRow(comment = comment)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Text(
                            text = "No comments yet. Share your workplace perspective!",
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (onAddComment != null) {
                        OutlinedTextField(
                            value = inlineCommentText,
                            onValueChange = { inlineCommentText = it },
                            placeholder = { Text("Add professional insight or reply...", fontSize = 12.sp) },
                            singleLine = false,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WorkCircleBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firestore_comment_input_${post.id}")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = isInlineCommentAnonymous,
                                    onCheckedChange = { isInlineCommentAnonymous = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = WorkCircleNavy,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier.size(width = 44.dp, height = 28.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isInlineCommentAnonymous) "Anonymous" else "Verified",
                                    fontSize = 11.sp,
                                    color = if (isInlineCommentAnonymous) WorkCircleNavy else WorkCircleSecondaryText,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    if (inlineCommentText.isNotBlank()) {
                                        onAddComment(inlineCommentText.trim(), isInlineCommentAnonymous)
                                        inlineCommentText = ""
                                    }
                                },
                                enabled = inlineCommentText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("firestore_post_comment_submit_${post.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Submit Comment",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Display item for an individual comment on a Firestore community post.
 */
@Composable
fun FirestoreCommentRow(comment: FirestoreCommentItem) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    initials = comment.authorAvatar.ifBlank { "CX" },
                    isAnonymous = comment.isAnonymous,
                    sizeDp = 28
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (comment.isAnonymous) "Anonymous Colleague" else comment.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = WorkCircleNavy
                    )
                    Text(
                        text = if (comment.isAnonymous) "Verified Professional" else comment.authorHeadline.ifBlank { "ColleagueX Member" },
                        fontSize = 10.sp,
                        color = WorkCircleSecondaryText
                    )
                }
                Text(
                    text = formatRelativeTime(comment.createdAt),
                    fontSize = 10.sp,
                    color = WorkCircleSecondaryText
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = comment.body,
                fontSize = 13.sp,
                color = WorkCircleNavyLight,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Poll choices with interactive voting option and visual progress distribution.
 */
@Composable
fun FirestorePollSection(
    optionsString: String,
    votesString: String,
    userVotedIndex: Int,
    onVote: (Int) -> Unit
) {
    val options = remember(optionsString) {
        optionsString.split("|").map { it.trim() }.filter { it.isNotBlank() }
    }
    val votes = remember(votesString, options.size) {
        val parsed = votesString.split("|").mapNotNull { it.toIntOrNull() }
        if (parsed.size == options.size) parsed else List(options.size) { 0 }
    }

    val totalVotes = remember(votes, userVotedIndex) {
        votes.sum() + (if (userVotedIndex >= 0) 1 else 0)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Poll,
                    contentDescription = "Community Poll",
                    tint = WorkCircleBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Community Poll • $totalVotes total votes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkCircleNavy
                )
            }

            options.forEachIndexed { index, option ->
                val optionVoteCount = (votes.getOrElse(index) { 0 }) + (if (userVotedIndex == index) 1 else 0)
                val percentage = if (totalVotes > 0) (optionVoteCount.toFloat() / totalVotes.toFloat()) else 0f
                val isSelected = userVotedIndex == index

                Surface(
                    color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) WorkCircleBlue else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onVote(index) }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = WorkCircleNavy,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${(percentage * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) WorkCircleBlue else WorkCircleSecondaryText
                            )
                        }

                        if (totalVotes > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { percentage },
                                color = if (isSelected) WorkCircleBlue else Color(0xFF94A3B8),
                                trackColor = Color(0xFFE2E8F0),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual banner showing the live real-time synchronization state with Firestore.
 */
@Composable
fun FirestoreSyncBanner(
    postCount: Int,
    pillarScope: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(WorkCircleVerifiedGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Cloud Synced",
                tint = WorkCircleBlue,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Live Firestore Feed • $postCount ${if (postCount == 1) "post" else "posts"}${if (pillarScope != null) " ($pillarScope)" else ""}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = WorkCircleNavy
            )
        }
    }
}

/**
 * Loading state with progress indicator and friendly message.
 */
@Composable
fun FirestoreFeedLoadingState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp)
    ) {
        CircularProgressIndicator(
            color = WorkCircleBlue,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Loading community discussions...",
            fontSize = 14.sp,
            color = WorkCircleSecondaryText,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Empty state card shown when no posts exist in the selected Firestore collection.
 */
@Composable
fun FirestoreFeedEmptyState(
    communityId: Long?,
    pillarScope: String?,
    onCreatePostClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(WorkCircleCardBorder)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = "No community posts",
                    tint = WorkCircleBlue,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Community Posts Yet",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = WorkCircleNavy
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Be the first professional to start a conversation, ask for workplace advice, or share an insight!",
                fontSize = 13.sp,
                color = WorkCircleSecondaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCreatePostClick,
                colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("empty_state_create_post_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Write New Post", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
