package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.local.CommentEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.PostEntity
import com.example.ui.components.WorkplaceKnowledgeFeedComponent
import com.example.ui.viewmodel.FeedTab
import kotlinx.coroutines.flow.Flow

/**
 * FeedScreen: Displays the workplace knowledge feed with posts including author name,
 * title, and body content, community badges, interactive filters, search, engagement controls,
 * and direct in-feed commenting backed by Room database.
 */
@Composable
fun FeedScreen(
    posts: List<PostEntity>,
    communities: List<CommunityEntity>,
    activeTab: FeedTab,
    onTabSelected: (FeedTab) -> Unit,
    onPostClick: (Long) -> Unit,
    onLikeClick: (Long, Boolean) -> Unit,
    onSaveClick: (Long, Boolean) -> Unit,
    onCommentClick: (Long) -> Unit,
    onVoteOption: (PostEntity, Int) -> Unit,
    onReportClick: (String, Long, String) -> Unit,
    onCreatePostClick: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    onAddComment: ((postId: Long, commentText: String, isAnonymous: Boolean) -> Unit)? = null,
    onCommentLikeClick: ((commentId: Long, currentLiked: Boolean) -> Unit)? = null,
    getCommentsForPost: ((postId: Long) -> Flow<List<CommentEntity>>)? = null,
    modifier: Modifier = Modifier
) {
    WorkplaceKnowledgeFeedComponent(
        posts = posts,
        communities = communities,
        activeTab = activeTab,
        onTabSelected = onTabSelected,
        onPostClick = onPostClick,
        onLikeClick = onLikeClick,
        onSaveClick = onSaveClick,
        onCommentClick = onCommentClick,
        onVoteOption = onVoteOption,
        onReportClick = onReportClick,
        onCreatePostClick = onCreatePostClick,
        onAuthorClick = onAuthorClick,
        onAddComment = onAddComment,
        onCommentLikeClick = onCommentLikeClick,
        getCommentsForPost = getCommentsForPost,
        showCreateFab = true,
        showConfidentialityBanner = true,
        modifier = modifier
    )
}
