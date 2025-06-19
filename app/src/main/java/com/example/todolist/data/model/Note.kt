package com.example.todolist.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb


data class Note(
    val id: Long,
    val title: String = "",
    val content: String = "",
    val type: String = "TEXT",
    val timestamp: Long = System.currentTimeMillis(),
    val color: Int = NoteColor.DEFAULT.toArgb(),
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val category: String = "",
){
    val noteType: NoteType
        get() = NoteType.valueOf(type)
}


enum class NoteType {
    TEXT, LIST;

    companion object {
        fun fromString(value: String): NoteType {
            return when (value.lowercase()) {
                "list" -> LIST
                else -> TEXT
            }
        }
    }
}


enum class NoteColor(val color: Color) {
    DEFAULT(Color.White),
    RED(Color(0xFFE57373)),
    GREEN(Color(0xFF81C784)),
    BLUE(Color(0xFF64B5F6)),
    YELLOW(Color(0xFFFFD54F)),
    BLACK(Color.Black),
    PURPLE(Color(0xFFBA68C8)),
    ORANGE(Color(0xFFFF8A65)),
    GRAY(Color(0xFF90A4AE)),
    PURPLE_LIGHT(Color(0xFFD8D8F6));


    fun toArgb(): Int = color.toArgb()

    companion object {
        fun fromArgb(argb: Int): NoteColor {
            return entries.find { it.color.toArgb() == argb } ?: DEFAULT
        }
    }
}



