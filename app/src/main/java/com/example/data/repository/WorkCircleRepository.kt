package com.example.data.repository

import com.example.data.local.ArticleEntity
import com.example.data.local.AuditLogEntity
import com.example.data.local.CommentEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.EventEntity
import com.example.data.local.JobEntity
import com.example.data.local.MentorEntity
import com.example.data.local.MessageEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PostEntity
import com.example.data.local.ReportEntity
import com.example.data.local.SampleData
import com.example.data.local.SkillEndorsementEntity
import com.example.data.local.UserConnectionEntity
import com.example.data.local.UserEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VerificationRequestEntity
import com.example.data.local.WorkCircleDao
import com.example.data.model.ConnectionStatus
import com.example.data.model.ModerationStatus
import com.example.data.model.PostType
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class WorkCircleRepository(private val dao: WorkCircleDao) {

    val allPosts: Flow<List<PostEntity>> = dao.getAllPosts()
    val savedPosts: Flow<List<PostEntity>> = dao.getSavedPosts()
    val allCommunities: Flow<List<CommunityEntity>> = dao.getAllCommunities()
    val joinedCommunities: Flow<List<CommunityEntity>> = dao.getJoinedCommunities()
    val allArticles: Flow<List<ArticleEntity>> = dao.getAllArticles()
    val savedArticles: Flow<List<ArticleEntity>> = dao.getSavedArticles()
    val notifications: Flow<List<NotificationEntity>> = dao.getNotifications()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allMessages: Flow<List<MessageEntity>> = dao.getAllMessages()
    val allConnections: Flow<List<UserConnectionEntity>> = dao.getAllConnections()
    val verificationRequests: Flow<List<VerificationRequestEntity>> = dao.getAllVerificationRequests()
    val reports: Flow<List<ReportEntity>> = dao.getAllReports()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getRecentAuditLogs()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile(1)
    val allEndorsements: Flow<List<SkillEndorsementEntity>> = dao.getAllEndorsements()
    val allJobs: Flow<List<JobEntity>> = dao.getAllJobs()
    val allEvents: Flow<List<EventEntity>> = dao.getAllEvents()
    val allMentors: Flow<List<MentorEntity>> = dao.getAllMentors()

    fun getUser(userId: Long): Flow<UserEntity?> = dao.getUserById(userId)

    fun getCommunity(id: Long): Flow<CommunityEntity?> = dao.getCommunityById(id)

    fun getPostsByCommunity(communityId: Long): Flow<List<PostEntity>> = dao.getPostsByCommunity(communityId)

    fun getPostById(postId: Long): Flow<PostEntity?> = dao.getPostById(postId)

    fun getComments(postId: Long): Flow<List<CommentEntity>> = dao.getCommentsForPost(postId)

    fun getConversationMessages(convId: String): Flow<List<MessageEntity>> = dao.getMessagesForConversation(convId)

    suspend fun createPost(
        author: UserEntity,
        communityId: Long,
        communityName: String,
        postType: PostType,
        title: String,
        body: String,
        tags: String,
        isAnonymous: Boolean,
        pollOptions: List<String> = emptyList()
    ): Long {
        val anonymousBadge = if (isAnonymous) {
            "Verified ${author.role} @ ${author.industry}"
        } else {
            ""
        }
        val optionsDelimited = if (pollOptions.isNotEmpty()) pollOptions.joinToString("|") else ""
        val initialVotes = if (pollOptions.isNotEmpty()) pollOptions.map { "0" }.joinToString("|") else ""

        val post = PostEntity(
            authorId = if (isAnonymous) 0 else author.id,
            authorName = if (isAnonymous) "Anonymous Professional" else author.fullName,
            authorHeadline = if (isAnonymous) "Industry Peer" else author.headline,
            authorAvatar = if (isAnonymous) "AP" else author.avatarInitials,
            isAnonymous = isAnonymous,
            anonymousBadge = anonymousBadge,
            communityId = communityId,
            communityName = communityName,
            postType = postType,
            title = title,
            body = body,
            tags = tags,
            pollOptions = optionsDelimited,
            pollVotes = initialVotes,
            likesCount = 0,
            commentsCount = 0,
            isLiked = false,
            isSaved = false,
            createdAt = System.currentTimeMillis(),
            moderationStatus = ModerationStatus.PUBLISHED
        )

        dao.insertAuditLog(
            AuditLogEntity(
                actorName = author.fullName,
                action = "CREATE_POST",
                targetType = "POST",
                targetId = title.take(30),
                details = "Post created in $communityName (Anonymous: $isAnonymous)"
            )
        )
        return dao.insertPost(post)
    }

    fun getPostsByTag(tag: String): Flow<List<PostEntity>> = dao.getPostsByTag(tag)

    suspend fun togglePostLike(postId: Long, currentLiked: Boolean) {
        dao.togglePostLike(postId, !currentLiked)
    }

    suspend fun togglePostSave(postId: Long, currentSaved: Boolean) {
        dao.togglePostSave(postId, !currentSaved)
    }

    suspend fun addComment(
        postId: Long,
        author: UserEntity,
        body: String,
        isAnonymous: Boolean
    ): Long {
        val comment = CommentEntity(
            postId = postId,
            authorId = if (isAnonymous) 0 else author.id,
            authorName = if (isAnonymous) "Anonymous Professional" else author.fullName,
            authorHeadline = if (isAnonymous) "Verified Professional" else author.headline,
            authorAvatar = if (isAnonymous) "AP" else author.avatarInitials,
            isAnonymous = isAnonymous,
            body = body,
            createdAt = System.currentTimeMillis()
        )
        dao.incrementCommentsCount(postId)
        return dao.insertComment(comment)
    }

    suspend fun toggleCommentLike(commentId: Long, currentLiked: Boolean) {
        dao.toggleCommentLike(commentId, !currentLiked)
    }

    suspend fun votePoll(post: PostEntity, optionIndex: Int) {
        if (post.userVotedOptionIndex >= 0) return // already voted
        val votes = post.pollVotes.split("|").map { it.toIntOrNull() ?: 0 }.toMutableList()
        if (optionIndex in votes.indices) {
            votes[optionIndex] = votes[optionIndex] + 1
            val updated = post.copy(
                pollVotes = votes.joinToString("|"),
                userVotedOptionIndex = optionIndex
            )
            dao.updatePost(updated)
        }
    }

    suspend fun toggleJoinCommunity(communityId: Long, currentJoined: Boolean) {
        dao.toggleCommunityJoin(communityId, !currentJoined)
    }

    suspend fun sendMessage(
        sender: UserEntity,
        recipient: UserEntity,
        body: String
    ): Long {
        val convId = if (sender.id < recipient.id) "conv_${sender.id}_${recipient.id}" else "conv_${recipient.id}_${sender.id}"
        val message = MessageEntity(
            conversationId = convId,
            senderId = sender.id,
            senderName = sender.fullName,
            senderAvatar = sender.avatarInitials,
            recipientId = recipient.id,
            recipientName = recipient.fullName,
            body = body,
            timestamp = System.currentTimeMillis(),
            isRead = true
        )
        return dao.insertMessage(message)
    }

    suspend fun sendMessage(
        senderId: Long,
        senderName: String,
        senderAvatar: String,
        recipientId: Long,
        recipientName: String,
        body: String
    ): Long {
        val convId = if (senderId < recipientId) "conv_${senderId}_${recipientId}" else "conv_${recipientId}_${senderId}"
        val message = MessageEntity(
            conversationId = convId,
            senderId = senderId,
            senderName = senderName,
            senderAvatar = senderAvatar,
            recipientId = recipientId,
            recipientName = recipientName,
            body = body,
            timestamp = System.currentTimeMillis(),
            isRead = true
        )
        return dao.insertMessage(message)
    }

    suspend fun sendConnectionRequest(
        requester: UserEntity,
        receiver: UserEntity,
        note: String = "",
        isMessageRequest: Boolean = false
    ): Long {
        val connection = UserConnectionEntity(
            requesterId = requester.id,
            requesterName = requester.fullName,
            requesterHeadline = requester.headline,
            requesterAvatar = requester.avatarInitials,
            receiverId = receiver.id,
            receiverName = receiver.fullName,
            receiverHeadline = receiver.headline,
            receiverAvatar = receiver.avatarInitials,
            note = note,
            status = ConnectionStatus.PENDING,
            isMessageRequest = isMessageRequest,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertNotification(
            NotificationEntity(
                title = if (isMessageRequest) "New Message Request" else "New Connection Request",
                body = "${requester.fullName} (${requester.headline}) sent you a ${if (isMessageRequest) "message request" else "connection request"}${if (note.isNotBlank()) ": \"$note\"" else "."}",
                type = "CONNECTION",
                isRead = false,
                timestamp = System.currentTimeMillis(),
                relatedEntityId = requester.id
            )
        )
        return dao.insertConnection(connection)
    }

    suspend fun acceptConnectionRequest(requestId: Long) {
        dao.updateConnectionStatus(requestId, ConnectionStatus.ACCEPTED)
    }

    suspend fun declineConnectionRequest(requestId: Long) {
        dao.updateConnectionStatus(requestId, ConnectionStatus.DECLINED)
    }


    suspend fun markNotificationRead(id: Long) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsRead() {
        dao.markAllNotificationsAsRead()
    }

    suspend fun submitReport(
        reporter: UserEntity,
        entityType: String,
        entityId: Long,
        entityTitle: String,
        reason: String,
        details: String
    ): Long {
        val report = ReportEntity(
            reporterId = reporter.id,
            reporterName = reporter.fullName,
            entityType = entityType,
            entityId = entityId,
            entityTitle = entityTitle,
            reason = reason,
            details = details,
            status = "PENDING_REVIEW",
            createdAt = System.currentTimeMillis()
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorName = reporter.fullName,
                action = "SUBMIT_REPORT",
                targetType = entityType,
                targetId = entityId.toString(),
                details = "Reason: $reason"
            )
        )
        return dao.insertReport(report)
    }

    suspend fun moderateReport(reportId: Long, newStatus: String, adminName: String) {
        dao.updateReportStatus(reportId, newStatus)
        dao.insertAuditLog(
            AuditLogEntity(
                actorName = adminName,
                action = "MODERATE_REPORT",
                targetType = "REPORT",
                targetId = reportId.toString(),
                details = "Resolution set to $newStatus"
            )
        )
    }

    suspend fun updatePostModeration(postId: Long, status: ModerationStatus, adminName: String) {
        dao.updatePostModerationStatus(postId, status)
        dao.insertAuditLog(
            AuditLogEntity(
                actorName = adminName,
                action = "MODERATE_POST",
                targetType = "POST",
                targetId = postId.toString(),
                details = "Post status updated to $status"
            )
        )
    }

    suspend fun deletePost(postId: Long) {
        dao.deletePost(postId)
    }

    suspend fun submitVerificationRequest(
        user: UserEntity,
        companyName: String,
        workEmail: String,
        method: String
    ): Long {
        val req = VerificationRequestEntity(
            userId = user.id,
            userName = user.fullName,
            companyName = companyName,
            workEmail = workEmail,
            method = method,
            status = VerificationStatus.PENDING,
            reviewerNotes = "Awaiting verification token / HR attestation",
            createdAt = System.currentTimeMillis()
        )
        dao.updateUserVerification(user.id, VerificationStatus.PENDING, false)
        return dao.insertVerificationRequest(req)
    }

    suspend fun reviewVerificationRequest(
        requestId: Long,
        userId: Long,
        status: VerificationStatus,
        notes: String,
        adminName: String
    ) {
        dao.updateVerificationRequestStatus(requestId, status, notes)
        val isVerified = status == VerificationStatus.VERIFIED
        dao.updateUserVerification(userId, status, isVerified)
        dao.insertAuditLog(
            AuditLogEntity(
                actorName = adminName,
                action = "REVIEW_VERIFICATION",
                targetType = "VERIFICATION_REQUEST",
                targetId = requestId.toString(),
                details = "Decision: $status. Notes: $notes"
            )
        )
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }

    suspend fun updateUserProfile(
        bio: String,
        jobTitle: String,
        companyName: String
    ) {
        val existing = dao.getUserProfile(1).firstOrNull()
        if (existing == null) {
            dao.insertUserProfile(
                UserProfileEntity(
                    id = 1,
                    fullName = "Alex Chen",
                    bio = bio,
                    jobTitle = jobTitle,
                    companyName = companyName
                )
            )
        } else {
            dao.updateUserProfileDetails(
                id = 1,
                bio = bio,
                jobTitle = jobTitle,
                companyName = companyName,
                updatedAt = System.currentTimeMillis()
            )
        }

        // Keep UserEntity synchronized with the updated profile
        val currentUser = dao.getUserById(1).firstOrNull()
        if (currentUser != null) {
            dao.updateUser(
                currentUser.copy(
                    bio = bio,
                    role = jobTitle,
                    employer = companyName,
                    headline = "$jobTitle • $companyName"
                )
            )
        }
    }

    suspend fun toggleArticleSave(articleId: Long, currentSaved: Boolean) {
        dao.toggleArticleSave(articleId, !currentSaved)
    }

    suspend fun toggleArticleLike(articleId: Long, currentLiked: Boolean) {
        dao.updateArticleLikes(articleId, !currentLiked)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)

    suspend fun getUserByFirebaseUid(uid: String): UserEntity? = dao.getUserByFirebaseUid(uid)

    fun observeUserByFirebaseUid(uid: String): Flow<UserEntity?> = dao.observeUserByFirebaseUid(uid)

    suspend fun saveUserProfileByFirebaseUid(
        firebaseUid: String,
        displayName: String,
        jobTitle: String,
        bio: String,
        headline: String = "",
        employer: String = "",
        industry: String = "Technology & AI",
        location: String = "Global / Remote",
        skills: String = "Product Strategy, Cross-Functional Leadership"
    ): UserEntity {
        val existing = if (firebaseUid.isNotBlank()) dao.getUserByFirebaseUid(firebaseUid) else null
        val resolvedHeadline = headline.ifBlank { "$jobTitle • ${employer.ifBlank { "Independent" }}" }
        val initials = if (displayName.isNotBlank()) {
            displayName.split(" ")
                .filter { it.isNotBlank() }
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .uppercase()
        } else "WC"

        if (existing != null) {
            val updated = existing.copy(
                fullName = displayName,
                role = jobTitle,
                bio = bio,
                headline = resolvedHeadline,
                employer = employer.ifBlank { existing.employer },
                industry = industry.ifBlank { existing.industry },
                location = location.ifBlank { existing.location },
                skills = skills.ifBlank { existing.skills },
                avatarInitials = if (initials.isNotBlank()) initials else existing.avatarInitials
            )
            dao.updateUser(updated)
            return updated
        } else {
            val email = "user_${firebaseUid.take(8)}@colleaguex.io"
            val newUser = UserEntity(
                email = email,
                fullName = displayName,
                username = displayName.lowercase().replace(" ", "_"),
                headline = resolvedHeadline,
                bio = bio,
                employer = employer.ifBlank { "Independent" },
                industry = industry,
                role = jobTitle,
                location = location,
                experienceLevel = "Mid-Senior",
                avatarInitials = initials,
                isVerified = false,
                verificationStatus = VerificationStatus.NOT_STARTED,
                roleType = UserRole.REGULAR_PROFESSIONAL,
                firebaseUid = firebaseUid,
                authProvider = "firebase",
                photoUrl = "",
                skills = skills
            )
            val newId = dao.insertUser(newUser)
            return newUser.copy(id = newId)
        }
    }

    suspend fun registerOrUpdateFirebaseUser(
        email: String,
        fullName: String,
        firebaseUid: String,
        authProvider: String,
        photoUrl: String? = null,
        headline: String = "",
        employer: String = "",
        industry: String = "Technology & AI",
        role: String = "Professional"
    ): UserEntity {
        val existing = if (firebaseUid.isNotBlank()) {
            dao.getUserByFirebaseUid(firebaseUid) ?: dao.getUserByEmail(email)
        } else {
            dao.getUserByEmail(email)
        }

        if (existing != null) {
            val updated = existing.copy(
                fullName = if (fullName.isNotBlank()) fullName else existing.fullName,
                firebaseUid = firebaseUid.ifBlank { existing.firebaseUid },
                authProvider = authProvider.ifBlank { existing.authProvider },
                photoUrl = photoUrl ?: existing.photoUrl
            )
            dao.updateUser(updated)
            return updated
        } else {
            val initials = if (fullName.isNotBlank()) {
                fullName.split(" ")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
            } else {
                email.take(2).uppercase()
            }
            val newUser = UserEntity(
                email = email,
                fullName = fullName.ifBlank { email.substringBefore("@").replace(".", " ") },
                username = email.substringBefore("@").replace(".", "_").lowercase(),
                headline = headline.ifBlank { "$role • ${employer.ifBlank { "Independent" }}" },
                bio = "Member of the ColleagueX professional network.",
                employer = employer.ifBlank { "Independent" },
                industry = industry,
                role = role,
                location = "Global / Remote",
                experienceLevel = "Mid-Senior",
                avatarInitials = if (initials.isNotBlank()) initials else "CX",
                isVerified = false,
                verificationStatus = VerificationStatus.NOT_STARTED,
                roleType = UserRole.REGULAR_PROFESSIONAL,
                firebaseUid = firebaseUid,
                authProvider = authProvider,
                photoUrl = photoUrl ?: ""
            )
            val newId = dao.insertUser(newUser)
            return newUser.copy(id = newId)
        }
    }

    // --- Skill Endorsements ---
    fun getEndorsementsForUser(userId: Long): Flow<List<SkillEndorsementEntity>> = dao.getAllEndorsementsForUser(userId)

    fun getEndorsementsForSkill(userId: Long, skillName: String): Flow<List<SkillEndorsementEntity>> =
        dao.getEndorsementsForUserSkill(userId, skillName)

    fun getEndorsementCount(userId: Long, skillName: String): Flow<Int> =
        dao.getEndorsementCount(userId, skillName)

    suspend fun endorseSkill(
        targetUserId: Long,
        endorser: UserEntity,
        skillName: String,
        note: String = ""
    ): Boolean {
        val existing = dao.findEndorsement(targetUserId, endorser.id, skillName)
        return if (existing != null) {
            dao.deleteEndorsement(targetUserId, endorser.id, skillName)
            false
        } else {
            dao.insertEndorsement(
                SkillEndorsementEntity(
                    targetUserId = targetUserId,
                    endorserUserId = endorser.id,
                    endorserName = endorser.fullName,
                    endorserRole = endorser.role.ifBlank { endorser.headline },
                    endorserPhotoUrl = endorser.photoUrl,
                    isEndorserVerified = endorser.isVerified,
                    skillName = skillName,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
            true
        }
    }

    suspend fun deleteEndorsementById(id: Long) {
        dao.deleteEndorsementById(id)
    }

    // --- ColleagueX Jobs ---
    suspend fun toggleJobApply(jobId: Long, isApplied: Boolean) {
        dao.updateJobApplied(jobId, isApplied)
    }

    suspend fun toggleJobSave(jobId: Long, isSaved: Boolean) {
        dao.updateJobSaved(jobId, isSaved)
    }

    suspend fun postJob(job: JobEntity): Long {
        return dao.insertJob(job)
    }

    // --- ColleagueX Events ---
    suspend fun toggleEventRsvp(eventId: Long, isRsvp: Boolean) {
        dao.updateEventRsvp(eventId, isRsvp)
    }

    suspend fun toggleEventSave(eventId: Long, isSaved: Boolean) {
        dao.updateEventSaved(eventId, isSaved)
    }

    suspend fun createEvent(event: EventEntity): Long {
        return dao.insertEvent(event)
    }

    // --- ColleagueX Learn ---
    suspend fun toggleMentorBooking(mentorId: Long, isBooked: Boolean) {
        dao.updateMentorBooked(mentorId, isBooked)
    }

    suspend fun ensureInitialDataLoaded() {
        if (dao.getJobsCount() == 0) {
            dao.insertJobs(SampleData.getInitialJobs())
        }
        if (dao.getEventsCount() == 0) {
            dao.insertEvents(SampleData.getInitialEvents())
        }
        if (dao.getMentorsCount() == 0) {
            dao.insertMentors(SampleData.getInitialMentors())
        }
    }
}
