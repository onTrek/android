package com.ontrek.mobile.screens.friends

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.screens.friends.tabs.FriendsTab
import com.ontrek.mobile.screens.friends.tabs.RequestsTab
import com.ontrek.mobile.screens.friends.tabs.SearchTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    token: String,
    navController: NavHostController
) {
    val viewModel: FriendsViewModel = viewModel()
    val tabIndex = remember { mutableIntStateOf(0) }
    val tabs = listOf("Friends", "Search", "Requests")
    val msgToast by viewModel.msgToast.collectAsState()
    val context = LocalContext.current
    val requests by viewModel.requestsState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.loadFriends(token)
        viewModel.loadFriendRequests(token)
    }

    LaunchedEffect(msgToast) {
        if (msgToast.isNotEmpty()) {
            Toast.makeText(context, msgToast, Toast.LENGTH_SHORT).show()
            viewModel.resetMsgToast()
        }
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
                        text = {
                            val count = when (requests) {
                                is FriendsViewModel.RequestsState.Success -> {
                                    val successState = requests as FriendsViewModel.RequestsState.Success
                                    successState.requests.size
                                }
                                else -> 0
                            }

                            if (count > 0 && title == "Requests") {
                                Text("$title ($count)")
                            } else {
                                Text(title)
                            }
                        }
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