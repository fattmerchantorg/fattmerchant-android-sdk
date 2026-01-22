# UI/UX Guidelines

Best practices for creating excellent payment experiences with Tap to Pay.

---

## Table of Contents

- [Design Principles](#design-principles)
- [Transaction Flow UI](#transaction-flow-ui)
- [Card Prompts](#card-prompts)
- [Progress Indicators](#progress-indicators)
- [Success & Error States](#success--error-states)
- [Payment Method Selection](#payment-method-selection)
- [Accessibility](#accessibility)
- [Animations](#animations)

---

## Design Principles

### Keep It Simple

- **One action per screen** - Don't overwhelm users with choices during payment
- **Clear call-to-action** - Make it obvious what the user should do next
- **Minimal input** - Pre-fill data when possible, only ask for essentials

### Provide Feedback

- **Immediate response** - React to user actions within 100ms
- **Progress indication** - Show what's happening during processing
- **Clear outcomes** - Make success and failure states obvious

### Build Trust

- **Security messaging** - Remind users their payment is secure
- **Error handling** - Explain what went wrong and how to fix it
- **Confirmation** - Show transaction details before and after processing

---

## Transaction Flow UI

### Complete Transaction Screen Example

```kotlin
@Composable
fun PaymentScreen(
    amount: Double,
    viewModel: PaymentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
                navigationIcon = {
                    IconButton(onClick = { /* go back */ }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Amount display
            AmountDisplay(amount = amount)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Transaction state UI
            when (val state = uiState) {
                is PaymentUiState.Idle -> {
                    ReadyToPayUI(
                        amount = amount,
                        onStartPayment = { viewModel.startPayment(amount) }
                    )
                }
                is PaymentUiState.Processing -> {
                    ProcessingUI(update = state.update)
                }
                is PaymentUiState.Success -> {
                    SuccessUI(transaction = state.transaction)
                }
                is PaymentUiState.Error -> {
                    ErrorUI(
                        message = state.message,
                        onRetry = { viewModel.startPayment(amount) }
                    )
                }
            }
        }
    }
}

@Composable
fun AmountDisplay(amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Amount",
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
            Text(
                text = "$${"%.2f".format(amount)}",
                style = MaterialTheme.typography.h3,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

---

## Card Prompts

### Tap Card Prompt

```kotlin
@Composable
fun TapCardPrompt() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        // Animated NFC icon
        NfcIcon(
            modifier = Modifier.size(120.dp),
            animated = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Hold card near device",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Position card at the top of your device",
            style = MaterialTheme.typography.body2,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NfcIcon(modifier: Modifier = Modifier, animated: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Icon(
        painter = painterResource(R.drawable.ic_nfc),
        contentDescription = "NFC",
        modifier = modifier,
        tint = MaterialTheme.colors.primary.copy(
            alpha = if (animated) alpha else 1f
        )
    )
}
```

### External Reader Prompt

```kotlin
@Composable
fun ExternalReaderPrompt(readerName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_card_reader),
            contentDescription = "Card Reader",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colors.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Insert, tap, or swipe card",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Using $readerName",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}
```

---

## Progress Indicators

### Transaction Progress

```kotlin
@Composable
fun TransactionProgress(update: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = formatUpdateMessage(update),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
    }
}

fun formatUpdateMessage(update: String): String {
    return when (update) {
        "Transaction Started" -> "Starting payment..."
        "Prompt Insert Card", "Prompt Swipe Card" -> "Waiting for card..."
        "Card Inserted", "Card Swiped" -> "Card detected"
        "Reading Card" -> "Reading card..."
        "Authorizing" -> "Authorizing payment..."
        "Authorized" -> "Payment approved!"
        else -> update
    }
}
```

### Step-by-Step Progress

```kotlin
@Composable
fun TransactionSteps(currentStep: TransactionStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StepIndicator(
            step = 1,
            label = "Tap Card",
            isActive = currentStep.ordinal >= 0,
            isComplete = currentStep.ordinal > 0
        )
        
        StepConnector(isActive = currentStep.ordinal > 0)
        
        StepIndicator(
            step = 2,
            label = "Processing",
            isActive = currentStep.ordinal >= 1,
            isComplete = currentStep.ordinal > 1
        )
        
        StepConnector(isActive = currentStep.ordinal > 1)
        
        StepIndicator(
            step = 3,
            label = "Complete",
            isActive = currentStep.ordinal >= 2,
            isComplete = currentStep.ordinal > 2
        )
    }
}

@Composable
fun StepIndicator(
    step: Int,
    label: String,
    isActive: Boolean,
    isComplete: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = when {
                        isComplete -> Color.Green
                        isActive -> MaterialTheme.colors.primary
                        else -> Color.LightGray
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = Color.White
                )
            } else {
                Text(
                    text = step.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = if (isActive) Color.Black else Color.Gray
        )
    }
}

@Composable
fun StepConnector(isActive: Boolean) {
    Divider(
        modifier = Modifier
            .width(40.dp)
            .padding(top = 20.dp),
        color = if (isActive) MaterialTheme.colors.primary else Color.LightGray,
        thickness = 2.dp
    )
}

enum class TransactionStep {
    TAP_CARD, PROCESSING, COMPLETE
}
```

---

## Success & Error States

### Success Screen

```kotlin
@Composable
fun SuccessUI(transaction: Transaction) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        // Success animation
        LottieAnimation(
            composition = rememberLottieComposition(R.raw.success_checkmark),
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Payment Successful",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            color = Color.Green
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Transaction details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TransactionDetailRow("Amount", "$${"%.2f".format(transaction.total)}")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                TransactionDetailRow("Card", "${transaction.cardType} •••• ${transaction.lastFour}")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                TransactionDetailRow("Auth Code", transaction.authCode ?: "N/A")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                TransactionDetailRow("Date", formatDate(transaction.created_at))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = { /* send receipt */ }) {
                Icon(Icons.Default.Email, "Email")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Email Receipt")
            }
            
            Button(onClick = { /* done */ }) {
                Text("Done")
            }
        }
    }
}

@Composable
fun TransactionDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium
        )
    }
}
```

### Error Screen

```kotlin
@Composable
fun ErrorUI(message: String, onRetry: () -> Unit, onCancel: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(120.dp),
            tint = Color.Red
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Payment Failed",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = parseErrorMessage(message),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}

fun parseErrorMessage(error: String): String {
    return when {
        error.contains("declined", ignoreCase = true) -> 
            "Card was declined. Please try a different card."
        error.contains("insufficient", ignoreCase = true) -> 
            "Insufficient funds. Please try a different card."
        error.contains("expired", ignoreCase = true) -> 
            "Card is expired. Please use a different card."
        error.contains("network", ignoreCase = true) -> 
            "Network error. Please check your connection and try again."
        error.contains("NFC", ignoreCase = true) -> 
            "Could not read card. Please try holding the card closer to your device."
        else -> "Transaction failed. Please try again."
    }
}
```

---

## Payment Method Selection

### Method Switcher

```kotlin
@Composable
fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    readerConnected: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.subtitle2,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tap to Pay option
                PaymentMethodButton(
                    method = PaymentMethod.TAP_TO_PAY,
                    selected = selectedMethod == PaymentMethod.TAP_TO_PAY,
                    enabled = true,
                    onClick = { onMethodSelected(PaymentMethod.TAP_TO_PAY) }
                )
                
                // External reader option
                PaymentMethodButton(
                    method = PaymentMethod.EXTERNAL_READER,
                    selected = selectedMethod == PaymentMethod.EXTERNAL_READER,
                    enabled = readerConnected,
                    onClick = { onMethodSelected(PaymentMethod.EXTERNAL_READER) }
                )
            }
        }
    }
}

@Composable
fun RowScope.PaymentMethodButton(
    method: PaymentMethod,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val (icon, label) = when (method) {
        PaymentMethod.TAP_TO_PAY -> R.drawable.ic_nfc to "Tap to Pay"
        PaymentMethod.EXTERNAL_READER -> R.drawable.ic_card_reader to "Card Reader"
    }
    
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        enabled = enabled,
        colors = if (selected) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.outlinedButtonColors()
        },
        border = if (!selected) {
            BorderStroke(1.dp, Color.Gray)
        } else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(label)
            
            if (!enabled && method == PaymentMethod.EXTERNAL_READER) {
                Text(
                    "Not connected",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }
        }
    }
}

enum class PaymentMethod {
    TAP_TO_PAY, EXTERNAL_READER
}
```

---

## Accessibility

### Screen Reader Support

```kotlin
@Composable
fun AccessiblePaymentButton(
    amount: Double,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                // Provide clear description for screen readers
                contentDescription = "Pay $${String.format("%.2f", amount)} with card"
                // Announce when payment starts
                liveRegion = LiveRegionMode.Polite
            }
    ) {
        Text("Pay $${String.format("%.2f", amount)}")
    }
}

@Composable
fun AccessibleTransactionProgress(update: String) {
    Column(
        modifier = Modifier.semantics(mergeDescendants = true) {
            // Announce transaction progress to screen readers
            contentDescription = formatUpdateMessage(update)
            liveRegion = LiveRegionMode.Assertive
        }
    ) {
        CircularProgressIndicator()
        Text(formatUpdateMessage(update))
    }
}
```

### Large Text Support

```kotlin
@Composable
fun ScalableAmountDisplay(amount: Double) {
    // Respects user's font size settings
    Text(
        text = "$${"%.2f".format(amount)}",
        style = MaterialTheme.typography.h3,
        // Allows text to scale up to 200% of default size
        maxLines = 1,
        overflow = TextOverflow.Visible
    )
}
```

### High Contrast

```kotlin
@Composable
fun HighContrastStatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "Approved" -> Color(0xFF006600) to Color.White  // Dark green background
        "Declined" -> Color(0xFFCC0000) to Color.White  // Dark red background
        "Processing" -> Color(0xFF0066CC) to Color.White  // Dark blue background
        else -> Color.Gray to Color.White
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold
        )
    }
}
```

---

## Animations

### Card Tap Animation

```kotlin
@Composable
fun CardTapAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Pulsing scale
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Ripple effect
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // Ripple rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Blue.copy(alpha = alpha),
                radius = size.minDimension / 2 * scale
            )
        }
        
        // Card icon
        Icon(
            painter = painterResource(R.drawable.ic_credit_card),
            contentDescription = "Card",
            modifier = Modifier
                .size(64.dp)
                .scale(scale),
            tint = MaterialTheme.colors.primary
        )
    }
}
```

### Success Checkmark Animation

```kotlin
@Composable
fun AnimatedCheckmark() {
    var animationPlayed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    LaunchedEffect(Unit) {
        delay(100)
        animationPlayed = true
    }
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .background(Color.Green, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = "Success",
            modifier = Modifier.size(64.dp),
            tint = Color.White
        )
    }
}
```

### Loading Dots

```kotlin
@Composable
fun LoadingDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .offset(y = offsetY.dp)
                    .background(MaterialTheme.colors.primary, CircleShape)
            )
        }
    }
}
```

---

## Best Practices

### Do's ✅

- **Show clear amount** before starting payment
- **Provide real-time feedback** during transaction
- **Use familiar icons** (card, NFC symbol, checkmark)
- **Support landscape orientation** for tablet devices
- **Test with TalkBack** for accessibility
- **Show retry option** when errors occur
- **Use haptic feedback** on key actions
- **Provide receipt options** after success

### Don'ts ❌

- **Don't hide the amount** during processing
- **Don't use technical error messages** - explain in plain language
- **Don't block the UI** without progress indication
- **Don't auto-dismiss success** - let user confirm they saw it
- **Don't use red for everything** - reserve for actual errors
- **Don't forget loading states** - even if brief
- **Don't skip animations** - they provide important feedback

---

## Next Steps

- **[Compliance](05-compliance.md)** - Required UI elements and branding
- **[Testing](06-testing.md)** - Test your UI with real scenarios
- **[Your First Transaction](02-your-first-transaction.md)** - Implement these patterns
