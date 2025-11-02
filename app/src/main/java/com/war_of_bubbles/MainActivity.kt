package com.war_of_bubbles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.war_of_bubbles.game.GameScreen
import com.war_of_bubbles.ui.SettingsScreen
import com.war_of_bubbles.ui.theme.LocalThemeState
import com.war_of_bubbles.ui.theme.ThemeState
import com.war_of_bubbles.ui.theme.War_of_bubblesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Сохраняем предпочтение темы
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val defaultDarkTheme = prefs.getBoolean("dark_theme", false)
        
        setContent {
            var isDarkTheme by remember { mutableStateOf(defaultDarkTheme) }
            var showSettings by remember { mutableStateOf(false) }
            
            val themeState = remember {
                ThemeState(
                    isDarkTheme = isDarkTheme,
                    toggleTheme = {
                        isDarkTheme = !isDarkTheme
                        prefs.edit().putBoolean("dark_theme", isDarkTheme).apply()
                    }
                )
            }
            
            androidx.compose.runtime.CompositionLocalProvider(LocalThemeState provides themeState) {
                War_of_bubblesTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        if (showSettings) {
                            SettingsScreen(
                                onBackClick = { showSettings = false },
                                onThemeChange = { newTheme ->
                                    isDarkTheme = newTheme
                                    prefs.edit().putBoolean("dark_theme", isDarkTheme).apply()
                                },
                                isDarkTheme = isDarkTheme
                            )
                        } else {
                            GameScreen(onSettingsClick = { showSettings = true })
                        }
                    }
                }
            }
        }
    }
}