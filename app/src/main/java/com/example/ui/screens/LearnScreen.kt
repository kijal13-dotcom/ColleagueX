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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.local.ArticleEntity
import com.example.data.local.MentorEntity
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen

@Composable
fun LearnScreen(
    mentors: List<MentorEntity>,
    articles: List<ArticleEntity>,
    onBookMentor: (Long, Boolean) -> Unit,
    onArticleClick: (Long) -> Unit,
    onArticleSaveToggle: (Long, Boolean) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLearnTab by remember { mutableStateOf(0) } // 0: 1:1 Mentorship, 1: Knowledge Playbooks

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .testTag("colleaguex_learn_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Header Banner
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
                                    .background(Color(0xFFEDE9FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ColleagueX Learn",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Mentoring & Knowledge Sharing",
                                    fontSize = 11.sp,
                                    color = Color(0xFF7C3AED),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Level up through 1:1 mentorship from verified senior colleagues, battle-tested engineering/management playbooks, and peer skill endorsements.",
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tab Switcher (Mentorship vs Knowledge Playbooks)
                        TabRow(
                            selectedTabIndex = selectedLearnTab,
                            containerColor = Color(0xFFF1F5F9),
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedLearnTab]),
                                    color = Color(0xFF7C3AED),
                                    height = 3.dp
                                )
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            Tab(
                                selected = selectedLearnTab == 0,
                                onClick = { selectedLearnTab = 0 },
                                text = {
                                    Text(
                                        "1:1 Mentorship (${mentors.size})",
                                        fontWeight = if (selectedLearnTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (selectedLearnTab == 0) Color(0xFF7C3AED) else WorkCircleSecondaryText
                                    )
                                }
                            )
                            Tab(
                                selected = selectedLearnTab == 1,
                                onClick = { selectedLearnTab = 1 },
                                text = {
                                    Text(
                                        "Playbooks & Guides (${articles.size})",
                                        fontWeight = if (selectedLearnTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (selectedLearnTab == 1) Color(0xFF7C3AED) else WorkCircleSecondaryText
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 2. Content Sections based on Tab
            if (selectedLearnTab == 0) {
                // Mentorship Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = "Verified Colleague Mentors",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleNavy
                            )
                            Text(
                                text = "Free 1:1 guidance from staff architects, VP of products, and leadership coaches.",
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }
                }

                items(mentors) { mentor ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("mentor_card_${mentor.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(initials = mentor.avatarInitials, sizeDp = 44)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = mentor.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkCircleNavy
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = WorkCircleBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = "${mentor.headline} • ${mentor.company}",
                                            fontSize = 11.sp,
                                            color = WorkCircleSecondaryText,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "📍 ${mentor.city}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = WorkCircleBlue
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("${mentor.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = mentor.bio,
                                fontSize = 12.sp,
                                color = WorkCircleSecondaryText,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Expertise Pill
                            Surface(
                                color = Color(0xFFEDE9FE),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Specialties: ${mentor.expertise}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7C3AED),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventAvailable, contentDescription = null, tint = WorkCircleSecondaryText, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Available: ${mentor.availableSlots}",
                                    fontSize = 11.sp,
                                    color = WorkCircleSecondaryText
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "• ${mentor.sessionsCompleted} sessions completed",
                                    fontSize = 11.sp,
                                    color = WorkCircleSecondaryText
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Book Session Button
                            Button(
                                onClick = { onBookMentor(mentor.id, mentor.isBooked) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (mentor.isBooked) WorkCircleVerifiedGreen else Color(0xFF7C3AED)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (mentor.isBooked) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("1:1 Session Confirmed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Book 1:1 Mentoring Session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // Knowledge Playbooks Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = "Curated Career & Engineering Playbooks",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleNavy
                            )
                            Text(
                                text = "Actionable frameworks written by practicing colleagues at top organizations.",
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }
                }

                items(articles) { article ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { onArticleClick(article.id) }
                            .testTag("article_card_${article.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = article.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkCircleBlue,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onArticleSaveToggle(article.id, article.isSaved) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (article.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Save Article",
                                        tint = if (article.isSaved) WorkCircleBlue else WorkCircleSecondaryText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = article.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleNavy
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = article.subtitle,
                                fontSize = 12.sp,
                                color = WorkCircleSecondaryText,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "By ${article.authorName} (${article.authorCompany})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "${article.readTimeMinutes} min read",
                                    fontSize = 11.sp,
                                    color = WorkCircleSecondaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
