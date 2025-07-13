package com.ontrek.mobile.screens.friends

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ontrek.mobile.utils.components.BottomNavBar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    token: String,
    navController: NavHostController
) {
    val viewModel: FriendsViewModel = viewModel()
    val tabIndex = remember { mutableIntStateOf(0) }
    val tabs = listOf("Amici", "Cerca", "Richieste")

    LaunchedEffect(Unit) {
        viewModel.loadFriends(token)
        viewModel.loadFriendRequests(token)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Friends",
                    )
                },
            )
        },
        bottomBar = { BottomNavBar(navController) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = tabIndex.intValue
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex.intValue == index,
                        onClick = { tabIndex.intValue = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (tabIndex.intValue) {
                0 -> FriendsTab(viewModel, token)
                1 -> SearchTab(viewModel, token)
                2 -> RequestsTab(viewModel, token)
            }
        }
    }
}