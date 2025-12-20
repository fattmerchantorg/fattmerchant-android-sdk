# Omni Cardpresent

Accept payments using mobile card readers and NFC Tap to Pay functionality.

## Table of Contents
- [Installation](#installation)
- [Getting Started](#getting-started)
- [Tap to Pay (NFC)](#tap-to-pay)
- [Mobile Reader Payments](#mobile-reader-payments)
- [Transaction Updates](#transaction-updates)
- [Signature Handling](#signature-handling)

## <a name="installation">Installation</a>

#### Jitpack
To install,

1. Add the JitPack repository to your build file

```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

2. Add the following line to your `build.gradle` file

```
implementation 'com.github.fattmerchantorg:fattmerchant-android-sdk:1.2.0'
```


## <a name="getting-started">Getting Started</a>

In order to take mobile reader payments, you will need the following:

* **Ephemeral Omni API Key**: Using your Omni API key, you will need to create an ephemeral token. The cardpresent codebase does not store your token, so you'll need to pass one in every time you create the `Omni` object. [Here](https://fattmerchant.docs.apiary.io/#reference/0/authentication-tokens/generate-an-ephemeral-token) is a helpful guide showing you how to create an ephemeral token
* **App Name**: A name for your app!
* **Mobile Reader**: A Fattmerchant-provided mobile reader


## Initialize

Create an instance of `InitParams`

```kotlin
var initParams = InitParams(applicationContext, ephemeralApiKey, OmniApi.Environment.DEV)
```

Pass the initParams to `Omni.initialize(...)`, along with a completion lambda and an error lambda

```kotlin
Omni.initialize(params, {
	// Success!
    System.out.println("Omni is initialized")
}) {
	// There was an error
}
```

You can now use `Omni.shared()` to get the instance of Omni that you will be using

---

## <a name="tap-to-pay">Tap to Pay (NFC)</a>

Tap to Pay allows you to accept contactless payments using your Android device's built-in NFC reader, without requiring an external card reader.

### Requirements

* Android device with NFC capability
* NFC enabled on the device
* Omni SDK initialized with valid API key or ephemeral token
* Activity reference for NFC operations

### Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.NFC" />
<uses-permission android:name="android.permission.INTERNET" />

<uses-feature
    android:name="android.hardware.nfc"
    android:required="false" />
```

### Basic Tap to Pay Implementation

#### 1. Initialize Omni SDK

```kotlin
class PaymentViewModel : ViewModel() {
    
    fun initializeOmni(context: Context, application: Application, apiKey: String) {
        viewModelScope.launch {
            val params = InitParams(context, application, apiKey)
            
            Omni.initialize(
                params = params,
                completion = {
                    Log.d("Payment", "Omni initialized successfully!")
                    Omni.shared()?.signatureProvider = MySignatureProvider()
                },
                error = { exception ->
                    Log.e("Payment", "Error: ${exception.message}")
                }
            )
        }
    }
}
```

#### 2. Process Tap to Pay Transaction

```kotlin
fun processTapToPayPayment(amount: Double) {
    val transactionAmount = Amount(amount)
    val request = TransactionRequest(
        amount = transactionAmount,
        tokenize = true  // Tokenize for future use
    )
    
    // Optional metadata
    request.memo = "Tap to Pay transaction"
    request.reference = "ORDER-12345"
    
    Omni.shared()?.apply {
        // Listen to transaction events
        transactionUpdateListener = object : TransactionUpdateListener {
            override fun onTransactionUpdate(update: TransactionUpdate) {
                Log.d("Payment", "${update.value} | ${update.userFriendlyMessage}")
                
                when (update.value) {
                    "Card Swiped", "Card Inserted" -> {
                        // Card detected - show feedback to user
                    }
                    "Authorizing" -> {
                        // Transaction is being authorized
                    }
                    "Card Swipe Error" -> {
                        // Error reading card
                    }
                }
            }
        }
        
        // Execute the transaction
        takeMobileReaderTransaction(
            request = request,
            completion = { transaction ->
                if (transaction.success == true) {
                    Log.d("Payment", "Success! Transaction ID: ${transaction.id}")
                } else {
                    Log.e("Payment", "Transaction declined")
                }
            },
            error = { exception ->
                Log.e("Payment", "Error: ${exception.message}")
            }
        )
    }
}
```

#### 3. Complete Example with UI State Management

```kotlin
data class TapToPayUiState(
    val showPrompt: Boolean = false,
    val statusMessage: String? = null,
    val amount: String = "0.00",
    val isProcessing: Boolean = false
)

class PaymentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TapToPayUiState())
    val uiState: StateFlow<TapToPayUiState> = _uiState.asStateFlow()
    
    fun startTapToPayTransaction(amount: Double, subtotal: Double, tip: Double) {
        viewModelScope.launch {
            // Show prompt
            _uiState.update {
                it.copy(
                    showPrompt = true,
                    statusMessage = "Hold card near the back",
                    amount = String.format("%.2f", amount),
                    isProcessing = true
                )
            }
            
            val request = TransactionRequest(Amount(amount))
            
            Omni.shared()?.apply {
                transactionUpdateListener = object : TransactionUpdateListener {
                    override fun onTransactionUpdate(update: TransactionUpdate) {
                        when (update.value) {
                            "Prompt Insert Card", "Prompt Swipe Card" -> {
                                _uiState.update { it.copy(statusMessage = "Present card") }
                            }
                            "Card Inserted", "Card Swiped" -> {
                                _uiState.update { it.copy(statusMessage = "Card detected") }
                            }
                            "Authorizing" -> {
                                _uiState.update { it.copy(statusMessage = "Authorizing...") }
                            }
                            "Card Swipe Error" -> {
                                _uiState.update { 
                                    it.copy(statusMessage = "Card read error, try again") 
                                }
                            }
                        }
                    }
                }
                
                takeMobileReaderTransaction(
                    request = request,
                    completion = { transaction ->
                        handleTransactionComplete(transaction)
                    },
                    error = { exception ->
                        handleTransactionError(exception)
                    }
                )
            }
        }
    }
    
    private fun handleTransactionComplete(transaction: Transaction) {
        if (transaction.success == true) {
            _uiState.update {
                it.copy(
                    statusMessage = "Payment successful!",
                    isProcessing = false
                )
            }
            // Auto-dismiss after 2 seconds
            viewModelScope.launch {
                delay(2000)
                dismissPrompt()
            }
        } else {
            _uiState.update {
                it.copy(
                    statusMessage = "Transaction declined",
                    isProcessing = false
                )
            }
        }
    }
    
    private fun handleTransactionError(exception: OmniException) {
        val message = when {
            exception.message?.contains("cancelled") == true -> "Transaction cancelled"
            exception.message?.contains("timeout") == true -> "Transaction timed out"
            else -> "An error occurred, try again"
        }
        
        _uiState.update {
            it.copy(statusMessage = message, isProcessing = false)
        }
    }
    
    fun dismissPrompt() {
        _uiState.update { it.copy(showPrompt = false, statusMessage = null) }
    }
    
    fun cancelTransaction() {
        Omni.shared()?.cancelMobileReaderTransaction(
            completion = {
                Log.d("Payment", "Transaction cancelled")
                dismissPrompt()
            },
            error = { exception ->
                Log.e("Payment", "Cancel failed: ${exception.message}")
            }
        )
    }
}
```

#### 4. Jetpack Compose UI Integration

```kotlin
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    activity: ComponentActivity?
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(activity) {
        viewModel.setActivity(activity)
        viewModel.initializeOmni(apiKey = "your_api_key")
    }
    
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.startTapToPayTransaction(
                        amount = 28.00,
                        subtotal = 25.00,
                        tip = 3.00
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Process Payment - $28.00")
            }
        }
    }
    
    // Tap to Pay Modal
    AnimatedVisibility(
        visible = uiState.showPrompt,
        enter = fadeIn(tween(300)) + slideInVertically(tween(400)) { it / 2 },
        exit = fadeOut(tween(250)) + slideOutVertically(tween(300)) { it / 2 }
    ) {
        TapToPayPrompt(
            amount = uiState.amount,
            statusMessage = uiState.statusMessage,
            onCancel = { viewModel.cancelTransaction() }
        )
    }
}
```

### Transaction Management

#### Capture Pre-Authorization

```kotlin
fun captureAuth(transaction: Transaction) {
    Omni.shared()?.capture(
        transaction = transaction,
        completion = { success ->
            if (success) {
                Log.d("Payment", "Authorization captured")
            }
        },
        error = { exception ->
            Log.e("Payment", "Capture failed: ${exception.message}")
        }
    )
}
```

#### Void Transaction

```kotlin
fun voidTransaction(transaction: Transaction) {
    Omni.shared()?.voidTransaction(
        transaction = transaction,
        completion = { success ->
            if (success) {
                Log.d("Payment", "Transaction voided")
            }
        },
        error = { exception ->
            Log.e("Payment", "Void failed: ${exception.message}")
        }
    )
}
```

#### Refund Transaction

```kotlin
fun refundTransaction(transaction: Transaction, refundAmount: Amount? = null) {
    Omni.shared()?.refundMobileReaderTransaction(
        transaction = transaction,
        refundAmount = refundAmount, // null for full refund
        completion = { success ->
            if (success) {
                Log.d("Payment", "Refund processed")
            }
        },
        error = { exception ->
            Log.e("Payment", "Refund failed: ${exception.message}")
        }
    )
}
```

---

## <a name="mobile-reader-payments">Mobile Reader Payments</a>

For external card readers (Miura M010, BBPOS Chipper 2XBT), you'll need to pair and connect the device.
### Pairing the Reader

**Miura M010**

1. Turn the Miura M010 mobile reader on and wait until the display shows "MIURA SYSTEMS" along with a bluetooth indicator.
2. Press and hold the bluetooth indicator button until it flashes rapidly (this lets you know it is in discovery mode)
3. Find the Miura on your Android device's list of bluetooth devices and pair it

**BBPOS Chipper 2XBT**

1. Turn the BBPOS Chipper 2XBT mobile reader on by holding the power button
2. Wait until the LED indicator repeatedly flashes blue (this lets you know it is in discovery mode)
3. In the Android device's list of bluetooth devices, find the BBPOS device. Note that the mobile reader will appear twice; once with a phone icon and another time with a Bluetooth icon. Choose the one with the phone icon and pair.


### Connect a Mobile Reader
Once the reader has been paired, you should be able to find and connect to it using the fattmerchant android sdk

The first step is to search for a list of available readers

```kotlin
Omni.shared().getAvailableReaders { readers ->
	
}
```

Once you have the list of available ones, you can choose which one you'd like to connect

```kotlin
Omni.shared().getAvailableReaders { readers ->
	Omni.shared().connectReader(mobileReader, onConnected: { reader ->
		// Reader is connected
	}, onFail: { error ->
		// Error connecting reader
	}
}
```

### Taking a Payment
To take a payment, simply create a `TransactionRequest` and pass it along to `omni.takeMobileReaderTransaction(...)`

```kotlin
// Create an Amount
var amount = Amount(50)
    
// Create the TransactionRequest
var request = TransactionRequest(amount)
    
// Take the payment
Omni.shared()?.takeMobileReaderTransaction(request, {
    // Payment successful!
}) {
    // Error
}
```

---

## <a name="transaction-updates">Subscribing to Transaction Updates</a>
To receive Transaction updates, register a `TransactionUpdateListener` on the Omni object. This object will receive transaction updates such as:

- `PromptSwipeCard` - The mobile reader is waiting for a card to be swiped
- `CardSwiped` - A card was swiped on the mobile reader
- `Authorizing` - The payment is being authorized
- `PromptProvideSignature` - The payment requires a signature

These will be instances of `TransactionUpdate`, and will each have a `value` and a `userFriendlyMessage`. The `value` is a key you can use to identify the event, and the `userFriendlyMessage` is a string that can be shown to an end user, should you choose to.

```kotlin
// Register to listen to the transaction events
Omni.shared()?.transactionUpdateListener = object: TransactionUpdateListener {
    override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
        print("${transactionUpdate.value} | ${transactionUpdate.userFriendlyMessage}")
    }
}

// Begin the transaction
Omni.shared()?.takeMobileReaderTransaction(...) 
```

---

## <a name="signature-handling">Providing a Signature</a>
Should a transaction require a signature, one can be provided by registering a `SignatureProviding` on the Omni object. This object will be required to implement a method called 

```kotlin
/**
 * Called when a transaction requires a signature
 *
 * @param completion a block to run once the signature is complete. This should be given the
 * signature as a Base64 encoded string
 */
fun signatureRequired(completion: (String) -> Unit)
```

You can then pass a base64 encoded string representation of the signature and pass it to the completion block.

```kotlin
Omni.shared()?.signatureProvider = object : SignatureProviding {
  override fun signatureRequired(completion: (String) -> Unit) {
       var base64EncodedSignature = // ...
       completion(base64EncodedSignature)
  }
}
```

---

## Additional Features

### Pre-Authorization

To perform a pre-authorization instead of a sale:

```kotlin
val request = TransactionRequest(Amount(50.00))
request.preauth = true

Omni.shared()?.takeMobileReaderTransaction(
    request = request,
    completion = { transaction ->
        // Pre-auth successful, capture later
    },
    error = { exception ->
        Log.e("Payment", "Pre-auth failed: ${exception.message}")
    }
)
```

### User Notifications

Listen to user-level notifications for additional feedback:

```kotlin
Omni.shared()?.userNotificationListener = object : UserNotificationListener {
    override fun onUserNotification(notification: UserNotification) {
        Log.d("Payment", "Notification: ${notification.userFriendlyMessage}")
    }
    
    override fun onRawUserNotification(notification: String) {
        Log.d("Payment", "Raw notification: $notification")
    }
}
```

### Cancel Transaction

Cancel an in-progress transaction:

```kotlin
Omni.shared()?.cancelMobileReaderTransaction(
    completion = {
        Log.d("Payment", "Transaction cancelled")
    },
    error = { exception ->
        Log.e("Payment", "Cancel failed: ${exception.message}")
    }
)
```

---

## Refunding a Payment
To refund a payment, you must first get the `Transaction` that you want to refund. You can use the [Omni API](https://fattmerchant.docs.apiary.io/#reference/0/transactions) to do so. 
Once you get the transaction, you can use the `refundMobileReaderTransaction` method to attempt the refund.

> At this time, you may only refund transactions that were performed on the same device that performed the original transaction 

```kotlin
// Attain a transaction
var transaction = Transaction()
    
// Perform refund
Omni.shared()?.refundMobileReaderTransaction(transaction, {
    // Refund successful!
}) {
    // Error
}
```


