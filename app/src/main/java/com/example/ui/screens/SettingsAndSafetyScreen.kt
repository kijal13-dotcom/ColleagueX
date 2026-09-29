package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.theme.colleagueXTextFieldColors
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
import com.example.ui.components.ConfidentialityWarningBanner
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndSafetyScreen(
    onBack: () -> Unit,
    onSubmitTicket: (subject: String, description: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var ticketSubject by remember { mutableStateOf("") }
    var ticketDescription by remember { mutableStateOf("") }
    var ticketSubmitted by remember { mutableStateOf(false) }

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
                    Text("Safety & Guidelines", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WorkCircleNavy)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ConfidentialityWarningBanner()

            // Community Guidelines Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Policy, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ColleagueX Core Principles",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WorkCircleNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1. Connect Beyond Silos: Learn from colleagues across companies, industries, and locations without corporate borders.\n\n" +
                            "2. Zero Disclosure of Confidential Material: Never publish customer data, trade secrets, non-public financials, passwords, or internal proprietary docs.\n\n" +
                            "3. Protected Anonymity: Anonymous posting empowers genuine questions on compensation, culture, or career hurdles while upholding safety.\n\n" +
                            "4. Constructive Professional Courtesy: Harassment, personal attacks, or aggressive behavior will result in swift removal.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = WorkCircleNavy.copy(alpha = 0.9f)
                    )
                }
            }

            // FAQ Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = WorkCircleNavy, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Frequently Asked Questions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WorkCircleNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Can my current employer see what I post anonymously?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WorkCircleNavy)
                    Text("No. When anonymous mode is toggled, your name, profile photo, and company name are hidden from all other users.", fontSize = 12.sp, color = WorkCircleSecondaryText)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("How does employment verification work?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WorkCircleNavy)
                    Text("You submit an OTP token sent to your work domain email. We confirm the DNS record and grant a verified badge. Your work email is never made public.", fontSize = 12.sp, color = WorkCircleSecondaryText)
                }
            }

            // Contact & Support Ticket Form
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contact Support & Safety Team",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WorkCircleNavy
                        )
                    }

                    OutlinedTextField(
                        value = ticketSubject,
                        onValueChange = { ticketSubject = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                        label = { Text("Inquiry Subject") },
                        placeholder = { Text("e.g. Verification question, policy query", color = Color(0xFF64748B)) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = ticketDescription,
                        onValueChange = { ticketDescription = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                        label = { Text("Description") },
                        placeholder = { Text("Please describe your question or issue in detail...", color = Color(0xFF64748B)) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (ticketSubject.isNotBlank() && ticketDescription.isNotBlank()) {
                                onSubmitTicket(ticketSubject.trim(), ticketDescription.trim())
                                ticketSubmitted = true
                                ticketSubject = ""
                                ticketDescription = ""
                            }
                        },
                        enabled = ticketSubject.isNotBlank() && ticketDescription.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("submit_support_ticket_button")
                    ) {
                        Text("Submit Support Ticket", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (ticketSubmitted) {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Ticket submitted successfully. Our safety & support team will respond via notifications.",
                                fontSize = 12.sp,
                                color = WorkCircleVerifiedGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // About Application & Owners Info
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_application_owners_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = WorkCircleBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "About ColleagueX & Application Owners",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = WorkCircleNavy
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = "v1.0.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkCircleBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ColleagueX is a trusted community platform connecting professionals across local cities, nationwide industries, and global employee circles.",
                        fontSize = 12.sp,
                        color = WorkCircleSecondaryText,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Application Owner", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Kijal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkCircleNavy)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Owner",
                                        tint = WorkCircleVerifiedGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Owner Email / Inquiries", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                Text(
                                    text = "kijal13@gmail.com",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = WorkCircleBlue
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Role & Ownership", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                Text(
                                    text = "Product Owner & Principal Architect",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = WorkCircleNavy
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Engineering Platform", fontSize = 12.sp, color = WorkCircleSecondaryText)
                                Text(
                                    text = "ColleagueX Core Systems",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = WorkCircleNavy
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
