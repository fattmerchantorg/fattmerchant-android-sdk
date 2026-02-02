package com.fattmerchant.omni.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fattmerchant.R
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.TransactionUpdate
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.ui.theme.Gray100
import com.fattmerchant.omni.ui.theme.Gray500
import com.fattmerchant.omni.ui.theme.Purple50
import com.fattmerchant.omni.ui.theme.Purple500
import com.fattmerchant.omni.ui.theme.Purple700
import com.fattmerchant.omni.ui.theme.StaxBlack
import com.fattmerchant.omni.ui.theme.StaxPaymentTheme

/**
 * Transaction state for the Tap to Pay prompt
 */
sealed class TapToPayState {
    object Idle : TapToPayState()
    data class Processing(val message: String) : TapToPayState()
    data class Success(val transaction: Transaction) : TapToPayState()
    data class Error(val message: String) : TapToPayState()
}

/**
 * Tap to Pay prompt screen that displays NFC payment instructions and handles transaction flow.
 * 
 * This component:
 * - Automatically sets up transaction update listeners
 * - Displays real-time transaction status updates
 * - Shows appropriate UI for each transaction state
 * - Handles success, error, and cancellation
 * 
 * @param amount The total payment amount (e.g., "28.00")
 * @param subtotal The subtotal before tip (e.g., "25.00")
 * @param tip The tip amount (e.g., "3.00")
 * @param transactionRequest Optional pre-built transaction request, or builds one from amount
 * @param onSuccess Callback when transaction completes successfully
 * @param onError Callback when transaction fails
 * @param onCancel Callback when user cancels
 * @param modifier Optional modifier for the component
 */
@Composable
fun TapToPayPrompt(
    amount: String,
    subtotal: String,
    tip: String,
    transactionRequest: TransactionRequest? = null,
    onSuccess: (Transaction) -> Unit = {},
    onError: (String) -> Unit = {},
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionState by remember { mutableStateOf<TapToPayState>(TapToPayState.Idle) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    
    // Set up transaction update listener
    DisposableEffect(Unit) {
        val listener = object : TransactionUpdateListener {
            override fun onTransactionUpdate(update: TransactionUpdate) {
                statusMessage = update.userFriendlyMessage ?: formatTransactionUpdate(update.value)
                
                // Update state based on transaction progress
                when (update.value) {
                    "Transaction Started" -> {
                        transactionState = TapToPayState.Processing("Starting transaction...")
                    }
                    "Prompt Insert Card", "Prompt Swipe Card" -> {
                        transactionState = TapToPayState.Processing("Waiting for card...")
                    }
                    "Card Inserted", "Card Swiped" -> {
                        transactionState = TapToPayState.Processing("Card detected")
                    }
                    "Reading Card" -> {
                        transactionState = TapToPayState.Processing("Reading card...")
                    }
                    "Authorizing" -> {
                        transactionState = TapToPayState.Processing("Authorizing payment...")
                    }
                    "Card Swipe Error", "Card Read Error" -> {
                        statusMessage = "Card read failed. Please try again."
                    }
                }
            }
        }
        
        Omni.shared()?.transactionUpdateListener = listener
        
        onDispose {
            // Clean up listener when component is removed
            if (Omni.shared()?.transactionUpdateListener === listener) {
                Omni.shared()?.transactionUpdateListener = null
            }
        }
    }
    
    // Start transaction automatically when component appears
    LaunchedEffect(Unit) {
        val request = transactionRequest ?: TransactionRequest(
            Amount(amount.toDoubleOrNull() ?: 0.0)
        ).apply {
            // Add any additional transaction metadata here
            memo = "Tap to Pay transaction"
        }
        
        Omni.shared()?.takeMobileReaderTransaction(
            request = request,
            completion = { transaction ->
                if (transaction.success == true) {
                    transactionState = TapToPayState.Success(transaction)
                    statusMessage = "Payment approved!"
                    onSuccess(transaction)
                } else {
                    val errorMsg = transaction.message ?: "Transaction declined"
                    transactionState = TapToPayState.Error(errorMsg)
                    statusMessage = errorMsg
                    onError(errorMsg)
                }
            },
            error = { exception ->
                val errorMsg = exception.message ?: "Transaction failed"
                transactionState = TapToPayState.Error(errorMsg)
                statusMessage = errorMsg
                onError(errorMsg)
            }
        )
    }
    
    TapToPayPromptContent(
        amount = amount,
        subtotal = subtotal,
        tip = tip,
        transactionState = transactionState,
        statusMessage = statusMessage,
        onCancel = onCancel,
        modifier = modifier
    )
}

/**
 * Formats transaction update values into user-friendly messages
 */
private fun formatTransactionUpdate(value: String): String {
    return when (value) {
        "Transaction Started" -> "Starting payment..."
        "Prompt Insert Card", "Prompt Swipe Card" -> "Hold card near device"
        "Card Inserted", "Card Swiped" -> "Card detected"
        "Reading Card" -> "Reading card..."
        "Authorizing" -> "Authorizing payment..."
        "Authorized" -> "Payment approved!"
        "Declined" -> "Payment declined"
        "Card Swipe Error", "Card Read Error" -> "Card read failed"
        "Transaction Error" -> "An error occurred"
        else -> value
    }
}

/**
 * Content-only version of TapToPayPrompt for preview/testing
 */
@Composable
private fun TapToPayPromptContent(
    amount: String,
    subtotal: String,
    tip: String,
    transactionState: TapToPayState,
    statusMessage: String?,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // Theme colors
    val backgroundColor = if (isDarkTheme) StaxBlack else Color.White
    val textColor = if (isDarkTheme) Color.White else StaxBlack
    val circleColor = if (isDarkTheme) Purple700 else Purple50
    val cancelButtonBg = if (isDarkTheme) StaxBlack else Gray100
    val cancelButtonBorder = if (isDarkTheme) Color.White else Color.Transparent
    val cancelIconColor = if (isDarkTheme) Color.White else Gray500
    
    // Determine if we should show processing indicator
    val isProcessing = transactionState is TapToPayState.Processing
    val isSuccess = transactionState is TapToPayState.Success
    val isError = transactionState is TapToPayState.Error
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 89.dp, bottom = 40.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status message area
                if (statusMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusMessage,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            color = when {
                                isSuccess -> Color(0xFF4CAF50)  // Green for success
                                isError -> Color(0xFFE53935)    // Red for error
                                else -> textColor
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Content section with circle, cards, and amount
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(40.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    // Show appropriate UI based on transaction state
                    when (transactionState) {
                        is TapToPayState.Success -> {
                            // Show success icon
                            Box(
                                modifier = Modifier.size(194.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓",
                                    fontSize = 80.sp,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        is TapToPayState.Error -> {
                            // Show error icon
                            Box(
                                modifier = Modifier.size(194.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✗",
                                    fontSize = 80.sp,
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        else -> {
                            // Show normal NFC prompt with optional processing indicator
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                PulsingCirclePrompt(
                                    isDarkTheme = isDarkTheme,
                                    circleColor = circleColor,
                                    textColor = textColor
                                )
                                
                                // Show spinner when processing
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(240.dp),
                                        color = Purple500,
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        }
                    }
                    
                    // Payment card icons
                    PaymentCardIcons(isDarkTheme = isDarkTheme)
                    
                    // Amount display with breakdown
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$$amount",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Amount: $$subtotal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tip: $$tip",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Cancel button
            CancelButton(
                onCancel = onCancel,
                backgroundColor = cancelButtonBg,
                borderColor = cancelButtonBorder,
                iconColor = cancelIconColor
            )
        }
    }
}

@Composable
private fun PulsingCirclePrompt(
    isDarkTheme: Boolean,
    circleColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier.size(360.dp),
        contentAlignment = Alignment.Center
    ) {
        // Breathing glow layer
        Box(
            modifier = Modifier
                .size(340.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Purple500.copy(alpha = 0.5f),
                            Purple500.copy(alpha = 0.7f),
                            Purple500.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Static border ring
        Box(
            modifier = Modifier
                .size(300.dp)
                .border(3.dp, Purple500.copy(alpha = 0.7f), CircleShape)
        )
        
        // Main circle
        Box(
            modifier = Modifier
                .size(300.dp)
                .shadow(10.dp, CircleShape)
                .background(circleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(60.dp)
            ) {
                Text(
                    text = "Tap on the back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                
                NFCIcon(
                    modifier = Modifier.size(width = 135.dp, height = 80.dp),
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
private fun NFCIcon(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    Image(
        painter = painterResource(R.drawable.ic_nfc_contactless),
        contentDescription = "NFC Contactless",
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(
            if (isDarkTheme) Color.White else StaxBlack
        )
    )
}

@Composable
private fun PaymentCardIcons(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Visa
        PaymentCardIcon(iconRes = R.drawable.ic_visa, isDarkTheme = isDarkTheme)
        // Mastercard
        PaymentCardIcon(iconRes = R.drawable.ic_mastercard, isDarkTheme = isDarkTheme)
        // Discover
        PaymentCardIcon(iconRes = R.drawable.ic_discover, isDarkTheme = isDarkTheme)
        // Amex
        PaymentCardIcon(iconRes = R.drawable.ic_amex, isDarkTheme = isDarkTheme)
        // JCB
        PaymentCardIcon(iconRes = R.drawable.ic_jcb, isDarkTheme = isDarkTheme)
    }
}

@Composable
private fun PaymentCardIcon(
    iconRes: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(34.dp)
            .height(24.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFD9D9D9),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.padding(4.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun CancelButton(
    onCancel: () -> Unit,
    backgroundColor: Color,
    borderColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(50.dp)
            .background(backgroundColor, CircleShape)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.dp, borderColor, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable { onCancel() },
        contentAlignment = Alignment.Center
    ) {
        // X icon
        Text(
            text = "✕",
            fontSize = 22.sp,
            color = iconColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun TapToPayPromptLightPreview() {
    StaxPaymentTheme(darkTheme = false) {
        TapToPayPrompt(
            amount = "28.00",
            subtotal = "25.00",
            tip = "3.00",
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
private fun TapToPayPromptDarkPreview() {
    StaxPaymentTheme(darkTheme = true) {
        TapToPayPrompt(
            amount = "28.00",
            subtotal = "25.00",
            tip = "3.00",
            onCancel = {}
        )
    }
}
