package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.local.EventEntity
import com.example.data.local.JobEntity
import com.example.data.local.PostEntity
import com.example.data.local.UserEntity
import com.example.ui.components.PostCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen
import kotlinx.coroutines.flow.Flow

val SUPPORTED_CITIES = listOf(
    "Bengaluru",
    "Mumbai",
    "Delhi-NCR",
    "Hyderabad",
    "Pune",
    "Chennai",
    "San Francisco",
    "London",
    "Singapore"
)

val CITY_TECH_HUBS = mapOf(
    "Bengaluru" to "Outer Ring Road • Koramangala • Whitefield • Electronic City",
    "Mumbai" to "BKC • Lower Parel • Powai • Andheri East",
    "Delhi-NCR" to "CyberCity Gurgaon • Noida Sector 62 • Okhla • Aerocity",
    "Hyderabad" to "Hitec City • Financial District • Gachibowli • Madhapur",
    "Pune" to "Hinjewadi Phase 1-3 • Viman Nagar • Magarpatta • Kharadi",
    "Chennai" to "OMR Tech Corridor • Guindy • Taramani • Ambattur",
    "San Francisco" to "SoMa • Mission Bay • Financial District • Silicon Valley",
    "London" to "Shoreditch Tech City • Canary Wharf • King's Cross • Soho",
    "Singapore" to "Marina Bay • One-North • Tanjong Pagar • Changi Business Park"
)

@Composable
fun LocalScreen(
    currentCity: String,
    allPosts: List<PostEntity>,
    allUsers: List<UserEntity>,
    allJobs: List<JobEntity>,
    allEvents: List<EventEntity>,
    onSelectCity: (String) -> Unit,
    onPostClick: (Long) -> Unit,
    onLikeClick: (Long, Boolean) -> Unit,
    onSaveClick: (Long, Boolean) -> Unit,
    onCommentClick: (Long) -> Unit,
    onVoteOption: (PostEntity, Int) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onConnectWithColleague: (Long) -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onCreatePostClick: () -> Unit,
    onAddComment: ((Long, String, Boolean) -> Unit)? = null,
    onCommentLikeClick: ((Long, Boolean) -> Unit)? = null,
    getCommentsForPost: ((Long) -> Flow<List<CommentEntity>>)? = null,
    modifier: Modifier = Modifier
) {
    var isCityDropdownOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedChannelFilter by remember { mutableStateOf("All") }

    val cityPosts = remember(allPosts, currentCity, searchQuery, selectedChannelFilter) {
        allPosts.filter { post ->
            val matchCity = post.city.equals(currentCity, ignoreCase = true) ||
                    post.tags.contains(currentCity, ignoreCase = true) ||
                    post.pillarScope == "LOCAL" ||
                    post.body.contains(currentCity, ignoreCase = true)

            val matchChannel = when (selectedChannelFilter) {
                "Commute" -> post.tags.contains("Commute", ignoreCase = true) || post.body.contains("traffic", ignoreCase = true) || post.body.contains("metro", ignoreCase = true)
                "Tech Parks" -> post.tags.contains("Tech", ignoreCase = true) || post.body.contains("park", ignoreCase = true) || post.body.contains("office", ignoreCase = true)
                "Food & Coffee" -> post.tags.contains("Food", ignoreCase = true) || post.body.contains("coffee", ignoreCase = true) || post.body.contains("lunch", ignoreCase = true)
                else -> true
            }

            val matchQuery = if (searchQuery.isBlank()) true else {
                post.title.contains(searchQuery, ignoreCase = true) ||
                        post.body.contains(searchQuery, ignoreCase = true) ||
                        post.authorName.contains(searchQuery, ignoreCase = true)
            }

            (matchCity || allPosts.size < 5) && matchChannel && matchQuery
        }.ifEmpty {
            // Fallback so users always have rich content
            allPosts.take(6)
        }
    }

    val cityColleagues = remember(allUsers, currentCity) {
        allUsers.filter { user ->
            user.location.contains(currentCity, ignoreCase = true) || user.location.contains("India", ignoreCase = true)
        }.ifEmpty { allUsers }
    }

    val cityJobsCount = remember(allJobs, currentCity) {
        allJobs.count { it.city.contains(currentCity, ignoreCase = true) || it.city.contains("India", ignoreCase = true) }
    }

    val cityEventsCount = remember(allEvents, currentCity) {
        allEvents.count { it.city.contains(currentCity, ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .testTag("colleaguex_local_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. City Header Banner with Selector
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ColleagueX Local",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                                Text(
                                    text = "Employees in $currentCity",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WorkCircleNavy
                                )
                            }

                            // City Dropdown Selector
                            Box {
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { isCityDropdownOpen = true }
                                        .testTag("city_selector_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = currentCity,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WorkCircleNavy
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("▼", fontSize = 10.sp, color = WorkCircleSecondaryText)
                                    }
                                }

                                DropdownMenu(
                                    expanded = isCityDropdownOpen,
                                    onDismissRequest = { isCityDropdownOpen = false }
                                ) {
                                    SUPPORTED_CITIES.forEach { city ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (city == currentCity) {
                                                        Icon(
                                                            Icons.Default.LocationOn,
                                                            contentDescription = null,
                                                            tint = WorkCircleBlue,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    Text(
                                                        text = city,
                                                        fontWeight = if (city == currentCity) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            },
                                            onClick = {
                                                onSelectCity(city)
                                                isCityDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Hub Description & Major Tech Zones
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Top Employment Zones in $currentCity:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WorkCircleSecondaryText
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = CITY_TECH_HUBS[currentCity] ?: "Major commercial & tech clusters",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = WorkCircleNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Stat Highlights Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToJobs() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(Icons.Default.Work, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = "$cityJobsCount Jobs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkCircleBlue)
                                        Text(text = "Referrals", fontSize = 9.sp, color = WorkCircleSecondaryText)
                                    }
                                }
                            }

                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToEvents() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = "$cityEventsCount Events", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                        Text(text = "Meetups", fontSize = 9.sp, color = WorkCircleSecondaryText)
                                    }
                                }
                            }

                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = WorkCircleVerifiedGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = "${cityColleagues.size * 120}+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkCircleVerifiedGreen)
                                        Text(text = "Colleagues", fontSize = 9.sp, color = WorkCircleSecondaryText)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Meet Local Colleagues Section (Horizontal Carousel)
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
                            text = "Meet Colleagues in $currentCity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy
                        )
                        Text(
                            text = "${cityColleagues.size} verified",
                            fontSize = 11.sp,
                            color = WorkCircleBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(cityColleagues) { colleague ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .width(160.dp)
                                    .border(1.dp, WorkCircleCardBorder, RoundedCornerShape(14.dp))
                                    .clickable { onAuthorClick(colleague.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    UserAvatar(initials = colleague.avatarInitials, sizeDp = 44)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = colleague.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = WorkCircleNavy,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = colleague.headline,
                                        fontSize = 10.sp,
                                        color = WorkCircleSecondaryText,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "@${colleague.employer}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WorkCircleBlue,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onConnectWithColleague(colleague.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkCircleNavy),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
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

            // 3. City Channel Filters & Discussions
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "City Channels & Workplace Discussions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Channel Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Commute", "Tech Parks", "Food & Coffee").forEach { channel ->
                                val isSelected = selectedChannelFilter == channel
                                Surface(
                                    color = if (isSelected) WorkCircleBlue else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedChannelFilter = channel }
                                ) {
                                    Text(
                                        text = channel,
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

            // 4. City Posts Stream
            items(cityPosts) { post ->
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

        // Floating Action Button to post in local city
        FloatingActionButton(
            onClick = onCreatePostClick,
            containerColor = Color(0xFFD97706),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("create_local_post_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post in $currentCity")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Post in $currentCity", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
