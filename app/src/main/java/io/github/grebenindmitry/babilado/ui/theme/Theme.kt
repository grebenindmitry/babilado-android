package io.github.grebenindmitry.babilado.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette =
    darkColors(primary = Blue200, primaryVariant = Blue700, secondary = Green200, secondaryVariant = Green700,
        surface = Black800)

private val LightColorPalette =
    lightColors(primary = Blue500, primaryVariant = Blue700, secondary = Green500, secondaryVariant = Green700,
        onSecondary = Color.White, surface = White100

        /* Other default colors to override
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        */)

@Composable
fun BabiladoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(colors = colors, typography = Typography, shapes = Shapes, content = content)
}