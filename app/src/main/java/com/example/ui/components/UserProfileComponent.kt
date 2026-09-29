package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthState
import com.example.data.local.SkillEndorsementEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.ColleagueXInputTextStyle
import com.example.ui.theme.colleagueXTextFieldColors
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen

/**
 * User Profile component using Room Database persistence for local details (display name, job title, bio)
 * linked to the user's Firebase Auth UID.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileComponent(
    user: UserEntity,
    authState: AuthState = AuthState.Unauthenticated,
    isCurrentUser: Boolean = false,
    endorsements: List<SkillEndorsementEntity> = emptyList(),
    currentUserId: Long = 1,
    onEndorseSkill: ((skillName: String, note: String) -> Unit)? = null,
    onSaveProfile: (displayName: String, jobTitle: String, bio: String, headline: String, employer: String, location: String, skills: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateToVerification: () -> Unit = {},
    onToggleEmployerVisibility: (Boolean) -> Unit = {},
    onToggleLocationVisibility: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var activeEndorsementSkill by remember { mutableStateOf<Pair<String, Int>?>(null) }

    // Resolve Firebase UID linkage
    val linkedFirebaseUid = when {
        user.firebaseUid.isNotBlank() -> user.firebaseUid
        authState is AuthState.Authenticated -> authState.uid
        else -> "local_uid_${user.id}"
    }

    val isFirebaseLinked = user.firebaseUid.isNotBlank() || authState is AuthState.Authenticated

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Main Profile Card (Room Local Data) ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("user_profile_main_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Top row: Avatar, Badges & Edit Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserProfilePicture(
                        photoUrl = user.photoUrl.ifBlank { null },
                        contentDescription = "Profile picture for ${user.fullName}",
                        sizeDp = 64,
                        modifier = Modifier
                            .shadow(2.dp, CircleShape)
                            .border(1.5.dp, WorkCircleCardBorder, CircleShape)
                            .clip(CircleShape)
                            .testTag("user_profile_picture")
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoleBadge(role = user.roleType)

                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalIconButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("edit_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = WorkCircleBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display Name (fullName in Room)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = WorkCircleNavy,
                        modifier = Modifier.testTag("profile_display_name")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (user.isVerified) {
                        VerifiedBadge(text = user.verifiedBadgeText.ifEmpty { "Verified" })
                    }
                }

                // Job Title & Headline (role & headline in Room)
                Text(
                    text = if (user.role.isNotBlank()) "${user.role} • ${user.headline}" else user.headline,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkCircleBlue,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .testTag("profile_job_title")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Employer and Location Row
                if (user.employerVisibility) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = WorkCircleSecondaryText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.employer.ifBlank { "Independent" },
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText,
                            modifier = Modifier.testTag("profile_employer")
                        )

                        if (user.locationVisibility) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = WorkCircleSecondaryText,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.location.ifBlank { "Global / Remote" },
                                fontSize = 12.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bio (Stored in Room)
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ABOUT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleSecondaryText,
                                letterSpacing = 0.5.sp
                            )
                            if (isCurrentUser) {
                                Text(
                                    text = "Edit Bio",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WorkCircleBlue,
                                    modifier = Modifier
                                        .clickable { showEditDialog = true }
                                        .testTag("edit_bio_quick_button")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.bio.ifBlank { "No bio added yet. Click edit to add your background." },
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = WorkCircleNavy.copy(alpha = 0.9f),
                            modifier = Modifier.testTag("profile_bio_text")
                        )
                    }
                }

                // Skills Tags (Stored in Room)
                if (user.skills.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Professional Skills",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy
                        )
                        if (endorsements.isNotEmpty()) {
                            Text(
                                text = "${endorsements.size} ${if (endorsements.size == 1) "Endorsement" else "Endorsements"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_profile_skills_list")
                    ) {
                        user.skills.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(6).forEachIndexed { idx, skill ->
                            val skillEndorsementCount = endorsements.count { it.skillName.equals(skill, ignoreCase = true) }
                            SkillPill(
                                skill = skill,
                                index = idx,
                                isSelected = activeEndorsementSkill?.first.equals(skill, ignoreCase = true),
                                endorsementCount = skillEndorsementCount,
                                onClick = {
                                    activeEndorsementSkill = Pair(skill, idx)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp))
                        .padding(vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${user.connectionsCount}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WorkCircleNavy)
                        Text("Connections", fontSize = 11.sp, color = WorkCircleSecondaryText)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${user.contributionsCount}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WorkCircleNavy)
                        Text("Contributions", fontSize = 11.sp, color = WorkCircleSecondaryText)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(user.experienceLevel, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WorkCircleNavy)
                        Text("Experience", fontSize = 11.sp, color = WorkCircleSecondaryText)
                    }
                }
            }
        }

        // --- 2. Firebase Auth UID & Room Database Linkage Card ---
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("firebase_uid_linkage_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = WorkCircleBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Local Room DB & Firebase Link",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = WorkCircleNavy
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isFirebaseLinked) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = if (isFirebaseLinked) "LINKED" else "LOCAL ONLY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFirebaseLinked) Color(0xFF166534) else Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Firebase Auth UID
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = WorkCircleBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Firebase Auth UID",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleSecondaryText
                            )
                            Text(
                                text = linkedFirebaseUid,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = WorkCircleNavy,
                                modifier = Modifier.testTag("firebase_uid_value")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Room Database Entity Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Room Table: ",
                            fontSize = 11.sp,
                            color = WorkCircleSecondaryText
                        )
                        Text(
                            text = "users (id: ${user.id})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = WorkCircleNavy
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Provider: ",
                            fontSize = 11.sp,
                            color = WorkCircleSecondaryText
                        )
                        Text(
                            text = when (user.authProvider) {
                                "google.com" -> "Google"
                                "password" -> "Email/Pass"
                                else -> user.authProvider
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleBlue
                        )
                    }
                }
            }
        }

        // --- 3. Privacy & Identity Controls (for Current User) ---
        if (isCurrentUser) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = WorkCircleBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Privacy & Visibility Controls",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = WorkCircleNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Display Current Employer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WorkCircleNavy)
                            Text("Turn off to hide your company name on your public profile", fontSize = 11.sp, color = WorkCircleSecondaryText)
                        }
                        Switch(
                            checked = user.employerVisibility,
                            onCheckedChange = onToggleEmployerVisibility,
                            colors = SwitchDefaults.colors(checkedTrackColor = WorkCircleBlue)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Display Location", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WorkCircleNavy)
                            Text("Turn off to mask your city/state from public view", fontSize = 11.sp, color = WorkCircleSecondaryText)
                        }
                        Switch(
                            checked = user.locationVisibility,
                            onCheckedChange = onToggleLocationVisibility,
                            colors = SwitchDefaults.colors(checkedTrackColor = WorkCircleBlue)
                        )
                    }
                }
            }
        }
    }

    // --- 4. Interactive Edit Profile Dialog (Direct Room Persistence) ---
    if (showEditDialog) {
        EditProfileModalDialog(
            user = user,
            linkedFirebaseUid = linkedFirebaseUid,
            onDismiss = { showEditDialog = false },
            onSave = { displayName, jobTitle, bio, headline, employer, location, skills ->
                onSaveProfile(displayName, jobTitle, bio, headline, employer, location, skills)
                showEditDialog = false
            }
        )
    }

    // Interactive Skill Endorsement Bottom Sheet
    activeEndorsementSkill?.let { (skillName, skillIndex) ->
        SkillEndorsementSheet(
            skillName = skillName,
            skillIndex = skillIndex,
            targetUserId = user.id,
            currentUserId = currentUserId,
            endorsements = endorsements,
            onDismiss = { activeEndorsementSkill = null },
            onEndorse = { sName, note ->
                onEndorseSkill?.invoke(sName, note)
            }
        )
    }
}

@Composable
private fun EditProfileModalDialog(
    user: UserEntity,
    linkedFirebaseUid: String,
    onDismiss: () -> Unit,
    onSave: (displayName: String, jobTitle: String, bio: String, headline: String, employer: String, location: String, skills: String) -> Unit
) {
    var displayName by remember { mutableStateOf(user.fullName) }
    var jobTitle by remember { mutableStateOf(user.role) }
    var bio by remember { mutableStateOf(user.bio) }
    var headline by remember { mutableStateOf(user.headline) }
    var employer by remember { mutableStateOf(user.employer) }
    var location by remember { mutableStateOf(user.location) }
    var skills by remember { mutableStateOf(user.skills) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = WorkCircleBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Local User Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = WorkCircleNavy
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Info banner explaining Room persistence
                Surface(
                    color = Color(0xFFEBF2FF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = WorkCircleBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Changes are saved to SQLite via Room Database and linked to Firebase UID: ${linkedFirebaseUid.take(10)}...",
                            fontSize = 11.sp,
                            color = WorkCircleNavy,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Display Name Field
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. Alex Chen") },
                    textStyle = ColleagueXInputTextStyle,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = WorkCircleBlue)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_display_name_input"),
                    colors = colleagueXTextFieldColors()
                )

                // Job Title Field
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title / Role") },
                    placeholder = { Text("e.g. Staff Product Manager") },
                    textStyle = ColleagueXInputTextStyle,
                    leadingIcon = {
                        Icon(Icons.Default.Work, contentDescription = null, tint = WorkCircleBlue)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_job_title_input"),
                    colors = colleagueXTextFieldColors()
                )

                // Bio Field (Multi-line)
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio / About") },
                    placeholder = { Text("Share your expertise, career focus, and discussion interests...") },
                    textStyle = ColleagueXInputTextStyle,
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null, tint = WorkCircleBlue)
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_bio_input"),
                    colors = colleagueXTextFieldColors(),
                    supportingText = {
                        Text(
                            text = "${bio.length} characters • Persisted in Room database",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                )

                // Professional Headline
                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = { Text("Professional Headline") },
                    placeholder = { Text("e.g. AI Platform • Strategic Alignment") },
                    textStyle = ColleagueXInputTextStyle,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = colleagueXTextFieldColors()
                )

                // Company / Employer Field
                OutlinedTextField(
                    value = employer,
                    onValueChange = { employer = it },
                    label = { Text("Company / Employer") },
                    placeholder = { Text("e.g. Stripe, Bain, or Independent") },
                    textStyle = ColleagueXInputTextStyle,
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = null, tint = WorkCircleBlue)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_company_name_input"),
                    colors = colleagueXTextFieldColors()
                )

                // Location Field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. San Francisco, CA or Remote") },
                    textStyle = ColleagueXInputTextStyle,
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkCircleBlue)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = colleagueXTextFieldColors()
                )

                // Skills Field
                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Skills & Focus Areas") },
                    placeholder = { Text("e.g. Product Strategy, AI Systems, Leadership") },
                    textStyle = ColleagueXInputTextStyle,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = colleagueXTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(displayName, jobTitle, bio, headline, employer, location, skills)
                },
                enabled = displayName.isNotBlank() && jobTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WorkCircleNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save to Room DB", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun FilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFFEBF2FF),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
