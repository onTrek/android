package com.ontrek.mobile.screens.track.detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest.Builder
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.DeleteConfirmationDialog
import com.ontrek.mobile.utils.components.ErrorComponent
import com.ontrek.shared.utils.formatDate
import com.ontrek.shared.utils.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailScreen(
    trackId: Int,
    currentUser: String,
    navController: NavHostController,
) {
    val viewModel: TrackDetailViewModel = viewModel()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val trackDetailState by viewModel.trackDetailState.collectAsState()
    val imageState by viewModel.imageState.collectAsState()
    val msgToast by viewModel.msgToast.collectAsState()
    val current = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val minScale = 1f
    val maxScale = 3f
    val scale = remember { mutableFloatStateOf(1f) }
    val offset = remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale.floatValue * zoomChange).coerceIn(minScale, maxScale)
        scale.floatValue = newScale

        if (newScale > 1.5f) {
            offset.value += panChange
        }
    }

    LaunchedEffect(trackId) {
        viewModel.loadTrackDetails(trackId)
        viewModel.loadTrackImage(trackId)
    }

    LaunchedEffect(msgToast) {
        if (msgToast.isNotEmpty()) {
            Toast.makeText(current, msgToast, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (trackDetailState) {
                is TrackDetailViewModel.TrackDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is TrackDetailViewModel.TrackDetailState.Error -> {
                    val errorState = trackDetailState as TrackDetailViewModel.TrackDetailState.Error
                    ErrorComponent(
                        errorMsg = errorState.message,
                    )
                }

                is TrackDetailViewModel.TrackDetailState.Success -> {
                    val track =
                        (trackDetailState as TrackDetailViewModel.TrackDetailState.Success).track

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (showDeleteConfirmation) {
                            DeleteConfirmationDialog(
                                title = "Delete Track",
                                onDismiss = { showDeleteConfirmation = false },
                                onConfirm = {
                                    viewModel.deleteTrack(track.id,
                                        onSuccess = {
                                            navController.navigateUp()
                                        },
                                    )
                                }
                            )
                        }

                        // Titolo
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        do {
                                            val event = awaitPointerEvent()
                                        } while (event.changes.any { it.pressed })

                                        scale.floatValue = 1f
                                        offset.value = Offset.Zero
                                    }
                                }
                                .transformable(state = transformableState),
                            contentAlignment = Alignment.Center
                        ) {
                            when (imageState) {
                                is TrackDetailViewModel.ImageState.Loading -> {
                                    CircularProgressIndicator()
                                }

                                is TrackDetailViewModel.ImageState.Error -> {
                                    val errorState =
                                        imageState as TrackDetailViewModel.ImageState.Error
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.BrokenImage,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "Impossible to upload the image: ${errorState.message}",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                is TrackDetailViewModel.ImageState.SuccessBinary -> {
                                    val imageBytes =
                                        (imageState as TrackDetailViewModel.ImageState.SuccessBinary).imageBytes
                                    AsyncImage(
                                        model = Builder(LocalContext.current)
                                            .data(imageBytes)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Track Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(
                                                scaleX = scale.floatValue,
                                                scaleY = scale.floatValue,
                                                translationX = offset.value.x,
                                                translationY = offset.value.y
                                            )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Card con informazioni della traccia
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Sezione Informazioni Generali
                                Text(
                                    text = "Track Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = DividerDefaults.Thickness,
                                    color = DividerDefaults.color
                                )

                                // Data
                                TrackInfoRow(
                                    icon = Icons.Default.DateRange,
                                    label = "Upload Date",
                                    value = formatDate(track.upload_date)
                                )

                                // Distanza
                                TrackInfoRow(
                                    icon = Icons.Default.Straighten,
                                    label = "Distance",
                                    value = "${track.stats.km} km"
                                )

                                // Durata
                                TrackInfoRow(
                                    icon = Icons.Default.Timer,
                                    label = "Duration",
                                    value = formatDuration(track.stats.duration)
                                )

                                // Sezione Elevazione
                                Text(
                                    text = "Elevation Stats",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 16.dp)
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = DividerDefaults.Thickness,
                                    color = DividerDefaults.color
                                )

                                // Salita
                                TrackInfoRow(
                                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                                    label = "Ascent",
                                    value = "${track.stats.ascent} m"
                                )

                                // Discesa
                                TrackInfoRow(
                                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                                    label = "Descent",
                                    value = "${track.stats.descent} m"
                                )

                                // Altitudine Massima
                                TrackInfoRow(
                                    icon = Icons.Default.KeyboardDoubleArrowUp,
                                    label = "Max Altitude",
                                    value = "${track.stats.max_altitude} m"
                                )

                                // Altitudine Minima
                                TrackInfoRow(
                                    icon = Icons.Default.KeyboardDoubleArrowDown,
                                    label = "Min Altitude",
                                    value = "${track.stats.min_altitude} m"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottone per eliminazione traccia
                        if (track.owner == currentUser)  {
                            TextButton(
                                onClick = { showDeleteConfirmation = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Track",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delete Track",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}