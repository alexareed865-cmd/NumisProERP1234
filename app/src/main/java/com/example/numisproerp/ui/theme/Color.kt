package com.numisproerp.ui.theme

import androidx.compose.ui.graphics.Color

// iOS-стиль: світла тема (system grouped background)
val LightBackground = Color(0xFFF2F2F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEFEFF4)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF000000)
val LightOnSurfaceVariant = Color(0xFF3C3C43)

// iOS-стиль: темна тема
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF1C1C1E)
val DarkSurfaceVariant = Color(0xFF2C2C2E)
val DarkOnPrimary = Color(0xFFFFFFFF)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFFEBEBF5)

// iOS system colors (https://developer.apple.com/design/human-interface-guidelines/color)
val IOSBlue = Color(0xFF007AFF)
val IOSBlueDark = Color(0xFF0A84FF)
val IOSGreen = Color(0xFF34C759)
val IOSGreenDark = Color(0xFF30D158)
val IOSRed = Color(0xFFFF3B30)
val IOSRedDark = Color(0xFFFF453A)
val IOSOrange = Color(0xFFFF9500)
val IOSOrangeDark = Color(0xFFFF9F0A)
val IOSYellow = Color(0xFFFFCC00)
val IOSPurple = Color(0xFFAF52DE)
val IOSPink = Color(0xFFFF2D55)
val IOSTeal = Color(0xFF5AC8FA)
val IOSIndigo = Color(0xFF5856D6)

// Контейнерні (приглушені) iOS-кольори для фону Material primaryContainer тощо
val IOSBlueContainer = Color(0xFFD9EBFF)
val IOSGreenContainer = Color(0xFFD7F4DD)
val IOSRedContainer = Color(0xFFFFD9D7)
val IOSOrangeContainer = Color(0xFFFFE4BF)

// Акцентні кольори (зворотна сумісність з існуючим кодом, але з оновленими значеннями iOS)
val AccentGreen = IOSGreen
val AccentBlue = IOSBlue
val AccentRed = IOSRed
val AccentOrange = IOSOrange

// Сумісність зі старими назвами
val Purple40 = IOSBlue
val Purple80 = IOSBlueDark
val PurpleGrey40 = Color(0xFF8E8E93)
val PurpleGrey80 = Color(0xFFAEAEB2)
val Pink40 = IOSPink
val Pink80 = IOSPink

// =============================================================================
// OlegSmile theme palette — чорно-золота фірмова тема із емблемою лева.
// Використовується, якщо в Налаштуваннях обрано AppTheme.OLEG_SMILE.
// background робимо прозорим, щоб у NumisProERPTheme можна було показати
// фірмове зображення лева як загальний фон екрану.
// =============================================================================
val OlegBackground = Color(0x00000000)
val OlegBackgroundSolid = Color(0xFF0B0B0D)
val OlegSurface = Color(0xCC15141A)
val OlegSurfaceVariant = Color(0xCC1F1D26)
val OlegPrimaryContainer = Color(0xCC2A2316)
val OlegOnSurface = Color(0xFFEDE3C8)
val OlegOnSurfaceVariant = Color(0xFFC4B68A)

val OlegGold = Color(0xFFD4AF37)
val OlegGoldBright = Color(0xFFF5D76E)
val OlegGoldDim = Color(0xFF8E7625)

val OlegRed = Color(0xFFE57373)
val OlegGreen = Color(0xFF7FCB7F)
val OlegOrange = Color(0xFFE5A54A)
val OlegBlue = Color(0xFF6FA8DC)

// =============================================================================
// OlegSmile v2: темна тема, але без золотих/коричневих відтінків у тексті і панелях.
// Текст — білий/сірий, панелі — нейтрально-темні, як Samsung One UI dark mode.
// Емблема і значки залишаються тими самими.
// =============================================================================
val OlegV2Background = Color(0x00000000)
val OlegV2BackgroundSolid = Color(0xFF121212)
val OlegV2Surface = Color(0xCC1E1E1E)
val OlegV2SurfaceVariant = Color(0xCC2A2A2A)
val OlegV2PrimaryContainer = Color(0xCC2C2C2C)
val OlegV2OnSurface = Color(0xFFFFFFFF)
val OlegV2OnSurfaceVariant = Color(0xFFB0B0B0)

// =============================================================================
// OlegSmile Light: світла тема з кремовим фоном та золотими акцентами,
// як на референсному скріншоті дашборду. Емблема лева видима у верхньому
// блоці, цифри відображаються золотим кольором, картки — білі з м'якою тінню.
// =============================================================================
val OlegLightBackground = Color(0x00000000)
val OlegLightBackgroundSolid = Color(0xFFF5EFE3)
val OlegLightSurface = Color(0xFFFFFFFF)
val OlegLightSurfaceVariant = Color(0xFFFAF4E6)
val OlegLightPrimaryContainer = Color(0xFFFFF6DA)
val OlegLightOnSurface = Color(0xFF1F1B16)
val OlegLightOnSurfaceVariant = Color(0xFF5F584D)
