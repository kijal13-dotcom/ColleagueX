package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.theme.colleagueXTextFieldColors
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JobEntity
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen

@Composable
fun JobsScreen(
    jobs: List<JobEntity>,
    onApplyToggle: (Long, Boolean) -> Unit,
    onSaveToggle: (Long, Boolean) -> Unit,
    onRequestReferral: (Long, String) -> Unit,
    onPostJob: (title: String, company: String, city: String, scope: String, exp: String, salary: String, type: String, dept: String, desc: String, reqs: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocationFilter by remember { mutableStateOf("All") }
    var selectedDeptFilter by remember { mutableStateOf("All") }

    var referralJobTarget by remember { mutableStateOf<JobEntity?>(null) }
    var referralPitchNote by remember { mutableStateOf("") }

    var showPostJobDialog by remember { mutableStateOf(false) }

    val locations = listOf("All", "Bengaluru", "Mumbai", "Delhi-NCR", "Hyderabad", "Remote")
    val departments = listOf("All", "Engineering", "Product", "Data & AI", "Finance")

    val filteredJobs = remember(jobs, searchQuery, selectedLocationFilter, selectedDeptFilter) {
        jobs.filter { job ->
            val matchLocation = when (selectedLocationFilter) {
                "All" -> true
                "Remote" -> job.jobType.contains("Remote", ignoreCase = true) || job.city.contains("Remote", ignoreCase = true)
                else -> job.city.contains(selectedLocationFilter, ignoreCase = true)
            }
            val matchDept = if (selectedDeptFilter == "All") true else job.department.contains(selectedDeptFilter, ignoreCase = true)
            val matchQuery = if (searchQuery.isBlank()) true else {
                job.title.contains(searchQuery, ignoreCase = true) ||
                        job.company.contains(searchQuery, ignoreCase = true) ||
                        job.description.contains(searchQuery, ignoreCase = true) ||
                        job.requirements.contains(searchQuery, ignoreCase = true)
            }
            matchLocation && matchDept && matchQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .testTag("colleaguex_jobs_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Header Banner
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFECFDF5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Work, contentDescription = null, tint = WorkCircleVerifiedGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ColleagueX Jobs",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Career Opportunities & Verified Referrals",
                                    fontSize = 11.sp,
                                    color = WorkCircleVerifiedGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Explore roles with transparent CTC bands and request direct referrals from verified colleagues working inside hiring companies.",
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp),
                            placeholder = { Text("Search roles, skills (e.g. Distributed Systems, PM, CTC)", fontSize = 12.sp, color = Color(0xFF64748B)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkCircleSecondaryText, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = WorkCircleSecondaryText)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = colleagueXTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("jobs_search_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Location Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(locations) { loc ->
                                val isSelected = selectedLocationFilter == loc
                                Surface(
                                    color = if (isSelected) WorkCircleNavy else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedLocationFilter = loc }
                                ) {
                                    Text(
                                        text = loc,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else WorkCircleNavy,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Department Filter Strip
            item {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(departments) { dept ->
                        val isSelected = selectedDeptFilter == dept
                        Surface(
                            color = if (isSelected) WorkCircleBlue else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(1.dp, if (isSelected) WorkCircleBlue else WorkCircleCardBorder, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedDeptFilter = dept }
                        ) {
                            Text(
                                text = dept,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else WorkCircleNavy,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. Results Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredJobs.size} Opportunities Found",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy
                    )
                    Text(
                        text = "100% Verified Referrals",
                        fontSize = 11.sp,
                        color = WorkCircleVerifiedGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 4. Job Listings Cards
            items(filteredJobs) { job ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("job_card_${job.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                UserAvatar(initials = job.company.take(2).uppercase(), sizeDp = 42)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = job.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkCircleNavy
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = job.company,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WorkCircleBlue
                                        )
                                        Text(text = " • ", fontSize = 11.sp, color = WorkCircleSecondaryText)
                                        Text(
                                            text = job.city,
                                            fontSize = 11.sp,
                                            color = WorkCircleSecondaryText
                                        )
                                        Text(text = " • ", fontSize = 11.sp, color = WorkCircleSecondaryText)
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = job.jobType,
                                                fontSize = 10.sp,
                                                color = WorkCircleNavy,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Bookmark Toggle
                            IconButton(
                                onClick = { onSaveToggle(job.id, job.isSaved) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (job.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save Job",
                                    tint = if (job.isSaved) WorkCircleBlue else WorkCircleSecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Transparent Salary & Experience Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = WorkCircleVerifiedGreen, modifier = Modifier.size(13.dp))
                                    Text(
                                        text = job.salaryRange,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkCircleVerifiedGreen
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Exp: ${job.experience}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = WorkCircleNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description
                        Text(
                            text = job.description,
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText,
                            lineHeight = 17.sp,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Requirements tags
                        Text(
                            text = "Requirements: ${job.requirements}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = WorkCircleNavy
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Verified Referrer Info Box
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Referral Available by ${job.referrerName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkCircleNavy
                                    )
                                    Text(
                                        text = job.referrerRole,
                                        fontSize = 10.sp,
                                        color = WorkCircleSecondaryText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Request Referral & Quick Apply
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    referralJobTarget = job
                                    referralPitchNote = "I have strong experience matching this role's requirements. Would appreciate your referral!"
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Handshake, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask Referral", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkCircleBlue)
                            }

                            Button(
                                onClick = { onApplyToggle(job.id, job.isApplied) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (job.isApplied) WorkCircleVerifiedGreen else WorkCircleNavy
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (job.isApplied) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Applied", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Quick Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Post a Referral / Job Opportunity FAB
        FloatingActionButton(
            onClick = { showPostJobDialog = true },
            containerColor = WorkCircleNavy,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("post_job_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post Referral Opening")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Post Referral Role", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Referral Request Dialog
    if (referralJobTarget != null) {
        val target = referralJobTarget!!
        AlertDialog(
            onDismissRequest = { referralJobTarget = null },
            title = {
                Text(
                    text = "Request Referral for ${target.title}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your request will be sent directly to ${target.referrerName} (${target.referrerRole} @ ${target.company}).",
                        fontSize = 12.sp,
                        color = WorkCircleSecondaryText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = referralPitchNote,
                        onValueChange = { referralPitchNote = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp),
                        label = { Text("Intro Note & Resume Summary", fontSize = 11.sp) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestReferral(target.id, referralPitchNote)
                        referralJobTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue)
                ) {
                    Text("Send Referral Pitch", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { referralJobTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Post a Job Dialog
    if (showPostJobDialog) {
        var postTitle by remember { mutableStateOf("") }
        var postCompany by remember { mutableStateOf("") }
        var postCity by remember { mutableStateOf("Bengaluru") }
        var postSalary by remember { mutableStateOf("₹35L - ₹50L CTC") }
        var postExp by remember { mutableStateOf("4-7 Years") }
        var postDept by remember { mutableStateOf("Engineering") }
        var postType by remember { mutableStateOf("Hybrid") }
        var postDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPostJobDialog = false },
            title = { Text("Post Referral Role for Colleagues", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                        label = { Text("Job Title", fontSize = 11.sp) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = postCompany,
                        onValueChange = { postCompany = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                        label = { Text("Company Name", fontSize = 11.sp) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = postCity,
                            onValueChange = { postCity = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                            label = { Text("City", fontSize = 11.sp) },
                            colors = colleagueXTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = postSalary,
                            onValueChange = { postSalary = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                            label = { Text("Salary Band", fontSize = 11.sp) },
                            colors = colleagueXTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = postDesc,
                        onValueChange = { postDesc = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                        label = { Text("Role Description & Requirements", fontSize = 11.sp) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postTitle.isNotBlank() && postCompany.isNotBlank()) {
                            onPostJob(
                                postTitle, postCompany, postCity, "INDIA", postExp, postSalary, postType, postDept,
                                postDesc.ifBlank { "Exciting role with internal team referral available." },
                                "Strong problem solving, teamwork, relevant tech experience."
                            )
                            showPostJobDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleNavy)
                ) {
                    Text("Post Role", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostJobDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
