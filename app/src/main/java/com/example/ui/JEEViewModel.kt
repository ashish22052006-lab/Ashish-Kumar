package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class JEEViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: JEETrackerRepository

    // Initialized Database and Repository
    init {
        val database = AppDatabase.getDatabase(application)
        repository = JEETrackerRepository(
            database.chapterDao(),
            database.studySessionDao(),
            database.mockTestDao(),
            database.goalDao(),
            database.preferenceDao()
        )
        
        // Seed syllabus details on first run
        viewModelScope.launch {
            repository.checkAndPopulateSyllabus()
            loadProfile()
            calculateDaysLeft()
        }
    }

    // --- State Streams ---
    val allChapters = repository.allChapters.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSessions = repository.allSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTests = repository.allTests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allGoals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Profile & Customizations ---
    var username by mutableStateOf("JEE Aspirant")
    var targetCollege by mutableStateOf("IIT Bombay (Computer Science)")
    var targetRank by mutableStateOf("AIR 100")
    var dailyGoalHours by mutableStateOf(8.0)
    var onboardingCompleted by mutableStateOf(false)

    // --- Gamification Sync ---
    val xpFlow = repository.observePreference("user_xp")
        .map { it?.toIntOrNull() ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val levelFlow = repository.observePreference("user_level")
        .map { it?.toIntOrNull() ?: 1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val streakFlow = repository.observePreference("user_streak")
        .map { it?.toIntOrNull() ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Study Timer Engine ---
    var isTimerRunning by mutableStateOf(false)
    var timerSeconds by mutableStateOf(0L)
    var selectedSubjectForTimer by mutableStateOf("Physics")
    var selectedChapterForTimer by mutableStateOf("General Topics")
    var isPomodoroMode by mutableStateOf(false)
    var pomodoroTargetSeconds by mutableStateOf(1500L) // 25 mins initial
    var isDeepWorkMode by mutableStateOf(false)

    private var timerJob: Job? = null

    // --- Countdown Streams ---
    var daysToJEEMain by mutableStateOf(0L)
    var daysToJEEAdvanced by mutableStateOf(0L)

    // --- AI recommendation and mentor states ---
    var aiRecommendationText by mutableStateOf("")
    var isGeneratingRecommendation by mutableStateOf(false)

    var mentorChatMessages = mutableStateListOf<ChatMessage>()
    var isMentorThinking by mutableStateOf(false)

    // --- Quotes List ---
    val motivationalQuotes = listOf(
        "\"The secret of getting ahead is getting started.\" — Mark Twain",
        "\"An IIT seat isn't just about intellect; it is about absolute perseverance.\" — IIT Bombay Alumnus",
        "\"There are no secrets to success. It is the result of preparation, hard work, and learning from failure.\"",
        "\"Do not wish it were easier, wish you were better.\"",
        "\"Focus on consistency over perfection. 6 hours every day beats 14 hours once a week.\"",
        "\"Your target college is waiting. Stay focused. Stay disciplined.\"",
        "\"The difference between a successful person and others is not a lack of strength, but a lack of will.\""
    )
    val dailyQuote = motivationalQuotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % motivationalQuotes.size]

    // --- Profile Loading ---
    private suspend fun loadProfile() {
        username = repository.getPreference("username", "JEE Aspirant")
        targetCollege = repository.getPreference("target_college", "IIT Bombay")
        targetRank = repository.getPreference("target_rank", "AIR 100")
        dailyGoalHours = repository.getPreference("daily_goal_hours", "8.0").toDoubleOrNull() ?: 8.0
        onboardingCompleted = repository.getPreference("onboarding_completed", "false") == "true"
    }

    fun saveProfile(name: String, college: String, rank: String, hours: Double) {
        viewModelScope.launch {
            username = name
            targetCollege = college
            targetRank = rank
            dailyGoalHours = hours
            onboardingCompleted = true
            
            repository.setPreference("username", name)
            repository.setPreference("target_college", college)
            repository.setPreference("target_rank", rank)
            repository.setPreference("daily_goal_hours", hours.toString())
            repository.setPreference("onboarding_completed", "true")
        }
    }

    // --- Timer Actions ---
    fun startTimer() {
        if (isTimerRunning) return
        isTimerRunning = true
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                timerSeconds++
                
                if (isPomodoroMode && timerSeconds >= pomodoroTargetSeconds) {
                    completeTimerSession()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        isTimerRunning = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        timerSeconds = 0L
    }

    fun completeTimerSession() {
        pauseTimer()
        if (timerSeconds < 10) {
            timerSeconds = 0L
            return // don't track hyper-short micro clicks
        }

        viewModelScope.launch {
            val session = StudySessionEntity(
                subject = selectedSubjectForTimer,
                chapterName = selectedChapterForTimer,
                durationSeconds = timerSeconds,
                isPomodoro = isPomodoroMode,
                note = if (isDeepWorkMode) "Deep Work Focused" else "Regular Session"
            )
            repository.insertSession(session)
            timerSeconds = 0L
        }
    }

    // --- Countdown Calculation ---
    private fun calculateDaysLeft() {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        // JEE Main Standard Tentative: Jan 24, 2027
        val mainCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2027)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 24)
            set(Calendar.HOUR_OF_DAY, 9)
        }
        val advancedCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2027)
            set(Calendar.MONTH, Calendar.JUNE)
            set(Calendar.DAY_OF_MONTH, 6)
            set(Calendar.HOUR_OF_DAY, 9)
        }

        val mainDiff = mainCal.timeInMillis - today
        daysToJEEMain = max(0L, mainDiff / (1000 * 60 * 60 * 24))

        val advDiff = advancedCal.timeInMillis - today
        daysToJEEAdvanced = max(0L, advDiff / (1000 * 60 * 60 * 24))
    }

    // --- Syllabus Actions ---
    fun updateChapterStatus(id: Int, status: String) {
        viewModelScope.launch {
            // Logically if mark completed, assign some minor XP
            if (status == "COMPLETED" || status == "REVISED") {
                // minor reward for completing chapters
                repository.updateChapterStatus(id, status)
                repository.setPreference("user_xp", ((repository.getPreference("user_xp", "0").toIntOrNull() ?: 0) + 40).toString())
            } else {
                repository.updateChapterStatus(id, status)
            }
        }
    }

    fun toggleWeakChapter(id: Int, isCurrentlyWeak: Boolean) {
        viewModelScope.launch {
            repository.updateChapterWeakStatus(id, !isCurrentlyWeak)
        }
    }

    // --- Goal Actions ---
    fun addGoal(title: String, targetHours: Double = 0.0, type: String = "DAILY") {
        viewModelScope.launch {
            val goal = GoalEntity(title = title, targetHours = targetHours, type = type)
            repository.insertGoal(goal)
        }
    }

    fun toggleGoalStatus(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateGoalStatus(id, isCompleted)
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    // --- Mock Test Actions ---
    fun addMockTest(title: String, phy: Int, chem: Int, math: Int, maxScore: Int = 300) {
        viewModelScope.launch {
            val total = phy + chem + math
            val predictions = GeminiService.getMockTestPercentileAndRankPrediction(phy, chem, math, total, maxScore)
            
            val mock = MockTestEntity(
                title = title,
                physicsScore = phy,
                chemistryScore = chem,
                mathsScore = math,
                totalScore = total,
                maxScore = maxScore,
                percentile = predictions.first,
                rank = predictions.second
            )
            repository.insertTest(mock)
        }
    }

    fun deleteMockTest(id: Int) {
        viewModelScope.launch {
            repository.deleteTest(id)
        }
    }

    // --- AI Advisory Recommendations ---
    fun generateAIRecommendations() {
        if (isGeneratingRecommendation) return
        isGeneratingRecommendation = true
        aiRecommendationText = ""
        
        viewModelScope.launch {
            val chapters = allChapters.value
            val tests = allTests.value
            val sessions = allSessions.value

            // 1. Calculate Syllabus completion
            val total = chapters.size
            val completedCount = chapters.count { it.status == "COMPLETED" || it.status == "REVISED" }
            val inProgressCount = chapters.count { it.status == "IN_PROGRESS" }
            val weakChapters = chapters.filter { it.isWeak }.joinToString { "${it.subject}: ${it.name}" }

            val syllabusSummary = "Total Chapters: $total, Completed: $completedCount ($inProgressCount in progress). Weak Chapters: $weakChapters"

            // 2. Aggregate Study Session statistics
            val totalHours = sessions.sumOf { it.durationSeconds } / 3600.0
            val phyHours = sessions.filter { it.subject == "Physics" }.sumOf { it.durationSeconds } / 3600.0
            val chemHours = sessions.filter { it.subject == "Chemistry" }.sumOf { it.durationSeconds } / 3600.0
            val mathHours = sessions.filter { it.subject == "Mathematics" }.sumOf { it.durationSeconds } / 3600.0
            val pomodoros = sessions.count { it.isPomodoro }
            val studySessionSummary = String.format(
                Locale.US,
                "Total study time logged: %.1f hours (%d focus sessions, %d Pomodoro blocks). Subject-level split: Physics = %.1f hrs, Chemistry = %.1f hrs, Mathematics = %.1f hrs. Current streak: %d days.",
                totalHours, sessions.size, pomodoros, phyHours, chemHours, mathHours, streakFlow.value
            )

            // 2. Latest Mock results
            val latestTest = tests.firstOrNull()
            val mockSummary = if (latestTest != null) {
                "Latest score: ${latestTest.totalScore}/${latestTest.maxScore} (P: ${latestTest.physicsScore}, C: ${latestTest.chemistryScore}, M: ${latestTest.mathsScore}) on ${latestTest.title}." +
                        "Predicted percentile: ${latestTest.percentile}%, Estimated AIR: ${latestTest.rank}."
            } else {
                "No mock tests logged yet."
            }

            val result = GeminiService.getAIStudyRecommendations(
                syllabusSummary,
                studySessionSummary,
                mockSummary,
                targetCollege,
                targetRank
            )
            aiRecommendationText = result
            isGeneratingRecommendation = false
        }
    }

    // --- AI Chatbot Mentor ---
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty() || isMentorThinking) return
        
        mentorChatMessages.add(ChatMessage(text = text, isUser = true))
        isMentorThinking = true
        
        viewModelScope.launch {
            val systemPrompt = "You are a warm, wise, and elite IIT JEE preparation mentor named 'AI Guru'. " +
                    "Your mission is to clear student doubts about IIT strategy, textbooks, revision schedules, and mindset battles (overcoming distraction, depression, low test scores). " +
                    "Keep answers punchy, visually rich (using bullet points and bold headers), and highly supportive. " +
                    "Use metaphors of climbing Mt. Everest. Give specific JEE book names when requested."

            val result = GeminiService.generateResponse(text, systemPrompt)
            mentorChatMessages.add(ChatMessage(text = result, isUser = false))
            isMentorThinking = false
        }
    }

    fun clearMentorChat() {
        mentorChatMessages.clear()
        mentorChatMessages.add(ChatMessage(text = "Hello aspirant! I am your AI Mentor. Ask me any strategic questions about syllabus clearing, book choice, or overcoming study burnout. What's bothering you today?", isUser = false))
    }
}

// Support Structs in Composable architecture
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Helper to construct ViewModel with Context
class JEEViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JEEViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JEEViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
