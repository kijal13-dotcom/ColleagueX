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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CommentEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.PostEntity
import com.example.ui.components.FeedCommentsSheet
import com.example.ui.components.PostCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText

enum class CommunityViewMode(val label: String) {
    COMMUNITY_FEED("Community Feed"),
    EXPLORE_CIRCLES("Explore Circles")
}

/**
 * CommunitiesScreen: Implements the Community Feed feature displaying posts from other users
 * and community members across workplace circles with basic text-based content, timestamps,
 * community filters, search, and interactions, alongside an Explore Circles directory.
 */
@Composable
fun CommunitiesScreen(
    communities: List<CommunityEntity>,
    posts: List<PostEntity> = emptyList(),
    initialTab: CommunityViewMode = CommunityViewMode.COMMUNITY_FEED,
    onCommunityClick: (Long) -> Unit = {},
    onJoinToggle: (Long, Boolean) -> Unit = { _, _ -> },
    onPostClick: (Long) -> Unit = {},
    onLikeClick: (Long, Boolean) -> Unit = { _, _ -> },
    onSaveClick: (Long, Boolean) -> Unit = { _, _ -> },
    onCommentClick: (Long) -> Unit = {},
    onVoteOption: (PostEntity, Int) -> Unit = { _, _ -> },
    onReportClick: (String, Long, String) -> Unit = { _, _, _ -> },
    onAuthorClick: (Long) -> Unit = {},
    onCreatePost: () -> Unit = {},
    onAddComment: ((postId: Long, commentText: String, isAnonymous: Boolean) -> Unit)? = null,
    onCommentLikeClick: ((commentId: Long, currentLiked: Boolean) -> Unit)? = null,
    getCommentsForPost: ((postId: Long) -> Flow<List<CommentEntity>>)? = null,
    modifier: Modifier = Modifier
) {
    var activeViewMode by remember { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCircleFilter by remember { mutableStateOf<Long?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    var activeCommentsPost by remember { mutableStateOf<PostEntity?>(null) }

    val categories = listOf(
        "All",
        "Technology & AI",
        "Banking and Finance",
        "Consulting",
        "Leadership",
        "Human Resources",
        "Remote Work",
        "Career Growth",
        "Healthcare"
    )

    // Popular and extracted workplace topics for quick keyword filtering
    val popularTopics by remember(posts) {
        derivedStateOf {
            val extracted = posts.flatMap { post ->
                post.tags.split(",").map { it.trim().removePrefix("#") }.filter { it.isNotBlank() }
            }
            val defaults = listOf("Salary", "Remote Work", "Leadership", "Career Growth", "Interview", "AI Tools", "Engineering", "Hiring")
            (defaults + extracted).distinct().take(12)
        }
    }

    // Filter community feed posts by circle, topic, and search keywords
    val filteredFeedPosts by remember(posts, selectedCircleFilter, searchQuery, selectedTopic) {
        derivedStateOf {
            var result = posts.sortedByDescending { it.createdAt }
            if (selectedCircleFilter != null) {
                result = result.filter { it.communityId == selectedCircleFilter }
            }
            if (selectedTopic != null) {
                val topicLower = selectedTopic!!.lowercase()
                result = result.filter { post ->
                    post.tags.lowercase().contains(topicLower) ||
                    post.title.lowercase().contains(topicLower) ||
                    post.body.lowercase().contains(topicLower)
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

    // Filter circle directory
    val filteredCommunities by remember(communities, selectedCategory, searchQuery) {
        derivedStateOf {
            communities.filter { community ->
                val matchesCategory = selectedCategory == "All" || community.category.equals(selectedCategory, ignoreCase = true)
                val matchesSearch = searchQuery.isBlank() ||
                    community.name.contains(searchQuery, ignoreCase = true) ||
                    community.description.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (activeViewMode == CommunityViewMode.COMMUNITY_FEED) {
                ExtendedFloatingActionButton(
                    onClick = onCreatePost,
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                    text = { Text("Post in Circle", color = Color.White, fontWeight = FontWeight.Bold) },
                    containerColor = WorkCircleBlue,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("create_community_post_fab")
                )
            }
        },
        containerColor = WorkCircleBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Top Bar with Screen Title & View Mode Selector
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Title and Description
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(WorkCircleBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (activeViewMode == CommunityViewMode.COMMUNITY_FEED) Icons.Default.Forum else Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = WorkCircleBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (activeViewMode == CommunityViewMode.COMMUNITY_FEED) "Community Feed" else "Explore Circles",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 19.sp,
                                        color = WorkCircleNavy
                                    )
                                    Text(
                                        text = if (activeViewMode == CommunityViewMode.COMMUNITY_FEED)
                                            "Posts and insights from community members across circles"
                                        else
                                            "Join verified circles tailored to your role and focus",
                                        fontSize = 11.sp,
                                        color = WorkCircleSecondaryText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Input Field at top of Community Feed
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                            placeholder = {
                                Text(
                                    text = if (activeViewMode == CommunityViewMode.COMMUNITY_FEED)
                                        "Search community posts or topics by keywords..."
                                    else
                                        "Search circles by name or industry...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search posts or topics",
                                    tint = WorkCircleSecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.testTag("clear_search_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = WorkCircleSecondaryText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
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
                                .testTag("communities_search_input")
                                .testTag("community_feed_search_bar")
                                .testTag("community_feed_search_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab Selector: Community Feed vs Explore Circles
                    TabRow(
                        selectedTabIndex = activeViewMode.ordinal,
                        containerColor = Color.White,
                        contentColor = WorkCircleBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[activeViewMode.ordinal]),
                                color = WorkCircleBlue,
                                height = 3.dp
                            )
                        }
                    ) {
                        CommunityViewMode.values().forEach { mode ->
                            val isSelected = activeViewMode == mode
                            Tab(
                                selected = isSelected,
                                onClick = { activeViewMode = mode },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (mode == CommunityViewMode.COMMUNITY_FEED) Icons.Default.DynamicFeed else Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = if (isSelected) WorkCircleBlue else WorkCircleSecondaryText,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = mode.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) WorkCircleBlue else WorkCircleSecondaryText
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("tab_${mode.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Body Content based on active mode
            when (activeViewMode) {
                CommunityViewMode.COMMUNITY_FEED -> {
                    // Community Feed Content
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("community_feed_list")
                    ) {
                        // 1. Topic Keyword Filter Chips Row
                        item {
                            Column(modifier = Modifier.padding(bottom = 2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Tag,
                                            contentDescription = "Topics",
                                            tint = WorkCircleBlue,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Search by Topic:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WorkCircleNavy
                                        )
                                    }
                                    if (selectedTopic != null) {
                                        Text(
                                            text = "Clear Topic",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WorkCircleBlue,
                                            modifier = Modifier
                                                .clickable { selectedTopic = null }
                                                .testTag("clear_topic_filter")
                                        )
                                    }
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("community_topic_chips")
                                ) {
                                    items(popularTopics) { topic ->
                                        val isSelected = selectedTopic.equals(topic, ignoreCase = true)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedTopic = if (isSelected) null else topic
                                            },
                                            label = {
                                                Text(
                                                    text = "#$topic",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = WorkCircleBlue,
                                                selectedLabelColor = Color.White,
                                                containerColor = Color(0xFFF1F5F9),
                                                labelColor = WorkCircleNavy
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.testTag("topic_chip_${topic.lowercase().replace(" ", "_")}")
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Circle Selector Row
                        item {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = WorkCircleBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Filter by Circle:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkCircleNavy
                                    )
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        FilterChip(
                                            selected = selectedCircleFilter == null,
                                            onClick = { selectedCircleFilter = null },
                                            label = { Text("All Circles (${posts.size})", fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = WorkCircleNavy,
                                                selectedLabelColor = Color.White,
                                                containerColor = Color(0xFFF1F5F9),
                                                labelColor = WorkCircleNavy
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.testTag("circle_filter_all")
                                        )
                                    }

                                    items(communities) { circle ->
                                        val isSelected = selectedCircleFilter == circle.id
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedCircleFilter = if (isSelected) null else circle.id
                                            },
                                            label = { Text(circle.name, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = WorkCircleBlue,
                                                selectedLabelColor = Color.White,
                                                containerColor = Color(0xFFF8FAFC),
                                                labelColor = WorkCircleNavy
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.testTag("circle_filter_${circle.id}")
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Active Search / Topic Banner
                        if (searchQuery.isNotBlank() || selectedTopic != null) {
                            item {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("active_search_banner")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = WorkCircleBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val querySummary = buildString {
                                                if (searchQuery.isNotBlank()) append("Keywords: \"$searchQuery\"")
                                                if (selectedTopic != null) {
                                                    if (isNotEmpty()) append(" • ")
                                                    append("Topic: #$selectedTopic")
                                                }
                                            }
                                            Text(
                                                text = "$querySummary (${filteredFeedPosts.size} found)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = WorkCircleNavy
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                searchQuery = ""
                                                selectedTopic = null
                                            },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("clear_search_banner_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = WorkCircleBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Feed Header Counter
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (selectedCircleFilter != null) {
                                        val circleName = communities.find { it.id == selectedCircleFilter }?.name ?: "Circle"
                                        "$circleName Posts (${filteredFeedPosts.size})"
                                    } else {
                                        "Community Posts (${filteredFeedPosts.size})"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = WorkCircleNavy
                                )

                                if (selectedCircleFilter != null || searchQuery.isNotBlank() || selectedTopic != null) {
                                    Text(
                                        text = "Reset Filters",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WorkCircleBlue,
                                        modifier = Modifier
                                            .clickable {
                                                selectedCircleFilter = null
                                                searchQuery = ""
                                                selectedTopic = null
                                            }
                                            .testTag("reset_circle_filters")
                                    )
                                }
                            }
                        }

                        // 5. Feed Posts / Empty Search State
                        if (filteredFeedPosts.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("empty_search_results")
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(32.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEFF6FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (searchQuery.isNotBlank() || selectedTopic != null)
                                                    Icons.Default.SearchOff
                                                else
                                                    Icons.Default.Forum,
                                                contentDescription = null,
                                                tint = WorkCircleBlue,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = when {
                                                searchQuery.isNotBlank() -> "No discussions matching '$searchQuery'"
                                                selectedTopic != null -> "No discussions found in topic '#$selectedTopic'"
                                                else -> "No community posts in this circle yet"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = WorkCircleNavy,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (searchQuery.isNotBlank() || selectedTopic != null)
                                                "Try searching with different keywords, exploring popular topics above, or clearing filters."
                                            else
                                                "Be the first to share an insight, ask peers for guidance, or start a discussion.",
                                            fontSize = 12.sp,
                                            color = WorkCircleSecondaryText,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (searchQuery.isNotBlank() || selectedTopic != null || selectedCircleFilter != null) {
                                                OutlinedButton(
                                                    onClick = {
                                                        searchQuery = ""
                                                        selectedTopic = null
                                                        selectedCircleFilter = null
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.testTag("empty_state_clear_search_btn")
                                                ) {
                                                    Icon(
                                                        Icons.Default.Clear,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Clear Search", fontSize = 12.sp)
                                                }
                                            }
                                            Button(
                                                onClick = onCreatePost,
                                                colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Create Discussion", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredFeedPosts, key = { it.id }) { post ->
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
                                    onAuthorClick = { onAuthorClick(post.authorId) }
                                )
                            }
                        }
                    }
                }

                CommunityViewMode.EXPLORE_CIRCLES -> {
                    // Category Chips Bar
                    Surface(
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { category ->
                                val isSelected = selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category, fontSize = 12.sp) },
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

                    // Circles Directory List
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("communities_directory_list")
                    ) {
                        items(filteredCommunities, key = { it.id }) { community ->
                            CommunityCard(
                                community = community,
                                onClick = { onCommunityClick(community.id) },
                                onJoinToggle = { onJoinToggle(community.id, community.isJoined) }
                            )
                        }
                    }
                }
            }
        }

        // In-Feed Comment & Reply Discussion Sheet (backed by Room DB)
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

@Composable
fun CommunityCard(
    community: CommunityEntity,
    onClick: () -> Unit,
    onJoinToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("community_card_${community.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEBF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = WorkCircleBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = community.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WorkCircleNavy
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = community.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WorkCircleBlue
                            )
                            Text(
                                text = " • ${community.memberCount} members",
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }
                }

                // Join / Leave Button
                if (community.isJoined) {
                    OutlinedButton(
                        onClick = onJoinToggle,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("join_toggle_button_${community.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Joined", fontSize = 12.sp, color = WorkCircleBlue)
                    }
                } else {
                    Button(
                        onClick = onJoinToggle,
                        colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("join_toggle_button_${community.id}")
                    ) {
                        Text("Join", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = community.description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = WorkCircleNavy.copy(alpha = 0.85f),
                maxLines = 2
            )
        }
    }
}
