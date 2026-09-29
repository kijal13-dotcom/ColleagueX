package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CommentEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.PostEntity
import com.example.ui.components.FeedCommentsSheet
import com.example.ui.components.PostCard
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(
    community: CommunityEntity?,
    posts: List<PostEntity>,
    onBack: () -> Unit,
    onJoinToggle: (Long, Boolean) -> Unit,
    onPostClick: (Long) -> Unit,
    onLikeClick: (Long, Boolean) -> Unit,
    onSaveClick: (Long, Boolean) -> Unit,
    onCommentClick: (Long) -> Unit,
    onVoteOption: (PostEntity, Int) -> Unit,
    onReportClick: (String, Long, String) -> Unit,
    onCreatePostInCommunity: () -> Unit,
    onAddComment: ((postId: Long, commentText: String, isAnonymous: Boolean) -> Unit)? = null,
    onCommentLikeClick: ((commentId: Long, currentLiked: Boolean) -> Unit)? = null,
    getCommentsForPost: ((postId: Long) -> Flow<List<CommentEntity>>)? = null,
    modifier: Modifier = Modifier
) {
    var activeCommentsPost by remember { mutableStateOf<PostEntity?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WorkCircleNavy)
                    }
                },
                title = {
                    Text(community?.name ?: "Circle", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WorkCircleNavy)
                }
            )
        },
        containerColor = WorkCircleBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreatePostInCommunity,
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                text = { Text("Post in Circle", color = Color.White, fontWeight = FontWeight.Bold) },
                containerColor = WorkCircleBlue,
                shape = RoundedCornerShape(16.dp)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (community == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text("Community not found", color = WorkCircleSecondaryText)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header Banner Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Decorative gradient header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(WorkCircleBlue, Color(0xFF1E3A8A))
                                        )
                                    )
                            )

                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = community.name,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp,
                                            color = WorkCircleNavy
                                        )
                                        Text(
                                            text = "${community.category} • ${community.memberCount} members • ${community.privacyType}",
                                            fontSize = 12.sp,
                                            color = WorkCircleSecondaryText,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    if (community.isJoined) {
                                        OutlinedButton(
                                            onClick = { onJoinToggle(community.id, true) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Joined", color = WorkCircleBlue, fontSize = 12.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = { onJoinToggle(community.id, false) },
                                            colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Join Circle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = community.description,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = WorkCircleNavy
                                )
                            }
                        }
                    }
                }

                // Community Rules Card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Circle Guidelines",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = WorkCircleNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = community.rules,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = WorkCircleNavy.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Discussions (${posts.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = WorkCircleNavy,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (posts.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text("No discussions in this circle yet.", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Be the first to start a conversation.", fontSize = 12.sp, color = WorkCircleSecondaryText)
                            }
                        }
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
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
                            onReportClick = { onReportClick("POST", post.id, post.title) }
                        )
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
