package com.example

import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.data.model.PostType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommentFeatureUnitTest {

    @Test
    fun `test creating public comment on workplace knowledge post`() {
        val postId = 101L
        val publicComment = CommentEntity(
            id = 1L,
            postId = postId,
            authorId = 42L,
            authorName = "Elena Rostova",
            authorHeadline = "Head of People & Organizational Design",
            authorAvatar = "ER",
            isAnonymous = false,
            body = "We implemented something similar! The hardest part was getting directors to stop scheduling syncs out of habit.",
            likesCount = 5,
            isLiked = true
        )

        assertNotNull(publicComment)
        assertEquals(postId, publicComment.postId)
        assertEquals("Elena Rostova", publicComment.authorName)
        assertEquals("Head of People & Organizational Design", publicComment.authorHeadline)
        assertEquals("ER", publicComment.authorAvatar)
        assertFalse(publicComment.isAnonymous)
        assertTrue(publicComment.isLiked)
        assertEquals(5, publicComment.likesCount)
        assertTrue(publicComment.body.contains("scheduling syncs out of habit"))
    }

    @Test
    fun `test creating anonymous comment on workplace knowledge post`() {
        val postId = 101L
        val anonComment = CommentEntity(
            id = 2L,
            postId = postId,
            authorId = 0L,
            authorName = "Anonymous Peer",
            authorHeadline = "Verified Staff Engineer",
            authorAvatar = "AP",
            isAnonymous = true,
            body = "In our org, we had strict non-disclosure around compensation bands, but this benchmark aligns with our internal L6 range.",
            likesCount = 12,
            isLiked = false
        )

        assertTrue(anonComment.isAnonymous)
        assertEquals(0L, anonComment.authorId)
        assertEquals("Anonymous Peer", anonComment.authorName)
        assertEquals("Verified Staff Engineer", anonComment.authorHeadline)
        assertEquals("AP", anonComment.authorAvatar)
        assertEquals(12, anonComment.likesCount)
        assertFalse(anonComment.isLiked)
    }

    @Test
    fun `test post comments count updates when discussion grows`() {
        val initialPost = PostEntity(
            id = 101L,
            authorId = 1L,
            authorName = "Alex Chen",
            authorHeadline = "Staff Product Manager • AI Platform",
            authorAvatar = "AC",
            isAnonymous = false,
            communityId = 1L,
            communityName = "Tech & AI Engineers",
            postType = PostType.KNOWLEDGE_INSIGHT,
            title = "Zero-Downtime Database Migration Patterns Across Kubernetes Clusters",
            body = "We migrated 4TB PostgreSQL clusters without dropped connections...",
            commentsCount = 0
        )

        assertEquals(0, initialPost.commentsCount)

        // Simulate increment comments count
        val postAfterComment1 = initialPost.copy(commentsCount = initialPost.commentsCount + 1)
        assertEquals(1, postAfterComment1.commentsCount)

        val postAfterComment2 = postAfterComment1.copy(commentsCount = postAfterComment1.commentsCount + 1)
        assertEquals(2, postAfterComment2.commentsCount)
    }

    @Test
    fun `test comment like toggling`() {
        val comment = CommentEntity(
            id = 5L,
            postId = 101L,
            authorId = 10L,
            authorName = "Jordan Reed",
            authorHeadline = "Principal Engineer",
            authorAvatar = "JR",
            body = "Great point about CDC pipelines.",
            likesCount = 3,
            isLiked = false
        )

        // Toggle like to true
        val likedComment = comment.copy(
            isLiked = true,
            likesCount = comment.likesCount + 1
        )
        assertTrue(likedComment.isLiked)
        assertEquals(4, likedComment.likesCount)

        // Toggle like to false
        val unlikedComment = likedComment.copy(
            isLiked = false,
            likesCount = likedComment.likesCount - 1
        )
        assertFalse(unlikedComment.isLiked)
        assertEquals(3, unlikedComment.likesCount)
    }

    @Test
    fun `test replying to a community post in the feed`() {
        val communityPost = PostEntity(
            id = 42L,
            authorId = 3L,
            authorName = "Marcus Vance",
            authorHeadline = "VP of Engineering • Distributed Systems",
            authorAvatar = "MV",
            communityId = 2L,
            communityName = "First-Time Managers",
            title = "What 1-on-1 question unlocks the most honest feedback from your direct reports?",
            body = "What high-signal questions do other engineering managers ask in 1-on-1s?",
            commentsCount = 2
        )

        // User submits a reply in the feed
        val replyText = "I ask: 'If you were in my shoes, what is the single biggest priority you would focus on next week?' It surfaces operational bottlenecks fast."
        val reply = CommentEntity(
            id = 501L,
            postId = communityPost.id,
            authorId = 1L,
            authorName = "Alex Chen",
            authorHeadline = "Staff Product Manager • AI Platform",
            authorAvatar = "AC",
            isAnonymous = false,
            body = replyText,
            createdAt = System.currentTimeMillis()
        )

        assertEquals(communityPost.id, reply.postId)
        assertEquals("Alex Chen", reply.authorName)
        assertFalse(reply.isAnonymous)
        assertEquals(replyText, reply.body)
        assertTrue(reply.createdAt > 0)

        // Post comments count is incremented
        val updatedPost = communityPost.copy(commentsCount = communityPost.commentsCount + 1)
        assertEquals(3, updatedPost.commentsCount)
    }

    @Test
    fun `test anonymous reply to community post in the feed`() {
        val communityPostId = 42L
        val anonReply = CommentEntity(
            id = 502L,
            postId = communityPostId,
            authorId = 0L,
            authorName = "Anonymous Professional",
            authorHeadline = "Verified Professional",
            authorAvatar = "AP",
            isAnonymous = true,
            body = "From my experience at a large fintech, asking about team morale without judgment creates instant psychological safety.",
            createdAt = System.currentTimeMillis()
        )

        assertEquals(communityPostId, anonReply.postId)
        assertTrue(anonReply.isAnonymous)
        assertEquals("Anonymous Professional", anonReply.authorName)
        assertEquals("AP", anonReply.authorAvatar)
        assertTrue(anonReply.body.isNotBlank())
    }
}
