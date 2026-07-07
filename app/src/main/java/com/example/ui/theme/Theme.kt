package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Violet Themes
private val VioletDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F)
)

private val VioletLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE)
)

// Forest Green Themes
private val GreenDarkColorScheme = darkColorScheme(
    primary = Emerald80,
    secondary = EmeraldGrey80,
    tertiary = Mint80,
    background = Color(0xFF061A12),
    surface = Color(0xFF061A12)
)

private val GreenLightColorScheme = lightColorScheme(
    primary = Emerald40,
    secondary = EmeraldGrey40,
    tertiary = Mint40,
    background = Color(0xFFF0FDF4),
    surface = Color(0xFFF0FDF4)
)

// Sunset Orange Themes
private val OrangeDarkColorScheme = darkColorScheme(
    primary = Orange80,
    secondary = OrangeGrey80,
    tertiary = Gold80,
    background = Color(0xFF1E1005),
    surface = Color(0xFF1E1005)
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = Orange40,
    secondary = OrangeGrey40,
    tertiary = Gold40,
    background = Color(0xFFFFF7ED),
    surface = Color(0xFFFFF7ED)
)

// Cosmic Slate Themes
private val SlateDarkColorScheme = darkColorScheme(
    primary = Indigo80,
    secondary = SlateGrey80,
    tertiary = Slate80,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B)
)

private val SlateLightColorScheme = lightColorScheme(
    primary = Indigo40,
    secondary = SlateGrey40,
    tertiary = Slate40,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFF1F5F9)
)

@Composable
fun MyApplicationTheme(
    themeName: String = "violet",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeName.lowercase()) {
        "green" -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        "orange" -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
        "slate" -> if (darkTheme) SlateDarkColorScheme else SlateLightColorScheme
        else -> if (darkTheme) VioletDarkColorScheme else VioletLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
