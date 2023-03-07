package com.staxpayments.sample.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.staxpayments.sample.ui.components.WideButton
import com.staxpayments.sample.ui.theme.Gray50
import com.staxpayments.sample.ui.theme.Purple500
import com.staxpayments.sample.ui.theme.Purple800
import com.staxpayments.sample.ui.theme.StaxAndroidSDKTheme
import com.staxpayments.sample.viewmodel.StaxViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    staxViewModel: StaxViewModel = viewModel()
) {
    val topAppBarColor = if (isSystemInDarkTheme()) Purple800 else Purple500

    val horizontalPadding = 20.dp

    Scaffold(
        topBar = { TopAppBar(
            title = { Text(
                text = "Stax SDK Sample",
                color = Gray50,
                fontWeight = FontWeight.Bold,
            ) },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = topAppBarColor,
            )
        ) }
    ) {
        // Main Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        ) {
            Spacer(modifier = Modifier.size(it.calculateTopPadding() + 8.dp))

            // Content
            Column {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red)
                    .weight(3f)
                )

                Spacer(modifier = Modifier.size(16.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = "0.01",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Amount") }
                )
                Spacer(modifier = Modifier.size(16.dp))

                // Buttons
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(2f)
                ) {
                    WideButton(text = "Initialize") { staxViewModel.onInitialize() }
                    WideButton(text = "Search for Reader") { staxViewModel.onSearchForReaders() }
                    WideButton(text = "Connect to Selected Reader") { staxViewModel.onConnectToSelectedReader() }
                    WideButton(text = "Perform Sale With Reader") { staxViewModel.onPerformSaleWithReader() }
                    WideButton(text = "Perform Auth With Reader") { staxViewModel.onPerformAuthWithReader() }
                    WideButton(text = "Capture Last Auth") { staxViewModel.onCaptureLastAuth() }
                    WideButton(text = "Void Last Auth") { staxViewModel.onVoidLastAuth() }
                    WideButton(text = "Void Last Transaction") { staxViewModel.onVoidLastTransaction() }
                    WideButton(text = "Tokenize Card") { staxViewModel.onTokenizeCard() }
                    WideButton(text = "Get Connected Reader Details") { staxViewModel.onGetConnectedReaderDetails() }
                    WideButton(text = "Disconnect Reader") { staxViewModel.onDisconnectReader() }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun MainScreenPreviewLight() {
    StaxAndroidSDKTheme {
        MainScreen()
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, device = Devices.NEXUS_5)
@Composable
private fun MainScreenPreviewDark() {
    StaxAndroidSDKTheme {
        MainScreen()
    }
}