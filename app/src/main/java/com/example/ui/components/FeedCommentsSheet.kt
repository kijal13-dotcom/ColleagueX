package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.ui.components.formatRelativeTime
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleRedDestructive
import com.example.ui.theme.WorkCircleSecondaryText

/**
 * Material 3 Bottom Sheet allowing users to view, discuss, and add comments
 * for workplace knowledge posts directly on the Feed screen using Room database storage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedCommentsSheet(
    post: PostEntity,
    comments: List<CommentEntity>,
    onDismiss: () -> Unit,
    onAddComment: (postId: Long, commentText: String, isAnonymous: Boolean) -> Unit,
    onCommentLikeClick: (commentId: Long, currentLiked: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier.testTag("feed_comments_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
        ) {
            // Header: Post Summary & Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = WorkCircleBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Workplace Discussion (${comments.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = post.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = WorkCircleSecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp).testTag("close_comments_sheet_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = WorkCircleNavy)
                }
            }

            HorizontalDivider(color = WorkCircleCardBorder, thickness = 1.dp)

            // Comments List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("feed_comments_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (comments.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp)
                                .testTag("feed_no_comments_placeholder")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                tint = WorkCircleSecondaryText,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No comments yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = WorkCircleNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Be the first professional to share an insight or experience!",
                                fontSize = 12.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }
                } else {
                    items(comments, key = { it.id }) { comment ->
                        CommentItem(
                            comment = comment,
                            onLikeClick = { onCommentLikeClick(comment.id, comment.isLiked) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            HorizontalDivider(color = WorkCircleCardBorder, thickness = 1.dp)

            // In-Feed Comment Compose Bar
            Surface(
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    // Anonymous toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isAnonymous) WorkCircleBlue else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAnonymous) "Commenting Anonymously" else "Comment with Verified Profile",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isAnonymous) WorkCircleBlue else WorkCircleNavy
                            )
                        }

                        Switch(
                            checked = isAnonymous,
                            onCheckedChange = { isAnonymous = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WorkCircleBlue),
                            modifier = Modifier.size(width = 38.dp, height = 24.dp).testTag("anonymous_comment_toggle")
                        )
                    }

                    // Input & Send button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                            placeholder = { Text("Add your workplace insight or feedback...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black,
                                focusedBorderColor = WorkCircleBlue,
                                unfocusedBorderColor = WorkCircleCardBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("feed_comment_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onAddComment(post.id, commentText.trim(), isAnonymous)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    if (commentText.isNotBlank()) WorkCircleBlue else Color(0xFFCBD5E1),
                                    CircleShape
                                )
                                .testTag("submit_feed_comment_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Submit Comment to Room Database",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: CommentEntity,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("comment_item_${comment.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Author row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (comment.isAnonymous) Color(0xFF475569) else WorkCircleBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (comment.isAnonymous) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = comment.authorAvatar.ifEmpty { "P" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = comment.authorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = WorkCircleNavy,
                            modifier = Modifier.testTag("comment_author_${comment.id}")
                        )
                        if (!comment.isAnonymous) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = WorkCircleBlue,
                                modifier = Modifier.size(12.dp)
                            )
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

                    if (comment.authorHeadline.isNotBlank()) {
                        Text(
                            text = comment.authorHeadline,
                            fontSize = 11.sp,
                            color = WorkCircleSecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Like comment button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLikeClick() }
                        .padding(4.dp)
                        .testTag("like_comment_button_${comment.id}")
                ) {
                    Icon(
                        imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like comment",
                        tint = if (comment.isLiked) WorkCircleRedDestructive else WorkCircleSecondaryText,
                        modifier = Modifier.size(15.dp)
                    )
                    if (comment.likesCount > 0) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${comment.likesCount}",
                            fontSize = 11.sp,
                            color = if (comment.isLiked) WorkCircleRedDestructive else WorkCircleSecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment text
            Text(
                text = comment.body,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = WorkCircleNavy,
                modifier = Modifier.testTag("comment_body_${comment.id}")
            )
        }
    }
}
