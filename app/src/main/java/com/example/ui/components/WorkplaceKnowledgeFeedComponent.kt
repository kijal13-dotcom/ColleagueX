package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.local.CommunityEntity
import com.example.data.local.PostEntity
import com.example.data.model.PostType
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.viewmodel.FeedTab

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.example.data.local.CommentEntity

/**
 * Workplace Knowledge Feed Component.
 * Fetches and displays a reactive list of workplace knowledge posts,
 * clearly presenting author name, post title, body content, knowledge category,
 * community context, and rich engagement metrics.
 */
@Composable
fun WorkplaceKnowledgeFeedComponent(
    posts: List<PostEntity>,
    communities: List<CommunityEntity> = emptyList(),
    activeTab: FeedTab = FeedTab.FOR_YOU,
    onTabSelected: (FeedTab) -> Unit = {},
    onPostClick: (Long) -> Unit = {},
    onLikeClick: (Long, Boolean) -> Unit = { _, _ -> },
    onSaveClick: (Long, Boolean) -> Unit = { _, _ -> },
    onCommentClick: (Long) -> Unit = {},
    onVoteOption: (PostEntity, Int) -> Unit = { _, _ -> },
    onReportClick: (String, Long, String) -> Unit = { _, _, _ -> },
    onCreatePostClick: () -> Unit = {},
    onAuthorClick: (Long) -> Unit = {},
    onAddComment: ((postId: Long, commentText: String, isAnonymous: Boolean) -> Unit)? = null,
    onCommentLikeClick: ((commentId: Long, currentLiked: Boolean) -> Unit)? = null,
    getCommentsForPost: ((postId: Long) -> Flow<List<CommentEntity>>)? = null,
    showCreateFab: Boolean = true,
    showConfidentialityBanner: Boolean = true,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<PostType?>(null) }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }
    var activeCommentsPost by remember { mutableStateOf<PostEntity?>(null) }

    // Popular and extracted workplace knowledge tags
    val allAvailableTags by remember(posts) {
        derivedStateOf {
            val extracted = posts.flatMap { post ->
                post.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }
            val primary = listOf("Career Advice", "Tech Tips", "Leadership", "Productivity", "Compensation", "Hybrid Work")
            (primary + extracted).distinct()
        }
    }

    // Filter posts based on tab, category chip, tag, and search query
    val tabFilteredPosts by remember(posts, activeTab, communities) {
        derivedStateOf {
            when (activeTab) {
                FeedTab.COMMUNITY_FEED -> posts.sortedByDescending { it.createdAt }
                FeedTab.FOR_YOU -> posts
                FeedTab.FOLLOWING -> posts.filter { it.likesCount > 50 || it.commentsCount > 10 }
                FeedTab.LATEST -> posts.sortedByDescending { it.createdAt }
                FeedTab.POPULAR -> posts.sortedByDescending { it.likesCount + it.commentsCount }
                FeedTab.MY_COMMUNITIES -> {
                    val joinedIds = communities.filter { it.isJoined }.map { it.id }.toSet()
                    if (joinedIds.isEmpty()) posts.take(5) else posts.filter { it.communityId in joinedIds }
                }
            }
        }
    }

    val displayPosts by remember(tabFilteredPosts, searchQuery, selectedCategoryFilter, selectedTagFilter) {
        derivedStateOf {
            var result = tabFilteredPosts
            if (selectedCategoryFilter != null) {
                result = result.filter { it.postType == selectedCategoryFilter }
            }
            if (selectedTagFilter != null) {
                result = result.filter { post ->
                    post.tags.split(",").any { it.trim().equals(selectedTagFilter, ignoreCase = true) }
                }
            }
            if (searchQuery.isNotBlank()) {
                val keywords = searchQuery.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
                result = result.filter { post ->
                    keywords.all { kw ->
                        post.title.lowercase().contains(kw) ||
                        post.body.lowercase().contains(kw) ||
                        post.authorName.lowercase().contains(kw) ||
                        post.authorHeadline.lowercase().contains(kw) ||
                        post.communityName.lowercase().contains(kw) ||
                        post.tags.lowercase().contains(kw) ||
                        post.anonymousBadge.lowercase().contains(kw)
                    }
                }
            }
            result
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .testTag("workplace_knowledge_feed_component")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Navigation Feed Tabs (For You, Following, Latest, Popular, Communities)
            ScrollableTabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = Color.White,
                contentColor = WorkCircleBlue,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (activeTab.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                            color = WorkCircleBlue,
                            height = 3.dp
                        )
                    }
                }
            ) {
                FeedTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) WorkCircleBlue else WorkCircleSecondaryText
                            )
                        },
                        modifier = Modifier.testTag("feed_tab_${tab.name}")
                    )
                }
            }

            // 2. Search Bar for Workplace Knowledge
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                        placeholder = {
                            Text(
                                if (activeTab == FeedTab.COMMUNITY_FEED)
                                    "Search community posts or topics by keywords..."
                                else
                                    "Search knowledge, insights, advice...",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = WorkCircleSecondaryText, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp).testTag("clear_search_button")
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = WorkCircleSecondaryText)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder,
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("feed_search_input")
                            .testTag("community_feed_search_bar")
                            .testTag("community_feed_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Knowledge Category Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            ElevatedFilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("All Posts (${tabFilteredPosts.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = WorkCircleNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        val categories = listOf(
                            PostType.KNOWLEDGE_INSIGHT to "Insights",
                            PostType.QUESTION to "Questions",
                            PostType.WORKPLACE_STORY to "Stories",
                            PostType.POLL to "Polls"
                        )

                        items(categories) { (type, label) ->
                            val isSelected = selectedCategoryFilter == type
                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryFilter = if (isSelected) null else type
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = WorkCircleBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Workplace Knowledge Tag Filters Row (Career Advice, Tech Tips, Leadership, etc.)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = if (selectedTagFilter != null) WorkCircleBlue.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = "Tags",
                                    tint = if (selectedTagFilter != null) WorkCircleBlue else WorkCircleSecondaryText,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Tags",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTagFilter != null) WorkCircleBlue else WorkCircleSecondaryText
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Surface(
                                    color = if (selectedTagFilter == null) WorkCircleNavy else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (selectedTagFilter == null) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .clickable { selectedTagFilter = null }
                                        .testTag("tag_filter_all")
                                ) {
                                    Text(
                                        text = "All Tags",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTagFilter == null) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTagFilter == null) Color.White else WorkCircleSecondaryText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            items(allAvailableTags) { tag ->
                                val isSelected = selectedTagFilter.equals(tag, ignoreCase = true)
                                Surface(
                                    color = if (isSelected) WorkCircleBlue else Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier
                                        .clickable {
                                            selectedTagFilter = if (isSelected) null else tag
                                        }
                                        .testTag("tag_filter_$tag")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else WorkCircleBlue
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove tag filter",
                                                tint = Color.White,
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

            // 3. Main Posts: Either live Firestore Feed or cached tab LazyColumn
            if (activeTab == FeedTab.COMMUNITY_FEED) {
                FirestoreCommunityPostsList(
                    onPostClick = { firestorePost ->
                        val localId = firestorePost.id.toLongOrNull() ?: 1L
                        onPostClick(localId)
                    },
                    onAuthorClick = { firestorePost ->
                        val authorId = firestorePost.authorId.toLongOrNull() ?: 1L
                        onAuthorClick(authorId)
                    },
                    onTagClick = { tag ->
                        selectedTagFilter = tag.removePrefix("#")
                    },
                    onCommentClick = { firestorePost ->
                        val localId = firestorePost.id.toLongOrNull() ?: 1L
                        onCommentClick(localId)
                    },
                    onReportClick = { firestorePost ->
                        val localId = firestorePost.id.toLongOrNull() ?: 1L
                        onReportClick("POST", localId, firestorePost.title)
                    },
                    headerContent = {
                        if (showConfidentialityBanner) {
                            ConfidentialityWarningBanner(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    },
                    onCreatePostClick = onCreatePostClick,
                    showCreateFab = showCreateFab,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("workplace_knowledge_posts_list")
                ) {
                    if (showConfidentialityBanner) {
                        item {
                            ConfidentialityWarningBanner()
                        }
                    }

                // Active Tag Filter Banner
                if (selectedTagFilter != null) {
                    item {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth().testTag("active_tag_filter_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = WorkCircleBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Filtered by tag: #$selectedTagFilter",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WorkCircleBlue
                                    )
                                }
                                Text(
                                    text = "Clear",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleBlue,
                                    modifier = Modifier
                                        .clickable { selectedTagFilter = null }
                                        .testTag("clear_tag_filter_button")
                                )
                            }
                        }
                    }
                }

                // Header info showing post count
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = WorkCircleBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "Search Results (${displayPosts.size})" else "Knowledge Discussions (${displayPosts.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleNavy
                            )
                        }

                        if (selectedCategoryFilter != null || selectedTagFilter != null || searchQuery.isNotBlank()) {
                            Text(
                                text = "Reset Filters",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WorkCircleBlue,
                                modifier = Modifier
                                    .clickable {
                                        selectedCategoryFilter = null
                                        selectedTagFilter = null
                                        searchQuery = ""
                                    }
                                    .testTag("reset_filters_button")
                            )
                        }
                    }
                }

                // Empty State
                if (displayPosts.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                                .testTag("feed_empty_state_card")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = WorkCircleBlue,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = when {
                                        selectedTagFilter != null -> "No posts found with tag #$selectedTagFilter"
                                        searchQuery.isNotBlank() -> "No discussions matching '$searchQuery'"
                                        else -> "No workplace posts in this category yet"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = WorkCircleNavy
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (selectedTagFilter != null) "Try selecting another tag or create a new post with #$selectedTagFilter." else "Be the first to share an insight, ask a question, or start a discussion with peers.",
                                    fontSize = 12.sp,
                                    color = WorkCircleSecondaryText,
                                    lineHeight = 16.sp
                                )
                                if (selectedTagFilter != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { selectedTagFilter = null },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Show All Posts", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Render Workplace Knowledge Posts with Author Name, Title, and Body
                    items(displayPosts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onPostClick = { onPostClick(post.id) },
                            onLikeClick = { onLikeClick(post.id, post.isLiked) },
                            onSaveClick = { onSaveClick(post.id, post.isSaved) },
                            onCommentClick = {
                                if (getCommentsForPost != null && onAddComment != null) {
                                    activeCommentsPost = post
                                } else {
                                    onCommentClick(post.id)
                                }
                            },
                            onVoteOption = { optionIndex -> onVoteOption(post, optionIndex) },
                            onReportClick = { onReportClick("POST", post.id, post.title) },
                            onAuthorClick = { onAuthorClick(post.authorId) },
                            onTagClick = { tag -> selectedTagFilter = tag }
                        )
                    }
                }
            }
        }
    }

        // 4. Floating Action Button to Create Workplace Knowledge Post
        if (showCreateFab) {
            CreatePostFloatingActionButton(
                onClick = onCreatePostClick,
                text = "Share Knowledge",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            )
        }

        // 5. In-Feed Comment Discussion Sheet (Room DB)
        activeCommentsPost?.let { post ->
            val postCommentsFlow = remember(post.id, getCommentsForPost) {
                getCommentsForPost?.invoke(post.id) ?: flowOf(emptyList())
            }
            val currentComments by postCommentsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

            FeedCommentsSheet(
                post = post,
                comments = currentComments,
                onDismiss = { activeCommentsPost = null },
                onAddComment = { pId, text, anon ->
                    onAddComment?.invoke(pId, text, anon)
                },
                onCommentLikeClick = { cId, liked ->
                    onCommentLikeClick?.invoke(cId, liked)
                }
            )
        }
    }
}
