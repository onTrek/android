package com.ontrek.wear.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import com.ontrek.wear.R
import com.ontrek.wear.data.TokenViewModel

@Composable
fun Login(modifier: Modifier = Modifier, tokenViewModel : TokenViewModel = viewModel(factory = TokenViewModel.Factory)) {
    Box(
        modifier
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "In order to login, please open the OnTrek app on your phone.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                //Only for debug purposes
                OutlinedButton(
                    onClick = {
                        tokenViewModel.saveToken("041da16a-dcda-4d43-947b-2194524ee114") //fuck it we ball
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                ) {
                    Text("Skip Login")
                }
            }
        }
    }
}

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    appName: String = "OnTrek",           // Optional app name
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                //da aggiungere poi il nostro logo
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = appName,
            fontSize = 20.sp,
        )
    }
}