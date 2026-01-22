# Troubleshooting Guide

Solutions to common issues when integrating Tap to Pay.

---

## Table of Contents

- [SDK Initialization Issues](#sdk-initialization-issues)
- [NFC Problems](#nfc-problems)
- [Transaction Failures](#transaction-failures)
- [Network Errors](#network-errors)
- [Card Reading Issues](#card-reading-issues)
- [Build & Compilation](#build--compilation)
- [Runtime Errors](#runtime-errors)

---

## SDK Initialization Issues

### Error: "Omni not initialized"

**Symptom:**
```
java.lang.NullPointerException: Omni.shared() returns null
```

**Cause:** SDK not initialized before use

**Solution:**

```kotlin
// ✅ CORRECT: Initialize in Application class
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        Omni.initialize(
            context = this,
            apiKey = "your_api_key",
            environment = Environment.DEV
        )
    }
}

// Update AndroidManifest.xml
<application
    android:name=".MyApplication"  <!-- Add this -->
    ...>
</application>
```

### Error: "Invalid API key"

**Symptom:**
```
401 Unauthorized - Invalid API key
```

**Cause:** Wrong or expired API key

**Solution:**

```kotlin
// Check your API key
fun validateApiKey(apiKey: String): Boolean {
    return when {
        apiKey.isEmpty() -> {
            Log.e("Stax", "API key is empty")
            false
        }
        apiKey.startsWith("test_") && environment == Environment.LIVE -> {
            Log.e("Stax", "Using test key in production environment")
            false
        }
        apiKey.startsWith("live_") && environment == Environment.DEV -> {
            Log.e("Stax", "Using production key in test environment")
            false
        }
        else -> true
    }
}

// Get fresh API keys from Stax dashboard
// Test: https://dashboard.staxpayments.com/test
// Production: https://dashboard.staxpayments.com/
```

### Error: "Context must be Application context"

**Symptom:**
```
IllegalArgumentException: Context must be Application context
```

**Cause:** Passing Activity context instead of Application context

**Solution:**

```kotlin
// ❌ WRONG
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Omni.initialize(this, ...) // Activity context
    }
}

// ✅ CORRECT
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Omni.initialize(this, ...) // Application context
    }
}
```

---

## NFC Problems

### Issue: "NFC not available"

**Diagnosis:**

```kotlin
fun diagnoseNfc(context: Context): String {
    val nfcManager = context.getSystemService(Context.NFC_SERVICE) as? NfcManager
    val nfcAdapter = nfcManager?.defaultAdapter
    
    return when {
        nfcAdapter == null -> {
            "❌ Device does not have NFC hardware"
        }
        !nfcAdapter.isEnabled -> {
            "⚠️ NFC is disabled - needs to be enabled in Settings"
        }
        else -> {
            "✅ NFC is available and enabled"
        }
    }
}
```

**Solutions:**

#### Solution 1: Device doesn't have NFC

```kotlin
fun handleNoNfcHardware() {
    AlertDialog.Builder(this)
        .setTitle("NFC Not Supported")
        .setMessage("Your device does not support NFC. Tap to Pay requires an NFC-enabled Android device.")
        .setPositiveButton("View Compatible Devices") { _, _ ->
            // Open list of compatible devices
            openCompatibleDevicesList()
        }
        .setNegativeButton("OK", null)
        .show()
}
```

#### Solution 2: NFC is disabled

```kotlin
fun promptEnableNfc() {
    AlertDialog.Builder(this)
        .setTitle("Enable NFC")
        .setMessage("NFC is required for contactless payments. Would you like to enable it now?")
        .setPositiveButton("Open Settings") { _, _ ->
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }
        .setNegativeButton("Cancel", null)
        .show()
}

// Check when returning from settings
override fun onResume() {
    super.onResume()
    
    if (isNfcEnabled()) {
        showMessage("NFC enabled! Ready to accept payments.")
    }
}
```

### Issue: Card not detected

**Symptom:** Holding card near device doesn't trigger transaction

**Solutions:**

```kotlin
// Solution 1: Check NFC permission in manifest
// AndroidManifest.xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />

// Solution 2: Guide user on card positioning
@Composable
fun CardPositioningGuide() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Text("Card not detected?", style = MaterialTheme.typography.h6)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Try these tips:")
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TroubleshootingTip("Position card at the top-center of device")
        TroubleshootingTip("Hold card flat against phone")
        TroubleshootingTip("Remove phone case if thick")
        TroubleshootingTip("Keep card still for 2-3 seconds")
        TroubleshootingTip("Try different card orientation")
    }
}

@Composable
fun TroubleshootingTip(tip: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("• ", fontWeight = FontWeight.Bold)
        Text(tip)
    }
}

// Solution 3: Device-specific NFC antenna location
fun getNfcAntennaLocation(): String {
    return when (Build.MANUFACTURER.lowercase()) {
        "samsung" -> "Top-center of device, slightly to the right"
        "google" -> "Top-center of device"
        "oneplus" -> "Center of device"
        "xiaomi" -> "Top-center of device"
        else -> "Typically at top or center of device - try different positions"
    }
}
```

---

## Transaction Failures

### Error: "Transaction declined"

**Symptom:** Transaction completes but is declined

**Diagnosis:**

```kotlin
fun analyzeDecline(transaction: Transaction): DeclineInfo {
    val message = transaction.message ?: "Unknown decline reason"
    
    return when {
        message.contains("insufficient", ignoreCase = true) -> {
            DeclineInfo(
                reason = "Insufficient Funds",
                userMessage = "Card has insufficient funds. Please try a different card.",
                canRetry = true
            )
        }
        message.contains("expired", ignoreCase = true) -> {
            DeclineInfo(
                reason = "Card Expired",
                userMessage = "Card is expired. Please use a different card.",
                canRetry = true
            )
        }
        message.contains("invalid", ignoreCase = true) -> {
            DeclineInfo(
                reason = "Invalid Card",
                userMessage = "Card is invalid. Please try a different card.",
                canRetry = true
            )
        }
        message.contains("restricted", ignoreCase = true) -> {
            DeclineInfo(
                reason = "Card Restricted",
                userMessage = "Card is restricted. Please contact your card issuer.",
                canRetry = false
            )
        }
        message.contains("stolen", ignoreCase = true) || message.contains("lost", ignoreCase = true) -> {
            DeclineInfo(
                reason = "Card Reported Lost/Stolen",
                userMessage = "Card cannot be used. Please use a different card.",
                canRetry = false
            )
        }
        else -> {
            DeclineInfo(
                reason = "Declined",
                userMessage = "Transaction declined. Please try a different card or payment method.",
                canRetry = true
            )
        }
    }
}

data class DeclineInfo(
    val reason: String,
    val userMessage: String,
    val canRetry: Boolean
)
```

**Solutions:**

```kotlin
fun handleDecline(transaction: Transaction) {
    val declineInfo = analyzeDecline(transaction)
    
    AlertDialog.Builder(this)
        .setTitle("Payment Declined")
        .setMessage(declineInfo.userMessage)
        .setPositiveButton(if (declineInfo.canRetry) "Try Another Card" else "OK") { _, _ ->
            if (declineInfo.canRetry) {
                // Retry with different card
                retryTransaction()
            }
        }
        .setNegativeButton("Cancel", null)
        .show()
    
    // Log for analytics
    logDecline(transaction, declineInfo.reason)
}
```

### Error: "Transaction timeout"

**Symptom:** Transaction never completes (no success or error callback)

**Solution:**

```kotlin
class TransactionWithTimeout {
    
    private var timeoutHandler: Handler? = null
    private var completed = false
    
    fun processTransaction(
        request: TransactionRequest,
        timeoutSeconds: Int = 60,
        onComplete: (Transaction) -> Unit,
        onError: (Exception) -> Unit
    ) {
        completed = false
        timeoutHandler = Handler(Looper.getMainLooper())
        
        // Start timeout timer
        timeoutHandler?.postDelayed({
            if (!completed) {
                completed = true
                onError(TimeoutException("Transaction timed out after $timeoutSeconds seconds"))
            }
        }, timeoutSeconds * 1000L)
        
        // Start transaction
        Omni.shared()?.takeMobileReaderTransaction(
            request,
            completion = { transaction ->
                if (!completed) {
                    completed = true
                    timeoutHandler?.removeCallbacksAndMessages(null)
                    onComplete(transaction)
                }
            },
            error = { exception ->
                if (!completed) {
                    completed = true
                    timeoutHandler?.removeCallbacksAndMessages(null)
                    onError(exception)
                }
            }
        )
    }
    
    fun cancel() {
        completed = true
        timeoutHandler?.removeCallbacksAndMessages(null)
    }
}

// Usage
val transactionManager = TransactionWithTimeout()

transactionManager.processTransaction(
    request = TransactionRequest(Amount(25.00)),
    timeoutSeconds = 60,
    onComplete = { transaction ->
        handleSuccess(transaction)
    },
    onError = { exception ->
        if (exception is TimeoutException) {
            showError("Transaction took too long. Please try again.")
        } else {
            handleError(exception)
        }
    }
)
```

---

## Network Errors

### Error: "Network request failed"

**Symptom:**
```
IOException: Unable to resolve host
SocketTimeoutException: timeout
```

**Diagnosis:**

```kotlin
fun diagnoseNetworkIssue(): NetworkDiagnosis {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    
    return NetworkDiagnosis(
        isConnected = activeNetwork != null,
        hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true,
        hasValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true,
        transportType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
            else -> "None"
        }
    )
}

data class NetworkDiagnosis(
    val isConnected: Boolean,
    val hasInternet: Boolean,
    val hasValidated: Boolean,
    val transportType: String
)
```

**Solutions:**

```kotlin
// Solution 1: Check network before transaction
fun processTransactionWithNetworkCheck(request: TransactionRequest) {
    val diagnosis = diagnoseNetworkIssue()
    
    when {
        !diagnosis.isConnected -> {
            showError("No internet connection. Please connect to WiFi or cellular data.")
            return
        }
        !diagnosis.hasValidated -> {
            showError("Internet connection is unstable. Please check your connection.")
            return
        }
        else -> {
            // Network is good, proceed with transaction
            Omni.shared()?.takeMobileReaderTransaction(request, ...)
        }
    }
}

// Solution 2: Implement retry logic
fun processWithRetry(
    request: TransactionRequest,
    maxRetries: Int = 3,
    onComplete: (Transaction) -> Unit,
    onError: (Exception) -> Unit
) {
    var retryCount = 0
    
    fun attempt() {
        Omni.shared()?.takeMobileReaderTransaction(
            request,
            completion = onComplete,
            error = { exception ->
                if (isNetworkError(exception) && retryCount < maxRetries) {
                    retryCount++
                    showMessage("Network error. Retrying ($retryCount/$maxRetries)...")
                    
                    Handler(Looper.getMainLooper()).postDelayed({
                        attempt()
                    }, 2000 * retryCount) // Exponential backoff
                } else {
                    onError(exception)
                }
            }
        )
    }
    
    attempt()
}

fun isNetworkError(exception: Exception): Boolean {
    return exception is IOException ||
           exception is SocketTimeoutException ||
           exception.message?.contains("network", ignoreCase = true) == true
}

// Solution 3: Offline queue
class OfflineTransactionQueue {
    private val queue = mutableListOf<TransactionRequest>()
    
    fun addToQueue(request: TransactionRequest) {
        queue.add(request)
        showMessage("No internet. Transaction queued for when connection is restored.")
    }
    
    fun processQueue() {
        if (queue.isEmpty() || !diagnoseNetworkIssue().hasValidated) {
            return
        }
        
        val request = queue.removeAt(0)
        processTransaction(request,
            onComplete = {
                showMessage("Queued transaction processed successfully")
                processQueue() // Process next
            },
            onError = {
                // Re-add to front of queue
                queue.add(0, request)
            }
        )
    }
}
```

### Error: "SSL handshake failed"

**Symptom:**
```
SSLHandshakeException: Handshake failed
```

**Cause:** Outdated SSL certificates or security settings

**Solution:**

```kotlin
// Ensure you're using HTTPS
// res/xml/network_security_config.xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.staxpayments.com</domain>
    </domain-config>
</network-security-config>

// AndroidManifest.xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="false"
    ...>
</application>

// Update WebView SSL if needed
fun updateWebViewSsl() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }
}
```

---

## Card Reading Issues

### Issue: "Card read error"

**Symptom:** Card detected but fails to read

**Solutions:**

```kotlin
// Solution 1: Implement retry logic
class CardReadWithRetry {
    
    private var readAttempts = 0
    private val maxAttempts = 3
    
    fun attemptRead(
        request: TransactionRequest,
        onSuccess: (Transaction) -> Unit,
        onFailure: (String) -> Unit
    ) {
        readAttempts++
        
        Omni.shared()?.takeMobileReaderTransaction(
            request,
            completion = { transaction ->
                readAttempts = 0
                onSuccess(transaction)
            },
            error = { exception ->
                if (isCardReadError(exception) && readAttempts < maxAttempts) {
                    showRetryPrompt("Card read failed. Please try again ($readAttempts/$maxAttempts)")
                    // User can retry
                } else {
                    readAttempts = 0
                    onFailure("Could not read card after $maxAttempts attempts. Please try a different card.")
                }
            }
        )
    }
    
    fun isCardReadError(exception: Exception): Boolean {
        return exception.message?.contains("card read", ignoreCase = true) == true ||
               exception.message?.contains("card error", ignoreCase = true) == true
    }
}

// Solution 2: Provide helpful guidance
@Composable
fun CardReadErrorHelp() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Having trouble reading your card?", style = MaterialTheme.typography.h6)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Try these steps:", fontWeight = FontWeight.Bold)
        
        HelpStep("1", "Remove phone case if thick or metallic")
        HelpStep("2", "Clean card's contactless chip")
        HelpStep("3", "Hold card flat against phone")
        HelpStep("4", "Keep card still for 3-5 seconds")
        HelpStep("5", "Try different card position")
        HelpStep("6", "Ensure phone is not in power saving mode")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Still having issues?", fontWeight = FontWeight.Bold)
        Text("• Try a different card")
        Text("• Use an external card reader")
        Text("• Contact support")
    }
}

@Composable
fun HelpStep(number: String, instruction: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("$number. ", fontWeight = FontWeight.Bold)
        Text(instruction)
    }
}
```

---

## Build & Compilation

### Error: "Duplicate class found"

**Symptom:**
```
Duplicate class com.creditcall.chipdnamobile.ParameterKeys found in modules
```

**Cause:** Multiple versions of dependencies

**Solution:**

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.fattmerchant:cardpresent:2.7.0")
    
    // If you see duplicate class errors, add exclusions
    implementation("com.fattmerchant:cardpresent:2.7.0") {
        exclude(group = "com.creditcall", module = "chipdnamobile")
    }
    
    // Or force specific version
    configurations.all {
        resolutionStrategy {
            force("com.creditcall:chipdnamobile:version")
        }
    }
}
```

### Error: "Manifest merger failed"

**Symptom:**
```
AndroidManifest.xml merger failed : Attribute application@label value=(AppName) from AndroidManifest.xml is also present at [com.fattmerchant:cardpresent]
```

**Solution:**

```xml
<!-- AndroidManifest.xml -->
<application
    android:label="@string/app_name"
    tools:replace="android:label"  <!-- Add this -->
    ...>
</application>
```

### Error: "Unsupported Java version"

**Symptom:**
```
Android Gradle plugin requires Java 11 to run
```

**Solution:**

```kotlin
// build.gradle.kts
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

---

## Runtime Errors

### Error: "ClassNotFoundException"

**Symptom:**
```
java.lang.ClassNotFoundException: com.fattmerchant.omni.Omni
```

**Cause:** ProGuard removed SDK classes

**Solution:**

```proguard
# proguard-rules.pro

# Keep all Stax SDK classes
-keep class com.fattmerchant.** { *; }
-keep interface com.fattmerchant.** { *; }
-keepclassmembers class com.fattmerchant.** { *; }

# Keep payment processor SDK classes
-keep class com.creditcall.** { *; }

# Keep model classes
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
```

### Error: "Context leaked"

**Symptom:**
```
Activity has leaked IntentReceiver
```

**Cause:** Not cleaning up listeners

**Solution:**

```kotlin
class PaymentActivity : AppCompatActivity() {
    
    private val transactionListener = object : TransactionUpdateListener {
        override fun onTransactionUpdate(update: TransactionUpdate) {
            // Handle update
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Register listener
        Omni.shared()?.transactionUpdateListener = transactionListener
    }
    
    override fun onPause() {
        super.onPause()
        // Clean up listener
        Omni.shared()?.transactionUpdateListener = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up all listeners
        Omni.shared()?.transactionUpdateListener = null
        Omni.shared()?.userNotificationListener = null
    }
}
```

---

## Getting Help

### Contact Stax Support

- **Developer Support:** developer@staxpayments.com
- **Technical Issues:** support@staxpayments.com
- **Documentation:** https://www.staxpayments.com/developers
- **Phone:** 1-800-XXX-XXXX

### Information to Include

When contacting support, provide:

```kotlin
fun generateSupportInfo(): String {
    return """
        SDK VERSION: 2.7.0
        APP VERSION: ${BuildConfig.VERSION_NAME}
        
        DEVICE INFO:
        - Manufacturer: ${Build.MANUFACTURER}
        - Model: ${Build.MODEL}
        - Android Version: ${Build.VERSION.RELEASE}
        - SDK Int: ${Build.VERSION.SDK_INT}
        
        NFC INFO:
        - Has NFC: ${hasNfcCapability()}
        - NFC Enabled: ${isNfcEnabled()}
        
        NETWORK INFO:
        - Connected: ${diagnoseNetworkIssue().isConnected}
        - Type: ${diagnoseNetworkIssue().transportType}
        
        ERROR DETAILS:
        - Error message: [paste error message]
        - Stack trace: [paste stack trace]
        - Steps to reproduce: [describe steps]
        
        TRANSACTION INFO:
        - Transaction ID: [if available]
        - Amount: [if relevant]
        - Card Type: [if known]
    """.trimIndent()
}
```

---

## Next Steps

- **[API Reference](09-api-reference.md)** - Complete API documentation
- **[Testing](06-testing.md)** - Test comprehensive scenarios
- **[Getting Started](01-getting-started.md)** - Review setup guide
