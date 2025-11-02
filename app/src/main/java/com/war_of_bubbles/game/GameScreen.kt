package com.war_of_bubbles.game

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.war_of_bubbles.entities.Ball
import com.war_of_bubbles.entities.BallType
import com.war_of_bubbles.entities.Boss
import com.war_of_bubbles.game.GameState.Action
import com.war_of_bubbles.systems.CombatSystem
import kotlinx.coroutines.delay

/**
 * Главный игровой экран
 */
@Composable
fun GameScreen() {
    val gameState = remember { GameState() }
    var canvasSize by remember { mutableStateOf(Size(0f, 0f)) }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(canvasSize) {
        if (canvasSize.width > 0f && canvasSize.height > 0f && !isInitialized) {
            gameState.reset(canvasSize.width, canvasSize.height)
            isInitialized = true
        }
    }
    
    // Автоматическое выполнение хода врагов и босса
    LaunchedEffect(gameState.currentTurn, gameState.isGameOver) {
        if (gameState.isGameOver) return@LaunchedEffect
        
        when (gameState.currentTurn) {
            GameState.Turn.ENEMY -> {
                delay(500)
                gameState.enemyBalls.filter { it.isAlive }.forEach { enemy ->
                    AIController.executeEnemyTurn(enemy, gameState.playerBalls, gameState)
                    delay(800)
                }
                delay(500)
                gameState.nextTurn()
            }
            GameState.Turn.BOSS -> {
                delay(500)
                if (gameState.boss.isAlive && gameState.enemyBalls.all { !it.isAlive }) {
                    AIController.executeBossTurn(gameState.boss, gameState.playerBalls, gameState)
                }
                delay(800)
                gameState.nextTurn()
            }
            GameState.Turn.PLAYER -> {
                // Ход игрока - ожидаем клика
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
            .padding(16.dp)
    ) {
        // Заголовок и информация
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎮 Боевые шарики",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ход: ${gameState.turnNumber}",
                color = Color(0xFF888888),
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Информация о текущем ходе
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (gameState.currentTurn) {
                    GameState.Turn.PLAYER -> Color(0xFF2d8659)
                    GameState.Turn.ENEMY -> Color(0xFF8b2d2d)
                    GameState.Turn.BOSS -> Color(0xFF4a2d8b)
                }
            )
        ) {
            Text(
                text = when (gameState.currentTurn) {
                    GameState.Turn.PLAYER -> "⚡ Ваш ход! Выберите шарик и действие"
                    GameState.Turn.ENEMY -> "⏳ Ход врагов..."
                    GameState.Turn.BOSS -> "👹 Ход босса..."
                },
                modifier = Modifier.padding(12.dp),
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Игровое поле
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GameCanvas(
                    gameState = gameState,
                    onCanvasSizeChange = { size ->
                        canvasSize = size
                    },
                    onBallClick = { ball ->
                        if (gameState.currentTurn == GameState.Turn.PLAYER && !gameState.isGameOver) {
                            if (ball in gameState.playerBalls) {
                                gameState.selectedBall = ball
                                gameState.selectedAction = null
                                gameState.targetBall = null
                            }
                        }
                    },
                    onEnemyClick = { enemy ->
                        if (gameState.currentTurn == GameState.Turn.PLAYER && 
                            gameState.selectedBall != null && 
                            !gameState.isGameOver) {
                            gameState.targetBall = enemy
                        }
                    },
                    onBossClick = { boss ->
                        if (gameState.currentTurn == GameState.Turn.PLAYER && 
                            gameState.selectedBall != null && 
                            !gameState.isGameOver && 
                            gameState.enemyBalls.all { !it.isAlive } &&
                            gameState.boss.isAlive) {
                            // Босс выбран как цель (для визуальной обратной связи)
                            gameState.targetBall = null
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Панель действий
        if (gameState.selectedBall != null && gameState.currentTurn == GameState.Turn.PLAYER && !gameState.isGameOver) {
            ActionPanel(gameState = gameState)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Лог боя
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0f1419))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "📜 Журнал боя:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                gameState.combatLog.forEach { log ->
                    Text(
                        text = log,
                        color = Color(0xFFaaaaaa),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Экран победы/поражения
        if (gameState.isGameOver) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (gameState.isVictory) Color(0xFF2d8659) else Color(0xFF8b2d2d)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (gameState.isVictory) "🎉 Победа!" else "💀 Поражение",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            isInitialized = false
                            gameState.reset(canvasSize.width, canvasSize.height)
                            isInitialized = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("🔄 Начать заново")
                    }
                }
            }
        }
    }
}

/**
 * Canvas для отрисовки игрового поля
 */
@Composable
fun GameCanvas(
    gameState: GameState,
    onCanvasSizeChange: (Size) -> Unit,
    onBallClick: (Ball) -> Unit,
    onEnemyClick: (Ball) -> Unit,
    onBossClick: (Boss) -> Unit
) {
    val density = LocalDensity.current
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Проверяем клик по шарикам игрока
                    gameState.playerBalls.forEach { ball ->
                        val distance = kotlin.math.sqrt(
                            (offset.x - ball.position.x).pow(2) + 
                            (offset.y - ball.position.y).pow(2)
                        )
                        if (distance < 50f) {
                            onBallClick(ball)
                        }
                    }
                    
                    // Проверяем клик по врагам
                    gameState.enemyBalls.forEach { enemy ->
                        val distance = kotlin.math.sqrt(
                            (offset.x - enemy.position.x).pow(2) + 
                            (offset.y - enemy.position.y).pow(2)
                        )
                        if (distance < 50f) {
                            onEnemyClick(enemy)
                        }
                    }
                    
                    // Проверяем клик по боссу
                    val bossDistance = kotlin.math.sqrt(
                        (offset.x - gameState.boss.position.x).pow(2) + 
                        (offset.y - gameState.boss.position.y).pow(2)
                    )
                    if (bossDistance < 60f) {
                        onBossClick(gameState.boss)
                    }
                }
            }
    ) {
        // Сохраняем размер Canvas
        onCanvasSizeChange(size)
        
        // Фон
        drawRect(
            color = Color(0xFF0f3460),
            size = size
        )
        
        // Разметка поля
        drawLine(
            color = Color(0xFF1e3a5f),
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 3f
        )

        // Отрисовка врагов
        gameState.enemyBalls.forEach { enemy ->
            drawBall(
                ball = enemy,
                isSelected = gameState.targetBall == enemy,
                isEnemy = true
            )
        }

        // Отрисовка босса
        if (gameState.boss.isAlive && gameState.enemyBalls.all { !it.isAlive }) {
            drawBoss(
                boss = gameState.boss,
                isTarget = gameState.selectedBall != null && 
                          gameState.currentTurn == GameState.Turn.PLAYER &&
                          gameState.targetBall == null
            )
        }

        // Отрисовка шариков игрока
        gameState.playerBalls.forEach { ball ->
            drawBall(
                ball = ball,
                isSelected = gameState.selectedBall == ball,
                isEnemy = false
            )
        }
    }
}

/**
 * Отрисовка шарика
 */
fun DrawScope.drawBall(ball: Ball, isSelected: Boolean, isEnemy: Boolean) {
    if (!ball.isAlive) return
    
    val radius = 45f
    val center = ball.position
    
    // Подсветка выбранного
    if (isSelected) {
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = radius + 10f,
            center = center
        )
    }
    
    // Тень
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = radius,
        center = Offset(center.x + 2f, center.y + 2f)
    )
    
    // Основной круг
    drawCircle(
        color = ball.type.color.copy(alpha = if (ball.isShielded) 0.9f else 0.8f),
        radius = radius,
        center = center
    )
    
    // Обводка щита
    if (ball.isShielded) {
        drawCircle(
            color = Color(0xFF00FFFF),
            radius = radius + 5f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
    }
    
    // Эмодзи (рисуем как текст)
    val emojiSize = 40f
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = emojiSize
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    
    drawContext.canvas.nativeCanvas.drawText(
        ball.type.emoji,
        center.x,
        center.y + emojiSize / 3,
        textPaint
    )
    
    // Полоска HP
    val hpPercent = ball.hp.toFloat() / ball.maxHp.toFloat()
    val hpBarWidth = radius * 2f
    val hpBarHeight = 8f
    val hpBarY = center.y + radius + 12f
    
    // Фон полоски
    drawRect(
        color = Color(0xFF333333),
        topLeft = Offset(center.x - hpBarWidth / 2, hpBarY),
        size = Size(hpBarWidth, hpBarHeight)
    )
    
    // HP полоска
    drawRect(
        color = when {
            hpPercent > 0.6f -> Color(0xFF00FF00)
            hpPercent > 0.3f -> Color(0xFFFFAA00)
            else -> Color(0xFFFF0000)
        },
        topLeft = Offset(center.x - hpBarWidth / 2, hpBarY),
        size = Size(hpBarWidth * hpPercent, hpBarHeight)
    )
    
    // Текст HP
    val hpTextPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 18f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    
    drawContext.canvas.nativeCanvas.drawText(
        "${ball.hp}/${ball.maxHp}",
        center.x,
        hpBarY + 22f,
        hpTextPaint
    )
    
    // Иконка перезарядки
    if (ball.specialCooldown > 0) {
        val cooldownPaint = Paint().apply {
            color = android.graphics.Color.RED
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            "⏳${ball.specialCooldown}",
            center.x,
            center.y - radius - 15f,
            cooldownPaint
        )
    }
}

/**
 * Отрисовка босса
 */
fun DrawScope.drawBoss(boss: Boss, isTarget: Boolean = false) {
    if (!boss.isAlive) return
    
    val radius = 60f
    val center = boss.position
    
    // Подсветка при выборе цели
    if (isTarget) {
        drawCircle(
            color = Color.Yellow.copy(alpha = 0.4f),
            radius = radius + 15f,
            center = center
        )
    }
    
    // Пульсация для босса
    val pulseAlpha = 0.5f + 0.3f * kotlin.math.sin(System.currentTimeMillis() / 500.0).toFloat()
    
    // Тень
    drawCircle(
        color = Color.Black.copy(alpha = 0.5f),
        radius = radius,
        center = Offset(center.x + 3f, center.y + 3f)
    )
    
    // Основной круг босса
    drawCircle(
        color = Color(0xFF1a1a1a).copy(alpha = pulseAlpha),
        radius = radius,
        center = center
    )
    
    // Обводка для второй фазы
    if (boss.phase == 2) {
        drawCircle(
            color = Color(0xFFFF0000),
            radius = radius + 8f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )
    }
    
    // Эмодзи босса
    val emojiSize = 55f
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = emojiSize
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    
    drawContext.canvas.nativeCanvas.drawText(
        boss.emoji,
        center.x,
        center.y + emojiSize / 3,
        textPaint
    )
    
    // Полоска HP босса
    val hpPercent = boss.hp.toFloat() / boss.maxHp.toFloat()
    val hpBarWidth = radius * 2.5f
    val hpBarHeight = 10f
    val hpBarY = center.y + radius + 15f
    
    drawRect(
        color = Color(0xFF333333),
        topLeft = Offset(center.x - hpBarWidth / 2, hpBarY),
        size = Size(hpBarWidth, hpBarHeight)
    )
    
    drawRect(
        color = when {
            hpPercent > 0.6f -> Color(0xFF8B0000)
            hpPercent > 0.3f -> Color(0xFF6B0000)
            else -> Color(0xFF4B0000)
        },
        topLeft = Offset(center.x - hpBarWidth / 2, hpBarY),
        size = Size(hpBarWidth * hpPercent, hpBarHeight)
    )
    
    val hpTextPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 20f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    
    drawContext.canvas.nativeCanvas.drawText(
        "${boss.name}: ${boss.hp}/${boss.maxHp}",
        center.x,
        hpBarY + 25f,
        hpTextPaint
    )
    
    if (boss.specialCooldown > 0) {
        val cooldownPaint = Paint().apply {
            color = android.graphics.Color.RED
            textSize = 22f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            "💀${boss.specialCooldown}",
            center.x,
            center.y - radius - 20f,
            cooldownPaint
        )
    }
}

/**
 * Панель действий
 */
@Composable
fun ActionPanel(gameState: GameState) {
    val selectedBall = gameState.selectedBall ?: return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Выбрано: ${selectedBall.type.emoji} ${selectedBall.type.name}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "HP: ${selectedBall.hp}/${selectedBall.maxHp} | ATK: ${selectedBall.atk} | DEF: ${selectedBall.def}",
                color = Color(0xFFaaaaaa),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка атаки
                Button(
                    onClick = {
                        gameState.selectedAction = Action.ATTACK
                        if (gameState.targetBall != null) {
                            executeAttack(gameState)
                        } else if (gameState.enemyBalls.all { !it.isAlive } && gameState.boss.isAlive) {
                            executeBossAttack(gameState)
                        }
                    },
                    enabled = (gameState.targetBall != null && gameState.targetBall!!.isAlive) || 
                             (gameState.enemyBalls.all { !it.isAlive } && gameState.boss.isAlive),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⚔️ Атака")
                }
                
                // Кнопка специальной способности
                Button(
                    onClick = {
                        gameState.selectedAction = Action.SPECIAL
                        executeSpecial(gameState)
                    },
                    enabled = selectedBall.specialCooldown == 0 && selectedBall.isAlive,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9b59b6)
                    )
                ) {
                    Text(
                        text = selectedBall.type.getSpecialAbilityName(),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
            
            if (gameState.enemyBalls.any { it.isAlive }) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Выберите врага на поле для атаки",
                    color = Color(0xFF888888),
                    fontSize = 12.sp
                )
            } else if (gameState.boss.isAlive) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Атакуйте босса!",
                    color = Color(0xFFFF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Выполнение атаки
 */
fun executeAttack(gameState: GameState) {
    val attacker = gameState.selectedBall ?: return
    val target = gameState.targetBall
    
    if (target != null && attacker.isAlive && target.isAlive) {
        val damage = CombatSystem.attack(attacker, target)
        gameState.addLog("${attacker.type.emoji} ${attacker.type.name} атакует ${target.type.emoji} ${target.type.name} на $damage урона!")
        
        if (!target.isAlive) {
            gameState.addLog("  💀 ${target.type.emoji} ${target.type.name} повержен!")
        }
        
        gameState.nextTurn()
    }
}

/**
 * Выполнение атаки по боссу
 */
fun executeBossAttack(gameState: GameState) {
    val attacker = gameState.selectedBall ?: return
    
    if (attacker.isAlive && gameState.boss.isAlive) {
        val damage = CombatSystem.attackBoss(attacker, gameState.boss)
        gameState.addLog("${attacker.type.emoji} ${attacker.type.name} атакует ${gameState.boss.emoji} ${gameState.boss.name} на $damage урона!")
        
        if (!gameState.boss.isAlive) {
            gameState.addLog("  💀 ${gameState.boss.name} повержен!")
        }
        
        gameState.nextTurn()
    }
}

/**
 * Выполнение специальной способности
 */
fun executeSpecial(gameState: GameState) {
    val attacker = gameState.selectedBall ?: return
    
    if (!attacker.isAlive) return
    
    when (attacker.type) {
        BallType.RED -> {
            val aliveEnemies = gameState.enemyBalls.filter { it.isAlive }
            if (aliveEnemies.isNotEmpty()) {
                val results = CombatSystem.redSpecial(attacker, aliveEnemies)
                gameState.addLog("${attacker.type.emoji} ${attacker.type.name} использует ${attacker.type.getSpecialAbilityName()}!")
                results.forEach { (ball, damage) ->
                    gameState.addLog("  → ${ball.type.emoji} получает $damage урона")
                    if (!ball.isAlive) {
                        gameState.addLog("    💀 ${ball.type.emoji} повержен!")
                    }
                }
            } else if (gameState.boss.isAlive) {
                val damage = CombatSystem.redSpecialBoss(attacker, gameState.boss)
                gameState.addLog("${attacker.type.emoji} ${attacker.type.name} использует ${attacker.type.getSpecialAbilityName()} по боссу!")
                gameState.addLog("  → ${gameState.boss.emoji} получает $damage урона!")
            }
        }
        BallType.BLUE -> {
            val shielded = CombatSystem.blueSpecial(attacker, gameState.playerBalls)
            gameState.addLog("${attacker.type.emoji} ${attacker.type.name} использует ${attacker.type.getSpecialAbilityName()}!")
            gameState.addLog("  → Защита активирована для ${shielded.size} союзников!")
        }
        BallType.YELLOW -> {
            val target = gameState.targetBall
            if (target != null && target.isAlive) {
                val damage = CombatSystem.yellowSpecial(attacker, target)
                gameState.addLog("${attacker.type.emoji} ${attacker.type.name} использует ${attacker.type.getSpecialAbilityName()}!")
                gameState.addLog("  → ${target.type.emoji} получает $damage урона!")
                if (!target.isAlive) {
                    gameState.addLog("    💀 ${target.type.emoji} повержен!")
                }
            } else if (gameState.boss.isAlive && gameState.enemyBalls.all { !it.isAlive }) {
                val damage = CombatSystem.yellowSpecialBoss(attacker, gameState.boss)
                gameState.addLog("${attacker.type.emoji} ${attacker.type.name} использует ${attacker.type.getSpecialAbilityName()} по боссу!")
                gameState.addLog("  → ${gameState.boss.emoji} получает $damage урона!")
            } else {
                gameState.addLog("⚠️ Нужна цель для атаки!")
                return
            }
        }
    }
    
    gameState.nextTurn()
}

// Расширение для возведения в степень
fun Float.pow(n: Int): Float {
    var result = 1f
    repeat(n) { result *= this }
    return result
}

