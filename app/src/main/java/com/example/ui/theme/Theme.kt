package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.TextStyle

private val DarkColorScheme = darkColorScheme(
    primary = WorkCircleBlueLight,
    onPrimary = Color.White,
    primaryContainer = WorkCircleNavyLight,
    onPrimaryContainer = Color.White,
    secondary = WorkCircleTealAccent,
    onSecondary = Color.White,
    background = WorkCircleBackground,
    surface = WorkCircleSurface,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = WorkCircleBlue,
    onPrimary = Color.White,
    primaryContainer = WorkCircleContainer,
    onPrimaryContainer = WorkCircleNavy,
    secondary = WorkCircleNavy,
    onSecondary = Color.White,
    background = WorkCircleBackground,
    surface = WorkCircleSurface,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = WorkCircleSecondaryText,
    outline = WorkCircleCardBorder
)

@Composable
fun colleagueXTextFieldColors(
    focusedBorderColor: Color = WorkCircleBlue,
    unfocusedBorderColor: Color = WorkCircleCardBorder,
    containerColor: Color = Color.White
) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    cursorColor = Color.Black,
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor,
    focusedLabelColor = WorkCircleBlue,
    unfocusedLabelColor = Color(0xFF4A5568),
    focusedPlaceholderColor = Color(0xFF718096),
    unfocusedPlaceholderColor = Color(0xFF94A3B8),
    focusedLeadingIconColor = WorkCircleBlue,
    unfocusedLeadingIconColor = Color(0xFF718096),
    focusedTrailingIconColor = WorkCircleBlue,
    unfocusedTrailingIconColor = Color(0xFF718096),
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor
)

val ColleagueXInputTextStyle: TextStyle
    @Composable
    get() = LocalTextStyle.current.copy(color = Color.Black)

@Composable
fun WorkCircleTheme(
    darkTheme: Boolean = false, // Maintain crisp, clear ColleagueX corporate light theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun ColleagueXTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    WorkCircleTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    WorkCircleTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

