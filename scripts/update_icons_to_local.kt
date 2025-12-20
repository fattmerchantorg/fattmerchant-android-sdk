// Helper reference for updating TapToPayPrompt.kt to use local drawables
// After running download_figma_assets.sh, replace the remote URLs with these local references

/*
BEFORE (Remote URLs):
```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data("https://www.figma.com/api/mcp/asset/df422283...")
        .crossfade(true)
        .build(),
    contentDescription = "NFC Contactless",
    modifier = modifier,
    contentScale = ContentScale.Fit,
    colorFilter = ColorFilter.tint(if (isDarkTheme) Color.White else StaxBlack)
)
```

AFTER (Local Drawables):
```kotlin
Image(
    painter = painterResource(R.drawable.ic_nfc_contactless),
    contentDescription = "NFC Contactless",
    modifier = modifier,
    contentScale = ContentScale.Fit,
    colorFilter = ColorFilter.tint(if (isDarkTheme) Color.White else StaxBlack)
)
```

Payment Card Icons:
- PaymentCardIcon("https://...c04f40c8...", "Visa")
  → PaymentCardIcon(R.drawable.ic_payment_visa, "Visa")

- PaymentCardIcon("https://...00f67878...", "Mastercard")
  → PaymentCardIcon(R.drawable.ic_payment_mastercard, "Mastercard")

- PaymentCardIcon("https://...ce571b86...", "Discover")
  → PaymentCardIcon(R.drawable.ic_payment_discover, "Discover")

- PaymentCardIcon("https://...b8fa4dd9...", "Amex")
  → PaymentCardIcon(R.drawable.ic_payment_amex, "Amex")

- PaymentCardIcon("https://...d81d73f8...", "JCB")
  → PaymentCardIcon(R.drawable.ic_payment_jcb, "JCB")

Update function signatures:
```kotlin
@Composable
private fun PaymentCardIcon(
    @DrawableRes iconRes: Int,  // Changed from String to Int
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 34.dp, height = 24.dp)
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.padding(4.dp),
            contentScale = ContentScale.Fit
        )
    }
}
```

Remove imports:
- Remove: import coil.compose.AsyncImage
- Remove: import coil.request.ImageRequest
- Remove: import androidx.compose.ui.platform.LocalContext
- Add: import androidx.annotation.DrawableRes
- Keep: import androidx.compose.ui.res.painterResource (already present)

Remove from build.gradle.kts if not used elsewhere:
- implementation("io.coil-kt:coil-compose:2.5.0")
*/
