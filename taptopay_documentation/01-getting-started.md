# Getting Started with Stax Android SDK

Welcome! This guide will help you integrate Tap to Pay functionality into your Android app using the Stax SDK.

> **⚠️ IMPORTANT - Read Before Starting:**
> 1. **Tap to Pay Configuration Required:** You MUST configure `TapToPayConfiguration` during initialization to enable NFC payments
> 2. **UI Components Included:** The SDK includes reference UI components (like `TapToPayPrompt`) in the sample app that you can use or customize

## What is Tap to Pay?

Tap to Pay on Android allows merchants to accept contactless payments directly on their Android device using the built-in NFC reader. No external hardware required—customers can simply tap their contactless card, Apple Pay, Google Pay, or other digital wallets on the merchant's phone.

### Benefits

- ✅ **No Hardware Required** - Uses the device's built-in NFC reader
- ✅ **Fast Setup** - Accept payments in minutes
- ✅ **Secure** - EMV-compliant, PCI-certified
- ✅ **Lower Costs** - No need to purchase card readers
- ✅ **Flexible** - Works alongside external card readers if needed

---

## Requirements

### Device Requirements

- **Android Version**: Android 8.0 (API 26) or higher
- **NFC Hardware**: Device must have NFC capability
- **Internet Connection**: Always online (required for transaction processing)
- **Location Services**: Must be enabled for fraud prevention

### Development Requirements

- **Gradle**: 7.0 or higher
- **Java**: 11 or higher
- **Target SDK**: Android 14+ (API 34) recommended
- **Kotlin**: 1.8+ recommended

### Business Requirements

- **Stax Merchant Account** - Contact your account manager to enable Tap to Pay
- **API Credentials** - Obtain API keys from the Stax Dashboard
- **Google Play** - Production apps must be published to Google Play Store

---

## Installation

### Step 1: Add Dependency

Add the Stax SDK to your app's `build.gradle`:

```gradle
dependencies {
    implementation 'com.fattmerchant:cardpresent:2.7.0'
    
    // Optional: If using tokenization features
    implementation 'com.fattmerchant:tokenization:2.7.0'
}
```

The SDK is published to Maven Central, which is included by default in Android projects.

### Step 2: Sync Project

Sync your Gradle files:
```bash
./gradlew sync
```

---

## Declare Permissions

Add required permissions to your `AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Required for Tap to Pay -->
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    
    <!-- Optional: For external Bluetooth readers -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <!-- Declare NFC feature (not required, allows non-NFC devices to install) -->
    <uses-feature
        android:name="android.hardware.nfc"
        android:required="false" />

    <application
        android:name=".MyApplication"
        ...>
        <!-- Your activities -->
    </application>
</manifest>
```

### Runtime Permissions

Request location permission at runtime (required for Android 6.0+):

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request location permission
        if (ContextCompat.checkSelfPermission(this, 
                Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted - can proceed with Tap to Pay
                initializeSDK()
            } else {
                // Permission denied - show explanation
                showLocationPermissionRequired()
            }
        }
    }
}
```

---

## Initialize the SDK

### Option 1: In Application Class (Recommended)

Create or modify your Application class:

```kotlin
import android.app.Application
import android.util.Log
import com.fattmerchant.android.InitParams
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.Environment
import com.fattmerchant.omni.data.TapToPayConfiguration

class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // REQUIRED: Configure Tap to Pay
        val tapToPayConfig = TapToPayConfiguration.hybrid(
            testMode = true,  // false for production
            mockMode = false  // true only for UI testing during development
        )
        
        // Initialize Stax SDK
        val params = InitParams(
            appContext = applicationContext,
            application = this,
            apiKey = getEphemeralApiKey(), // Your API key
            environment = Environment.DEV,
            tapToPayConfig = tapToPayConfig  // REQUIRED for NFC
        )
        
        Omni.initialize(
            params = params,
            completion = {
                Log.d("Stax", "SDK initialized successfully")
                // SDK is ready to accept payments
            },
            error = { exception ->
                Log.e("Stax", "SDK initialization failed: ${exception.message}", exception)
                // Handle initialization error
            }
        )
    }
    
    private fun getEphemeralApiKey(): String {
        // In production, fetch ephemeral key from your backend
        // Never hardcode production API keys in your app
        return "your_ephemeral_api_key"
    }
}
```

Register your Application class in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
```

---

## Connect to Tap to Pay

### Connecting to the Tap Reader

Before processing transactions, connect to the Tap to Pay reader:

```kotlin
import com.fattmerchant.omni.data.TapToPayReader

// Create Tap reader instance
val tapReader = TapToPayReader(
    testMode = true  // Use false for production
)

// Connect to reader
Omni.shared()?.connectReader(
    mobileReader = tapReader,
    onConnected = { reader ->
        println("Tap reader connected: ${reader.name}")
        // Reader is ready to accept payments
    },
    onFail = { error ->
        println("Connection failed: $error")
    }
)
```

### Reader Parameters

| Parameter | Description | Development | Production |
|-----------|-------------|-------------|------------|
| `testMode` | Uses sandbox environment | `true` | `false` |

### Specifying Payment Method

You can explicitly specify which reader to use when processing transactions:

```kotlin
import com.fattmerchant.omni.data.ReaderType
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.Amount

val request = TransactionRequest(Amount(25.00))

// Option 1: Explicitly use Tap to Pay (NFC)
Omni.shared()?.takeMobileReaderTransaction(
    request = request,
    readerType = ReaderType.TAP_TO_PAY,
    completion = { transaction -> },
    error = { exception -> }
)

// Option 2: Explicitly use external reader
Omni.shared()?.takeMobileReaderTransaction(
    request = request,
    readerType = ReaderType.EXTERNAL_READER,
    completion = { transaction -> },
    error = { exception -> }
)

// Option 3: Auto-select based on connected reader (default)
Omni.shared()?.takeMobileReaderTransaction(
    request = request,
    readerType = ReaderType.AUTO,  // This is the default
    completion = { transaction -> },
    error = { exception -> }
)
```

### ReaderType Options

| ReaderType | Description |
|------------|-------------|
| `TAP_TO_PAY` | Use device's NFC (Tap to Pay) |
| `EXTERNAL_READER` | Use Bluetooth/USB card reader |
| `AUTO` | Let SDK decide based on connected reader (default) |

**Auto Selection Logic:**
- If Tap reader connected but no external reader → Uses Tap to Pay
- If external reader connected → Uses external reader
- If both connected → Prefers external reader
- If neither connected → Transaction fails

---

## UI Components

### Built-In TapToPayPrompt

The SDK now includes **TapToPayPrompt**, a fully integrated Jetpack Compose UI component:

#### Features
- ✅ **Automatic Transaction Handling** - Sets up listeners and processes payments automatically
- ✅ **Real-Time Status Updates** - Shows transaction progress ("Reading card...", "Authorizing...")
- ✅ **Visual Feedback** - Processing indicators, success checkmarks, error states
- ✅ **Professional Design** - Circular NFC prompt with animated gradient, payment card logos
- ✅ **Theme Support** - Automatic Light/Dark mode adaptation
- ✅ **Self-Contained** - No manual listener management needed

#### Quick Start

```kotlin
import com.fattmerchant.omni.ui.TapToPayPrompt

@Composable
fun PaymentScreen() {
    TapToPayPrompt(
        amount = "28.00",
        subtotal = "25.00",
        tip = "3.00",
        transactionRequest = request,  // Optional, builds from amount if null
        onSuccess = { transaction ->
            // Payment approved!
            Log.d("Payment", "Transaction ID: ${transaction.id}")
        },
        onError = { errorMessage ->
            // Payment failed
            Log.e("Payment", "Error: $errorMessage")
        },
        onCancel = {
            // User cancelled
        }
    )
}
```

#### How It Works

1. **Shows UI** - Displays animated NFC prompt
2. **Starts Transaction** - Automatically calls `takeMobileReaderTransaction()`
3. **Updates Status** - Shows real-time messages ("Card detected", "Authorizing...")
4. **Handles Completion** - Shows success ✓ or error ✗, calls your callback
5. **Cleans Up** - Removes listeners automatically when dismissed

**No need to manually set up TransactionUpdateListener** - the component handles everything!

For detailed documentation, see [TapToPayPrompt README](../cardpresent/src/main/java/com/fattmerchant/omni/ui/README_TAP_TO_PAY_UI.md).

### Core SDK Functionality

The SDK handles:
- ✅ Card communication (NFC/Bluetooth)
- ✅ Transaction processing
- ✅ Communication with payment processor
- ✅ Transaction status callbacks

### Building Your Own UI (Optional)

If you want custom UI instead of using the included components, the SDK provides **callbacks** that tell you what's happening:

```kotlin
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.data.TransactionUpdate

// Set up listener to receive updates
Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
    override fun onTransactionUpdate(update: TransactionUpdate) {
        // Update YOUR UI based on the status
        when (update.value) {
            "Prompt Insert Card" -> showYourCardPromptUI()
            "Reading Card" -> showYourProgressUI("Reading card...")
            "Authorizing" -> showYourProgressUI("Authorizing...")
            "Authorized" -> showYourSuccessUI()
        }
    }
}

// Process transaction
val request = TransactionRequest(Amount(25.00))

Omni.shared()?.takeMobileReaderTransaction(
    request,
    completion = { transaction ->
        // Show YOUR success/decline screen
        if (transaction.success == true) {
            showYourSuccessScreen(transaction)
        } else {
            showYourDeclineScreen(transaction.message)
        }
    },
    error = { exception ->
        // Show YOUR error screen
        showYourErrorScreen(exception.message)
    }
)
```

See **[UI/UX Guidelines](04-ui-ux-guidelines.md)** for complete UI implementation examples.

Register your Application class in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
```

### Option 2: In Activity

If you don't want to use an Application class:

```kotlin
import androidx.appcompat.app.AppCompatActivity
import com.fattmerchant.android.InitParams
import com.fattmerchant.android.Omni

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeSDK()
    }
    
    private fun initializeSDK() {
        val params = InitParams(
            context = applicationContext,
            application = application,
            apiKey = "your_ephemeral_api_key"
        )
        
        Omni.initialize(params,
            completion = {
                // SDK ready
                enablePaymentButtons()
            },
            error = { exception ->
                // Handle error
                showError("Failed to initialize SDK: ${exception.message}")
            }
        )
    }
}
```

---

## Understanding Payment Methods

The Stax SDK supports two payment methods through a single unified API:

### 1. Tap to Pay (NFC)
Uses the device's built-in NFC reader. No external hardware required.

**When to use:**
- Most common use case
- Lower cost (no hardware purchase)
- Faster checkout experience
- Mobile/on-the-go payments

### 2. External Card Readers
Bluetooth or USB mobile card readers for traditional card insertion/swiping.

**When to use:**
- Environments where NFC may not work reliably
- Need for backup payment method
- Support for non-contactless cards

### Explicit Payment Method Selection

You control which payment method to use:

```kotlin
import com.fattmerchant.omni.data.ReaderType

// Explicitly use Tap to Pay
Omni.shared()?.takeMobileReaderTransaction(
    request = request,
    readerType = ReaderType.TAP_TO_PAY,
    completion = { transaction -> },
    error = { exception -> }
)

// Or let SDK auto-select (omit readerType parameter, defaults to AUTO)
Omni.shared()?.takeMobileReaderTransaction(
    request = request,
    completion = { transaction -> },
    error = { exception -> }
)
```

**Selection Logic:**
1. If only Tap reader connected → Uses Tap to Pay (NFC)
2. If external reader connected → Uses external reader  
3. If both connected → Prefers external reader
4. If neither connected → Transaction fails

**Key Point:** You must connect to a reader before processing transactions. Use the `readerType` parameter to explicitly control which reader to use.

#### Listening for Reader Connection Changes

To update your UI when readers connect/disconnect:

```kotlin
import com.fattmerchant.omni.MobileReaderConnectionStatusListener
import com.fattmerchant.omni.data.MobileReaderConnectionStatus

Omni.shared()?.mobileReaderConnectionStatusListener = 
    object : MobileReaderConnectionStatusListener {
        override fun mobileReaderConnectionStatusUpdate(
            status: MobileReaderConnectionStatus
        ) {
            when (status) {
                MobileReaderConnectionStatus.CONNECTED -> {
                    // A reader connected (Tap OR external)
                    // Check which type to update UI
                    val reader = Omni.shared()?.getConnectedReader()
                    if (reader?.name == "Tap") {
                        updateUI("Ready: Tap")
                    } else {
                        updateUI("Ready: ${reader?.name ?: "Reader"}")
                    }
                }
                MobileReaderConnectionStatus.DISCONNECTED -> {
                    // A reader disconnected
                    updateUI("No reader connected")
                }
                MobileReaderConnectionStatus.CONNECTING -> {
                    updateUI("Connecting to reader...")
                }
                else -> {
                    updateUI("Reader status: $status")
                }
            }
        }
    }
```

#### Dynamic UI Example

```kotlin
@Composable
fun PaymentMethodIndicator() {
    val connectedReader = remember { Omni.shared()?.getConnectedReader() }
    
    if (connectedReader != null) {
        Text("💳 External Reader: ${connectedReader.name}")
    } else {
        Text("📱 Tap to Pay Ready")
    }
}
```

---

## Verify NFC Availability

Check if the device supports NFC:

```kotlin
import android.nfc.NfcAdapter

fun checkNfcSupport(): Boolean {
    val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    
    return when {
        nfcAdapter == null -> {
            // Device doesn't support NFC
            showError("This device doesn't support Tap to Pay")
            false
        }
        !nfcAdapter.isEnabled -> {
            // NFC is disabled
            showError("Please enable NFC in device settings")
            // Optionally: Open NFC settings
            // startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            false
        }
        else -> {
            // NFC is available and enabled
            true
        }
    }
}
```

---

## API Key Management

### Development/Testing

For development, you can use test API keys:

```kotlin
private fun getApiKey(): String {
    return if (BuildConfig.DEBUG) {
        "your_test_api_key"
    } else {
        fetchEphemeralKeyFromBackend()
    }
}
```

### Production (Recommended)

**NEVER hardcode production API keys in your app.**

Fetch ephemeral tokens from your backend:

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.http.POST

interface StaxBackendApi {
    @POST("api/generate-ephemeral-key")
    suspend fun getEphemeralKey(): EphemeralKeyResponse
}

data class EphemeralKeyResponse(val ephemeralKey: String)

suspend fun fetchEphemeralKeyFromBackend(): String {
    return withContext(Dispatchers.IO) {
        val api = Retrofit.Builder()
            .baseUrl("https://your-backend.com/")
            .build()
            .create(StaxBackendApi::class.java)
        
        val response = api.getEphemeralKey()
        response.ephemeralKey
    }
}
```

---

## Troubleshooting Setup

### SDK Initialization Fails

**Problem:** SDK initialization returns an error

**Solutions:**
- Verify API key is valid
- Check internet connection
- Ensure all required permissions are granted
- Verify device meets minimum requirements (Android 8.0+)

### NFC Not Working

**Problem:** NFC transactions fail or aren't detected

**Solutions:**
- Check NFC is enabled in device settings
- Verify device has NFC hardware
- Ensure `android.permission.NFC` permission is declared
- Test on a different NFC-capable device

### Compilation Errors

**Problem:** Gradle sync or build fails

**Solutions:**
- Verify Gradle version is 7.0+
- Check Java version is 11+
- Ensure Maven Central repository is accessible
- Clean and rebuild: `./gradlew clean build`

---

## Next Steps

Now that you have the SDK installed and initialized, you're ready to:

1. **[Process Your First Transaction](02-your-first-transaction.md)** - Learn how to accept a payment
2. **[Transaction Management](03-transaction-management.md)** - Handle complex payment scenarios
3. **[UI/UX Guidelines](04-ui-ux-guidelines.md)** - Create a great payment experience

---

## Getting Help

### Documentation
- [Transaction Management](03-transaction-management.md)
- [UI/UX Guidelines](04-ui-ux-guidelines.md)
- [API Reference](09-api-reference.md)

### Support
- **Email**: sdk-support@staxpayments.com
- **Documentation**: https://docs.staxpayments.com/docs/mobile-sdk
- **GitHub**: https://github.com/fattmerchantorg/fattmerchant-android-sdk

### Account & API Keys
- Contact your Stax Account Manager
- Dashboard: https://dashboard.staxpayments.com

---

**Note:** All SDK support is provided by Stax. Contact your Stax Account Manager for assistance.
