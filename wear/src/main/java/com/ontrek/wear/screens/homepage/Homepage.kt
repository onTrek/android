package com.ontrek.wear.screens.homepage

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.ScrollIndicatorColors
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.theme.OnTrekTheme

@Composable
fun Homepage(
    onNavigateToTracks: () -> Unit,
    onNavigateToHikes: () -> Unit,
    onLogout: () -> Unit,
) {
    val listState = rememberScalingLazyListState()
    ScreenScaffold(
        scrollState = listState,
        scrollIndicator = {
            ScrollIndicator(
                state = listState,
                colors = ScrollIndicatorColors(
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(start = 8.dp)
            )
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = listState),
        ) {
            item {
                Text(
                    text = "OnTrek",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                )
            }

            item {
                TitleCard(
                    onClick = onNavigateToHikes,
                    title = {
                        Icon(
                            imageVector = Icons.Outlined.Hiking,
                            contentDescription = "Hikes Icon",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Hikes",
                            textAlign = TextAlign.Center,
                        )
                    },
                ) {
                    Text(
                        text = "",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item {
                TitleCard(
                    onClick = onNavigateToTracks,
                    title = {
                        Icon(
                            imageVector = Icons.Outlined.Landscape,
                            contentDescription = "Hikes Icon",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Explore")
                    }
                ) {
                    Text(
                        text = "View Tracks",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item {
                Button(
                    onClick = onLogout
                ) {
                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HomepagePreview() {
    OnTrekTheme {
        Homepage(onNavigateToTracks = {}, onNavigateToHikes = {}, onLogout = {})
    }
}
