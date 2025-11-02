package com.war_of_bubbles.game

import com.war_of_bubbles.entities.Ball
import com.war_of_bubbles.entities.BallType
import com.war_of_bubbles.systems.CombatSystem
import kotlin.random.Random

/**
 * ИИ контроллер для врагов и босса
 */
object AIController {
    
    fun executeEnemyTurn(enemy: Ball, playerBalls: List<Ball>, gameState: GameState) {
        if (!enemy.isAlive) return

        val aliveTargets = playerBalls.filter { it.isAlive }
        if (aliveTargets.isEmpty()) return

        // Выбираем цель с наименьшим HP
        val target = aliveTargets.minByOrNull { it.hp } ?: aliveTargets.random()

        // Иногда используем специальную способность
        val useSpecial = Random.nextInt(100) < 30 && enemy.specialCooldown == 0

        when (enemy.type) {
            BallType.RED -> {
                if (useSpecial) {
                    val results = CombatSystem.redSpecial(enemy, aliveTargets)
                    if (results.isNotEmpty()) {
                        gameState.addLog("${enemy.type.emoji} ${enemy.type.typeName} использует ${enemy.type.getSpecialAbilityName()}!")
                        results.forEach { (ball, damage) ->
                            gameState.addLog("  → ${ball.type.emoji} получает $damage урона")
                        }
                        return
                    }
                }
                val damage = CombatSystem.attack(enemy, target)
                gameState.addLog("${enemy.type.emoji} ${enemy.type.typeName} атакует ${target.type.emoji} на $damage урона!")
            }
            BallType.BLUE -> {
                if (useSpecial) {
                    val shielded = CombatSystem.blueSpecial(enemy, gameState.enemyBalls)
                    if (shielded.isNotEmpty()) {
                        gameState.addLog("${enemy.type.emoji} ${enemy.type.typeName} использует ${enemy.type.getSpecialAbilityName()}!")
                        gameState.addLog("  → Защита активирована!")
                        return
                    }
                }
                val damage = CombatSystem.attack(enemy, target)
                gameState.addLog("${enemy.type.emoji} ${enemy.type.typeName} атакует ${target.type.emoji} на $damage урона!")
            }
            BallType.YELLOW -> {
                if (useSpecial) {
                    val damage = CombatSystem.yellowSpecial(enemy, target)
                    gameState.addLog("${enemy.type.emoji} ${enemy.type.typeName} использует ${enemy.type.getSpecialAbilityName()}!")
                    gameState.addLog("  → ${target.type.emoji} получает $damage урона!")
                    return
                }
                val damage = CombatSystem.attack(enemy, target)
                gameState.addLog("${enemy.type.emoji} ${enemy.type.typeName} атакует ${target.type.emoji} на $damage урона!")
            }
        }
    }

    fun executeBossTurn(boss: com.war_of_bubbles.entities.Boss, playerBalls: List<Ball>, gameState: GameState) {
        if (!boss.isAlive) return

        val aliveTargets = playerBalls.filter { it.isAlive }
        if (aliveTargets.isEmpty()) return

        // Босс чаще использует специальную атаку
        val useSpecial = Random.nextInt(100) < 40 && boss.specialCooldown == 0

        if (useSpecial && aliveTargets.size >= 2) {
            val results = CombatSystem.bossDarkWave(boss, aliveTargets)
            gameState.addLog("${boss.emoji} ${boss.name} использует Тёмную волну!")
            results.forEach { (ball, damage) ->
                gameState.addLog("  → ${ball.type.emoji} получает $damage урона")
            }
        } else {
            val target = aliveTargets.random()
            val damage = CombatSystem.bossAttack(boss, target)
            gameState.addLog("${boss.emoji} ${boss.name} атакует ${target.type.emoji} на $damage урона!")
            
            if (boss.phase == 2) {
                gameState.addLog("  ⚠️ Босс вошёл во вторую фазу! Урон увеличен!")
            }
        }
    }
}

