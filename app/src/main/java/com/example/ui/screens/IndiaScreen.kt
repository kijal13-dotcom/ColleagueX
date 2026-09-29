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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.data.local.UserEntity
import com.example.ui.components.PostCard
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import kotlinx.coroutines.flow.Flow

data class IndiaTrendItem(
    val title: String,
    val category: String,
    val metric: String,
    val tag: String
)

@Composable
fun IndiaScreen(
    allPosts: List<PostEntity>,
    allUsers: List<UserEntity>,
    onPostClick: (Long) -> Unit,
    onLikeClick: (Long, Boolean) -> Unit,
    onSaveClick: (Long, Boolean) -> Unit,
    onCommentClick: (Long) -> Unit,
    onVoteOption: (PostEntity, Int) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onCreatePostClick: () -> Unit,
    onAddComment: ((Long, String, Boolean) -> Unit)? = null,
    onCommentLikeClick: ((Long, Boolean) -> Unit)? = null,
    getCommentsForPost: ((Long) -> Flow<List<CommentEntity>>)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }

    val trendingTopics = listOf(
        IndiaTrendItem("Appraisal Season Benchmarks", "Compensation", "Avg 7.8% CTC Hike", "Appraisals"),
        IndiaTrendItem("90-Day vs 30-Day Notice Period", "Policy", "1.2k Colleagues Discussing", "NoticePeriod"),
        IndiaTrendItem("GCC Hiring Expansion in BLR/HYD", "Tech Hubs", "250+ Open Roles", "GCCs"),
        IndiaTrendItem("New vs Old Tax Regime for CTC > 25L", "Personal Finance", "HRA & NPS Insights", "Taxation")
    )

    val topEmployers = listOf(
        "Razorpay", "Swiggy", "Google India", "Infosys", "Microsoft India", "Zomato", "TCS", "Flipkart", "Bain India"
    )

    val indiaPosts = remember(allPosts, selectedCategory) {
        allPosts.filter { post ->
            val matchScope = post.pillarScope == "INDIA" || post.pillarScope.isBlank() || post.tags.contains("India", ignoreCase = true)
            val matchCategory = when (selectedCategory) {
                "Compensation" -> post.tags.contains("Compensation", ignoreCase = true) || post.title.contains("salary", ignoreCase = true) || post.body.contains("CTC", ignoreCase = true)
                "Leadership" -> post.tags.contains("Leadership", ignoreCase = true) || post.tags.contains("Management", ignoreCase = true)
                "Tech Insights" -> post.tags.contains("Tech", ignoreCase = true) || post.tags.contains("Productivity", ignoreCase = true)
                else -> true
            }
            matchCategory
        }.ifEmpty { allPosts }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .testTag("colleaguex_india_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. India Header Banner
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
                            Text(text = "🇮🇳", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ColleagueX India",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Nationwide Employee Community",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD97706),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Connect with professionals across Indian tech hubs, GCCs, startups, and enterprises. Benchmark salaries, discuss policy, and learn workplace insights.",
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Trending India Topics Horizontal Carousel
                        Text(
                            text = "Nationwide Workplace Trends",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy
                        )
                    }
                }
            }

            // 2. Trend cards carousel
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = 14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trendingTopics) { trend ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(200.dp)
                                .border(1.dp, WorkCircleCardBorder, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = trend.category,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = trend.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = WorkCircleNavy,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = trend.metric,
                                    fontSize = 11.sp,
                                    color = WorkCircleBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Indian Tech Hubs & Top Employers Strip
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Trending Employer Circles in India",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(topEmployers) { employer ->
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = WorkCircleNavy, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = employer, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = WorkCircleNavy)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Topic Filter Pills
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Compensation", "Leadership", "Tech Insights").forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                color = if (isSelected) WorkCircleBlue else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
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

            // 5. Nationwide Feed Posts
            items(indiaPosts) { post ->
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
            containerColor = Color(0xFF0F766E),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("create_india_post_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post to India")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Post to India", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
