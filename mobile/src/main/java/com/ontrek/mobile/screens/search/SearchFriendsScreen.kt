package com.ontrek.mobile.screens.search

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ErrorComponent
import com.ontrek.mobile.utils.components.ProfileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFriendsScreen(
    navController: NavHostController
) {
    val viewModel: SearchFriendsViewModel = viewModel()
    val msgToast by viewModel.msgToast.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var query by remember { mutableStateOf("") }
    val searchState by viewModel.searchState.collectAsState()

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
                        text = "Search",
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = { BottomNavBar(navController) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.search(query)
                },
                label = { Text("Search user") },
                modifier = Modifier.fillMaxWidth()
            )

            when (val state = searchState) {
                is SearchFriendsViewModel.SearchState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is SearchFriendsViewModel.SearchState.Error -> {
                    ErrorComponent(
                        errorMsg = state.message
                    )
                }
                is SearchFriendsViewModel.SearchState.Success -> {
                    LazyColumn {
                        items(state.users) { user ->
                            ProfileItem(
                                user = user,
                                onClick = { viewModel.sendRequest(user.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                SearchFriendsViewModel.SearchState.Empty -> {
                    EmptyComponent(
                        title = "No users found",
                        description = "Try searching with a different keyword",
                        icon = Icons.Default.Search,
                    )
                }
            }
        }
    }
}