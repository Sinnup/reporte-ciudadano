package com.espert.reporteciudadano

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val md_primary_light = Color(0xFF1A6B4A)
private val md_on_primary_light = Color(0xFFFFFFFF)
private val md_primary_container_light = Color(0xFFB7F0D4)
private val md_on_primary_container_light = Color(0xFF002114)
private val md_secondary_light = Color(0xFF4D6357)
private val md_on_secondary_light = Color(0xFFFFFFFF)
private val md_secondary_container_light = Color(0xFFD0E8D8)
private val md_on_secondary_container_light = Color(0xFF0B1F16)
private val md_tertiary_light = Color(0xFF8B5E3C)
private val md_on_tertiary_light = Color(0xFFFFFFFF)
private val md_tertiary_container_light = Color(0xFFFFDCC1)
private val md_on_tertiary_container_light = Color(0xFF321200)
private val md_error_light = Color(0xFFBA1A1A)
private val md_on_error_light = Color(0xFFFFFFFF)
private val md_error_container_light = Color(0xFFFFDAD6)
private val md_on_error_container_light = Color(0xFF410002)
private val md_background_light = Color(0xFFF5FBF6)
private val md_on_background_light = Color(0xFF171D1A)
private val md_surface_light = Color(0xFFF5FBF6)
private val md_on_surface_light = Color(0xFF171D1A)
private val md_surface_variant_light = Color(0xFFDBE5DD)
private val md_on_surface_variant_light = Color(0xFF404943)
private val md_outline_light = Color(0xFF707972)
private val md_outline_variant_light = Color(0xFFBFC9C1)

private val md_primary_dark = Color(0xFF8FCFAC)
private val md_on_primary_dark = Color(0xFF003824)
private val md_primary_container_dark = Color(0xFF005236)
private val md_on_primary_container_dark = Color(0xFFB7F0D4)
private val md_secondary_dark = Color(0xFFB4CCBC)
private val md_on_secondary_dark = Color(0xFF20352A)
private val md_secondary_container_dark = Color(0xFF364B40)
private val md_on_secondary_container_dark = Color(0xFFD0E8D8)
private val md_tertiary_dark = Color(0xFFF4BA87)
private val md_on_tertiary_dark = Color(0xFF4E2600)
private val md_tertiary_container_dark = Color(0xFF6E3A11)
private val md_on_tertiary_container_dark = Color(0xFFFFDCC1)
private val md_error_dark = Color(0xFFFFB4AB)
private val md_on_error_dark = Color(0xFF690005)
private val md_error_container_dark = Color(0xFF93000A)
private val md_on_error_container_dark = Color(0xFFFFDAD6)
private val md_background_dark = Color(0xFF0F1512)
private val md_on_background_dark = Color(0xFFDEE4DF)
private val md_surface_dark = Color(0xFF0F1512)
private val md_on_surface_dark = Color(0xFFDEE4DF)
private val md_surface_variant_dark = Color(0xFF404943)
private val md_on_surface_variant_dark = Color(0xFFBFC9C1)
private val md_outline_dark = Color(0xFF89938C)
private val md_outline_variant_dark = Color(0xFF404943)

private val LightColors = lightColorScheme(
    primary = md_primary_light,
    onPrimary = md_on_primary_light,
    primaryContainer = md_primary_container_light,
    onPrimaryContainer = md_on_primary_container_light,
    secondary = md_secondary_light,
    onSecondary = md_on_secondary_light,
    secondaryContainer = md_secondary_container_light,
    onSecondaryContainer = md_on_secondary_container_light,
    tertiary = md_tertiary_light,
    onTertiary = md_on_tertiary_light,
    tertiaryContainer = md_tertiary_container_light,
    onTertiaryContainer = md_on_tertiary_container_light,
    error = md_error_light,
    onError = md_on_error_light,
    errorContainer = md_error_container_light,
    onErrorContainer = md_on_error_container_light,
    background = md_background_light,
    onBackground = md_on_background_light,
    surface = md_surface_light,
    onSurface = md_on_surface_light,
    surfaceVariant = md_surface_variant_light,
    onSurfaceVariant = md_on_surface_variant_light,
    outline = md_outline_light,
    outlineVariant = md_outline_variant_light,
)

private val DarkColors = darkColorScheme(
    primary = md_primary_dark,
    onPrimary = md_on_primary_dark,
    primaryContainer = md_primary_container_dark,
    onPrimaryContainer = md_on_primary_container_dark,
    secondary = md_secondary_dark,
    onSecondary = md_on_secondary_dark,
    secondaryContainer = md_secondary_container_dark,
    onSecondaryContainer = md_on_secondary_container_dark,
    tertiary = md_tertiary_dark,
    onTertiary = md_on_tertiary_dark,
    tertiaryContainer = md_tertiary_container_dark,
    onTertiaryContainer = md_on_tertiary_container_dark,
    error = md_error_dark,
    onError = md_on_error_dark,
    errorContainer = md_error_container_dark,
    onErrorContainer = md_on_error_container_dark,
    background = md_background_dark,
    onBackground = md_on_background_dark,
    surface = md_surface_dark,
    onSurface = md_on_surface_dark,
    surfaceVariant = md_surface_variant_dark,
    onSurfaceVariant = md_on_surface_variant_dark,
    outline = md_outline_dark,
    outlineVariant = md_outline_variant_dark,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
