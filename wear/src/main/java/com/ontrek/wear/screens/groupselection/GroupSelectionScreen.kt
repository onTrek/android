package com.ontrek.wear.screens.groupselection

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.ScrollIndicatorColors
import androidx.wear.compose.material3.Text
import com.ontrek.wear.data.DatabaseProvider
import com.ontrek.wear.utils.components.Loading
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GroupSelectionScreen(
    navController: NavHostController = rememberNavController(),
) {
    val groupSelectionViewModel = viewModel<GroupSelectionViewModel>(
        factory = GroupSelectionViewModel.Factory(DatabaseProvider.getDatabase(LocalContext.current.applicationContext))
    )

    val isLoading by groupSelectionViewModel.isLoading.collectAsStateWithLifecycle()
    val fetchError by groupSelectionViewModel.fetchError.collectAsStateWithLifecycle()
    val groups by groupSelectionViewModel.groupListState.collectAsStateWithLifecycle()


    val listState = rememberScalingLazyListState()

    LaunchedEffect(Unit) {
        groupSelectionViewModel.fetchGroupsList()
    }


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

        if (isLoading) {
            Loading(modifier = Modifier.fillMaxSize())
        } else if (groups.isEmpty() && fetchError.isNullOrEmpty()) {
            EmptyList()
        } else ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = listState),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(top = 15.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    text = "Hiking groups",
                )
            }
            items(groups) { group ->
                Button(
                    onClick = {
                        Log.d("GroupSelectionScreen", "Selected group: ${group.description}")
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                ) {
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            item {
                IconButton(
                    onClick = {
                        Log.d("GroupSelectionScreen", "Refresh groups")
                        groupSelectionViewModel.fetchGroupsList()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh groups",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyList() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = "No groups available",
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleSmall,
            text = "No groups available.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = "Please join a group to see it here or create a new one.",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}