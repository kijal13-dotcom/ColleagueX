package com.example

import com.example.data.local.PostEntity
import com.example.data.local.SampleData
import com.example.data.model.ModerationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Community Feed keyword search and topic filtering functionality.
 */
class CommunityFeedSearchUnitTest {

    private lateinit var samplePosts: List<PostEntity>

    @Before
    fun setUp() {
        samplePosts = SampleData.getInitialPosts()
    }

    /**
     * Helper simulating Community Feed keyword search engine.
     */
    private fun searchCommunityPosts(
        posts: List<PostEntity>,
        circleFilter: Long? = null,
        topicFilter: String? = null,
        searchQuery: String = ""
    ): List<PostEntity> {
        var result = posts.sortedByDescending { it.createdAt }

        if (circleFilter != null) {
            result = result.filter { it.communityId == circleFilter }
        }

        if (!topicFilter.isNullOrBlank()) {
            val topicLower = topicFilter.trim().lowercase()
            result = result.filter { post ->
                post.tags.lowercase().contains(topicLower) ||
                post.title.lowercase().contains(topicLower) ||
                post.body.lowercase().contains(topicLower)
            }
        }

        if (searchQuery.isNotBlank()) {
            val keywords = searchQuery.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
            result = result.filter { post ->
                keywords.all { kw ->
                    post.title.lowercase().contains(kw) ||
                    post.body.lowercase().contains(kw) ||
                    post.authorName.lowercase().contains(kw) ||
                    post.authorHeadline.lowercase().contains(kw) ||
                    post.communityName.lowercase().contains(kw) ||
                    post.tags.lowercase().contains(kw) ||
                    post.anonymousBadge.lowercase().contains(kw)
                }
            }
        }

        return result
    }

    @Test
    fun `test search by keyword in post title`() {
        // Query matching title keyword "meetings"
        val results = searchCommunityPosts(samplePosts, searchQuery = "meetings")
        assertTrue("Expected search results for 'meetings'", results.isNotEmpty())
        assertTrue(results.any { it.title.contains("meetings", ignoreCase = true) })
    }

    @Test
    fun `test search by keyword in post body`() {
        // Query matching body keyword "boundaries"
        val results = searchCommunityPosts(samplePosts, searchQuery = "boundaries")
        assertTrue("Expected search results for 'boundaries'", results.isNotEmpty())
        assertTrue(results.all {
            it.title.contains("boundaries", ignoreCase = true) ||
            it.body.contains("boundaries", ignoreCase = true) ||
            it.tags.contains("boundaries", ignoreCase = true)
        })
    }

    @Test
    fun `test search by topic keyword chip`() {
        // Filter by topic "AI"
        val results = searchCommunityPosts(samplePosts, topicFilter = "AI")
        assertTrue("Expected results for topic 'AI'", results.isNotEmpty())
        assertTrue(results.all {
            it.tags.contains("AI", ignoreCase = true) ||
            it.title.contains("AI", ignoreCase = true) ||
            it.body.contains("AI", ignoreCase = true)
        })
    }

    @Test
    fun `test search with multi-word keywords`() {
        // Query "remote work"
        val results = searchCommunityPosts(samplePosts, searchQuery = "remote work")
        assertTrue("Expected results for multi-word query 'remote work'", results.isNotEmpty())
        results.forEach { post ->
            val fullText = "${post.title} ${post.body} ${post.tags} ${post.communityName}".lowercase()
            assertTrue("Expected post to match both 'remote' and 'work'", fullText.contains("remote") && fullText.contains("work"))
        }
    }

    @Test
    fun `test search is case insensitive`() {
        val upperResults = searchCommunityPosts(samplePosts, searchQuery = "LEADERSHIP")
        val lowerResults = searchCommunityPosts(samplePosts, searchQuery = "leadership")
        val mixedResults = searchCommunityPosts(samplePosts, searchQuery = "LeaderShip")

        assertEquals(upperResults.size, lowerResults.size)
        assertEquals(lowerResults.size, mixedResults.size)
        assertTrue("Expected leadership posts to be found", upperResults.isNotEmpty())
    }

    @Test
    fun `test search combined with circle filter`() {
        // Search "meetings" specifically in Circle 1 (Technology & AI)
        val techMeetingPosts = searchCommunityPosts(samplePosts, circleFilter = 1L, searchQuery = "meetings")
        assertTrue(techMeetingPosts.all { it.communityId == 1L })
        assertTrue(techMeetingPosts.all { it.title.contains("meetings", ignoreCase = true) || it.body.contains("meetings", ignoreCase = true) })
    }

    @Test
    fun `test search by author or anonymous badge`() {
        val results = searchCommunityPosts(samplePosts, searchQuery = "Elena")
        assertTrue("Expected results searching by author name 'Elena'", results.isNotEmpty())
        assertEquals("Elena Rostova", results.first().authorName)
    }

    @Test
    fun `test search with no matches returns empty list`() {
        val results = searchCommunityPosts(samplePosts, searchQuery = "xyznonexistentquery999")
        assertTrue("Expected empty result for nonexistent keyword", results.isEmpty())
    }

    @Test
    fun `test clearing search restores all community posts`() {
        val filtered = searchCommunityPosts(samplePosts, searchQuery = "salary")
        val cleared = searchCommunityPosts(samplePosts, searchQuery = "")
        assertTrue(filtered.size < cleared.size)
        assertEquals(samplePosts.size, cleared.size)
    }
}
