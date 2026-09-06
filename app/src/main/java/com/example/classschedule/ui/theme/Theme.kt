package com.example.classschedule.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Curated "study" palette so course-block colours stay consistent across devices.
// Dynamic color is kept OFF by default (see ClassScheduleTheme) — dynamic color
// would replace the whole scheme and break the visual language of the timetable.
private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = BrandSurface,
    primaryContainer = BrandBlueContainer,
    onPrimaryContainer = OnBrandBlueContainer,

    secondary = BrandTeal,
    onSecondary = BrandSurface,
    secondaryContainer = BrandTealContainer,
    onSecondaryContainer = OnBrandTealContainer,

    tertiary = BrandAmber,
    onTertiary = BrandSurface,
    tertiaryContainer = BrandAmberContainer,
    onTertiaryContainer = OnBrandAmberContainer,

    background = BrandBackground,
    onBackground = BrandBackgroundDark,
    surface = BrandSurface,
    onSurface = BrandBackgroundDark,
    surfaceVariant = BrandSurfaceVariant,
    onSurfaceVariant = BrandBackgroundDark
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlueDark,
    onPrimary = BrandBackgroundDark,
    primaryContainer = BrandBlueDarkContainer,
    onPrimaryContainer = OnBrandBlueDarkContainer,

    secondary = BrandTealDark,
    onSecondary = BrandBackgroundDark,
    secondaryContainer = BrandTealDarkContainer,
    onSecondaryContainer = OnBrandTealDarkContainer,

    tertiary = BrandAmberDark,
    onTertiary = BrandBackgroundDark,
    tertiaryContainer = BrandAmberDarkContainer,
    onTertiaryContainer = OnBrandAmberDarkContainer,

    background = BrandBackgroundDark,
    onBackground = OnBrandBackgroundDark,
    surface = BrandSurfaceDark,
    onSurface = OnBrandBackgroundDark,
    surfaceVariant = BrandSurfaceVariantDark,
    onSurfaceVariant = OnBrandBackgroundDark
)

@Composable
fun ClassScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is only available on Android 12+ and would replace the curated
    // palette; it is disabled by default and can be turned on from Settings.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
