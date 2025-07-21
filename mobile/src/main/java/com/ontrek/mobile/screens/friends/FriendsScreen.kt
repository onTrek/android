package com.ontrek.mobile.screens.friends

import android.widget.Toast
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.screens.friends.tabs.FriendsTab
import com.ontrek.mobile.screens.friends.tabs.RequestsTab
import com.ontrek.mobile.screens.friends.tabs.SearchTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    token: String,
    navController: NavHostController
) {
    val viewModel: FriendsViewModel = viewModel()
    val tabs = listOf("Friends", "Search", "Requests")
    val msgToast by viewModel.msgToast.collectAsState()
    val context = LocalContext.current
    val requests by viewModel.requestsState.collectAsState()
    val charge by viewModel.isCharge.collectAsState()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(charge) {
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
            .fillMaxSize()
            .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical),
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
                selectedTabIndex = pagerState.currentPage
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> FriendsTab(viewModel, token)
                    1 -> SearchTab(viewModel, token)
                    2 -> RequestsTab(viewModel, token)
                }
            }
        }
    }
}