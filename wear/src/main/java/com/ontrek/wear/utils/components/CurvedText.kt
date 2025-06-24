package com.ontrek.wear.utils.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.AnchorType
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.foundation.curvedRow
import androidx.wear.compose.material.curvedText

/**
 * A composable function that displays curved text at a specified anchor point.
 * @param anchor The anchor point for the curved text, ranging from 0.0 to 360 where 0.0 is 3 o'clock.
 * @param color The color of the curved text.
 * @param text The text to be displayed in a curved format.
 * @param modifier The modifier to be applied to the curved layout.
 */
@Composable
fun CurvedText(anchor: Float, color: Color, text: String, fontSize: Int = 14, modifier: Modifier = Modifier) {
    CurvedLayout(
        anchor = anchor,
        anchorType = AnchorType.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        curvedRow(
        ) {
            curvedText(
                text = text,
                style = CurvedTextStyle(
                    fontSize = fontSize.sp,
                    color = color,
                )
            )
        }
    }
}