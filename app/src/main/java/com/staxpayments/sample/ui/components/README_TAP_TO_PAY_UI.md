# Tap to Pay UI Component

This component implements the Tap to Pay payment prompt UI based on the Figma designs.

## Design Source

- **Figma File**: Tap-to-Pay
- **Light Mode**: [View Design](https://www.figma.com/design/HC8m4zQD5zEIdK0ShetbJ5/Tap-to-Pay?node-id=943-5157&m=dev)
- **Dark Mode**: [View Design](https://www.figma.com/design/HC8m4zQD5zEIdK0ShetbJ5/Tap-to-Pay?node-id=1154-3791&m=dev)

## Components

### TapToPayPrompt

Main composable that displays the NFC payment prompt with:

- **Circular Prompt**: Purple gradient circle with "Tap on the back" text and NFC icon
- **Payment Card Logos**: Shows supported payment methods (Visa, Mastercard, Discover, Amex, JCB)
- **Amount Display**: Total amount with breakdown (subtotal + tip)
- **Cancel Button**: Circular button to dismiss the prompt

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

```kotlin
@Composable
fun PaymentScreen() {
    val viewModel: StaxViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.showTapToPayPrompt) {
        TapToPayPrompt(
            amount = "28.00",
            subtotal = "25.00",
            tip = "3.00",
            onCancel = { viewModel.dismissTapToPayPrompt() }
        )
    }
}
```

## Integration Flow

1. User selects Tap to Pay mode (Tap to Pay Only or Hybrid)
2. User clicks "Perform Sale With Reader"
3. ViewModel updates state to show the prompt:
   ```kotlin
   _uiState.update { state ->
       state.copy(
           showTapToPayPrompt = true,
           transactionAmount = "28.00",
           transactionSubtotal = "25.00",
           transactionTip = "3.00"
       )
   }
   ```
4. TapToPayPrompt is displayed as an overlay
5. User taps card/device to phone (NFC transaction occurs)
6. On completion or cancellation, prompt is dismissed

## Design Tokens

The component uses the following design tokens from Figma:

### Colors
- `Purple/50`: #F7E9FC (Light mode circle)
- `Purple/500`: #B93BE4 (Shadow color)
- `Purple/700`: #602F8B (Dark mode circle)
- `Stax Black`: #062333 (Dark background, light text)
- `Gray/0`: #FFFFFF (White)
- `Gray/100`: #DDDFE4 (Light cancel button)
- `Gray/500`: #627684 (Light cancel icon)

### Typography
- `Heading/H1-Bold`: Roboto Bold, 48sp, line height 72sp (Total amount)
- `Heading/H4-Bold`: Roboto Bold, 24sp, line height 36sp (Prompt text)
- `Heading/H4-Regular`: Roboto Regular, 24sp, line height 36sp (Amount breakdown)

### Spacing
- Circle size: 300dp
- Circle shadow: 10dp elevation
- Card logo spacing: 8dp gap
- Section spacing: 40dp gap

## TODO

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
