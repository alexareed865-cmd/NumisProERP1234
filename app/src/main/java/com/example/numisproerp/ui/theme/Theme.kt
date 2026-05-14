package com.numisproerp.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.numisproerp.R
import com.numisproerp.data.settings.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = IOSBlueDark,
    onPrimary = DarkOnPrimary,
    primaryContainer = Color(0xFF0A2540),
    onPrimaryContainer = IOSBlueDark,
    secondary = IOSTeal,
    onSecondary = DarkOnPrimary,
    tertiary = IOSPurple,
    onTertiary = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = IOSRedDark,
    onError = DarkOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = IOSBlue,
    onPrimary = LightOnPrimary,
    primaryContainer = IOSBlueContainer,
    onPrimaryContainer = Color(0xFF003C7A),
    secondary = IOSTeal,
    onSecondary = LightOnPrimary,
    tertiary = IOSPurple,
    onTertiary = LightOnPrimary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = IOSRed,
    onError = LightOnPrimary
)

private val OlegSmileColorScheme = darkColorScheme(
    primary = OlegGold,
    onPrimary = Color(0xFF0B0B0D),
    primaryContainer = OlegPrimaryContainer,
    onPrimaryContainer = OlegGoldBright,
    secondary = OlegGoldBright,
    onSecondary = Color(0xFF0B0B0D),
    tertiary = OlegOrange,
    onTertiary = Color(0xFF0B0B0D),
    background = OlegBackground,
    onBackground = OlegOnSurface,
    surface = OlegSurface,
    onSurface = OlegOnSurface,
    surfaceVariant = OlegSurfaceVariant,
    onSurfaceVariant = OlegOnSurfaceVariant,
    error = OlegRed,
    onError = Color(0xFF0B0B0D)
)

private val OlegSmileV2ColorScheme = darkColorScheme(
    primary = OlegGold,
    onPrimary = Color(0xFF121212),
    primaryContainer = OlegV2PrimaryContainer,
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFBBBBBB),
    onSecondary = Color(0xFF121212),
    tertiary = Color(0xFF999999),
    onTertiary = Color(0xFF121212),
    background = OlegV2Background,
    onBackground = OlegV2OnSurface,
    surface = OlegV2Surface,
    onSurface = OlegV2OnSurface,
    surfaceVariant = OlegV2SurfaceVariant,
    onSurfaceVariant = OlegV2OnSurfaceVariant,
    error = OlegRed,
    onError = Color(0xFF121212)
)

private val OlegSmileLightColorScheme = lightColorScheme(
    primary = OlegGold,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = OlegLightPrimaryContainer,
    onPrimaryContainer = Color(0xFF1F1B16),
    secondary = OlegOrange,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = OlegBlue,
    onTertiary = Color(0xFFFFFFFF),
    background = OlegLightBackground,
    onBackground = OlegLightOnSurface,
    surface = OlegLightSurface,
    onSurface = OlegLightOnSurface,
    surfaceVariant = OlegLightSurfaceVariant,
    onSurfaceVariant = OlegLightOnSurfaceVariant,
    error = IOSRed,
    onError = Color(0xFFFFFFFF)
)

private val IOSShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NumisProERPTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.OLEG_SMILE -> OlegSmileColorScheme
        AppTheme.OLEG_SMILE_V2 -> OlegSmileV2ColorScheme
        AppTheme.OLEG_SMILE_LIGHT -> OlegSmileLightColorScheme
        AppTheme.DEFAULT -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = IOSShapes
        ) {
            when (appTheme) {
                AppTheme.OLEG_SMILE -> {
                    Box(modifier = Modifier.fillMaxSize().background(OlegBackgroundSolid)) {
                        Image(
                            painter = painterResource(id = R.drawable.oleg_smile_background),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xAA000000),
                                            Color(0x99000000),
                                            Color(0xAA000000)
                                        )
                                    )
                                )
                        )
                        content()
                    }
                }
                AppTheme.OLEG_SMILE_V2 -> {
                    Box(modifier = Modifier.fillMaxSize().background(OlegV2BackgroundSolid)) {
                        Image(
                            painter = painterResource(id = R.drawable.oleg_smile_background),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.15f
                        )
                        content()
                    }
                }
                AppTheme.OLEG_SMILE_LIGHT -> {
                    Box(modifier = Modifier.fillMaxSize().background(OlegLightBackgroundSolid)) {
                        Image(
                            painter = painterResource(id = R.drawable.oleg_smile_background),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.06f
                        )
                        content()
                    }
                }
                else -> {
                    content()
                }
            }
        }
    }
}
