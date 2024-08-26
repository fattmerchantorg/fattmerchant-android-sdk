package com.staxpayments.sample.ui.screens


import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.staxpayments.sample.ui.components.WideButton
import com.staxpayments.sample.ui.theme.Gray50
import com.staxpayments.sample.ui.theme.Purple500
import com.staxpayments.sample.ui.theme.Purple800
import com.staxpayments.sample.ui.theme.StaxAndroidSDKTheme
import com.staxpayments.sample.viewmodel.StaxViewModel

// Only use bluetooth permissions if on Android S (12) or higher.
val bluetoothPermissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    listOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
    )
} else {
    listOf()
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    staxViewModel: StaxViewModel = viewModel()
) {
    val topAppBarColor = if (isSystemInDarkTheme()) Purple800 else Purple500
    val padding = 16.dp

    val staxUiState by staxViewModel.uiState.collectAsState()


    val locationPermissionLauncher = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val bluetoothPermissionLauncher = rememberMultiplePermissionsState(bluetoothPermissionsList) {
        // TODO: Check Permissions Results
        locationPermissionLauncher.launchPermissionRequest()
    }

    LaunchedEffect(Unit) {
        bluetoothPermissionLauncher.launchMultiplePermissionRequest()
    }

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
                .padding(horizontal = padding)
        ) {
            Spacer(modifier = Modifier.size(it.calculateTopPadding() + 16.dp))

            // Content
            Column {
                // Log View
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f)
                        .verticalScroll(rememberScrollState()),
                    text = staxUiState.logString
                )

                // Amount Text Input
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = padding),
                    value = "0.01",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Amount") }
                )

                // Buttons
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(2f)
                ) {
                    val context = LocalContext.current

                    WideButton(text = "Initialize") { staxViewModel.onInitialize() }
                    WideButton(text = "Search & Connect to Reader") { staxViewModel.onSearchAndConnectToReaders(context) }
                    WideButton(text = "Perform Sale With Reader") { staxViewModel.onPerformSaleWithReader() }
                    WideButton(text = "Perform Auth With Reader") { staxViewModel.onPerformAuthWithReader() }
                    WideButton(text = "Capture Last Auth") { staxViewModel.onCaptureLastAuth() }
                    WideButton(text = "Void Last Transaction") { staxViewModel.onVoidLastTransaction() }
                    WideButton(text = "Tokenize Card") { staxViewModel.onTokenizeCard() }
                    WideButton(text = "Get Connected Reader Details") { staxViewModel.onGetConnectedReaderDetails() }
                    WideButton(text = "Disconnect Reader") { staxViewModel.onDisconnectReader() }
                    WideButton(text = "Cancel Transaction") { staxViewModel.onCancelTransaction() }
                }
                Spacer(modifier = Modifier.size(padding))
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