package com.example.testproject // ← Твой пакет!

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============ ТЕМА ============
private val LightColors = lightColorScheme(
    primary = Color(0xFF006A4E),
    onPrimary = Color.White,
    secondary = Color(0xFF4B6356),
    background = Color(0xFFFBFDF9),
    surface = Color(0xFFFBFDF9),
    onBackground = Color(0xFF191C1A),
    onSurface = Color(0xFF191C1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6CDBB7),
    onPrimary = Color(0xFF003827),
    secondary = Color(0xFFB3CCC0),
    background = Color(0xFF191C1A),
    surface = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3E0),
    onSurface = Color(0xFFE1E3E0),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
// ============ КОНЕЦ ТЕМЫ ============

// ============ НАВИГАЦИЯ ============
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Challenge : Screen("challenge/{challengeId}") {
        fun createRoute(challengeId: Int) = "challenge/$challengeId"
    }
}
// ============ КОНЕЦ НАВИГАЦИИ ============

// ============ ГЛАВНЫЙ ЭКРАН ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onChallengeClick: (Int) -> Unit
) {
    val challenges = viewModel.getChallenges()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kotlin Koans") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            items(challenges) { challenge ->
                ChallengeCard(challenge = challenge, onClick = { onChallengeClick(challenge.id) })
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: Challenge, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (challenge.difficulty) {
                            ChallengeDifficulty.EASY -> Color(0xFF4CAF50)
                            ChallengeDifficulty.MEDIUM -> Color(0xFFFFC107)
                            ChallengeDifficulty.HARD -> Color(0xFFF44336)
                        }
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "${challenge.id}. ${challenge.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
// ============ КОНЕЦ ГЛАВНОГО ЭКРАНА ============

// ============ ЭКРАН ЗАДАНИЯ (С РЕДАКТОРОМ КОДА!) ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    challengeId: Int,
    onBackClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val challenge = viewModel.getChallengeById(challengeId)
    var codeState by remember { mutableStateOf(TextFieldValue(challenge?.initialCode ?: "")) }
    var output by remember { mutableStateOf("") }
    var solutionVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(challenge?.title ?: "Задание") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Назад", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (challenge != null) {
                Text(
                    text = "${challenge.id}. ${challenge.title}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = challenge.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // Поле ввода кода
                OutlinedTextField(
                    value = codeState,
                    onValueChange = { codeState = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 250.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    label = { Text("Ваш код") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                output = "Выполняется..."
                                val result = withContext(Dispatchers.IO) {
                                    // Просто возвращаем код пользователя как "вывод"
                                    codeState.text
                                        .lines()
                                        .filter { it.contains("println(") }
                                        .joinToString("\n") { line ->
                                            line.substringAfter("println(").substringBefore(")")
                                        }
                                }
                                // Сравниваем вывод пользователя с ожидаемым
                                output = if (result.replace("\"", "").trim() == challenge.expectedOutput.replace("\"", "").trim()) {
                                    "✅ Успех!\n\nВывод:\n$result"
                                } else {
                                    "❌ Ожидалось:\n${challenge.expectedOutput}\n\nПолучено:\n$result"
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Запустить")
                    }

                    OutlinedButton(
                        onClick = {
                            codeState = TextFieldValue(challenge.initialCode)
                            output = ""
                            solutionVisible = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сбросить")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопка "Показать решение"
                TextButton(onClick = { solutionVisible = !solutionVisible }) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (solutionVisible) "Скрыть решение" else "Показать решение")
                }

                if (solutionVisible) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = "Решение:\n${challenge.expectedOutput}",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Вывод программы
                if (output.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = output,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

            } else {
                Text("Задание не найдено", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}
// ============ КОНЕЦ ЭКРАНА ЗАДАНИЯ ============

// ============ НАВИГАЦИЯ ============
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(onChallengeClick = { id -> navController.navigate(Screen.Challenge.createRoute(id)) })
        }
        composable(
            route = Screen.Challenge.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.IntType })
        ) { entry ->
            ChallengeScreen(
                challengeId = entry.arguments?.getInt("challengeId") ?: 0,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
// ============ КОНЕЦ НАВИГАЦИИ ============

// ============ ТОЧКА ВХОДА ============
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}