package com.example.todolist.ui.addedit

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.data.model.Note
import com.example.todolist.data.model.NoteColor
import com.example.todolist.data.model.NoteType
import com.example.todolist.storage.SharedPrefsManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID


@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun NoteEditScreen(
    note: Note?,
    onUpdate: (Note) -> Unit,
    onDelete: () -> Unit
) {
    val currentNote = requireNotNull(note) { "Note cannot be null in NoteEditScreen" }

    var title by rememberSaveable { mutableStateOf(currentNote.title) }
    var content by rememberSaveable { mutableStateOf(currentNote.content) }
    var category by rememberSaveable { mutableStateOf(currentNote.category) }
    var selectedColor by rememberSaveable { mutableStateOf(NoteColor.fromArgb(currentNote.color)) }
    var noteType by rememberSaveable {
        mutableStateOf(
            try {
                NoteType.valueOf(currentNote.type.uppercase())
            } catch (_: Exception) {
                NoteType.TEXT
            }
        )
    }

    val taskList = remember(note.id) {
        mutableStateListOf<TaskItem>().apply {
            if (note.type == NoteType.LIST.name) {
                try {
                    if (note.content.isNotBlank()) {
                        addAll(Json.decodeFromString(note.content))
                    }
                } catch (e: Exception) {
                    Log.e("NOTE_EDIT", "Ошибка декодирования задач: ${e.message}")
                }
            }
            if (isEmpty()) {
                add(TaskItem(id = System.currentTimeMillis().toString(), text = ""))
            }
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(max = 160.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp) // для кнопок
        ) {
            // Цвет
            Text("Выберите цвет:", style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NoteColor.entries.forEach { color ->
                    Box(
                        Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .background(color.color)
                            .border(
                                width = if (selectedColor == color) 3.dp else 1.dp,
                                color = if (selectedColor == color) Color.Blue else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            // Тип заметки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NoteType.entries.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            noteType = type
                            content = if (type == NoteType.LIST) {
                                ""
                            } else {
                                taskList.joinToString(separator = "\n") { "- ${it.text}" }
                            }
                        }
                    ) {
                        RadioButton(
                            selected = noteType == type,
                            onClick = null
                        )
                        Text(type.name)
                    }
                }
            }

            // Заголовок
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Контент
            if (noteType == NoteType.TEXT) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Содержание") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Категория
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Категория") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Список задач
            if (noteType == NoteType.LIST) {
                val activeTasks = taskList.filter { !it.isChecked }
                val completedTasks = taskList.filter { it.isChecked }

                Text("Активные задачи", style = MaterialTheme.typography.titleMedium)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                ) {
                    items(activeTasks, key = { it.id }) { task: TaskItem ->
                        TaskRow(
                            task = task,
                            onUpdate = { updated ->
                                val index = taskList.indexOfFirst { it.id == task.id }
                                if (index != -1) taskList[index] = updated
                            },
                            onDelete = {
                                taskList.removeIf { it.id == task.id }
                            }
                        )
                    }

                    if (completedTasks.isNotEmpty()) {
                        item {
                            Text("Выполненные задачи", style = MaterialTheme.typography.titleMedium)
                        }
                        items(completedTasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onUpdate = { updated ->
                                    val index = taskList.indexOfFirst { it.id == task.id }
                                    if (index != -1) taskList[index] = updated
                                },
                                onDelete = {
                                    taskList.removeIf { it.id == task.id }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Кнопки внизу
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (noteType == NoteType.LIST) {
                Button(
                    onClick = {
                        val newTask = TaskItem(
                            id = UUID.randomUUID().toString(),
                            text = "",
                            isChecked = false
                        )
                        taskList.add(newTask)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Добавить задачу")
                }
            }

            Button(
                onClick = onDelete,
                modifier = Modifier.weight(1f)
            ) {
                Text("Удалить")
            }
            Button(
                onClick = onClick@{
                    val filteredTasks = taskList.filter { it.text.isNotBlank() }
                    val finalContent = if (noteType == NoteType.LIST) {
                        try {
                            Json.encodeToString(filteredTasks)
                        } catch (e: Exception) {
                            Log.e("SAVE", "Ошибка сериализации: ${e.message}")
                            return@onClick
                        }
                    } else content

                    val updatedNote = note.copy(
                        title = title,
                        content = finalContent,
                        color = selectedColor.toArgb(),
                        type = noteType.name,
                        category = category
                    )

                    SharedPrefsManager.saveNote(updatedNote)
                    onUpdate(updatedNote)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Сохранить")
            }
        }
    }
}

@Serializable
data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false
)

@Composable
fun TaskRow(
    task: TaskItem,
    onUpdate: (TaskItem) -> Unit,
    onDelete: () -> Unit
) {
    var text by remember { mutableStateOf(task.text) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isChecked,
            onCheckedChange = { isChecked ->
                onUpdate(task.copy(isChecked = isChecked))
            }
        )
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onUpdate(task.copy(text = it))
            },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить")
        }
    }
}

