package com.war_of_bubbles.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "🎮 Как играть",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TutorialSection(
                    title = "1️⃣ Выбор шарика",
                    content = "Нажмите на один из ваших шариков внизу поля. Вы увидите его характеристики и панель действий."
                )
                
                TutorialSection(
                    title = "2️⃣ Выбор цели",
                    content = "Кликните на вражеский шарик вверху поля, чтобы выбрать его как цель для атаки."
                )
                
                TutorialSection(
                    title = "3️⃣ Атака",
                    content = "Нажмите кнопку '⚔️ Атака' для обычной атаки или используйте специальную способность."
                )
                
                TutorialSection(
                    title = "🔴 Красный шарик",
                    content = "Агрессор (HP: 100, ATK: 30, DEF: 10). Сильнее против Жёлтого. Спецспособность: '🔥 Взрыв ярости' - атакует всех врагов."
                )
                
                TutorialSection(
                    title = "🔵 Синий шарик",
                    content = "Защитник (HP: 120, ATK: 20, DEF: 25). Блокирует атаки Красного. Спецспособность: '🛡️ Щит эмпатии' - защищает союзников."
                )
                
                TutorialSection(
                    title = "🟡 Жёлтый шарик",
                    content = "Скороход (HP: 80, ATK: 25, DEF: 15). Быстрее Синего. Спецспособность: '⚡ Молниеносный рывок' - двойная атака."
                )
                
                TutorialSection(
                    title = "⚫ Босс",
                    content = "Тёмный шар появляется после победы над всеми врагами. Имеет 300 HP и две фазы боя!"
                )
                
                TutorialSection(
                    title = "💡 Советы",
                    content = "• Используйте систему 'камень-ножницы-бумага': Красный > Жёлтый > Синий > Красный\n• Специальные способности имеют перезарядку\n• Планируйте ходы заранее\n• Защищайте слабых шариков"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Понятно! 👍", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun TutorialSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

