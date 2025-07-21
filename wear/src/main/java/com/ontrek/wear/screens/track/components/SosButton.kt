package com.ontrek.wear.screens.track.components

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.theme.OnTrekTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val animationDuration = 5000L

@Composable
fun SosButton(
    modifier: Modifier = Modifier,
    sweepAngle: Float = 0f,
    onSosTriggered: () -> Unit,
    onPressStateChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    }
    val scope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    val scaleAnim = remember { Animatable(1f) }

    val widthFactor = remember(sweepAngle) {
        val normalizedAngle = (sweepAngle / 360f).coerceIn(0f, 1f)
        0.4f + (normalizedAngle * 0.7f)
    }

    LaunchedEffect(isPressed) {
        onPressStateChanged(isPressed)
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val initialHeight = 0.14f
        val scaleRate = 1.0f / initialHeight

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    // Set the transform origin to bottom center
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1.0f)
                    alpha = if (isPressed) 0.9f else 1f
                }
                .fillMaxHeight(initialHeight)
                .fillMaxWidth(widthFactor)
                .clip(
                    RoundedCornerShape(
                        topStart = 100.dp, topEnd = 100.dp, bottomStart = 0.dp, bottomEnd = 0.dp
                    )
                )
                .background(MaterialTheme.colorScheme.errorContainer)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true

                            scope.launch {
                                scaleAnim.snapTo(1f)
                                scaleAnim.animateTo(
                                    scaleRate, animationSpec = tween(
                                        durationMillis = animationDuration.toInt(),
                                        easing = LinearEasing
                                    )
                                )
                            }

                            // Vibrate progressively
                            scope.launch {
                                // Create a waveform that increases in intensity
                                val steps = 20 // Number of intensity steps
                                val stepDuration = animationDuration / steps

                                // Create timing and amplitude arrays
                                val timings = LongArray(steps) { stepDuration }
                                val amplitudes = IntArray(steps) { i ->
                                    val progress = (i.toFloat() / (steps - 1)).coerceIn(0f, 1f)
                                    (progress * 254 + 1).toInt() // Intensity from 1 to 255
                                }

                                Log.d("VIBRATION", "Starting continuous increasing vibration")

                                // Create a single waveform vibration that increases in intensity
                                vibrator.defaultVibrator.vibrate(
                                    VibrationEffect.createWaveform(
                                        timings,
                                        amplitudes,
                                        -1
                                    )
                                )
                            }

                            val success = try {
                                withTimeoutOrNull(animationDuration) {
                                    awaitRelease()
                                } == null
                            } catch (_: Exception) {
                                false
                            }

                            isPressed = false
                            vibrator.defaultVibrator.cancel()

                            if (success) {
                                onSosTriggered()
                            } else {
                                scope.launch {
                                    scaleAnim.animateTo(
                                        1f, animationSpec = tween(durationMillis = 300)
                                    )
                                }
                                Toast.makeText(context, "Hold for 5 seconds", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })
                }
        ) {}
        Icon(
            imageVector = Icons.Filled.Sos,
            contentDescription = "SOS Icon",
            modifier = Modifier
                .padding(2.dp),
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SosButtonPreview() {
    OnTrekTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            SosButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onSosTriggered = {},
                onPressStateChanged = {}
            )
        }
    }
}
