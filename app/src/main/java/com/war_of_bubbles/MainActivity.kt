package com.war_of_bubbles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.war_of_bubbles.game.GameScreen
import com.war_of_bubbles.ui.theme.War_of_bubblesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            War_of_bubblesTheme {
                GameScreen()
            }
        }
    }
}