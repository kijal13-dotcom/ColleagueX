package com.example

import com.example.data.local.PostEntity
import com.example.data.local.SampleData
import com.example.data.model.PostType
import com.example.ui.components.formatRelativeTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedComponentUnitTest {

    @Test
    fun `test sample workplace knowledge posts have author name title and body`() {
        val samplePosts = SampleData.getInitialPosts()
        assertTrue("Expected non-empty list of initial posts", samplePosts.isNotEmpty())

        samplePosts.forEach { post ->
            // Verify author name is present
            assertNotNull("Author name should not be null", post.authorName)
            assertTrue("Author name should not be empty for post ${post.id}", post.authorName.isNotBlank())

            // Verify title is present
            assertNotNull("Post title should not be null", post.title)
            assertTrue("Post title should not be empty for post ${post.id}", post.title.isNotBlank())

            // Verify body content is present
            assertNotNull("Post body content should not be null", post.body)
            assertTrue("Post body content should not be empty for post ${post.id}", post.body.isNotBlank())
        }
    }

    @Test
    fun `test workplace knowledge post fields and content integrity`() {
        val knowledgePost = PostEntity(
            id = 201L,
            communityId = 1L,
            communityName = "Tech & AI Engineers",
            authorId = 101L,
            authorName = "Elena Rostova",
            authorHeadline = "Principal AI Researcher • Anthropic",
            authorAvatar = "ER",
            title = "Architectural tradeoffs of speculative decoding in production",
            body = "Over the last 6 months running 70B parameter models at scale, we evaluated draft models vs early exit layers. Key findings: 1. Speedup reaches 2.4x for code generation. 2. Memory bandwidth was the primary bottleneck...",
            postType = PostType.KNOWLEDGE_INSIGHT,
            tags = "LLM, System Architecture, Inference Optimization",
            likesCount = 245,
            commentsCount = 38,
            isAnonymous = false,
            isLiked = true,
            isSaved = true
        )

        // Validate author name
        assertEquals("Elena Rostova", knowledgePost.authorName)
        assertEquals("Principal AI Researcher • Anthropic", knowledgePost.authorHeadline)
        assertFalse(knowledgePost.isAnonymous)

        // Validate title
        assertEquals("Architectural tradeoffs of speculative decoding in production", knowledgePost.title)

        // Validate body content
        assertTrue(knowledgePost.body.startsWith("Over the last 6 months"))
        assertTrue(knowledgePost.body.contains("Speedup reaches 2.4x"))

        // Validate knowledge classification and community
        assertEquals(PostType.KNOWLEDGE_INSIGHT, knowledgePost.postType)
        assertEquals("Tech & AI Engineers", knowledgePost.communityName)
        assertEquals(245, knowledgePost.likesCount)
        assertEquals(38, knowledgePost.commentsCount)
    }

    @Test
    fun `test anonymous workplace knowledge post displays masked or anonymous author attribution`() {
        val anonPost = PostEntity(
            id = 202L,
            communityId = 2L,
            communityName = "Compensation & Career Growth",
            authorId = 102L,
            authorName = "Anonymous Peer",
            authorHeadline = "Staff Engineer at Tier-1 Tech",
            authorAvatar = "AP",
            title = "2025 Staff Engineer Refresh & Retention Package Breakdown",
            body = "Sharing transparent breakdown for L6 Staff level: Base: $250k, Annual Target Bonus: 25%, RSUs: $400k/year with quarterly vesting. Hope this helps anyone preparing for upcoming negotiations.",
            postType = PostType.WORKPLACE_STORY,
            tags = "Compensation, Staff Engineer, Negotiation",
            isAnonymous = true,
            anonymousBadge = "Tier-1 Cloud Provider Employee"
        )

        assertTrue(anonPost.isAnonymous)
        assertEquals("Anonymous Peer", anonPost.authorName)
        assertEquals("Tier-1 Cloud Provider Employee", anonPost.anonymousBadge)
        assertEquals("2025 Staff Engineer Refresh & Retention Package Breakdown", anonPost.title)
        assertTrue(anonPost.body.contains("Sharing transparent breakdown"))
    }

    @Test
    fun `test post tagging system categorizes workplace knowledge`() {
        val post = PostEntity(
            id = 301L,
            communityId = 1L,
            communityName = "Engineering Leadership",
            authorId = 101L,
            authorName = "Alex Chen",
            authorHeadline = "Staff PM",
            authorAvatar = "AC",
            title = "Mentorship & 1-on-1 Best Practices",
            body = "Key takeaways for 1-on-1s that build trust and drive career acceleration.",
            postType = PostType.KNOWLEDGE_INSIGHT,
            tags = "Career Advice, Leadership, Management"
        )

        val tagList = post.tags.split(",").map { it.trim() }
        assertTrue(tagList.contains("Career Advice"))
        assertTrue(tagList.contains("Leadership"))
        assertTrue(tagList.contains("Management"))
        assertEquals(3, tagList.size)
    }

    @Test
    fun `test filtering posts by workplace knowledge tags`() {
        val posts = listOf(
            PostEntity(
                id = 1L,
                communityId = 1L,
                communityName = "Tech",
                authorId = 1L,
                authorName = "Author 1",
                authorHeadline = "",
                authorAvatar = "A1",
                title = "Async Decision Memos",
                body = "How to run async meetings",
                tags = "Tech Tips, Productivity"
            ),
            PostEntity(
                id = 2L,
                communityId = 2L,
                communityName = "Career",
                authorId = 2L,
                authorName = "Author 2",
                authorHeadline = "",
                authorAvatar = "A2",
                title = "Negotiating Boundaries",
                body = "Setting healthy boundaries with leadership",
                tags = "Career Advice, Leadership"
            ),
            PostEntity(
                id = 3L,
                communityId = 3L,
                communityName = "Management",
                authorId = 3L,
                authorName = "Author 3",
                authorHeadline = "",
                authorAvatar = "A3",
                title = "From IC to Lead",
                body = "Overcoming the delegation barrier",
                tags = "Leadership, Career Advice"
            )
        )

        // Filter by 'Career Advice'
        val careerAdviceFilter = "Career Advice"
        val careerAdvicePosts = posts.filter { post ->
            post.tags.split(",").any { it.trim().equals(careerAdviceFilter, ignoreCase = true) }
        }
        assertEquals(2, careerAdvicePosts.size)
        assertTrue(careerAdvicePosts.all { it.id == 2L || it.id == 3L })

        // Filter by 'Tech Tips'
        val techTipsFilter = "Tech Tips"
        val techTipsPosts = posts.filter { post ->
            post.tags.split(",").any { it.trim().equals(techTipsFilter, ignoreCase = true) }
        }
        assertEquals(1, techTipsPosts.size)
        assertEquals(1L, techTipsPosts.first().id)

        // Filter by 'Leadership'
        val leadershipFilter = "Leadership"
        val leadershipPosts = posts.filter { post ->
            post.tags.split(",").any { it.trim().equals(leadershipFilter, ignoreCase = true) }
        }
        assertEquals(2, leadershipPosts.size)
    }

    @Test
    fun `test community feed displays posts from other users and community members with text content and timestamps`() {
        val samplePosts = SampleData.getInitialPosts()
        assertTrue(samplePosts.size >= 5)

        // Verify distinct community members and other users
        val authors = samplePosts.map { it.authorName }.distinct()
        assertTrue("Expected multiple distinct community members", authors.size >= 4)
        assertTrue(authors.contains("Alex Chen"))
        assertTrue(authors.contains("Elena Rostova"))
        assertTrue(authors.contains("Marcus Vance"))
        assertTrue(authors.contains("Priya Sharma"))

        // Verify basic text-based content and timestamps
        samplePosts.forEach { post ->
            // Title and body exist and are readable text
            assertTrue("Post title must be non-empty", post.title.isNotBlank())
            assertTrue("Post text content must be non-empty", post.body.isNotBlank())
            assertTrue("Post body must be descriptive text", post.body.length >= 20)

            // Timestamp verification
            assertTrue("Post timestamp must be positive", post.createdAt > 0)
            assertTrue("Post timestamp must be in the past or now", post.createdAt <= System.currentTimeMillis())

            // Formatted relative timestamp
            val relativeTime = formatRelativeTime(post.createdAt)
            assertTrue("Relative timestamp should be formatted string", relativeTime.isNotBlank())

            // Community mapping
            assertTrue("Post must be associated with a valid community", post.communityId > 0)
            assertTrue("Post must have a community name", post.communityName.isNotBlank())
        }
    }

    @Test
    fun `test formatRelativeTime outputs human-friendly relative time strings`() {
        val now = System.currentTimeMillis()

        val justNow = formatRelativeTime(now - 15 * 1000)
        assertEquals("Just now", justNow)

        val fiveMinutesAgo = formatRelativeTime(now - 5 * 60 * 1000)
        assertEquals("5m ago", fiveMinutesAgo)

        val twoHoursAgo = formatRelativeTime(now - 2 * 3600 * 1000)
        assertEquals("2h ago", twoHoursAgo)

        val threeDaysAgo = formatRelativeTime(now - 3 * 24 * 3600 * 1000)
        assertEquals("3d ago", threeDaysAgo)
    }

    @Test
    fun `test community feed filtering by circle and search keyword`() {
        val samplePosts = SampleData.getInitialPosts()

        // Filter by community 1 (Technology & AI)
        val techPosts = samplePosts.filter { it.communityId == 1L }
        assertTrue("Expected posts in Technology & AI community", techPosts.isNotEmpty())
        assertTrue(techPosts.all { it.communityName == "Technology & AI" })

        // Filter by community 2 (First-Time Managers)
        val managerPosts = samplePosts.filter { it.communityId == 2L }
        assertTrue("Expected posts in First-Time Managers community", managerPosts.isNotEmpty())
        assertTrue(managerPosts.all { it.communityName == "First-Time Managers" })

        // Search within community posts by keyword
        val query = "meetings"
        val meetingPosts = samplePosts.filter {
            it.title.contains(query, ignoreCase = true) || it.body.contains(query, ignoreCase = true)
        }
        assertTrue("Expected search matches for 'meetings'", meetingPosts.isNotEmpty())
        assertEquals(1L, meetingPosts.first().id)
    }
}
