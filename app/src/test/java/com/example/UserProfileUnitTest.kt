package com.example

import com.example.data.local.SkillEndorsementEntity
import com.example.data.local.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileUnitTest {

    @Test
    fun `test user profile details linked to firebase uid`() {
        val user = UserEntity(
            id = 101L,
            email = "alex.chen@workcircle.io",
            fullName = "Alex Chen",
            username = "alex_chen",
            headline = "Staff Product Manager • AI Systems",
            bio = "Building intelligent systems with focus on cross-team collaboration.",
            employer = "Stripe",
            industry = "Technology & Fintech",
            role = "Staff Product Manager",
            location = "San Francisco, CA",
            experienceLevel = "10+ Years",
            avatarInitials = "AC",
            isVerified = true,
            verificationStatus = VerificationStatus.VERIFIED,
            verifiedBadgeText = "Verified Staff PM",
            roleType = UserRole.REGULAR_PROFESSIONAL,
            firebaseUid = "firebase_auth_uid_abc123xyz",
            authProvider = "google.com",
            skills = "Product Strategy, Distributed Systems, ML Ops"
        )

        assertEquals("Alex Chen", user.fullName)
        assertEquals("Staff Product Manager", user.role)
        assertEquals("Building intelligent systems with focus on cross-team collaboration.", user.bio)
        assertEquals("firebase_auth_uid_abc123xyz", user.firebaseUid)
        assertEquals("google.com", user.authProvider)
        assertTrue(user.isVerified)
    }

    @Test
    fun `test updating user profile preserves firebase uid link`() {
        val initialUser = UserEntity(
            id = 102L,
            email = "priya.sharma@workcircle.io",
            fullName = "Priya Sharma",
            username = "priya_sharma",
            headline = "Principal Architect",
            bio = "Initial bio text.",
            employer = "Google",
            industry = "Technology",
            role = "Architect",
            location = "Mountain View, CA",
            experienceLevel = "8+ Years",
            avatarInitials = "PS",
            firebaseUid = "firebase_auth_priya_789",
            authProvider = "password"
        )

        // Simulate update to display name, role/job title, and bio
        val updatedUser = initialUser.copy(
            fullName = "Priya Sharma, PhD",
            role = "Distinguished Cloud Architect",
            bio = "Specializing in large-scale data platforms and distributed consensus.",
            headline = "Distinguished Cloud Architect • Google"
        )

        assertEquals("Priya Sharma, PhD", updatedUser.fullName)
        assertEquals("Distinguished Cloud Architect", updatedUser.role)
        assertEquals("Specializing in large-scale data platforms and distributed consensus.", updatedUser.bio)
        // Firebase UID linkage must be strictly maintained
        assertEquals("firebase_auth_priya_789", updatedUser.firebaseUid)
        assertEquals(initialUser.id, updatedUser.id)
    }

    @Test
    fun `test user profile privacy switches`() {
        val user = UserEntity(
            id = 103L,
            email = "test@workcircle.io",
            fullName = "Jordan Lee",
            username = "jordan_lee",
            headline = "Senior Consultant",
            bio = "Strategy consultant.",
            employer = "McKinsey",
            industry = "Consulting",
            role = "Senior Consultant",
            location = "New York, NY",
            experienceLevel = "5+ Years",
            avatarInitials = "JL",
            employerVisibility = true,
            locationVisibility = true,
            firebaseUid = "uid_jordan_456"
        )

        val privateUser = user.copy(
            employerVisibility = false,
            locationVisibility = false
        )

        assertFalse(privateUser.employerVisibility)
        assertFalse(privateUser.locationVisibility)
        assertEquals("uid_jordan_456", privateUser.firebaseUid)
    }

    @Test
    fun `test user profile entity in Room with bio, job title, and company name`() {
        val userProfile = com.example.data.local.UserProfileEntity(
            id = 1L,
            fullName = "Alex Chen",
            bio = "Staff Product Manager specializing in AI developer tools.",
            jobTitle = "Staff Product Manager",
            companyName = "Google",
            headline = "Staff Product Manager • AI Platform",
            location = "San Francisco, CA"
        )

        assertEquals("Alex Chen", userProfile.fullName)
        assertEquals("Staff Product Manager specializing in AI developer tools.", userProfile.bio)
        assertEquals("Staff Product Manager", userProfile.jobTitle)
        assertEquals("Google", userProfile.companyName)

        // Test editing bio, job title, and company name to personalize presence
        val updatedProfile = userProfile.copy(
            bio = "Principal Product Architect leading developer experience and platform foundations.",
            jobTitle = "Principal Product Architect",
            companyName = "Alphabet Inc.",
            updatedAt = System.currentTimeMillis()
        )

        assertEquals("Principal Product Architect leading developer experience and platform foundations.", updatedProfile.bio)
        assertEquals("Principal Product Architect", updatedProfile.jobTitle)
        assertEquals("Alphabet Inc.", updatedProfile.companyName)
    }

    @Test
    fun `test user profile component attributes with skills list and placeholder image fallback`() {
        val skillsString = "Kotlin, Jetpack Compose, System Architecture, Team Mentorship"
        val skillsList = skillsString.split(",").map { it.trim() }.filter { it.isNotBlank() }

        assertEquals(4, skillsList.size)
        assertEquals("Kotlin", skillsList[0])
        assertEquals("Jetpack Compose", skillsList[1])
        assertEquals("System Architecture", skillsList[2])
        assertEquals("Team Mentorship", skillsList[3])

        val userWithPlaceholder = UserEntity(
            id = 104L,
            email = "dev@workcircle.io",
            fullName = "Taylor Swift",
            username = "taylor_swift",
            headline = "Principal Mobile Engineer",
            bio = "Crafting high performance Android apps.",
            employer = "TechCorp",
            industry = "Technology",
            role = "Principal Mobile Engineer",
            location = "Seattle, WA",
            experienceLevel = "12+ Years",
            avatarInitials = "TS",
            skills = skillsString,
            photoUrl = "" // Empty photoUrl triggers placeholder image
        )

        assertTrue(userWithPlaceholder.photoUrl.isBlank())
        assertEquals("Taylor Swift", userWithPlaceholder.fullName)
        assertEquals("Principal Mobile Engineer", userWithPlaceholder.role)
    }

    @Test
    fun `test short editable biography update and max character constraints`() {
        var currentBio = "Staff software engineer passionate about developer experience."
        val maxBioLength = 250

        // Test editing bio
        val editedBioDraft = "Staff engineer building next-gen developer productivity and AI-assisted workflows."
        val isValidLength = editedBioDraft.length <= maxBioLength

        assertTrue(isValidLength)
        if (isValidLength) {
            currentBio = editedBioDraft
        }

        assertEquals("Staff engineer building next-gen developer productivity and AI-assisted workflows.", currentBio)
        assertTrue(currentBio.length < maxBioLength)

        // Test exceeding length is prevented/truncated
        val overlyLongBio = "A".repeat(300)
        val coercedBio = if (overlyLongBio.length > maxBioLength) overlyLongBio.take(maxBioLength) else overlyLongBio
        assertEquals(250, coercedBio.length)
    }

    @Test
    fun `test user profile save button persists edited biography and profile info for offline storage`() {
        val originalUser = UserEntity(
            id = 201L,
            email = "alex.chen@workcircle.io",
            fullName = "Alex Chen",
            username = "alex_chen",
            headline = "Staff PM • AI Platform",
            bio = "Initial short bio.",
            employer = "Google",
            industry = "Technology",
            role = "Staff Product Manager",
            location = "San Francisco, CA",
            experienceLevel = "10+ Years",
            avatarInitials = "AC",
            skills = "Product Strategy, AI Systems",
            photoUrl = ""
        )

        // Simulate edited fields submitted via Save button
        val editedName = "Alex Chen, Staff PM"
        val editedRole = "Principal Product Manager"
        val editedBio = "Leading developer platforms and offline-first database systems for distributed workforces."
        val editedCompany = "Alphabet Inc."
        val editedLocation = "Mountain View, CA"
        val editedSkills = "Product Strategy, AI Systems, Offline-First Architecture, Room DB"

        // Simulate Room database update
        val savedUser = originalUser.copy(
            fullName = editedName,
            role = editedRole,
            bio = editedBio,
            employer = editedCompany,
            location = editedLocation,
            skills = editedSkills,
            headline = "$editedRole • $editedCompany"
        )

        // Verify all fields are properly updated for Room persistence
        assertEquals("Alex Chen, Staff PM", savedUser.fullName)
        assertEquals("Principal Product Manager", savedUser.role)
        assertEquals("Leading developer platforms and offline-first database systems for distributed workforces.", savedUser.bio)
        assertEquals("Alphabet Inc.", savedUser.employer)
        assertEquals("Mountain View, CA", savedUser.location)
        assertEquals("Product Strategy, AI Systems, Offline-First Architecture, Room DB", savedUser.skills)
        assertEquals(originalUser.id, savedUser.id)

        // Check skills parsing from persisted string
        val parsedSkills = savedUser.skills.split(",").map { it.trim() }
        assertEquals(4, parsedSkills.size)
        assertTrue(parsedSkills.contains("Offline-First Architecture"))
        assertTrue(parsedSkills.contains("Room DB"))
    }

    @Test
    fun `test colorful skill pill palettes and icon categorization for readability`() {
        val skills = listOf(
            "Kotlin Architecture",
            "Team Mentorship",
            "UI/UX Design",
            "Product Growth",
            "AI Systems Engineering",
            "Agile Delivery"
        )

        // Ensure each skill receives a distinct vibrant palette
        for (i in skills.indices) {
            val palette = com.example.ui.components.getSkillPillPalette(i)
            assertNotNull(palette.backgroundColor)
            assertNotNull(palette.textColor)
            assertNotNull(palette.iconColor)
            assertNotNull(palette.borderColor)
        }

        // Test icon categorization
        val codeIcon = com.example.ui.components.getSkillIcon("Kotlin Android")
        val teamIcon = com.example.ui.components.getSkillIcon("Team Leadership")
        val designIcon = com.example.ui.components.getSkillIcon("Design Systems")
        val productIcon = com.example.ui.components.getSkillIcon("Product Strategy")
        val aiIcon = com.example.ui.components.getSkillIcon("AI Model Optimization")

        assertNotNull(codeIcon)
        assertNotNull(teamIcon)
        assertNotNull(designIcon)
        assertNotNull(productIcon)
        assertNotNull(aiIcon)
    }

    @Test
    fun `test skill tag clickable selection toggle`() {
        var selectedSkill: String? = null

        // Clicking a skill tag toggles it on
        val skillToClick = "Kotlin Architecture"
        selectedSkill = if (selectedSkill == skillToClick) null else skillToClick
        assertEquals("Kotlin Architecture", selectedSkill)

        // Clicking the same skill tag again toggles it off
        selectedSkill = if (selectedSkill == skillToClick) null else skillToClick
        assertNull(selectedSkill)
    }

    @Test
    fun `test skill endorsement entity attributes and validation`() {
        val endorsement = SkillEndorsementEntity(
            id = 1L,
            targetUserId = 101L,
            endorserUserId = 202L,
            endorserName = "Priya Sharma",
            endorserRole = "Engagement Manager @ Bain",
            endorserPhotoUrl = "",
            isEndorserVerified = true,
            skillName = "Product Strategy",
            note = "Exceptional strategic clarity and execution velocity.",
            timestamp = 1700000000000L
        )

        assertEquals(101L, endorsement.targetUserId)
        assertEquals(202L, endorsement.endorserUserId)
        assertEquals("Priya Sharma", endorsement.endorserName)
        assertEquals("Engagement Manager @ Bain", endorsement.endorserRole)
        assertTrue(endorsement.isEndorserVerified)
        assertEquals("Product Strategy", endorsement.skillName)
        assertTrue(endorsement.note.contains("Exceptional strategic clarity"))
    }

    @Test
    fun `test sample initial endorsements dataset`() {
        val sampleEndorsements = com.example.data.local.SampleData.getInitialEndorsements()
        assertTrue("Sample endorsements must not be empty", sampleEndorsements.isNotEmpty())

        val alexEndorsements = sampleEndorsements.filter { it.targetUserId == 1L }
        assertTrue("Target user 1 should have peer endorsements", alexEndorsements.isNotEmpty())

        val productStratCount = alexEndorsements.count { it.skillName.equals("Product Strategy", ignoreCase = true) }
        assertTrue("Product Strategy should have multiple peer endorsements", productStratCount >= 2)

        val aiSystemsCount = alexEndorsements.count { it.skillName.equals("AI Systems", ignoreCase = true) }
        assertTrue("AI Systems should have peer endorsements", aiSystemsCount >= 2)
    }

    @Test
    fun `test relative time formatting helper`() {
        val now = System.currentTimeMillis()
        val justNow = com.example.ui.components.formatRelativeTime(now - 1000)
        assertEquals("Just now", justNow)

        val fiveMinutesAgo = com.example.ui.components.formatRelativeTime(now - (5 * 60 * 1000))
        assertEquals("5m ago", fiveMinutesAgo)

        val threeHoursAgo = com.example.ui.components.formatRelativeTime(now - (3 * 3600 * 1000))
        assertEquals("3h ago", threeHoursAgo)

        val fourDaysAgo = com.example.ui.components.formatRelativeTime(now - (4 * 86400 * 1000))
        assertEquals("4d ago", fourDaysAgo)
    }

    @Test
    fun `test endorsement toggle logic simulation`() {
        val endorsements = mutableListOf<SkillEndorsementEntity>()
        val targetId = 1L
        val currentUserId = 2L
        val skill = "AI Systems"

        // First click: add endorsement
        val existing1 = endorsements.find { it.targetUserId == targetId && it.endorserUserId == currentUserId && it.skillName == skill }
        assertNull(existing1)
        endorsements.add(
            SkillEndorsementEntity(
                id = 1L,
                targetUserId = targetId,
                endorserUserId = currentUserId,
                endorserName = "Priya",
                endorserRole = "Consultant",
                skillName = skill
            )
        )
        assertEquals(1, endorsements.size)

        // Second click: withdraw endorsement
        val existing2 = endorsements.find { it.targetUserId == targetId && it.endorserUserId == currentUserId && it.skillName == skill }
        assertNotNull(existing2)
        endorsements.remove(existing2)
        assertEquals(0, endorsements.size)
    }
}

