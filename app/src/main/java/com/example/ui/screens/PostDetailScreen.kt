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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import com.example.ui.theme.colleagueXTextFieldColors
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.ui.components.PostCard
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.formatRelativeTime
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleRedDestructive
import com.example.ui.theme.WorkCircleSecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: PostEntity?,
    comments: List<CommentEntity>,
    onBack: () -> Unit,
    onLikeClick: (Long, Boolean) -> Unit,
    onSaveClick: (Long, Boolean) -> Unit,
    onVoteOption: (PostEntity, Int) -> Unit,
    onReportClick: (String, Long, String) -> Unit,
    onAddComment: (postId: Long, body: String, isAnonymous: Boolean) -> Unit,
    onCommentLikeClick: (Long, Boolean) -> Unit,
    onAuthorClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var commentAnonymous by remember { mutableStateOf(false) }

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
                    Text("Discussion", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WorkCircleNavy)
                }
            )
        },
        containerColor = WorkCircleBackground,
        bottomBar = {
            // Comment input bar
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (commentAnonymous) WorkCircleBlue else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reply Anonymously",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (commentAnonymous) WorkCircleBlue else WorkCircleSecondaryText
                            )
                        }
                        Switch(
                            checked = commentAnonymous,
                            onCheckedChange = { commentAnonymous = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WorkCircleBlue),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                            placeholder = { Text("Add constructive professional insight...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                            colors = colleagueXTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input_field"),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank() && post != null) {
                                    onAddComment(post.id, commentText.trim(), commentAnonymous)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .background(if (commentText.isNotBlank()) WorkCircleBlue else Color(0xFFE2E8F0), shape = RoundedCornerShape(22.dp))
                                .testTag("send_comment_button")
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
        },
        modifier = modifier
    ) { innerPadding ->
        if (post == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text("Discussion not found", color = WorkCircleSecondaryText)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    PostCard(
                        post = post,
                        onPostClick = {},
                        onLikeClick = { onLikeClick(post.id, post.isLiked) },
                        onSaveClick = { onSaveClick(post.id, post.isSaved) },
                        onCommentClick = {},
                        onVoteOption = { optionIndex -> onVoteOption(post, optionIndex) },
                        onReportClick = { onReportClick("POST", post.id, post.title) },
                        onAuthorClick = { onAuthorClick(post.authorId) }
                    )
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Replies (${comments.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WorkCircleNavy
                        )
                    }
                }

                if (comments.isEmpty()) {
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
                                Text(
                                    text = "No replies yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Share your perspective or experience to get the dialogue started.",
                                    fontSize = 12.sp,
                                    color = WorkCircleSecondaryText
                                )
                            }
                        }
                    }
                } else {
                    items(comments, key = { it.id }) { comment ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    UserAvatar(
                                        initials = comment.authorAvatar,
                                        isAnonymous = comment.isAnonymous,
                                        sizeDp = 34
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = comment.authorName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = WorkCircleNavy
                                            )
                                            if (comment.isAnonymous) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "• Anonymous",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                VerifiedBadge(text = "Verified")
                                            }
                                            Text(
                                                text = "•",
                                                fontSize = 11.sp,
                                                color = WorkCircleSecondaryText,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                            Text(
                                                text = formatRelativeTime(comment.createdAt),
                                                fontSize = 10.sp,
                                                color = WorkCircleSecondaryText
                                            )
                                        }
                                        Text(
                                            text = comment.authorHeadline,
                                            fontSize = 11.sp,
                                            color = WorkCircleSecondaryText
                                        )
                                    }
                                    IconButton(
                                        onClick = { onCommentLikeClick(comment.id, comment.isLiked) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Like reply",
                                            tint = if (comment.isLiked) WorkCircleRedDestructive else WorkCircleSecondaryText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = comment.body,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = WorkCircleNavy
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
