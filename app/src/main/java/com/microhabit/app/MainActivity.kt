package com.microhabit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.microhabit.app.ui.MainScreen
import com.microhabit.app.ui.theme.MicroHabitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MicroHabitTheme {
                MainScreen()
            }
        }
    }
}
