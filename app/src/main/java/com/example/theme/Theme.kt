package com.example.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.techevent.ui.theme.BlueU
import com.example.techevent.ui.theme.GoldU
import com.example.techevent.ui.theme.RedU
import com.example.techevent.ui.theme.WhiteU
private val DarkColorScheme by lazy {
    darkColorScheme(
        primary = RedU,
        secondary = BlueU,
        tertiary = GoldU,
    )
}

private val LightColorScheme = lightColorScheme(
    primary = RedU,
    secondary = BlueU,
    tertiary = GoldU,
    background = WhiteU,
    surface = WhiteU,
    onPrimary = WhiteU,
    onSecondary = WhiteU,
    onTertiary = WhiteU,
    onBackground = WhiteU,
    onSurface = WhiteU,


    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TechEventTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    dynamicColor: Boolean = true,
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