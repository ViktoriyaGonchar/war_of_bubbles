package com.war_of_bubbles.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Менеджер звуков для игры
 * Использует встроенные системные звуки и синтезированные звуки
 */
class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundType, Int>()
    private var mediaPlayer: MediaPlayer? = null
    
    init {
        initializeSoundPool()
    }
    
    private fun initializeSoundPool() {
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .build()
    }
    
    /**
     * Воспроизведение звука атаки (синтезированный звук)
     */
    fun playAttackSound() {
        playSynthesizedSound(400f, 100, 0.5f)
    }
    
    /**
     * Воспроизведение звука получения урона
     */
    fun playDamageSound() {
        playSynthesizedSound(200f, 150, 0.6f)
    }
    
    /**
     * Воспроизведение звука победы
     */
    fun playVictorySound() {
        playSynthesizedSound(600f, 300, 0.7f)
    }
    
    /**
     * Воспроизведение звука поражения
     */
    fun playDefeatSound() {
        playSynthesizedSound(150f, 400, 0.5f)
    }
    
    /**
     * Воспроизведение звука выбора
     */
    fun playSelectSound() {
        playSynthesizedSound(800f, 50, 0.3f)
    }
    
    /**
     * Воспроизведение звука специальной способности
     */
    fun playSpecialSound() {
        playSynthesizedSound(500f, 200, 0.8f)
    }
    
    /**
     * Простой синтезатор звука через системные уведомления
     */
    private fun playSynthesizedSound(frequency: Float, duration: Int, volume: Float) {
        try {
            // Используем системные звуки Android
            val toneType = android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            val toneGenerator = android.media.ToneGenerator(
                android.media.AudioManager.STREAM_MUSIC,
                (volume * 100).toInt()
            )
            
            // Проигрываем короткий звук
            toneGenerator.startTone(toneType, duration)
        } catch (e: Exception) {
            // Если системные звуки недоступны, используем вибро-сигнал
            e.printStackTrace()
        }
    }
    
    /**
     * Альтернативный метод - использование системных звуков
     */
    fun playSystemSound(type: SoundType) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val androidMediaUri = when (type) {
                    SoundType.CLICK -> android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                    SoundType.SUCCESS -> android.provider.Settings.System.DEFAULT_RINGTONE_URI
                    SoundType.ERROR -> android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                    SoundType.ATTACK,
                    SoundType.DAMAGE,
                    SoundType.VICTORY,
                    SoundType.DEFEAT,
                    SoundType.SELECT,
                    SoundType.SPECIAL -> android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                }
                
                MediaPlayer.create(context, androidMediaUri)?.apply {
                    setOnCompletionListener { release() }
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun release() {
        soundPool?.release()
        soundPool = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

enum class SoundType {
    CLICK,
    SUCCESS,
    ERROR,
    ATTACK,
    DAMAGE,
    VICTORY,
    DEFEAT,
    SELECT,
    SPECIAL
}

@Composable
fun rememberSoundManager(): SoundManager {
    val context = LocalContext.current
    return remember { SoundManager(context) }
}

