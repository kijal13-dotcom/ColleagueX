package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SampleData
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ColleagueX", appName)
  }

  @Test
  fun `sample data integrity check`() {
    val users = SampleData.getInitialUsers()
    val communities = SampleData.getInitialCommunities()
    val posts = SampleData.getInitialPosts()
    val articles = SampleData.getInitialArticles()

    assertTrue(users.isNotEmpty())
    assertTrue(communities.isNotEmpty())
    assertTrue(posts.isNotEmpty())
    assertTrue(articles.isNotEmpty())

    val admin = users.find { it.roleType == UserRole.PLATFORM_ADMIN }
    assertNotNull(admin)
  }
}
