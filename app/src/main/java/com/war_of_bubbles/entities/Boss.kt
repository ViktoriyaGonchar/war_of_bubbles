package com.war_of_bubbles.entities

import androidx.compose.ui.geometry.Offset

/**
 * Босс - Тёмный шар - антагонист игры
 */
data class Boss(
    val id: Int = 999,
    var hp: Int = 300,
    var maxHp: Int = 300,
    var atk: Int = 35,
    var def: Int = 20,
    var position: Offset = Offset.Zero,
    var isAlive: Boolean = true,
    var phase: Int = 1,
    var specialCooldown: Int = 0
) {
    val emoji: String = "⚫"
    val name: String = "Тёмный шар"

    fun takeDamage(damage: Int) {
        val actualDamage = (damage - def).coerceAtLeast(1)
        hp = (hp - actualDamage).coerceAtLeast(0)
        
        // При снижении HP до 50% - вторая фаза
        if (hp <= maxHp / 2 && phase == 1) {
            phase = 2
            atk += 10
        }
        
        if (hp <= 0) {
            isAlive = false
        }
    }

    fun updateTurn() {
        if (specialCooldown > 0) {
            specialCooldown--
        }
    }

    fun useDarkAbility(): Boolean {
        if (specialCooldown > 0) return false
        specialCooldown = 4
        return true
    }

    fun reset() {
        hp = maxHp
        isAlive = true
        phase = 1
        atk = 35
        specialCooldown = 0
    }
}

