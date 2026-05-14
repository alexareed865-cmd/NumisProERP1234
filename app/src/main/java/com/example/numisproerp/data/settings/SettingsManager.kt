package com.numisproerp.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Тема додатку. Користувач обирає в Налаштуваннях.
 *
 * - [DEFAULT] — класична iOS-Blue палітра, з якою додаток був до цього.
 * - [OLEG_SMILE] — фірмова чорно-золота тема із емблемою лева.
 */
enum class AppTheme {
    DEFAULT,
    OLEG_SMILE,
    OLEG_SMILE_V2,
    OLEG_SMILE_LIGHT;

    companion object {
        fun fromKey(key: String?): AppTheme = when (key) {
            OLEG_SMILE.name -> OLEG_SMILE
            OLEG_SMILE_V2.name -> OLEG_SMILE_V2
            OLEG_SMILE_LIGHT.name -> OLEG_SMILE_LIGHT
            else -> DEFAULT
        }
    }
}

/**
 * Мова інтерфейсу. Перемикається миттєво без рестарту Activity через
 * `LocalAppLanguage` CompositionLocal та helper [com.numisproerp.ui.i18n.tr].
 */
enum class AppLanguage {
    UA,
    EN;

    companion object {
        fun fromKey(key: String?): AppLanguage = when (key) {
            EN.name -> EN
            else -> UA
        }
    }
}

/**
 * Простий менеджер користувацьких налаштувань на основі SharedPreferences.
 * Тримає Compose-friendly реактивний стан через [MutableState], щоб тема
 * та мова перемикалися миттєво без рестарту Activity.
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _theme: MutableState<AppTheme> =
        mutableStateOf(AppTheme.fromKey(prefs.getString(KEY_THEME, null)))

    val themeState: MutableState<AppTheme>
        get() = _theme

    var theme: AppTheme
        get() = _theme.value
        set(value) {
            _theme.value = value
            prefs.edit().putString(KEY_THEME, value.name).apply()
        }

    private val _language: MutableState<AppLanguage> =
        mutableStateOf(AppLanguage.fromKey(prefs.getString(KEY_LANGUAGE, null)))

    val languageState: MutableState<AppLanguage>
        get() = _language

    var language: AppLanguage
        get() = _language.value
        set(value) {
            _language.value = value
            prefs.edit().putString(KEY_LANGUAGE, value.name).apply()
        }

    /**
     * Поріг низького залишку для in-app сповіщень (товар з `1..threshold` шт.
     * показується як WARNING). 0 — функцію вимкнено.
     */
    private val _lowStockThreshold: MutableState<Int> =
        mutableStateOf(prefs.getInt(KEY_LOW_STOCK_THRESHOLD, DEFAULT_LOW_STOCK_THRESHOLD))

    val lowStockThresholdState: MutableState<Int>
        get() = _lowStockThreshold

    var lowStockThreshold: Int
        get() = _lowStockThreshold.value
        set(value) {
            val clamped = value.coerceIn(0, MAX_LOW_STOCK_THRESHOLD)
            _lowStockThreshold.value = clamped
            prefs.edit().putInt(KEY_LOW_STOCK_THRESHOLD, clamped).apply()
        }

    /**
     * URI звуку нагадування для замітки. Може бути:
     * - порожнім рядком (тоді використовується системний DEFAULT_NOTIFICATION_URI),
     * - системним RingtoneManager URI (`content://media/...`),
     * - локальним файлом з cacheDir/custom_sounds/ (`file://...`).
     */
    private val _noteAlarmSoundUri: MutableState<String> =
        mutableStateOf(prefs.getString(KEY_NOTE_ALARM_SOUND_URI, "") ?: "")

    val noteAlarmSoundUriState: MutableState<String>
        get() = _noteAlarmSoundUri

    var noteAlarmSoundUri: String
        get() = _noteAlarmSoundUri.value
        set(value) {
            _noteAlarmSoundUri.value = value
            prefs.edit().putString(KEY_NOTE_ALARM_SOUND_URI, value).apply()
        }

    private val _noteAlarmSoundLabel: MutableState<String> =
        mutableStateOf(prefs.getString(KEY_NOTE_ALARM_SOUND_LABEL, "") ?: "")

    val noteAlarmSoundLabelState: MutableState<String>
        get() = _noteAlarmSoundLabel

    var noteAlarmSoundLabel: String
        get() = _noteAlarmSoundLabel.value
        set(value) {
            _noteAlarmSoundLabel.value = value
            prefs.edit().putString(KEY_NOTE_ALARM_SOUND_LABEL, value).apply()
        }

    // ==================== ШРИФТИ ====================
    private val _fontSize: MutableState<Int> =
        mutableStateOf(prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE))

    val fontSizeState: MutableState<Int>
        get() = _fontSize

    var fontSize: Int
        get() = _fontSize.value
        set(value) {
            val clamped = value.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
            _fontSize.value = clamped
            prefs.edit().putInt(KEY_FONT_SIZE, clamped).apply()
        }

    private val _fontFamily: MutableState<String> =
        mutableStateOf(prefs.getString(KEY_FONT_FAMILY, DEFAULT_FONT_FAMILY) ?: DEFAULT_FONT_FAMILY)

    val fontFamilyState: MutableState<String>
        get() = _fontFamily

    var fontFamily: String
        get() = _fontFamily.value
        set(value) {
            _fontFamily.value = value
            prefs.edit().putString(KEY_FONT_FAMILY, value).apply()
        }

    private val _fontColor: MutableState<String> =
        mutableStateOf(prefs.getString(KEY_FONT_COLOR, "") ?: "")

    val fontColorState: MutableState<String>
        get() = _fontColor

    var fontColor: String
        get() = _fontColor.value
        set(value) {
            _fontColor.value = value
            prefs.edit().putString(KEY_FONT_COLOR, value).apply()
        }

    // ==================== ФОНОВИЙ МАЛЮНОК ====================
    private val _backgroundImagePath: MutableState<String> =
        mutableStateOf(prefs.getString(KEY_BG_IMAGE_PATH, "") ?: "")

    val backgroundImagePathState: MutableState<String>
        get() = _backgroundImagePath

    var backgroundImagePath: String
        get() = _backgroundImagePath.value
        set(value) {
            _backgroundImagePath.value = value
            prefs.edit().putString(KEY_BG_IMAGE_PATH, value).apply()
        }

    companion object {
        private const val PREFS_NAME = "numispro_settings"
        private const val KEY_THEME = "app_theme"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_LOW_STOCK_THRESHOLD = "low_stock_threshold"
        private const val KEY_NOTE_ALARM_SOUND_URI = "note_alarm_sound_uri"
        private const val KEY_NOTE_ALARM_SOUND_LABEL = "note_alarm_sound_label"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_FONT_FAMILY = "font_family"
        private const val KEY_FONT_COLOR = "font_color"
        private const val KEY_BG_IMAGE_PATH = "bg_image_path"
        const val DEFAULT_LOW_STOCK_THRESHOLD = 3
        const val MAX_LOW_STOCK_THRESHOLD = 20
        const val DEFAULT_FONT_SIZE = 14
        const val MIN_FONT_SIZE = 10
        const val MAX_FONT_SIZE = 24
        const val DEFAULT_FONT_FAMILY = "system"
    }
}
