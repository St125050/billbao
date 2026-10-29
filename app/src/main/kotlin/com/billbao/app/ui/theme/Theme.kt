package com.billbao.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = BillbaoGreenPrimary,
    onPrimary = Color(0xFF06140D),
    primaryContainer = BillbaoGreenContainer,
    onPrimaryContainer = BillbaoGreenLight,
    secondary = BillbaoShieldBlue,
    onSecondary = Color(0xFF032230),
    background = BillbaoBgDark,
    onBackground = BillbaoTextWhite,
    surface = BillbaoCardDark,
    onSurface = BillbaoTextWhite,
    surfaceVariant = BillbaoCardElevatedDark,
    onSurfaceVariant = BillbaoTextMuted,
    outline = BillbaoBorderDark,
    error = BillbaoErrorRed
)

val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = Color.White,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = Color(0xFF003920),
    secondary = secondaryLight,
    onSecondary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = BillbaoErrorRed
)

@Composable
fun BillbaoTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode like the screenshot
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
