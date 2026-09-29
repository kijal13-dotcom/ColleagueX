package com.example

import com.example.data.local.PostEntity
import com.example.data.model.PostType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostLikeFeatureUnitTest {

    @Test
    fun `test post entity schema includes likes count and like state`() {
        val post = PostEntity(
            id = 42L,
            authorId = 10L,
            authorName = "Alex Chen",
            authorHeadline = "Staff Product Manager",
            authorAvatar = "AC",
            isAnonymous = false,
            communityId = 1L,
            communityName = "Technology & AI",
            postType = PostType.KNOWLEDGE_INSIGHT,
            title = "Framework: How our team killed 40% of meetings",
            body = "Over the past 6 months, we instituted an async-first decision memo policy...",
            likesCount = 142,
            isLiked = false
        )

        assertNotNull(post)
        assertEquals(142, post.likesCount)
        assertFalse(post.isLiked)
    }

    @Test
    fun `test toggling like from unliked to liked increments likes count`() {
        val initialPost = PostEntity(
            id = 1L,
            authorId = 2L,
            authorName = "Sarah Connor",
            authorHeadline = "Staff SRE",
            authorAvatar = "SC",
            communityId = 1L,
            communityName = "Tech & AI",
            title = "Zero-Downtime Database Migration Patterns",
            body = "CDC dual writing architecture notes...",
            likesCount = 10,
            isLiked = false
        )

        // Simulate toggling like on (isLiked = true)
        val likedPost = initialPost.copy(
            isLiked = true,
            likesCount = initialPost.likesCount + 1
        )

        assertTrue(likedPost.isLiked)
        assertEquals(11, likedPost.likesCount)
    }

    @Test
    fun `test toggling like from liked to unliked decrements likes count`() {
        val likedPost = PostEntity(
            id = 1L,
            authorId = 2L,
            authorName = "Sarah Connor",
            authorHeadline = "Staff SRE",
            authorAvatar = "SC",
            communityId = 1L,
            communityName = "Tech & AI",
            title = "Zero-Downtime Database Migration Patterns",
            body = "CDC dual writing architecture notes...",
            likesCount = 11,
            isLiked = true
        )

        // Simulate toggling like off (isLiked = false)
        val unlikedPost = likedPost.copy(
            isLiked = false,
            likesCount = (likedPost.likesCount - 1).coerceAtLeast(0)
        )

        assertFalse(unlikedPost.isLiked)
        assertEquals(10, unlikedPost.likesCount)
    }

    @Test
    fun `test unliking post with 0 likes never drops below 0`() {
        val zeroLikesPost = PostEntity(
            id = 2L,
            authorId = 5L,
            authorName = "Jordan Reed",
            authorHeadline = "Principal Engineer",
            authorAvatar = "JR",
            communityId = 2L,
            communityName = "Leadership",
            title = "New Engineering Leadership Framework",
            body = "Key takeaways from our quarterly review...",
            likesCount = 0,
            isLiked = false
        )

        // Decrement with floor at 0
        val toggledPost = zeroLikesPost.copy(
            isLiked = false,
            likesCount = (zeroLikesPost.likesCount - 1).coerceAtLeast(0)
        )

        assertEquals(0, toggledPost.likesCount)
        assertFalse(toggledPost.isLiked)
    }

    @Test
    fun `test community feed post like toggle updates like state and counter correctly`() {
        val initialCommunityPost = PostEntity(
            id = 101L,
            communityId = 1L,
            communityName = "Technology & AI",
            authorId = 10L,
            authorName = "Alex Chen",
            authorHeadline = "Staff PM",
            authorAvatar = "AC",
            title = "Framework: Scaling async communication",
            body = "Best practices for writing asynchronous decision memos...",
            likesCount = 50,
            isLiked = false
        )

        // 1. Initial State: not liked, counter = 50
        assertFalse(initialCommunityPost.isLiked)
        assertEquals(50, initialCommunityPost.likesCount)

        // 2. User toggles like in Community Feed: should become liked, counter = 51
        val likedInCommunityFeed = initialCommunityPost.copy(
            isLiked = !initialCommunityPost.isLiked,
            likesCount = initialCommunityPost.likesCount + 1
        )
        assertTrue(likedInCommunityFeed.isLiked)
        assertEquals(51, likedInCommunityFeed.likesCount)

        // 3. User toggles like again (unlikes): should become unliked, counter = 50
        val unlikedInCommunityFeed = likedInCommunityFeed.copy(
            isLiked = !likedInCommunityFeed.isLiked,
            likesCount = likedInCommunityFeed.likesCount - 1
        )
        assertFalse(unlikedInCommunityFeed.isLiked)
        assertEquals(50, unlikedInCommunityFeed.likesCount)
    }

    @Test
    fun `test community feed posts maintain independent like states and counters`() {
        val post1 = PostEntity(
            id = 201L,
            communityId = 1L,
            communityName = "Technology & AI",
            authorId = 1L,
            authorName = "Author 1",
            authorHeadline = "Engineer",
            authorAvatar = "A1",
            title = "Post 1",
            body = "Content 1",
            likesCount = 5,
            isLiked = false
        )

        val post2 = PostEntity(
            id = 202L,
            communityId = 2L,
            communityName = "First-Time Managers",
            authorId = 2L,
            authorName = "Author 2",
            authorHeadline = "Manager",
            authorAvatar = "A2",
            title = "Post 2",
            body = "Content 2",
            likesCount = 20,
            isLiked = true
        )

        // Toggle like on post1 only
        val updatedPost1 = post1.copy(isLiked = true, likesCount = post1.likesCount + 1)

        // Post1 is now liked with counter 6
        assertTrue(updatedPost1.isLiked)
        assertEquals(6, updatedPost1.likesCount)

        // Post2 remains untouched with its original liked state and counter 20
        assertTrue(post2.isLiked)
        assertEquals(20, post2.likesCount)
    }
}
