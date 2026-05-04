
package com.example.todo_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.example.todo_list.ui.theme.PatternBackground
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.animation.animateContentSize
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TodoApp()
            }
        }
    }
}
sealed class BottomTab(
    val title: String,
    val icon: ImageVector
) {
    object Tasks : BottomTab("Tasks", Icons.Default.Done)
    object Today : BottomTab("Today", Icons.Default.Home)
    object Details : BottomTab("Details", Icons.Default.List)
}


class TodoTask(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String = "",
    var date: LocalDate,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    completed: Boolean = false
) {
    var completed by mutableStateOf(completed)
}


@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onSave: (TodoTask) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val date = remember { LocalDate.now() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            TodoTask(
                                title = title,
                                description = description,
                                date = date
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        },
        title = { Text("New task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Text("Date: $date")
            }
        }
    )
}
@Composable
fun EditTaskDialog(
    task: TodoTask,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit task") },
        text = {
            Column {
                Text(
                    text = "Last edit: ${task.updatedAt.toLocalDate()}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    task.title = title
                    task.description = description
                    task.updatedAt = LocalDateTime.now()
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    )
}

@Composable
fun TodoApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var editMode by remember { mutableStateOf(false) }

    val tasks = remember { mutableStateListOf<TodoTask>() }

    PatternBackground {
        Scaffold(
            containerColor = Color.Transparent,  // <--- ДОБАВЬТЕ ЭТУ СТРОКУ
            bottomBar = {
                BottomNavigationBar(selectedTab) { selectedTab = it }
            },
            floatingActionButton = {
                var showCreateDialog by remember { mutableStateOf(false) }

                FloatingActionButton(
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }

                if (showCreateDialog) {
                    CreateTaskDialog(
                        onDismiss = { showCreateDialog = false },
                        onSave = { task ->
                            tasks.add(task)
                            showCreateDialog = false
                        }
                    )
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> TasksTab(tasks, editMode) { editMode = it }
                    1 -> TodayTab(tasks)
                    2 -> DetailsTab(tasks)
                }
            }
        }
    }
}
@Composable
fun BottomNavigationBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val tabs = listOf(
        BottomTab.Tasks,
        BottomTab.Today,
        BottomTab.Details
    )

    NavigationBar {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .then(
                                if (selectedIndex == index)
                                    Modifier.clip(CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    }
                },
                label = { Text(tab.title) }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTab(tasks: MutableList<TodoTask>, editMode: Boolean, onEditModeChange: (Boolean) -> Unit) {
    var taskToEdit by remember { mutableStateOf<TodoTask?>(null) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // РЕАЛЬНЫЙ размер картинки
    val actualCharacterHeight = 280.dp

    // ВИРТУАЛЬНАЯ высота для расчёта облачка
    val calculatedCharacterHeight = 180.dp

    val topBarHeight = 100.dp
    val navigationBarHeight = 80.dp

    // Максимальная высота облачка
    val maxCloudHeight = screenHeight - topBarHeight - calculatedCharacterHeight - navigationBarHeight

    val taskRowHeight = 70.dp
    val maxTasksWithoutScroll = if (maxCloudHeight.value > 0) {
        (maxCloudHeight.value / taskRowHeight.value).toInt()
    } else {
        3
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. СНАЧАЛА основной контент (облачко будет ПОД персонажем)
        Column {
            // Шапка с облачком
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = false
                    )
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(48.dp))

                    Text(
                        text = "Tasks",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A1D6D),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    IconButton(onClick = { onEditModeChange(!editMode) }) {
                        Icon(
                            imageVector = if (editMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = "Edit mode",
                            tint = Color(0xFF7C3AED)
                        )
                    }
                }
            }

            // Облачко с задачами
            if (tasks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateContentSize()
                        .heightIn(
                            min = 0.dp,
                            max = maxCloudHeight
                        )
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    if (tasks.size <= maxTasksWithoutScroll) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tasks.forEach { task ->
                                TaskRowInsideCloud(
                                    task = task,
                                    editMode = editMode,
                                    onDelete = { tasks.remove(task) },
                                    onEdit = { taskToEdit = task }
                                )

                                if (tasks.indexOf(task) < tasks.size - 1) {
                                    Divider(
                                        color = Color(0xFFE0D4F5),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(tasks, key = { it.id }) { task ->
                                TaskRowInsideCloud(
                                    task = task,
                                    editMode = editMode,
                                    onDelete = { tasks.remove(task) },
                                    onEdit = { taskToEdit = task }
                                )

                                if (tasks.indexOf(task) < tasks.size - 1) {
                                    Divider(
                                        color = Color(0xFFE0D4F5),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. ПОТОМ персонаж (будет ПОВЕРХ облачка, но на том же месте)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Image(
                painter = painterResource(id = R.drawable.ayanokoji),
                contentDescription = "Anime character",
                modifier = Modifier
                    .size(actualCharacterHeight)
                    .offset(x = (-10).dp, y = 10.dp)
                    .graphicsLayer {
                        alpha = 1f
                    }
            )
        }

        if (taskToEdit != null) {
            EditTaskDialog(
                task = taskToEdit!!,
                onDismiss = { taskToEdit = null },
                onDelete = {
                    tasks.remove(taskToEdit!!)
                    taskToEdit = null
                }
            )
        }
    }
}

// Строка задачи ВНУТРИ общего облачка (без своего фона и тени)
@Composable
fun TaskRowInsideCloud(
    task: TodoTask,
    editMode: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Дата слева
        Column(
            modifier = Modifier.width(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = task.date.dayOfMonth.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A1D6D)
            )
            Text(
                text = task.date.month.name.take(3),
                fontSize = 11.sp,
                color = Color(0xFF9B7BDD)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Название задачи
        Text(
            text = task.title,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (task.completed) Color(0xFFB3A0D0) else Color(0xFF2D1B4E)
        )

        // Кнопки
        if (!editMode) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { task.completed = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF7C3AED),
                    uncheckedColor = Color(0xFFB3A0D0)
                )
            )
        } else {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF7C3AED)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5252)
                    )
                }
            }
        }
    }
}

// Новая карточка для задачи (как облачко)
@Composable
fun TaskCard(
    task: TodoTask,
    editMode: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .background(
                color = Color(0xFFF5F0FF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Дата слева
        Column(
            modifier = Modifier.width(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = task.date.dayOfMonth.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A1D6D)
            )
            Text(
                text = task.date.month.name.take(3),
                fontSize = 11.sp,
                color = Color(0xFF9B7BDD)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Название задачи
        Text(
            text = task.title,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (task.completed) Color(0xFFB3A0D0) else Color(0xFF2D1B4E)
        )

        // Кнопки в зависимости от режима
        if (!editMode) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { task.completed = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF7C3AED),
                    uncheckedColor = Color(0xFFB3A0D0)
                )
            )
        } else {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF7C3AED)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5252)
                    )
                }
            }
        }
    }
}


@Composable
fun TaskRow(
    task: TodoTask,
    editMode: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.width(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = task.date.dayOfMonth.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = task.date.month.name.take(3),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = task.title,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                textDecoration = if (task.completed)
                    TextDecoration.LineThrough
                else TextDecoration.None
            )
        )

        if (!editMode) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { task.completed = it }
            )
        } else {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}



@Composable
fun TodayTab(tasks: List<TodoTask>) {
    val today = LocalDate.now()
    val todayTasks = tasks.filter { it.date == today }

    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    var currentDate by remember { mutableStateOf(getCurrentDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            currentTime = getCurrentTime()
            currentDate = getCurrentDate()
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // ФИКСИРОВАННЫЕ РАЗМЕРЫ
    val characterHeight = 280.dp           // высота персонажа
    val topBarHeight = 100.dp              // высота шапки "Tasks" с отступами
    val navigationBarHeight = 80.dp        // навигационная панель снизу
    val timeDateHeaderHeight = 140.dp      // высота шапки с временем и датой

    // Максимальная высота облачка
    val maxCloudHeight = screenHeight - topBarHeight - characterHeight - navigationBarHeight - timeDateHeaderHeight

    // Примерная высота одной задачи
    val taskRowHeight = 70.dp

    // Сколько задач помещается до появления скролла
    val maxTasksWithoutScroll = if (maxCloudHeight.value > 0) {
        (maxCloudHeight.value / taskRowHeight.value).toInt()
    } else {
        3
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Аниме персонаж в левом нижнем углу
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Image(
                painter = painterResource(id = R.drawable.ayanokoji1),
                contentDescription = "Anime character",
                modifier = Modifier
                    .size(characterHeight)
                    .offset(x = (-10).dp, y = 10.dp)
                    .graphicsLayer {
                        alpha = 1f
                    }
            )
        }

        // Основной контент
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Шапка с временем и датой
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Облачко для времени
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentTime,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A1D6D),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "время",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9B7BDD),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Облачко для даты
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentDate.day,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED),
                            lineHeight = 48.sp
                        )
                        Text(
                            text = "${currentDate.month} ${currentDate.year}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9B7BDD),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Список задач на сегодня
            if (todayTasks.isNotEmpty()) {
                Text(
                    text = "Задач на сегодня: ${todayTasks.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9B7BDD),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Облачко со списком задач
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = maxCloudHeight)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    if (todayTasks.size <= maxTasksWithoutScroll) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            todayTasks.forEach { task ->
                                TodayTaskRow(task = task)

                                if (todayTasks.indexOf(task) < todayTasks.size - 1) {
                                    Divider(
                                        color = Color(0xFFE0D4F5),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(todayTasks, key = { it.id }) { task ->
                                TodayTaskRow(task = task)

                                if (todayTasks.indexOf(task) < todayTasks.size - 1) {
                                    Divider(
                                        color = Color(0xFFE0D4F5),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Если задач на сегодня нет
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(32.dp)
                ) {
                    Text(
                        text = "На сегодня задач нет",
                        fontSize = 18.sp,
                        color = Color(0xFF9B7BDD),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
// Вспомогательные функции
private fun getCurrentTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return LocalDateTime.now().format(formatter)
}

private fun getCurrentDate(): DateInfo {
    val now = LocalDate.now()
    val dayFormatter = DateTimeFormatter.ofPattern("dd")
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale("ru"))
    val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

    return DateInfo(
        day = now.format(dayFormatter),
        month = now.format(monthFormatter).replaceFirstChar { it.uppercase() },
        year = now.format(yearFormatter)
    )
}

data class DateInfo(
    val day: String,
    val month: String,
    val year: String
)

@Composable
fun TodayTaskRow(task: TodoTask) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.completed,
            onCheckedChange = { task.completed = it },
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF7C3AED),
                uncheckedColor = Color(0xFFB3A0D0)
            )
        )

        Text(
            text = task.title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (task.completed) Color(0xFFB3A0D0) else Color(0xFF2D1B4E)
        )
    }
}

@Composable
fun DetailsTab(tasks: MutableList<TodoTask>) {
    var taskToEdit by remember { mutableStateOf<TodoTask?>(null) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // РЕАЛЬНЫЙ размер картинки (меняйте, если хотите изменить размер персонажа)
    val actualCharacterHeight = 280.dp

    // ВИРТУАЛЬНАЯ высота для расчёта облачка (скролл появится раньше)
    val calculatedCharacterHeight = 180.dp  // ← облачко будет останавливаться выше

    val topBarHeight = 70.dp               // высота шапки "Все задачи"
    val navigationBarHeight = 80.dp        // навигационная панель снизу

    // Максимальная высота облачка (используем calculatedCharacterHeight)
    val maxCloudHeight = screenHeight - topBarHeight - calculatedCharacterHeight - navigationBarHeight

    // Примерная высота одной задачи
    val taskRowHeight = 100.dp

    // Сколько задач помещается до появления скролла
    val maxTasksWithoutScroll = if (maxCloudHeight.value > 0) {
        (maxCloudHeight.value / taskRowHeight.value).toInt()
    } else {
        3
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. СНАЧАЛА основной контент (облачко будет ПОД персонажем)
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Заголовок
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = false
                    )
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Все задачи",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A1D6D),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Облачко со списком задач
            if (tasks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = maxCloudHeight)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    if (tasks.size <= maxTasksWithoutScroll) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tasks.forEach { task ->
                                DetailsTaskRow(
                                    task = task,
                                    onEdit = { taskToEdit = task }
                                )

                                if (tasks.indexOf(task) < tasks.size - 1) {
                                    Divider(
                                        color = Color(0xFFE0D4F5),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(tasks, key = { it.id }) { task ->
                                DetailsTaskRow(
                                    task = task,
                                    onEdit = { taskToEdit = task }
                                )

                                if (tasks.indexOf(task) < tasks.size - 1) {
                                    Divider(
                                        color = Color(0xFFE0D4F5),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Если задач нет
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(32.dp)
                ) {
                    Text(
                        text = "Нет задач",
                        fontSize = 16.sp,
                        color = Color(0xFF9B7BDD),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 2. ПОТОМ персонаж (будет ПОВЕРХ облачка)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Image(
                painter = painterResource(id = R.drawable.ayanokoji2),
                contentDescription = "Anime character",
                modifier = Modifier
                    .size(actualCharacterHeight)
                    .offset(x = (-10).dp, y = 10.dp)
                    .graphicsLayer {
                        alpha = 1f
                    }
            )
        }

        if (taskToEdit != null) {
            EditTaskDialog(
                task = taskToEdit!!,
                onDismiss = { taskToEdit = null },
                onDelete = {
                    tasks.remove(taskToEdit!!)
                    taskToEdit = null
                }
            )
        }
    }
}

@Composable
fun DetailsTaskRow(
    task: TodoTask,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = task.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D1B4E)
        )

        if (task.description.isNotBlank()) {
            Text(
                text = task.description,
                fontSize = 14.sp,
                color = Color(0xFF9B7BDD),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(
            text = "Создано: ${task.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
            fontSize = 12.sp,
            color = Color(0xFFB3A0D0),
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Дата задачи: ${task.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
            fontSize = 12.sp,
            color = Color(0xFFB3A0D0),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
