package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PostEntity
import com.example.data.model.PostType
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleRedDestructive
import com.example.ui.theme.WorkCircleSecondaryText

@Composable
fun PostCard(
    post: PostEntity,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onVoteOption: (Int) -> Unit,
    onReportClick: () -> Unit,
    onAuthorClick: () -> Unit = {},
    onTagClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WorkCircleCardBorder)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick() }
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Author / Community / Context
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatar(
                    initials = post.authorAvatar,
                    isAnonymous = post.isAnonymous,
                    sizeDp = 42,
                    modifier = Modifier.clickable { if (!post.isAnonymous) onAuthorClick() }
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = post.authorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = WorkCircleNavy,
                            modifier = Modifier.testTag("post_author_name_${post.id}")
                        )
                        if (post.isAnonymous) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Anonymous Post",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Anonymous",
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge(text = "Verified")
                        }

                        // Dot separator
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = WorkCircleSecondaryText,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Post timestamp
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("post_timestamp_${post.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Posted timestamp",
                                tint = WorkCircleSecondaryText,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = formatRelativeTime(post.createdAt),
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Text(
                        text = if (post.isAnonymous && post.anonymousBadge.isNotEmpty()) post.anonymousBadge else post.authorHeadline,
                        fontSize = 11.sp,
                        color = WorkCircleSecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Community",
                            tint = WorkCircleBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = post.communityName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WorkCircleBlue,
                            modifier = Modifier.testTag("post_community_name_${post.id}")
                        )
                    }
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp)
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
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Flag, contentDescription = null, tint = WorkCircleRedDestructive, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Report Content", color = WorkCircleRedDestructive)
                                }
                            },
                            onClick = {
                                menuExpanded = false
                                onReportClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Type Badge
            Surface(
                color = when (post.postType) {
                    PostType.QUESTION -> Color(0xFFEFF6FF)
                    PostType.WORKPLACE_STORY -> Color(0xFFFDF4FF)
                    PostType.KNOWLEDGE_INSIGHT -> Color(0xFFFEF3C7)
                    PostType.POLL -> Color(0xFFF0FDF4)
                    else -> Color(0xFFF8FAFC)
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = post.postType.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (post.postType) {
                        PostType.QUESTION -> WorkCircleBlue
                        PostType.WORKPLACE_STORY -> Color(0xFF9333EA)
                        PostType.KNOWLEDGE_INSIGHT -> Color(0xFFD97706)
                        PostType.POLL -> Color(0xFF16A34A)
                        else -> WorkCircleNavy
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = WorkCircleNavy,
                lineHeight = 22.sp,
                modifier = Modifier.testTag("post_title_${post.id}")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Text-based content / Body
            var isTextExpanded by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = post.body,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = WorkCircleNavy.copy(alpha = 0.9f),
                    maxLines = if (isTextExpanded) Int.MAX_VALUE else 4,
                    overflow = if (isTextExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("post_body_${post.id}")
                )
                if (post.body.length > 180 || post.body.contains("\n\n")) {
                    Text(
                        text = if (isTextExpanded) "Show less" else "Read full post",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkCircleBlue,
                        modifier = Modifier
                            .clickable { isTextExpanded = !isTextExpanded }
                            .padding(top = 4.dp)
                            .testTag("post_expand_toggle_${post.id}")
                    )
                }
            }

            // Poll component if applicable
            if (post.postType == PostType.POLL && post.pollOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                PollView(
                    options = post.pollOptions.split("|"),
                    votes = post.pollVotes.split("|").map { it.toIntOrNull() ?: 0 },
                    userVotedIndex = post.userVotedOptionIndex,
                    onVote = onVoteOption
                )
            }

            // Tags
            if (post.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    post.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable { onTagClick(tag) }
                                .testTag("post_tag_${post.id}_$tag")
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = WorkCircleBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Actions: Like, Comment, Save, Share
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Toggleable Like Button with Counter & User State
                    val likeButtonBgColor by animateColorAsState(
                        targetValue = if (post.isLiked) Color(0xFFFEE2E2) else Color.Transparent,
                        label = "like_bg_color"
                    )
                    val likeIconTint by animateColorAsState(
                        targetValue = if (post.isLiked) WorkCircleRedDestructive else WorkCircleSecondaryText,
                        label = "like_icon_tint"
                    )

                    Surface(
                        color = likeButtonBgColor,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 54.dp, minHeight = 44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                onClickLabel = if (post.isLiked) "Unlike post" else "Like post"
                            ) { onLikeClick() }
                            .testTag("like_button_${post.id}")
                            .testTag("community_post_like_button_${post.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (post.isLiked) "Unlike post. Liked by ${post.likesCount} members" else "Like post. Liked by ${post.likesCount} members",
                                tint = likeIconTint,
                                modifier = Modifier
                                    .size(19.dp)
                                    .testTag("like_icon_${post.id}")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${post.likesCount}",
                                fontSize = 13.sp,
                                fontWeight = if (post.isLiked) FontWeight.Bold else FontWeight.Medium,
                                color = likeIconTint,
                                modifier = Modifier.testTag("post_likes_count_${post.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Comment & Reply action
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCommentClick() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("comment_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Replies and comments for ${post.title}",
                            tint = WorkCircleSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${post.commentsCount}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = WorkCircleSecondaryText,
                            modifier = Modifier.testTag("post_comments_count_${post.id}")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reply",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WorkCircleBlue,
                            modifier = Modifier.testTag("reply_button_${post.id}")
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bookmark / Save
                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("save_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (post.isSaved) WorkCircleBlue else WorkCircleSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PollView(
    options: List<String>,
    votes: List<Int>,
    userVotedIndex: Int,
    onVote: (Int) -> Unit
) {
    val totalVotes = votes.sum().coerceAtLeast(1)

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            val optionVotes = votes.getOrElse(index) { 0 }
            val percentage = (optionVotes.toFloat() / totalVotes * 100).toInt()
            val isSelected = userVotedIndex == index
            val hasVoted = userVotedIndex >= 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) WorkCircleBlue else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(Color(0xFFF8FAFC))
                    .clickable(enabled = !hasVoted) { onVote(index) }
            ) {
                if (hasVoted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(optionVotes.toFloat() / totalVotes)
                            .matchParentSize()
                            .background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFE2E8F0))
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = WorkCircleNavy,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasVoted) {
                        Text(
                            text = "$percentage% ($optionVotes)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) WorkCircleBlue else WorkCircleSecondaryText
                        )
                    }
                }
            }
        }

        Text(
            text = if (userVotedIndex >= 0) "• ${votes.sum()} total votes • You voted" else "• ${votes.sum()} votes • Tap an option to vote",
            fontSize = 11.sp,
            color = WorkCircleSecondaryText,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
