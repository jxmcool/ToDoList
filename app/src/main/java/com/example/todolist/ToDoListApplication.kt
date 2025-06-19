// ToDoListApplication.kt
package com.example.todolist

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ToDoListApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        // Инициализация аналитики, БД и др.
    }
}

