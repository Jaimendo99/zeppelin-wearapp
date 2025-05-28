package com.zeppelin.zeppelin_wear.presentation.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.zeppelin.zeppelin_wear.R
import com.zeppelin.zeppelin_wear.data.ScreenUiState
import com.zeppelin.zeppelin_wear.presentation.theme.ZeppelinTheme
import com.zeppelin.zeppelin_wear.presentation.theme.displayFontFamily

@Composable
fun MainScreen(
    screenState: ScreenUiState,
    timerPercentage: Float = 0f,
    minutes: Int = 0,
    seconds: Int = 0
) {
    Scaffold(
        timeText = { TimeText(modifier = Modifier.offset(y = (10).dp)) },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (screenState) {
                is ScreenUiState.PhoneNotConnected, is  ScreenUiState.PhoneConnected,
                ScreenUiState.SessionIdle -> {
                    NormalScreenLayout(screenState)
                }
                is ScreenUiState.SessionWork, is ScreenUiState.SessionBreak -> {
                    SessionScreen(screenState)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SessionScreen(
    mainScreenState: ScreenUiState,
) {

    when(mainScreenState){
        is ScreenUiState.SessionWork, is ScreenUiState.SessionBreak -> {

            val timerState = when(mainScreenState) {
                is ScreenUiState.SessionWork -> mainScreenState.timerState
                is ScreenUiState.SessionBreak -> mainScreenState.timerState
                else -> return
            }

            val shape = remember {
                RoundedPolygon.star(
                    8,
                    rounding = CornerRounding(10f),
                    innerRadius = 0.75f
                )
            }

            val ringColor = when (mainScreenState) {
                is ScreenUiState.SessionWork -> MaterialTheme.colorScheme.primaryContainer
                is ScreenUiState.SessionBreak -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                OuterRing(
                    percentage = timerState.timerPercentage,
                    ringColor = ringColor,
                    trackColor = ringColor.copy(alpha = 0.15f),
                    strokeWidth = 7.dp,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "%02d".format(timerState.minutes),
                            style = MaterialTheme.typography.displaySmall,
                        )
                        Text(
                            text = "min",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.offset(y = (-6).dp)
                        )
                    }
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .offset(y = (-10).dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "%02d".format(timerState.seconds),
                            style = MaterialTheme.typography.displaySmall,
                        )
                        Text(
                            text = "sec",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.offset(y = (-6).dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.BottomCenter)

                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-15).dp)
                            .align(Alignment.Center)
                            .size(6.dp)
                            .clip(shape.toShape())
                            .background(color = ringColor, shape = shape.toShape())
                    )
                    Text(
                        text = when (mainScreenState) {
                            is ScreenUiState.SessionWork -> "Trabajo"
                            is ScreenUiState.SessionBreak -> "Descanso"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }
        else -> { return }
    }

}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NormalScreenLayout(state: ScreenUiState = ScreenUiState.SessionIdle) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                state.message,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                fontFamily = displayFontFamily,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = MaterialTheme.typography.bodySmall.fontWeight
            )
            Spacer(modifier = Modifier.padding(8.dp))
            when (state) {
                is ScreenUiState.PhoneNotConnected -> PhoneNotConnectedIcon()
                is ScreenUiState.PhoneConnected -> PhoneConnectedIcon()
                else -> ContainedLoadingIndicator(
                    modifier = Modifier.size(48.dp),
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
        ZeppelinLogo(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun PhoneNotConnectedIcon() {
    Image(
        painter = painterResource(R.drawable.phone_not_conected),
        contentDescription = "Phone not connected",
        modifier = Modifier.size(48.dp)
    )
}

@Composable
fun PhoneConnectedIcon() {
    Image(
        painter = painterResource(R.drawable.phone_conected),
        contentDescription = "Phone connected",
        modifier = Modifier.size(48.dp),
    )
}

@Composable
fun ZeppelinLogo(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.logo_on_dark),
        contentDescription = "Zeppelin Logo",
        modifier = modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.background
    )
}


@Composable
fun OuterRing(
    percentage: Float,
    modifier: Modifier = Modifier,
    ringColor: Color = MaterialTheme.colorScheme.primaryContainer,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
    strokeWidth: Dp = 5.dp
) {
    val clampedPercentage = percentage.coerceIn(0f, 100f)

    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(
                RoundedCornerShape(100.dp)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)

            val radius =
                (size.minDimension / 2f) - (strokeWidthPx / 2f)

            val arcTopLeft = Offset(
                canvasCenter.x - radius,
                canvasCenter.y - radius
            )
            val arcSize = Size(radius * 2, radius * 2)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx)
            )

            if (clampedPercentage > 0f) {
                val sweepAngle = (clampedPercentage / 100f) * 360f
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}


@Composable
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true, backgroundColor = 0xFF000000)
fun MainScreenPreview() {
    ZeppelinTheme {
        MainScreen(ScreenUiState.PhoneNotConnected)
    }
}

@Composable
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true, backgroundColor = 0xFF000000)
fun MainScreenPreview1() {
    ZeppelinTheme {
        MainScreen(ScreenUiState.PhoneConnected)
    }
}

@Composable
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true, backgroundColor = 0xFF000000)
fun MainScreenPreview2() {
    ZeppelinTheme {
        MainScreen(ScreenUiState.SessionIdle)
    }
}

@Composable
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true, backgroundColor = 0xFF000000)
fun MainScreenPreview3() {
    ZeppelinTheme {
        MainScreen(screenState = ScreenUiState.SessionWork(
            timerState = com.zeppelin.zeppelin_wear.data.TimerState(
                timerPercentage = 50f,
                minutes = 25,
                seconds = 30,
                isRunning = true
            )
        ))
    }
}

@Composable
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true, backgroundColor = 0xFF000000)
fun MainScreenPreview4() {
    ZeppelinTheme {
        MainScreen(ScreenUiState.SessionBreak(
            timerState = com.zeppelin.zeppelin_wear.data.TimerState(
                timerPercentage = 75f,
                minutes = 5,
                seconds = 15,
                isRunning = true
            )
        ))
    }
}