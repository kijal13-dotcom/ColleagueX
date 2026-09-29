package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.AuditLogEntity
import com.example.data.local.ReportEntity
import com.example.data.local.VerificationRequestEntity
import com.example.data.model.ModerationStatus
import com.example.data.model.VerificationStatus
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleRedDestructive
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    reports: List<ReportEntity>,
    verifications: List<VerificationRequestEntity>,
    auditLogs: List<AuditLogEntity>,
    totalUsersCount: Int,
    totalPostsCount: Int,
    onBack: () -> Unit,
    onModerateReport: (reportId: Long, resolution: String) -> Unit,
    onReviewVerification: (requestId: Long, userId: Long, status: VerificationStatus, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Metrics & Overview", "Reports Queue", "Verifications", "Audit Log")

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WorkCircleNavy)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = WorkCircleRedDestructive, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Platform Administration", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WorkCircleNavy)
                    }
                }
            )
        },
        containerColor = WorkCircleBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Admin Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = WorkCircleBlue,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = WorkCircleBlue,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tab,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == index) WorkCircleBlue else WorkCircleSecondaryText
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    // Metrics & Overview
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text("Real-Time Community Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WorkCircleNavy)
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                MetricCard(title = "Registered Users", value = "$totalUsersCount", icon = Icons.Default.AdminPanelSettings, modifier = Modifier.weight(1f))
                                MetricCard(title = "Discussions", value = "$totalPostsCount", icon = Icons.Default.Shield, modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                MetricCard(title = "Open Reports", value = "${reports.filter { it.status == "PENDING_REVIEW" }.size}", icon = Icons.Default.Warning, isAlert = true, modifier = Modifier.weight(1f))
                                MetricCard(title = "Pending Verifications", value = "${verifications.filter { it.status == VerificationStatus.PENDING }.size}", icon = Icons.Default.VerifiedUser, modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Platform Health & Safeguards", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkCircleNavy)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Automated trade-secret scanning: ACTIVE", fontSize = 12.sp, color = WorkCircleVerifiedGreen, fontWeight = FontWeight.SemiBold)
                                    Text("• Anonymous user audit tracking: ENABLED", fontSize = 12.sp, color = WorkCircleVerifiedGreen, fontWeight = FontWeight.SemiBold)
                                    Text("• Spam & harassment threshold: STRICT", fontSize = 12.sp, color = WorkCircleBlue, fontWeight = FontWeight.SemiBold)
                                    Text("• Database backup status: 100% HEALTHY", fontSize = 12.sp, color = WorkCircleNavy, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Reports Queue
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Flagged Content Queue (${reports.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WorkCircleNavy)
                        }

                        items(reports, key = { it.id }) { report ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            color = if (report.status == "PENDING_REVIEW") Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = report.reason,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (report.status == "PENDING_REVIEW") WorkCircleRedDestructive else WorkCircleSecondaryText,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = report.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (report.status == "PENDING_REVIEW") WorkCircleRedDestructive else WorkCircleVerifiedGreen
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(report.entityTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkCircleNavy)
                                    Text(report.details, fontSize = 12.sp, color = WorkCircleSecondaryText)
                                    Text("Reported by ${report.reporterName}", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                                    if (report.status == "PENDING_REVIEW") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = { onModerateReport(report.id, "DISMISSED") },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Dismiss", fontSize = 11.sp)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { onModerateReport(report.id, "RESOLVED_REMOVED") },
                                                colors = ButtonDefaults.buttonColors(containerColor = WorkCircleRedDestructive),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Remove Content", fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Verifications Queue
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Employment Verifications (${verifications.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WorkCircleNavy)
                        }

                        items(verifications, key = { it.id }) { req ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(req.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkCircleNavy)
                                        Surface(
                                            color = if (req.status == VerificationStatus.VERIFIED) Color(0xFFECFDF5) else Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = req.status.label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (req.status == VerificationStatus.VERIFIED) WorkCircleVerifiedGreen else Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Company: ${req.companyName}", fontSize = 12.sp, color = WorkCircleNavy)
                                    Text("Work Email: ${req.workEmail}", fontSize = 12.sp, color = WorkCircleBlue)
                                    Text("Notes: ${req.reviewerNotes}", fontSize = 11.sp, color = WorkCircleSecondaryText)

                                    if (req.status == VerificationStatus.PENDING) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = { onReviewVerification(req.id, req.userId, VerificationStatus.REJECTED, "Invalid email domain") },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Reject", fontSize = 11.sp)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { onReviewVerification(req.id, req.userId, VerificationStatus.VERIFIED, "Domain DNS and employee match confirmed") },
                                                colors = ButtonDefaults.buttonColors(containerColor = WorkCircleVerifiedGreen),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Approve Verification", fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Audit Logs
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text("System Audit Trail", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WorkCircleNavy)
                        }

                        items(auditLogs, key = { it.id }) { log ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WorkCircleBlue)
                                        Text(log.actorName, fontSize = 11.sp, color = WorkCircleSecondaryText)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(log.details, fontSize = 12.sp, color = WorkCircleNavy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isAlert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAlert) WorkCircleRedDestructive else WorkCircleBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = if (isAlert) WorkCircleRedDestructive else WorkCircleNavy
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = WorkCircleSecondaryText
            )
        }
    }
}
