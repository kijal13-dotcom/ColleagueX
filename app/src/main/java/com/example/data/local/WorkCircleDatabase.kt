package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        UserProfileEntity::class,
        CommunityEntity::class,
        PostEntity::class,
        CommentEntity::class,
        MessageEntity::class,
        NotificationEntity::class,
        VerificationRequestEntity::class,
        ReportEntity::class,
        ArticleEntity::class,
        AuditLogEntity::class,
        UserConnectionEntity::class,
        SkillEndorsementEntity::class,
        JobEntity::class,
        EventEntity::class,
        MentorEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class WorkCircleDatabase : RoomDatabase() {

    abstract fun workCircleDao(): WorkCircleDao

    companion object {
        @Volatile
        private var INSTANCE: WorkCircleDatabase? = null

        fun getInstance(context: Context): WorkCircleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkCircleDatabase::class.java,
                    "workcircle_platform.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).workCircleDao()
                                dao.insertUsers(SampleData.getInitialUsers())
                                dao.insertUserProfile(
                                    UserProfileEntity(
                                        id = 1,
                                        fullName = "Alex Chen",
                                        bio = "Staff Product Manager specializing in AI workflows, developer velocity, and building transparent engineering cultures.",
                                        jobTitle = "Staff Product Manager",
                                        companyName = "Google",
                                        headline = "Staff Product Manager • AI Platform",
                                        location = "Bengaluru, India",
                                        skills = "Product Strategy, AI Systems, Cross-Functional Leadership",
                                        avatarInitials = "AC",
                                        isVerified = true
                                    )
                                )
                                dao.insertCommunities(SampleData.getInitialCommunities())
                                dao.insertPosts(SampleData.getInitialPosts())
                                dao.insertComments(SampleData.getInitialComments())
                                dao.insertArticles(SampleData.getInitialArticles())
                                dao.insertNotifications(SampleData.getInitialNotifications())
                                dao.insertVerificationRequests(SampleData.getInitialVerificationRequests())
                                dao.insertReports(SampleData.getInitialReports())
                                dao.insertMessages(SampleData.getInitialMessages())
                                dao.insertConnections(SampleData.getInitialConnections())
                                dao.insertEndorsements(SampleData.getInitialEndorsements())
                                dao.insertJobs(SampleData.getInitialJobs())
                                dao.insertEvents(SampleData.getInitialEvents())
                                dao.insertMentors(SampleData.getInitialMentors())
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
