package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CommentEntity
import com.example.ui.components.ColleagueXPillarsBar
import com.example.ui.components.ReportDialog
import com.example.ui.components.RoleSwitcherDialog
import com.example.ui.components.WorkCircleBottomBar
import com.example.ui.components.WorkCircleTopBar
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ArticleDetailScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CommunitiesScreen
import com.example.ui.screens.CommunityDetailScreen
import com.example.ui.screens.CommunityViewMode
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.GlobalScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.IndiaScreen
import com.example.ui.screens.JobsScreen
import com.example.ui.screens.KnowledgeHubScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.LocalScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PostDetailScreen
import com.example.ui.screens.ProfessionalsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavedItemsScreen
import com.example.ui.screens.SettingsAndSafetyScreen
import com.example.ui.screens.VerificationScreen
import com.example.ui.theme.WorkCircleTheme
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.WorkCircleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkCircleTheme {
                WorkCircleApp()
            }
        }
    }
}

@Composable
fun WorkCircleApp(viewModel: WorkCircleViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allPosts by viewModel.allPosts.collectAsStateWithLifecycle()
    val savedPosts by viewModel.savedPosts.collectAsStateWithLifecycle()
    val allCommunities by viewModel.allCommunities.collectAsStateWithLifecycle()
    val allArticles by viewModel.allArticles.collectAsStateWithLifecycle()
    val savedArticles by viewModel.savedArticles.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    val allConnections by viewModel.allConnections.collectAsStateWithLifecycle()
    val verificationRequests by viewModel.verificationRequests.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val allEndorsements by viewModel.allEndorsements.collectAsStateWithLifecycle()
    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEvents.collectAsStateWithLifecycle()
    val allMentors by viewModel.allMentors.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Handle back button
    BackHandler(enabled = uiState.currentScreen !is Screen.Feed && uiState.currentScreen !is Screen.Local && uiState.currentScreen !is Screen.AuthOnboarding) {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            if (uiState.currentScreen !is Screen.AuthOnboarding) {
                Column {
                    WorkCircleTopBar(
                        activeRole = uiState.activeRole,
                        currentUser = currentUser,
                        unreadNotificationsCount = notifications.count { !it.isRead },
                        unreadMessagesCount = allConnections.count { it.receiverId == currentUser?.id && it.status == com.example.data.model.ConnectionStatus.PENDING },
                        isGuestMode = uiState.isGuestMode,
                        onRoleClick = { viewModel.showRoleSwitcher(true) },
                        onNotificationsClick = { viewModel.navigateTo(Screen.Notifications) },
                        onMessagesClick = { viewModel.navigateTo(Screen.Messages) },
                        onProfileClick = {
                            currentUser?.let { viewModel.navigateTo(Screen.UserProfile(it.id)) }
                        },
                        onAdminClick = { viewModel.navigateTo(Screen.AdminConsole) },
                        onSafetyClick = { viewModel.navigateTo(Screen.SafetyGuidelines) },
                        onAuthClick = { viewModel.navigateTo(Screen.AuthOnboarding) }
                    )
                    ColleagueXPillarsBar(
                        activePillar = uiState.activePillar,
                        onSelectPillar = { pillar -> viewModel.setActivePillar(pillar) }
                    )
                }
            }
        },
        bottomBar = {
            if (uiState.currentScreen !is Screen.AuthOnboarding) {
                WorkCircleBottomBar(
                    currentScreen = uiState.currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = uiState.currentScreen) {
                is Screen.Feed -> {
                    FeedScreen(
                        posts = allPosts,
                        communities = allCommunities,
                        activeTab = uiState.activeFeedTab,
                        onTabSelected = { viewModel.setFeedTab(it) },
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onCommentClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onVoteOption = { post, option -> viewModel.votePoll(post, option) },
                        onReportClick = { type, id, title -> viewModel.openReportDialog(type, id, title) },
                        onCreatePostClick = { viewModel.navigateTo(Screen.CreatePost) },
                        onAuthorClick = { viewModel.navigateTo(Screen.UserProfile(it)) },
                        onAddComment = { id, body, anon -> viewModel.addComment(id, body, anon) },
                        onCommentLikeClick = { id, liked -> viewModel.toggleCommentLike(id, liked) },
                        getCommentsForPost = { viewModel.getComments(it) }
                    )
                }

                is Screen.Explore -> {
                    ExploreScreen(
                        posts = allPosts,
                        communities = allCommunities,
                        articles = allArticles,
                        professionals = allUsers,
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onCommunityClick = { viewModel.navigateTo(Screen.CommunityDetail(it)) },
                        onArticleClick = { viewModel.navigateTo(Screen.ArticleDetail(it)) },
                        onProfessionalClick = { viewModel.navigateTo(Screen.UserProfile(it)) },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) }
                    )
                }

                is Screen.Communities, is Screen.CommunityFeed -> {
                    CommunitiesScreen(
                        communities = allCommunities,
                        posts = allPosts,
                        initialTab = if (screen is Screen.CommunityFeed) CommunityViewMode.COMMUNITY_FEED else CommunityViewMode.COMMUNITY_FEED,
                        onCommunityClick = { viewModel.navigateTo(Screen.CommunityDetail(it)) },
                        onJoinToggle = { id, joined -> viewModel.toggleCommunityJoin(id, joined) },
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onCommentClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onVoteOption = { post, opt -> viewModel.votePoll(post, opt) },
                        onReportClick = { type, id, title -> viewModel.openReportDialog(type, id, title) },
                        onAuthorClick = { viewModel.navigateTo(Screen.UserProfile(it)) },
                        onCreatePost = { viewModel.navigateTo(Screen.CreatePost) },
                        onAddComment = { id, body, anon -> viewModel.addComment(id, body, anon) },
                        onCommentLikeClick = { id, liked -> viewModel.toggleCommentLike(id, liked) },
                        getCommentsForPost = { viewModel.getComments(it) }
                    )
                }

                is Screen.CommunityDetail -> {
                    val community = allCommunities.find { it.id == screen.communityId }
                    val communityPosts = allPosts.filter { it.communityId == screen.communityId }
                    CommunityDetailScreen(
                        community = community,
                        posts = communityPosts,
                        onBack = { viewModel.navigateBack() },
                        onJoinToggle = { id, joined -> viewModel.toggleCommunityJoin(id, joined) },
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onCommentClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onVoteOption = { post, opt -> viewModel.votePoll(post, opt) },
                        onReportClick = { type, id, title -> viewModel.openReportDialog(type, id, title) },
                        onCreatePostInCommunity = { viewModel.navigateTo(Screen.CreatePost) },
                        onAddComment = { id, body, anon -> viewModel.addComment(id, body, anon) },
                        onCommentLikeClick = { id, liked -> viewModel.toggleCommentLike(id, liked) },
                        getCommentsForPost = { viewModel.getComments(it) }
                    )
                }

                is Screen.KnowledgeHub -> {
                    KnowledgeHubScreen(
                        articles = allArticles,
                        onArticleClick = { viewModel.navigateTo(Screen.ArticleDetail(it)) },
                        onSaveToggle = { id, saved -> viewModel.toggleArticleSave(id, saved) }
                    )
                }

                is Screen.ArticleDetail -> {
                    val article = allArticles.find { it.id == screen.articleId }
                    ArticleDetailScreen(
                        article = article,
                        onBack = { viewModel.navigateBack() },
                        onSaveToggle = { id, saved -> viewModel.toggleArticleSave(id, saved) },
                        onLikeToggle = { id, liked -> viewModel.toggleArticleLike(id, liked) }
                    )
                }

                is Screen.PostDetail -> {
                    val post = allPosts.find { it.id == screen.postId }
                    val comments = remember(post, allPosts) {
                        viewModel.allPosts // triggers recomposition
                        // fetch comments reactively
                    }
                    val postComments by viewModel.getComments(screen.postId)
                        .collectAsStateWithLifecycle(initialValue = emptyList<CommentEntity>())

                    PostDetailScreen(
                        post = post,
                        comments = postComments,
                        onBack = { viewModel.navigateBack() },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onVoteOption = { p, opt -> viewModel.votePoll(p, opt) },
                        onReportClick = { type, id, title -> viewModel.openReportDialog(type, id, title) },
                        onAddComment = { id, body, anon -> viewModel.addComment(id, body, anon) },
                        onCommentLikeClick = { id, liked -> viewModel.toggleCommentLike(id, liked) },
                        onAuthorClick = { viewModel.navigateTo(Screen.UserProfile(it)) }
                    )
                }

                is Screen.CreatePost -> {
                    CreatePostScreen(
                        communities = allCommunities,
                        currentUser = currentUser,
                        onBack = { viewModel.navigateBack() },
                        onSubmit = { commId, commName, type, title, body, tags, anon, pollOpts ->
                            viewModel.createPost(commId, commName, type, title, body, tags, anon, pollOpts)
                        }
                    )
                }

                is Screen.Professionals -> {
                    ProfessionalsScreen(
                        professionals = allUsers,
                        connections = allConnections,
                        currentUserId = currentUser?.id ?: 1,
                        onProfessionalClick = { viewModel.navigateTo(Screen.UserProfile(it)) },
                        onMessageClick = { viewModel.navigateTo(Screen.ChatConversation(it)) },
                        onConnectClick = { targetId, note, isDirectMessage ->
                            viewModel.sendConnectionRequest(targetId, note, isDirectMessage)
                        },
                        onAcceptRequest = { reqId ->
                            viewModel.acceptConnectionRequest(reqId)
                        }
                    )
                }

                is Screen.Connections, is Screen.Messages, is Screen.ChatConversation -> {
                    val activeRecipient = if (screen is Screen.ChatConversation) screen.recipientId else null
                    MessagesScreen(
                        messages = allMessages,
                        currentUser = currentUser,
                        allUsers = allUsers,
                        connections = allConnections,
                        activeRecipientId = activeRecipient,
                        isGuestMode = uiState.isGuestMode,
                        onSelectConversation = { viewModel.navigateTo(Screen.ChatConversation(it)) },
                        onSendMessage = { recId, body -> viewModel.sendMessage(recId, body) },
                        onAcceptRequest = { viewModel.acceptConnectionRequest(it) },
                        onDeclineRequest = { viewModel.declineConnectionRequest(it) },
                        onSendConnectionRequest = { targetId, note, isDirectMessage ->
                            viewModel.sendConnectionRequest(targetId, note, isDirectMessage)
                        },
                        onBackToList = { viewModel.navigateTo(Screen.Messages) },
                        onLoginClick = { viewModel.navigateTo(Screen.AuthOnboarding) },
                        onFindProfessionals = { viewModel.navigateTo(Screen.Professionals) }
                    )
                }

                is Screen.UserProfile -> {
                    val user = allUsers.find { it.id == screen.userId }
                    val isCurrent = user?.id == currentUser?.id
                    val userPosts = allPosts.filter { it.authorId == screen.userId && (!it.isAnonymous || isCurrent) }
                    val authState by viewModel.authState.collectAsStateWithLifecycle()
                    val userEndorsements = allEndorsements.filter { it.targetUserId == screen.userId }
                    ProfileScreen(
                        user = user,
                        userPosts = userPosts,
                        isCurrentUser = isCurrent,
                        authState = authState,
                        endorsements = userEndorsements,
                        currentUserId = currentUser?.id ?: 1,
                        firestoreRepository = viewModel.firestoreRepository,
                        onCreatePostClick = { viewModel.navigateTo(Screen.CreatePost) },
                        onDeleteLocalPost = { postId -> viewModel.deletePost(postId) },
                        onEndorseSkill = { skillName, note ->
                            viewModel.endorseSkill(screen.userId, skillName, note)
                        },
                        onSaveProfile = { displayName, jobTitle, bio, headline, employer, location, skills ->
                            viewModel.updateUserProfileDetails(displayName, jobTitle, bio, headline, employer, location, skills)
                        },
                        onNavigateToVerification = { viewModel.navigateTo(Screen.VerificationHub) },
                        onToggleEmployerVisibility = { viewModel.toggleEmployerVisibility(it) },
                        onToggleLocationVisibility = { viewModel.toggleLocationVisibility(it) },
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onSignOut = { viewModel.signOut() },
                        onNavigateToAuth = { viewModel.navigateTo(Screen.AuthOnboarding) }
                    )
                }

                is Screen.AuthOnboarding -> {
                    val authState by viewModel.authState.collectAsStateWithLifecycle()
                    AuthScreen(
                        authState = authState,
                        isLoading = uiState.isAuthLoading,
                        errorMessage = uiState.authErrorMessage,
                        successMessage = uiState.authSuccessMessage,
                        onSignInWithEmail = { email, pass -> viewModel.signInWithFirebase(email, pass) },
                        onSignUpWithEmail = { email, pass, name, title, comp, ind ->
                            viewModel.signUpWithFirebase(email, pass, name, title, comp, ind)
                        },
                        onSignInWithGoogle = { activity -> viewModel.signInWithGoogle(activity) },
                        onSendPasswordReset = { email -> viewModel.sendPasswordReset(email) },
                        onContinueAsGuest = { viewModel.continueAsGuest() },
                        onDemoLogin = { viewModel.demoLogin() },
                        onClearError = { viewModel.clearAuthMessages() }
                    )
                }

                is Screen.VerificationHub -> {
                    VerificationScreen(
                        user = currentUser,
                        onBack = { viewModel.navigateBack() },
                        onSubmitVerification = { comp, email, method ->
                            viewModel.submitVerificationRequest(comp, email, method)
                        }
                    )
                }

                is Screen.Notifications -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onBack = { viewModel.navigateBack() },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() },
                        onNotificationClick = {
                            viewModel.markNotificationRead(it.id)
                            if (it.type == "COMMENT" && it.relatedEntityId > 0) {
                                viewModel.navigateTo(Screen.PostDetail(it.relatedEntityId))
                            } else if (it.type == "VERIFICATION") {
                                viewModel.navigateTo(Screen.VerificationHub)
                            }
                        }
                    )
                }

                is Screen.SavedItems -> {
                    SavedItemsScreen(
                        savedPosts = savedPosts,
                        savedArticles = savedArticles,
                        onBack = { viewModel.navigateBack() },
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onArticleClick = { viewModel.navigateTo(Screen.ArticleDetail(it)) },
                        onPostLike = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onPostSave = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onArticleSave = { id, saved -> viewModel.toggleArticleSave(id, saved) }
                    )
                }

                is Screen.AdminConsole -> {
                    AdminScreen(
                        reports = reports,
                        verifications = verificationRequests,
                        auditLogs = auditLogs,
                        totalUsersCount = allUsers.size,
                        totalPostsCount = allPosts.size,
                        onBack = { viewModel.navigateBack() },
                        onModerateReport = { id, res -> viewModel.moderateReport(id, res) },
                        onReviewVerification = { reqId, uId, status, notes ->
                            viewModel.reviewVerificationRequest(reqId, uId, status, notes)
                        }
                    )
                }

                is Screen.SafetyGuidelines -> {
                    SettingsAndSafetyScreen(
                        onBack = { viewModel.navigateBack() },
                        onSubmitTicket = { _, _ -> }
                    )
                }

                is Screen.Local -> {
                    LocalScreen(
                        currentCity = uiState.selectedCity,
                        allPosts = allPosts,
                        allUsers = allUsers,
                        allJobs = allJobs,
                        allEvents = allEvents,
                        onSelectCity = { viewModel.setSelectedCity(it) },
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onCommentClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onVoteOption = { post, opt -> viewModel.votePoll(post, opt) },
                        onAuthorClick = { viewModel.navigateTo(Screen.UserProfile(it)) },
                        onConnectWithColleague = { viewModel.sendConnectionRequest(it, "Colleague in ${uiState.selectedCity}") },
                        onNavigateToJobs = { viewModel.navigateTo(Screen.Jobs) },
                        onNavigateToEvents = { viewModel.navigateTo(Screen.Events) },
                        onCreatePostClick = { viewModel.navigateTo(Screen.CreatePost) }
                    )
                }

                is Screen.India -> {
                    IndiaScreen(
                        allPosts = allPosts,
                        allUsers = allUsers,
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onCommentClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onVoteOption = { post, opt -> viewModel.votePoll(post, opt) },
                        onAuthorClick = { viewModel.navigateTo(Screen.UserProfile(it)) },
                        onCreatePostClick = { viewModel.navigateTo(Screen.CreatePost) }
                    )
                }

                is Screen.Global -> {
                    GlobalScreen(
                        allPosts = allPosts,
                        allUsers = allUsers,
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onCommentClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onVoteOption = { post, opt -> viewModel.votePoll(post, opt) },
                        onAuthorClick = { viewModel.navigateTo(Screen.UserProfile(it)) },
                        onConnectWithColleague = { viewModel.sendConnectionRequest(it, "Global colleague connection") },
                        onCreatePostClick = { viewModel.navigateTo(Screen.CreatePost) }
                    )
                }

                is Screen.Groups -> {
                    GroupsScreen(
                        communities = allCommunities,
                        posts = allPosts,
                        onCommunityClick = { viewModel.navigateTo(Screen.CommunityDetail(it)) },
                        onJoinToggle = { id, joined -> viewModel.toggleCommunityJoin(id, joined) },
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onCommentClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onVoteOption = { post, opt -> viewModel.votePoll(post, opt) },
                        onReportClick = { type, id, title -> viewModel.openReportDialog(type, id, title) },
                        onAuthorClick = { viewModel.navigateTo(Screen.UserProfile(it)) },
                        onCreatePost = { viewModel.navigateTo(Screen.CreatePost) },
                        onAddComment = { id, body, anon -> viewModel.addComment(id, body, anon) },
                        onCommentLikeClick = { id, liked -> viewModel.toggleCommentLike(id, liked) },
                        getCommentsForPost = { viewModel.getComments(it) }
                    )
                }

                is Screen.Jobs, is Screen.JobDetail -> {
                    JobsScreen(
                        jobs = allJobs,
                        onApplyToggle = { id, applied -> viewModel.toggleJobApply(id, applied) },
                        onSaveToggle = { id, saved -> viewModel.toggleJobSave(id, saved) },
                        onRequestReferral = { id, note -> viewModel.requestReferral(id, note) },
                        onPostJob = { title, comp, city, scope, exp, sal, type, dept, desc, reqs ->
                            viewModel.postJob(title, comp, city, scope, exp, sal, type, dept, desc, reqs)
                        }
                    )
                }

                is Screen.Learn -> {
                    LearnScreen(
                        mentors = allMentors,
                        articles = allArticles,
                        onBookMentor = { id, booked -> viewModel.toggleMentorBooking(id, booked) },
                        onArticleClick = { viewModel.navigateTo(Screen.ArticleDetail(it)) },
                        onArticleSaveToggle = { id, saved -> viewModel.toggleArticleSave(id, saved) },
                        onNavigateToProfile = {
                            currentUser?.let { viewModel.navigateTo(Screen.UserProfile(it.id)) }
                        }
                    )
                }

                is Screen.Events, is Screen.EventDetail -> {
                    EventsScreen(
                        events = allEvents,
                        onToggleRsvp = { id, rsvp -> viewModel.toggleEventRsvp(id, rsvp) },
                        onToggleSave = { id, saved -> viewModel.toggleEventSave(id, saved) },
                        onHostEvent = { title, type, city, scope, venue, date, time, desc, cat ->
                            viewModel.createEvent(title, type, city, scope, venue, date, time, desc, cat)
                        }
                    )
                }

                else -> {
                    FeedScreen(
                        posts = allPosts,
                        communities = allCommunities,
                        activeTab = uiState.activeFeedTab,
                        onTabSelected = { viewModel.setFeedTab(it) },
                        onPostClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onLikeClick = { id, liked -> viewModel.togglePostLike(id, liked) },
                        onSaveClick = { id, saved -> viewModel.togglePostSave(id, saved) },
                        onCommentClick = { viewModel.navigateTo(Screen.PostDetail(it)) },
                        onVoteOption = { post, option -> viewModel.votePoll(post, option) },
                        onReportClick = { type, id, title -> viewModel.openReportDialog(type, id, title) },
                        onCreatePostClick = { viewModel.navigateTo(Screen.CreatePost) },
                        onAuthorClick = { viewModel.navigateTo(Screen.UserProfile(it)) }
                    )
                }
            }
        }
    }

    // Role Switcher Dialog
    if (uiState.showRoleSwitcher) {
        RoleSwitcherDialog(
            activeRole = uiState.activeRole,
            users = allUsers,
            onSelectRole = { role ->
                viewModel.switchRole(role)
                if (role == com.example.data.model.UserRole.PLATFORM_ADMIN) {
                    viewModel.navigateTo(Screen.AdminConsole)
                }
            },
            onSelectUser = { viewModel.switchUser(it) },
            onDismiss = { viewModel.showRoleSwitcher(false) }
        )
    }

    // Content Report Dialog
    if (uiState.showReportDialog) {
        ReportDialog(
            entityTitle = uiState.reportingEntityTitle,
            onDismiss = { viewModel.closeReportDialog() },
            onSubmit = { reason, details ->
                viewModel.submitReport(reason, details)
            }
        )
    }
}
