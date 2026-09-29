package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleRedDestructive
import com.example.ui.theme.WorkCircleVerifiedGreen
import com.example.ui.viewmodel.ColleagueXPillar
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkCircleTopBar(
    activeRole: UserRole,
    currentUser: UserEntity?,
    unreadNotificationsCount: Int,
    unreadMessagesCount: Int = 0,
    isGuestMode: Boolean = false,
    onRoleClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMessagesClick: () -> Unit = {},
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    onSafetyClick: () -> Unit,
    onAuthClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = WorkCircleNavy
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("app_brand_header")
            ) {
                // Circular network brand symbol
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CX",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ColleagueX",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = WorkCircleNavy
                    )
                    Text(
                        text = "The Professional Employee Community",
                        fontSize = 9.sp,
                        color = WorkCircleBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        actions = {
            if (isGuestMode) {
                Surface(
                    color = WorkCircleBlue,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAuthClick() }
                        .padding(end = 4.dp)
                        .testTag("topbar_login_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Log In",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                // Role switcher chip
                Surface(
                    color = when (activeRole) {
                        UserRole.PLATFORM_ADMIN -> Color(0xFFFEE2E2)
                        UserRole.COMMUNITY_MODERATOR -> Color(0xFFEDE9FE)
                        UserRole.COMPANY_REPRESENTATIVE -> Color(0xFFCCFBF1)
                        else -> Color(0xFFEBF2FF)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onRoleClick() }
                        .testTag("role_switcher_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = when (activeRole) {
                                UserRole.PLATFORM_ADMIN -> Icons.Default.AdminPanelSettings
                                UserRole.COMMUNITY_MODERATOR -> Icons.Default.SupervisorAccount
                                UserRole.COMPANY_REPRESENTATIVE -> Icons.Default.VerifiedUser
                                else -> Icons.Default.Person
                            },
                            contentDescription = null,
                            tint = Color(activeRole.badgeColorHex),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeRole.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(activeRole.badgeColorHex)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Safety & Guidelines shortcut
            IconButton(
                onClick = onSafetyClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Safety Policy",
                    tint = WorkCircleNavy,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Notification Bell with Badge
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("notifications_icon")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge(containerColor = WorkCircleRedDestructive) {
                                Text("$unreadNotificationsCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = WorkCircleNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Direct Chat / Messages with Badge
            IconButton(
                onClick = onMessagesClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("topbar_messages_icon")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadMessagesCount > 0) {
                            Badge(containerColor = WorkCircleBlue) {
                                Text("$unreadMessagesCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Messages",
                        tint = WorkCircleNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Current User Avatar / Profile
            if (currentUser != null) {
                UserAvatar(
                    initials = currentUser.avatarInitials,
                    sizeDp = 30,
                    modifier = Modifier
                        .clickable { onProfileClick() }
                        .padding(end = 4.dp)
                        .testTag("user_avatar_topbar")
                )
            }
        }
    )
}

@Composable
fun ColleagueXPillarsBar(
    activePillar: ColleagueXPillar,
    onSelectPillar: (ColleagueXPillar) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.testTag("colleaguex_pillars_bar")
        ) {
            items(ColleagueXPillar.values()) { pillar ->
                val isSelected = activePillar == pillar
                val (color, icon) = when (pillar) {
                    ColleagueXPillar.LOCAL -> Color(0xFFD97706) to Icons.Default.LocationOn
                    ColleagueXPillar.INDIA -> Color(0xFF0F766E) to Icons.Default.Flag
                    ColleagueXPillar.GLOBAL -> Color(0xFF4F46E5) to Icons.Default.Public
                    ColleagueXPillar.GROUPS -> Color(0xFF7C3AED) to Icons.Default.Groups
                    ColleagueXPillar.JOBS -> Color(0xFF2563EB) to Icons.Default.Work
                    ColleagueXPillar.LEARN -> Color(0xFF059669) to Icons.Default.School
                    ColleagueXPillar.EVENTS -> Color(0xFFE11D48) to Icons.Default.Event
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) color else color.copy(alpha = 0.08f),
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectPillar(pillar) }
                        .testTag("pillar_tab_${pillar.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = pillar.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else WorkCircleNavy
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkCircleBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val isFeedActive = currentScreen is Screen.Feed || currentScreen is Screen.Local || currentScreen is Screen.India || currentScreen is Screen.Global || currentScreen is Screen.Explore
        NavigationBarItem(
            selected = isFeedActive,
            onClick = { onNavigate(Screen.Local) },
            icon = { Icon(Icons.Default.DynamicFeed, contentDescription = "Pillars") },
            label = { Text("Pillars", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WorkCircleBlue,
                selectedTextColor = WorkCircleBlue,
                indicatorColor = Color(0xFFEBF2FF)
            ),
            modifier = Modifier.testTag("nav_pillars")
        )
        val isGroupsActive = currentScreen is Screen.Groups || currentScreen is Screen.Communities || currentScreen is Screen.CommunityDetail
        NavigationBarItem(
            selected = isGroupsActive,
            onClick = { onNavigate(Screen.Groups) },
            icon = { Icon(Icons.Default.Groups, contentDescription = "Groups") },
            label = { Text("Groups", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF7C3AED),
                selectedTextColor = Color(0xFF7C3AED),
                indicatorColor = Color(0xFFEDE9FE)
            ),
            modifier = Modifier.testTag("nav_groups")
        )
        val isJobsActive = currentScreen is Screen.Jobs || currentScreen is Screen.JobDetail
        NavigationBarItem(
            selected = isJobsActive,
            onClick = { onNavigate(Screen.Jobs) },
            icon = { Icon(Icons.Default.Work, contentDescription = "Jobs") },
            label = { Text("Jobs", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB),
                selectedTextColor = Color(0xFF2563EB),
                indicatorColor = Color(0xFFDBEAFE)
            ),
            modifier = Modifier.testTag("nav_jobs")
        )
        val isLearnActive = currentScreen is Screen.Learn || currentScreen is Screen.KnowledgeHub || currentScreen is Screen.ArticleDetail
        NavigationBarItem(
            selected = isLearnActive,
            onClick = { onNavigate(Screen.Learn) },
            icon = { Icon(Icons.Default.School, contentDescription = "Learn") },
            label = { Text("Learn", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF059669),
                selectedTextColor = Color(0xFF059669),
                indicatorColor = Color(0xFFD1FAE5)
            ),
            modifier = Modifier.testTag("nav_learn")
        )
        val isEventsActive = currentScreen is Screen.Events || currentScreen is Screen.EventDetail
        NavigationBarItem(
            selected = isEventsActive,
            onClick = { onNavigate(Screen.Events) },
            icon = { Icon(Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFE11D48),
                selectedTextColor = Color(0xFFE11D48),
                indicatorColor = Color(0xFFFFE4E6)
            ),
            modifier = Modifier.testTag("nav_events")
        )
    }
}

@Composable
fun RoleSwitcherDialog(
    activeRole: UserRole,
    users: List<UserEntity>,
    onSelectRole: (UserRole) -> Unit,
    onSelectUser: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Role-Based Persona Switcher",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WorkCircleNavy
                )
                Text(
                    text = "Experience ColleagueX from different perspectives:",
                    fontSize = 12.sp,
                    color = WorkCircleNavy.copy(alpha = 0.7f)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                UserRole.values().forEach { role ->
                    val isSelected = activeRole == role
                    val sampleUser = users.find { it.roleType == role }

                    Surface(
                        color = if (isSelected) Color(0xFFEBF2FF) else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, WorkCircleBlue) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectRole(role)
                                if (sampleUser != null) {
                                    onSelectUser(sampleUser.id)
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = when (role) {
                                    UserRole.REGULAR_PROFESSIONAL -> Icons.Default.Person
                                    UserRole.COMMUNITY_MODERATOR -> Icons.Default.SupervisorAccount
                                    UserRole.COMPANY_REPRESENTATIVE -> Icons.Default.VerifiedUser
                                    UserRole.PLATFORM_ADMIN -> Icons.Default.AdminPanelSettings
                                    UserRole.GUEST -> Icons.Default.Explore
                                },
                                contentDescription = null,
                                tint = Color(role.badgeColorHex),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = when (role) {
                                        UserRole.REGULAR_PROFESSIONAL -> "Alex Chen (Stripe PM) • Share knowledge, ask questions"
                                        UserRole.COMMUNITY_MODERATOR -> "Priya Sharma (Bain) • Moderate posts, enforce rules"
                                        UserRole.COMPANY_REPRESENTATIVE -> "Elena Rostova (GitLab) • Official corporate community lead"
                                        UserRole.PLATFORM_ADMIN -> "Admin • System metrics, verifications & safety audits"
                                        UserRole.GUEST -> "Read public knowledge & community previews"
                                    },
                                    fontSize = 11.sp,
                                    color = WorkCircleNavy.copy(alpha = 0.65f)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = WorkCircleBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = WorkCircleBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}
