package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommentEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.PostEntity
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import kotlinx.coroutines.flow.Flow

/**
 * GroupsScreen: ColleagueX Groups — Industries, interests & communities.
 * Wraps CommunitiesScreen while reinforcing the ColleagueX Groups identity.
 */
@Composable
fun GroupsScreen(
    communities: List<CommunityEntity>,
    posts: List<PostEntity>,
    onCommunityClick: (Long) -> Unit,
    onJoinToggle: (Long, Boolean) -> Unit,
    onPostClick: (Long) -> Unit,
    onLikeClick: (Long, Boolean) -> Unit,
    onSaveClick: (Long, Boolean) -> Unit,
    onCommentClick: (Long) -> Unit,
    onVoteOption: (PostEntity, Int) -> Unit,
    onReportClick: (String, Long, String) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onCreatePost: () -> Unit,
    onAddComment: ((Long, String, Boolean) -> Unit)? = null,
    onCommentLikeClick: ((Long, Boolean) -> Unit)? = null,
    getCommentsForPost: ((Long) -> Flow<List<CommentEntity>>)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .testTag("colleaguex_groups_screen")
    ) {
        // ColleagueX Groups Header Banner
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ColleagueX Groups",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = WorkCircleNavy
                    )
                    Text(
                        text = "Industries, Interests & Peer Communities",
                        fontSize = 11.sp,
                        color = WorkCircleBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Communities Screen implementation
        CommunitiesScreen(
            communities = communities,
            posts = posts,
            initialTab = CommunityViewMode.COMMUNITY_FEED,
            onCommunityClick = onCommunityClick,
            onJoinToggle = onJoinToggle,
            onPostClick = onPostClick,
            onLikeClick = onLikeClick,
            onSaveClick = onSaveClick,
            onCommentClick = onCommentClick,
            onVoteOption = onVoteOption,
            onReportClick = onReportClick,
            onAuthorClick = onAuthorClick,
            onCreatePost = onCreatePost,
            onAddComment = onAddComment,
            onCommentLikeClick = onCommentLikeClick,
            getCommentsForPost = getCommentsForPost,
            modifier = Modifier.weight(1f)
        )
    }
}
