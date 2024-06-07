package dev.datlag.burningseries.other

import androidx.compose.runtime.Composable
import dev.datlag.tooling.Platform

@Composable
actual fun Platform.rememberIsTv(): Boolean = false