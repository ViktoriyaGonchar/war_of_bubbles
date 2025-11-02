package com.war_of_bubbles.entities

import androidx.compose.ui.geometry.Offset

/**
 * Класс шарика - основная боевая единица
 */
data class Ball(
    val id: Int,
    val type: BallType,
    var hp: Int = type.baseHP,
    var maxHp: Int = type.baseHP,
    var atk: Int = type.baseATK,
    var def: Int = type.baseDEF,
    var position: Offset = Offset.Zero,
    var isAlive: Boolean = true,
    var specialCooldown: Int = 0,
    var isShielded: Boolean = false,
    var shieldDuration: Int = 0
) {
    fun takeDamage(damage: Int) {
        if (isShielded && shieldDuration > 0) {
            // Щит поглощает 50% урона
            hp = (hp - (damage * 0.5f).toInt()).coerceAtLeast(0)
        } else {
            val actualDamage = (damage - def).coerceAtLeast(1)
            hp = (hp - actualDamage).coerceAtLeast(0)
        }
        
        if (hp <= 0) {
            isAlive = false
        }
    }

    fun heal(amount: Int) {
        hp = (hp + amount).coerceAtMost(maxHp)
    }

    fun useSpecialAbility(): Boolean {
        if (specialCooldown > 0) return false
        specialCooldown = 3 // Перезарядка 3 хода
        return true
    }

    fun updateTurn() {
        if (specialCooldown > 0) {
            specialCooldown--
        }
        if (shieldDuration > 0) {
            shieldDuration--
            if (shieldDuration == 0) {
                isShielded = false
            }
        }
    }

    fun activateShield(duration: Int = 2) {
        isShielded = true
        shieldDuration = duration
    }

    fun reset() {
        hp = maxHp
        isAlive = true
        specialCooldown = 0
        isShielded = false
        shieldDuration = 0
    }
}

