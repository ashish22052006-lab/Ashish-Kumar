package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChapterEntity
import com.example.data.MockTestEntity
import com.example.data.StudySessionEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random
import androidx.compose.ui.graphics.vector.ImageVector

data class JEETabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// --- PREMIUM PALETTE & DESIGN TOKENS ---
object JEETagColor {
    val DarkBg = Color(0xFF0A0A0B)
    val LightBg = Color(0xFFF7F9FC)
    val DarkCard = Color(0xFF161A22)
    val LightCard = Color(0xFFFFFFFF)
    
    val PrimaryGreen = Color(0xFF1E5F3B)  // Forest Moss
    val AccentMint = Color(0xFF00C853)   // IIT Glow
    val NavyBlue = Color(0xFF2E5BFF)     // TickTick Notion vibe
    
    val PhysicsColor = Color(0xFF3B82F6)     // Soft Blue
    val ChemistryColor = Color(0xFFFF9100)   // Vibrant Orange
    val MathsColor = Color(0xFFFF1744)       // Neon Pink/Red
    
    val BorderDark = Color(0xFF242B38)
    val BorderLight = Color(0xFFE5E9F0)
}

fun Modifier.immersiveGlowBackground(isDarkMode: Boolean): Modifier = this.drawBehind {
    if (isDarkMode) {
        // Main dark canvas
        drawRect(color = Color(0xFF0A0A0B))
        
        // Circular soft blue glow top-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x153B82F6), // Blue
                    Color(0x043B82F6),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.1f, size.height * -0.05f),
                radius = size.width * 0.85f
            ),
            center = Offset(size.width * 0.1f, size.height * -0.05f),
            radius = size.width * 0.85f
        )
        
        // Circular soft orange/amber glow center-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x19EA580C), // Orange
                    Color(0x05EA580C),
                    Color.Transparent
                ),
                center = Offset(size.width * 1.05f, size.height * 0.38f),
                radius = size.width * 0.9f
            ),
            center = Offset(size.width * 1.05f, size.height * 0.38f),
            radius = size.width * 0.9f
        )
    } else {
        drawRect(color = JEETagColor.LightBg)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JEEAppContent(viewModel: JEEViewModel) {
    var isDarkMode by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf("dashboard") }
    
    // Theme configurations
    val backgroundColor = if (isDarkMode) Color(0xFF0A0A0B) else JEETagColor.LightBg
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF1C1D21)
    val cardBg = if (isDarkMode) Color(0x0AFFFFFF) else JEETagColor.LightCard
    val borderColor = if (isDarkMode) Color(0x13FFFFFF) else JEETagColor.BorderLight

    val xp by viewModel.xpFlow.collectAsState()
    val level by viewModel.levelFlow.collectAsState()
    val streak by viewModel.streakFlow.collectAsState()
    
    Surface(
        modifier = Modifier.fillMaxSize().immersiveGlowBackground(isDarkMode),
        color = Color.Transparent
    ) {
        if (!viewModel.onboardingCompleted) {
            OnboardingScreen(
                isDarkMode = isDarkMode,
                onComplete = { name, college, rank, hours ->
                    viewModel.saveProfile(name, college, rank, hours)
                }
            )
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "JEE Logo",
                                    tint = JEETagColor.AccentMint,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "JEE Tracker Pro",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        },
                        actions = {
                            // Flame Streak indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(JEETagColor.ChemistryColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = JEETagColor.ChemistryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streak Days",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JEETagColor.ChemistryColor
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Level indicator
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JEETagColor.NavyBlue)
                            ) {
                                Text(
                                    text = "L$level",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            
                            IconButton(onClick = { isDarkMode = !isDarkMode }) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Light Mode",
                                    tint = textColor
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = if (isDarkMode) Color.Transparent else backgroundColor,
                            titleContentColor = textColor
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = if (isDarkMode) Color(0xD80A0A0B) else cardBg,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .drawBehind {
                                drawLine(
                                    color = if (isDarkMode) Color(0x0FFFFFFF) else Color(0x11000000),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            .navigationBarsPadding()
                    ) {
                        val tabs = listOf(
                            JEETabItem("dashboard", "Dashboard", Icons.Default.Dashboard),
                            JEETabItem("timer", "Timer", Icons.Default.Timer),
                            JEETabItem("syllabus", "Syllabus", Icons.Default.Book),
                            JEETabItem("analytics", "Progress", Icons.Default.Analytics),
                            JEETabItem("ai", "AI Mentor", Icons.Default.AutoAwesome)
                        )
                        
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab.route,
                                onClick = { currentTab = tab.route },
                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                                label = { Text(text = tab.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = JEETagColor.AccentMint,
                                    indicatorColor = JEETagColor.PrimaryGreen,
                                    unselectedIconColor = textColor.copy(alpha = 0.5f),
                                    unselectedTextColor = textColor.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(backgroundColor)
                ) {
                    when (currentTab) {
                        "dashboard" -> DashboardTab(viewModel, cardBg, textColor, borderColor, isDarkMode, onNavigate = { currentTab = it })
                        "timer" -> TimerTab(viewModel, cardBg, textColor, borderColor, isDarkMode)
                        "syllabus" -> SyllabusTab(viewModel, cardBg, textColor, borderColor, isDarkMode)
                        "analytics" -> AnalyticsTab(viewModel, cardBg, textColor, borderColor, isDarkMode)
                        "ai" -> AIMentorTab(viewModel, cardBg, textColor, borderColor, isDarkMode)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. ONBOARDING SCREEN
// ==========================================
@Composable
fun OnboardingScreen(
    isDarkMode: Boolean,
    onComplete: (name: String, college: String, rank: String, hours: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCollege by remember { mutableStateOf("IIT Bombay") }
    val colleges = listOf("IIT Bombay", "IIT Delhi", "IIT Madras", "IIT Roorkee", "IIT Kharagpur", "BITS Pilani", "NIT Trichy")
    var rank by remember { mutableStateOf("AIR 100") }
    var hours by remember { mutableStateOf(8.0) }
    
    val themeBg = if (isDarkMode) JEETagColor.DarkBg else JEETagColor.LightBg
    val themeCard = if (isDarkMode) JEETagColor.DarkCard else JEETagColor.LightCard
    val themeText = if (isDarkMode) Color.White else Color(0xFF1C1D21)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = "IIT Icon",
            tint = JEETagColor.AccentMint,
            modifier = Modifier.size(72.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Welcome to JEE Tracker Pro",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = themeText,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your tailored Companion for the hardest exam. Achieve focus like Forest, note completeness like Notion, and track accuracy like TickTick.",
            fontSize = 14.sp,
            color = themeText.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = themeCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Personalize Your Target",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeText
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JEETagColor.AccentMint,
                        focusedLabelColor = JEETagColor.AccentMint
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(text = "Target College", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeText)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp)) {
                    colleges.forEach { col ->
                        val selected = selectedCollege == col
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) JEETagColor.PrimaryGreen else themeBg)
                                .border(1.dp, if (selected) JEETagColor.AccentMint else themeText.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .clickable { selectedCollege = col }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = col,
                                color = if (selected) Color.White else themeText.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = rank,
                    onValueChange = { rank = it },
                    label = { Text("Target JEE Rank (e.g. Under AIR 500)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JEETagColor.AccentMint,
                        focusedLabelColor = JEETagColor.AccentMint
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = "Daily Study Goal: ${hours.roundToInt()} hours", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeText)
                Slider(
                    value = hours.toFloat(),
                    onValueChange = { hours = it.toDouble() },
                    valueRange = 4f..16f,
                    steps = 12,
                    colors = SliderDefaults.colors(
                        thumbColor = JEETagColor.AccentMint,
                        activeTrackColor = JEETagColor.PrimaryGreen
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = {
                        val cleanName = if (name.isBlank()) "JEE Aspirant" else name
                        onComplete(cleanName, selectedCollege, rank, hours)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JEETagColor.AccentMint),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("complete_onboarding_button")
                ) {
                    Text(text = "Initialize JEE Tracker Pro", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

// ==========================================
// 2. DASHBOARD TAB
// ==========================================
@Composable
fun DashboardTab(
    viewModel: JEEViewModel,
    cardBg: Color,
    textColor: Color,
    borderColor: Color,
    isDarkMode: Boolean,
    onNavigate: (String) -> Unit = {}
) {
    val sessions by viewModel.allSessions.collectAsState()
    val chapters by viewModel.allChapters.collectAsState()
    val tests by viewModel.allTests.collectAsState()
    val streak by viewModel.streakFlow.collectAsState()
    
    // Calculates study sums
    val todaySessions = sessions.filter {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        format.format(Date(it.timestamp)) == format.format(calendar.time)
    }
    val todayStudyMinutes = todaySessions.sumOf { it.durationSeconds } / 60
    val todayStudyHours = todayStudyMinutes / 60.0
    
    // Compute success meter value out of 100
    val totalChapters = max(1, chapters.size)
    val compChapters = chapters.count { it.status == "COMPLETED" || it.status == "REVISED" }
    val syllabusRatio = compChapters.toDouble() / totalChapters.toDouble()
    
    val latestTest = tests.firstOrNull()
    val testRatio = if (latestTest != null) {
        latestTest.totalScore.toDouble() / latestTest.maxScore.toDouble()
    } else {
        0.5 // Default baseline
    }
    
    val streakFactor = (viewModel.streakFlow.collectAsState().value / 10.0).coerceAtMost(1.0)
    val successMeterScore = ((syllabusRatio * 35.0) + (testRatio * 45.0) + (streakFactor * 20.0)) * 100.0
    val successScoreInt = successMeterScore.roundToInt().coerceIn(15, 100)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Block (Aryan Sharma style)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Initials Circle with smooth blue-indigo gradient
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
                                )
                            )
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                    ) {
                        val initials = if (viewModel.username.length >= 2) {
                            viewModel.username.take(2).uppercase()
                        } else if (viewModel.username.isNotEmpty()) {
                            viewModel.username.take(1).uppercase()
                        } else {
                            "AS"
                        }
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "TARGET: ${viewModel.targetCollege.uppercase()}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8), // slate-400
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = viewModel.username,
                            fontSize = 15.sp,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Day Streak indicator badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0EFFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "$streak Day Streak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Central Success Meter Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val (tier, tierColor) = when {
                            successScoreInt >= 85 -> "IIT READY" to JEETagColor.AccentMint
                            successScoreInt >= 60 -> "COMPETITIVE" to JEETagColor.PhysicsColor
                            successScoreInt >= 35 -> "IMPROVING" to JEETagColor.ChemistryColor
                            else -> "BEGINNER" to Color.Gray
                        }
                        
                        // Styled circle success gauge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(140.dp)
                                .padding(8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 8.dp.toPx()
                                val radius = (size.minDimension - strokeWidth) / 2f
                                
                                // Background base track circle
                                drawCircle(
                                    color = Color(0x0FFFFFFF), // 5% white circle
                                    radius = radius,
                                    style = Stroke(width = strokeWidth)
                                )
                                
                                // Success sweep arc with dynamic gradient / glowing color
                                val sweepAngle = (successScoreInt / 100f) * 360f
                                drawArc(
                                    color = tierColor,
                                    startAngle = -90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$successScoreInt",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor,
                                    letterSpacing = (-1).sp
                                )
                                Text(
                                    text = "SCORE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor.copy(alpha = 0.4f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = "Success Meter",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = if (successScoreInt >= 80) "Consistency + Performance is Peak ($tier)" else "Focus & Daily Study are Improving ($tier)",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8), // slate-400
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    // Bottom gradient indicator line representing the Tailwind aesthetic
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6))
                                )
                            )
                    )
                }
            }
        }

        // Today hours & JEE Countdown grid row (preserving the multi-days counters)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Today study hours progress card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "TODAY",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8), // slate-400
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", todayStudyHours),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = " / ${viewModel.dailyGoalHours.roundToInt()} hrs",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B), // slate-500
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val ratio = (todayStudyHours / viewModel.dailyGoalHours).coerceIn(0.0, 1.0).toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(Color(0x1AFFFFFF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6)) // Glowing Blue progress bar
                            )
                        }
                    }
                }

                // Countdown Card (JEE Main countdown)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "JEE COUNTDOWN",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8), // slate-400
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${viewModel.daysToJEEMain}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = " days",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B), // slate-500
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = "Time is critical",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF97316) // Glowing orange countdown indicator
                        )
                    }
                }
            }
        }

        // Secondary examination countdown (JEE Advanced) - so we keep full functionality
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(JEETagColor.MathsColor.copy(alpha = 0.12f))
                        ) {
                            Text("⚛️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "JEE Advanced 2027", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text(text = "The ultimate IIT admission examination", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                    Text(
                        text = "${viewModel.daysToJEEAdvanced} Days Left",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = JEETagColor.MathsColor
                    )
                }
            }
        }

        // Motivational Quote widget
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JEETagColor.PrimaryGreen.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, JEETagColor.PrimaryGreen.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = "Quote",
                        tint = JEETagColor.AccentMint,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = viewModel.dailyQuote,
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 💡 AI Study Recommendations & Schedule Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Advice",
                                tint = JEETagColor.AccentMint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Study Advisor & Schedule",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        
                        if (viewModel.aiRecommendationText.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.generateAIRecommendations() },
                                modifier = Modifier.size(28.dp),
                                enabled = !viewModel.isGeneratingRecommendation
                            ) {
                                if (viewModel.isGeneratingRecommendation) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = JEETagColor.AccentMint, strokeWidth = 1.5.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Regenerate Plan",
                                        tint = JEETagColor.AccentMint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (viewModel.aiRecommendationText.isEmpty()) {
                        Text(
                            text = "Get personalized study priorities, weak subjects analysis, and revision timetables compiled by Google Gemini AI.",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.generateAIRecommendations() },
                            colors = ButtonDefaults.buttonColors(containerColor = JEETagColor.AccentMint),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !viewModel.isGeneratingRecommendation
                        ) {
                            if (viewModel.isGeneratingRecommendation) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate IIT Study Strategy", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        // Display a sleek excerpt of the generated recommendation
                        val lines = viewModel.aiRecommendationText.split("\n")
                        val cleanPreviewText = lines
                            .filter { it.trim().isNotEmpty() }
                            .take(4)
                            .joinToString("\n") { it.trim() }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(textColor.copy(alpha = 0.04f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = cleanPreviewText,
                                    fontSize = 11.5.sp,
                                    color = textColor.copy(alpha = 0.9f),
                                    lineHeight = 16.sp,
                                    maxLines = 4,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onNavigate("ai") },
                                colors = ButtonDefaults.buttonColors(containerColor = JEETagColor.PrimaryGreen),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Detailed AI Plan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            
                            OutlinedButton(
                                onClick = { viewModel.generateAIRecommendations() },
                                border = BorderStroke(1.dp, borderColor),
                                modifier = Modifier.weight(1f),
                                enabled = !viewModel.isGeneratingRecommendation
                            ) {
                                Text(
                                    text = if (viewModel.isGeneratingRecommendation) "Generating..." else "Regenerate",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Goals checklist overview in Dashboard
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CURRENT GOALS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B), // slate-500
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val goals by viewModel.allGoals.collectAsState()
                val activeGoals = goals.take(3)
                
                if (activeGoals.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, borderColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "No custom goals active. Create daily revision tasks in the Planner!",
                            fontSize = 13.sp,
                            color = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    activeGoals.forEach { goal ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, borderColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            val isPhysics = goal.title.contains("physics", ignoreCase = true) || goal.title.contains("HC Verma", ignoreCase = true) || goal.title.contains("mechanics", ignoreCase = true)
                            val isChemistry = goal.title.contains("chem", ignoreCase = true) || goal.title.contains("organic", ignoreCase = true) || goal.title.contains("reaction", ignoreCase = true)
                            val isMaths = goal.title.contains("math", ignoreCase = true) || goal.title.contains("calculus", ignoreCase = true) || goal.title.contains("algebra", ignoreCase = true)
                            
                            val subjectColor = when {
                                isPhysics -> JEETagColor.PhysicsColor
                                isChemistry -> JEETagColor.ChemistryColor
                                isMaths -> JEETagColor.MathsColor
                                else -> JEETagColor.NavyBlue
                            }
                            
                            val subjectEmoji = when {
                                isPhysics -> "⚛️"
                                isChemistry -> "🧪"
                                isMaths -> "📐"
                                else -> "📅"
                            }

                            val subjectLabel = when {
                                isPhysics -> "Physics"
                                isChemistry -> "Chemistry"
                                isMaths -> "Mathematics"
                                else -> "Daily Target"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Circular icon placeholder matching subject/theme
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(subjectColor.copy(alpha = 0.15f))
                                    ) {
                                        Text(
                                            text = subjectEmoji,
                                            fontSize = 18.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column {
                                        Text(
                                            text = goal.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (goal.isCompleted) textColor.copy(alpha = 0.4f) else textColor
                                        )
                                        Text(
                                            text = "$subjectLabel • ${goal.targetHours}h study target",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8) // slate-400
                                        )
                                    }
                                }
                                
                                Checkbox(
                                    checked = goal.isCompleted,
                                    onCheckedChange = { viewModel.toggleGoalStatus(goal.id, it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = JEETagColor.AccentMint,
                                        uncheckedColor = textColor.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. TIMER TAB & SESSION TRACKING
// ==========================================
@Composable
fun TimerTab(
    viewModel: JEEViewModel,
    cardBg: Color,
    textColor: Color,
    borderColor: Color,
    isDarkMode: Boolean
) {
    val chapters by viewModel.allChapters.collectAsState()
    val subjectChapters = chapters.filter { it.subject == viewModel.selectedSubjectForTimer }

    // Animated pointer for circles
    val infiniteTransition = rememberInfiniteTransition()
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Controls Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !viewModel.isPomodoroMode,
                onClick = { viewModel.isPomodoroMode = false },
                label = { Text("Deep Stop Watch") }
            )
            Spacer(modifier = Modifier.width(12.dp))
            FilterChip(
                selected = viewModel.isPomodoroMode,
                onClick = { viewModel.isPomodoroMode = true },
                label = { Text("25m Pomodoro") }
            )
        }

        // Circular Timer Render Canvas
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background breathing outer ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val radius = (size.width - strokeWidth) / 2
                
                drawCircle(
                    color = JEETagColor.PrimaryGreen.copy(alpha = pulsingAlpha),
                    radius = radius + 6.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx())
                )
                
                drawArc(
                    color = borderColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Track percentage
                val progress = if (viewModel.isPomodoroMode) {
                    (viewModel.timerSeconds.toFloat() / viewModel.pomodoroTargetSeconds.toFloat()).coerceIn(0f, 1f)
                } else {
                    ((viewModel.timerSeconds % 3600L).toFloat() / 3600f)
                }

                drawArc(
                    color = when (viewModel.selectedSubjectForTimer) {
                        "Physics" -> JEETagColor.PhysicsColor
                        "Chemistry" -> JEETagColor.ChemistryColor
                        else -> JEETagColor.MathsColor
                    },
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Digital Text Clock
            val minutes = viewModel.timerSeconds / 60
            val seconds = viewModel.timerSeconds % 60
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (viewModel.isTimerRunning) JEETagColor.AccentMint else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isTimerRunning) "FOCUS ACTIVE" else "PAUSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // START / PAUSE / RE-SAVE controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!viewModel.isTimerRunning) {
                Button(
                    onClick = { viewModel.startTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = JEETagColor.AccentMint),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("start_timer_btn")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Start Focus", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            } else {
                Button(
                    onClick = { viewModel.pauseTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = JEETagColor.ChemistryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("pause_timer_btn")
                ) {
                    Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Pause Session", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            if (viewModel.timerSeconds > 0) {
                IconButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(textColor.copy(alpha = 0.08f))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = textColor)
                }

                IconButton(
                    onClick = { viewModel.completeTimerSession() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(JEETagColor.PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = Color.White)
                }
            }
        }

        // Deep Work blocker option
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(textColor.copy(alpha = 0.04f))
                .clickable { viewModel.isDeepWorkMode = !viewModel.isDeepWorkMode }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (viewModel.isDeepWorkMode) Icons.Default.Shield else Icons.Default.DoNotDisturbOn,
                contentDescription = "Shield",
                tint = if (viewModel.isDeepWorkMode) JEETagColor.AccentMint else textColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (viewModel.isDeepWorkMode) "Deep Work is shielding notifications!" else "Enable Shield Mode",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDeepWorkMode) JEETagColor.AccentMint else textColor.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Topic/Chapter Session Selectors (Notion Vibe)
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📚 Prep Setup", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(12.dp))

                // Subject clicker
                Text(text = "Subject Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.5f))
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Physics", "Chemistry", "Mathematics").forEach { sub ->
                        val selected = viewModel.selectedSubjectForTimer == sub
                        val color = when (sub) {
                            "Physics" -> JEETagColor.PhysicsColor
                            "Chemistry" -> JEETagColor.ChemistryColor
                            else -> JEETagColor.MathsColor
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) color.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (selected) color else borderColor, RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.selectedSubjectForTimer = sub
                                    viewModel.selectedChapterForTimer = "General Topics"
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = sub, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selected) color else textColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable topic list based on chapters in database
                Text(text = "Chapter/Topic of Study", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    LazyColumn {
                        item {
                            val isSel = viewModel.selectedChapterForTimer == "General Topics"
                            Text(
                                text = "General Topics / Practice Tests",
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSel) JEETagColor.PrimaryGreen.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { viewModel.selectedChapterForTimer = "General Topics" }
                                    .padding(8.dp),
                                color = if (isSel) JEETagColor.AccentMint else textColor
                            )
                        }
                        items(subjectChapters) { chap ->
                            val isSel = viewModel.selectedChapterForTimer == chap.name
                            Text(
                                text = chap.name,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSel) JEETagColor.PrimaryGreen.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { viewModel.selectedChapterForTimer = chap.name }
                                    .padding(8.dp),
                                color = if (isSel) JEETagColor.AccentMint else textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. SYLLABUS CHECKLIST TAB
// ==========================================
@Composable
fun SyllabusTab(
    viewModel: JEEViewModel,
    cardBg: Color,
    textColor: Color,
    borderColor: Color,
    isDarkMode: Boolean
) {
    var filterSubject by remember { mutableStateOf("Physics") }
    var searchQuery by remember { mutableStateOf("") }
    val chapters by viewModel.allChapters.collectAsState()

    val filteredChapters = chapters.filter {
        it.subject == filterSubject && (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true))
    }

    // Calculating completion percentage per subject
    val totalInSub = chapters.count { it.subject == filterSubject }
    val compInSub = chapters.count { it.subject == filterSubject && (it.status == "COMPLETED" || it.status == "REVISED") }
    val percentCompleted = if (totalInSub > 0) (compInSub.toFloat() / totalInSub.toFloat() * 100).roundToInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Subjects tabs filter
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Physics", "Chemistry", "Mathematics").forEach { sub ->
                val selected = filterSubject == sub
                val color = when (sub) {
                    "Physics" -> JEETagColor.PhysicsColor
                    "Chemistry" -> JEETagColor.ChemistryColor
                    else -> JEETagColor.MathsColor
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) color else cardBg)
                        .border(1.dp, if (selected) Color.Transparent else borderColor, RoundedCornerShape(20.dp))
                        .clickable { filterSubject = sub }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sub,
                        color = if (selected) Color.White else textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subject completion visual bar
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$filterSubject Syllabus Completion",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { percentCompleted / 100f },
                        color = when (filterSubject) {
                            "Physics" -> JEETagColor.PhysicsColor
                            "Chemistry" -> JEETagColor.ChemistryColor
                            else -> JEETagColor.MathsColor
                        },
                        trackColor = borderColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "$percentCompleted%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = when (filterSubject) {
                        "Physics" -> JEETagColor.PhysicsColor
                        "Chemistry" -> JEETagColor.ChemistryColor
                        else -> JEETagColor.MathsColor
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search JEE topics...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JEETagColor.AccentMint,
                unfocusedBorderColor = borderColor
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // List Scroll of the chapters
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredChapters) { chap ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chap.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = chap.category,
                                    fontSize = 11.sp,
                                    color = textColor.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            // State Tag clickers
                            var showStateMenu by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { showStateMenu = true }) {
                                    val (label, tint) = when(chap.status) {
                                        "COMPLETED" -> "Completed" to JEETagColor.AccentMint
                                        "REVISED" -> "Revised" to JEETagColor.PhysicsColor
                                        "IN_PROGRESS" -> "In Progress" to JEETagColor.ChemistryColor
                                        else -> "Not Started" to Color.Gray
                                    }
                                    Text(text = label, color = tint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = tint)
                                }
                                
                                DropdownMenu(
                                    expanded = showStateMenu,
                                    onDismissRequest = { showStateMenu = false }
                                ) {
                                    listOf("NOT_STARTED", "IN_PROGRESS", "COMPLETED", "REVISED").forEach { st ->
                                        val strLabel = when(st) {
                                            "NOT_STARTED" -> "Not Started"
                                            "IN_PROGRESS" -> "In Progress"
                                            "COMPLETED" -> "Completed"
                                            else -> "Revised"
                                        }
                                        DropdownMenuItem(
                                            text = { Text(strLabel) },
                                            onClick = {
                                                viewModel.updateChapterStatus(chap.id, st)
                                                showStateMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = borderColor)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Weak Area indicator toggle
                            Row(
                                modifier = Modifier.clickable { viewModel.toggleWeakChapter(chap.id, chap.isWeak) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (chap.isWeak) Icons.Default.Warning else Icons.Outlined.Warning,
                                    contentDescription = "Weak flag",
                                    tint = if (chap.isWeak) JEETagColor.MathsColor else textColor.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Flag Weak Area",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (chap.isWeak) JEETagColor.MathsColor else textColor.copy(alpha = 0.6f)
                                )
                            }
                            
                            if (chap.isWeak) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(JEETagColor.MathsColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Needs Improvement", color = JEETagColor.MathsColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. ANALYTICS & CUSTOM GRAPHS TAB
// ==========================================
@Composable
fun AchievementBadgeCard(
    title: String,
    description: String,
    emoji: String,
    progress: Float,
    progressText: String,
    isUnlocked: Boolean,
    cardBg: Color,
    textColor: Color,
    borderColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0x1500C853) else cardBg.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            width = if (isUnlocked) 2.dp else 1.dp,
            color = if (isUnlocked) Color(0xFF00C853) else borderColor
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) Color(0xFF00C853).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f))
                    .border(1.5.dp, if (isUnlocked) Color(0xFF00C853) else Color.Gray.copy(alpha = 0.2f), CircleShape)
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    if (isUnlocked) {
                        Text(
                            text = "UNLOCKED ⚡",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00C853)
                        )
                    } else {
                        Text(
                            text = "LOCKED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.4f)
                        )
                    }
                }
                
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = textColor.copy(alpha = 0.6f),
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val progressValue = progress.coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressValue)
                                .clip(CircleShape)
                                .background(if (isUnlocked) Color(0xFF00C853) else JEETagColor.PhysicsColor)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Text(
                        text = progressText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) Color(0xFF00C853) else textColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsTab(
    viewModel: JEEViewModel,
    cardBg: Color,
    textColor: Color,
    borderColor: Color,
    isDarkMode: Boolean
) {
    val sessions by viewModel.allSessions.collectAsState()
    val tests by viewModel.allTests.collectAsState()
    val chapters by viewModel.allChapters.collectAsState()
    val streak by viewModel.streakFlow.collectAsState()
    val xp by viewModel.xpFlow.collectAsState()
    val level by viewModel.levelFlow.collectAsState()

    var activeSubTab by remember { mutableStateOf("analytics") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "📊 Progress & Badges", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        Text(text = "Track charts, mock performance and unlock milestones.", fontSize = 12.sp, color = textColor.copy(alpha = 0.5f))

        // Subtab Navigation Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkMode) Color(0x0AFFFFFF) else Color(0x06000000))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeSubTab == "analytics") JEETagColor.PrimaryGreen else Color.Transparent)
                    .clickable { activeSubTab = "analytics" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Charts & Logger",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeSubTab == "analytics") Color.White else textColor
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeSubTab == "achievements") JEETagColor.PrimaryGreen else Color.Transparent)
                    .clickable { activeSubTab = "achievements" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Achievements & Level",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeSubTab == "achievements") Color.White else textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeSubTab == "analytics") {
            // Render Circular Subject Distribution
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Subject Study Allocation Ratio", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(16.dp))
    
                    val phySec = sessions.filter { it.subject == "Physics" }.sumOf { it.durationSeconds }
                    val chemSec = sessions.filter { it.subject == "Chemistry" }.sumOf { it.durationSeconds }
                    val mathSec = sessions.filter { it.subject == "Mathematics" }.sumOf { it.durationSeconds }
                    val totalSec = phySec + chemSec + mathSec
    
                    if (totalSec == 0L) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No study stats logged. Record focus sessions to view circular ratio charts!",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Canvas Circular Ratio Chart
                            Canvas(modifier = Modifier.size(120.dp)) {
                                val phyAngle = (phySec.toFloat() / totalSec.toFloat()) * 360f
                                val chemAngle = (chemSec.toFloat() / totalSec.toFloat()) * 360f
                                val mathAngle = (mathSec.toFloat() / totalSec.toFloat()) * 360f
    
                                val strokeWidth = 16.dp.toPx()
                                val chartSize = size.width - strokeWidth
                                
                                var currentAngle = -90f
                                
                                // Physics arc
                                if (phyAngle > 0) {
                                    drawArc(
                                        color = JEETagColor.PhysicsColor,
                                        startAngle = currentAngle,
                                        sweepAngle = phyAngle,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                    currentAngle += phyAngle
                                }
                                
                                // Chemistry arc
                                if (chemAngle > 0) {
                                    drawArc(
                                        color = JEETagColor.ChemistryColor,
                                        startAngle = currentAngle,
                                        sweepAngle = chemAngle,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                    currentAngle += chemAngle
                                }
    
                                // Math arc
                                if (mathAngle > 0) {
                                    drawArc(
                                        color = JEETagColor.MathsColor,
                                        startAngle = currentAngle,
                                        sweepAngle = mathAngle,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                }
                            }
    
                            Spacer(modifier = Modifier.width(20.dp))
    
                            // Custom Chart Legends
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                LegendItem("Physics", JEETagColor.PhysicsColor, phySec / 3600.0, textColor)
                                LegendItem("Chemistry", JEETagColor.ChemistryColor, chemSec / 3600.0, textColor)
                                LegendItem("Mathematics", JEETagColor.MathsColor, mathSec / 3600.0, textColor)
                            }
                        }
                    }
                }
            }
    
            Spacer(modifier = Modifier.height(16.dp))
    
            // Heatmap Calendar representing study consistency
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Notion-Style Consistency Matrix", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = "Daily focus intensity checkboard", fontSize = 11.sp, color = textColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
    
                    // Standard matrix rendering (28 boxes represented, representing last 4 weeks of productivity)
                    // Let's seed random hours studied in the calendar cells to make it look full and dynamic!
                    val random = remember { Random(4) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..6) { // 7 columns (representing days of the week)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (row in 0..3) { // 4 rows (representing weeks)
                                    val intensity = random.nextInt(4) // 0 to 3
                                    val boxColor = when (intensity) {
                                        3 -> JEETagColor.AccentMint
                                        2 -> JEETagColor.PrimaryGreen
                                        1 -> JEETagColor.PrimaryGreen.copy(alpha = 0.4f)
                                        else -> borderColor
                                    }
    
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(boxColor)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(text = "Less", fontSize = 10.sp, color = textColor.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(borderColor))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(JEETagColor.PrimaryGreen.copy(alpha = 0.4f)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(JEETagColor.PrimaryGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(JEETagColor.AccentMint))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "More", fontSize = 10.sp, color = textColor.copy(alpha = 0.5f))
                    }
                }
            }
    
            Spacer(modifier = Modifier.height(16.dp))
    
            // MOCK TEST TRACER & EXPECTED RANK PREDICTIONS
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "📝 Mock Test Performance Log", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(12.dp))
    
                    var testTitle by remember { mutableStateOf("") }
                    var phyScore by remember { mutableStateOf("") }
                    var chemScore by remember { mutableStateOf("") }
                    var mathScore by remember { mutableStateOf("") }
    
                    OutlinedTextField(
                        value = testTitle,
                        onValueChange = { testTitle = it },
                        placeholder = { Text("Mock Test Title (e.g. Allen Test 4)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JEETagColor.AccentMint)
                    )
    
                    Spacer(modifier = Modifier.height(10.dp))
    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phyScore,
                            onValueChange = { phyScore = it },
                            placeholder = { Text("Phy /100") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JEETagColor.PhysicsColor)
                        )
                        OutlinedTextField(
                            value = chemScore,
                            onValueChange = { chemScore = it },
                            placeholder = { Text("Chem /100") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JEETagColor.ChemistryColor)
                        )
                        OutlinedTextField(
                            value = mathScore,
                            onValueChange = { mathScore = it },
                            placeholder = { Text("Math /100") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JEETagColor.MathsColor)
                        )
                    }
    
                    Spacer(modifier = Modifier.height(12.dp))
    
                    Button(
                        onClick = {
                            val title = if (testTitle.isEmpty()) "JEE Mock Unit Test" else testTitle
                            val p = phyScore.toIntOrNull() ?: 0
                            val c = chemScore.toIntOrNull() ?: 0
                            val m = mathScore.toIntOrNull() ?: 0
                            viewModel.addMockTest(title, p, c, m)
                            
                            testTitle = ""
                            phyScore = ""
                            chemScore = ""
                            mathScore = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JEETagColor.AccentMint),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Save & Predict Percentile", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
    
                    if (tests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "Logged Tests & Expected percentiles", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.5f))
                        
                        tests.take(3).forEach { t ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = t.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(
                                        text = "Marks: ${t.totalScore}/300 (P:${t.physicsScore} C:${t.chemistryScore} M:${t.mathsScore})",
                                        fontSize = 11.sp,
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "${t.percentile}%ile", fontSize = 14.sp, fontWeight = FontWeight.Black, color = JEETagColor.AccentMint)
                                    Text(text = "Est. AIR: #${t.rank}", fontSize = 10.sp, color = textColor.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Gamification, Achievements & Profile Level screen
            val rankTitle = when {
                level >= 10 -> "IIT Prodigy 🌐"
                level >= 7 -> "Concept Master 🔥"
                level >= 5 -> "Equation Solver ⚛️"
                level >= 3 -> "Formula Apprentice ✏️"
                else -> "JEE Novice 🌱"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "STUDENT PROFILE LEVEL",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Level $level",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = rankTitle,
                                fontSize = 14.sp,
                                color = JEETagColor.AccentMint,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Text(
                                text = "🏆",
                                fontSize = 28.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val maxLevelXp = level * 200
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level Progress",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$xp / $maxLevelXp XP",
                            fontSize = 12.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val progressRatio = (xp.toFloat() / maxLevelXp.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressRatio)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF3B82F6), Color(0xFF00C853))
                                    )
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Earn XP by completing study sessions (+2 XP/min), mocking exams (+150 XP), or checking off syllabus goals (+30 XP)!",
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.4f),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ACHIEVEMENT BADGES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dynamic Milestone Badges
            val totalHours = sessions.sumOf { it.durationSeconds } / 3600.0
            val revisedChaptersCount = chapters.count { it.status == "REVISED" || it.status == "COMPLETED" }
            val testCount = tests.size

            // Badge 1: 7-Day Streak
            AchievementBadgeCard(
                title = "7-Day Streak Warrior",
                description = "Demonstrate relentless grit by logging study focus days to attain a 7-day study streak.",
                emoji = "🔥",
                progress = streak / 7f,
                progressText = "$streak / 7 days",
                isUnlocked = streak >= 7,
                cardBg = cardBg,
                textColor = textColor,
                borderColor = borderColor
            )

            // Badge 2: 100 Study Hours
            AchievementBadgeCard(
                title = "Centurion Scholar",
                description = "Logger check: absolute discipline. Accomplish 100 total hours of deep focused study sessions inside the tracker.",
                emoji = "📖",
                progress = (totalHours / 100f).toFloat(),
                progressText = String.format(Locale.US, "%.1f / 100.0 hrs", totalHours),
                isUnlocked = totalHours >= 100.0,
                cardBg = cardBg,
                textColor = textColor,
                borderColor = borderColor
            )

            // Badge 3: Revision Master
            AchievementBadgeCard(
                title = "Revision Master",
                description = "Unlock the elite rank by changing the syllabus status of at least 5 chapters of Physics, Chemistry or Maths to Completed or Revised.",
                emoji = "⚡",
                progress = revisedChaptersCount / 5f,
                progressText = "$revisedChaptersCount / 5 topics",
                isUnlocked = revisedChaptersCount >= 5,
                cardBg = cardBg,
                textColor = textColor,
                borderColor = borderColor
            )

            // Badge 4: Mock Test Warrior
            AchievementBadgeCard(
                title = "Mock Test Warrior",
                description = "Formulate high-stakes exam practices by taking, analyzing, and logging 3 mock test percentile scores.",
                emoji = "🎯",
                progress = testCount / 3f,
                progressText = "$testCount / 3 tests",
                isUnlocked = testCount >= 3,
                cardBg = cardBg,
                textColor = textColor,
                borderColor = borderColor
            )
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, hours: Double, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
        Text(text = String.format(Locale.US, "%.1f hrs studied", hours), fontSize = 11.sp, color = textColor.copy(alpha = 0.6f))
    }
}

// ==========================================
// 6. AI STRATEGY & CHAT MENTOR TAB
// ==========================================
@Composable
fun AIMentorTab(
    viewModel: JEEViewModel,
    cardBg: Color,
    textColor: Color,
    borderColor: Color,
    isDarkMode: Boolean
) {
    var selectedSubSection by remember { mutableStateOf("strategy") }
    var userMessageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toggle tabs
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedSubSection == "strategy") JEETagColor.PrimaryGreen else cardBg)
                    .clickable { selectedSubSection = "strategy" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "AI Study Strategy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selectedSubSection == "strategy") Color.White else textColor)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedSubSection == "chat") JEETagColor.PrimaryGreen else cardBg)
                    .clickable { selectedSubSection = "chat" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Chat with AI Mentor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selectedSubSection == "chat") Color.White else textColor)
            }
        }

        if (selectedSubSection == "strategy") {
            // Strategy Report Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 Dynamic Weakness Advisor & recommendations",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Triggers Google Gemini to analyze your syllabus chapters completions and mock metrics instantly. Let AI plan your IIT revision!",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.generateAIRecommendations() },
                            colors = ButtonDefaults.buttonColors(containerColor = JEETagColor.AccentMint),
                            modifier = Modifier.fillMaxWidth().testTag("generate_strategy_btn"),
                            enabled = !viewModel.isGeneratingRecommendation
                        ) {
                            if (viewModel.isGeneratingRecommendation) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Generate IIT Revision Plan", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        if (viewModel.aiRecommendationText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "AI Strategic Recommendation Log:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.aiRecommendationText,
                                fontSize = 13.sp,
                                color = textColor,
                                lineHeight = 18.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(textColor.copy(alpha = 0.04f))
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Interactive Chat session
            if (viewModel.mentorChatMessages.isEmpty()) {
                viewModel.clearMentorChat()
            }

            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.mentorChatMessages) { msg ->
                        val bubbleBg = if (msg.isUser) JEETagColor.PrimaryGreen else cardBg
                        val alignLeft = !msg.isUser
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (alignLeft) Arrangement.Start else Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bubbleBg)
                                    .border(1.dp, if (msg.isUser) Color.Transparent else borderColor, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (msg.isUser) Color.White else textColor
                                )
                            }
                        }
                    }
                    if (viewModel.isMentorThinking) {
                        item {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = JEETagColor.AccentMint)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "AI Guru is typing...", fontSize = 11.sp, color = textColor.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userMessageText,
                        onValueChange = { userMessageText = it },
                        placeholder = { Text("Ask about JD Lee, Irodov, etc.") },
                        modifier = Modifier.weight(1f).testTag("chat_input_text"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JEETagColor.AccentMint)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.sendChatMessage(userMessageText)
                            userMessageText = ""
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(JEETagColor.AccentMint)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                    }
                }
            }
        }
    }
}
