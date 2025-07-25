package com.ontrek.mobile.screens.friends

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.screens.friends.tabs.FriendsTab
import com.ontrek.mobile.screens.friends.tabs.RequestsTab
import com.ontrek.mobile.screens.friends.tabs.RequestsSent
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.SearchUsersDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    token: String,
    navController: NavHostController
) {
    val viewModel: FriendsViewModel = viewModel()
    val tabs = listOf("Friends", "Requests Sent", "Requests")
    val msgToast by viewModel.msgToast.collectAsState()
    val context = LocalContext.current
    val requests by viewModel.requestsState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showSearchUsers by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFriends(token)
        viewModel.loadFriendRequests(token)
        viewModel.loadSentFriendRequests(token)
    }

    if (msgToast.isNotEmpty()) {
        LaunchedEffect(msgToast) {
            Toast.makeText(context, msgToast, Toast.LENGTH_SHORT).show()
            viewModel.clearMsgToast()
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
                        text = "Friends",
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (pagerState.currentPage == 1) {
                FloatingActionButton(
                    onClick = {
                       showSearchUsers = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAddAlt1,
                        contentDescription = "Add Track",
                    )
                }
            }
        },
        bottomBar = { BottomNavBar(navController) },
    ) { paddingValues ->
        if (showSearchUsers) {
            SearchUsersDialog(
                onDismiss = { showSearchUsers = false },
                onUserSelected = { user -> viewModel.sendFriendRequest(user, token) },
                token = token
            )
        }

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
                                    val successState =
                                        requests as FriendsViewModel.RequestsState.Success
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
                    1 -> RequestsSent(viewModel)
                    2 -> RequestsTab(viewModel, token)
                }
            }
        }
    }
}