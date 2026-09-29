package com.example

import com.example.data.local.PostEntity
import com.example.data.local.UserEntity
import com.example.data.model.PostType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatePostUnitTest {

    @Test
    fun `test creating public workplace knowledge post for local Room storage`() {
        val author = UserEntity(
            id = 55L,
            email = "sarah.connor@cloudcorp.io",
            fullName = "Sarah Connor",
            username = "sarah_c",
            headline = "Staff Site Reliability Engineer • Multi-Cloud",
            bio = "Specializing in distributed storage and high availability clusters.",
            employer = "CloudCorp",
            industry = "Cloud & Infrastructure",
            role = "Staff SRE",
            location = "Austin, TX",
            experienceLevel = "10+ Years",
            avatarInitials = "SC",
            isVerified = true
        )

        val newPost = PostEntity(
            authorId = author.id,
            authorName = author.fullName,
            authorHeadline = author.headline,
            authorAvatar = author.avatarInitials,
            isAnonymous = false,
            anonymousBadge = "",
            communityId = 1L,
            communityName = "Tech & AI Engineers",
            postType = PostType.KNOWLEDGE_INSIGHT,
            title = "Zero-Downtime Database Migration Patterns Across Kubernetes Clusters",
            body = "We migrated 4TB PostgreSQL clusters without dropped connections by utilizing dual-writing with CDC and pg_stat_statements. Here are the 3 hard-won lessons...",
            tags = "Database, Kubernetes, SRE, Distributed Systems",
            pollOptions = "",
            pollVotes = "",
            likesCount = 0,
            commentsCount = 0,
            isLiked = false,
            isSaved = false,
            createdAt = System.currentTimeMillis()
        )

        // Validate local Room post entity creation
        assertNotNull(newPost)
        assertEquals("Sarah Connor", newPost.authorName)
        assertEquals("Staff Site Reliability Engineer • Multi-Cloud", newPost.authorHeadline)
        assertEquals("Tech & AI Engineers", newPost.communityName)
        assertEquals("Zero-Downtime Database Migration Patterns Across Kubernetes Clusters", newPost.title)
        assertTrue(newPost.body.contains("4TB PostgreSQL clusters"))
        assertEquals(PostType.KNOWLEDGE_INSIGHT, newPost.postType)
        assertFalse(newPost.isAnonymous)
        assertEquals(0, newPost.likesCount)
        assertEquals(0, newPost.commentsCount)
    }

    @Test
    fun `test creating anonymous workplace knowledge post for local Room storage`() {
        val author = UserEntity(
            id = 77L,
            email = "lead.dev@enterprise.com",
            fullName = "Jordan Reed",
            username = "jordan_reed",
            headline = "Principal Engineer",
            bio = "Tech lead focusing on latency sensitive finance systems.",
            employer = "Major Fintech",
            industry = "Financial Services",
            role = "Principal Engineer",
            location = "New York, NY",
            experienceLevel = "12+ Years",
            avatarInitials = "JR",
            isVerified = true
        )

        // For anonymous post, author name and avatar are masked for community safety
        val anonPost = PostEntity(
            authorId = author.id,
            authorName = "Anonymous Peer",
            authorHeadline = "Verified Principal Engineer",
            authorAvatar = "AP",
            isAnonymous = true,
            anonymousBadge = "Principal Engineer @ Tier-1 Tech",
            communityId = 3L,
            communityName = "Compensation & Career Growth",
            postType = PostType.WORKPLACE_STORY,
            title = "How I negotiated an out-of-band level adjustment during executive transition",
            body = "During our company re-org, our team was shifted to a new VP. I structured a 6-month impact doc illustrating cross-team velocity and unblocked 3 strategic deals...",
            tags = "Negotiation, Promotion, Career Growth",
            pollOptions = "",
            pollVotes = ""
        )

        assertTrue(anonPost.isAnonymous)
        assertEquals("Anonymous Peer", anonPost.authorName)
        assertEquals("Principal Engineer @ Tier-1 Tech", anonPost.anonymousBadge)
        assertEquals("How I negotiated an out-of-band level adjustment during executive transition", anonPost.title)
        assertTrue(anonPost.body.contains("6-month impact doc"))
    }

    @Test
    fun `test creating poll workplace knowledge post with serialized options`() {
        val pollOptions = listOf("Under $150k", "$150k - $220k", "$220k - $300k", "$300k+")
        val optionsDelimited = pollOptions.joinToString("|")
        val initialVotes = pollOptions.map { "0" }.joinToString("|")

        val pollPost = PostEntity(
            authorId = 99L,
            authorName = "Community Moderator",
            authorHeadline = "Community Team",
            authorAvatar = "CM",
            isAnonymous = false,
            communityId = 4L,
            communityName = "Compensation & Career Growth",
            postType = PostType.POLL,
            title = "2025 Staff Engineer Base Salary Survey",
            body = "Please vote on your current base compensation tier for L6/Staff levels across US metros.",
            tags = "Salary, Benchmark, StaffEngineer",
            pollOptions = optionsDelimited,
            pollVotes = initialVotes,
            userVotedOptionIndex = -1
        )

        assertEquals(PostType.POLL, pollPost.postType)
        assertEquals("Under $150k|$150k - $220k|$220k - $300k|$300k+", pollPost.pollOptions)
        assertEquals("0|0|0|0", pollPost.pollVotes)
        assertEquals(-1, pollPost.userVotedOptionIndex)
        assertEquals(4, pollPost.pollOptions.split("|").size)
    }
}
