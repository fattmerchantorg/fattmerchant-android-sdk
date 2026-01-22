# Testing Guide

Comprehensive testing strategies for Tap to Pay integration.

---

## Table of Contents

- [Test Environment Setup](#test-environment-setup)
- [Test Cards](#test-cards)
- [Testing Scenarios](#testing-scenarios)
- [Device Testing](#device-testing)
- [Automated Testing](#automated-testing)
- [Common Issues](#common-issues)

---

## Test Environment Setup

### Development API Keys

Use test API keys provided by Stax for development:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Use test environment during development
        val testApiKey = "test_your_api_key_here"
        
        Omni.initialize(
            context = this,
            apiKey = testApiKey,
            environment = Environment.DEV // Test environment
        )
    }
}
```

### Test Mode Indicator

Display a clear indicator when using test mode:

```kotlin
@Composable
fun TestModeIndicator() {
    if (Omni.shared()?.environment == Environment.DEV) {
        Surface(
            color = Color.Yellow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Test Mode",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "TEST MODE - No real charges",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
```

---

## Test Cards

### Physical Test Cards

Request physical test cards from Stax for comprehensive testing:

**Why Physical Cards?**
- Test actual NFC communication
- Verify card reading reliability
- Test different card types and conditions
- Validate user experience

**How to Get Test Cards:**
- Contact Stax support: developer@staxpayments.com
- Request test cards for Visa, Mastercard, Amex, Discover
- Cards are configured to always approve in test environment

### Test Card Numbers

For API testing (non-NFC scenarios), use these test card numbers:

| Card Brand        | Card Number         | CVV | Expiry | Expected Result |
|-------------------|---------------------|-----|--------|-----------------|
| Visa              | 4111111111111111    | 123 | 12/25  | Approved        |
| Visa              | 4242424242424242    | 123 | 12/25  | Approved        |
| Mastercard        | 5555555555554444    | 123 | 12/25  | Approved        |
| Mastercard        | 2223000048400011    | 123 | 12/25  | Approved        |
| American Express  | 378282246310005     | 1234| 12/25  | Approved        |
| American Express  | 371449635398431     | 1234| 12/25  | Approved        |
| Discover          | 6011111111111117    | 123 | 12/25  | Approved        |
| Discover          | 6011000990139424    | 123 | 12/25  | Approved        |

### Decline Testing

Test decline scenarios with these card numbers:

| Card Number         | Expected Result           |
|---------------------|---------------------------|
| 4000000000000002    | Declined - Card Declined  |
| 4000000000009995    | Declined - Insufficient Funds |
| 4000000000009987    | Declined - Lost Card      |
| 4000000000009979    | Declined - Stolen Card    |
| 4000000000000069    | Declined - Expired Card   |
| 4000000000000127    | Declined - Invalid CVV    |

### Test Transaction Amounts

Certain amounts trigger specific behaviors:

```kotlin
// Test different scenarios with specific amounts
fun testScenarioAmount(scenario: TestScenario): Double {
    return when (scenario) {
        TestScenario.APPROVED -> 10.00
        TestScenario.DECLINED_INSUFFICIENT_FUNDS -> 10.01
        TestScenario.DECLINED_CARD_ERROR -> 10.02
        TestScenario.DECLINED_PROCESSING_ERROR -> 10.03
        TestScenario.DECLINED_INVALID_CARD -> 10.05
        TestScenario.PARTIAL_APPROVAL -> 10.10
    }
}

enum class TestScenario {
    APPROVED,
    DECLINED_INSUFFICIENT_FUNDS,
    DECLINED_CARD_ERROR,
    DECLINED_PROCESSING_ERROR,
    DECLINED_INVALID_CARD,
    PARTIAL_APPROVAL
}
```

---

## Testing Scenarios

### Essential Test Cases

#### 1. Successful Transaction

```kotlin
@Test
fun testSuccessfulTransaction() {
    // Setup
    val amount = 25.00
    val request = TransactionRequest(Amount(amount))
    
    // Execute
    var resultTransaction: Transaction? = null
    var resultError: Exception? = null
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { resultTransaction = it },
        error = { resultError = it }
    )
    
    // Wait for completion (in real tests, use proper async testing)
    Thread.sleep(5000)
    
    // Verify
    assertNotNull(resultTransaction)
    assertEquals(true, resultTransaction?.success)
    assertEquals(amount, resultTransaction?.total)
    assertNotNull(resultTransaction?.id)
    assertNotNull(resultTransaction?.authCode)
}
```

#### 2. Declined Transaction

```kotlin
@Test
fun testDeclinedTransaction() {
    val request = TransactionRequest(Amount(10.01)) // Amount that triggers decline
    
    var resultTransaction: Transaction? = null
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { resultTransaction = it },
        error = { /* expected */ }
    )
    
    Thread.sleep(5000)
    
    // Verify decline
    assertEquals(false, resultTransaction?.success)
    assertNotNull(resultTransaction?.message) // Decline reason
}
```

#### 3. Void Transaction

```kotlin
@Test
fun testVoidTransaction() {
    // First, create a transaction
    val request = TransactionRequest(Amount(15.00))
    var transaction: Transaction? = null
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { transaction = it },
        error = { fail("Transaction failed") }
    )
    
    Thread.sleep(5000)
    
    // Then void it
    val voidUseCase = VoidMobileReaderTransaction(Omni.shared()!!)
    var voidSuccess = false
    
    voidUseCase.void(
        transaction?.id!!,
        completion = { voidSuccess = true },
        error = { fail("Void failed: ${it.message}") }
    )
    
    Thread.sleep(3000)
    
    assertTrue(voidSuccess)
}
```

#### 4. Refund Transaction

```kotlin
@Test
fun testRefundTransaction() {
    // Create transaction
    val originalAmount = 20.00
    val request = TransactionRequest(Amount(originalAmount))
    var transaction: Transaction? = null
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { transaction = it },
        error = { fail("Transaction failed") }
    )
    
    Thread.sleep(5000)
    
    // Refund it
    val refundUseCase = RefundMobileReaderTransaction(Omni.shared()!!)
    var refundTransaction: Transaction? = null
    
    refundUseCase.refund(
        transaction?.id!!,
        Amount(originalAmount),
        completion = { refundTransaction = it },
        error = { fail("Refund failed: ${it.message}") }
    )
    
    Thread.sleep(3000)
    
    assertNotNull(refundTransaction)
    assertEquals(originalAmount, refundTransaction?.total)
}
```

#### 5. Partial Refund

```kotlin
@Test
fun testPartialRefund() {
    // Create $50 transaction
    val request = TransactionRequest(Amount(50.00))
    var transaction: Transaction? = null
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { transaction = it },
        error = { fail() }
    )
    
    Thread.sleep(5000)
    
    // Refund $20
    val refundUseCase = RefundMobileReaderTransaction(Omni.shared()!!)
    var refundTransaction: Transaction? = null
    
    refundUseCase.refund(
        transaction?.id!!,
        Amount(20.00),
        completion = { refundTransaction = it },
        error = { fail() }
    )
    
    Thread.sleep(3000)
    
    assertEquals(20.00, refundTransaction?.total)
}
```

#### 6. Tokenization

```kotlin
@Test
fun testTokenization() {
    val request = TransactionRequest(Amount(10.00))
    request.tokenize = true
    
    var transaction: Transaction? = null
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { transaction = it },
        error = { fail() }
    )
    
    Thread.sleep(5000)
    
    // Verify token was created
    assertNotNull(transaction?.paymentToken)
    assertNotNull(transaction?.lastFour)
    assertNotNull(transaction?.cardType)
}
```

#### 7. Network Error

```kotlin
@Test
fun testNetworkError() {
    // Disable network
    disableNetwork()
    
    val request = TransactionRequest(Amount(10.00))
    var error: Exception? = null
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { fail("Should not succeed without network") },
        error = { error = it }
    )
    
    Thread.sleep(3000)
    
    assertNotNull(error)
    assertTrue(error?.message?.contains("network", ignoreCase = true) ?: false)
    
    // Re-enable network
    enableNetwork()
}
```

### Testing Checklist

Use this checklist for comprehensive testing:

```kotlin
data class TestCase(
    val name: String,
    val description: String,
    var passed: Boolean = false
)

class TestingChecklist {
    val testCases = listOf(
        // Basic Transactions
        TestCase("Successful Visa", "Process approved Visa transaction"),
        TestCase("Successful Mastercard", "Process approved Mastercard transaction"),
        TestCase("Successful Amex", "Process approved American Express transaction"),
        TestCase("Successful Discover", "Process approved Discover transaction"),
        
        // Declines
        TestCase("Declined - Insufficient Funds", "Handle insufficient funds decline"),
        TestCase("Declined - Invalid Card", "Handle invalid card decline"),
        TestCase("Declined - Expired Card", "Handle expired card decline"),
        
        // Transaction Management
        TestCase("Void Same Day", "Void a transaction on same day"),
        TestCase("Full Refund", "Process full refund"),
        TestCase("Partial Refund", "Process partial refund"),
        TestCase("Multiple Partial Refunds", "Process multiple partial refunds"),
        
        // Advanced Features
        TestCase("Tokenization", "Save payment token"),
        TestCase("Charge Saved Token", "Charge a saved payment token"),
        TestCase("Pre-Authorization", "Pre-authorize payment"),
        TestCase("Capture Pre-Auth", "Capture pre-authorized payment"),
        TestCase("Transaction with Tip", "Process payment with tip"),
        TestCase("Transaction with Metadata", "Add custom metadata to transaction"),
        
        // Error Handling
        TestCase("Network Error", "Handle network failure gracefully"),
        TestCase("NFC Not Available", "Handle device without NFC"),
        TestCase("Card Read Error", "Handle failed card read with retry"),
        TestCase("Timeout", "Handle transaction timeout"),
        
        // UI/UX
        TestCase("Transaction Updates", "Display all transaction update messages"),
        TestCase("Success Screen", "Show success screen with transaction details"),
        TestCase("Error Screen", "Show error screen with retry option"),
        TestCase("Loading States", "Show appropriate loading indicators"),
        
        // External Reader
        TestCase("Connect External Reader", "Connect Bluetooth/USB reader"),
        TestCase("External Reader Transaction", "Process payment with external reader"),
        TestCase("Switch Payment Methods", "Switch between NFC and external reader"),
        TestCase("Disconnect Reader", "Handle reader disconnection gracefully")
    )
    
    fun printResults() {
        val passed = testCases.count { it.passed }
        val total = testCases.size
        
        println("Testing Results: $passed/$total passed")
        println()
        
        testCases.forEach { test ->
            val status = if (test.passed) "✅ PASS" else "❌ FAIL"
            println("$status - ${test.name}: ${test.description}")
        }
    }
}
```

---

## Device Testing

### Required Test Devices

Test on various devices to ensure compatibility:

**Minimum:**
- At least one phone with NFC (Pixel, Samsung Galaxy S series)
- At least one tablet with NFC (Samsung Galaxy Tab)

**Recommended:**
- Multiple manufacturers (Samsung, Google Pixel, OnePlus)
- Different Android versions (Android 9, 10, 11, 12, 13, 14)
- Various screen sizes (small phone, large phone, tablet)

### Device Configuration

```kotlin
class DeviceTestingHelper {
    
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            hasNfc = hasNfcCapability(),
            nfcEnabled = isNfcEnabled()
        )
    }
    
    fun hasNfcCapability(): Boolean {
        val nfcManager = context.getSystemService(Context.NFC_SERVICE) as? NfcManager
        return nfcManager?.defaultAdapter != null
    }
    
    fun isNfcEnabled(): Boolean {
        val nfcManager = context.getSystemService(Context.NFC_SERVICE) as? NfcManager
        return nfcManager?.defaultAdapter?.isEnabled == true
    }
    
    fun logDeviceInfo() {
        val info = getDeviceInfo()
        Log.d("DeviceTest", """
            Device: ${info.manufacturer} ${info.model}
            Android: ${info.androidVersion} (SDK ${info.sdkVersion})
            NFC Available: ${info.hasNfc}
            NFC Enabled: ${info.nfcEnabled}
        """.trimIndent())
    }
}

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val hasNfc: Boolean,
    val nfcEnabled: Boolean
)
```

### Device-Specific Issues

Common device-specific issues to test:

```kotlin
fun checkKnownDeviceIssues(): List<String> {
    val issues = mutableListOf<String>()
    
    // Samsung devices may require specific NFC position
    if (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) {
        issues.add("NFC antenna typically at top-center of device")
    }
    
    // Some devices have NFC disabled by default
    if (!isNfcEnabled()) {
        issues.add("NFC is disabled - guide user to enable in Settings")
    }
    
    // Older Android versions may have compatibility issues
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        issues.add("Android version below 9 - some features may not be available")
    }
    
    return issues
}
```

---

## Automated Testing

### Unit Tests

```kotlin
class TransactionRequestTest {
    
    @Test
    fun testTransactionRequestCreation() {
        val amount = 25.50
        val request = TransactionRequest(Amount(amount))
        
        assertEquals(amount, request.amount.dollars())
        assertNotNull(request.meta)
    }
    
    @Test
    fun testTransactionRequestWithMetadata() {
        val request = TransactionRequest(Amount(10.00))
        request.memo = "Test transaction"
        request.reference = "REF123"
        
        assertEquals("Test transaction", request.memo)
        assertEquals("REF123", request.reference)
    }
    
    @Test
    fun testAmountFormatting() {
        val amount = Amount(19.99)
        assertEquals(19.99, amount.dollars())
        assertEquals(1999, amount.cents())
    }
}
```

### Integration Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class OmniIntegrationTest {
    
    private lateinit var omni: Omni
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        
        Omni.initialize(
            context = context,
            apiKey = "test_api_key",
            environment = Environment.DEV
        )
        
        omni = Omni.shared()!!
    }
    
    @Test
    fun testOmniInitialization() {
        assertNotNull(omni)
        assertEquals(Environment.DEV, omni.environment)
    }
    
    @Test
    fun testTransactionRequestValidation() {
        val request = TransactionRequest(Amount(0.50)) // Minimum amount
        assertNotNull(request)
        
        // Test that request can be serialized properly
        val json = Gson().toJson(request)
        assertNotNull(json)
    }
}
```

### UI Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class PaymentScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testPaymentScreenDisplaysAmount() {
        val amount = 25.00
        
        composeTestRule.setContent {
            PaymentScreen(amount = amount)
        }
        
        // Verify amount is displayed
        composeTestRule
            .onNodeWithText("$25.00")
            .assertIsDisplayed()
    }
    
    @Test
    fun testPayButtonIsEnabled() {
        composeTestRule.setContent {
            PaymentScreen(amount = 10.00)
        }
        
        // Verify pay button exists and is enabled
        composeTestRule
            .onNodeWithText("Pay $10.00")
            .assertIsEnabled()
    }
    
    @Test
    fun testErrorMessageDisplayed() {
        composeTestRule.setContent {
            ErrorUI(
                message = "Card declined",
                onRetry = {},
                onCancel = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Payment Failed")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("Try Again")
            .assertIsDisplayed()
    }
}
```

---

## Common Issues

### Issue: NFC Not Working

**Symptoms:**
- Card tap not detected
- "NFC not available" error
- Transaction never starts

**Solutions:**

```kotlin
fun diagnoseNfcIssue(): String {
    return when {
        !hasNfcCapability() -> {
            "Device does not have NFC hardware"
        }
        !isNfcEnabled() -> {
            "NFC is disabled - guide user to enable in Settings"
        }
        !hasNfcPermission() -> {
            "NFC permission not granted in AndroidManifest.xml"
        }
        else -> {
            "NFC should be working - try restarting device"
        }
    }
}

fun guideUserToEnableNfc() {
    AlertDialog.Builder(this)
        .setTitle("Enable NFC")
        .setMessage("NFC is required for Tap to Pay. Would you like to enable it now?")
        .setPositiveButton("Open Settings") { _, _ ->
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

### Issue: Transaction Timeout

**Symptoms:**
- Transaction never completes
- No completion or error callback

**Solutions:**

```kotlin
fun processTransactionWithTimeout(
    request: TransactionRequest,
    timeoutSeconds: Int = 60
) {
    val handler = Handler(Looper.getMainLooper())
    var completed = false
    
    // Set timeout
    handler.postDelayed({
        if (!completed) {
            Log.e("Payment", "Transaction timed out after $timeoutSeconds seconds")
            showError("Transaction timed out. Please try again.")
        }
    }, timeoutSeconds * 1000L)
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { transaction ->
            completed = true
            handler.removeCallbacksAndMessages(null)
            handleSuccess(transaction)
        },
        error = { exception ->
            completed = true
            handler.removeCallbacksAndMessages(null)
            handleError(exception)
        }
    )
}
```

### Issue: Inconsistent Card Reading

**Symptoms:**
- Card sometimes detected, sometimes not
- "Card read error" frequently

**Solutions:**

```kotlin
fun improveCardReadReliability() {
    // 1. Guide user on card positioning
    showCardPositionGuide()
    
    // 2. Implement retry logic
    var retryCount = 0
    val maxRetries = 3
    
    fun attemptTransaction() {
        Omni.shared()?.takeMobileReaderTransaction(
            request,
            completion = { /* success */ },
            error = { exception ->
                if (exception.message?.contains("card read", ignoreCase = true) == true &&
                    retryCount < maxRetries) {
                    retryCount++
                    showMessage("Card read failed. Please try again ($retryCount/$maxRetries)")
                    Handler(Looper.getMainLooper()).postDelayed({
                        attemptTransaction()
                    }, 2000)
                } else {
                    showError("Could not read card. Please try a different card.")
                }
            }
        )
    }
    
    attemptTransaction()
}

@Composable
fun CardPositionGuide() {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("How to Tap Card") },
        text = {
            Column {
                Text("For best results:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Hold card flat against device")
                Text("• Position at top-center of phone")
                Text("• Hold still for 2-3 seconds")
                Text("• Remove card when prompted")
            }
        },
        confirmButton = {
            Button(onClick = { /* dismiss */ }) {
                Text("Got it")
            }
        }
    )
}
```

---

## Next Steps

- **[Production Deployment](07-production-deployment.md)** - Launch your app
- **[Troubleshooting](08-troubleshooting.md)** - Detailed problem solving
- **[Compliance](05-compliance.md)** - Ensure you meet all requirements
