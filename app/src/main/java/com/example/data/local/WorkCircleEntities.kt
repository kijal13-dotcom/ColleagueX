package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ConnectionStatus
import com.example.data.model.ModerationStatus
import com.example.data.model.PostType
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val fullName: String,
    val username: String,
    val headline: String,
    val bio: String,
    val employer: String,
    val industry: String,
    val role: String,
    val location: String,
    val experienceLevel: String,
    val avatarInitials: String,
    val isVerified: Boolean = false,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_STARTED,
    val verifiedBadgeText: String = "",
    val roleType: UserRole = UserRole.REGULAR_PROFESSIONAL,
    val employerVisibility: Boolean = true,
    val locationVisibility: Boolean = true,
    val connectionsCount: Int = 0,
    val contributionsCount: Int = 0,
    val skills: String = "Product Strategy, Cross-Functional Leadership, Mentorship",
    val firebaseUid: String = "",
    val authProvider: String = "local",
    val photoUrl: String = ""
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val fullName: String = "Alex Chen",
    val bio: String = "Staff Product Manager specializing in AI workflows, developer velocity, and building transparent engineering cultures.",
    val jobTitle: String = "Staff Product Manager",
    val companyName: String = "Google",
    val headline: String = "Staff Product Manager • AI Platform",
    val location: String = "San Francisco, CA",
    val skills: String = "Product Strategy, AI Systems, Cross-Functional Leadership",
    val avatarInitials: String = "AC",
    val isVerified: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "communities")
data class CommunityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val slug: String,
    val description: String,
    val category: String,
    val memberCount: Int,
    val privacyType: String = "Public",
    val isJoined: Boolean = false,
    val rules: String = "1. Respect privacy & confidentiality\n2. Constructive discussions only\n3. No unsolicited job spam\n4. Anonymity must adhere to safety rules",
    val iconCategory: String = "Tech"
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: Long,
    val authorName: String,
    val authorHeadline: String,
    val authorAvatar: String,
    val isAnonymous: Boolean = false,
    val anonymousBadge: String = "", // e.g., "Senior PM @ Tier-1 Tech" without revealing identity
    val communityId: Long,
    val communityName: String,
    val postType: PostType = PostType.DISCUSSION,
    val title: String,
    val body: String,
    val tags: String = "",
    val pollOptions: String = "", // Delimited by "|"
    val pollVotes: String = "", // Delimited by "|"
    val userVotedOptionIndex: Int = -1,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val moderationStatus: ModerationStatus = ModerationStatus.PUBLISHED,
    val pillarScope: String = "INDIA", // "LOCAL", "INDIA", "GLOBAL", "GROUPS", "JOBS", "LEARN", "EVENTS"
    val city: String = "Bengaluru"
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorId: Long,
    val authorName: String,
    val authorHeadline: String,
    val authorAvatar: String,
    val isAnonymous: Boolean = false,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val senderId: Long,
    val senderName: String,
    val senderAvatar: String,
    val recipientId: Long,
    val recipientName: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val type: String, // CONNECTION, COMMENT, REACTION, VERIFICATION, COMMUNITY
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedEntityId: Long = 0
)

@Entity(tableName = "verification_requests")
data class VerificationRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val companyName: String,
    val workEmail: String,
    val method: String, // CORPORATE_EMAIL, DOMAIN_CHECK, HR_ATTESTATION
    val status: VerificationStatus = VerificationStatus.PENDING,
    val reviewerNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reporterId: Long,
    val reporterName: String,
    val entityType: String, // POST, COMMENT, USER
    val entityId: Long,
    val entityTitle: String,
    val reason: String,
    val details: String,
    val status: String = "PENDING_REVIEW", // PENDING_REVIEW, RESOLVED_REMOVED, DISMISSED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val authorName: String,
    val authorHeadline: String,
    val authorCompany: String,
    val category: String,
    val readTimeMinutes: Int,
    val content: String,
    val likesCount: Int = 0,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorName: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_connections")
data class UserConnectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requesterId: Long,
    val requesterName: String,
    val requesterHeadline: String,
    val requesterAvatar: String,
    val receiverId: Long,
    val receiverName: String,
    val receiverHeadline: String,
    val receiverAvatar: String,
    val note: String = "",
    val status: ConnectionStatus = ConnectionStatus.ACCEPTED,
    val isMessageRequest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "skill_endorsements",
    indices = [
        Index(value = ["targetUserId", "skillName"]),
        Index(value = ["targetUserId", "endorserUserId", "skillName"], unique = true)
    ]
)
data class SkillEndorsementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetUserId: Long,
    val endorserUserId: Long,
    val endorserName: String,
    val endorserRole: String,
    val endorserPhotoUrl: String = "",
    val isEndorserVerified: Boolean = false,
    val skillName: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val company: String,
    val city: String,
    val scope: String = "INDIA", // "LOCAL", "INDIA", "GLOBAL"
    val experience: String, // e.g. "3-6 Years", "Staff (8+ Yrs)"
    val salaryRange: String, // e.g. "₹32L - ₹48L CTC", "$170k - $210k"
    val jobType: String = "Hybrid", // "Full-time", "Hybrid", "Remote"
    val department: String = "Engineering", // "Engineering", "Product", "Design", "Data & AI", "Finance"
    val description: String,
    val requirements: String = "Strong technical foundation, high ownership, and collaborative communication.",
    val referralAvailable: Boolean = true,
    val referrerName: String,
    val referrerRole: String,
    val referrerAvatar: String = "CX",
    val postedByUserId: Long = 1,
    val applicantsCount: Int = 18,
    val isApplied: Boolean = false,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val organizerName: String,
    val organizerRole: String,
    val organizerAvatar: String = "CX",
    val eventType: String = "MEETUP", // "MEETUP", "MIXER", "WEBINAR", "ROUNDTABLE", "HACKATHON"
    val city: String, // "Bengaluru", "Mumbai", "Delhi-NCR", "Hyderabad", "Virtual / Global"
    val scope: String = "LOCAL", // "LOCAL", "INDIA", "GLOBAL"
    val venue: String, // e.g. "WeWork Galaxy, Residency Rd, Bengaluru" or "Google Meet / Zoom"
    val dateText: String, // e.g. "Saturday, Oct 12"
    val timeText: String, // e.g. "5:00 PM - 8:00 PM IST"
    val description: String,
    val attendeesCount: Int = 42,
    val isRsvp: Boolean = false,
    val isSaved: Boolean = false,
    val category: String = "Tech & Product",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mentors")
data class MentorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val headline: String,
    val company: String,
    val city: String,
    val avatarInitials: String = "MN",
    val expertise: String, // "System Design, IC to Manager, Staff PM Roadmapping"
    val availableSlots: String = "Weekends • 45 min 1:1 sessions",
    val sessionsCompleted: Int = 24,
    val rating: Double = 4.9,
    val bio: String,
    val isAcceptingMentees: Boolean = true,
    val isBooked: Boolean = false
)

