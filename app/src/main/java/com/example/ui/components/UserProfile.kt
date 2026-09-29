package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.SkillEndorsementEntity
import com.example.data.local.UserEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen

/**
 * UserProfile screen component that displays a profile picture (with placeholder image support),
 * name, job title, editable biography, and a list of professional skills.
 *
 * Includes a 'Save' button to persist edited biography and profile information
 * using Room Database for offline storage.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfile(
    name: String,
    jobTitle: String,
    skills: List<String>,
    modifier: Modifier = Modifier,
    profilePictureUrl: String? = null,
    company: String? = null,
    location: String? = null,
    bio: String? = null,
    isBioEditable: Boolean = true,
    isProfileEditable: Boolean = true,
    isVerified: Boolean = false,
    verifiedBadgeText: String = "Verified Professional",
    onEditProfileClick: (() -> Unit)? = null,
    onSaveProfile: ((name: String, jobTitle: String, bio: String, company: String, location: String, skills: String) -> Unit)? = null,
    onBioUpdated: ((String) -> Unit)? = null,
    onSkillClick: ((String) -> Unit)? = null,
    endorsements: List<SkillEndorsementEntity> = emptyList(),
    targetUserId: Long = 1,
    currentUserId: Long = 1,
    onEndorseSkill: ((skillName: String, note: String) -> Unit)? = null
) {
    // Current profile state
    var currentName by remember(name) { mutableStateOf(name) }
    var currentJobTitle by remember(jobTitle) { mutableStateOf(jobTitle) }
    var currentBio by remember(bio) { mutableStateOf(bio ?: "") }
    var currentCompany by remember(company) { mutableStateOf(company ?: "") }
    var currentLocation by remember(location) { mutableStateOf(location ?: "") }
    var currentSkillsList by remember(skills) { mutableStateOf(skills) }

    // Draft edit states
    var isEditingFullProfile by remember { mutableStateOf(false) }
    var nameDraft by remember(currentName) { mutableStateOf(currentName) }
    var jobTitleDraft by remember(currentJobTitle) { mutableStateOf(currentJobTitle) }
    var companyDraft by remember(currentCompany) { mutableStateOf(currentCompany) }
    var locationDraft by remember(currentLocation) { mutableStateOf(currentLocation) }
    var skillsDraft by remember(currentSkillsList) { mutableStateOf(currentSkillsList.joinToString(", ")) }

    // Inline bio editing states
    var isEditingBio by remember { mutableStateOf(false) }
    var bioDraft by remember(currentBio) { mutableStateOf(currentBio) }

    // Status message for Room persistence
    var lastSavedMessage by remember { mutableStateOf<String?>(null) }

    // Selected skill tag state for interactive clickable feedback
    var selectedSkill by remember { mutableStateOf<String?>(null) }
    var activeEndorsementSkill by remember { mutableStateOf<Pair<String, Int>?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, WorkCircleCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_profile_component")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header accent banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                WorkCircleNavy,
                                Color(0xFF1E3A8A),
                                WorkCircleBlue
                            )
                        )
                    )
            ) {
                if (isProfileEditable) {
                    IconButton(
                        onClick = {
                            if (onEditProfileClick != null) {
                                onEditProfileClick()
                            } else {
                                // Toggle inline full profile edit mode
                                nameDraft = currentName
                                jobTitleDraft = currentJobTitle
                                bioDraft = currentBio
                                companyDraft = currentCompany
                                locationDraft = currentLocation
                                skillsDraft = currentSkillsList.joinToString(", ")
                                isEditingFullProfile = !isEditingFullProfile
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .testTag("user_profile_edit_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isEditingFullProfile) Icons.Default.Close else Icons.Default.Edit,
                                    contentDescription = if (isEditingFullProfile) "Cancel Edit" else "Edit Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Profile Information Column with overlapping Avatar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                // Profile Picture (Avatar with placeholder image support)
                Box(
                    modifier = Modifier
                        .offset(y = (-44).dp)
                        .testTag("user_profile_picture_container")
                ) {
                    UserProfilePicture(
                        photoUrl = profilePictureUrl,
                        contentDescription = "Profile picture for $currentName",
                        sizeDp = 92,
                        modifier = Modifier
                            .shadow(6.dp, CircleShape)
                            .border(3.5.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .testTag("user_profile_picture")
                    )

                    if (isVerified) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Profile",
                                    tint = WorkCircleVerifiedGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Adjust spacing for avatar offset
                Spacer(modifier = Modifier.height((-32).dp))

                // Offline storage confirmation banner
                AnimatedVisibility(visible = lastSavedMessage != null) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("user_profile_offline_storage_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Offline Storage",
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = lastSavedMessage ?: "Persisted to Room Database (Offline Available)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                if (isEditingFullProfile) {
                    // --- FULL EDIT MODE (With dedicated Save button for Room DB) ---
                    Text(
                        text = "Edit Profile & Biography",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy
                    )
                    Text(
                        text = "Changes will be saved to Room Database for instant offline access.",
                        fontSize = 12.sp,
                        color = WorkCircleSecondaryText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Edit Name
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_profile_edit_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Edit Job Title
                    OutlinedTextField(
                        value = jobTitleDraft,
                        onValueChange = { jobTitleDraft = it },
                        label = { Text("Job Title / Role") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_profile_edit_job_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Edit Biography
                    OutlinedTextField(
                        value = bioDraft,
                        onValueChange = { if (it.length <= 300) bioDraft = it },
                        label = { Text("Short Biography") },
                        placeholder = { Text("Describe your expertise, experience, and background...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_profile_bio_edit_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder
                        ),
                        supportingText = {
                            Text("${bioDraft.length}/300 characters", fontSize = 11.sp, color = WorkCircleSecondaryText)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Edit Company
                    OutlinedTextField(
                        value = companyDraft,
                        onValueChange = { companyDraft = it },
                        label = { Text("Company / Organization") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_profile_edit_company_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Edit Location
                    OutlinedTextField(
                        value = locationDraft,
                        onValueChange = { locationDraft = it },
                        label = { Text("Location") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_profile_edit_location_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Edit Skills
                    OutlinedTextField(
                        value = skillsDraft,
                        onValueChange = { skillsDraft = it },
                        label = { Text("Professional Skills (comma separated)") },
                        placeholder = { Text("e.g. Kotlin, Jetpack Compose, System Architecture") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_profile_edit_skills_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkCircleBlue,
                            unfocusedBorderColor = WorkCircleCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Row with primary 'Save' Button
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                isEditingFullProfile = false
                            },
                            modifier = Modifier.testTag("user_profile_cancel_button")
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                // Persist edited biography and profile info
                                currentName = nameDraft.trim()
                                currentJobTitle = jobTitleDraft.trim()
                                currentBio = bioDraft.trim()
                                currentCompany = companyDraft.trim()
                                currentLocation = locationDraft.trim()
                                currentSkillsList = skillsDraft
                                    .split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }

                                isEditingFullProfile = false
                                lastSavedMessage = "Saved to Room Database for offline storage"

                                // Callback triggers Room database update
                                onSaveProfile?.invoke(
                                    currentName,
                                    currentJobTitle,
                                    currentBio,
                                    currentCompany,
                                    currentLocation,
                                    skillsDraft
                                )
                                onBioUpdated?.invoke(currentBio)
                            },
                            enabled = nameDraft.isNotBlank() && jobTitleDraft.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = WorkCircleNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("user_profile_save_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Profile",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // --- VIEW MODE ---

                    // Name & Verified Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentName.ifBlank { "Professional Member" },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy,
                            modifier = Modifier.testTag("user_profile_name")
                        )

                        if (isVerified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = WorkCircleVerifiedGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = verifiedBadgeText,
                                        color = WorkCircleVerifiedGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Job Title
                    Text(
                        text = currentJobTitle.ifBlank { "Professional" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkCircleBlue,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .testTag("user_profile_job_title")
                    )

                    // Optional Company & Location Metadata
                    if (currentCompany.isNotBlank() || currentLocation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (currentCompany.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        tint = WorkCircleSecondaryText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentCompany,
                                        fontSize = 12.sp,
                                        color = WorkCircleSecondaryText,
                                        modifier = Modifier.testTag("user_profile_company")
                                    )
                                }
                            }

                            if (currentLocation.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = WorkCircleSecondaryText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentLocation,
                                        fontSize = 12.sp,
                                        color = WorkCircleSecondaryText,
                                        modifier = Modifier.testTag("user_profile_location")
                                    )
                                }
                            }
                        }
                    }

                    // --- Short, Editable Biography Text Section ---
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isEditingBio) WorkCircleBlue else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_profile_bio_section")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = WorkCircleBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "BIOGRAPHY",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkCircleNavy,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                if (isBioEditable && !isEditingBio) {
                                    TextButton(
                                        onClick = {
                                            bioDraft = currentBio
                                            isEditingBio = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.testTag("user_profile_edit_bio_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit biography",
                                            modifier = Modifier.size(13.dp),
                                            tint = WorkCircleBlue
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (currentBio.isBlank()) "Add Bio" else "Edit",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WorkCircleBlue
                                        )
                                    }
                                }
                            }

                            if (isEditingBio) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = bioDraft,
                                    onValueChange = { if (it.length <= 250) bioDraft = it },
                                    placeholder = {
                                        Text(
                                            "Write a short professional bio...",
                                            fontSize = 13.sp,
                                            color = WorkCircleSecondaryText
                                        )
                                    },
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_bio_edit_field"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WorkCircleBlue,
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "${bioDraft.length}/250",
                                        fontSize = 11.sp,
                                        color = WorkCircleSecondaryText
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        TextButton(
                                            onClick = {
                                                bioDraft = currentBio
                                                isEditingBio = false
                                            },
                                            modifier = Modifier.testTag("user_profile_cancel_bio_button")
                                        ) {
                                            Text("Cancel", fontSize = 12.sp, color = Color.Gray)
                                        }

                                        // 'Save' button in biography section that persists to Room Database
                                        Button(
                                            onClick = {
                                                currentBio = bioDraft.trim()
                                                isEditingBio = false
                                                lastSavedMessage = "Biography saved offline to Room DB"

                                                // Trigger Room database persistence
                                                onBioUpdated?.invoke(currentBio)
                                                onSaveProfile?.invoke(
                                                    currentName,
                                                    currentJobTitle,
                                                    currentBio,
                                                    currentCompany,
                                                    currentLocation,
                                                    currentSkillsList.joinToString(", ")
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = WorkCircleNavy),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("user_profile_save_bio_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Save,
                                                contentDescription = "Save Biography",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (currentBio.isNotBlank()) currentBio else "No biography added yet. Click edit to introduce yourself.",
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = if (currentBio.isNotBlank()) WorkCircleNavy.copy(alpha = 0.9f) else WorkCircleSecondaryText,
                                    modifier = Modifier.testTag("user_profile_bio")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Professional Skills Section Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = WorkCircleNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Professional Skills",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleNavy,
                                letterSpacing = 0.2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = currentSkillsList.size.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleSecondaryText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }

                        if (selectedSkill != null) {
                            TextButton(
                                onClick = { selectedSkill = null },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.testTag("user_profile_clear_skill_selection")
                            ) {
                                Text(
                                    text = "Clear selection",
                                    fontSize = 11.sp,
                                    color = WorkCircleBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Interactive selection banner for clicked skill
                    AnimatedVisibility(visible = selectedSkill != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .testTag("user_profile_active_skill_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Active Skill Tag: ${selectedSkill}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF15803D)
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        val skill = selectedSkill
                                        if (skill != null) {
                                            val idx = currentSkillsList.indexOfFirst { it.equals(skill, ignoreCase = true) }
                                            activeEndorsementSkill = Pair(skill, if (idx >= 0) idx else 0)
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.testTag("user_profile_view_endorsements_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Endorsements",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // List of Professional Skills as colorful, clickable tags or pills
                    if (currentSkillsList.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_profile_skills_list")
                        ) {
                            currentSkillsList.forEachIndexed { index, skill ->
                                val isSelected = selectedSkill.equals(skill, ignoreCase = true)
                                val skillEndorsementCount = endorsements.count { it.skillName.equals(skill, ignoreCase = true) }
                                SkillPill(
                                    skill = skill,
                                    index = index,
                                    isSelected = isSelected,
                                    endorsementCount = skillEndorsementCount,
                                    onClick = {
                                        selectedSkill = if (isSelected) null else skill
                                        onSkillClick?.invoke(skill)
                                        activeEndorsementSkill = Pair(skill, index)
                                    }
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No skills added yet.",
                                fontSize = 12.sp,
                                color = WorkCircleSecondaryText,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Interactive Skill Endorsement Bottom Sheet
    activeEndorsementSkill?.let { (skillName, skillIndex) ->
        SkillEndorsementSheet(
            skillName = skillName,
            skillIndex = skillIndex,
            targetUserId = targetUserId,
            currentUserId = currentUserId,
            endorsements = endorsements,
            onDismiss = { activeEndorsementSkill = null },
            onEndorse = { sName, note ->
                onEndorseSkill?.invoke(sName, note)
            }
        )
    }
}

/**
 * Overload of [UserProfile] that accepts a [UserEntity] from the Room Database.
 */
@Composable
fun UserProfile(
    user: UserEntity,
    modifier: Modifier = Modifier,
    isBioEditable: Boolean = true,
    isProfileEditable: Boolean = true,
    onEditProfileClick: (() -> Unit)? = null,
    onSaveProfile: ((name: String, jobTitle: String, bio: String, company: String, location: String, skills: String) -> Unit)? = null,
    onBioUpdated: ((String) -> Unit)? = null,
    onSkillClick: ((String) -> Unit)? = null,
    endorsements: List<SkillEndorsementEntity> = emptyList(),
    currentUserId: Long = 1,
    onEndorseSkill: ((skillName: String, note: String) -> Unit)? = null
) {
    val skillsList = user.skills
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    UserProfile(
        name = user.fullName,
        jobTitle = user.role.ifBlank { user.headline },
        skills = skillsList,
        profilePictureUrl = user.photoUrl.ifBlank { null },
        company = user.employer.takeIf { user.employerVisibility },
        location = user.location.takeIf { user.locationVisibility },
        bio = user.bio,
        isBioEditable = isBioEditable,
        isProfileEditable = isProfileEditable,
        isVerified = user.isVerified,
        verifiedBadgeText = user.verifiedBadgeText.ifBlank { "Verified" },
        onEditProfileClick = onEditProfileClick,
        onSaveProfile = onSaveProfile,
        onBioUpdated = onBioUpdated,
        onSkillClick = onSkillClick,
        endorsements = endorsements,
        targetUserId = user.id,
        currentUserId = currentUserId,
        onEndorseSkill = onEndorseSkill,
        modifier = modifier
    )
}

/**
 * Overload of [UserProfile] that accepts a [UserProfileEntity].
 */
@Composable
fun UserProfile(
    profile: UserProfileEntity,
    modifier: Modifier = Modifier,
    isBioEditable: Boolean = true,
    isProfileEditable: Boolean = true,
    onEditProfileClick: (() -> Unit)? = null,
    onSaveProfile: ((name: String, jobTitle: String, bio: String, company: String, location: String, skills: String) -> Unit)? = null,
    onBioUpdated: ((String) -> Unit)? = null,
    onSkillClick: ((String) -> Unit)? = null,
    endorsements: List<SkillEndorsementEntity> = emptyList(),
    currentUserId: Long = 1,
    onEndorseSkill: ((skillName: String, note: String) -> Unit)? = null
) {
    val skillsList = profile.skills
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    UserProfile(
        name = profile.fullName,
        jobTitle = profile.jobTitle.ifBlank { profile.headline },
        skills = skillsList,
        profilePictureUrl = null, // uses placeholder image
        company = profile.companyName,
        location = profile.location,
        bio = profile.bio,
        isBioEditable = isBioEditable,
        isProfileEditable = isProfileEditable,
        isVerified = profile.isVerified,
        verifiedBadgeText = "Verified Member",
        onEditProfileClick = onEditProfileClick,
        onSaveProfile = onSaveProfile,
        onBioUpdated = onBioUpdated,
        onSkillClick = onSkillClick,
        endorsements = endorsements,
        targetUserId = profile.id,
        currentUserId = currentUserId,
        onEndorseSkill = onEndorseSkill,
        modifier = modifier
    )
}

/**
 * Profile picture component that loads an image URL using Coil or displays
 * the placeholder profile image drawable [R.drawable.ic_profile_placeholder].
 */
@Composable
fun UserProfilePicture(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Profile picture",
    sizeDp: Int = 92
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .background(Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.ic_profile_placeholder),
                error = painterResource(id = R.drawable.ic_profile_placeholder),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(sizeDp.dp)
                    .clip(CircleShape)
            )
        } else {
            // Displays the placeholder profile picture image
            Image(
                painter = painterResource(id = R.drawable.ic_profile_placeholder),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(sizeDp.dp)
                    .clip(CircleShape)
            )
        }
    }
}

/**
 * Color palette definition for colorful professional skill tags/pills.
 */
data class SkillPillPalette(
    val backgroundColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val iconColor: Color,
    val selectedBgColor: Color,
    val selectedBorderColor: Color
)

fun getSkillPillPalette(index: Int): SkillPillPalette {
    val palettes = listOf(
        // Royal Indigo
        SkillPillPalette(
            backgroundColor = Color(0xFFEEF2FF),
            borderColor = Color(0xFFC7D2FE),
            textColor = Color(0xFF3730A3),
            iconColor = Color(0xFF4F46E5),
            selectedBgColor = Color(0xFF4F46E5),
            selectedBorderColor = Color(0xFF3730A3)
        ),
        // Emerald Green
        SkillPillPalette(
            backgroundColor = Color(0xFFECFDF5),
            borderColor = Color(0xFFA7F3D0),
            textColor = Color(0xFF065F46),
            iconColor = Color(0xFF059669),
            selectedBgColor = Color(0xFF059669),
            selectedBorderColor = Color(0xFF047857)
        ),
        // Warm Amber
        SkillPillPalette(
            backgroundColor = Color(0xFFFFFBEB),
            borderColor = Color(0xFFFDE68A),
            textColor = Color(0xFF92400E),
            iconColor = Color(0xFFD97706),
            selectedBgColor = Color(0xFFD97706),
            selectedBorderColor = Color(0xFFB45309)
        ),
        // Vivid Violet
        SkillPillPalette(
            backgroundColor = Color(0xFFFAF5FF),
            borderColor = Color(0xFFE9D5FF),
            textColor = Color(0xFF581C87),
            iconColor = Color(0xFF7C3AED),
            selectedBgColor = Color(0xFF7C3AED),
            selectedBorderColor = Color(0xFF6D28D9)
        ),
        // Ocean Cyan
        SkillPillPalette(
            backgroundColor = Color(0xFFECFEFF),
            borderColor = Color(0xFFA5F3FC),
            textColor = Color(0xFF155E75),
            iconColor = Color(0xFF0891B2),
            selectedBgColor = Color(0xFF0891B2),
            selectedBorderColor = Color(0xFF0E7490)
        ),
        // Modern Rose
        SkillPillPalette(
            backgroundColor = Color(0xFFFFF1F2),
            borderColor = Color(0xFFFECDD3),
            textColor = Color(0xFF9F1239),
            iconColor = Color(0xFFE11D48),
            selectedBgColor = Color(0xFFE11D48),
            selectedBorderColor = Color(0xFFBE123C)
        )
    )
    return palettes[index % palettes.size]
}

fun getSkillIcon(skill: String): ImageVector {
    val lower = skill.lowercase()
    return when {
        lower.contains("code") || lower.contains("kotlin") || lower.contains("compose") ||
        lower.contains("java") || lower.contains("python") || lower.contains("android") ||
        lower.contains("engineer") || lower.contains("architect") || lower.contains("backend") ||
        lower.contains("frontend") || lower.contains("dev") -> Icons.Default.Code

        lower.contains("lead") || lower.contains("mentor") || lower.contains("team") ||
        lower.contains("manage") || lower.contains("people") || lower.contains("culture") -> Icons.Default.Groups

        lower.contains("design") || lower.contains("ui") || lower.contains("ux") ||
        lower.contains("art") || lower.contains("visual") || lower.contains("figma") -> Icons.Default.Palette

        lower.contains("product") || lower.contains("strat") || lower.contains("growth") ||
        lower.contains("scale") || lower.contains("market") || lower.contains("business") -> Icons.Default.TrendingUp

        lower.contains("ai") || lower.contains("ml") || lower.contains("model") ||
        lower.contains("data") || lower.contains("intelligence") || lower.contains("prompt") -> Icons.Default.Psychology

        else -> Icons.Default.Star
    }
}

/**
 * A colorful, clickable tag/pill for a professional skill.
 * Provides distinct hues, tactile touch target, active state styling,
 * and accessibility semantics for maximum readability.
 */
@Composable
fun SkillPill(
    skill: String,
    index: Int,
    isSelected: Boolean = false,
    endorsementCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = getSkillPillPalette(index)
    val icon = if (isSelected) Icons.Default.Check else getSkillIcon(skill)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) palette.selectedBgColor else palette.backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) palette.selectedBorderColor else palette.borderColor
        ),
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("user_profile_skill_$index")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (isSelected) "Skill $skill selected" else "Skill $skill",
                tint = if (isSelected) Color.White else palette.iconColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = skill,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) Color.White else palette.textColor,
                letterSpacing = 0.2.sp
            )
            if (endorsementCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else palette.iconColor.copy(alpha = 0.15f),
                    modifier = Modifier.testTag("user_profile_skill_count_$index")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else palette.iconColor,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = endorsementCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else palette.textColor
                        )
                    }
                }
            }
        }
    }
}

