package com.wbrawner.trainterval.shared.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientConfiguration

private val LightThemeColors = lightColors(
    primary = Yellow500,
    primaryVariant = Orange500,
    onPrimary = Color.White,
    secondary = Red700,
    secondaryVariant = Red900,
    onSecondary = Color.White,
    error = Red800
)

private val DarkThemeColors = darkColors(
    primary = Yellow500,
    primaryVariant = Red700,
    onPrimary = Color.White,
    secondary = Red300,
    onSecondary = Color.White,
    error = Red200
)

@Composable
fun TraintervalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkThemeColors else LightThemeColors,
        content = content
    )
}

object TraintervalColors {
//    private val darkTheme: Boolean
//        get() {
//            val uiMode = AmbientConfiguration.current.uiMode
//            return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
//        }

    val warmUp: Color
        get() = Color.White

    val lowIntensity: Color
        get() =  FadedRed

    val highIntensity: Color
        get() = FadedGreen

    val rest: Color
        get() = FadedYellow

    val cooldown: Color
        get() = FadedBlue
  }