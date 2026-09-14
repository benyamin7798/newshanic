package ir.newshanik.watcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val NewshanikGreen = Color(0xFF2E7D32)
val NewshanikGreenLight = Color(0xFF66BB6A)
val NewshanikGreenDark = Color(0xFF1B5E20)

private val LightColors = lightColorScheme(
    primary = NewshanikGreen,
    secondary = NewshanikGreenLight,
    tertiary = NewshanikGreenDark
)

private val DarkColors = darkColorScheme(
    primary = NewshanikGreenLight,
    secondary = NewshanikGreen,
    tertiary = NewshanikGreenDark
)

enum class AppThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun NewshanikWatcherTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(colorScheme = colors) {
        // چون اپ کاملاً فارسی است، جهت راست‌چین را همیشه اعمال می‌کنیم
        // (حتی اگر زبان سیستم گوشی فارسی نباشد).
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
