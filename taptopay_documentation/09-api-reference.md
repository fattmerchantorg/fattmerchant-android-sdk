# API Reference

Complete reference documentation for the Stax Tap to Pay SDK.

---

## Table of Contents

- [Omni Class](#omni-class)
- [TransactionRequest](#transactionrequest)
- [Transaction](#transaction)
- [TransactionUpdate](#transactionupdate)
- [Amount](#amount)
- [Use Cases](#use-cases)
- [Listeners](#listeners)
- [Enums](#enums)
- [Exceptions](#exceptions)

---

## Omni Class

The main entry point for the SDK.

### Initialization

```kotlin
class Omni {
    companion object {
        fun initialize(
            context: Context,
            apiKey: String,
            environment: Environment = Environment.LIVE
        )
        
        fun shared(): Omni?
    }
}
```

**Parameters:**
- `context: Context` - Application context (required)
- `apiKey: String` - Stax API key (required)
- `environment: Environment` - `Environment.DEV` or `Environment.LIVE` (default: LIVE)

**Returns:** Nothing (initialization method)

**Example:**
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        Omni.initialize(
            context = this,
            apiKey = "your_api_key_here",
            environment = Environment.DEV
        )
    }
}
```

### Properties

#### environment

```kotlin
val environment: Environment
```

Current environment (DEV or LIVE)

#### transactionUpdateListener

```kotlin
var transactionUpdateListener: TransactionUpdateListener?
```

Listener for real-time transaction updates

#### userNotificationListener

```kotlin
var userNotificationListener: UserNotificationListener?
```

Listener for user-facing notifications

#### connectedReader

```kotlin
fun getConnectedReader(): MobileReader?
```

Currently connected external card reader, or null if none connected

**Returns:** `MobileReader?` - Connected reader or null

**Example:**
```kotlin
val reader = Omni.shared()?.getConnectedReader()
if (reader != null) {
    println("Reader: ${reader.name}")
} else {
    println("No reader connected - will use NFC")
}
```

### Methods

#### takeMobileReaderTransaction

Process a payment transaction (NFC or external reader)

```kotlin
fun takeMobileReaderTransaction(
    request: TransactionRequest,
    completion: (Transaction) -> Unit,
    error: (Exception) -> Unit
)
```

**Parameters:**
- `request: TransactionRequest` - Transaction details
- `completion: (Transaction) -> Unit` - Success callback
- `error: (Exception) -> Unit` - Error callback

**Example:**
```kotlin
val request = TransactionRequest(Amount(25.00))

Omni.shared()?.takeMobileReaderTransaction(
    request,
    completion = { transaction ->
        if (transaction.success == true) {
            println("Approved: ${transaction.id}")
        } else {
            println("Declined: ${transaction.message}")
        }
    },
    error = { exception ->
        println("Error: ${exception.message}")
    }
)
```

#### connectReader

Connect to an external Bluetooth or USB card reader

```kotlin
fun connectReader(
    reader: MobileReader,
    completion: () -> Unit,
    error: (Exception) -> Unit
)
```

**Parameters:**
- `reader: MobileReader` - Reader to connect to
- `completion: () -> Unit` - Success callback
- `error: (Exception) -> Unit` - Error callback

#### disconnectReader

Disconnect from currently connected reader

```kotlin
fun disconnectReader(
    completion: () -> Unit,
    error: (Exception) -> Unit
)
```

#### searchForReaders

Search for available Bluetooth/USB card readers

```kotlin
fun searchForReaders(
    completion: (List<MobileReader>) -> Unit,
    error: (Exception) -> Unit
)
```

**Returns:** List of available readers via callback

---

## TransactionRequest

Request object for creating transactions

### Constructor

```kotlin
class TransactionRequest(
    val amount: Amount
)
```

### Properties

#### amount (required)

```kotlin
val amount: Amount
```

Transaction amount

#### tokenize

```kotlin
var tokenize: Boolean = false
```

Whether to save card token for future use

**Example:**
```kotlin
val request = TransactionRequest(Amount(50.00))
request.tokenize = true // Save card
```

#### preAuth

```kotlin
var preAuth: Boolean = false
```

Whether to pre-authorize (not capture) payment

**Example:**
```kotlin
val request = TransactionRequest(Amount(100.00))
request.preAuth = true // Pre-auth only, capture later
```

#### memo

```kotlin
var memo: String? = null
```

Optional transaction note

**Example:**
```kotlin
request.memo = "Table 5 - John's order"
```

#### reference

```kotlin
var reference: String? = null
```

Optional external reference ID

**Example:**
```kotlin
request.reference = "ORDER-2025-001"
```

#### customerId

```kotlin
var customerId: String? = null
```

Optional customer ID

**Example:**
```kotlin
request.customerId = "CUST-123456"
```

#### paymentToken

```kotlin
var paymentToken: String? = null
```

Saved payment token to charge (instead of prompting for card)

**Example:**
```kotlin
val request = TransactionRequest(Amount(25.00))
request.paymentToken = "tok_abc123xyz" // Charge saved card
```

#### meta

```kotlin
var meta: Map<String, String>? = null
```

Custom metadata key-value pairs

**Example:**
```kotlin
request.meta = mapOf(
    "server_name" to "John",
    "table_number" to "5",
    "order_type" to "dine_in"
)
```

### Complete Example

```kotlin
val request = TransactionRequest(Amount(75.00)).apply {
    tokenize = false
    preAuth = false
    memo = "Restaurant order - Table 5"
    reference = "ORDER-2025-001"
    customerId = "CUST-789"
    meta = mapOf(
        "server" to "Jane",
        "table" to "5",
        "items" to "3"
    )
}
```

---

## Transaction

Response object containing transaction details

### Properties

#### id

```kotlin
val id: String?
```

Unique transaction ID

#### total

```kotlin
val total: Double?
```

Transaction amount

#### success

```kotlin
val success: Boolean?
```

Whether transaction was approved (`true`) or declined (`false`)

#### message

```kotlin
val message: String?
```

Response message (often decline reason)

#### authCode

```kotlin
val authCode: String?
```

Authorization code from processor

#### cardType

```kotlin
val cardType: String?
```

Card brand: "visa", "mastercard", "american express", "discover"

#### lastFour

```kotlin
val lastFour: String?
```

Last 4 digits of card number

#### paymentToken

```kotlin
val paymentToken: String?
```

Saved card token (if `tokenize = true`)

#### isVoided

```kotlin
val isVoided: Boolean?
```

Whether transaction has been voided

#### created_at

```kotlin
val created_at: String?
```

Transaction timestamp (ISO 8601 format)

#### method

```kotlin
val method: String?
```

Payment method: "card", "bank_account"

#### source

```kotlin
val source: String?
```

Entry method: "CONTACTLESS", "INSERT", "SWIPE"

#### transactionType

```kotlin
val transactionType: String?
```

Transaction type: "SALE", "REFUND", "VOID", "PREAUTH"

#### metadata

```kotlin
val meta: Map<String, Any>?
```

Custom metadata from request

### Example Usage

```kotlin
fun handleTransaction(transaction: Transaction) {
    when {
        transaction.success == true -> {
            println("""
                ✅ APPROVED
                ID: ${transaction.id}
                Amount: $${transaction.total}
                Card: ${transaction.cardType} •••• ${transaction.lastFour}
                Auth Code: ${transaction.authCode}
            """.trimIndent())
        }
        
        transaction.success == false -> {
            println("""
                ❌ DECLINED
                Reason: ${transaction.message}
            """.trimIndent())
        }
    }
}
```

---

## TransactionUpdate

Real-time updates during transaction processing

### Properties

#### value

```kotlin
val value: String
```

Update message

#### userFacingMessage

```kotlin
val userFacingMessage: String?
```

Optional user-friendly message

### Common Update Values

| Value | Meaning |
|-------|---------|
| `"Transaction Started"` | Transaction initiated |
| `"Prompt Insert Card"` | Waiting for card tap |
| `"Prompt Swipe Card"` | Waiting for card tap |
| `"Card Inserted"` | Card detected |
| `"Card Swiped"` | Card detected |
| `"Reading Card"` | Reading card data |
| `"Authorizing"` | Sending to processor |
| `"Authorized"` | Approved |
| `"Declined"` | Declined |
| `"Card Swipe Error"` | Card read failed |
| `"Transaction Error"` | Fatal error |

### Example Usage

```kotlin
Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
    override fun onTransactionUpdate(update: TransactionUpdate) {
        println("Update: ${update.value}")
        
        // Show user-friendly message if available
        update.userFacingMessage?.let { message ->
            showMessage(message)
        }
        
        // Update UI based on status
        when (update.value) {
            "Prompt Insert Card" -> showTapCardPrompt()
            "Reading Card" -> showProgress("Reading card...")
            "Authorizing" -> showProgress("Authorizing...")
            "Authorized" -> showSuccess()
        }
    }
}
```

---

## Amount

Represents monetary amounts

### Constructor

```kotlin
class Amount(value: Double)
```

**Parameters:**
- `value: Double` - Amount in dollars (e.g., 25.00)

### Methods

#### dollars

```kotlin
fun dollars(): Double
```

Get amount in dollars

#### cents

```kotlin
fun cents(): Int
```

Get amount in cents

### Example

```kotlin
val amount = Amount(19.99)

println(amount.dollars()) // 19.99
println(amount.cents())   // 1999

// Used in requests
val request = TransactionRequest(Amount(25.50))
```

---

## Use Cases

Pre-built use cases for common operations

### VoidMobileReaderTransaction

Void a transaction

```kotlin
class VoidMobileReaderTransaction(omni: Omni) {
    fun void(
        transactionId: String,
        completion: () -> Unit,
        error: (Exception) -> Unit
    )
}
```

**Example:**
```kotlin
val voidUseCase = VoidMobileReaderTransaction(Omni.shared()!!)

voidUseCase.void(
    transactionId = "txn_123",
    completion = {
        println("Transaction voided successfully")
    },
    error = { exception ->
        println("Void failed: ${exception.message}")
    }
)
```

### RefundMobileReaderTransaction

Refund a transaction (full or partial)

```kotlin
class RefundMobileReaderTransaction(omni: Omni) {
    fun refund(
        transactionId: String,
        amount: Amount,
        completion: (Transaction) -> Unit,
        error: (Exception) -> Unit
    )
}
```

**Example:**
```kotlin
val refundUseCase = RefundMobileReaderTransaction(Omni.shared()!!)

// Full refund
refundUseCase.refund(
    transactionId = "txn_123",
    amount = Amount(50.00),
    completion = { refundTransaction ->
        println("Refunded: ${refundTransaction.id}")
    },
    error = { exception ->
        println("Refund failed: ${exception.message}")
    }
)

// Partial refund
refundUseCase.refund(
    transactionId = "txn_123",
    amount = Amount(20.00), // Partial amount
    completion = { refundTransaction ->
        println("Partial refund: ${refundTransaction.id}")
    },
    error = { /* handle error */ }
)
```

### CapturePreauthTransaction

Capture a pre-authorized transaction

```kotlin
class CapturePreauthTransaction(omni: Omni) {
    fun capture(
        transactionId: String,
        amount: Amount,
        completion: () -> Unit,
        error: (Exception) -> Unit
    )
}
```

**Example:**
```kotlin
val captureUseCase = CapturePreauthTransaction(Omni.shared()!!)

captureUseCase.capture(
    transactionId = "txn_preauth_123",
    amount = Amount(75.00), // Can capture less than pre-auth amount
    completion = {
        println("Pre-auth captured successfully")
    },
    error = { exception ->
        println("Capture failed: ${exception.message}")
    }
)
```

### TakePayment

Charge a saved payment token

```kotlin
class TakePayment(omni: Omni) {
    fun takePayment(
        request: TransactionRequest,
        completion: (Transaction) -> Unit,
        error: (Exception) -> Unit
    )
}
```

**Example:**
```kotlin
val takePayment = TakePayment(Omni.shared()!!)

val request = TransactionRequest(Amount(30.00))
request.paymentToken = "tok_saved_card"

takePayment.takePayment(
    request = request,
    completion = { transaction ->
        println("Charged saved card: ${transaction.id}")
    },
    error = { exception ->
        println("Charge failed: ${exception.message}")
    }
)
```

---

## Listeners

### TransactionUpdateListener

Receive real-time transaction updates

```kotlin
interface TransactionUpdateListener {
    fun onTransactionUpdate(update: TransactionUpdate)
}
```

**Example:**
```kotlin
Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
    override fun onTransactionUpdate(update: TransactionUpdate) {
        when (update.value) {
            "Transaction Started" -> println("Starting...")
            "Prompt Insert Card" -> println("Tap card now")
            "Reading Card" -> println("Reading...")
            "Authorizing" -> println("Authorizing...")
            "Authorized" -> println("Approved!")
            else -> println("Update: ${update.value}")
        }
    }
}
```

### UserNotificationListener

Receive user-facing notifications

```kotlin
interface UserNotificationListener {
    fun onUserNotification(notification: String)
}
```

**Example:**
```kotlin
Omni.shared()?.userNotificationListener = object : UserNotificationListener {
    override fun onUserNotification(notification: String) {
        // Show notification to user
        Toast.makeText(context, notification, Toast.LENGTH_SHORT).show()
    }
}
```

### MobileReaderConnectionStatusListener

Receive reader connection status updates

```kotlin
interface MobileReaderConnectionStatusListener {
    fun onConnected(reader: MobileReader)
    fun onDisconnected(reader: MobileReader?)
    fun onError(error: Exception)
}
```

**Example:**
```kotlin
val connectionListener = object : MobileReaderConnectionStatusListener {
    override fun onConnected(reader: MobileReader) {
        println("Reader connected: ${reader.name}")
    }
    
    override fun onDisconnected(reader: MobileReader?) {
        println("Reader disconnected")
    }
    
    override fun onError(error: Exception) {
        println("Connection error: ${error.message}")
    }
}
```

---

## Enums

### Environment

```kotlin
enum class Environment {
    DEV,  // Test environment
    LIVE  // Production environment
}
```

**Usage:**
```kotlin
Omni.initialize(
    context = this,
    apiKey = apiKey,
    environment = Environment.DEV // or Environment.LIVE
)
```

### TransactionType

```kotlin
enum class TransactionType {
    SALE,
    REFUND,
    VOID,
    PREAUTH,
    CAPTURE
}
```

---

## Exceptions

### Common Exceptions

#### TransactionDeclinedException

```kotlin
class TransactionDeclinedException(
    message: String,
    val declineReason: String?
) : Exception(message)
```

Thrown when a transaction is declined

#### NetworkException

```kotlin
class NetworkException(
    message: String
) : Exception(message)
```

Thrown for network-related errors

#### CardReadException

```kotlin
class CardReadException(
    message: String
) : Exception(message)
```

Thrown when card cannot be read

#### NfcUnavailableException

```kotlin
class NfcUnavailableException(
    message: String
) : Exception(message)
```

Thrown when NFC is not available

### Exception Handling Example

```kotlin
Omni.shared()?.takeMobileReaderTransaction(
    request,
    completion = { transaction -> /* success */ },
    error = { exception ->
        when (exception) {
            is TransactionDeclinedException -> {
                showError("Card declined: ${exception.declineReason}")
            }
            is NetworkException -> {
                showError("Network error. Please check your connection.")
            }
            is CardReadException -> {
                showError("Could not read card. Please try again.")
            }
            is NfcUnavailableException -> {
                showError("NFC is not available on this device.")
            }
            else -> {
                showError("Transaction failed: ${exception.message}")
            }
        }
    }
)
```

---

## Models

### MobileReader

Represents an external card reader

```kotlin
data class MobileReader(
    val name: String,
    val serialNumber: String,
    val connectionType: ConnectionType,
    val batteryLevel: Int?
)

enum class ConnectionType {
    BLUETOOTH,
    USB
}
```

**Example:**
```kotlin
fun displayReaderInfo(reader: MobileReader) {
    println("""
        Reader: ${reader.name}
        Serial: ${reader.serialNumber}
        Connection: ${reader.connectionType}
        Battery: ${reader.batteryLevel}%
    """.trimIndent())
}
```

---

## Code Examples

### Complete Transaction Flow

```kotlin
class PaymentManager(private val context: Context) {
    
    private val omni = Omni.shared()!!
    
    init {
        setupListeners()
    }
    
    private fun setupListeners() {
        omni.transactionUpdateListener = object : TransactionUpdateListener {
            override fun onTransactionUpdate(update: TransactionUpdate) {
                handleUpdate(update)
            }
        }
    }
    
    fun processPayment(
        amount: Double,
        onSuccess: (Transaction) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = TransactionRequest(Amount(amount))
        
        omni.takeMobileReaderTransaction(
            request,
            completion = { transaction ->
                if (transaction.success == true) {
                    onSuccess(transaction)
                } else {
                    onError(transaction.message ?: "Transaction declined")
                }
            },
            error = { exception ->
                onError(exception.message ?: "Transaction failed")
            }
        )
    }
    
    fun voidTransaction(
        transactionId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val voidUseCase = VoidMobileReaderTransaction(omni)
        
        voidUseCase.void(
            transactionId,
            completion = onSuccess,
            error = { exception ->
                onError(exception.message ?: "Void failed")
            }
        )
    }
    
    fun refundTransaction(
        transactionId: String,
        amount: Double,
        onSuccess: (Transaction) -> Unit,
        onError: (String) -> Unit
    ) {
        val refundUseCase = RefundMobileReaderTransaction(omni)
        
        refundUseCase.refund(
            transactionId,
            Amount(amount),
            completion = onSuccess,
            error = { exception ->
                onError(exception.message ?: "Refund failed")
            }
        )
    }
    
    private fun handleUpdate(update: TransactionUpdate) {
        // Handle transaction updates
        Log.d("Payment", "Update: ${update.value}")
    }
}
```

### ViewModel Integration

```kotlin
class PaymentViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()
    
    init {
        Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
            override fun onTransactionUpdate(update: TransactionUpdate) {
                _uiState.value = PaymentUiState.Processing(update.value)
            }
        }
    }
    
    fun processPayment(amount: Double) {
        _uiState.value = PaymentUiState.Processing("Starting...")
        
        val request = TransactionRequest(Amount(amount))
        
        Omni.shared()?.takeMobileReaderTransaction(
            request,
            completion = { transaction ->
                if (transaction.success == true) {
                    _uiState.value = PaymentUiState.Success(transaction)
                } else {
                    _uiState.value = PaymentUiState.Error(
                        transaction.message ?: "Transaction declined"
                    )
                }
            },
            error = { exception ->
                _uiState.value = PaymentUiState.Error(
                    exception.message ?: "Transaction failed"
                )
            }
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        Omni.shared()?.transactionUpdateListener = null
    }
}

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    data class Processing(val update: String) : PaymentUiState()
    data class Success(val transaction: Transaction) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}
```

---

## Next Steps

- **[Getting Started](01-getting-started.md)** - Set up the SDK
- **[Your First Transaction](02-your-first-transaction.md)** - Process payments
- **[Transaction Management](03-transaction-management.md)** - Voids, refunds, and more
- **[Troubleshooting](08-troubleshooting.md)** - Solve common issues
