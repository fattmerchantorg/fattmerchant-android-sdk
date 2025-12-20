package com.staxpayments.sample.ui.components

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.staxpayments.R
import com.staxpayments.sample.ui.theme.Gray100
import com.staxpayments.sample.ui.theme.Gray500
import com.staxpayments.sample.ui.theme.Purple50
import com.staxpayments.sample.ui.theme.Purple500
import com.staxpayments.sample.ui.theme.Purple700
import com.staxpayments.sample.ui.theme.StaxAndroidSDKTheme
import com.staxpayments.sample.ui.theme.StaxBlack

/**
 * Tap to Pay prompt screen that displays NFC payment instructions.
 * 
 * This component shows:
 * - Optional status/error/guide message
 * - A circular "Tap on the back" prompt with NFC icon and breathing animation
 * - Accepted payment card logos (Visa, Mastercard, Discover, Amex, JCB)
 * - Total payment amount with breakdown
 * - Cancel button
 * 
 * Supports both Light and Dark themes matching the Figma design.
 *
 * @param amount The total payment amount (e.g., "28.00")
 * @param subtotal The subtotal before tip (e.g., "25.00")
 * @param tip The tip amount (e.g., "3.00")
 * @param statusMessage Optional status/error/guide message to display above the circle (e.g., "Hold for a little longer", "No Card Detected", "An error occurred, try again")
 * @param onCancel Callback when cancel button is clicked
 * @param modifier Optional modifier for the component
 */
@Composable
fun TapToPayPrompt(
    amount: String,
    subtotal: String,
    tip: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    statusMessage: String? = null
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    // Theme colors
    val backgroundColor = if (isDarkTheme) StaxBlack else Color.White
    val textColor = if (isDarkTheme) Color.White else StaxBlack
    val circleColor = if (isDarkTheme) Purple700 else Purple50
    val cancelButtonBg = if (isDarkTheme) StaxBlack else Gray100
    val cancelButtonBorder = if (isDarkTheme) Color.White else Color.Transparent
    val cancelIconColor = if (isDarkTheme) Color.White else Gray500
    
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
                            color = textColor,
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
                    // Circular NFC prompt with breathing animation
                    PulsingCirclePrompt(
                        isDarkTheme = isDarkTheme,
                        circleColor = circleColor,
                        textColor = textColor
                    )
                    
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
    StaxAndroidSDKTheme(darkTheme = false) {
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
    StaxAndroidSDKTheme(darkTheme = true) {
        TapToPayPrompt(
            amount = "28.00",
            subtotal = "25.00",
            tip = "3.00",
            onCancel = {}
        )
    }
}
