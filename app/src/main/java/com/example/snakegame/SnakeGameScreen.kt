package com.example.snakegame

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snakegame.ui.theme.Citrine
import com.example.snakegame.ui.theme.Custard
import com.example.snakegame.ui.theme.RoyalBlue

@Composable
fun SnakeGameScreen(
) {
    val viewModel = viewModel<SnakeGameViewModel>()
    val state by viewModel.state.collectAsState()
    val onEvents = viewModel::onEvent
    val foodImageBitmap = ImageBitmap.imageResource(id = R.drawable.img_apple)
    val snakeHeadImageBitmap = when (state.direction) {
        Direction.UP -> ImageBitmap.imageResource(id = R.drawable.img_snake_head3)
        Direction.DOWN -> ImageBitmap.imageResource(id = R.drawable.img_snake_head4)
        Direction.LEFT -> ImageBitmap.imageResource(id = R.drawable.img_snake_head2)
        Direction.RIGHT -> ImageBitmap.imageResource(id = R.drawable.img_snake_head)
    }
    val context = LocalContext.current
    val foodSound = remember {
        MediaPlayer.create(
            context,
            R.raw.food
        )
    }
    val gameOverSound = remember {
        MediaPlayer.create(
            context,
            R.raw.gameover
        )
    }
    LaunchedEffect(key1 = state.snake.size) {
        if (state.snake.size != 1)
            foodSound?.start()
    }
    LaunchedEffect(key1 = state.isGameOver) {
        if (state.isGameOver)
            gameOverSound?.start()
    }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier
                        .padding(16.dp),
                    text = "Score ${state.snake.size - 1}",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2 / 3f)
                    .pointerInput(
                        state.gameStatus
                    ) {
                        if (state.gameStatus != GameStatus.STARTED) {
                            return@pointerInput
                        }
                        detectTapGestures {
                            onEvents(SnakeGameEvents.UpdateDirection(it, size.width))

                        }
                    },
                contentDescription = stringResource(R.string.snake_play_area)
            ) {
                val cellSize = size.width / 20
                drawGameBoard(
                    cellSize = cellSize,
                    cellColor = Custard,
                    borderCellColor = RoyalBlue,
                    gridWidth = state.xAxisGridSize,
                    gridHeight = state.yAxisGridSize
                )
                drawFood(
                    foodImage = foodImageBitmap,
                    cellSize = cellSize.toInt(),
                    coordinates = state.food
                )
                drawSnake(
                    snakeHeadImage = snakeHeadImageBitmap,
                    cellSize = cellSize,
                    snake = state.snake
                )
            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f),
                    enabled = state.gameStatus == GameStatus.PAUSED || state.isGameOver,
                    onClick = {
                        onEvents(SnakeGameEvents.Reset)
                    }
                ) {
                    Text(
                        text = if (state.isGameOver) stringResource(R.string.reset) else stringResource(
                            R.string.new_game
                        )
                    )
                }
                Spacer(
                    modifier = Modifier.width(10.dp)
                )
                Button(
                    modifier = Modifier
                        .weight(1f),
                    onClick = {
                        when (state.gameStatus) {
                            GameStatus.IDLE, GameStatus.PAUSED -> {
                                onEvents(SnakeGameEvents.Start)
                            }

                            GameStatus.STARTED -> {
                                onEvents(SnakeGameEvents.Pause)
                            }

                        }
                    },
                    enabled = !state.isGameOver
                ) {
                    Text(
                        text = when (state.gameStatus) {
                            GameStatus.IDLE -> stringResource(R.string.start)
                            GameStatus.STARTED -> stringResource(R.string.pause)
                            GameStatus.PAUSED -> stringResource(R.string.resume)
                        }
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = state.isGameOver
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp),
                text = stringResource(R.string.game_over),
                style = MaterialTheme.typography.displayMedium
            )

        }
    }

}

private fun DrawScope.drawGameBoard(
    cellSize: Float,
    cellColor: Color,
    borderCellColor: Color,
    gridWidth: Int,
    gridHeight: Int
) {
    for (i in 0 until gridWidth)
        for (j in 0 until gridHeight) {
            val isBorderCell = i == 0 || j == 0 || i == gridWidth - 1 || j == gridHeight - 1
            drawRect(
                color = if (isBorderCell) borderCellColor
                else if ((i + j) % 2 == 0) cellColor
                else cellColor.copy(alpha = 0.5f),
                topLeft = Offset(x = i * cellSize, y = j * cellSize),
                size = Size(cellSize, cellSize)
            )
        }
}

private fun DrawScope.drawFood(
    foodImage: ImageBitmap,
    cellSize: Int,
    coordinates: Coordinate
) {
    drawImage(
        image = foodImage,
        dstOffset = IntOffset(
            x = (coordinates.x * cellSize),
            y = (coordinates.y * cellSize)
        ),
        dstSize = IntSize(cellSize, cellSize)
    )
}

private fun DrawScope.drawSnake(
    snakeHeadImage: ImageBitmap,
    cellSize: Float,
    snake: List<Coordinate>
) {
    snake.forEachIndexed { index, coordinates ->
        val radius = if (index == snake.lastIndex) cellSize / 2.5f else cellSize / 2
        if (index == 0)
            drawImage(
                image = snakeHeadImage,
                dstOffset = IntOffset(
                    x = (coordinates.x * cellSize).toInt(),
                    y = (coordinates.y * cellSize).toInt()
                ),
                dstSize = IntSize(cellSize.toInt(), cellSize.toInt())
            )
        else
            drawCircle(
                color = Citrine,
                center = Offset(
                    x = (coordinates.x * cellSize) + radius,
                    y = (coordinates.y * cellSize) + radius
                ),
                radius = radius
            )
    }
}

