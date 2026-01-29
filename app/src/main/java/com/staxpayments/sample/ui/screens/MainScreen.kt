package com.staxpayments.sample.ui.screens


import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.staxpayments.sample.state.TapToPayMode
// Import TapToPayPrompt from the SDK - this demonstrates proper SDK usage
import com.fattmerchant.omni.ui.TapToPayPrompt
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
    activity: ComponentActivity? = null,
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
    
    // Set the activity in the ViewModel for NFC operations
    LaunchedEffect(activity) {
        staxViewModel.setActivity(activity)
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
                // Tap to Pay Configuration Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(padding)) {
                        Text(
                            text = "Tap to Pay Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        
                        // Radio buttons for Tap to Pay modes
                        TapToPayModeOption(
                            text = "Disabled (External readers only)",
                            selected = staxUiState.tapToPayMode == TapToPayMode.DISABLED,
                            onClick = { staxViewModel.setTapToPayMode(TapToPayMode.DISABLED) }
                        )
                        TapToPayModeOption(
                            text = "Tap to Pay Only (NFC)",
                            selected = staxUiState.tapToPayMode == TapToPayMode.TAP_TO_PAY_ONLY,
                            onClick = { staxViewModel.setTapToPayMode(TapToPayMode.TAP_TO_PAY_ONLY) }
                        )
                        TapToPayModeOption(
                            text = "Hybrid (NFC + External)",
                            selected = staxUiState.tapToPayMode == TapToPayMode.HYBRID,
                            onClick = { staxViewModel.setTapToPayMode(TapToPayMode.HYBRID) }
                        )
                    }
                }
                
                // Tap Reader Connection Status
                if (staxUiState.tapToPayMode != TapToPayMode.DISABLED) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (staxUiState.tapReaderConnected) 
                                MaterialTheme.colorScheme.tertiaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            modifier = Modifier.padding(padding),
                            text = if (staxUiState.tapReaderConnected) 
                                "✓ Tap Reader Connected" 
                            else 
                                "⚠ Tap Reader Not Connected",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Transaction Status
                if (staxUiState.transactionStatus.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            modifier = Modifier.padding(padding),
                            text = staxUiState.transactionStatus,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
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
                    WideButton(text = "Initialize w/Ephemeral Token") { staxViewModel.onEphemeralInitialize() }
                    
                    // Tap to Pay Connection Button
                    if (staxUiState.tapToPayMode != TapToPayMode.DISABLED) {
                        WideButton(
                            text = if (staxUiState.tapReaderConnected) "Reconnect to Tap Reader" else "Connect to Tap Reader",
                            onClick = { staxViewModel.connectToTapReader() }
                        )
                    }
                    
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
            // Close the content Column
        }
    }
    
    // Full-screen Tap to Pay Prompt Modal with animation
    // 
    // This demonstrates the recommended SDK usage pattern:
    // 1. Import: com.fattmerchant.omni.ui.TapToPayPrompt
    // 2. Show the component with transaction details
    // 3. It automatically handles the entire transaction flow
    // 4. You just provide success/error/cancel callbacks
    //
    AnimatedVisibility(
        visible = staxUiState.showTapToPayPrompt,
        enter = fadeIn(animationSpec = tween(300)) + 
                slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 2 }),
        exit = fadeOut(animationSpec = tween(250)) + 
               slideOutVertically(animationSpec = tween(300), targetOffsetY = { it / 2 })
    ) {
        // TapToPayPrompt from SDK - handles transaction automatically
        TapToPayPrompt(
            amount = staxUiState.transactionAmount,
            subtotal = staxUiState.transactionSubtotal,
            tip = staxUiState.transactionTip,
            transactionRequest = staxUiState.transactionRequest,
            onSuccess = { transaction -> staxViewModel.onTransactionSuccess(transaction) },
            onError = { errorMessage -> staxViewModel.onTransactionError(errorMessage) },
            onCancel = { staxViewModel.dismissTapToPayPrompt() }
        )
    }
}

@Composable
private fun TapToPayModeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
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