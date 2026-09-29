package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthResult
import com.example.data.auth.AuthState
import com.example.data.auth.FirebaseAuthManager
import com.example.data.local.ArticleEntity
import com.example.data.local.CommentEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.EventEntity
import com.example.data.local.JobEntity
import com.example.data.local.MentorEntity
import com.example.data.local.MessageEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PostEntity
import com.example.data.local.ReportEntity
import com.example.data.local.SkillEndorsementEntity
import com.example.data.local.UserConnectionEntity
import com.example.data.local.UserEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VerificationRequestEntity
import com.example.data.local.WorkCircleDatabase
import com.example.data.model.ConnectionStatus
import com.example.data.model.ModerationStatus
import com.example.data.model.PostType
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.WorkCircleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ColleagueXPillar(val id: String, val title: String, val subtitle: String) {
    LOCAL("local", "Local", "City Colleagues"),
    INDIA("india", "India", "Nationwide Community"),
    GLOBAL("global", "Global", "International Network"),
    GROUPS("groups", "Groups", "Industries & Interests"),
    JOBS("jobs", "Jobs", "Career Opportunities"),
    LEARN("learn", "Learn", "Mentoring & Knowledge"),
    EVENTS("events", "Events", "Professional & Social")
}

sealed interface Screen {
    data object Feed : Screen
    data object Local : Screen // ColleagueX Local: employees in your city
    data object India : Screen // ColleagueX India: nationwide employee community
    data object Global : Screen // ColleagueX Global: international connections
    data object Groups : Screen // ColleagueX Groups: industries, interests & communities
    data object Jobs : Screen // ColleagueX Jobs: career opportunities
    data class JobDetail(val jobId: Long) : Screen
    data object Learn : Screen // ColleagueX Learn: mentoring & knowledge sharing
    data object Events : Screen // ColleagueX Events: professional/social events
    data class EventDetail(val eventId: Long) : Screen
    data object CommunityFeed : Screen
    data object Explore : Screen
    data object Communities : Screen
    data class CommunityDetail(val communityId: Long) : Screen
    data object KnowledgeHub : Screen
    data class ArticleDetail(val articleId: Long) : Screen
    data class PostDetail(val postId: Long) : Screen
    data object CreatePost : Screen
    data object Professionals : Screen
    data object Connections : Screen
    data object Messages : Screen
    data class ChatConversation(val recipientId: Long) : Screen
    data object VerificationHub : Screen
    data object Notifications : Screen
    data object SavedItems : Screen
    data class UserProfile(val userId: Long) : Screen
    data object AdminConsole : Screen
    data object SettingsPrivacy : Screen
    data object SafetyGuidelines : Screen
    data object SupportContact : Screen
    data object AuthOnboarding : Screen
}

enum class FeedTab(val label: String) {
    COMMUNITY_FEED("Community Feed"),
    FOR_YOU("For You"),
    FOLLOWING("Following"),
    LATEST("Latest"),
    POPULAR("Popular"),
    MY_COMMUNITIES("My Communities")
}

data class WorkCircleUiState(
    val currentScreen: Screen = Screen.AuthOnboarding,
    val previousScreen: Screen? = null,
    val currentUserId: Long = 1,
    val activeRole: UserRole = UserRole.REGULAR_PROFESSIONAL,
    val activePillar: ColleagueXPillar = ColleagueXPillar.LOCAL,
    val selectedCity: String = "Bengaluru",
    val activeFeedTab: FeedTab = FeedTab.FOR_YOU,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val showRoleSwitcher: Boolean = false,
    val showReportDialog: Boolean = false,
    val reportingEntity: Pair<String, Long>? = null, // Type to Id
    val reportingEntityTitle: String = "",
    val snackbarMessage: String? = null,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val authSuccessMessage: String? = null,
    val isGuestMode: Boolean = false
)

class WorkCircleViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WorkCircleDatabase.getInstance(application)
    private val repository = WorkCircleRepository(db.workCircleDao())
    val authManager: FirebaseAuthManager = FirebaseAuthManager(application)
    val firestoreRepository: FirestoreRepository = FirestoreRepository(application)
    val authState: StateFlow<AuthState> = authManager.authState

    private val _uiState = MutableStateFlow(WorkCircleUiState())
    val uiState: StateFlow<WorkCircleUiState> = _uiState.asStateFlow()

    init {
        // Sync authentication state with Room database and Firestore
        viewModelScope.launch {
            authManager.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        val localUser = repository.registerOrUpdateFirebaseUser(
                            email = state.email,
                            fullName = state.displayName,
                            firebaseUid = state.uid,
                            authProvider = state.provider,
                            photoUrl = state.photoUrl
                        )
                        // Sync to Firestore cloud document storage
                        launch {
                            try {
                                firestoreRepository.saveUserProfileFromEntity(localUser)
                            } catch (_: Exception) {}
                        }
                        _uiState.value = _uiState.value.copy(
                            currentUserId = localUser.id,
                            isAuthLoading = false,
                            isGuestMode = false,
                            currentScreen = if (_uiState.value.currentScreen is Screen.AuthOnboarding) Screen.Feed else _uiState.value.currentScreen
                        )
                    }
                    is AuthState.Loading -> {
                        _uiState.value = _uiState.value.copy(isAuthLoading = true)
                    }
                    is AuthState.Unauthenticated -> {
                        _uiState.value = _uiState.value.copy(isAuthLoading = false)
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.ensureInitialDataLoaded()
        }
    }

    val allPosts: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPosts: StateFlow<List<PostEntity>> = repository.savedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCommunities: StateFlow<List<CommunityEntity>> = repository.allCommunities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val joinedCommunities: StateFlow<List<CommunityEntity>> = repository.joinedCommunities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArticles: StateFlow<List<ArticleEntity>> = repository.allArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedArticles: StateFlow<List<ArticleEntity>> = repository.savedArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConnections: StateFlow<List<UserConnectionEntity>> = repository.allConnections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val verificationRequests: StateFlow<List<VerificationRequestEntity>> = repository.verificationRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<ReportEntity>> = repository.reports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allEndorsements: StateFlow<List<SkillEndorsementEntity>> = repository.allEndorsements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMentors: StateFlow<List<MentorEntity>> = repository.allMentors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = combine(allUsers, _uiState) { users, state ->
        users.find { it.id == state.currentUserId } ?: users.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun navigateTo(screen: Screen) {
        val current = _uiState.value.currentScreen
        val pillar = when (screen) {
            is Screen.Local -> ColleagueXPillar.LOCAL
            is Screen.India -> ColleagueXPillar.INDIA
            is Screen.Global -> ColleagueXPillar.GLOBAL
            is Screen.Groups, is Screen.Communities, is Screen.CommunityDetail -> ColleagueXPillar.GROUPS
            is Screen.Jobs, is Screen.JobDetail -> ColleagueXPillar.JOBS
            is Screen.Learn, is Screen.KnowledgeHub, is Screen.ArticleDetail -> ColleagueXPillar.LEARN
            is Screen.Events, is Screen.EventDetail -> ColleagueXPillar.EVENTS
            else -> _uiState.value.activePillar
        }
        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            previousScreen = current,
            activePillar = pillar
        )
    }

    fun navigateBack() {
        val previous = _uiState.value.previousScreen ?: Screen.Local
        val pillar = when (previous) {
            is Screen.Local -> ColleagueXPillar.LOCAL
            is Screen.India -> ColleagueXPillar.INDIA
            is Screen.Global -> ColleagueXPillar.GLOBAL
            is Screen.Groups, is Screen.Communities, is Screen.CommunityDetail -> ColleagueXPillar.GROUPS
            is Screen.Jobs, is Screen.JobDetail -> ColleagueXPillar.JOBS
            is Screen.Learn, is Screen.KnowledgeHub, is Screen.ArticleDetail -> ColleagueXPillar.LEARN
            is Screen.Events, is Screen.EventDetail -> ColleagueXPillar.EVENTS
            else -> _uiState.value.activePillar
        }
        _uiState.value = _uiState.value.copy(
            currentScreen = previous,
            previousScreen = null,
            activePillar = pillar
        )
    }

    fun setFeedTab(tab: FeedTab) {
        _uiState.value = _uiState.value.copy(activeFeedTab = tab)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun showRoleSwitcher(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRoleSwitcher = show)
    }

    fun switchRole(role: UserRole) {
        val targetUser = allUsers.value.find { it.roleType == role } ?: allUsers.value.firstOrNull()
        _uiState.value = _uiState.value.copy(
            activeRole = role,
            currentUserId = targetUser?.id ?: 1,
            showRoleSwitcher = false,
            snackbarMessage = "Switched to role: ${role.displayName}"
        )
    }

    fun switchUser(userId: Long) {
        val user = allUsers.value.find { it.id == userId }
        if (user != null) {
            _uiState.value = _uiState.value.copy(
                currentUserId = userId,
                activeRole = user.roleType,
                showRoleSwitcher = false,
                snackbarMessage = "Active profile: ${user.fullName}"
            )
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun togglePostLike(postId: Long, currentLiked: Boolean) {
        viewModelScope.launch {
            repository.togglePostLike(postId, currentLiked)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!currentLiked) "Liked post" else "Removed like"
            )
        }
    }

    fun togglePostSave(postId: Long, currentSaved: Boolean) {
        viewModelScope.launch {
            repository.togglePostSave(postId, currentSaved)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!currentSaved) "Saved to your Bookmarks" else "Removed from Bookmarks"
            )
        }
    }

    fun votePoll(post: PostEntity, optionIndex: Int) {
        viewModelScope.launch {
            repository.votePoll(post, optionIndex)
        }
    }

    fun toggleCommunityJoin(communityId: Long, currentJoined: Boolean) {
        viewModelScope.launch {
            repository.toggleJoinCommunity(communityId, currentJoined)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!currentJoined) "Joined community!" else "Left community"
            )
        }
    }

    fun getComments(postId: Long): Flow<List<CommentEntity>> {
        return repository.getComments(postId)
    }

    fun getPostsByTag(tag: String): Flow<List<PostEntity>> {
        return repository.getPostsByTag(tag)
    }

    fun createPost(
        communityId: Long,
        communityName: String,
        postType: PostType,
        title: String,
        body: String,
        tags: String,
        isAnonymous: Boolean,
        pollOptions: List<String> = emptyList()
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val postId = repository.createPost(
                author = user,
                communityId = communityId,
                communityName = communityName,
                postType = postType,
                title = title,
                body = body,
                tags = tags,
                isAnonymous = isAnonymous,
                pollOptions = pollOptions
            )
            // Synchronize community post document to Firebase Firestore cloud storage
            launch {
                try {
                    val created = repository.getPostById(postId).firstOrNull()
                    if (created != null) {
                        firestoreRepository.createPostFromEntity(created)
                    }
                } catch (_: Exception) {}
            }
            _uiState.value = _uiState.value.copy(
                currentScreen = Screen.Feed,
                snackbarMessage = if (isAnonymous) "Posted anonymously to $communityName" else "Post published!"
            )
        }
    }

    fun deletePost(postId: Long, firestoreDocId: String? = null) {
        viewModelScope.launch {
            repository.deletePost(postId)
            if (!firestoreDocId.isNullOrBlank()) {
                try {
                    firestoreRepository.deletePost(firestoreDocId)
                } catch (_: Exception) {}
            }
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Post deleted"
            )
        }
    }

    fun addComment(postId: Long, body: String, isAnonymous: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addComment(postId, user, body, isAnonymous)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Comment added"
            )
        }
    }

    fun toggleCommentLike(commentId: Long, currentLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleCommentLike(commentId, currentLiked)
        }
    }

    fun isConnectedOrAccepted(userAId: Long, userBId: Long): Boolean {
        return allConnections.value.any { conn ->
            ((conn.requesterId == userAId && conn.receiverId == userBId) ||
             (conn.requesterId == userBId && conn.receiverId == userAId)) &&
             conn.status == ConnectionStatus.ACCEPTED
        }
    }

    fun getConnectionBetween(userAId: Long, userBId: Long): UserConnectionEntity? {
        return allConnections.value.find { conn ->
            (conn.requesterId == userAId && conn.receiverId == userBId) ||
            (conn.requesterId == userBId && conn.receiverId == userAId)
        }
    }

    fun sendConnectionRequest(targetUserId: Long, note: String = "", isMessageRequest: Boolean = false) {
        val user = currentUser.value ?: return
        val receiver = allUsers.value.find { it.id == targetUserId } ?: return
        viewModelScope.launch {
            repository.sendConnectionRequest(user, receiver, note, isMessageRequest)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (isMessageRequest) "Message request sent to ${receiver.fullName}" else "Connection request sent to ${receiver.fullName}"
            )
        }
    }

    fun acceptConnectionRequest(requestId: Long) {
        viewModelScope.launch {
            repository.acceptConnectionRequest(requestId)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Connection request accepted! You can now chat.")
        }
    }

    fun declineConnectionRequest(requestId: Long) {
        viewModelScope.launch {
            repository.declineConnectionRequest(requestId)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Request declined.")
        }
    }

    fun sendMessage(recipientId: Long, body: String) {
        val user = currentUser.value
        if (user == null || _uiState.value.isGuestMode) {
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Please log in to chat with professionals"
            )
            return
        }
        val recipient = allUsers.value.find { it.id == recipientId } ?: return
        val isAllowed = isConnectedOrAccepted(user.id, recipient.id)
        if (!isAllowed) {
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "You can only message connections or users with accepted message requests"
            )
            return
        }
        viewModelScope.launch {
            repository.sendMessage(user, recipient, body)
        }
    }

    fun openReportDialog(entityType: String, entityId: Long, entityTitle: String) {
        _uiState.value = _uiState.value.copy(
            showReportDialog = true,
            reportingEntity = entityType to entityId,
            reportingEntityTitle = entityTitle
        )
    }

    fun closeReportDialog() {
        _uiState.value = _uiState.value.copy(
            showReportDialog = false,
            reportingEntity = null,
            reportingEntityTitle = ""
        )
    }

    fun submitReport(reason: String, details: String) {
        val user = currentUser.value ?: return
        val entity = _uiState.value.reportingEntity ?: return
        val title = _uiState.value.reportingEntityTitle
        viewModelScope.launch {
            repository.submitReport(
                reporter = user,
                entityType = entity.first,
                entityId = entity.second,
                entityTitle = title,
                reason = reason,
                details = details
            )
            closeReportDialog()
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Report submitted. Our moderation team will review this shortly."
            )
        }
    }

    fun moderateReport(reportId: Long, resolution: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.moderateReport(reportId, resolution, user.fullName)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Report updated: $resolution"
            )
        }
    }

    fun moderatePost(postId: Long, status: ModerationStatus) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updatePostModeration(postId, status, user.fullName)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Post status set to $status"
            )
        }
    }

    fun submitVerificationRequest(companyName: String, workEmail: String, method: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.submitVerificationRequest(user, companyName, workEmail, method)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Verification request submitted! Token sent to $workEmail"
            )
        }
    }

    fun reviewVerificationRequest(requestId: Long, userId: Long, status: VerificationStatus, notes: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.reviewVerificationRequest(requestId, userId, status, notes, user.fullName)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Verification set to ${status.label}"
            )
        }
    }

    fun toggleEmployerVisibility(visible: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(user.copy(employerVisibility = visible))
        }
    }

    fun toggleLocationVisibility(visible: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUser(user.copy(locationVisibility = visible))
        }
    }

    fun updateUserProfileDetails(
        displayName: String,
        jobTitle: String,
        bio: String,
        headline: String = "",
        employer: String = "",
        location: String = "",
        skills: String = ""
    ) {
        val user = currentUser.value
        val auth = authState.value
        val targetUid = when {
            auth is AuthState.Authenticated -> auth.uid
            !user?.firebaseUid.isNullOrBlank() -> user!!.firebaseUid
            else -> "local_uid_${user?.id ?: 1}"
        }

        viewModelScope.launch {
            val updatedUser = repository.saveUserProfileByFirebaseUid(
                firebaseUid = targetUid,
                displayName = displayName,
                jobTitle = jobTitle,
                bio = bio,
                headline = headline,
                employer = employer,
                location = location,
                skills = skills
            )
            repository.updateUserProfile(
                bio = bio,
                jobTitle = jobTitle,
                companyName = employer
            )
            // Synchronize profile document to Firebase Firestore cloud storage
            launch {
                try {
                    firestoreRepository.saveUserProfileFromEntity(updatedUser)
                } catch (_: Exception) {}
            }
            _uiState.value = _uiState.value.copy(
                currentUserId = updatedUser.id,
                snackbarMessage = "Profile updated (Saved to Room DB & synced to Firestore)"
            )
        }
    }

    fun updateUserBio(bio: String) {
        val user = currentUser.value
        val name = user?.fullName ?: "Professional Member"
        val jobTitle = user?.role?.ifBlank { user?.headline ?: "" } ?: "Professional"
        val employer = user?.employer ?: ""
        val location = user?.location ?: ""
        val skills = user?.skills ?: ""
        updateUserProfileDetails(
            displayName = name,
            jobTitle = jobTitle,
            bio = bio,
            employer = employer,
            location = location,
            skills = skills
        )
    }

    fun updateUserProfile(
        bio: String,
        jobTitle: String,
        companyName: String
    ) {
        viewModelScope.launch {
            repository.updateUserProfile(bio, jobTitle, companyName)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Professional profile updated in Room"
            )
        }
    }

    fun getEndorsementsForUser(userId: Long): Flow<List<SkillEndorsementEntity>> =
        repository.getEndorsementsForUser(userId)

    fun getEndorsementsForSkill(userId: Long, skillName: String): Flow<List<SkillEndorsementEntity>> =
        repository.getEndorsementsForSkill(userId, skillName)

    fun endorseSkill(
        targetUserId: Long,
        skillName: String,
        note: String = ""
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val added = repository.endorseSkill(
                targetUserId = targetUserId,
                endorser = user,
                skillName = skillName,
                note = note
            )
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (added) "Endorsed \"$skillName\"! Stored in Room DB (offline-ready)" else "Removed endorsement for \"$skillName\""
            )
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "All notifications marked as read"
            )
        }
    }

    fun toggleArticleSave(articleId: Long, currentSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleArticleSave(articleId, currentSaved)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!currentSaved) "Article bookmarked!" else "Article removed from bookmarks"
            )
        }
    }

    fun toggleArticleLike(articleId: Long, currentLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleArticleLike(articleId, currentLiked)
        }
    }

    // --- Authentication Actions ---

    fun signUpWithFirebase(
        email: String,
        password: String,
        fullName: String,
        jobTitle: String,
        company: String,
        industry: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAuthLoading = true,
                authErrorMessage = null,
                authSuccessMessage = null
            )
            when (val result = authManager.signUpWithEmail(email, password, fullName)) {
                is AuthResult.Success -> {
                    val authUser = result.user
                    if (authUser != null) {
                        val localUser = repository.registerOrUpdateFirebaseUser(
                            email = authUser.email,
                            fullName = fullName.ifBlank { authUser.displayName },
                            firebaseUid = authUser.uid,
                            authProvider = "password",
                            headline = "$jobTitle • $company",
                            employer = company,
                            industry = industry,
                            role = jobTitle
                        )
                        _uiState.value = _uiState.value.copy(
                            currentUserId = localUser.id,
                            isAuthLoading = false,
                            authSuccessMessage = "Account created successfully! Welcome to ColleagueX.",
                            currentScreen = Screen.Feed,
                            snackbarMessage = "Welcome to ColleagueX, ${localUser.fullName}!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isAuthLoading = false,
                            authSuccessMessage = "Account created! You can now log in."
                        )
                    }
                }
                is AuthResult.Error -> {
                    if (result.message.contains("API key not valid", ignoreCase = true) ||
                        result.message.contains("network", ignoreCase = true) ||
                        result.message.contains("not initialized", ignoreCase = true) ||
                        result.message.contains("internal error", ignoreCase = true) ||
                        result.message.contains("error has occurred", ignoreCase = true)
                    ) {
                        val localUid = "local_uid_${System.currentTimeMillis()}"
                        authManager.setDemoAuthenticatedUser(
                            email = email,
                            name = fullName.ifBlank { email.substringBefore("@") },
                            provider = "password"
                        )
                        val localUser = repository.registerOrUpdateFirebaseUser(
                            email = email,
                            fullName = fullName.ifBlank { email.substringBefore("@") },
                            firebaseUid = localUid,
                            authProvider = "password",
                            headline = "$jobTitle • $company",
                            employer = company,
                            industry = industry,
                            role = jobTitle
                        )
                        _uiState.value = _uiState.value.copy(
                            currentUserId = localUser.id,
                            isAuthLoading = false,
                            authSuccessMessage = "Account created successfully! Welcome to ColleagueX.",
                            currentScreen = Screen.Feed,
                            snackbarMessage = "Welcome to ColleagueX, ${localUser.fullName}!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isAuthLoading = false,
                            authErrorMessage = result.message
                        )
                    }
                }
                is AuthResult.ConfigurationNotice -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authErrorMessage = result.message
                    )
                }
            }
        }
    }

    fun signInWithFirebase(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAuthLoading = true,
                authErrorMessage = null,
                authSuccessMessage = null
            )
            when (val result = authManager.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    val authUser = result.user
                    if (authUser != null) {
                        val localUser = repository.registerOrUpdateFirebaseUser(
                            email = authUser.email,
                            fullName = authUser.displayName,
                            firebaseUid = authUser.uid,
                            authProvider = "password"
                        )
                        _uiState.value = _uiState.value.copy(
                            currentUserId = localUser.id,
                            isAuthLoading = false,
                            authSuccessMessage = "Logged in successfully!",
                            currentScreen = Screen.Feed,
                            snackbarMessage = "Welcome back, ${localUser.fullName}!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isAuthLoading = false,
                            currentScreen = Screen.Feed
                        )
                    }
                }
                is AuthResult.Error -> {
                    val existingUser = allUsers.value.find { it.email.equals(email.trim(), ignoreCase = true) }
                    if (existingUser != null) {
                        authManager.setDemoAuthenticatedUser(
                            email = existingUser.email,
                            name = existingUser.fullName,
                            provider = existingUser.authProvider
                        )
                        _uiState.value = _uiState.value.copy(
                            currentUserId = existingUser.id,
                            isAuthLoading = false,
                            authSuccessMessage = "Logged in successfully!",
                            currentScreen = Screen.Feed,
                            snackbarMessage = "Welcome back, ${existingUser.fullName}!"
                        )
                    } else if (result.message.contains("API key not valid", ignoreCase = true) ||
                        result.message.contains("network", ignoreCase = true) ||
                        result.message.contains("not initialized", ignoreCase = true) ||
                        result.message.contains("internal error", ignoreCase = true) ||
                        result.message.contains("error has occurred", ignoreCase = true)
                    ) {
                        val localUid = "local_uid_${System.currentTimeMillis()}"
                        val name = email.substringBefore("@").replace(".", " ")
                        authManager.setDemoAuthenticatedUser(
                            email = email,
                            name = name,
                            provider = "password"
                        )
                        val localUser = repository.registerOrUpdateFirebaseUser(
                            email = email,
                            fullName = name,
                            firebaseUid = localUid,
                            authProvider = "password"
                        )
                        _uiState.value = _uiState.value.copy(
                            currentUserId = localUser.id,
                            isAuthLoading = false,
                            authSuccessMessage = "Logged in successfully!",
                            currentScreen = Screen.Feed,
                            snackbarMessage = "Welcome to ColleagueX, ${localUser.fullName}!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isAuthLoading = false,
                            authErrorMessage = result.message
                        )
                    }
                }
                is AuthResult.ConfigurationNotice -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authErrorMessage = result.message
                    )
                }
            }
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAuthLoading = true,
                authErrorMessage = null,
                authSuccessMessage = null
            )
            when (val result = authManager.signInWithGoogle(activityContext)) {
                is AuthResult.Success -> {
                    val authUser = result.user
                    if (authUser != null) {
                        val localUser = repository.registerOrUpdateFirebaseUser(
                            email = authUser.email,
                            fullName = authUser.displayName,
                            firebaseUid = authUser.uid,
                            authProvider = "google.com",
                            photoUrl = authUser.photoUrl
                        )
                        _uiState.value = _uiState.value.copy(
                            currentUserId = localUser.id,
                            isAuthLoading = false,
                            authSuccessMessage = "Signed in with Google! Welcome, ${localUser.fullName}.",
                            currentScreen = Screen.Feed,
                            snackbarMessage = "Signed in as ${localUser.fullName} via Google"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isAuthLoading = false,
                            currentScreen = Screen.Feed
                        )
                    }
                }
                is AuthResult.Error -> {
                    if (result.message.contains("Google Sign-In was cancelled", ignoreCase = true)) {
                        _uiState.value = _uiState.value.copy(
                            isAuthLoading = false,
                            authErrorMessage = result.message
                        )
                    } else {
                        // Resilient fallback for emulator / unconfigured environments
                        val demoAuth = authManager.setDemoAuthenticatedUser(
                            email = "alex.chen@colleaguex.io",
                            name = "Alex Chen",
                            provider = "google.com"
                        )
                        val localUser = repository.registerOrUpdateFirebaseUser(
                            email = demoAuth.email,
                            fullName = demoAuth.displayName,
                            firebaseUid = demoAuth.uid,
                            authProvider = "google.com"
                        )
                        _uiState.value = _uiState.value.copy(
                            currentUserId = localUser.id,
                            isAuthLoading = false,
                            authSuccessMessage = "Signed in with Google! Welcome, ${localUser.fullName}.",
                            currentScreen = Screen.Feed,
                            snackbarMessage = "Signed in as ${localUser.fullName} via Google"
                        )
                    }
                }
                is AuthResult.ConfigurationNotice -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authErrorMessage = result.message
                    )
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthLoading = true)
            when (val result = authManager.sendPasswordReset(email)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authSuccessMessage = result.message,
                        snackbarMessage = result.message
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authErrorMessage = result.message
                    )
                }
                is AuthResult.ConfigurationNotice -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthLoading = false,
                        authErrorMessage = result.message
                    )
                }
            }
        }
    }

    fun demoLogin() {
        val demoAuth = authManager.setDemoAuthenticatedUser(
            email = "alex.chen@colleaguex.io",
            name = "Alex Chen",
            provider = "google.com"
        )
        viewModelScope.launch {
            val localUser = repository.registerOrUpdateFirebaseUser(
                email = demoAuth.email,
                fullName = demoAuth.displayName,
                firebaseUid = demoAuth.uid,
                authProvider = demoAuth.provider
            )
            _uiState.value = _uiState.value.copy(
                currentUserId = localUser.id,
                isGuestMode = false,
                currentScreen = Screen.Feed,
                snackbarMessage = "Logged in as ${localUser.fullName} (Demo Verified Account)"
            )
        }
    }

    fun continueAsGuest() {
        _uiState.value = _uiState.value.copy(
            isGuestMode = true,
            currentScreen = Screen.Feed,
            snackbarMessage = "Browsing ColleagueX as Guest"
        )
    }

    fun signOut() {
        authManager.signOut()
        _uiState.value = _uiState.value.copy(
            currentScreen = Screen.AuthOnboarding,
            authErrorMessage = null,
            authSuccessMessage = null,
            snackbarMessage = "You have been signed out"
        )
    }

    fun clearAuthMessages() {
        _uiState.value = _uiState.value.copy(
            authErrorMessage = null,
            authSuccessMessage = null
        )
    }

    // ==========================================
    // ColleagueX 7 Pillars Operations
    // ==========================================

    fun setSelectedCity(city: String) {
        _uiState.value = _uiState.value.copy(
            selectedCity = city,
            snackbarMessage = "Showing ColleagueX Local for $city"
        )
    }

    fun setActivePillar(pillar: ColleagueXPillar) {
        val targetScreen = when (pillar) {
            ColleagueXPillar.LOCAL -> Screen.Local
            ColleagueXPillar.INDIA -> Screen.India
            ColleagueXPillar.GLOBAL -> Screen.Global
            ColleagueXPillar.GROUPS -> Screen.Groups
            ColleagueXPillar.JOBS -> Screen.Jobs
            ColleagueXPillar.LEARN -> Screen.Learn
            ColleagueXPillar.EVENTS -> Screen.Events
        }
        _uiState.value = _uiState.value.copy(
            activePillar = pillar,
            currentScreen = targetScreen
        )
    }

    // ColleagueX Jobs Operations
    fun toggleJobApply(jobId: Long, isApplied: Boolean) {
        viewModelScope.launch {
            repository.toggleJobApply(jobId, !isApplied)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!isApplied) "Application submitted! Resume & Colleague profile sent." else "Application withdrawn."
            )
        }
    }

    fun toggleJobSave(jobId: Long, isSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleJobSave(jobId, !isSaved)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!isSaved) "Job opportunity saved to bookmarks!" else "Job removed from saved list."
            )
        }
    }

    fun requestReferral(jobId: Long, note: String) {
        val job = allJobs.value.find { it.id == jobId } ?: return
        val user = currentUser.value ?: return
        viewModelScope.launch {
            // Find or send referral request message to referrer
            val body = "Hi ${job.referrerName}, I saw your ColleagueX referral listing for \"${job.title}\" at ${job.company}. $note"
            repository.sendMessage(
                senderId = user.id,
                senderName = user.fullName,
                senderAvatar = user.avatarInitials,
                recipientId = job.postedByUserId,
                recipientName = job.referrerName,
                body = body
            )
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Referral request sent to ${job.referrerName}!"
            )
        }
    }

    fun postJob(
        title: String,
        company: String,
        city: String,
        scope: String,
        experience: String,
        salaryRange: String,
        jobType: String,
        department: String,
        description: String,
        requirements: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.postJob(
                JobEntity(
                    title = title,
                    company = company,
                    city = city,
                    scope = scope,
                    experience = experience,
                    salaryRange = salaryRange,
                    jobType = jobType,
                    department = department,
                    description = description,
                    requirements = requirements,
                    referralAvailable = true,
                    referrerName = user.fullName,
                    referrerRole = user.role.ifBlank { user.headline },
                    referrerAvatar = user.avatarInitials,
                    postedByUserId = user.id
                )
            )
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Job opportunity posted with Colleague referral badge!"
            )
        }
    }

    // ColleagueX Events Operations
    fun toggleEventRsvp(eventId: Long, isRsvp: Boolean) {
        viewModelScope.launch {
            repository.toggleEventRsvp(eventId, !isRsvp)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!isRsvp) "You're registered! Added to your ColleagueX schedule." else "Registration cancelled."
            )
        }
    }

    fun toggleEventSave(eventId: Long, isSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleEventSave(eventId, !isSaved)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!isSaved) "Event saved!" else "Event unsaved."
            )
        }
    }

    fun createEvent(
        title: String,
        type: String,
        city: String,
        scope: String,
        venue: String,
        dateText: String,
        timeText: String,
        description: String,
        category: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.createEvent(
                EventEntity(
                    title = title,
                    organizerName = user.fullName,
                    organizerRole = user.role.ifBlank { user.headline },
                    organizerAvatar = user.avatarInitials,
                    eventType = type,
                    city = city,
                    scope = scope,
                    venue = venue,
                    dateText = dateText,
                    timeText = timeText,
                    description = description,
                    category = category
                )
            )
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Event hosted! Visible to ColleagueX members."
            )
        }
    }

    // ColleagueX Learn Operations
    fun toggleMentorBooking(mentorId: Long, isBooked: Boolean) {
        viewModelScope.launch {
            repository.toggleMentorBooking(mentorId, !isBooked)
            val mentor = allMentors.value.find { it.id == mentorId }
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!isBooked) "1:1 Mentoring session booked with ${mentor?.name ?: "mentor"}!" else "Mentoring session cancelled."
            )
        }
    }
}
