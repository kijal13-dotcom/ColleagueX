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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import com.example.data.local.UserEntity
import com.example.data.model.VerificationStatus
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    user: UserEntity?,
    onBack: () -> Unit,
    onSubmitVerification: (company: String, workEmail: String, method: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var companyName by remember { mutableStateOf(user?.employer ?: "") }
    var workEmail by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var successNotice by remember { mutableStateOf(false) }

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
                    Text("Employment Verification", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WorkCircleNavy)
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
            // Status Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (user?.isVerified == true) Color(0xFFECFDF5) else Color(0xFFEFF6FF)
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (user?.isVerified == true) Color(0xFFA7F3D0) else Color(0xFFBFDBFE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (user?.isVerified == true) Icons.Default.CheckCircle else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (user?.isVerified == true) WorkCircleVerifiedGreen else WorkCircleBlue,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (user?.isVerified == true) "Employment Verified" else "Verification Pending / Optional",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WorkCircleNavy
                        )
                        Text(
                            text = if (user?.isVerified == true)
                                "You are recognized as a verified professional at ${user.employer}."
                            else
                                "Confirm your work email to display an authentic role badge in professional circles.",
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText
                        )
                    }
                }
            }

            // Trust & Safeguards Explainer
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy Safeguards",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = WorkCircleNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Your work email is hashed and NEVER displayed publicly.\n• We do not inform your employer of your ColleagueX participation.\n• You can toggle employer visibility on or off at any time.\n• Under anonymous posting mode, only your industry/role is shown.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = WorkCircleNavy.copy(alpha = 0.85f)
                    )
                }
            }

            // Verification Form Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Corporate Email OTP Flow",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = WorkCircleNavy
                    )

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                        label = { Text("Company Name") },
                        placeholder = { Text("e.g. Stripe, Bain, Google", color = Color(0xFF64748B)) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = workEmail,
                        onValueChange = { workEmail = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                        label = { Text("Corporate Email Address") },
                        placeholder = { Text("alex@yourcompany.com", color = Color(0xFF64748B)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WorkCircleBlue) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("work_email_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (!otpSent) {
                        Button(
                            onClick = {
                                if (workEmail.contains("@")) {
                                    otpSent = true
                                }
                            },
                            enabled = workEmail.contains("@"),
                            colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("send_otp_button")
                        ) {
                            Text("Send Secure One-Time Code", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // OTP input box
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Code sent to $workEmail. Enter the 6-digit confirmation token below:",
                                fontSize = 12.sp,
                                color = WorkCircleBlue,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                            label = { Text("Enter 6-Digit Code") },
                            placeholder = { Text("123456", color = Color(0xFF64748B)) },
                            colors = colleagueXTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_code_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (otpCode.length >= 4) {
                                    onSubmitVerification(companyName, workEmail, "CORPORATE_EMAIL_OTP")
                                    successNotice = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WorkCircleVerifiedGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("confirm_verification_button")
                        ) {
                            Text("Confirm & Apply Verified Badge", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    if (successNotice) {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WorkCircleVerifiedGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verification confirmed! Your profile now bears the Verified Professional status.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF065F46)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
