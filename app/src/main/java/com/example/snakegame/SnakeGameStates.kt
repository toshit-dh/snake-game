package com.example.snakegame

data class SnakeGameStates(
    val xAxisGridSize: Int = 20,
    val yAxisGridSize: Int = 30,
    val direction: Direction = Direction.RIGHT,
    val snake: List<Coordinate> = listOf(
        Coordinate(5,5)
    ),
    val food: Coordinate = generateFood(),
    val isGameOver: Boolean = false,
    val gameStatus: GameStatus = GameStatus.IDLE
){
    companion object{
        fun generateFood(): Coordinate {
            return Coordinate(
                (1..19).random(),
                (1..29).random()
            )
        }
    }
}
enum class Direction{
    UP,
    DOWN,
    LEFT,
    RIGHT
}

enum class GameStatus{
    IDLE,
    STARTED,
    PAUSED
}

data class Coordinate(
    val x: Int,
    val y: Int
)