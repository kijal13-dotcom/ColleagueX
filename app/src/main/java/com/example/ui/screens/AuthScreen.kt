package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthState
import com.example.ui.theme.ColleagueXInputTextStyle
import com.example.ui.theme.colleagueXTextFieldColors
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleRedDestructive
import com.example.ui.theme.WorkCircleVerifiedGreen

@Composable
fun AuthScreen(
    authState: AuthState,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onSignInWithEmail: (String, String) -> Unit,
    onSignUpWithEmail: (String, String, String, String, String, String) -> Unit,
    onSignInWithGoogle: (Activity) -> Unit,
    onSendPasswordReset: (String) -> Unit,
    onContinueAsGuest: () -> Unit,
    onDemoLogin: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Create Account

    // Input fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Additional Sign-up fields
    var fullName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var selectedIndustry by remember { mutableStateOf("Technology & AI") }

    // Dialog state
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var showConfigInfo by remember { mutableStateOf(false) }

    val industries = listOf("Technology & AI", "Finance & Banking", "Consulting", "Healthcare", "Leadership")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo and Branding Header
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CX",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "ColleagueX",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = WorkCircleNavy
        )

        Text(
            text = "The Verified Professional Network",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = WorkCircleBlue
        )

        Text(
            text = "Connect with peers, share workplace insights, and explore anonymous industry discussions.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Error Banner
        AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
            errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = WorkCircleRedDestructive,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onClearError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("✕", fontSize = 12.sp, color = Color(0xFF991B1B))
                        }
                    }
                }
            }
        }

        // Success Banner
        AnimatedVisibility(visible = !successMessage.isNullOrBlank()) {
            successMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WorkCircleVerifiedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Authentication Card Container
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Tab Row for Sign In / Sign Up
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = WorkCircleBlue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .padding(bottom = 16.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Log In",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Create Account",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                }

                // SIGN UP EXTRA FIELDS
                if (selectedTab == 1) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Sarah Jenkins") },
                        textStyle = ColleagueXInputTextStyle,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = WorkCircleBlue)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("signup_fullname_input"),
                        colors = colleagueXTextFieldColors()
                    )

                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text("Current Role / Title") },
                        placeholder = { Text("e.g. Senior Software Engineer") },
                        textStyle = ColleagueXInputTextStyle,
                        leadingIcon = {
                            Icon(Icons.Default.Work, contentDescription = null, tint = WorkCircleBlue)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("signup_role_input"),
                        colors = colleagueXTextFieldColors()
                    )

                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company / Organization") },
                        placeholder = { Text("e.g. Stripe, Bain, or Independent") },
                        textStyle = ColleagueXInputTextStyle,
                        leadingIcon = {
                            Icon(Icons.Default.Business, contentDescription = null, tint = WorkCircleBlue)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("signup_company_input"),
                        colors = colleagueXTextFieldColors()
                    )

                    Text(
                        text = "Primary Industry",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkCircleNavy,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        industries.take(3).forEach { ind ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedIndustry == ind) WorkCircleBlue else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedIndustry = ind }
                            ) {
                                Text(
                                    text = ind.substringBefore(" &"),
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedIndustry == ind) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedIndustry == ind) Color.White else WorkCircleNavy,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // EMAIL FIELD
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (selectedTab == 0) "Email Address" else "Work or Personal Email") },
                    placeholder = { Text("name@example.com") },
                    textStyle = ColleagueXInputTextStyle,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = WorkCircleBlue)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("email_input"),
                    colors = colleagueXTextFieldColors()
                )

                // PASSWORD FIELD
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("••••••••") },
                    textStyle = ColleagueXInputTextStyle,
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = WorkCircleBlue)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("password_visibility_toggle")
                        ) {
                            val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            Icon(
                                imageVector = icon,
                                contentDescription = description,
                                tint = if (passwordVisible) WorkCircleBlue else Color(0xFF64748B)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (passwordVisible) KeyboardType.Text else KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (selectedTab == 0) {
                                onSignInWithEmail(email, password)
                            } else {
                                onSignUpWithEmail(email, password, fullName, jobTitle, company, selectedIndustry)
                            }
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (selectedTab == 0) 4.dp else 12.dp)
                        .testTag("password_input"),
                    colors = colleagueXTextFieldColors()
                )

                if (selectedTab == 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot password?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WorkCircleBlue,
                            modifier = Modifier
                                .clickable {
                                    resetEmail = email
                                    showResetDialog = true
                                }
                                .padding(4.dp)
                        )
                    }
                }

                // PRIMARY ACTION BUTTON (Email Sign In / Sign Up)
                Button(
                    onClick = {
                        if (selectedTab == 0) {
                            onSignInWithEmail(email, password)
                        } else {
                            onSignUpWithEmail(email, password, fullName, jobTitle, company, selectedIndustry)
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.length >= 6,
                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (selectedTab == 0) "Log In to ColleagueX" else "Create Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // DIVIDER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = WorkCircleCardBorder)
                    Text(
                        text = "  OR  ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = WorkCircleCardBorder)
                }

                // GOOGLE SIGN IN BUTTON (Using Credential Manager)
                Surface(
                    onClick = {
                        if (context is Activity) {
                            onSignInWithGoogle(context)
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_signin_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google "G" Badge
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF3C4043)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // DEMO ACCOUNT / QUICK LOGIN FOR TESTING
                OutlinedButton(
                    onClick = onDemoLogin,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("demo_login_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = WorkCircleBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Demo Login (Pre-configured)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkCircleBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // GUEST EXPLORE BUTTON
                TextButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Skip and browse as Guest →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Firebase Setup & Architecture Info Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, WorkCircleCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showConfigInfo = !showConfigInfo }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WorkCircleBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Firebase Auth & Google Sign-In Architecture",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (showConfigInfo) "Hide" else "Details",
                        fontSize = 11.sp,
                        color = WorkCircleBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (showConfigInfo) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Firebase Auth SDK: Email/Password & Google Provider integrated\n" +
                                "• Android Credential Manager: One-tap Google Sign-In with GoogleIdTokenCredential\n" +
                                "• Local Persistence: Authenticated accounts automatically synchronize with the Room database\n" +
                                "• Production Config: Place your google-services.json in /app/ and set Web Client ID from Google Cloud Console.",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // Password Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Password",
                    fontWeight = FontWeight.Bold,
                    color = WorkCircleNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your account email address. We'll send you a password reset link via Firebase Auth.",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Account Email") },
                        placeholder = { Text("name@example.com") },
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
                        if (resetEmail.isNotBlank()) {
                            onSendPasswordReset(resetEmail)
                            showResetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkCircleBlue)
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
