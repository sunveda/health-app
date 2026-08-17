package com.sunveda.healthapp.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HealthColorScheme = lightColorScheme()

@Composable
fun HealthAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HealthColorScheme,
        content = content
    )
}
