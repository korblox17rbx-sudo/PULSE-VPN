package com.pulse.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF07070A)
val Card = Color(0xFF16171C)
val CardHi = Color(0xFF23252D)
val Accent = Color(0xFF3D6DF5)
val AccentSoft = Color(0xFF2A4FBF)
val TextMain = Color(0xFFF2F3F7)
val TextDim = Color(0xFF8A8F98)
val Green = Color(0xFF35C759)

@Composable
fun PulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(background = Bg, surface = Card, primary = Accent),
        content = content
    )
}
