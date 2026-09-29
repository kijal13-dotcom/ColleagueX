package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.SampleData
import com.example.ui.components.PostCard
import com.example.ui.theme.WorkCircleTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun post_card_screenshot() {
    val samplePost = SampleData.getInitialPosts().first()
    composeTestRule.setContent {
      WorkCircleTheme {
        PostCard(
          post = samplePost,
          onPostClick = {},
          onLikeClick = {},
          onSaveClick = {},
          onCommentClick = {},
          onVoteOption = {},
          onReportClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/postcard.png")
  }
}
