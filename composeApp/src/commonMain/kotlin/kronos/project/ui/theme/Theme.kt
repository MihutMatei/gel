package kronos.project.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import gel.composeapp.generated.resources.Res
import gel.composeapp.generated.resources.inter
import org.jetbrains.compose.resources.Font

val md_theme_light_primary = Color(0xFF006C4C)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFF89F8C7)
val md_theme_light_onPrimaryContainer = Color(0xFF002114)
val md_theme_light_secondary = Color(0xFF4D6357)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFCFE9D9)
val md_theme_light_onSecondaryContainer = Color(0xFF092016)

val md_theme_dark_primary = Color(0xFF6CDBAC)
val md_theme_dark_onPrimary = Color(0xFF003825)
val md_theme_dark_primaryContainer = Color(0xFF005138)
val md_theme_dark_onPrimaryContainer = Color(0xFF89F8C7)
val md_theme_dark_secondary = Color(0xFFB3CCBD)
val md_theme_dark_onSecondary = Color(0xFF1F352A)
val md_theme_dark_secondaryContainer = Color(0xFF354B3F)
val md_theme_dark_onSecondaryContainer = Color(0xFFCFE9D9)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
)

@Composable
fun CivicLensTypography(): Typography {
    val cleanFontFamily = FontFamily(Font(Res.font.inter))
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = cleanFontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = cleanFontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = cleanFontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = cleanFontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = cleanFontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = cleanFontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = cleanFontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = cleanFontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = cleanFontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = cleanFontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = cleanFontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = cleanFontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = cleanFontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = cleanFontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = cleanFontFamily)
    )
}

@Composable
fun CivicLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = CivicLensTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

fun Modifier.shimmerLoadingAnimation(): Modifier = composed {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    background(brush)
}
