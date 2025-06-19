package com.example.todolist.ui.main
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.todolist.data.model.Note
import com.example.todolist.data.model.NoteType
import com.example.todolist.ui.addedit.TaskItem
import kotlinx.serialization.json.Json

@Composable
fun NoteListScreen(
    notes: List<Note>,
    onDeleteNote: (Note) -> Unit,
    navController: NavHostController,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Button(
            onClick = { navController.navigate("note_type") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text("Добавить заметку")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (notes.isEmpty()) {
                item {
                    Text("Нет заметок", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onDeleteNote = { onDeleteNote(note) },
                        onClick = { navController.navigate("edit_note/${note.id}") }
                    )
                }
            }
        }
    }
}


@SuppressLint("MissingColorAlphaChannel")
@Composable
fun NoteCard(note: Note, onDeleteNote: () -> Unit, onClick: () -> Unit) {
    val safeBackgroundColor = if (
        note.color == Color.Unspecified.toArgb() ||
        note.color == Color.Transparent.toArgb()
    ) {
        Color.White
    } else {
        Color(note.color)
    }

    val isDarkBackground = safeBackgroundColor.luminance() < 0.3
    val textColor = if (isDarkBackground) Color.White else Color.Black
    val taskList = remember(note.id) {
        try {
            Json.decodeFromString<List<TaskItem>>(note.content)
        } catch (e: Exception) {
            Log.e("NoteCard", "Ошибка декодирования JSON: ${e.message}")
            emptyList()
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = safeBackgroundColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = safeBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor
                )
                IconButton(onClick = onDeleteNote) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = if (isDarkBackground) Color.White else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (note.noteType == NoteType.LIST) {

                val visibleTasks = taskList.sortedBy { it.isChecked }.take(3)

                Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                    visibleTasks.forEach { task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = task.isChecked,
                                onCheckedChange = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = task.text,
                                fontSize = 14.sp,
                                textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                                color = if (task.isChecked) Color.Gray else textColor,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }


            } else {
                val previewText = note.content
                    .replace("\n", " ")   // Удаляем переносы строк
                    .trim()
                Text(
                    text = previewText,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
            }

            if (note.category.isNotBlank()) {
                Text(
                    text = "Категория: ${note.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkBackground) Color.White else Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun NoteTypeSelectionScreen(
    onTypeSelected: (NoteType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Выберите тип заметки", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onTypeSelected(NoteType.TEXT) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 Текстовая")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onTypeSelected(NoteType.LIST) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("✅ Список задач")
        }
    }
}



