package com.example.snakegame

import androidx.compose.ui.geometry.Offset

sealed class SnakeGameEvents {
    data object Start : SnakeGameEvents()
    data object Pause : SnakeGameEvents()
    data object Reset : SnakeGameEvents()
    data class UpdateDirection(
        val offset: Offset,
        val canvasWidth: Int
    ): SnakeGameEvents()
}