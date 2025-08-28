package com.example.taskmaster.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskmaster.data.database.Task
import com.example.taskmaster.viewmodel.TaskViewModel
import com.example.taskmaster.viewmodel.provideTaskViewModelFactory
import com.example.taskmaster.notification.NotificationHelper
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: TaskViewModel = viewModel(
        factory = provideTaskViewModelFactory(context)
    )
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showEditTaskDialog by remember { mutableStateOf<Task?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf("deadline") }
    
    val tasks by viewModel.tasks.collectAsState()
    val sortedByDeadline by viewModel.sortedByDeadline.collectAsState()
    val sortedByCategory by viewModel.sortedByPriority.collectAsState()
    
    // Filter out completed tasks and group by category
    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }
    
    val currentTasks = when (currentSort) {
        "deadline" -> sortedByDeadline.filter { !it.isCompleted }
        "category" -> sortedByCategory.filter { !it.isCompleted }
        else -> activeTasks
    }
    
    // Group tasks by category for category view
    val tasksByCategory = if (currentSort == "category") {
        currentTasks.groupBy { it.priority }
    } else {
        emptyMap()
    }
    
    // Get unique existing categories for suggestions
    val existingCategories = tasks.map { it.priority }.distinct().filter { it.isNotBlank() }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Tasks (${activeTasks.size})") },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, "Sort options")
                    }
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(Icons.Default.History, "View completed tasks")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Deadline") },
                            onClick = {
                                currentSort = "deadline"
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Category") },
                            onClick = {
                                currentSort = "category"
                                showSortMenu = false
                            }
                        )
                    }
                    // Add this in the TopAppBar actions:
                    TextButton(
                        onClick = { 
                            val notificationHelper = NotificationHelper(context)
                            notificationHelper.showTestNotification()
                        }
                    ) {
                        Text("Test Notification")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Default.Add, "Add Task")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (currentSort == "category") {
                // Show tasks grouped by category
                tasksByCategory.forEach { (category, categoryTasks) ->
                    item {
                        Text(
                            text = "Category: $category",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(categoryTasks) { task ->
                        TaskItem(
                            task = task,
                            onTaskUpdated = { updatedTask ->
                                viewModel.updateTask(updatedTask)
                            },
                            onTaskDeleted = { taskToDelete ->
                                viewModel.deleteTask(taskToDelete)
                            },
                            onTaskEdit = { taskToEdit ->
                                showEditTaskDialog = taskToEdit
                            }
                        )
                    }
                }
            } else {
                // Show tasks in regular list
                items(currentTasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskUpdated = { updatedTask ->
                            viewModel.updateTask(updatedTask)
                        },
                        onTaskDeleted = { taskToDelete ->
                            viewModel.deleteTask(taskToDelete)
                        },
                        onTaskEdit = { taskToEdit ->
                            showEditTaskDialog = taskToEdit
                        }
                    )
                }
            }
        }
    }
    
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskAdded = { newTask ->
                viewModel.insertTask(newTask)
                showAddTaskDialog = false
            },
            existingCategories = existingCategories
        )
    }
    
    if (showEditTaskDialog != null) {
        EditTaskDialog(
            task = showEditTaskDialog!!,
            onDismiss = { showEditTaskDialog = null },
            onTaskUpdated = { updatedTask ->
                viewModel.updateTask(updatedTask)
                showEditTaskDialog = null
            },
            existingCategories = existingCategories
        )
    }
    
    if (showHistoryDialog) {
        HistoryDialog(
            completedTasks = completedTasks,
            onDismiss = { showHistoryDialog = false }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskUpdated: (Task) -> Unit,
    onTaskDeleted: (Task) -> Unit,
    onTaskEdit: (Task) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { isChecked ->
                            onTaskUpdated(task.copy(isCompleted = isChecked))
                        }
                    )
                    IconButton(
                        onClick = { onTaskEdit(task) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit task"
                        )
                    }
                    IconButton(
                        onClick = { onTaskDeleted(task) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete task"
                        )
                    }
                }
            }
            
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Category: ${task.priority}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Due: ${dateFormat.format(Date(task.deadline))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (Task) -> Unit,
    existingCategories: List<String>
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedHour by remember { mutableIntStateOf(selectedDate.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(selectedDate.get(Calendar.MINUTE)) }
    var selectedNotificationTime by remember { mutableLongStateOf(0L) } // 0 = no notification
    var showValidationError by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Add some default categories if no existing ones
    val defaultCategories = existingCategories.ifEmpty {
        listOf("Work", "Personal", "Shopping", "Health", "Learning", "Other")
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter custom category or select from suggestions") }
                )
                
                // Show existing categories as chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(defaultCategories) { defaultCategory ->
                        FilterChip(
                            selected = category == defaultCategory,
                            onClick = { category = defaultCategory },
                            label = { Text(defaultCategory) }
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deadline: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(selectedDate.time)}",
                        fontSize = 14.sp
                    )
                    Button(onClick = { showDatePicker = true }) {
                        Text("Set Date")
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = selectedHour.toString(),
                        onValueChange = { 
                            val hour = it.toIntOrNull() ?: 0
                            if (hour in 0..23) {
                                selectedHour = hour
                                selectedDate = Calendar.getInstance().apply {
                                    timeInMillis = selectedDate.timeInMillis
                                    set(Calendar.HOUR_OF_DAY, selectedHour)
                                    set(Calendar.MINUTE, selectedMinute)
                                }
                            }
                        },
                        label = { Text("Hour") },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text(":", fontSize = 16.sp)
                    OutlinedTextField(
                        value = selectedMinute.toString(),
                        onValueChange = { 
                            val minute = it.toIntOrNull() ?: 0
                            if (minute in 0..59) {
                                selectedMinute = minute
                                selectedDate = Calendar.getInstance().apply {
                                    timeInMillis = selectedDate.timeInMillis
                                    set(Calendar.HOUR_OF_DAY, selectedHour)
                                    set(Calendar.MINUTE, selectedMinute)
                                }
                            }
                        },
                        label = { Text("Min") },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                
                // Notification selection
                Text(
                    text = "Notification Reminder:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("No notification", "15 minutes", "2 hours", "6 hours")) { notificationOption ->
                        val notificationValue = when (notificationOption) {
                            "No notification" -> 0L
                            "15 minutes" -> NotificationHelper.NOTIFICATION_15_MIN
                            "2 hours" -> NotificationHelper.NOTIFICATION_2_HOURS
                            "6 hours" -> NotificationHelper.NOTIFICATION_6_HOURS
                            else -> 0L
                        }
                        
                        FilterChip(
                            selected = selectedNotificationTime == notificationValue,
                            onClick = { selectedNotificationTime = notificationValue },
                            label = { Text(notificationOption) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && category.isNotBlank()) {
                        // Validate that deadline is not in the past
                        val currentTime = System.currentTimeMillis()
                        if (selectedDate.timeInMillis <= currentTime) {
                            showValidationError = true
                            Toast.makeText(context, "Deadline cannot be in the past", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        
                        showValidationError = false
                        
                        val newTask = Task(
                            title = title,
                            description = description,
                            priority = category, // Store category as string
                            deadline = selectedDate.timeInMillis,
                            notificationTime = selectedNotificationTime
                        )
                        
                        onTaskAdded(newTask)
                    }
                },
                enabled = title.isNotBlank() && category.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.timeInMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { 
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            selectedDate = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onTaskUpdated: (Task) -> Unit,
    existingCategories: List<String>
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var category by remember { mutableStateOf(task.priority) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = task.deadline }) }
    var selectedHour by remember { mutableStateOf(selectedDate.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(selectedDate.get(Calendar.MINUTE)) }
    var selectedNotificationTime by remember { mutableStateOf(task.notificationTime) }
    var showValidationError by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Add some default categories if no existing ones
    val defaultCategories = existingCategories.ifEmpty {
        listOf("Work", "Personal", "Shopping", "Health", "Learning", "Other")
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter custom category or select from suggestions") }
                )
                
                // Show existing categories as chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(defaultCategories) { defaultCategory ->
                        FilterChip(
                            selected = category == defaultCategory,
                            onClick = { category = defaultCategory },
                            label = { Text(defaultCategory) }
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deadline: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(selectedDate.time)}",
                        fontSize = 14.sp
                    )
                    Button(onClick = { showDatePicker = true }) {
                        Text("Set Date")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time: ${String.format(Locale.getDefault(), "%02d:%02d", selectedDate.get(Calendar.HOUR_OF_DAY), selectedDate.get(Calendar.MINUTE))}",
                        fontSize = 14.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = selectedHour.toString(),
                            onValueChange = { 
                                val hour = it.toIntOrNull() ?: 0
                                if (hour in 0..23) {
                                    selectedHour = hour
                                    selectedDate = Calendar.getInstance().apply {
                                        timeInMillis = selectedDate.timeInMillis
                                        set(Calendar.HOUR_OF_DAY, selectedHour)
                                        set(Calendar.MINUTE, selectedMinute)
                                    }
                                }
                            },
                            label = { Text("Hour") },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Text(":", fontSize = 16.sp)
                        OutlinedTextField(
                            value = selectedMinute.toString(),
                            onValueChange = { 
                                val minute = it.toIntOrNull() ?: 0
                                if (minute in 0..59) {
                                    selectedMinute = minute
                                    selectedDate = Calendar.getInstance().apply {
                                        timeInMillis = selectedDate.timeInMillis
                                        set(Calendar.HOUR_OF_DAY, selectedHour)
                                        set(Calendar.MINUTE, selectedMinute)
                                    }
                                }
                            },
                            label = { Text("Min") },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                
                // Notification selection
                Text(
                    text = "Notification Reminder:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("No notification", "15 minutes", "2 hours", "6 hours")) { notificationOption ->
                        val notificationValue = when (notificationOption) {
                            "No notification" -> 0L
                            "15 minutes" -> NotificationHelper.NOTIFICATION_15_MIN
                            "2 hours" -> NotificationHelper.NOTIFICATION_2_HOURS
                            "6 hours" -> NotificationHelper.NOTIFICATION_6_HOURS
                            else -> 0L
                        }
                        
                        FilterChip(
                            selected = selectedNotificationTime == notificationValue,
                            onClick = { selectedNotificationTime = notificationValue },
                            label = { Text(notificationOption) }
                        )
                    }
                }
                
                if (showValidationError) {
                    Text(
                        text = "Deadline cannot be in the past",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && category.isNotBlank()) {
                        // Validate that deadline is not in the past
                        val currentTime = System.currentTimeMillis()
                        if (selectedDate.timeInMillis <= currentTime) {
                            showValidationError = true
                            Toast.makeText(context, "Deadline cannot be in the past", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        
                        showValidationError = false
                        
                        val updatedTask = task.copy(
                            title = title,
                            description = description,
                            priority = category, // Store category as string
                            deadline = selectedDate.timeInMillis,
                            notificationTime = selectedNotificationTime
                        )
                        
                        onTaskUpdated(updatedTask)
                    }
                },
                enabled = title.isNotBlank() && category.isNotBlank()
            ) {
                Text("Update Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.timeInMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { 
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            selectedDate = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
fun HistoryDialog(
    completedTasks: List<Task>,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Completed Tasks") },
        text = {
            if (completedTasks.isEmpty()) {
                Text("No completed tasks yet.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(completedTasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = task.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                if (task.description.isNotBlank()) {
                                    Text(
                                        text = task.description,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Category: ${task.priority}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Completed: ${dateFormat.format(Date(task.deadline))}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
