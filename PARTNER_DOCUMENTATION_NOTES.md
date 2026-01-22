# Partner Documentation Guide: Stax Android SDK

This document provides guidance on what partner developers need to know about the Stax Android SDK. Our SDK abstracts payment processor implementation details and handles authentication, entitlements, and SDK initialization automatically.

**Key Principle:** Partners interact only with Stax—they never need to contact payment processors, manage certificates, or handle low-level payment processor SDKs directly.

---

## 📄 Overview for Partners

### What Partners DON'T Need to Do:

1. ❌ **Download separate payment processor SDKs** - Everything is bundled
2. ❌ **Configure payment processor settings** - Handled by our SDK
3. ❌ **Manage certificates or signing keys** - Extracted automatically
4. ❌ **Handle Play Integrity Keys** - Managed on backend
5. ❌ **Contact payment processors** - All support through Stax
6. ❌ **Configure API keys with processors** - Backend handles authentication
7. ❌ **Manage different SDKs for Tap vs external readers** - One unified API
8. ❌ **Calculate certificate fingerprints** - SDK extracts from app signing
9. ❌ **Email processor onboarding teams** - Stax handles all onboarding
10. ❌ **Configure App IDs or Application Identifiers** - Managed internally

### What Partners DO Need to Do:

1. ✅ **Add Maven dependency** - `com.fattmerchant:cardpresent:2.7.0`
2. ✅ **Initialize SDK with Stax API key** - Simple initialization
3. ✅ **Declare Android permissions** - Standard NFC/Bluetooth permissions
4. ✅ **Call transaction methods** - Simple unified API
5. ✅ **Comply with EMVCo branding** - Contactless symbol requirements

---

## 📄 SDK Installation

### Step 1: Add Dependency

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.fattmerchant:cardpresent:2.7.0")
    
    // Optional: If using tokenization features
    implementation("com.fattmerchant:tokenization:2.7.0")
}
```

The SDK is available on Maven Central—no additional repositories needed!

### Step 2: Declare Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.NFC" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<uses-feature
    android:name="android.hardware.nfc"
    android:required="false" />
```

### Step 3: Initialize SDK

Initialize the Stax SDK in your Application class:

```kotlin
import com.fattmerchant.android.InitParams
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.Environment

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val params = InitParams(
            context = applicationContext,
            application = this,
            apiKey = "your_ephemeral_api_key"
        )
        
        Omni.initialize(
            params = params,
            completion = {
                Log.d("Stax", "SDK initialized successfully")
            },
            error = { exception ->
                Log.e("Stax", "Initialization failed: ${exception.message}")
            }
        )
    }
}
```

**Important Notes:**
- Use an **ephemeral API key** for security
- The SDK handles all payment processor authentication and configuration automatically
- No need to manage Play Integrity Keys, App IDs, or certificate fingerprints
- Entitlements and attestation are handled on the backend

---

## 📄 Payment Methods: Tap vs External Readers

### Unified Transaction API

Partners use ONE method for both payment types:

```kotlin
import com.fattmerchant.omni.data.ReaderType

// Explicit control over payment method
Omni.shared()?.takeMobileReaderTransaction(
    request = request,
    readerType = ReaderType.TAP_TO_PAY,  // or AUTO (default), or EXTERNAL_READER
    completion = { transaction -> },
    error = { exception -> }
)
```

### How Payment Method Selection Works

You can explicitly specify which reader to use for each transaction:

#### ReaderType Options

| ReaderType | Description |
|------------|-------------|
| `TAP_TO_PAY` | Use device's NFC (Tap to Pay) |
| `EXTERNAL_READER` | Use Bluetooth/USB card reader |
| `AUTO` | Let SDK decide based on connected reader (default) |

#### Auto Selection Logic (When readerType = AUTO)

- **Only Tap reader connected** → Uses NFC Tap to Pay
- **Only external reader connected** → Uses external reader
- **Both connected** → Prefers external reader
- **Neither connected** → Transaction fails

### Checking Current Payment Method

```kotlin
val connectedReader: MobileReader? = Omni.shared()?.getConnectedReader()

if (connectedReader != null) {
    println("Connected: ${connectedReader.name}")
    // Could be "Tap" or external reader name like "BBPOS Chipper 2X BT"
} else {
    println("No reader connected")
}
```

### Connecting to Tap (NFC)

```kotlin
import com.fattmerchant.omni.data.TapToPayReader

// Create Tap reader instance
val tapReader = TapToPayReader(
    testMode = false  // Use true for sandbox
)

// Connect to Tap reader
Omni.shared()?.connectReader(
    mobileReader = tapReader,
    onConnected = { reader ->
        Log.d("Stax", "Tap reader connected: ${reader.name}")
        // Tap reader ready - can now accept NFC payments
    },
    onFail = { error ->
        Log.e("Stax", "Connection failed: $error")
    }
)
```

**Important:** The Tap reader (name: "Tap") must be explicitly connected before use. It does NOT automatically activate when external readers disconnect.

### Connecting to External Readers (Optional)

```kotlin
import com.fattmerchant.omni.usecase.SearchForReaders
import com.fattmerchant.omni.usecase.ConnectMobileReader

// Search for nearby Bluetooth readers
val searchUseCase = SearchForReaders(Omni.shared()!!)
searchUseCase.start { reader ->
    // Found a reader
    println("Found: ${reader.name}")
    
    // Connect to it
    val connectUseCase = ConnectMobileReader(Omni.shared()!!)
    connectUseCase.connect(
        reader = reader,
        completion = {
            println("Reader connected! Future transactions will use this reader.")
        },
        error = { exception ->
            println("Connection failed: ${exception.message}")
        }
    )
}

searchUseCase.onError { exception ->
    println("Search failed: ${exception.message}")
}
```

### Disconnecting Readers

```kotlin
import com.fattmerchant.omni.usecase.DisconnectMobileReader

val disconnectUseCase = DisconnectMobileReader(Omni.shared()!!)
disconnectUseCase.disconnect(
    completion = {
        println("Reader disconnected")
    },
    error = { exception ->
        println("Disconnect failed: ${exception.message}")
    }
)
```

---

## 📄 Processing Transactions

### Basic Transaction with Explicit Reader Type

```kotlin
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.ReaderType

fun processPayment(amount: Double, useTap: Boolean = true) {
    val request = TransactionRequest(
        amount = Amount(amount),
        tokenize = true  // Optional: tokenize for future use
    )
    
    Omni.shared()?.takeMobileReaderTransaction(
        request = request,
        readerType = if (useTap) ReaderType.TAP_TO_PAY else ReaderType.EXTERNAL_READER,
        completion = { transaction ->
            if (transaction.success == true) {
                println("Transaction approved: ${transaction.id}")
            } else {
                println("Transaction declined")
            }
        },
        error = { exception ->
            println("Transaction error: ${exception.message}")
        }
    )
}
```

### Transaction with Updates

```kotlin
import com.fattmerchant.omni.TransactionUpdateListener

Omni.shared()?.apply {
    // Listen to transaction events
    transactionUpdateListener = object : TransactionUpdateListener {
        override fun onTransactionUpdate(update: TransactionUpdate) {
            when (update.value) {
                "Prompt Insert Card", "Prompt Swipe Card" -> {
                    // Show "Tap Card" or "Insert Card" UI
                }
                "Card Inserted", "Card Swiped" -> {
                    // Show "Card Detected" feedback
                }
                "Authorizing" -> {
                    // Show "Processing..." UI
                }
            }
        }
    }
    
    takeMobileReaderTransaction(request, completion, error)
}
```

### Transaction Results

All transactions return a `Transaction` object with:

- `success: Boolean` - Whether transaction was approved
- `id: String` - Unique transaction identifier
- `last_four: String` - Card last 4 digits
- `card_type: String` - Card brand (VISA, MASTERCARD, etc.)
- `total: Double` - Transaction amount

---

## 📄 Requirements and Restrictions

### SDK Requirements

- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14+ (API 34) recommended
- **Gradle**: 7.0+
- **Java**: 11+

### Device Requirements for Tap to Pay

- NFC-enabled Android device
- Android 8.0 or higher
- Internet connection (always online)
- Location services enabled

### Development Environment

No special restrictions during development:
- Developer options can remain enabled
- USB debugging is allowed
- Standard Android development workflow

### Production Requirements

For production deployment:
- App must be published to Google Play Store
- Provide Stax with your app's Package Name
- App must be signed with production signing key

---

## 📄 Compliance & Branding

### EMVCo Contactless Symbol Requirements

**CRITICAL:** Your app MUST display the EMVCo Contactless Symbol during Tap to Pay transactions.

#### Required Display

When prompting for card tap:
```
┌─────────────────────┐
│   Tap Card to Pay   │
│                     │
│        ))))         │  ← Contactless Symbol
│                     │
└─────────────────────┘
```

#### Symbol Placement Rules

- Display symbol prominently on payment screen
- Ensure symbol is clearly visible during entire transaction
- Use official EMVCo contactless symbol assets
- Maintain symbol aspect ratio and proportions

#### Download Official Assets

Get the official EMVCo contactless symbol from:
https://www.emvco.com/resources/emvco-branding-guidelines/

### Card Brand Logo Requirements

Display logos for supported card brands:
- Visa
- Mastercard
- American Express
- Discover

---

## 📄 What Stax Handles Automatically

Partners never need to manage:

1. **Payment Processor Integration**
   - NMI SDK integration
   - ChipDNA configuration
   - API authentication with processors
   - Network endpoints and IPs

2. **Security & Attestation**
   - Play Integrity Keys
   - Certificate fingerprints (extracted from app signing automatically)
   - Keystore hashes
   - App attestation with processors

3. **Configuration**
   - App IDs and Application Identifiers
   - Environment setup (test/production)
   - TapToMobilePOI and PaymentDevicePOI parameters
   - connectAndConfigure() calls

4. **Onboarding**
   - Payment processor app onboarding
   - Email communications with processors
   - Backend entitlements
   - Merchant account setup with processors

---

## 📄 Support & Resources

### For Partners

- **Technical Support**: Contact your Stax account manager
- **API Documentation**: https://docs.staxpayments.com
- **SDK Issues**: Submit through Stax support channels

### What Partners Should NOT Do

- ❌ Contact payment processors directly (NMI, etc.)
- ❌ Email processor onboarding teams
- ❌ Attempt to configure processor SDKs manually
- ❌ Manage processor API keys or certificates
- ❌ Modify payment processor settings

---

## 📄 Migration from NMI Direct Integration

If migrating from direct NMI integration to Stax SDK:

### What Changes

| Before (NMI Direct) | After (Stax SDK) |
|---------------------|------------------|
| Download NMI .aar file | Use Maven dependency |
| Configure App ID | Automatic |
| Calculate certificate fingerprint | Automatic |
| Email NMI onboarding | Stax handles |
| Extend ChipDnaApplication | Not required |
| Call connectAndConfigure() | Not required |
| Set TapToMobilePOI parameters | Use TapToPayConfiguration |
| Manage multiple SDKs | Single unified API |

### What Stays the Same

- Android permissions requirements
- EMVCo branding compliance
- Card brand logo requirements
- Transaction flow and events
- Receipt handling

---

## 📄 Code Examples

### Complete Integration Example

```kotlin
// 1. Application initialization
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        Omni.initialize(
            params = InitParams(
                context = applicationContext,
                application = this,
                apiKey = "your_ephemeral_api_key"
            ),
            completion = { Log.d("Stax", "Ready") },
            error = { Log.e("Stax", "Error: ${it.message}") }
        )
    }
}

// 2. Connect Tap reader
class PaymentActivity : AppCompatActivity() {
    
    fun connectTapReader() {
        val tapReader = TapToPayReader(testMode = false)
        
        Omni.shared()?.connectReader(
            mobileReader = tapReader,
            onConnected = { reader ->
                Log.d("Stax", "Tap reader ready: ${reader.name}")
            },
            onFail = { error ->
                Log.e("Stax", "Connection failed: $error")
            }
        )
    }
    
    // 3. Process payment with explicit reader type
    fun processPayment(amount: Double, useTapToPay: Boolean) {
        val request = TransactionRequest(Amount(amount))
        
        val readerType = if (useTapToPay) {
            ReaderType.TAP_TO_PAY
        } else {
            ReaderType.EXTERNAL_READER
        }
        
        Omni.shared()?.takeMobileReaderTransaction(
            request = request,
            readerType = readerType,
            completion = { transaction ->
                if (transaction.success == true) {
                    showSuccess(transaction)
                } else {
                    showDeclined()
                }
            },
            error = { exception ->
                showError(exception)
            }
        )
    }
}
```

---

## 📄 Testing

### Test Card Simulator

Partners DO NOT need the NMI test card simulator app.

### Testing with Real Cards

Use test cards provided by Stax:
- Test Visa
- Test Mastercard
- Test American Express

### Test API Keys

Use test API keys provided by Stax for development and testing.

---

## 📄 ProGuard Rules

If using ProGuard/R8, add these rules to `proguard-rules.pro`:

```proguard
# Stax SDK
-keep class com.fattmerchant.** { *; }
-keep class com.staxpayments.** { *; }

# Payment processor SDK (managed by Stax)
-keep class creditcall.** { *; }
-dontwarn creditcall.**
```

---

## 📄 Summary

**Partner Integration = Simple**

1. Add `com.fattmerchant:cardpresent:2.7.0` dependency
2. Declare Android permissions
3. Initialize with Stax API key
4. Connect reader (Tap or external)
5. Call `takeMobileReaderTransaction()`

**Stax Handles = Everything Else**

- Payment processor SDKs
- Certificates and attestation
- App onboarding with processors
- Network configuration
- Security and compliance

Partners focus on building great apps. Stax handles the payment complexity.
