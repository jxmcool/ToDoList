package com.example.todolist.ui.addedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.data.model.NoteType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Composable
internal fun AddNoteScreen(
    onSave: (String, String, NoteType) -> Unit,
    onCancel: () -> Unit,
    initialTitle: String = "",
    initialContent: String = "",
    type: NoteType = NoteType.TEXT
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var noteType by remember { mutableStateOf(type) }

    val taskList = remember { mutableStateListOf<TaskItem>() }
    var wasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(noteType, initialContent) {
        if (!wasInitialized && noteType == NoteType.LIST) {
            taskList.clear()
            if (initialContent.isNotBlank()) {
                try {
                    taskList.addAll(Json.decodeFromString<List<TaskItem>>(initialContent))
                } catch (_: Exception) {
                    taskList.add(TaskItem(text = ""))
                }
            } else {
                taskList.add(TaskItem(text = ""))
            }
            wasInitialized = true
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Тип заметки:")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { noteType = NoteType.TEXT
                    wasInitialized = false},
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (noteType == NoteType.TEXT) MaterialTheme.colorScheme.primary else Color.Gray
                )
            ) { Text("Текст") }

            Button(
                onClick = { noteType = NoteType.LIST
                    wasInitialized = false},
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (noteType == NoteType.LIST) MaterialTheme.colorScheme.primary else Color.Gray
                )
            ) { Text("Список") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (noteType == NoteType.TEXT) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Содержимое") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        } else {
            Column {
                taskList.forEachIndexed { index, task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = task.isChecked,
                            onCheckedChange = { checked ->
                                taskList[index] = task.copy(isChecked = checked)
                            }
                        )
                        OutlinedTextField(
                            value = task.text,
                            onValueChange = { text ->
                                taskList[index] = task.copy(text = text)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { taskList.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }

                Button(
                    onClick = { taskList.add(TaskItem(text = "")) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Добавить задачу")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClick@{
                val filteredTasks = taskList.filter { it.text.isNotBlank() }
                val contentToSave = if (noteType == NoteType.LIST) {
                    Json.encodeToString(filteredTasks)
                } else content

                onSave(title, contentToSave, noteType)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Сохранить")
        }

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Отмена")
        }
    }
}















