package com.war_of_bubbles.game

import androidx.compose.ui.geometry.Offset
import com.war_of_bubbles.entities.Ball
import com.war_of_bubbles.entities.BallType
import com.war_of_bubbles.entities.Boss

/**
 * Состояние игры
 */
data class GameState(
    val playerBalls: MutableList<Ball> = mutableListOf(),
    val enemyBalls: MutableList<Ball> = mutableListOf(),
    val boss: Boss = Boss(),
    var currentTurn: Turn = Turn.PLAYER,
    var selectedBall: Ball? = null,
    var selectedAction: Action? = null,
    var targetBall: Ball? = null,
    var isBossSelected: Boolean = false,
    var isGameOver: Boolean = false,
    var isVictory: Boolean = false,
    var combatLog: MutableList<String> = mutableListOf(),
    var turnNumber: Int = 1
) {
    enum class Turn {
        PLAYER, ENEMY, BOSS
    }

    enum class Action {
        ATTACK, SPECIAL, WAIT
    }

    fun reset(canvasWidth: Float = 800f, canvasHeight: Float = 400f) {
        // Создаём стартовую команду игрока (3 типа шариков)
        playerBalls.clear()
        val playerY = canvasHeight * 0.75f
        playerBalls.add(Ball(1, BallType.RED, position = Offset(canvasWidth * 0.2f, playerY)))
        playerBalls.add(Ball(2, BallType.BLUE, position = Offset(canvasWidth * 0.5f, playerY)))
        playerBalls.add(Ball(3, BallType.YELLOW, position = Offset(canvasWidth * 0.8f, playerY)))

        // Создаём вражеских шариков для первого уровня
        enemyBalls.clear()
        val enemyY = canvasHeight * 0.4f
        enemyBalls.add(Ball(10, BallType.RED, position = Offset(canvasWidth * 0.2f, enemyY)))
        enemyBalls.add(Ball(11, BallType.BLUE, position = Offset(canvasWidth * 0.5f, enemyY)))
        enemyBalls.add(Ball(12, BallType.YELLOW, position = Offset(canvasWidth * 0.8f, enemyY)))

        boss.reset()
        boss.position = Offset(canvasWidth * 0.5f, canvasHeight * 0.2f)

        currentTurn = Turn.PLAYER
        selectedBall = null
        selectedAction = null
        targetBall = null
        isBossSelected = false
        isGameOver = false
        isVictory = false
        combatLog.clear()
        turnNumber = 1
        
        // Начальное сообщение
        addLog("🎮 Битва началась! Выберите свой шарик для атаки!")
    }

    fun checkGameOver() {
        val allPlayerDead = playerBalls.all { !it.isAlive }
        val allEnemiesDead = enemyBalls.all { !it.isAlive } && !boss.isAlive

        if (allPlayerDead) {
            isGameOver = true
            isVictory = false
            addLog("💀 Поражение! Все ваши шарики пали...")
        } else if (allEnemiesDead) {
            isGameOver = true
            isVictory = true
            addLog("🎉 Победа! Тёмный шар повержен!")
        }
    }

    fun addLog(message: String) {
        combatLog.add("Ход $turnNumber: $message")
        if (combatLog.size > 10) {
            combatLog.removeAt(0)
        }
    }

    fun nextTurn() {
        // Обновляем перезарядки
        playerBalls.forEach { it.updateTurn() }
        enemyBalls.forEach { it.updateTurn() }
        boss.updateTurn()

        when (currentTurn) {
            Turn.PLAYER -> {
                // Ход врагов
                if (enemyBalls.any { it.isAlive }) {
                    currentTurn = Turn.ENEMY
                } else {
                    currentTurn = Turn.BOSS
                }
            }
            Turn.ENEMY -> {
                currentTurn = Turn.BOSS
            }
            Turn.BOSS -> {
                currentTurn = Turn.PLAYER
                turnNumber++
            }
        }

        selectedBall = null
        selectedAction = null
        targetBall = null
        isBossSelected = false
        
        checkGameOver()
    }
}

