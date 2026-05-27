package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Chapters for Syllabus Tracker
@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String, // "Physics", "Chemistry", "Mathematics"
    val category: String, // e.g. "Mechanics", "Calculus", "Organic"
    val name: String,
    val status: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED", "REVISED"
    val isWeak: Boolean = false
)

// 2. Study Session Tracking
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val chapterName: String,
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val isPomodoro: Boolean = false,
    val note: String = ""
)

// 3. Mock Test Tracker
@Entity(tableName = "mock_tests")
data class MockTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val physicsScore: Int,
    val chemistryScore: Int,
    val mathsScore: Int,
    val totalScore: Int,
    val maxScore: Int = 300,
    val percentile: Double = 0.0,
    val rank: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

// 4. Daily & Weekly Goals
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetHours: Double = 0.0, // if simple hourly goal, otherwise action task
    val isCompleted: Boolean = false,
    val type: String = "DAILY", // "DAILY", "WEEKLY", "REVISION"
    val timestamp: Long = System.currentTimeMillis()
)

// 5. Config / User Profile Preferences Store (Key-Value)
@Entity(tableName = "preferences")
data class PreferenceEntity(
    @PrimaryKey val key: String,
    val value: String
)

// --- DAOs ---

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY subject, name")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT COUNT(*) FROM chapters")
    suspend fun getChapterCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("UPDATE chapters SET status = :status WHERE id = :id")
    suspend fun updateChapterStatus(id: Int, status: String)

    @Query("UPDATE chapters SET isWeak = :isWeak WHERE id = :id")
    suspend fun updateChapterWeakStatus(id: Int, isWeak: Boolean)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getSessionsSince(sinceTimestamp: Long): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSession(id: Int)
}

@Dao
interface MockTestDao {
    @Query("SELECT * FROM mock_tests ORDER BY timestamp DESC")
    fun getAllTests(): Flow<List<MockTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: MockTestEntity)

    @Query("DELETE FROM mock_tests WHERE id = :id")
    suspend fun deleteTest(id: Int)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY timestamp DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("UPDATE goals SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateGoalStatus(id: Int, isCompleted: Boolean)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Int)
}

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM preferences")
    fun getAllPreferences(): Flow<List<PreferenceEntity>>

    @Query("SELECT value FROM preferences WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Query("SELECT value FROM preferences WHERE `key` = :key LIMIT 1")
    fun observeValue(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(pref: PreferenceEntity)

    @Query("DELETE FROM preferences WHERE `key` = :key")
    suspend fun deleteKey(key: String)
}

// --- Database Class ---

@Database(
    entities = [
        ChapterEntity::class,
        StudySessionEntity::class,
        MockTestEntity::class,
        GoalEntity::class,
        PreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun mockTestDao(): MockTestDao
    abstract fun goalDao(): GoalDao
    abstract fun preferenceDao(): PreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jee_tracker_pro_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
