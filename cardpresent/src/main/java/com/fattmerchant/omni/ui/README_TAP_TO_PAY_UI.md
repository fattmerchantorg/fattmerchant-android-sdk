# Tap to Pay UI Component

This component implements the Tap to Pay payment prompt UI that handles the complete transaction flow with SDK callback integration.

## Design Source

- **Figma File**: Tap-to-Pay
- **Light Mode**: [View Design](https://www.figma.com/design/HC8m4zQD5zEIdK0ShetbJ5/Tap-to-Pay?node-id=943-5157&m=dev)
- **Dark Mode**: [View Design](https://www.figma.com/design/HC8m4zQD5zEIdK0ShetbJ5/Tap-to-Pay?node-id=1154-3791&m=dev)

## Features

### Integrated Transaction Handling

The `TapToPayPrompt` component is a fully integrated solution that:

- **Automatically manages transaction lifecycle**: Sets up and tears down SDK listeners
- **Real-time status updates**: Displays transaction progress messages from the SDK
- **Smart state management**: Shows processing indicators, success, and error states
- **Callback integration**: Handles all TransactionUpdateListener events internally
- **Self-contained**: Requires minimal external state management

### Visual Elements

- **Circular Prompt**: Purple gradient circle with "Tap on the back" text and NFC icon
- **Payment Card Logos**: Shows supported payment methods (Visa, Mastercard, Discover, Amex, JCB)
- **Amount Display**: Total amount with breakdown (subtotal + tip)
- **Status Messages**: Real-time transaction status with color-coded feedback
- **Processing Indicator**: Circular progress indicator during transaction
- **Success/Error States**: Visual feedback with checkmark (✓) or error (✗) icons
- **Cancel Button**: Circular button to cancel the transaction

### Theme Support

The component automatically adapts to Light and Dark themes:

#### Light Mode
- Background: White (#FFFFFF)
- Circle: Purple/50 (#F7E9FC) 
- Text: Stax Black (#062333)
- Cancel Button: Gray/100 (#DDDFE4)

#### Dark Mode
- Background: Stax Black (#062333)
- Circle: Purple/700 (#602F8B)
- Text: White (#FFFFFF)
- Cancel Button: Stax Black with white border

## Usage

### Basic Usage (Automatic Transaction)

The simplest way to use `TapToPayPrompt` - it handles everything automatically:

```kotlin
import com.fattmerchant.omni.ui.TapToPayPrompt

@Composable
fun PaymentScreen() {
    val viewModel: StaxViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.showTapToPayPrompt) {
        TapToPayPrompt(
            amount = "28.00",
            subtotal = "25.00",
            tip = "3.00",
            transactionRequest = uiState.transactionRequest,
            onSuccess = { transaction -> 
                viewModel.onTransactionSuccess(transaction)
            },
            onError = { errorMessage -> 
                viewModel.onTransactionError(errorMessage)
            },
            onCancel = { viewModel.dismissTapToPayPrompt() }
        )
    }
}
```

### ViewModel Integration

```kotlin
class StaxViewModel : ViewModel() {
    fun performSaleWithReader() {
        val amount = Amount(28.00)
        val request = TransactionRequest(amount).apply {
            memo = "Tap to Pay transaction"
        }
        
        // Show prompt with transaction request
        _uiState.update { state ->
            state.copy(
                showTapToPayPrompt = true,
                transactionAmount = "28.00",
                transactionSubtotal = "25.00",
                transactionTip = "3.00",
                transactionRequest = request
            )
        }
    }
    
    fun onTransactionSuccess(transaction: Transaction) {
        log("Transaction successful: ${transaction.id}")
        lastTransaction = transaction
        _uiState.update { it.copy(showTapToPayPrompt = false) }
    }
    
    fun onTransactionError(errorMessage: String) {
        log("Transaction error: $errorMessage")
        _uiState.update { it.copy(showTapToPayPrompt = false) }
    }
    
    fun dismissTapToPayPrompt() {
        // Cancel the transaction
        Omni.shared()?.cancelMobileReaderTransaction()
        _uiState.update { it.copy(showTapToPayPrompt = false) }
    }
}
```

## How It Works

### Transaction Lifecycle

1. **Component Appears**
   - `TapToPayPrompt` is rendered with transaction details
   - Sets up `TransactionUpdateListener` via `DisposableEffect`
   - Automatically calls `takeMobileReaderTransaction()` via `LaunchedEffect`

2. **Transaction Processing**
   - SDK sends updates via `TransactionUpdateListener`
   - Status messages automatically update in real-time
   - Processing indicator shown during authorization
   - Status messages color-coded based on state

3. **Transaction Completes**
   - Success: Shows checkmark (✓), calls `onSuccess` callback
   - Error/Decline: Shows error (✗), calls `onError` callback
   - User Cancel: Calls SDK's `cancelMobileReaderTransaction()`, triggers `onCancel`

4. **Cleanup**
   - `DisposableEffect` automatically removes listener when component unmounts
   - Ensures no memory leaks or orphaned listeners

### Transaction Status Messages

The component automatically maps SDK transaction updates to user-friendly messages:

| SDK Update | Displayed Message |
|------------|------------------|
| Transaction Started | "Starting payment..." |
| Prompt Insert Card | "Hold card near device" |
| Card Detected | "Card detected" |
| Reading Card | "Reading card..." |
| Authorizing | "Authorizing payment..." |
| Authorized | "Payment approved!" (green) |
| Declined | "Payment declined" (red) |
| Card Read Error | "Card read failed" (red) |

## Integration Flow

1. **User Initiates Payment**
   - User selects Tap to Pay mode (Tap to Pay Only or Hybrid)
   - User clicks "Perform Sale With Reader"

2. **ViewModel Prepares Transaction**
   ```kotlin
   _uiState.update { state ->
       state.copy(
           showTapToPayPrompt = true,
           transactionAmount = amount.dollarsString(),
           transactionSubtotal = subtotal.dollarsString(),
           transactionTip = tip.dollarsString(),
           transactionRequest = request  // Pass the request
       )
   }
   ```

3. **TapToPayPrompt Takes Over**
   - Component appears and sets up listeners
   - Automatically starts transaction
   - Displays real-time status updates
   - Shows processing indicator

4. **Transaction Completes**
   - Success: Calls `onSuccess(transaction)`, hides prompt
   - Error: Calls `onError(message)`, hides prompt
   - Cancel: Calls SDK cancellation, triggers `onCancel()`

5. **ViewModel Handles Result**
   - Logs the transaction result
   - Updates application state
   - Hides the prompt

## API Reference

### TapToPayPrompt Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `amount` | String | Yes | Total payment amount (e.g., "28.00") |
| `subtotal` | String | Yes | Subtotal before tip (e.g., "25.00") |
| `tip` | String | Yes | Tip amount (e.g., "3.00") |
| `transactionRequest` | TransactionRequest? | No | Pre-built transaction request. If null, builds from amount |
| `onSuccess` | (Transaction) -> Unit | No | Called when transaction succeeds. Default: no-op |
| `onError` | (String) -> Unit | No | Called when transaction fails. Default: no-op |
| `onCancel` | () -> Unit | Yes | Called when user cancels |
| `modifier` | Modifier | No | Optional Compose modifier |

### Transaction States

The component internally manages these states:

```kotlin
sealed class TapToPayState {
    object Idle : TapToPayState()
    data class Processing(val message: String) : TapToPayState()
    data class Success(val transaction: Transaction) : TapToPayState()
    data class Error(val message: String) : TapToPayState()
}
```

## Customization

While `TapToPayPrompt` handles most use cases automatically, you can customize behavior:

### Custom Transaction Request

```kotlin
val customRequest = TransactionRequest(Amount(50.00)).apply {
    memo = "Custom order #123"
    meta = mapOf("orderId" to "123", "customerId" to "456")
}

TapToPayPrompt(
    amount = "50.00",
    subtotal = "45.00",
    tip = "5.00",
    transactionRequest = customRequest,
    onSuccess = { transaction -> 
        // Handle success with custom logic
    },
    onError = { error ->
        // Handle error with custom logic
    },
    onCancel = { /* Cancel handler */ }
)
```

### Minimal Integration

For simpler cases where you don't need pre-built requests:

```kotlin
TapToPayPrompt(
    amount = "28.00",
    subtotal = "25.00", 
    tip = "3.00",
    onCancel = { dismissPrompt() }
)
```

The component will build the `TransactionRequest` automatically from the amount.

## Design Tokens

The component uses the following design tokens from Figma:

### Colors
- `Purple/50`: #F7E9FC (Light mode circle)
- `Purple/500`: #B93BE4 (Shadow/processing indicator)
- `Purple/700`: #602F8B (Dark mode circle)
- `Stax Black`: #062333 (Dark background, light text)
- `Gray/0`: #FFFFFF (White)
- `Gray/100`: #DDDFE4 (Light cancel button)
- `Gray/500`: #627684 (Light cancel icon)
- `Success Green`: #4CAF50 (Success messages/checkmark)
- `Error Red`: #E53935 (Error messages/error icon)

### Typography
- `Heading/H1-Bold`: Roboto Bold, 48sp, line height 72sp (Total amount)
- `Heading/H4-Bold`: Roboto Bold, 24sp, line height 36sp (Prompt text)
- `Heading/H4-Regular`: Roboto Regular, 24sp, line height 36sp (Amount breakdown, status)

### Spacing
- Circle size: 300dp
- Circle shadow: 10dp elevation
- Card logo spacing: 8dp gap
- Section spacing: 40dp gap

## Benefits

### For Developers

- **Reduced boilerplate**: No need to manually manage TransactionUpdateListener
- **Automatic cleanup**: DisposableEffect ensures proper listener lifecycle
- **Type-safe callbacks**: Strongly-typed success/error handlers
- **Self-contained**: All transaction logic encapsulated in the component
- **Easy testing**: Clear separation between UI and business logic

### For Users

- **Real-time feedback**: See transaction progress as it happens
- **Clear status**: Color-coded messages indicate success/error states
- **Visual feedback**: Animations and icons provide clear state indicators
- **Consistent UX**: Professional design that matches iOS Tap to Pay patterns

## Notes

- Component automatically calls `Omni.shared()?.takeMobileReaderTransaction()` when it appears
- Listener is automatically cleaned up when component is removed from composition
- If `transactionRequest` is null, builds a basic request from the `amount` parameter
- Status messages are mapped from SDK updates to user-friendly text
- Component handles both Light and Dark themes automatically

### Assets Needed
- [ ] Replace NFC icon placeholder with actual drawable
- [ ] Add payment card logo drawables (Visa, Mastercard, Discover, Amex, JCB)
- [ ] Add proper Font Awesome icon for cancel button

### Enhancements
- [ ] Add tap animation for the circular prompt
- [ ] Add shimmer or pulse effect while waiting for tap
- [ ] Add haptic feedback on cancel
- [ ] Support dynamic tip calculation
- [ ] Add accessibility labels

## Testing

Preview composables are included for both themes:

```kotlin
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
```

Run these previews in Android Studio to verify the design matches Figma.
