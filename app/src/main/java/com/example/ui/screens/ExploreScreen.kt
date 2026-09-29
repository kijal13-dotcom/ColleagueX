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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.theme.colleagueXTextFieldColors
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ArticleEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.PostEntity
import com.example.data.local.UserEntity
import com.example.ui.components.PostCard
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText

@Composable
fun ExploreScreen(
    posts: List<PostEntity>,
    communities: List<CommunityEntity>,
    articles: List<ArticleEntity>,
    professionals: List<UserEntity>,
    onPostClick: (Long) -> Unit,
    onCommunityClick: (Long) -> Unit,
    onArticleClick: (Long) -> Unit,
    onProfessionalClick: (Long) -> Unit,
    onLikeClick: (Long, Boolean) -> Unit,
    onSaveClick: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val trendingTopics = listOf("AsyncWork", "RTO", "Compensation", "AIStrategy", "FirstTimeManagers", "SalaryTransparency")
    val popularCompanies = listOf("Stripe", "Bain & Company", "Block", "GitLab", "McKinsey", "Google")

    val matchingPosts = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        posts.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.body.contains(searchQuery, ignoreCase = true) ||
                it.tags.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchingCommunities = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        communities.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
    ) {
        // Search Header
        item {
            Column {
                Text(
                    text = "Explore & Discover",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = WorkCircleNavy
                )
                Text(
                    text = "Search across conversations, peer circles, and workplace knowledge.",
                    fontSize = 12.sp,
                    color = WorkCircleSecondaryText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                    placeholder = { Text("Search topics, keywords, companies, or circles...", color = Color(0xFF64748B)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkCircleBlue) },
                    shape = RoundedCornerShape(12.dp),
                    colors = colleagueXTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_input")
                )
            }
        }

        if (searchQuery.isNotBlank()) {
            // Search Results Section
            item {
                Text(
                    text = "Search Results for \"$searchQuery\"",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = WorkCircleNavy
                )
            }

            if (matchingCommunities.isNotEmpty()) {
                item {
                    Text("Matching Circles", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = WorkCircleBlue)
                }
                items(matchingCommunities) { comm ->
                    CommunityCard(
                        community = comm,
                        onClick = { onCommunityClick(comm.id) },
                        onJoinToggle = {}
                    )
                }
            }

            if (matchingPosts.isNotEmpty()) {
                item {
                    Text("Matching Discussions", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = WorkCircleBlue)
                }
                items(matchingPosts) { post ->
                    PostCard(
                        post = post,
                        onPostClick = { onPostClick(post.id) },
                        onLikeClick = { onLikeClick(post.id, post.isLiked) },
                        onSaveClick = { onSaveClick(post.id, post.isSaved) },
                        onCommentClick = { onPostClick(post.id) },
                        onVoteOption = {},
                        onReportClick = {}
                    )
                }
            }

            if (matchingCommunities.isEmpty() && matchingPosts.isEmpty()) {
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
                            Text("No direct matches found", fontWeight = FontWeight.Bold, color = WorkCircleNavy)
                            Text("Try searching for tags like #AsyncWork or #Management", fontSize = 12.sp, color = WorkCircleSecondaryText)
                        }
                    }
                }
            }
        } else {
            // Trending Topics
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trending Workplace Topics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WorkCircleNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(trendingTopics) { topic ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                modifier = Modifier.clickable { searchQuery = topic }
                            ) {
                                Text(
                                    text = "#$topic",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WorkCircleNavy,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Popular Companies Spotlight
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = WorkCircleNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active Company Circles",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WorkCircleNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(popularCompanies) { company ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                modifier = Modifier.clickable { searchQuery = company }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEBF2FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(company.take(1), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WorkCircleBlue)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = company,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = WorkCircleNavy
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Featured Knowledge Articles
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Featured Insights",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WorkCircleNavy
                        )
                    }
                }
            }

            items(articles) { article ->
                ArticleCard(
                    article = article,
                    onClick = { onArticleClick(article.id) },
                    onSaveToggle = {}
                )
            }
        }
    }
}
