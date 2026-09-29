package com.example.data.model

enum class UserRole(val displayName: String, val badgeColorHex: Long) {
    REGULAR_PROFESSIONAL("Professional", 0xFF0B5FFF),
    COMMUNITY_MODERATOR("Moderator", 0xFF8B5CF6),
    COMPANY_REPRESENTATIVE("Company Rep", 0xFF0D9488),
    PLATFORM_ADMIN("Platform Admin", 0xFFEF4444),
    GUEST("Guest", 0xFF718096)
}

enum class PostType(val label: String, val iconName: String) {
    DISCUSSION("Discussion", "Forum"),
    QUESTION("Ask Community", "Help"),
    WORKPLACE_STORY("Workplace Story", "AutoStories"),
    KNOWLEDGE_INSIGHT("Insight", "Lightbulb"),
    POLL("Poll", "Poll"),
    ARTICLE("Article", "Article"),
    EVENT("Event", "Event")
}

enum class VerificationStatus(val label: String) {
    NOT_STARTED("Not Verified"),
    PENDING("Verification Pending"),
    VERIFIED("Verified Professional"),
    REJECTED("Action Required")
}

enum class ConnectionStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

enum class ModerationStatus {
    PUBLISHED,
    FLAGGED,
    REMOVED
}

enum class ReportReason(val title: String, val description: String) {
    CONFIDENTIAL_INFO("Confidential Information", "Contains proprietary employer data, internal docs or client secrets"),
    HARASSMENT("Harassment or Hate", "Attacking, bullying, or defaming an individual or group"),
    SPAM_OR_PROMOTION("Spam or Solicitations", "Repetitive messages, affiliate links, or unauthorized sales"),
    MISINFORMATION("Misinformation", "Inaccurate claims about companies or individuals"),
    IMPERSONATION("Impersonation", "Pretending to represent another person or organization")
}

data class CurrentUserSession(
    val userId: Long,
    val fullName: String,
    val username: String,
    val email: String,
    val headline: String,
    val employer: String,
    val industry: String,
    val role: String,
    val location: String,
    val avatarUrl: String,
    val isVerified: Boolean,
    val roleType: UserRole,
    val employerVisibility: Boolean = true,
    val locationVisibility: Boolean = true
)
