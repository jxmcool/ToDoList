package com.example.todolist.ui.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todolist.data.model.Note
import com.example.todolist.data.model.NoteType
import com.example.todolist.storage.SharedPrefsManager
import com.example.todolist.ui.addedit.AddNoteScreen
import com.example.todolist.ui.addedit.NoteEditScreen
import com.example.todolist.ui.main.NoteListScreen
import com.example.todolist.ui.main.NoteTypeSelectionScreen
import kotlin.random.Random


@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("MissingColorAlphaChannel")
@Composable
fun AppNavGraph(
    navController: NavHostController,
    sharedPrefsManager: SharedPrefsManager,
) {
    val notes = remember { mutableStateOf(sharedPrefsManager.getNotes()) }

    fun updateNotes(newList: List<Note>) {
        sharedPrefsManager.saveNotes(newList)
        notes.value = newList
    }

    NavHost(navController = navController, startDestination = "note_list") {
        composable("note_list") {
            NoteListScreen(
                notes = notes.value.sortedByDescending { it.timestamp },
                onDeleteNote = { noteToDelete ->
                    val updatedNotes = notes.value.toMutableList().apply { remove(noteToDelete) }
                    updateNotes(updatedNotes)
                },
                navController = navController
            )
        }
        composable("note_type") {
            NoteTypeSelectionScreen(
                onTypeSelected = { selectedType ->
                    navController.navigate("add_note?type=${selectedType.name.lowercase()}")
                }
            )
        }
        composable(
            route = "add_note?type={type}",
            arguments = listOf(navArgument("type") {
                type = NavType.StringType
                defaultValue = "text"
                nullable = true
            })
        ) { backStackEntry ->
            val typeString = backStackEntry.arguments?.getString("type") ?: "text"
            val noteType = NoteType.fromString(typeString)

            AddNoteScreen(
                onSave = { title, content, noteType ->
                    val newNote = Note(
                        id = Random.nextLong(),
                        title = title,
                        content = content,
                        timestamp = System.currentTimeMillis(),
                        color = if (noteType == NoteType.LIST) {
                            Color(0xFFD8D8F6).toArgb()
                        } else {
                            Color.White.toArgb()
                        },
                        type = noteType.name
                    )
                    val updatedNotes = notes.value.toMutableList().apply { add(newNote) }
                    updateNotes(updatedNotes)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
                type = noteType
            )
        }



        composable("edit_note/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull()

            if (noteId == null) {
                Text("Ошибка: Неверный ID заметки")
            } else {
                val note: Note? = notes.value.find { it.id == noteId }

                if (note == null) {
                    Text("Заметка не найдена")
                } else {
                    NoteEditScreen(
                        note = note,
                        onUpdate = { updatedNote: Note ->
                            val updatedList = notes.value.map {
                                if (it.id == updatedNote.id) updatedNote else it
                            }
                            updateNotes(updatedList)
                            navController.popBackStack()
                        },
                        onDelete = {
                            val updatedList = notes.value.toMutableList().apply { remove(note) }
                            updateNotes(updatedList)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
