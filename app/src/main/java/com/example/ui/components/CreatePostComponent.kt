package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommunityEntity
import com.example.data.local.UserEntity
import com.example.data.model.PostType
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText

/**
 * Reusable Create Post Component that enables professionals to compose
 * workplace knowledge insights and save them to local Room storage.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePostComponent(
    communities: List<CommunityEntity>,
    currentUser: UserEntity? = null,
    onSubmit: (
        communityId: Long,
        communityName: String,
        postType: PostType,
        title: String,
        body: String,
        tags: String,
        isAnonymous: Boolean,
        pollOptions: List<String>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPostType by remember { mutableStateOf(PostType.KNOWLEDGE_INSIGHT) }
    var selectedCommunity by remember {
        mutableStateOf(
            communities.firstOrNull() ?: CommunityEntity(1, "Tech & AI Engineers", "tech", "Engineering discussions", "Technology", 2400)
        )
    }
    var communityMenuExpanded by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }

    val pollOptions = remember { mutableStateListOf("Option 1", "Option 2") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickTags = listOf(
        "Career Advice", "Tech Tips", "Leadership", "Productivity", "Compensation", "Hybrid Work", "System Architecture", "Interview Prep"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("create_post_component"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Firestore & Storage Status Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth().testTag("firestore_storage_indicator")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = WorkCircleBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Saved to Firestore Cloud Storage & Local Cache",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = WorkCircleNavy
                    )
                    Text(
                        text = "Published with verified author metadata and streams real-time to the community feed.",
                        fontSize = 11.sp,
                        color = WorkCircleSecondaryText
                    )
                }
            }
        }

        // Author Identity Preview
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(if (isAnonymous) Color(0xFF475569) else WorkCircleBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAnonymous) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            text = currentUser?.avatarInitials ?: "ME",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isAnonymous) "Anonymous Peer" else (currentUser?.fullName ?: "Verified Professional"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = WorkCircleNavy
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = if (isAnonymous) Color.Gray else WorkCircleBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = if (isAnonymous) "Masked Identity • Verified Work Domain" else (currentUser?.headline ?: "Member"),
                        fontSize = 11.sp,
                        color = WorkCircleSecondaryText
                    )
                }
            }
        }

        // Knowledge Post Type Selector
        Column {
            Text(
                text = "Post Format",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = WorkCircleNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    PostType.KNOWLEDGE_INSIGHT to "Insight",
                    PostType.QUESTION to "Question",
                    PostType.WORKPLACE_STORY to "Story",
                    PostType.POLL to "Poll"
                ).forEach { (type, label) ->
                    val isSelected = selectedPostType == type
                    Surface(
                        color = if (isSelected) WorkCircleBlue else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPostType = type }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else WorkCircleNavy,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Community Selection
        Column {
            Text(
                text = "Target Workplace Circle",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = WorkCircleNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { communityMenuExpanded = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = selectedCommunity.name,
                                fontWeight = FontWeight.SemiBold,
                                color = WorkCircleNavy,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${selectedCommunity.category} • ${selectedCommunity.memberCount} members",
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select circle", tint = WorkCircleNavy)
                    }
                }

                DropdownMenu(
                    expanded = communityMenuExpanded,
                    onDismissRequest = { communityMenuExpanded = false }
                ) {
                    communities.forEach { community ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(community.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(community.category, fontSize = 11.sp, color = WorkCircleSecondaryText)
                                }
                            },
                            onClick = {
                                selectedCommunity = community
                                communityMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Post Title
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                errorMessage = null
            },
            label = { Text("Knowledge Post Title / Question *") },
            placeholder = { Text("e.g., Evaluating distributed vector databases in high-throughput systems") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkCircleBlue,
                unfocusedBorderColor = WorkCircleCardBorder,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("post_title_input"),
            shape = RoundedCornerShape(10.dp)
        )

        // Post Body Content
        Column {
            OutlinedTextField(
                value = body,
                onValueChange = {
                    body = it
                    errorMessage = null
                },
                label = { Text("Knowledge Context & Deep-Dive *") },
                placeholder = { Text("Detail the challenge, architecture decisions, numbers/metrics, and key takeaways for peers...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WorkCircleBlue,
                    unfocusedBorderColor = WorkCircleCardBorder,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .testTag("post_body_input"),
                shape = RoundedCornerShape(10.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = "Supports structured points & context",
                    fontSize = 11.sp,
                    color = WorkCircleSecondaryText
                )
                Text(
                    text = "${body.length} chars",
                    fontSize = 11.sp,
                    color = if (body.length > 2000) Color.Red else WorkCircleSecondaryText
                )
            }
        }

        // Poll Options (if poll is selected)
        if (selectedPostType == PostType.POLL) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Poll Choices (Min 2)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = WorkCircleNavy
                )
                pollOptions.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { pollOptions[index] = it },
                            label = { Text("Choice ${index + 1}") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        if (pollOptions.size > 2) {
                            IconButton(onClick = { pollOptions.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove choice", tint = Color.Gray)
                            }
                        }
                    }
                }
                if (pollOptions.size < 4) {
                    OutlinedButton(
                        onClick = { pollOptions.add("Option ${pollOptions.size + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Choice", fontSize = 12.sp)
                    }
                }
            }
        }

        // Quick Topic Tags
        Column {
            Text("Topic Tags", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkCircleNavy)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickTags.forEach { quickTag ->
                    val isIncluded = tags.contains(quickTag, ignoreCase = true)
                    Surface(
                        color = if (isIncluded) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable {
                            tags = if (isIncluded) {
                                tags.split(",").map { it.trim() }.filter { !it.equals(quickTag, true) }.joinToString(", ")
                            } else {
                                if (tags.isBlank()) quickTag else "$tags, $quickTag"
                            }
                        }
                    ) {
                        Text(
                            text = "+ $quickTag",
                            fontSize = 11.sp,
                            color = if (isIncluded) WorkCircleBlue else Color(0xFF334155),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                placeholder = { Text("Or enter custom tags (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Anonymous Posting Toggle
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isAnonymous) Color(0xFFF8FAFC) else Color.White),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isAnonymous) WorkCircleBlue else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Post Anonymously", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkCircleNavy)
                        Text(
                            text = "Masks your name. Verified company domain is safely hashed.",
                            fontSize = 11.sp,
                            color = WorkCircleSecondaryText
                        )
                    }
                }
                Switch(
                    checked = isAnonymous,
                    onCheckedChange = { isAnonymous = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WorkCircleBlue),
                    modifier = Modifier.testTag("anonymous_toggle")
                )
            }
        }

        // Error Feedback
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFDC2626),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Submit Button (Persists to Room DB)
        Button(
            onClick = {
                if (title.isBlank()) {
                    errorMessage = "Please enter a title for your knowledge post"
                    return@Button
                }
                if (body.isBlank()) {
                    errorMessage = "Please enter the context or body content"
                    return@Button
                }
                onSubmit(
                    selectedCommunity.id,
                    selectedCommunity.name,
                    selectedPostType,
                    title.trim(),
                    body.trim(),
                    tags.trim(),
                    isAnonymous,
                    if (selectedPostType == PostType.POLL) pollOptions.toList() else emptyList()
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_post_button")
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isAnonymous) "Publish to Firestore Anonymously" else "Publish to Firestore & Community Feed",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}
