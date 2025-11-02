package com.war_of_bubbles.systems

import com.war_of_bubbles.entities.Ball
import com.war_of_bubbles.entities.BallType
import com.war_of_bubbles.entities.Boss
import kotlin.random.Random

/**
 * Система боевой логики
 */
object CombatSystem {
    
    /**
     * Атака шарика по цели
     */
    fun attack(attacker: Ball, target: Ball): Int {
        if (!attacker.isAlive || !target.isAlive) return 0
        
        val baseDamage = attacker.atk
        val effectiveness = attacker.type.getEffectivenessModifier(target.type)
        val damage = (baseDamage * effectiveness).toInt()
        
        target.takeDamage(damage)
        return damage
    }

    /**
     * Атака шарика по боссу
     */
    fun attackBoss(attacker: Ball, target: Boss): Int {
        if (!attacker.isAlive || !target.isAlive) return 0
        
        val baseDamage = attacker.atk
        // Босс имеет среднюю уязвимость ко всем типам
        val damage = (baseDamage * 1.1f).toInt()
        
        target.takeDamage(damage)
        return damage
    }

    /**
     * Атака босса по шарику
     */
    fun bossAttack(boss: Boss, target: Ball): Int {
        if (!boss.isAlive || !target.isAlive) return 0
        
        val damage = boss.atk
        target.takeDamage(damage)
        return damage
    }

    /**
     * Специальная атака Красного - взрыв ярости
     * Атакует всех врагов в радиусе
     */
    fun redSpecial(attacker: Ball, enemies: List<Ball>): List<Pair<Ball, Int>> {
        if (!attacker.useSpecialAbility()) return emptyList()
        
        val damage = (attacker.atk * 0.8f).toInt()
        return enemies
            .filter { it.isAlive }
            .map { enemy ->
                val actualDamage = (damage * attacker.type.getEffectivenessModifier(enemy.type)).toInt()
                enemy.takeDamage(actualDamage)
                enemy to actualDamage
            }
    }

    /**
     * Специальная атака Красного по боссу
     */
    fun redSpecialBoss(attacker: Ball, boss: Boss): Int {
        if (!attacker.useSpecialAbility()) return 0
        
        val damage = (attacker.atk * 1.2f).toInt()
        boss.takeDamage(damage)
        return damage
    }

    /**
     * Специальная способность Синего - щит эмпатии
     */
    fun blueSpecial(attacker: Ball, allies: List<Ball>): List<Ball> {
        if (!attacker.useSpecialAbility()) return emptyList()
        
        attacker.activateShield(3)
        return allies
            .filter { it.isAlive && it.id != attacker.id }
            .map { ally ->
                ally.activateShield(2)
                ally
            }
    }

    /**
     * Специальная способность Жёлтого - молниеносный рывок
     * Двойная атака
     */
    fun yellowSpecial(attacker: Ball, target: Ball): Int {
        if (!attacker.useSpecialAbility()) return 0
        
        val damage1 = attack(attacker, target)
        if (target.isAlive) {
            val damage2 = attack(attacker, target)
            return damage1 + damage2
        }
        return damage1
    }

    /**
     * Специальная способность Жёлтого по боссу
     */
    fun yellowSpecialBoss(attacker: Ball, boss: Boss): Int {
        if (!attacker.useSpecialAbility()) return 0
        
        val damage1 = attackBoss(attacker, boss)
        if (boss.isAlive) {
            val damage2 = attackBoss(attacker, boss)
            return damage1 + damage2
        }
        return damage1
    }

    /**
     * Специальная атака босса - тёмная волна
     */
    fun bossDarkWave(boss: Boss, enemies: List<Ball>): List<Pair<Ball, Int>> {
        if (!boss.useDarkAbility()) return emptyList()
        
        val damage = (boss.atk * 0.7f).toInt()
        return enemies
            .filter { it.isAlive }
            .map { enemy ->
                enemy.takeDamage(damage)
                enemy to damage
            }
    }

    /**
     * Лечение шарика (для будущих зелёных шариков)
     */
    fun heal(target: Ball, amount: Int) {
        if (target.isAlive) {
            target.heal(amount)
        }
    }
}

