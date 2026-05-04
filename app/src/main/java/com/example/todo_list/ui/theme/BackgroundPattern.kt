package com.example.todo_list.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.todo_list.R

@Composable
fun PatternBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = Color(0xFF141414)  // белый
            )
    ) {
        // Паттерн из иконок
        DiagonalPatternWithImages()

        // Аниме персонаж в левом нижнем углу (над навигацией)
        //AnimeCharacter()

        // Основной контент
        content()
    }
}

@Composable
private fun AnimeCharacter() {
    // Получаем высоту навигационной панели (примерно 80dp)
    val navigationBarHeight = 80.dp

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        Image(
            painter = painterResource(id = R.drawable.ayanokoji),
            contentDescription = "Anime character",
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-10).dp, y = (-navigationBarHeight + 10.dp))  // поднят над навигацией
                .graphicsLayer {
                    alpha = 0.85f  // немного прозрачный, чтобы не перекрывал важные элементы
                }
        )
    }
}

@Composable
private fun DiagonalPatternWithImages() {
    val iconSize = 28.dp
    val horizontalSpacing = 32.dp
    val verticalSpacing = 28.dp

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Фиксированное количество рядов, которого хватит на любой экран
        for (row in 0..25) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconCount = if (row % 2 == 0) 10 else 9

                if (row % 2 != 0) {
                    Spacer(modifier = Modifier.size(horizontalSpacing / 2))
                }

                repeat(iconCount) { col ->
                    val shouldRotate = (row + col) % 2 != 0

                    Image(
                        painter = painterResource(id = R.drawable.spade),
                        contentDescription = null,
                        modifier = Modifier
                            .size(62.dp)
                            .graphicsLayer {
                                rotationZ = if (shouldRotate) 180f else 0f
                                alpha = 0.12f
                            },
                        colorFilter = ColorFilter.tint(
                            Color(0xFFE3E3E3).copy(alpha = 1f)
                        )
                    )

                    if (col < iconCount - 1) {
                        Spacer(modifier = Modifier.size(horizontalSpacing))
                    }
                }
            }
            Spacer(modifier = Modifier.size(verticalSpacing))
        }
    }
}