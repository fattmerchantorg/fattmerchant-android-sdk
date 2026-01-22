# Stax Tap to Pay SDK Documentation

Complete guide to integrating contactless card payments in your Android app.

---

## 📚 Documentation

### Getting Started

1. **[Getting Started](01-getting-started.md)**
   - What is Tap to Pay
   - Requirements
   - Installation
   - SDK initialization
   - API keys

2. **[Your First Transaction](02-your-first-transaction.md)**
   - Quick start guide
   - Complete transaction flow
   - Activity implementation
   - ViewModel pattern
   - Jetpack Compose UI

### Core Functionality

3. **[Transaction Management](03-transaction-management.md)**
   - Transaction types (sale, pre-auth)
   - Void transactions
   - Refunds (full and partial)
   - Tokenization
   - Tips and metadata

4. **[UI/UX Guidelines](04-ui-ux-guidelines.md)**
   - Design principles
   - Transaction flow UI
   - Card prompts
   - Success and error states
   - Accessibility

### Compliance & Testing

5. **[Compliance & Certification](05-compliance.md)**
   - EMVCo requirements
   - Card brand logos
   - Contactless symbols
   - Receipt requirements
   - PCI DSS compliance

6. **[Testing Guide](06-testing.md)**
   - Test environment setup
   - Test cards
   - Testing scenarios
   - Device testing
   - Automated testing

### Production

7. **[Production Deployment](07-production-deployment.md)**
   - Pre-launch checklist
   - Google SDK Console
   - Stax merchant onboarding
   - Google Play requirements
   - Go-live process

8. **[Troubleshooting](08-troubleshooting.md)**
   - SDK initialization issues
   - NFC problems
   - Transaction failures
   - Network errors
   - Build issues

### Reference

9. **[API Reference](09-api-reference.md)**
   - Omni class
   - TransactionRequest
   - Transaction
   - Use cases
   - Listeners
   - Exceptions

---

## 🚀 Quick Start

### 1. Install SDK

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.fattmerchant:cardpresent:2.7.0")
}
```

### 2. Connect to Tap to Pay and Initialize SDK

```kotlin
import com.fattmerchant.omni.data.TapToPayReader

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val initParams = InitParams(
            appContext = this,
            application = this,
            apiKey = "your_api_key",
            environment = Environment.DEV
        )
        
        Omni.initialize(
            params = initParams,
            completion = {
                // SDK initialized - now connect to Tap reader
                connectToTapReader()
            },
            error = { /* handle error */ }
        )
    }
    
    private fun connectToTapReader() {
        val tapReader = TapToPayReader(testMode = true)
        
        Omni.shared()?.connectReader(
            mobileReader = tapReader,
            onConnected = { reader ->
                Log.d(\"SDK\", \"Tap reader ready: ${reader.name}\")
            },
            onFail = { error ->
                Log.e(\"SDK\", \"Connection failed: $error\")
            }
        )
    }
}
```

### 3. Build Payment UI and Process Payment

> **Important:** The SDK does not provide UI screens. You must build your own using transaction updates.

```kotlin
// Set up transaction update listener
Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
    override fun onTransactionUpdate(update: TransactionUpdate) {
        // Update your UI based on status
        when (update.value) {
            "Prompt Insert Card" -> showCardPrompt()
            "Reading Card" -> showProgress("Reading card...")
            "Authorizing" -> showProgress("Authorizing...")
        }
    }
}

// Process payment with explicit reader type
import com.fattmerchant.omni.data.ReaderType

val request = TransactionRequest(Amount(25.00))

Omni.shared()?.takeMobileReaderTransaction(
    request,
    readerType = ReaderType.TAP_TO_PAY,  // or AUTO (default), or EXTERNAL_READER
    completion = { transaction ->
        if (transaction.success == true) {
            showSuccessScreen(transaction)  // Your UI
        } else {
            showErrorScreen(transaction.message)  // Your UI
        }
    },
    error = { exception ->
        showErrorScreen(exception.message)  // Your UI
    }
)
```

---

## 📖 Documentation Path

### For New Users

Follow this path to get started quickly:

1. Read **[Getting Started](01-getting-started.md)** for setup
2. Follow **[Your First Transaction](02-your-first-transaction.md)** tutorial
3. Review **[UI/UX Guidelines](04-ui-ux-guidelines.md)** for design
4. Check **[Testing Guide](06-testing.md)** before deploying

### For Production Launch

Complete these steps for production deployment:

1. Review **[Compliance & Certification](05-compliance.md)**
2. Complete **[Testing Guide](06-testing.md)** checklist
3. Follow **[Production Deployment](07-production-deployment.md)**
4. Keep **[Troubleshooting](08-troubleshooting.md)** handy

### For Developers

Quick reference materials:

- **[API Reference](09-api-reference.md)** - Complete API docs
- **[Transaction Management](03-transaction-management.md)** - Voids, refunds
- **[Troubleshooting](08-troubleshooting.md)** - Common issues

---

## 💡 Key Concepts

### Included UI Components

The SDK includes production-ready UI components in the sample app:

- **TapToPayPrompt** - Professional NFC payment screen
  - Circular gradient prompt
  - Payment card logos
  - Amount breakdown
  - Light/Dark theme support
  - Based on Figma designs

Location: `app/src/main/java/com/staxpayments/sample/ui/components/`

### Single Method for All Payment Types

The SDK provides one method for all payment types:

```kotlin
Omni.shared()?.takeMobileReaderTransaction(request, ...)
```

This method automatically:
- Uses **external card reader** if one is connected
- Falls back to **Tap to Pay (NFC)** if no reader connected
- No need to manually check or switch payment methods

### Payment Method Routing

The SDK handles payment method selection automatically:

```kotlin
val reader = Omni.shared()?.getConnectedReader()
if (reader != null) {
    // Will use external reader
} else {
    // Will use device NFC (Tap to Pay)
}

// You don't need to do anything different!
Omni.shared()?.takeMobileReaderTransaction(request, ...)
```

### Simplified Integration

No need to:
- ❌ Download payment processor SDKs separately
- ❌ Configure payment processor settings
- ❌ Manage certificates or signing keys
- ❌ Handle different payment methods separately

Just:
- ✅ Add Maven Central dependency
- ✅ Initialize with API key
- ✅ Call `takeMobileReaderTransaction()`
- ✅ Handle success/error callbacks

---

## 🔑 Key Features

- **Tap to Pay** - Accept contactless payments with device NFC
- **External Readers** - Support Bluetooth/USB card readers
- **Automatic Routing** - SDK selects payment method automatically
- **Ready-to-Use UI** - Includes TapToPayPrompt component with professional design
- **All Card Brands** - Visa, Mastercard, Amex, Discover
- **Refunds & Voids** - Full transaction management
- **Tokenization** - Save cards for future payments
- **Pre-Authorization** - Auth and capture workflows
- **PCI Compliant** - Secure by default

---

## 📱 Requirements

- **Android 9.0** (API 28) or higher
- **NFC-enabled** device for Tap to Pay
- **Stax merchant account** with API keys
- **Google SDK Console** registration for production

---

## 🆘 Support

### Documentation

- 📚 Complete docs in this folder
- 🔍 Use search to find specific topics
- 💡 Check troubleshooting for common issues

### Stax Support

- **Developer Support:** developer@staxpayments.com
- **Technical Issues:** support@staxpayments.com
- **Sales Inquiries:** sales@staxpayments.com
- **Website:** https://www.staxpayments.com/

### Common Issues

Quick links to solutions:

- [SDK won't initialize](08-troubleshooting.md#sdk-initialization-issues)
- [NFC not working](08-troubleshooting.md#nfc-problems)
- [Transaction declined](08-troubleshooting.md#transaction-failures)
- [Network errors](08-troubleshooting.md#network-errors)
- [Build problems](08-troubleshooting.md#build--compilation)

---

## 📝 Examples

### Basic Transaction

```kotlin
fun processPayment(amount: Double) {
    val request = TransactionRequest(Amount(amount))
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { transaction ->
            if (transaction.success == true) {
                showSuccess("Payment approved: ${transaction.id}")
            } else {
                showError("Payment declined: ${transaction.message}")
            }
        },
        error = { exception ->
            showError("Error: ${exception.message}")
        }
    )
}
```

### Transaction with Metadata

```kotlin
val request = TransactionRequest(Amount(50.00))
request.memo = "Table 5 - Dinner"
request.reference = "ORDER-123"
request.meta = mapOf(
    "server_name" to "John",
    "table_number" to "5"
)
```

### Void Transaction

```kotlin
val voidUseCase = VoidMobileReaderTransaction(Omni.shared()!!)

voidUseCase.void(
    transactionId = "txn_123",
    completion = { println("Voided") },
    error = { println("Failed: ${it.message}") }
)
```

### Refund Transaction

```kotlin
val refundUseCase = RefundMobileReaderTransaction(Omni.shared()!!)

refundUseCase.refund(
    transactionId = "txn_123",
    amount = Amount(25.00),
    completion = { refundTxn -> println("Refunded: ${refundTxn.id}") },
    error = { println("Failed: ${it.message}") }
)
```

---

## 🗺️ Navigation

- **← [Main README](../README.md)** - Repository overview
- **→ [Getting Started](01-getting-started.md)** - Start here
- **→ [API Reference](09-api-reference.md)** - Complete API docs
- **→ [Troubleshooting](08-troubleshooting.md)** - Solve issues

---

## 📄 License

This SDK is provided by Stax Payments. See main repository [LICENSE](../LICENSE) for details.

---

**Ready to get started?** → [Getting Started Guide](01-getting-started.md)
