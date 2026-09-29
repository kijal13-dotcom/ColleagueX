package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.PostEntity
import com.example.data.local.UserEntity
import com.example.data.model.ModerationStatus
import com.example.data.model.PostType
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Data representation of a user profile document stored in Firebase Firestore.
 */
data class FirestoreUserProfile(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val username: String = "",
    val headline: String = "",
    val bio: String = "",
    val employer: String = "",
    val industry: String = "",
    val role: String = "",
    val location: String = "",
    val avatarUrl: String = "",
    val avatarInitials: String = "",
    val isVerified: Boolean = false,
    val verificationStatus: String = "NOT_STARTED",
    val verifiedBadgeText: String = "",
    val roleType: String = "REGULAR_PROFESSIONAL",
    val employerVisibility: Boolean = true,
    val locationVisibility: Boolean = true,
    val connectionsCount: Int = 0,
    val contributionsCount: Int = 0,
    val skills: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Data representation of a comment item attached to a Firestore community post.
 */
data class FirestoreCommentItem(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorHeadline: String = "",
    val authorAvatar: String = "",
    val isAnonymous: Boolean = false,
    val body: String = "",
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Data representation of a community post document stored in Firebase Firestore.
 */
data class FirestorePost(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorHeadline: String = "",
    val authorAvatar: String = "",
    val isAnonymous: Boolean = false,
    val anonymousBadge: String = "",
    val communityId: Long = 0L,
    val communityName: String = "",
    val postType: String = "DISCUSSION",
    val title: String = "",
    val body: String = "",
    val tags: String = "",
    val pollOptions: String = "",
    val pollVotes: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val likedByUsers: List<String> = emptyList(),
    val comments: List<FirestoreCommentItem> = emptyList(),
    val pillarScope: String = "INDIA",
    val city: String = "Bengaluru",
    val moderationStatus: String = "PUBLISHED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Repository to manage cloud document storage and real-time synchronization with Firebase Firestore
 * for user profiles and community posts in ColleagueX.
 */
class FirestoreRepository(
    private val context: Context? = null,
    customFirestore: FirebaseFirestore? = null
) {
    private val tag = "FirestoreRepository"

    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_POSTS = "posts"
    }

    private val db: FirebaseFirestore by lazy {
        customFirestore ?: run {
            context?.let { ctx ->
                if (FirebaseApp.getApps(ctx).isEmpty()) {
                    try {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:201232242199:android:colleaguex")
                            .setApiKey("AIzaSyColleagueXFallbackKeyForAppInit12345")
                            .setProjectId("colleaguex-community")
                            .build()
                        FirebaseApp.initializeApp(ctx, options)
                        Log.d(tag, "Initialized default FirebaseApp for Firestore")
                    } catch (e: Exception) {
                        Log.w(tag, "FirebaseApp initialization: ${e.message}")
                    }
                }
            }
            FirebaseFirestore.getInstance()
        }
    }

    // ==========================================
    // USER PROFILE DOCUMENT STORAGE
    // ==========================================

    /**
     * Saves or overwrites a user profile document in the "users" collection.
     */
    suspend fun saveUserProfile(profile: FirestoreUserProfile): Result<Unit> = runCatching {
        val documentId = profile.uid.ifBlank { profile.email.ifBlank { "user_${System.currentTimeMillis()}" } }
        val docRef = db.collection(COLLECTION_USERS).document(documentId)
        val profileData = profile.copy(uid = documentId, updatedAt = System.currentTimeMillis())
        docRef.set(profileData, SetOptions.merge()).awaitTask()
        Log.d(tag, "Successfully saved user profile to Firestore: $documentId")
    }

    /**
     * Retrieves a user profile document by unique user ID or email.
     */
    suspend fun getUserProfile(userId: String): Result<FirestoreUserProfile?> = runCatching {
        if (userId.isBlank()) return@runCatching null
        val doc = db.collection(COLLECTION_USERS).document(userId).get().awaitTask()
        if (doc.exists()) {
            doc.toObject(FirestoreUserProfile::class.java)
        } else {
            null
        }
    }

    /**
     * Real-time Flow observing a specific user profile document in Firestore.
     */
    fun observeUserProfile(userId: String): Flow<FirestoreUserProfile?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = db.collection(COLLECTION_USERS).document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Error listening to user profile $userId: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(FirestoreUserProfile::class.java)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Updates specific fields in a user profile document.
     */
    suspend fun updateUserProfileFields(userId: String, updates: Map<String, Any?>): Result<Unit> = runCatching {
        val fieldsWithTimestamp = updates.toMutableMap().apply {
            put("updatedAt", System.currentTimeMillis())
        }
        db.collection(COLLECTION_USERS).document(userId).update(fieldsWithTimestamp).awaitTask()
        Log.d(tag, "Updated user profile fields in Firestore for user: $userId")
    }

    /**
     * Convenience method to sync a local Room UserEntity to Firestore document storage.
     */
    suspend fun saveUserProfileFromEntity(user: UserEntity): Result<Unit> {
        val firestoreProfile = user.toFirestoreUserProfile()
        return saveUserProfile(firestoreProfile)
    }

    // ==========================================
    // COMMUNITY POSTS DOCUMENT STORAGE
    // ==========================================

    /**
     * Creates a new community post document in the "posts" collection.
     * Returns the generated or assigned document ID.
     */
    suspend fun createPost(post: FirestorePost): Result<String> = runCatching {
        val collection = db.collection(COLLECTION_POSTS)
        val docRef = if (post.id.isNotBlank()) collection.document(post.id) else collection.document()
        val postToSave = post.copy(
            id = docRef.id,
            createdAt = if (post.createdAt > 0) post.createdAt else System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        docRef.set(postToSave).awaitTask()
        Log.d(tag, "Created post document in Firestore: ${docRef.id} in community ${post.communityName}")
        docRef.id
    }

    /**
     * Retrieves a single post document by ID.
     */
    suspend fun getPost(postId: String): Result<FirestorePost?> = runCatching {
        if (postId.isBlank()) return@runCatching null
        val doc = db.collection(COLLECTION_POSTS).document(postId).get().awaitTask()
        if (doc.exists()) {
            doc.toObject(FirestorePost::class.java)
        } else {
            null
        }
    }

    /**
     * Real-time Flow observing community posts from Firestore, optionally filtered
     * by community ID or pillar scope.
     */
    fun observeCommunityPosts(
        communityId: Long? = null,
        pillarScope: String? = null,
        limit: Long = 50
    ): Flow<List<FirestorePost>> = callbackFlow {
        var query: Query = db.collection(COLLECTION_POSTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        if (communityId != null && communityId > 0) {
            query = query.whereEqualTo("communityId", communityId)
        }
        if (!pillarScope.isNullOrBlank()) {
            query = query.whereEqualTo("pillarScope", pillarScope)
        }
        query = query.limit(limit)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(tag, "Error listening to posts: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestorePost::class.java)
                }
                trySend(posts)
            } else {
                trySend(emptyList())
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time Flow observing posts created by a specific user from Firestore.
     * Matches by authorId, authorFirebaseUid, or authorName.
     */
    fun observePostsByAuthor(
        authorId: String,
        authorFirebaseUid: String = "",
        authorName: String = "",
        limit: Long = 100
    ): Flow<List<FirestorePost>> = callbackFlow {
        val listener = db.collection(COLLECTION_POSTS)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Error listening to author posts $authorId: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FirestorePost::class.java)
                    }.filter { post ->
                        (authorId.isNotBlank() && post.authorId == authorId) ||
                        (authorFirebaseUid.isNotBlank() && post.authorId == authorFirebaseUid) ||
                        (authorName.isNotBlank() && !post.isAnonymous && post.authorName.equals(authorName, ignoreCase = true))
                    }.sortedByDescending { it.createdAt }
                    trySend(posts)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Updates specific fields of an existing post document.
     */
    suspend fun updatePost(postId: String, updates: Map<String, Any?>): Result<Unit> = runCatching {
        val fieldsWithTimestamp = updates.toMutableMap().apply {
            put("updatedAt", System.currentTimeMillis())
        }
        db.collection(COLLECTION_POSTS).document(postId).update(fieldsWithTimestamp).awaitTask()
        Log.d(tag, "Updated post document $postId in Firestore")
    }

    /**
     * Deletes a community post document from Firestore.
     */
    suspend fun deletePost(postId: String): Result<Unit> = runCatching {
        db.collection(COLLECTION_POSTS).document(postId).delete().awaitTask()
        Log.d(tag, "Deleted post document $postId from Firestore")
    }

    /**
     * Updates the like counter and tracks the user interaction in likedByUsers list.
     */
    suspend fun togglePostLike(postId: String, userId: String, isLiked: Boolean): Result<Unit> = runCatching {
        val incrementValue = if (isLiked) 1L else -1L
        val updateMap = if (isLiked) {
            mapOf(
                "likesCount" to FieldValue.increment(incrementValue),
                "likedByUsers" to FieldValue.arrayUnion(userId),
                "updatedAt" to System.currentTimeMillis()
            )
        } else {
            mapOf(
                "likesCount" to FieldValue.increment(incrementValue),
                "likedByUsers" to FieldValue.arrayRemove(userId),
                "updatedAt" to System.currentTimeMillis()
            )
        }
        db.collection(COLLECTION_POSTS).document(postId).update(updateMap).awaitTask()
        Log.d(tag, "Toggled like for user $userId on post $postId: isLiked=$isLiked")
    }

    /**
     * Updates the like counter on a community post.
     */
    suspend fun togglePostLike(postId: String, isLiked: Boolean): Result<Unit> = runCatching {
        val incrementValue = if (isLiked) 1L else -1L
        db.collection(COLLECTION_POSTS).document(postId).update(
            mapOf(
                "likesCount" to FieldValue.increment(incrementValue),
                "updatedAt" to System.currentTimeMillis()
            )
        ).awaitTask()
    }

    /**
     * Adds a comment to a post document, updating commentsCount and tracking the comment interaction.
     */
    suspend fun addCommentToPost(postId: String, comment: FirestoreCommentItem): Result<Unit> = runCatching {
        db.collection(COLLECTION_POSTS).document(postId).update(
            mapOf(
                "commentsCount" to FieldValue.increment(1L),
                "comments" to FieldValue.arrayUnion(comment),
                "updatedAt" to System.currentTimeMillis()
            )
        ).awaitTask()
        Log.d(tag, "Added comment to post document $postId in Firestore")
    }

    /**
     * Increments the comment counter on a post document.
     */
    suspend fun incrementPostComments(postId: String): Result<Unit> = runCatching {
        db.collection(COLLECTION_POSTS).document(postId).update(
            mapOf(
                "commentsCount" to FieldValue.increment(1L),
                "updatedAt" to System.currentTimeMillis()
            )
        ).awaitTask()
    }

    /**
     * Convenience method to sync a local Room PostEntity to Firestore document storage.
     */
    suspend fun createPostFromEntity(post: PostEntity): Result<String> {
        val firestorePost = post.toFirestorePost()
        return createPost(firestorePost)
    }

    // ==========================================
    // ASYNC TASK COROUTINE AWAIT HELPER
    // ==========================================

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) {
                continuation.resumeWithException(exception)
            }
        }
        addOnCanceledListener {
            if (continuation.isActive) {
                continuation.cancel()
            }
        }
    }
}

// ==========================================
// CONVERSION EXTENSION FUNCTIONS
// ==========================================

/**
 * Converts a Room [UserEntity] into a [FirestoreUserProfile].
 */
fun UserEntity.toFirestoreUserProfile(): FirestoreUserProfile {
    val uidKey = if (firebaseUid.isNotBlank()) firebaseUid else if (email.isNotBlank()) email else "user_$id"
    return FirestoreUserProfile(
        uid = uidKey,
        email = email,
        fullName = fullName,
        username = username,
        headline = headline,
        bio = bio,
        employer = employer,
        industry = industry,
        role = role,
        location = location,
        avatarUrl = photoUrl,
        avatarInitials = avatarInitials,
        isVerified = isVerified,
        verificationStatus = verificationStatus.name,
        verifiedBadgeText = verifiedBadgeText,
        roleType = roleType.name,
        employerVisibility = employerVisibility,
        locationVisibility = locationVisibility,
        connectionsCount = connectionsCount,
        contributionsCount = contributionsCount,
        skills = skills,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Converts a [FirestoreUserProfile] back into a Room [UserEntity].
 */
fun FirestoreUserProfile.toUserEntity(localId: Long = 0): UserEntity {
    val parsedRole = try {
        UserRole.valueOf(roleType)
    } catch (_: Exception) {
        UserRole.REGULAR_PROFESSIONAL
    }

    val parsedVerification = try {
        VerificationStatus.valueOf(verificationStatus)
    } catch (_: Exception) {
        VerificationStatus.NOT_STARTED
    }

    return UserEntity(
        id = localId,
        email = email,
        fullName = fullName,
        username = username.ifBlank { email.substringBefore("@") },
        headline = headline,
        bio = bio,
        employer = employer,
        industry = industry,
        role = role,
        location = location,
        experienceLevel = "Experienced",
        avatarInitials = avatarInitials.ifBlank {
            fullName.split(" ").filter { it.isNotBlank() }.map { it.first() }.take(2).joinToString("").uppercase()
        },
        isVerified = isVerified,
        verificationStatus = parsedVerification,
        verifiedBadgeText = verifiedBadgeText,
        roleType = parsedRole,
        employerVisibility = employerVisibility,
        locationVisibility = locationVisibility,
        connectionsCount = connectionsCount,
        contributionsCount = contributionsCount,
        skills = skills,
        firebaseUid = uid,
        authProvider = "firestore",
        photoUrl = avatarUrl
    )
}

/**
 * Converts a Room [PostEntity] into a [FirestorePost].
 */
fun PostEntity.toFirestorePost(): FirestorePost {
    return FirestorePost(
        id = id.takeIf { it > 0 }?.toString() ?: "",
        authorId = authorId.toString(),
        authorName = authorName,
        authorHeadline = authorHeadline,
        authorAvatar = authorAvatar,
        isAnonymous = isAnonymous,
        anonymousBadge = anonymousBadge,
        communityId = communityId,
        communityName = communityName,
        postType = postType.name,
        title = title,
        body = body,
        tags = tags,
        pollOptions = pollOptions,
        pollVotes = pollVotes,
        likesCount = likesCount,
        commentsCount = commentsCount,
        pillarScope = pillarScope,
        city = city,
        moderationStatus = moderationStatus.name,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Converts a [FirestorePost] back into a Room [PostEntity].
 */
fun FirestorePost.toPostEntity(localId: Long = 0): PostEntity {
    val parsedType = try {
        PostType.valueOf(postType)
    } catch (_: Exception) {
        PostType.DISCUSSION
    }

    val parsedModeration = try {
        ModerationStatus.valueOf(moderationStatus)
    } catch (_: Exception) {
        ModerationStatus.PUBLISHED
    }

    return PostEntity(
        id = localId.takeIf { it > 0 } ?: (id.toLongOrNull() ?: 0L),
        authorId = authorId.toLongOrNull() ?: 0L,
        authorName = authorName,
        authorHeadline = authorHeadline,
        authorAvatar = authorAvatar,
        isAnonymous = isAnonymous,
        anonymousBadge = anonymousBadge,
        communityId = communityId,
        communityName = communityName,
        postType = parsedType,
        title = title,
        body = body,
        tags = tags,
        pollOptions = pollOptions,
        pollVotes = pollVotes,
        likesCount = likesCount,
        commentsCount = commentsCount,
        isLiked = false,
        isSaved = false,
        createdAt = createdAt,
        moderationStatus = parsedModeration,
        pillarScope = pillarScope,
        city = city
    )
}
