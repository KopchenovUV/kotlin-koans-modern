package com.example.testproject // ← Твой пакет!

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ConfettiAnimation(isVisible: Boolean, onFinished: () -> Unit) {
    if (!isVisible) return

    var frame by remember { mutableStateOf(0) }
    val pieceCount = 150

    // Запоминаем начальные параметры для каждого кусочка
    val pieces = remember {
        List(pieceCount) { index ->
            val seed = Random.nextInt()
            ConfettiData(
                startX = Random.nextFloat(),
                speedX = (Random.nextFloat() - 0.5f) * 0.02f,
                speedY = Random.nextFloat() * 0.03f + 0.01f,
                color = Color(
                    red = Random.nextFloat(),
                    green = Random.nextFloat(),
                    blue = Random.nextFloat()
                ),
                size = Random.nextFloat() * 10 + 6,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 10f,
                delay = Random.nextInt(30)
            )
        }
    }

    // Анимация
    LaunchedEffect(isVisible) {
        val totalFrames = 200
        for (i in 0..totalFrames) {
            frame = i
            delay(16)
        }
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            pieces.forEach { piece ->
                if (frame > piece.delay) {
                    val localFrame = frame - piece.delay
                    val x = piece.startX * size.width + piece.speedX * localFrame * size.width
                    val y = -20f + piece.speedY * localFrame * size.height
                    val rotation = piece.rotationSpeed * localFrame

                    rotate(rotation, pivot = Offset(x, y)) {
                        drawRect(
                            color = piece.color,
                            topLeft = Offset(x, y),
                            size = Size(piece.size, piece.size * 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private data class ConfettiData(
    val startX: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val delay: Int
)