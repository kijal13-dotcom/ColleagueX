package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommunityEntity
import com.example.data.local.UserEntity
import com.example.data.model.PostType
import com.example.ui.components.ConfidentialityWarningBanner
import com.example.ui.components.CreatePostComponent
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleNavy

/**
 * Screen allowing users to create new workplace knowledge posts saved to Room storage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    communities: List<CommunityEntity>,
    onBack: () -> Unit,
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
    currentUser: UserEntity? = null,
    modifier: Modifier = Modifier
) {
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
                    Text("New Knowledge Post", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WorkCircleNavy)
                }
            )
        },
        containerColor = WorkCircleBackground,
        modifier = modifier.testTag("create_post_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ConfidentialityWarningBanner()

            CreatePostComponent(
                communities = communities,
                currentUser = currentUser,
                onSubmit = onSubmit,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
