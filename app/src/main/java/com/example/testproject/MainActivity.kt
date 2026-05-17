package com.example.testproject // ← Твой пакет!

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.google.firebase.auth.FirebaseAuth
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
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
    primaryContainer = Color(0xFFE8F5E9),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6CDBB7),
    onPrimary = Color(0xFF003827),
    secondary = Color(0xFFB3CCC0),
    background = Color(0xFF191C1A),
    surface = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3E0),
    onSurface = Color(0xFFE1E3E0),
    primaryContainer = Color(0xFF1B3B2F),
)

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}

// ============ НАВИГАЦИЯ ============
sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")

    object Profile : Screen("profile")
    object Challenges : Screen("challenges")
    object Challenge : Screen("challenge/{challengeId}") {
        fun createRoute(challengeId: Int) = "challenge/$challengeId"
    }
    object Theory : Screen("theory")
    object TheoryDetail : Screen("theory/{topicId}") {
        fun createRoute(topicId: Int) = "theory/$topicId"
    }
    object Sandbox : Screen("sandbox")
}


// ============ ДАННЫЕ ДЛЯ ТЕОРИИ ============
data class TheoryTopic(
    val id: Int,
    val title: String,
    val icon: String,
    val description: String,
    val content: String
)

class TheoryRepository {
    val topics = listOf(
        TheoryTopic(1, "Переменные и типы", "📦", "val, var и основные типы данных", "В Kotlin есть два ключевых слова для объявления переменных:\n\n• val — неизменяемая переменная (read-only)\n• var — изменяемая переменная\n\nОсновные типы: Int, Double, String, Boolean, Char, Long, Float, Short, Byte."),
        TheoryTopic(2, "Функции", "🔧", "Объявление и использование функций", "Функции объявляются с помощью ключевого слова fun:\n\nfun greet(name: String): String {\n    return \"Hello, \$name!\"\n}\n\nМожно использовать сокращённый синтаксис:\nfun greet(name: String) = \"Hello, \$name!\""),
        TheoryTopic(3, "Условные выражения", "🔀", "if, when и их использование", "В Kotlin if является выражением:\n\nval max = if (a > b) a else b\n\nwhen — мощная замена switch:\nwhen (x) {\n    1 -> \"один\"\n    2 -> \"два\"\n    else -> \"много\"\n}"),
        TheoryTopic(4, "Циклы", "🔄", "for, while и итерации", "Цикл for:\nfor (item in collection) { }\nfor (i in 1..10) { }\n\nЦикл while:\nwhile (condition) { }"),
        TheoryTopic(5, "Коллекции", "📚", "List, Set, Map", "List — упорядоченная коллекция\nSet — уникальные элементы\nMap — пары ключ-значение\n\nval list = listOf(1, 2, 3)\nval map = mapOf(\"a\" to 1, \"b\" to 2)"),
        TheoryTopic(6, "Null-безопасность", "🛡️", "Работа с null в Kotlin", "Типы делятся на nullable и non-null:\n\nvar name: String = \"Hello\" // не может быть null\nvar name: String? = null // может быть null\n\nОператоры: ?. (safe call), ?: (Elvis), !! (утверждение)"),
        TheoryTopic(7, "Классы и объекты", "🏗️", "ООП в Kotlin", "class Person(val name: String, var age: Int)\n\ndata class — автоматически генерирует equals(), hashCode(), toString()\n\nobject — синглтон"),
        TheoryTopic(8, "Расширения", "🧩", "Extension functions", "Можно добавлять методы к существующим классам:\n\nfun String.isEmail(): Boolean = this.contains(\"@\")\n\n\"test@mail.ru\".isEmail() // true"),
    )
}

// ============ ЭКРАН ТЕОРИИ (СПИСОК ТЕМ) ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryListScreen(
    onTopicClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val repository = remember { TheoryRepository() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Теория") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Назад", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            items(repository.topics) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTopicClick(topic.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(topic.icon, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = topic.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

// ============ ЭКРАН КОНКРЕТНОЙ ТЕМЫ ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryDetailScreen(
    topicId: Int,
    onBackClick: () -> Unit
) {
    val repository = remember { TheoryRepository() }
    val topic = repository.topics.find { it.id == topicId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topic?.title ?: "Теория") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Назад", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White
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
            if (topic != null) {
                Text(
                    text = "${topic.icon} ${topic.title}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = topic.content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 24.sp
                )
            } else {
                Text("Тема не найдена")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val userRepository = remember { UserRepository() }
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var operationInProgress by remember { mutableStateOf(false) }

    // Выбор аватарки
    var showAvatarDialog by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf(profile?.avatarUrl ?: "") }

    val coroutineScope = rememberCoroutineScope()

    // Список доступных аватарок (эмодзи)
    val avatarOptions = listOf(
        "👤", "🐱", "🦊", "🐼", "🐨", "🐸", "🦁", "🐯",
        "🐰", "🐙", "🦄", "🐳", "🦋", "🐉", "🦀", "🐧",
        "🤖", "👻", "🎃", "🌟", "🔥", "💎", "🎯", "🚀"
    )

    // Загрузка профиля
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val result = userRepository.getProfile()
            result.fold(
                onSuccess = {
                    profile = it
                    selectedAvatar = it.avatarUrl
                },
                onFailure = { errorMessage = it.message }
            )
        } catch (e: Exception) {
            errorMessage = e.message
        }
        isLoading = false
    }

    // Диалог выбора аватарки
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Выберите аватар") },
            text = {
                Column {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .height(300.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        items(avatarOptions) { emoji ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedAvatar == emoji) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedAvatar = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 24.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    operationInProgress = true
                    coroutineScope.launch {
                        val result = userRepository.updateAvatar(selectedAvatar)
                        result.fold(
                            onSuccess = {
                                profile = profile?.copy(avatarUrl = selectedAvatar)
                                showAvatarDialog = false
                            },
                            onFailure = { errorMessage = it.message }
                        )
                        operationInProgress = false
                    }
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Остальные диалоги...
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false; newPassword = "" },
            title = { Text("Сменить пароль") },
            text = {
                Column {
                    Text("Введите новый пароль (минимум 6 символов)")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Новый пароль") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPassword.length < 6) {
                        errorMessage = "Пароль должен быть не менее 6 символов"
                        return@TextButton
                    }
                    operationInProgress = true
                    coroutineScope.launch {
                        userRepository.updatePassword(newPassword)
                        operationInProgress = false
                        showPasswordDialog = false
                        newPassword = ""
                    }
                }) { Text("Сменить") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false; newPassword = "" }) { Text("Отмена") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить аккаунт?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        userRepository.deleteAccount()
                        onLogoutClick()
                    }
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (profile != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Аватар (кликабельный)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .clickable { showAvatarDialog = true }
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile!!.avatarUrl.isNotEmpty()) {
                        Text(profile!!.avatarUrl, fontSize = 40.sp)
                    } else {
                        Text(
                            text = profile!!.displayName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    "Сменить аватар",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Информация
                Text(
                    text = profile!!.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = profile!!.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                if (profile!!.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(profile!!.bio, modifier = Modifier.padding(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Статистика
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Статистика", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem(value = "${profile!!.level}", label = "Уровень", emoji = "⭐")
                            StatItem(value = "${profile!!.totalSolved}", label = "Решено", emoji = "✅")
                            StatItem(value = "0", label = "Достижения", emoji = "🏆")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки
                OutlinedButton(onClick = {
                    editName = profile!!.displayName
                    editBio = profile!!.bio
                    isEditing = true
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Редактировать профиль")
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Имя") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("О себе") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            coroutineScope.launch {
                                userRepository.updateProfile(editName, editBio)
                                profile = profile?.copy(displayName = editName, bio = editBio)
                                isEditing = false
                            }
                        }) { Text("Сохранить") }
                        OutlinedButton(onClick = { isEditing = false }) { Text("Отмена") }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(onClick = { showPasswordDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сменить пароль")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Управление аккаунтом", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Выйти из аккаунта")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Удалить аккаунт", color = MaterialTheme.colorScheme.error)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$emoji $value", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

// ============ ЭКРАН АВТОРИЗАЦИИ ============
// ============ ЭКРАН АВТОРИЗАЦИИ ============
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    val authManager = remember { AuthManager() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Запоминаем, что уже переходим на главный экран (чтобы не дёргаться)
    var navigateToHome by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Если уже вошёл или нужно перейти — сразу на главный экран
    LaunchedEffect(navigateToHome) {
        if (authManager.isLoggedIn() || navigateToHome) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🇰", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isLogin) "Вход" else "Регистрация",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Пароль") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Заполните все поля"
                    return@Button
                }
                if (password.length < 6) {
                    errorMessage = "Пароль должен быть не менее 6 символов"
                    return@Button
                }
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    val result = if (isLogin) {
                        authManager.login(email, password)
                    } else {
                        authManager.register(email, password)
                    }
                    isLoading = false
                    result.fold(
                        onSuccess = {
                            // Ставим флаг — и LaunchedEffect сам сделает переход
                            navigateToHome = true
                        },
                        onFailure = {
                            errorMessage = when {
                                it.message?.contains("no user record") == true -> "Пользователь не найден"
                                it.message?.contains("email address is already in use") == true -> "Email уже используется"
                                it.message?.contains("password is invalid") == true -> "Неверный пароль"
                                it.message?.contains("network error") == true -> "Ошибка сети. Проверьте интернет"
                                else -> it.message ?: "Неизвестная ошибка"
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLogin) "Входим..." else "Регистрируемся...")
            } else {
                Text(if (isLogin) "Войти" else "Зарегистрироваться")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            isLogin = !isLogin
            errorMessage = null
        }) {
            Text(if (isLogin) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти")
        }
    }
}

// ============ ГЛАВНЫЙ (ДОМАШНИЙ) ЭКРАН ============
@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onTheoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSandboxClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val solvedCount = viewModel.getSolvedCount()
    val totalCount = viewModel.getTotalCount()
    val progressPercent = viewModel.getProgressPercent()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Кнопка профиля в правом верхнем углу
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
                    }
                }

                Text("🇰", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Kotlin Koans",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Изучай Kotlin через практику",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(32.dp))

                if (solvedCount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Твой прогресс", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { progressPercent / 100f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$solvedCount из $totalCount заданий решено", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                HomeButton(icon = Icons.Default.Code, title = "Задания", subtitle = "$totalCount интерактивных задач", color = MaterialTheme.colorScheme.primary, onClick = onStartClick)
                Spacer(modifier = Modifier.height(12.dp))
                HomeButton(icon = Icons.Default.MenuBook, title = "Теория", subtitle = "Основы языка Kotlin", color = Color(0xFF1565C0), onClick = onTheoryClick)
                Spacer(modifier = Modifier.height(12.dp))
                // Кнопка "Песочница" вместо "Достижения"
                HomeButton(
                    icon = Icons.Default.Terminal,
                    title = "Песочница",
                    subtitle = "Пишите и запускайте код",
                    color = Color(0xFF7C4DFF),
                    onClick = onSandboxClick
                )

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("О приложении", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Kotlin Koans — это интерактивный тренажёр для изучения языка Kotlin.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Версия 2.0 • Дипломный проект", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HomeButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 2.dp else 0.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка в кружке
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (enabled) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ============ ЭКРАН СО СПИСКОМ ЗАДАНИЙ ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    viewModel: MainViewModel = viewModel(),
    onChallengeClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val challenges = viewModel.getChallenges()
    val solvedCount = viewModel.getSolvedCount()
    val totalCount = viewModel.getTotalCount()
    val progressPercent = viewModel.getProgressPercent()

    var filterState by remember { mutableStateOf(FilterState.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredChallenges = when (filterState) {
        FilterState.ALL -> challenges
        FilterState.SOLVED -> challenges.filter { viewModel.isSolved(it.id) }
        FilterState.UNSOLVED -> challenges.filter { !viewModel.isSolved(it.id) }
    }

    val searchedChallenges = if (searchQuery.isBlank()) {
        filteredChallenges
    } else {
        filteredChallenges.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сбросить прогресс?") },
            text = { Text("Будет удалён весь прогресс: решённые задачи и сохранённый код. Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAllProgress()
                    showResetDialog = false
                }) {
                    Text("Сбросить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задания") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Назад", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    if (solvedCount > 0) {
                        IconButton(onClick = { showResetDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Сбросить", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            // Прогресс
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Прогресс", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Решено: $solvedCount из $totalCount (${progressPercent.toInt()}%)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Поиск
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("🔍 Поиск по задачам...") },
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            TextButton(onClick = { searchQuery = "" }) { Text("✕") }
                        }
                    }
                )
            }

            // Фильтры
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = filterState == FilterState.ALL, onClick = { filterState = FilterState.ALL }, label = { Text("Все (${challenges.size})") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = filterState == FilterState.SOLVED, onClick = { filterState = FilterState.SOLVED }, label = { Text("Решено (${viewModel.getSolvedCount()})") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = filterState == FilterState.UNSOLVED, onClick = { filterState = FilterState.UNSOLVED }, label = { Text("Не решено (${totalCount - solvedCount})") }, modifier = Modifier.weight(1f))
                }
            }

            if (searchedChallenges.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (searchQuery.isNotBlank()) "Ничего не найдено по запросу \"$searchQuery\""
                            else "Нет задач для отображения",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                if (searchQuery.isNotBlank()) {
                    item { Text("Найдено: ${searchedChallenges.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                }
                items(searchedChallenges) { challenge ->
                    ChallengeCard(challenge = challenge, isSolved = viewModel.isSolved(challenge.id), onClick = { onChallengeClick(challenge.id) })
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: Challenge, isSolved: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSolved) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSolved) Text("✅", fontSize = 20.sp)
            else Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(
                when (challenge.difficulty) {
                    ChallengeDifficulty.EASY -> Color(0xFF4CAF50)
                    ChallengeDifficulty.MEDIUM -> Color(0xFFFFC107)
                    ChallengeDifficulty.HARD -> Color(0xFFF44336)
                }
            ))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("${challenge.id}. ${challenge.title}", style = MaterialTheme.typography.titleMedium, fontWeight = if (isSolved) FontWeight.Bold else FontWeight.Normal)
                Spacer(modifier = Modifier.height(4.dp))
                Text(challenge.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

// ============ ЭКРАН ЗАДАНИЯ ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    challengeId: Int,
    onBackClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val challenge = viewModel.getChallengeById(challengeId)
    val savedCode = viewModel.getSavedCode(challengeId)
    var codeState by remember { mutableStateOf(TextFieldValue(savedCode ?: challenge?.initialCode ?: "")) }
    var output by remember { mutableStateOf("") }
    var solutionVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isSolved = viewModel.isSolved(challengeId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(challenge?.title ?: "Задание") },
                navigationIcon = { TextButton(onClick = onBackClick) { Text("← Назад", color = MaterialTheme.colorScheme.onPrimary) } },
                actions = { if (isSolved) { Text("✅ Решено", color = MaterialTheme.colorScheme.onPrimary); Spacer(modifier = Modifier.width(16.dp)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (challenge != null) {
                Text("${challenge.id}. ${challenge.title}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(challenge.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = codeState,
                    onValueChange = { codeState = it; viewModel.saveCode(challengeId, it.text) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 250.dp),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                    label = { Text("Ваш код") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                output = "Выполняется..."
                                val result = withContext(Dispatchers.IO) {
                                    // Извлекаем всё, что внутри println()
                                    codeState.text
                                        .lines()
                                        .filter { it.contains("println(") }
                                        .joinToString("\n") { line ->
                                            line.substringAfter("println(")
                                                .substringBeforeLast(")")
                                                .trim()
                                                .removeSurrounding("\"")
                                        }
                                }
                                val expected = challenge.expectedOutput.trim()

                                // Очищаем результат и ожидаемый вывод
                                val resultClean = result.trim().replace("\r\n", "\n").replace("\r", "\n")
                                val expectedClean = expected.trim().replace("\r\n", "\n").replace("\r", "\n")

                                if (resultClean.equals(expectedClean, ignoreCase = true) ||
                                    resultClean.replace(" ", "") == expectedClean.replace(" ", "")) {
                                    viewModel.markSolved(challengeId)
                                    // Синхронизируем с Firestore
                                    val userRepository = UserRepository()
                                    val solvedCount = viewModel.getSolvedCount()
                                    val level = (solvedCount / 5) + 1
                                    userRepository.updateStats(level, solvedCount)
                                    output = "✅ Успех!\n\nВывод:\n$result"
                                } else {
                                    output = "❌ Ожидалось:\n${challenge.expectedOutput}\n\nПолучено:\n$result"
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Запустить")
                    }
                    OutlinedButton(onClick = {
                        codeState = TextFieldValue(challenge.initialCode)
                        output = ""
                        solutionVisible = false
                        viewModel.saveCode(challengeId, challenge.initialCode)
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сбросить")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { solutionVisible = !solutionVisible }) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (solutionVisible) "Скрыть решение" else "Показать решение")
                }

                if (solutionVisible) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text("Правильный ответ:\n${challenge.expectedOutput}", modifier = Modifier.padding(16.dp))
                    }
                }

                if (output.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(output, modifier = Modifier.padding(16.dp), fontFamily = FontFamily.Monospace)
                    }
                }
            } else Text("Задание не найдено")
        }
    }
}

// ============ ЭКРАН ПЕСОЧНИЦЫ (КОМПИЛЯТОР) ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxScreen(
    onBackClick: () -> Unit
) {
    var code by remember { mutableStateOf("println(\"Hello, Kotlin!\")") }
    var output by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Песочница Kotlin") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Назад", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7C4DFF),  // Фиолетовый
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Пишите код на Kotlin и сразу выполняйте!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Поле ввода кода
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                label = { Text("Ваш код") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isRunning = true
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                KotlinCompiler.executeCode(code)
                            }
                            output = result.ifEmpty { "Код выполнен (нет вывода)" }
                            isRunning = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isRunning
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Запустить")
                }

                OutlinedButton(
                    onClick = {
                        code = ""
                        output = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Очистить")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Примеры кода
            Text(
                text = "Примеры:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { code = "println(\"Hello, Kotlin!\")" },
                    label = { Text("Hello") }
                )
                AssistChip(
                    onClick = { code = "val sum = 2 + 2\nprintln(\"2 + 2 = \$sum\")" },
                    label = { Text("Сумма") }
                )
                AssistChip(
                    onClick = { code = "for (i in 1..5) {\n    println(i)\n}" },
                    label = { Text("Цикл") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Вывод
            if (output.isNotEmpty()) {
                Text(
                    text = "Вывод:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = output,
                        modifier = Modifier.padding(16.dp),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ============ НАВИГАЦИЯ ============
@Composable
fun AppNavGraph(navController: NavHostController) {
    val authManager = remember { AuthManager() }

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onStartClick = { navController.navigate(Screen.Challenges.route) },
                onTheoryClick = { navController.navigate(Screen.Theory.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onSandboxClick = { navController.navigate(Screen.Sandbox.route) },
                viewModel = viewModel()
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authManager.logout()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Список заданий
        composable(Screen.Challenges.route) {
            ChallengesScreen(
                onChallengeClick = { id -> navController.navigate(Screen.Challenge.createRoute(id)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Экран задания
        composable(
            route = Screen.Challenge.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.IntType })
        ) { entry ->
            ChallengeScreen(
                challengeId = entry.arguments?.getInt("challengeId") ?: 0,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Список тем теории
        composable(Screen.Theory.route) {
            TheoryListScreen(
                onTopicClick = { id -> navController.navigate(Screen.TheoryDetail.createRoute(id)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Конкретная тема
        composable(
            route = Screen.TheoryDetail.route,
            arguments = listOf(navArgument("topicId") { type = NavType.IntType })
        ) { entry ->
            TheoryDetailScreen(
                topicId = entry.arguments?.getInt("topicId") ?: 1,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Sandbox.route) {
            SandboxScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

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

enum class FilterState { ALL, SOLVED, UNSOLVED }