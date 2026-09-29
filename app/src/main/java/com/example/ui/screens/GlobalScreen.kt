package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.data.local.UserEntity
import com.example.ui.components.PostCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import kotlinx.coroutines.flow.Flow

data class GlobalColleague(
    val id: Long,
    val name: String,
    val role: String,
    val company: String,
    val location: String,
    val timezone: String,
    val initials: String
)

@Composable
fun GlobalScreen(
    allPosts: List<PostEntity>,
    allUsers: List<UserEntity>,
    onPostClick: (Long) -> Unit,
    onLikeClick: (Long, Boolean) -> Unit,
    onSaveClick: (Long, Boolean) -> Unit,
    onCommentClick: (Long) -> Unit,
    onVoteOption: (PostEntity, Int) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onConnectWithColleague: (Long) -> Unit,
    onCreatePostClick: () -> Unit,
    onAddComment: ((Long, String, Boolean) -> Unit)? = null,
    onCommentLikeClick: ((Long, Boolean) -> Unit)? = null,
    getCommentsForPost: ((Long) -> Flow<List<CommentEntity>>)? = null,
    modifier: Modifier = Modifier
) {
    var selectedRegion by remember { mutableStateOf("All Regions") }

    val regions = listOf("All Regions", "North America", "Europe & UK", "APAC & Singapore", "Middle East")

    val internationalColleagues = listOf(
        GlobalColleague(1, "Alex Chen", "Staff PM • AI Platform", "Stripe", "San Francisco, US", "UTC-7 (PST)", "AC"),
        GlobalColleague(4, "Elena Rostova", "Head of People & Org", "GitLab", "London, UK", "UTC+1 (BST)", "ER"),
        GlobalColleague(3, "Marcus Vance", "VP of Engineering", "Block", "Seattle, US", "UTC-7 (PST)", "MV"),
        GlobalColleague(6, "Kenji Sato", "Principal ML Architect", "Grab", "Singapore", "UTC+8 (SGT)", "KS"),
        GlobalColleague(7, "Amira Al-Mansoor", "Director of Fintech", "Careem / Uber", "Dubai, UAE", "UTC+4 (GST)", "AA")
    )

    val globalPosts = remember(allPosts, selectedRegion) {
        allPosts.filter { post ->
            val isGlobal = post.pillarScope == "GLOBAL" ||
                    post.tags.contains("Remote", ignoreCase = true) ||
                    post.tags.contains("Global", ignoreCase = true) ||
                    post.communityName.contains("Remote", ignoreCase = true) ||
                    post.body.contains("international", ignoreCase = true) ||
                    post.body.contains("global", ignoreCase = true)
            isGlobal
        }.ifEmpty { allPosts.take(5) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .testTag("colleaguex_global_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Global Header
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E7FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ColleagueX Global",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "International Connections & Remote Culture",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4F46E5),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Connect with colleagues across international offices and distributed remote teams. Discuss visa mobility, async coordination, and global compensation standards.",
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Region Selector Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(regions) { region ->
                                val isSelected = selectedRegion == region
                                Surface(
                                    color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedRegion = region }
                                ) {
                                    Text(
                                        text = region,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else WorkCircleNavy,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Global Colleagues Horizontal Carousel
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "International Colleagues",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy
                        )
                        Text(
                            text = "Across 14 timezones",
                            fontSize = 11.sp,
                            color = Color(0xFF4F46E5),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(internationalColleagues) { colleague ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .width(170.dp)
                                    .border(1.dp, WorkCircleCardBorder, RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    UserAvatar(initials = colleague.initials, sizeDp = 40)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = colleague.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = WorkCircleNavy,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = colleague.role,
                                        fontSize = 10.sp,
                                        color = WorkCircleSecondaryText,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = colleague.location,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4F46E5),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = WorkCircleSecondaryText, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = colleague.timezone, fontSize = 9.sp, color = WorkCircleSecondaryText)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onConnectWithColleague(colleague.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Connect", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Global Mobility & Career Playbook highlights
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4F46E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Global Mobility & Visa Playbook",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleNavy
                            )
                            Text(
                                text = "L1A vs H1B checklist, EU Blue Card transitions & remote tax structures.",
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }
                }
            }

            // 4. Global Posts Stream
            items(globalPosts) { post ->
                PostCard(
                    post = post,
                    onPostClick = { onPostClick(post.id) },
                    onLikeClick = { onLikeClick(post.id, post.isLiked) },
                    onSaveClick = { onSaveClick(post.id, post.isSaved) },
                    onCommentClick = { onCommentClick(post.id) },
                    onVoteOption = { opt -> onVoteOption(post, opt) },
                    onReportClick = { },
                    onAuthorClick = { onAuthorClick(post.authorId) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onCreatePostClick,
            containerColor = Color(0xFF4F46E5),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("create_global_post_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post Globally")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Post Globally", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
