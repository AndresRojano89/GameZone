package com.example.gamezone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gamezone.ui.GameZoneApp
import com.example.gamezone.ui.theme.GameZoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameZoneTheme {
                GameZoneApp()
            }
        }
    }
}


