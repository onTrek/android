package com.ontrek.mobile.screens.track.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailScreen(
    trackId: String,
    navController: NavHostController,
    token: String
) {
    val viewModel: TrackDetailViewModel = viewModel()
    val trackDetailState by viewModel.trackDetailState.collectAsState()
    val imageState by viewModel.imageState.collectAsState()

    LaunchedEffect(trackId) {
        viewModel.loadTrackDetails(trackId, token)
        viewModel.loadTrackImage(trackId, token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${errorState.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is TrackDetailViewModel.TrackDetailState.Success -> {
                    val track = (trackDetailState as TrackDetailViewModel.TrackDetailState.Success).track

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Titolo
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                // Data
                                TrackInfoRow(
                                    icon = Icons.Default.DateRange,
                                    label = "Date of Upload",
                                    value = viewModel.formatDate(track.upload_date)
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
                                    value = viewModel.formatDuration(track.stats.duration)
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
                                    icon = Icons.Default.Terrain,
                                    label = "Max Height",
                                    value = "${track.stats.max_altitude} m"
                                )

                                // Altitudine Minima
                                TrackInfoRow(
                                    icon = Icons.Default.Terrain,
                                    label = "Min Height",
                                    value = "${track.stats.min_altitude} m"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Sezione Immagine Traccia
                        Text(
                            text = "Image",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            when (imageState) {
                                is TrackDetailViewModel.ImageState.Loading -> {
                                    CircularProgressIndicator()
                                }
                                is TrackDetailViewModel.ImageState.Error -> {
                                    val errorState = imageState as TrackDetailViewModel.ImageState.Error
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
                                is TrackDetailViewModel.ImageState.Success -> {
                                    val imageUrl = (imageState as TrackDetailViewModel.ImageState.Success).imageUrl

                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Track Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
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

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}