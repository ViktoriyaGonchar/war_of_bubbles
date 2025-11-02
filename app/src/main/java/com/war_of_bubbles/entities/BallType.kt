package com.war_of_bubbles.entities

import androidx.compose.ui.graphics.Color

/**
 * Типы шариков с их характеристиками и эмодзи
 */
enum class BallType(
    val emoji: String,
    val color: Color,
    val name: String,
    val baseHP: Int,
    val baseATK: Int,
    val baseDEF: Int
) {
    RED(
        emoji = "🔴",
        color = Color(0xFFFF4444),
        name = "Красный",
        baseHP = 100,
        baseATK = 30,
        baseDEF = 10
    ),
    BLUE(
        emoji = "🔵",
        color = Color(0xFF4444FF),
        name = "Синий",
        baseHP = 120,
        baseATK = 20,
        baseDEF = 25
    ),
    YELLOW(
        emoji = "🟡",
        color = Color(0xFFFFDD44),
        name = "Жёлтый",
        baseHP = 80,
        baseATK = 25,
        baseDEF = 15
    );

    /**
     * Получить модификатор эффективности против другого типа
     * Красный > Жёлтый > Синий > Красный (камень-ножницы-бумага)
     */
    fun getEffectivenessModifier(against: BallType): Float {
        return when {
            this == RED && against == YELLOW -> 1.5f      // Красный сильнее Жёлтого
            this == YELLOW && against == BLUE -> 1.5f     // Жёлтый быстрее Синего
            this == BLUE && against == RED -> 0.7f        // Синий блокирует Красного
            else -> 1.0f
        }
    }

    /**
     * Получить название специальной способности
     */
    fun getSpecialAbilityName(): String {
        return when (this) {
            RED -> "🔥 Взрыв ярости"
            BLUE -> "🛡️ Щит эмпатии"
            YELLOW -> "⚡ Молниеносный рывок"
        }
    }
}

