package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConnectionStatus
import com.example.data.model.ModerationStatus
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkCircleDao {

    // --- Users ---
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    suspend fun getUserByFirebaseUid(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    fun observeUserByFirebaseUid(uid: String): Flow<UserEntity?>

    @Query("""
        UPDATE users 
        SET fullName = :displayName, 
            role = :jobTitle, 
            bio = :bio, 
            headline = :headline, 
            employer = :employer, 
            location = :location, 
            skills = :skills 
        WHERE firebaseUid = :firebaseUid
    """)
    suspend fun updateProfileByFirebaseUid(
        firebaseUid: String,
        displayName: String,
        jobTitle: String,
        bio: String,
        headline: String,
        employer: String,
        location: String,
        skills: String
    ): Int

    @Query("SELECT * FROM users WHERE id != :currentUserId")
    fun getOtherProfessionals(currentUserId: Long): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET verificationStatus = :status, isVerified = :isVerified WHERE id = :userId")
    suspend fun updateUserVerification(userId: Long, status: VerificationStatus, isVerified: Boolean)

    // --- User Profile Entity ---
    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun getUserProfile(id: Long = 1): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET bio = :bio, jobTitle = :jobTitle, companyName = :companyName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateUserProfileDetails(
        id: Long = 1,
        bio: String,
        jobTitle: String,
        companyName: String,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    // --- Communities ---
    @Query("SELECT * FROM communities ORDER BY memberCount DESC")
    fun getAllCommunities(): Flow<List<CommunityEntity>>

    @Query("SELECT * FROM communities WHERE isJoined = 1 ORDER BY name ASC")
    fun getJoinedCommunities(): Flow<List<CommunityEntity>>

    @Query("SELECT * FROM communities WHERE id = :id LIMIT 1")
    fun getCommunityById(id: Long): Flow<CommunityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(community: CommunityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunities(communities: List<CommunityEntity>)

    @Query("UPDATE communities SET isJoined = :isJoined, memberCount = memberCount + (CASE WHEN :isJoined = 1 THEN 1 ELSE -1 END) WHERE id = :communityId")
    suspend fun toggleCommunityJoin(communityId: Long, isJoined: Boolean)

    // --- Posts ---
    @Query("SELECT * FROM posts WHERE moderationStatus != 'REMOVED' ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE communityId = :communityId AND moderationStatus != 'REMOVED' ORDER BY createdAt DESC")
    fun getPostsByCommunity(communityId: Long): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorId = :authorId AND moderationStatus != 'REMOVED' ORDER BY createdAt DESC")
    fun getPostsByAuthor(authorId: Long): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isSaved = 1 AND moderationStatus != 'REMOVED' ORDER BY createdAt DESC")
    fun getSavedPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    fun getPostById(id: Long): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE tags LIKE '%' || :tag || '%' AND moderationStatus != 'REMOVED' ORDER BY createdAt DESC")
    fun getPostsByTag(tag: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("""
        UPDATE posts 
        SET isLiked = :isLiked, 
            likesCount = CASE 
                WHEN :isLiked = 1 THEN likesCount + 1 
                ELSE (CASE WHEN likesCount > 0 THEN likesCount - 1 ELSE 0 END) 
            END 
        WHERE id = :postId
    """)
    suspend fun togglePostLike(postId: Long, isLiked: Boolean)

    @Query("UPDATE posts SET isSaved = :isSaved WHERE id = :postId")
    suspend fun togglePostSave(postId: Long, isSaved: Boolean)

    @Query("UPDATE posts SET moderationStatus = :status WHERE id = :postId")
    suspend fun updatePostModerationStatus(postId: Long, status: ModerationStatus)

    @Query("UPDATE posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentsCount(postId: Long)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Long)

    // --- Comments ---
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Query("UPDATE comments SET isLiked = :isLiked, likesCount = likesCount + (CASE WHEN :isLiked = 1 THEN 1 ELSE -1 END) WHERE id = :commentId")
    suspend fun toggleCommentLike(commentId: Long, isLiked: Boolean)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Long)

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // --- Verification Requests ---
    @Query("SELECT * FROM verification_requests ORDER BY createdAt DESC")
    fun getAllVerificationRequests(): Flow<List<VerificationRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationRequest(req: VerificationRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationRequests(reqs: List<VerificationRequestEntity>)

    @Query("UPDATE verification_requests SET status = :status, reviewerNotes = :notes WHERE id = :id")
    suspend fun updateVerificationRequestStatus(id: Long, status: VerificationStatus, notes: String)

    // --- Reports ---
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ReportEntity>)

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: Long, status: String)

    // --- Articles ---
    @Query("SELECT * FROM articles ORDER BY createdAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun getSavedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    fun getArticleById(id: Long): Flow<ArticleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isSaved = :isSaved WHERE id = :articleId")
    suspend fun toggleArticleSave(articleId: Long, isSaved: Boolean)

    @Query("UPDATE articles SET likesCount = likesCount + (CASE WHEN :isLiked = 1 THEN 1 ELSE -1 END) WHERE id = :articleId")
    suspend fun updateArticleLikes(articleId: Long, isLiked: Boolean)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    // --- Connections & Message Requests ---
    @Query("SELECT * FROM user_connections ORDER BY updatedAt DESC")
    fun getAllConnections(): Flow<List<UserConnectionEntity>>

    @Query("SELECT * FROM user_connections WHERE (requesterId = :userId OR receiverId = :userId) AND status = 'ACCEPTED' ORDER BY updatedAt DESC")
    fun getAcceptedConnectionsForUser(userId: Long): Flow<List<UserConnectionEntity>>

    @Query("SELECT * FROM user_connections WHERE receiverId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingIncomingRequests(userId: Long): Flow<List<UserConnectionEntity>>

    @Query("SELECT * FROM user_connections WHERE requesterId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingOutgoingRequests(userId: Long): Flow<List<UserConnectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: UserConnectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnections(connections: List<UserConnectionEntity>)

    @Query("UPDATE user_connections SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateConnectionStatus(id: Long, status: ConnectionStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM user_connections WHERE id = :id")
    suspend fun deleteConnection(id: Long)

    // --- Skill Endorsements ---
    @Query("SELECT * FROM skill_endorsements ORDER BY timestamp DESC")
    fun getAllEndorsements(): Flow<List<SkillEndorsementEntity>>

    @Query("SELECT * FROM skill_endorsements WHERE targetUserId = :userId ORDER BY timestamp DESC")
    fun getAllEndorsementsForUser(userId: Long): Flow<List<SkillEndorsementEntity>>

    @Query("SELECT * FROM skill_endorsements WHERE targetUserId = :userId AND skillName = :skillName ORDER BY timestamp DESC")
    fun getEndorsementsForUserSkill(userId: Long, skillName: String): Flow<List<SkillEndorsementEntity>>

    @Query("SELECT COUNT(*) FROM skill_endorsements WHERE targetUserId = :userId AND skillName = :skillName")
    fun getEndorsementCount(userId: Long, skillName: String): Flow<Int>

    @Query("SELECT * FROM skill_endorsements WHERE targetUserId = :targetUserId AND endorserUserId = :endorserUserId AND skillName = :skillName LIMIT 1")
    suspend fun findEndorsement(targetUserId: Long, endorserUserId: Long, skillName: String): SkillEndorsementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndorsement(endorsement: SkillEndorsementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndorsements(endorsements: List<SkillEndorsementEntity>)

    @Query("DELETE FROM skill_endorsements WHERE targetUserId = :targetUserId AND endorserUserId = :endorserUserId AND skillName = :skillName")
    suspend fun deleteEndorsement(targetUserId: Long, endorserUserId: Long, skillName: String)

    @Query("DELETE FROM skill_endorsements WHERE id = :id")
    suspend fun deleteEndorsementById(id: Long)

    // --- ColleagueX Jobs ---
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity): Long

    @Query("UPDATE jobs SET isApplied = :applied, applicantsCount = applicantsCount + (CASE WHEN :applied THEN 1 ELSE -1 END) WHERE id = :jobId")
    suspend fun updateJobApplied(jobId: Long, applied: Boolean)

    @Query("UPDATE jobs SET isSaved = :saved WHERE id = :jobId")
    suspend fun updateJobSaved(jobId: Long, saved: Boolean)

    @Query("SELECT COUNT(*) FROM jobs")
    suspend fun getJobsCount(): Int

    // --- ColleagueX Events ---
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Query("UPDATE events SET isRsvp = :rsvp, attendeesCount = attendeesCount + (CASE WHEN :rsvp THEN 1 ELSE -1 END) WHERE id = :eventId")
    suspend fun updateEventRsvp(eventId: Long, rsvp: Boolean)

    @Query("UPDATE events SET isSaved = :saved WHERE id = :eventId")
    suspend fun updateEventSaved(eventId: Long, saved: Boolean)

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventsCount(): Int

    // --- ColleagueX Learn: Mentors ---
    @Query("SELECT * FROM mentors ORDER BY rating DESC")
    fun getAllMentors(): Flow<List<MentorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMentors(mentors: List<MentorEntity>)

    @Query("UPDATE mentors SET isBooked = :booked WHERE id = :mentorId")
    suspend fun updateMentorBooked(mentorId: Long, booked: Boolean)

    @Query("SELECT COUNT(*) FROM mentors")
    suspend fun getMentorsCount(): Int
}

