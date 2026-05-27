package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.max

class JEETrackerRepository(
    private val chapterDao: ChapterDao,
    private val studySessionDao: StudySessionDao,
    private val mockTestDao: MockTestDao,
    private val goalDao: GoalDao,
    private val preferenceDao: PreferenceDao
) {
    // Flows
    val allChapters: Flow<List<ChapterEntity>> = chapterDao.getAllChapters()
    val allSessions: Flow<List<StudySessionEntity>> = studySessionDao.getAllSessions()
    val allTests: Flow<List<MockTestEntity>> = mockTestDao.getAllTests()
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    val allPreferences: Flow<List<PreferenceEntity>> = preferenceDao.getAllPreferences()

    // Chapter Operations
    suspend fun updateChapter(chapter: ChapterEntity) = chapterDao.updateChapter(chapter)
    suspend fun updateChapterStatus(id: Int, status: String) = chapterDao.updateChapterStatus(id, status)
    suspend fun updateChapterWeakStatus(id: Int, isWeak: Boolean) = chapterDao.updateChapterWeakStatus(id, isWeak)

    // Session Operations
    suspend fun insertSession(session: StudySessionEntity) {
        studySessionDao.insertSession(session)
        // Add XP reward: e.g. 1 XP per minute studied
        val minutes = max(1L, session.durationSeconds / 60)
        addXP((minutes * 2).toInt()) // 2 XP per minute of study!
        
        // Auto update streak
        updateStreak()
    }
    suspend fun deleteSession(id: Int) = studySessionDao.deleteSession(id)

    // Test Operations
    suspend fun insertTest(test: MockTestEntity) {
        mockTestDao.insertTest(test)
        addXP(150) // 150 XP for writing a complete Mock Test!
    }
    suspend fun deleteTest(id: Int) = mockTestDao.deleteTest(id)

    // Goal Operations
    suspend fun insertGoal(goal: GoalEntity) = goalDao.insertGoal(goal)
    suspend fun updateGoalStatus(id: Int, isCompleted: Boolean) {
        goalDao.updateGoalStatus(id, isCompleted)
        if (isCompleted) {
            addXP(30) // 30 XP for completing a goal
        }
    }
    suspend fun deleteGoal(id: Int) = goalDao.deleteGoal(id)

    // Preferences Operations
    suspend fun getPreference(key: String, defaultValue: String): String {
        return preferenceDao.getValue(key) ?: defaultValue
    }
    suspend fun setPreference(key: String, value: String) {
        preferenceDao.insertPreference(PreferenceEntity(key, value))
    }
    fun observePreference(key: String): Flow<String?> = preferenceDao.observeValue(key)

    // Gamification Helpers
    private suspend fun addXP(xpAmount: Int) {
        val currentXp = getPreference("user_xp", "0").toIntOrNull() ?: 0
        val currentLevel = getPreference("user_level", "1").toIntOrNull() ?: 1
        
        var newXp = currentXp + xpAmount
        var newLevel = currentLevel
        
        // Dynamic formula: level cost = level * 200 XP
        while (newXp >= newLevel * 200) {
            newXp -= (newLevel * 200)
            newLevel++
        }
        
        setPreference("user_xp", newXp.toString())
        setPreference("user_level", newLevel.toString())
    }

    private suspend fun updateStreak() {
        val todayStr = getTodayString()
        val lastStudyDay = getPreference("last_study_day", "")
        
        if (lastStudyDay == todayStr) {
            return // already updated streak today
        }
        
        val yesterdayStr = getYesterdayString()
        val currentStreak = getPreference("user_streak", "0").toIntOrNull() ?: 0
        
        val newStreak = if (lastStudyDay == yesterdayStr) {
            currentStreak + 1
        } else if (lastStudyDay.isEmpty() || lastStudyDay != todayStr) {
            1 // Reset streak to 1 if yesterday was missed, or first launch
        } else {
            currentStreak
        }
        
        setPreference("user_streak", newStreak.toString())
        setPreference("last_study_day", todayStr)
    }

    private fun getTodayString(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return formatter.format(java.util.Date())
    }

    private fun getYesterdayString(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DATE, -1)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return formatter.format(cal.time)
    }

    // Populate standard syllabus checklist
    suspend fun checkAndPopulateSyllabus() {
        if (chapterDao.getChapterCount() > 0) return

        val initialChapters = listOf(
            // PHYSICS
            ChapterEntity(subject = "Physics", category = "Mechanics", name = "Units, Dimensions & Errors"),
            ChapterEntity(subject = "Physics", category = "Mechanics", name = "Kinematics (1D & 2D Motion)"),
            ChapterEntity(subject = "Physics", category = "Mechanics", name = "Laws of Motion & Friction"),
            ChapterEntity(subject = "Physics", category = "Mechanics", name = "Work, Energy & Power"),
            ChapterEntity(subject = "Physics", category = "Mechanics", name = "Center of Mass & Collision"),
            ChapterEntity(subject = "Physics", category = "Mechanics", name = "Rotational Dynamics"),
            ChapterEntity(subject = "Physics", category = "Mechanics", name = "Gravitation"),
            ChapterEntity(subject = "Physics", category = "Mechanics", name = "Mechanical Properties of Solids & Fluids"),
            ChapterEntity(subject = "Physics", category = "Thermodynamics", name = "Thermal Properties & Calorimetry"),
            ChapterEntity(subject = "Physics", category = "Thermodynamics", name = "Thermodynamics"),
            ChapterEntity(subject = "Physics", category = "Thermodynamics", name = "Kinetic Theory of Gases"),
            ChapterEntity(subject = "Physics", category = "Electrostatics & Magnetism", name = "Electrostatics & Capacitance"),
            ChapterEntity(subject = "Physics", category = "Electrostatics & Magnetism", name = "Current Electricity"),
            ChapterEntity(subject = "Physics", category = "Electrostatics & Magnetism", name = "Magnetic Effects of Current & Magnetism"),
            ChapterEntity(subject = "Physics", category = "Electrostatics & Magnetism", name = "Electromagnetic Induction & AC"),
            ChapterEntity(subject = "Physics", category = "Optics & Modern Physics", name = "Electromagnetic Waves & Wave Optics"),
            ChapterEntity(subject = "Physics", category = "Optics & Modern Physics", name = "Ray Optics"),
            ChapterEntity(subject = "Physics", category = "Optics & Modern Physics", name = "Dual Nature of Matter & Radiation"),
            ChapterEntity(subject = "Physics", category = "Optics & Modern Physics", name = "Atoms & Nuclei"),
            ChapterEntity(subject = "Physics", category = "Optics & Modern Physics", name = "Electronic Devices (Semiconductors)"),

            // CHEMISTRY
            ChapterEntity(subject = "Chemistry", category = "Physical Chemistry", name = "Some Basic Concepts of Chemistry"),
            ChapterEntity(subject = "Chemistry", category = "Physical Chemistry", name = "Atomic Structure"),
            ChapterEntity(subject = "Chemistry", category = "Physical Chemistry", name = "Chemical Bonding & Molecular Structure"),
            ChapterEntity(subject = "Chemistry", category = "Physical Chemistry", name = "Chemical Thermodynamics"),
            ChapterEntity(subject = "Chemistry", category = "Physical Chemistry", name = "Equilibrium (Chemical & Ionic)"),
            ChapterEntity(subject = "Chemistry", category = "Physical Chemistry", name = "Redox Reactions & Electrochemistry"),
            ChapterEntity(subject = "Chemistry", category = "Physical Chemistry", name = "Chemical Kinetics"),
            ChapterEntity(subject = "Chemistry", category = "Physical Chemistry", name = "Solutions"),
            ChapterEntity(subject = "Chemistry", category = "Inorganic Chemistry", name = "Periodic Table & Classification"),
            ChapterEntity(subject = "Chemistry", category = "Inorganic Chemistry", name = "p-Block Elements"),
            ChapterEntity(subject = "Chemistry", category = "Inorganic Chemistry", name = "d and f Block Elements"),
            ChapterEntity(subject = "Chemistry", category = "Inorganic Chemistry", name = "Coordination Compounds"),
            ChapterEntity(subject = "Chemistry", category = "Organic Chemistry", name = "Purification & Basic Organic Chemistry (GOC)"),
            ChapterEntity(subject = "Chemistry", category = "Organic Chemistry", name = "Hydrocarbons"),
            ChapterEntity(subject = "Chemistry", category = "Organic Chemistry", name = "Haloalkanes & Haloarenes"),
            ChapterEntity(subject = "Chemistry", category = "Organic Chemistry", name = "Alcohols, Phenols & Ethers"),
            ChapterEntity(subject = "Chemistry", category = "Organic Chemistry", name = "Aldehydes, Ketones & Carboxylic Acids"),
            ChapterEntity(subject = "Chemistry", category = "Organic Chemistry", name = "Organic Compounds Containing Nitrogen (Amines)"),
            ChapterEntity(subject = "Chemistry", category = "Organic Chemistry", name = "Biomolecules & Polymers"),

            // MATHEMATICS
            ChapterEntity(subject = "Mathematics", category = "Algebra", name = "Sets, Relations & Functions"),
            ChapterEntity(subject = "Mathematics", category = "Algebra", name = "Complex Numbers & Quadratic Equations"),
            ChapterEntity(subject = "Mathematics", category = "Algebra", name = "Matrices & Determinants"),
            ChapterEntity(subject = "Mathematics", category = "Algebra", name = "Permutations & Combinations"),
            ChapterEntity(subject = "Mathematics", category = "Algebra", name = "Mathematical Induction & Binomial Theorem"),
            ChapterEntity(subject = "Mathematics", category = "Algebra", name = "Sequences & Series (AP, GP, etc.)"),
            ChapterEntity(subject = "Mathematics", category = "Algebra", name = "Probability"),
            ChapterEntity(subject = "Mathematics", category = "Calculus", name = "Limits, Continuity & Differentiability"),
            ChapterEntity(subject = "Mathematics", category = "Calculus", name = "Application of Derivatives"),
            ChapterEntity(subject = "Mathematics", category = "Calculus", name = "Indefinite & Definite Integrals"),
            ChapterEntity(subject = "Mathematics", category = "Calculus", name = "Differential Equations"),
            ChapterEntity(subject = "Mathematics", category = "Coordinate Geometry", name = "Straight Lines"),
            ChapterEntity(subject = "Mathematics", category = "Coordinate Geometry", name = "Circles"),
            ChapterEntity(subject = "Mathematics", category = "Coordinate Geometry", name = "Conic Sections (Parabola, Ellipse, Hyperbola)"),
            ChapterEntity(subject = "Mathematics", category = "Trigonometry & Vectors", name = "Trigonometry (Ratios, Identities, Equations)"),
            ChapterEntity(subject = "Mathematics", category = "Trigonometry & Vectors", name = "Three Dimensional Geometry"),
            ChapterEntity(subject = "Mathematics", category = "Trigonometry & Vectors", name = "Vector Algebra"),
            ChapterEntity(subject = "Mathematics", category = "Trigonometry & Vectors", name = "Statistics & Mathematical Reasoning")
        )

        chapterDao.insertChapters(initialChapters)
    }
}
