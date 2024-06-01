package com.example.snakegame

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SnakeGameViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        SnakeGameStates()
    )
    val state = _state.asStateFlow()

    fun onEvent(event: SnakeGameEvents) {
        when (event) {
            SnakeGameEvents.Pause -> {
                _state.update {
                    it.copy(
                        gameStatus = GameStatus.PAUSED
                    )
                }
            }

            SnakeGameEvents.Start -> {
                _state.update {
                    it.copy(
                        gameStatus = GameStatus.STARTED
                    )
                }
                viewModelScope.launch {
                    while (state.value.gameStatus == GameStatus.STARTED) {
                        val delayMillis = when(state.value.snake.size){
                            in 1..5 -> 120L
                            in 6..10 -> 110L
                            else -> 100L
                        }
                        delay(delayMillis)
                        _state.update {
                            updateGame(it)
                        }
                    }
                }
            }

            is SnakeGameEvents.UpdateDirection -> {
                updateDirection(event.offset,event.canvasWidth)
            }

            SnakeGameEvents.Reset -> {
                _state.value = SnakeGameStates()
            }
        }
    }

    private fun updateDirection(
        offset: Offset,
        canvasWidth: Int
    ) {
        if (!state.value.isGameOver){
            val cellSize = canvasWidth / state.value.xAxisGridSize
            val xDirection = offset.x / cellSize
            val yDirection = offset.y / cellSize
            val head = state.value.snake.first()

            _state.update {
                it.copy(
                    direction = when(state.value.direction){
                        Direction.UP,Direction.DOWN -> {
                            if (xDirection < head.x) Direction.LEFT else Direction.RIGHT
                        }
                        Direction.LEFT,Direction.RIGHT -> {
                            if (yDirection < head.y) Direction.UP else Direction.DOWN
                        }
                    }
                )
            }
        }
    }

    private fun updateGame(currentGame: SnakeGameStates): SnakeGameStates {
        if (currentGame.isGameOver) {
            return currentGame
        }
        val head = currentGame.snake.first()
        val xAxisGridSize = currentGame.xAxisGridSize
        val yAxisGridSize = currentGame.yAxisGridSize

        val newHead = when (currentGame.direction) {
            Direction.UP -> {
                Coordinate(x = head.x, y = head.y - 1)
            }

            Direction.DOWN -> {
                Coordinate(x = head.x, y = head.y + 1)
            }

            Direction.LEFT -> {
                Coordinate(x = head.x - 1, y = head.y)
            }

            Direction.RIGHT -> {
                Coordinate(x = head.x + 1, y = head.y)
            }
        }

        if (
            currentGame.snake.contains(newHead) ||
            newHead.x < 0 ||
            newHead.x > xAxisGridSize ||
            newHead.y < 0 ||
            newHead.y > yAxisGridSize
        ) {
            return currentGame.copy(
                isGameOver = true
            )
        }

        var newSnake = mutableListOf(newHead) + currentGame.snake
        val newFood =
            if (newHead == currentGame.food) SnakeGameStates.generateFood() else currentGame.food

        if (newHead != currentGame.food) {
            newSnake = newSnake.toMutableList()
            newSnake.removeAt(newSnake.size - 1)
        }

        return currentGame.copy(
            snake = newSnake,
            food = newFood
        )
    }

}