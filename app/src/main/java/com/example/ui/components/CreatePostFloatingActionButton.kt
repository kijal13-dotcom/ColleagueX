package com.example.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WorkCircleBlue

/**
 * Floating Action Button dedicated to triggering the 'Create Post' flow,
 * saving workplace knowledge posts directly into the local Room database.
 */
@Composable
fun CreatePostFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true,
    text: String = "Share Knowledge"
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = "Create new workplace knowledge post",
                tint = Color.White
            )
        },
        text = {
            if (isExpanded) {
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        containerColor = WorkCircleBlue,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.testTag("create_post_fab")
    )
}

/**
 * Floating Action Button dedicated to triggering the 'Create Post' flow
 * for saving text-based community posts directly into Firebase Firestore.
 */
@Composable
fun FirestoreCreatePostFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true,
    text: String = "New Cloud Post"
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create new Firestore post",
                tint = Color.White
            )
        },
        text = {
            if (isExpanded) {
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        containerColor = WorkCircleBlue,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .testTag("firestore_create_post_fab")
            .testTag("create_post_fab")
    )
}
