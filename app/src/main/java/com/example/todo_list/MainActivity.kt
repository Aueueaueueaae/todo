
package com.example.todo_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

    Scaffold(
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

    Column {
        TopAppBar(
            title = { Text("Tasks") },
            actions = {
                IconButton(onClick = { onEditModeChange(!editMode) }) {
                    Icon(
                        imageVector = if (editMode)
                            Icons.Default.Check
                        else
                            Icons.Default.Edit,
                        contentDescription = "Edit mode"
                    )
                }
            }
        )


        LazyColumn {
            items(tasks, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    editMode = editMode,
                    onDelete = { tasks.remove(task) },
                    onEdit = { taskToEdit = task }
                )
            }
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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(today.format(DateTimeFormatter.ISO_DATE), fontSize = 22.sp)
        Text("Tasks today: ${todayTasks.size}", fontWeight = FontWeight.Bold)

        LazyColumn {
            items(todayTasks) { task ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = task.completed,
                        onCheckedChange = { task.completed = it }
                    )
                    Text(
                        task.title,
                        style = TextStyle(
                            textDecoration = if (task.completed)
                                TextDecoration.LineThrough
                            else TextDecoration.None
                        )
                    )
                }

            }
        }
    }
}


@Composable
fun DetailsTab(tasks: MutableList<TodoTask>) {
    var taskToEdit by remember { mutableStateOf<TodoTask?>(null) }
    LazyColumn {
        items(tasks, key = { it.id }) { task ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { taskToEdit = task }
                    .padding(12.dp)
            ) {
                Text(task.title, fontWeight = FontWeight.Bold)

                if (task.description.isNotBlank()) {
                    Text(task.description, fontSize = 14.sp)
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

                Text(
                    text = "Created: ${task.createdAt.toLocalDate()}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }

}
