# Your First Transaction

This guide walks you through processing your first Tap to Pay transaction using the Stax Android SDK.

> **� UI Components Included**
>
> The SDK sample app includes ready-to-use UI components:
> - ✅ `TapToPayPrompt` - Polished NFC payment screen with card logos, amount display, and animations
> - ✅ Light/Dark theme support
> - ✅ Based on professional Figma designs
>
> See `app/src/main/java/com/staxpayments/sample/ui/components/TapToPayPrompt.kt`
>
> You can use these components directly or build your own custom UI using the transaction callbacks.

## Quick Start

Here's the minimal code to process a payment:

```kotlin
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest

fun processPayment(amountInDollars: Double) {
    val request = TransactionRequest(Amount(amountInDollars))
    
    Omni.shared()?.takeMobileReaderTransaction(
        request = request,
        completion = { transaction ->
            if (transaction.success == true) {
                println("Payment successful! ID: ${transaction.id}")
            } else {
                println("Payment declined")
            }
        },
        error = { exception ->
            println("Error: ${exception.message}")
        }
    )
}
```

---

## Complete Example

### Step 1: Create Transaction Request

```kotlin
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest

// Create a transaction for $25.00
val amount = Amount(25.00)
val request = TransactionRequest(amount)

// Optional: Add metadata
request.memo = "Coffee and pastry"
request.reference = "ORDER-2026-001"
```

### Step 2: Set Up Transaction Listener

Listen to real-time updates during the transaction:

```kotlin
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.data.TransactionUpdate

Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
    override fun onTransactionUpdate(update: TransactionUpdate) {
        println("Status: ${update.value}")
        println("Message: ${update.userFriendlyMessage}")
        
        // Update UI based on status
        when (update.value) {
            "Prompt Insert Card", "Prompt Swipe Card" -> {
                showCardPrompt("Hold card near the back of the phone")
            }
            "Card Inserted", "Card Swiped" -> {
                showProcessing("Card detected, processing...")
            }
            "Authorizing" -> {
                showProcessing("Authorizing payment...")
            }
            "Authorized" -> {
                showSuccess("Payment approved!")
            }
        }
    }
}
```

### Step 3: Execute Transaction

```kotlin
Omni.shared()?.takeMobileReaderTransaction(
    request = request,
    completion = { transaction ->
        handleTransactionComplete(transaction)
    },
    error = { exception ->
        handleTransactionError(exception)
    }
)
```

### Step 4: Handle Results

```kotlin
import com.fattmerchant.omni.data.models.Transaction

fun handleTransactionComplete(transaction: Transaction) {
    if (transaction.success == true) {
        // ✅ Payment successful
        println("Transaction ID: ${transaction.id}")
        println("Amount: $${transaction.total}")
        println("Card: ${transaction.cardType} ending in ${transaction.lastFour}")
        println("Auth Code: ${transaction.authCode}")
        
        // Save transaction details
        saveTransactionRecord(transaction)
        
        // Show receipt
        emailReceipt(transaction)
        
        // Navigate to success screen
        showSuccessScreen(transaction)
        
    } else {
        // ❌ Payment declined
        println("Decline reason: ${transaction.message}")
        
        // Show decline reason to user
        showDeclineScreen(transaction.message)
    }
}

fun handleTransactionError(exception: Exception) {
    println("Transaction error: ${exception.message}")
    
    // Handle different error types
    when (exception) {
        is NetworkException -> {
            showError("No internet connection. Please check your network.")
        }
        is OmniException -> {
            showError("Payment error: ${exception.message}")
        }
        else -> {
            showError("An unexpected error occurred. Please try again.")
        }
    }
}
```

---

## Complete Activity Example

Here's a full implementation in an Activity:

```kotlin
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.TransactionUpdate
import com.fattmerchant.omni.data.models.Transaction

class PaymentActivity : AppCompatActivity() {
    
    private lateinit var amountTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var payButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        
        amountTextView = findViewById(R.id.amount_text)
        statusTextView = findViewById(R.id.status_text)
        payButton = findViewById(R.id.pay_button)
        
        // Set amount
        val amount = intent.getDoubleExtra("AMOUNT", 0.0)
        amountTextView.text = "$%.2f".format(amount)
        
        // Set up transaction listener
        setupTransactionListener()
        
        // Handle pay button click
        payButton.setOnClickListener {
            processPayment(amount)
        }
    }
    
    private fun setupTransactionListener() {
        Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
            override fun onTransactionUpdate(update: TransactionUpdate) {
                runOnUiThread {
                    statusTextView.text = update.userFriendlyMessage
                    
                    when (update.value) {
                        "Prompt Insert Card", "Prompt Swipe Card" -> {
                            payButton.isEnabled = false
                            statusTextView.text = "Hold card near back of phone"
                        }
                        "Card Inserted", "Card Swiped" -> {
                            statusTextView.text = "Card detected..."
                        }
                        "Authorizing" -> {
                            statusTextView.text = "Authorizing payment..."
                        }
                    }
                }
            }
        }
    }
    
    private fun processPayment(amount: Double) {
        // Disable pay button during transaction
        payButton.isEnabled = false
        statusTextView.text = "Starting payment..."
        
        // Create transaction request
        val request = TransactionRequest(Amount(amount))
        request.memo = "In-app purchase"
        request.reference = "ORDER-${System.currentTimeMillis()}"
        
        // Execute transaction
        Omni.shared()?.takeMobileReaderTransaction(
            request = request,
            completion = { transaction ->
                runOnUiThread {
                    handleTransactionComplete(transaction)
                }
            },
            error = { exception ->
                runOnUiThread {
                    handleTransactionError(exception)
                }
            }
        )
    }
    
    private fun handleTransactionComplete(transaction: Transaction) {
        if (transaction.success == true) {
            // Success
            statusTextView.text = "Payment approved!"
            Toast.makeText(this, 
                "Transaction ID: ${transaction.id}", 
                Toast.LENGTH_LONG).show()
            
            // Return to previous screen with result
            setResult(RESULT_OK)
            finish()
            
        } else {
            // Declined
            statusTextView.text = "Payment declined"
            payButton.isEnabled = true
            
            Toast.makeText(this, 
                transaction.message ?: "Transaction declined", 
                Toast.LENGTH_LONG).show()
        }
    }
    
    private fun handleTransactionError(exception: Exception) {
        statusTextView.text = "Transaction error"
        payButton.isEnabled = true
        
        Toast.makeText(this, 
            "Error: ${exception.message}", 
            Toast.LENGTH_LONG).show()
    }
}
```

---

## ViewModel Example (MVVM Pattern)

For a more structured approach using ViewModel and Kotlin Coroutines:

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.TransactionUpdate
import com.fattmerchant.omni.data.models.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaymentUiState(
    val isProcessing: Boolean = false,
    val statusMessage: String = "",
    val showCardPrompt: Boolean = false,
    val transaction: Transaction? = null,
    val error: String? = null
)

class PaymentViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()
    
    init {
        setupTransactionListener()
    }
    
    private fun setupTransactionListener() {
        Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
            override fun onTransactionUpdate(update: TransactionUpdate) {
                viewModelScope.launch {
                    when (update.value) {
                        "Prompt Insert Card", "Prompt Swipe Card" -> {
                            _uiState.value = _uiState.value.copy(
                                statusMessage = "Hold card near back of phone",
                                showCardPrompt = true
                            )
                        }
                        "Card Inserted", "Card Swiped" -> {
                            _uiState.value = _uiState.value.copy(
                                statusMessage = "Card detected",
                                showCardPrompt = false
                            )
                        }
                        "Authorizing" -> {
                            _uiState.value = _uiState.value.copy(
                                statusMessage = "Authorizing payment..."
                            )
                        }
                        "Authorized" -> {
                            _uiState.value = _uiState.value.copy(
                                statusMessage = "Payment approved!"
                            )
                        }
                        "Card Swipe Error" -> {
                            _uiState.value = _uiState.value.copy(
                                statusMessage = "Card read error, try again",
                                showCardPrompt = true
                            )
                        }
                    }
                }
            }
        }
    }
    
    fun processPayment(amount: Double, memo: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                statusMessage = "Starting payment...",
                error = null
            )
            
            val request = TransactionRequest(Amount(amount))
            request.memo = memo
            request.reference = "ORDER-${System.currentTimeMillis()}"
            
            Omni.shared()?.takeMobileReaderTransaction(
                request = request,
                completion = { transaction ->
                    handleSuccess(transaction)
                },
                error = { exception ->
                    handleError(exception)
                }
            )
        }
    }
    
    private fun handleSuccess(transaction: Transaction) {
        _uiState.value = if (transaction.success == true) {
            _uiState.value.copy(
                isProcessing = false,
                statusMessage = "Payment successful!",
                transaction = transaction,
                showCardPrompt = false
            )
        } else {
            _uiState.value.copy(
                isProcessing = false,
                statusMessage = "Payment declined",
                error = transaction.message,
                showCardPrompt = false
            )
        }
    }
    
    private fun handleError(exception: Exception) {
        _uiState.value = _uiState.value.copy(
            isProcessing = false,
            statusMessage = "Transaction error",
            error = exception.message,
            showCardPrompt = false
        )
    }
    
    fun resetState() {
        _uiState.value = PaymentUiState()
    }
}
```

Using the ViewModel in a Composable:

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PaymentScreen(
    amount: Double,
    viewModel: PaymentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Amount display
        Text(
            text = "$%.2f".format(amount),
            style = MaterialTheme.typography.h2
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Status message
        Text(
            text = uiState.statusMessage,
            style = MaterialTheme.typography.body1
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Card prompt animation
        if (uiState.showCardPrompt) {
            NfcPromptAnimation()
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Pay button
        Button(
            onClick = { viewModel.processPayment(amount, "App purchase") },
            enabled = !uiState.isProcessing
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Pay $%.2f".format(amount))
            }
        }
        
        // Error message
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colors.error
            )
        }
        
        // Success
        if (uiState.transaction?.success == true) {
            LaunchedEffect(Unit) {
                delay(2000)
                // Navigate to success screen
            }
        }
    }
}
```

---

## Testing Your First Transaction

### Test Cards

Use these test cards for development:

| Card Type | Number | Exp | CVV | Result |
|-----------|--------|-----|-----|--------|
| Visa | 4111111111111111 | Any future date | Any 3 digits | Approved |
| Mastercard | 5105105105105100 | Any future date | Any 3 digits | Approved |
| Amex | 378282246310005 | Any future date | Any 4 digits | Approved |
| Discover | 6011111111111117 | Any future date | Any 3 digits | Approved |

**Note:** Physical test cards for Tap to Pay NFC testing should be requested from your Stax account manager.

### Testing Checklist

- [ ] Transaction completes successfully
- [ ] Transaction updates display correctly
- [ ] Success screen shows transaction details
- [ ] Decline handling works properly
- [ ] Error handling works (test with airplane mode)
- [ ] Receipt can be sent via email/SMS
- [ ] Transaction appears in Stax Dashboard

---

## Common Issues

### Transaction Never Starts

**Symptoms:** Tapping card does nothing, no response

**Solutions:**
- Verify NFC is enabled on device
- Check SDK is initialized (`Omni.shared()` is not null)
- Ensure transaction listener is set before calling `takeMobileReaderTransaction()`
- Verify internet connection is active

### Transaction Hangs on "Authorizing"

**Symptoms:** Gets stuck on authorization step

**Solutions:**
- Check network connection
- Verify API key is valid
- Check Stax Dashboard for account status
- Try with a different card

### "Card Read Error" Repeatedly

**Symptoms:** Card detected but fails to read

**Solutions:**
- Hold card steady for 2-3 seconds
- Try different position on back of phone (NFC antenna location varies)
- Remove phone case if thick/metallic
- Try a different card
- Clean NFC area on phone

---

## Next Steps

Now that you can process a basic transaction, learn about:

1. **[Transaction Management](03-transaction-management.md)** - Void, refund, and advanced features
2. **[UI/UX Guidelines](04-ui-ux-guidelines.md)** - Create a polished payment experience
3. **[Testing](06-testing.md)** - Comprehensive testing strategies

---

## Need Help?

- **Email**: sdk-support@staxpayments.com
- **Documentation**: https://docs.staxpayments.com/docs/mobile-sdk
- **GitHub**: https://github.com/fattmerchantorg/fattmerchant-android-sdk
