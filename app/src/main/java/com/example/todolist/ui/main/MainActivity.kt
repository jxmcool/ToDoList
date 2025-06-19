package com.example.todolist.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.todolist.storage.SharedPrefsManager
import com.example.todolist.ui.navigation.AppNavGraph
import com.example.todolist.ui.theme.ToDoListTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPrefsManager.init(applicationContext)

        // Включаем поддержку edge-to-edge вручную
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()

            ToDoListTheme {
                AppNavGraph(
                    navController = navController,
                    sharedPrefsManager = SharedPrefsManager,

                )
            }
        }
    }
}







