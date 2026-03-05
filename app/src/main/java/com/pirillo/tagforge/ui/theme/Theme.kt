package com.pirillo.tagforge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Void = Color(0xFF0A0A0F)
val Surface = Color(0xFF12121A)
val SurfaceLight = Color(0xFF1A1A26)
val Accent = Color(0xFF00E5A0)
val AccentDim = Color(0x1F00E5A0)
val Warning = Color(0xFFFF6B4A)
val WarningDim = Color(0x1FFF6B4A)
val Clone = Color(0xFF7B61FF)
val CloneDim = Color(0x1F7B61FF)
val TextPrimary = Color(0xFFF0F0F5)
val TextMuted = Color(0xFF8888A0)
val TextDim = Color(0xFF55556A)
val Border = Color(0xFF2A2A38)
val InfoBlue = Color(0xFF00A0FF)
val InfoBlueDim = Color(0x1F00A0FF)

private val Colors = darkColorScheme(
    primary = Accent, onPrimary = Void, secondary = Clone, onSecondary = Color.White,
    tertiary = Warning, background = Void, surface = Surface, surfaceVariant = SurfaceLight,
    onBackground = TextPrimary, onSurface = TextPrimary, onSurfaceVariant = TextMuted,
    outline = Border, error = Warning,
)

val TagForgeTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = (-0.5).sp, color = TextPrimary),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = (-0.3).sp, color = TextPrimary),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, color = TextMuted),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, color = TextMuted),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.8.sp, color = TextDim),
    labelSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp, color = TextDim),
)

@Composable
fun TagForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = TagForgeTypography, content = content)
}
