package com.thanhtung.androidtaskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thanhtung.androidtaskmanager.ui.theme.AndroidTaskManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidTaskManagerTheme {
                TaskManagerApp()
            }
        }
    }
}

private data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val status: TaskStatus,
)

private enum class TaskStatus(val label: String) {
    TODO("Todo"),
    DOING("Doing"),
    DONE("Done"),
}

@Composable
private fun TaskManagerApp() {
    val tasks = remember {
        mutableStateListOf(
            Task(
                id = 1,
                title = "Finish Android project setup",
                description = "Configure package name, Gradle, Git, and GitHub repository.",
                status = TaskStatus.DONE,
            ),
            Task(
                id = 2,
                title = "Build task list UI",
                description = "Create a clean Compose screen for showing tasks and statuses.",
                status = TaskStatus.DOING,
            ),
            Task(
                id = 3,
                title = "Add local storage",
                description = "Persist tasks locally in a later version of the app.",
                status = TaskStatus.TODO,
            ),
        )
    }
    var nextTaskId by remember { mutableStateOf(4) }
    var selectedStatus by remember { mutableStateOf<TaskStatus?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val visibleTasks = remember(tasks.toList(), selectedStatus) {
        if (selectedStatus == null) {
            tasks
        } else {
            tasks.filter { it.status == selectedStatus }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
            ) {
                Text("Add task")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Header(
                totalTasks = tasks.size,
                doneTasks = tasks.count { it.status == TaskStatus.DONE },
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatusFilterRow(
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it },
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (visibleTasks.isEmpty()) {
                EmptyTaskState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(visibleTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onStatusChange = { newStatus ->
                                val index = tasks.indexOfFirst { it.id == task.id }
                                if (index >= 0) {
                                    tasks[index] = task.copy(status = newStatus)
                                }
                            },
                            onDelete = {
                                tasks.removeAll { it.id == task.id }
                            },
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAddTask = { title, description, status ->
                tasks.add(
                    Task(
                        id = nextTaskId,
                        title = title,
                        description = description,
                        status = status,
                    ),
                )
                nextTaskId += 1
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun Header(totalTasks: Int, doneTasks: Int) {
    Column {
        Text(
            text = "Android Task Manager",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$doneTasks of $totalTasks tasks completed",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun StatusFilterRow(
    selectedStatus: TaskStatus?,
    onStatusSelected: (TaskStatus?) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        FilterChip(
            selected = selectedStatus == null,
            onClick = { onStatusSelected(null) },
            label = { Text("All") },
        )
        TaskStatus.entries.forEach { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(status.label) },
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StatusPill(status = task.status)
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = task.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Move to:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.width(8.dp))
                TaskStatus.entries.forEach { status ->
                    TextButton(
                        enabled = status != task.status,
                        onClick = { onStatusChange(status) },
                    ) {
                        Text(status.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: TaskStatus) {
    val color = when (status) {
        TaskStatus.TODO -> Color(0xFF8A4B00)
        TaskStatus.DOING -> Color(0xFF0B5CAD)
        TaskStatus.DONE -> Color(0xFF1B6B3A)
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = CircleShape,
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EmptyTaskState() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "No tasks here",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Try another filter or add a new task.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, String, TaskStatus) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TaskStatus.TODO) }
    val isTitleValid = title.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add new task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    TaskStatus.entries.forEach { item ->
                        FilterChip(
                            selected = status == item,
                            onClick = { status = item },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isTitleValid,
                onClick = {
                    onAddTask(
                        title.trim(),
                        description.trim().ifEmpty { "No description" },
                        status,
                    )
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun TaskManagerPreview() {
    AndroidTaskManagerTheme {
        Surface(modifier = Modifier.size(width = 390.dp, height = 820.dp)) {
            TaskManagerApp()
        }
    }
}
