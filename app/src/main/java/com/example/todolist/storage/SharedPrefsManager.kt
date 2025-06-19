package com.example.todolist.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.todolist.data.model.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefsManager {
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private const val NOTES_KEY = "notes"

    // Инициализация (вызвать в MainActivity.onCreate)
    fun init(context: Context) {
        prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
    }

    fun getNotes(): List<Note> {
        val json = prefs.getString(NOTES_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveNotes(notes: List<Note>) {
        val json = gson.toJson(notes)
        prefs.edit { putString(NOTES_KEY, json) }
    }

    fun saveNote(note: Note) {
        val notes = getNotes().toMutableList()
        val index = notes.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            notes[index] = note
        } else {
            notes.add(note)
        }
        saveNotes(notes)
    }

    fun loadNote(id: Long): Note? {
        return getNotes().find { it.id == id }
    }

    private fun generateNextId(notes: List<Note>): Long {
        return (notes.maxOfOrNull { it.id } ?: 0) + 1
    }
}

