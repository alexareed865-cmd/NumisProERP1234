package com.numisproerp.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import com.numisproerp.data.settings.AppLanguage

/**
 * Поточна мова UI. Підмінюється у `MainActivity` під час composition і прокидається
 * через ієрархію Compose-дерева. Перемикання миттєве, без рестарту Activity.
 *
 * Чому такий підхід замість `res/values-en/strings.xml`?
 *  - 100+ рядків вже захардкоджено по екранах; масовий рефакторинг через
 *    `stringResource()` + `Configuration.locale` зайняв би занадто багато
 *    безпечних, але об'ємних змін у одному PR.
 *  - Перемикач у `Налаштуваннях` повинен бути миттєвим, без recreate().
 *  - Дві мови — це вся фактична вимога ТЗ; немає сенсу будувати «правильну»
 *    систему ресурсів під більше.
 *
 * Якщо у майбутньому буде потрібна третя мова — це buy-in для переходу
 * на стандартний механізм, тоді просто замінимо `tr` на `stringResource`.
 */
val LocalAppLanguage = compositionLocalOf { AppLanguage.UA }

/**
 * Повертає рядок у поточній мові UI. Використовуй у Composable замість
 * захардкодженого тексту, якщо хочеш, щоб рядок змінювався при перемиканні
 * мови у Налаштуваннях.
 *
 * ```kotlin
 * Text(tr("Склад", "Stock"))
 * ```
 */
@Composable
@ReadOnlyComposable
fun tr(ua: String, en: String): String = when (LocalAppLanguage.current) {
    AppLanguage.UA -> ua
    AppLanguage.EN -> en
}

/**
 * Не-Composable варіант для випадків, коли треба отримати переклад поза
 * Composable scope (наприклад, у ViewModel або при формуванні string-літералів
 * для toast'ів).
 */
fun tr(language: AppLanguage, ua: String, en: String): String = when (language) {
    AppLanguage.UA -> ua
    AppLanguage.EN -> en
}
