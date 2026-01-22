# Transaction Management

Complete guide to handling transactions, voids, refunds, and advanced payment features.

---

## Table of Contents

- [Transaction Types](#transaction-types)
- [Transaction Lifecycle](#transaction-lifecycle)
- [Void Transactions](#void-transactions)
- [Refund Transactions](#refund-transactions)
- [Tokenization](#tokenization)
- [Tipping](#tipping)
- [Custom Metadata](#custom-metadata)
- [Transaction History](#transaction-history)

---

## Transaction Types

The Stax SDK supports several transaction types:

### Sale (Default)

Standard payment transaction:

```kotlin
val request = TransactionRequest(Amount(50.00))

Omni.shared()?.takeMobileReaderTransaction(request, 
    completion = { /* handle */ },
    error = { /* handle */ }
)
```

### Pre-Authorization

Authorize a payment without capturing funds immediately:

```kotlin
val request = TransactionRequest(Amount(100.00))
request.preAuth = true // Pre-authorize only

Omni.shared()?.takeMobileReaderTransaction(request,
    completion = { transaction ->
        // Save transaction ID for later capture
        val authId = transaction.id
    },
    error = { /* handle */ }
)
```

Later, capture the pre-authorized amount:

```kotlin
import com.fattmerchant.omni.usecase.CapturePreauthTransaction

val captureUseCase = CapturePreauthTransaction(Omni.shared()!!)
captureUseCase.capture(
    transactionId = authId,
    amount = Amount(75.00), // Can capture less than authorized amount
    completion = {
        println("Captured successfully")
    },
    error = { exception ->
        println("Capture failed: ${exception.message}")
    }
)
```

---

## Transaction Lifecycle

Understanding transaction states:

```kotlin
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.data.TransactionUpdate

Omni.shared()?.transactionUpdateListener = object : TransactionUpdateListener {
    override fun onTransactionUpdate(update: TransactionUpdate) {
        when (update.value) {
            // 1. Initial state
            "Transaction Started" -> {
                // Transaction initiated
            }
            
            // 2. Card interaction
            "Prompt Insert Card", "Prompt Swipe Card" -> {
                // Waiting for card tap
            }
            
            "Card Inserted", "Card Swiped" -> {
                // Card detected and reading
            }
            
            // 3. Processing
            "Reading Card" -> {
                // Reading card data
            }
            
            "Authorizing" -> {
                // Sending to processor
            }
            
            // 4. Completion
            "Authorized" -> {
                // Approved by processor
            }
            
            "Declined" -> {
                // Declined by processor or issuer
            }
            
            // Error states
            "Card Swipe Error" -> {
                // Card read failed, can retry
            }
            
            "Transaction Error" -> {
                // Fatal error, cannot continue
            }
        }
    }
}
```

### Transaction Result States

```kotlin
fun handleTransactionComplete(transaction: Transaction) {
    when {
        transaction.success == true -> {
            // ✅ APPROVED
            // Transaction approved by both processor and card
            println("Status: APPROVED")
            println("Auth Code: ${transaction.authCode}")
        }
        
        transaction.success == false -> {
            // ❌ DECLINED
            // Transaction declined by processor or card issuer
            println("Status: DECLINED")
            println("Reason: ${transaction.message}")
            // Common decline reasons:
            // - Insufficient funds
            // - Invalid card
            // - Expired card
            // - Card restricted
        }
        
        transaction.isVoided == true -> {
            // 🔄 VOIDED
            // Transaction was voided by merchant
            println("Status: VOIDED")
        }
    }
}
```

---

## Void Transactions

Void a transaction immediately after approval (same day only):

### When to Use Void

- Customer changed their mind immediately after payment
- Wrong amount charged
- Duplicate transaction
- Must be done same day before settlement

### Basic Void

```kotlin
import com.fattmerchant.omni.usecase.VoidMobileReaderTransaction

fun voidTransaction(transactionId: String) {
    val voidUseCase = VoidMobileReaderTransaction(Omni.shared()!!)
    
    voidUseCase.void(
        transactionId = transactionId,
        completion = {
            println("Transaction voided successfully")
            showMessage("Transaction canceled")
        },
        error = { exception ->
            println("Void failed: ${exception.message}")
            showError("Could not void transaction: ${exception.message}")
        }
    )
}
```

### Void with Confirmation

```kotlin
fun requestVoid(transaction: Transaction) {
    // Show confirmation dialog
    AlertDialog.Builder(this)
        .setTitle("Void Transaction")
        .setMessage("Cancel transaction for $${transaction.total}?")
        .setPositiveButton("Yes") { _, _ ->
            voidTransaction(transaction.id!!)
        }
        .setNegativeButton("No", null)
        .show()
}
```

### Void Limitations

```kotlin
fun canVoidTransaction(transaction: Transaction): Boolean {
    // Check if transaction can be voided
    return when {
        transaction.id == null -> {
            false // No transaction ID
        }
        transaction.isVoided == true -> {
            false // Already voided
        }
        !isSameDay(transaction.created_at) -> {
            false // Too old - must refund instead
        }
        else -> true
    }
}

fun isSameDay(transactionDate: String?): Boolean {
    // Check if transaction is from today
    val transactionTime = parseDate(transactionDate) ?: return false
    val today = Calendar.getInstance()
    val transDate = Calendar.getInstance().apply { time = transactionTime }
    
    return today.get(Calendar.YEAR) == transDate.get(Calendar.YEAR) &&
           today.get(Calendar.DAY_OF_YEAR) == transDate.get(Calendar.DAY_OF_YEAR)
}
```

---

## Refund Transactions

Process a refund for a previous transaction:

### When to Use Refund

- Return merchandise
- Customer dispute
- Service cancellation
- Transaction is older than same day (cannot void)

### Full Refund

```kotlin
import com.fattmerchant.omni.usecase.RefundMobileReaderTransaction

fun refundTransaction(transactionId: String, originalAmount: Double) {
    val refundUseCase = RefundMobileReaderTransaction(Omni.shared()!!)
    
    refundUseCase.refund(
        transactionId = transactionId,
        amount = Amount(originalAmount), // Full refund
        completion = { refundTransaction ->
            println("Refund processed: ${refundTransaction.id}")
            showMessage("Refund successful")
        },
        error = { exception ->
            println("Refund failed: ${exception.message}")
            showError("Refund failed: ${exception.message}")
        }
    )
}
```

### Partial Refund

```kotlin
fun partialRefund(transactionId: String, refundAmount: Double) {
    val refundUseCase = RefundMobileReaderTransaction(Omni.shared()!!)
    
    refundUseCase.refund(
        transactionId = transactionId,
        amount = Amount(refundAmount), // Partial amount
        completion = { refundTransaction ->
            println("Partial refund of $${refundAmount} processed")
            showMessage("Refunded $${refundAmount}")
        },
        error = { exception ->
            showError("Refund failed: ${exception.message}")
        }
    )
}
```

### Refund with Reason

```kotlin
data class RefundRequest(
    val transactionId: String,
    val amount: Double,
    val reason: String
)

fun processRefundWithReason(refundRequest: RefundRequest) {
    val refundUseCase = RefundMobileReaderTransaction(Omni.shared()!!)
    
    refundUseCase.refund(
        transactionId = refundRequest.transactionId,
        amount = Amount(refundRequest.amount),
        completion = { refundTransaction ->
            // Log refund reason
            logRefund(
                originalId = refundRequest.transactionId,
                refundId = refundTransaction.id!!,
                amount = refundRequest.amount,
                reason = refundRequest.reason
            )
            
            showMessage("Refund processed: ${refundRequest.reason}")
        },
        error = { exception ->
            showError("Refund failed: ${exception.message}")
        }
    )
}
```

### Refund Limitations

- Cannot refund more than the original transaction amount
- Some processors may limit number of partial refunds
- Refunds may take 3-5 business days to appear on customer's card
- Already-refunded transactions cannot be refunded again

---

## Tokenization

Save payment methods for future use:

### Enable Tokenization

```kotlin
val request = TransactionRequest(Amount(50.00))
request.tokenize = true // Save card token

Omni.shared()?.takeMobileReaderTransaction(request,
    completion = { transaction ->
        if (transaction.success == true) {
            // Save token for future use
            val token = transaction.paymentToken
            val lastFour = transaction.lastFour
            val cardType = transaction.cardType
            
            savePaymentMethod(
                customerId = currentCustomer.id,
                token = token,
                lastFour = lastFour,
                cardType = cardType
            )
        }
    },
    error = { /* handle */ }
)
```

### Charge Saved Token

```kotlin
import com.fattmerchant.omni.usecase.TakePayment

fun chargeToken(token: String, amount: Double) {
    val takePayment = TakePayment(Omni.shared()!!)
    
    val request = TransactionRequest(Amount(amount))
    request.paymentToken = token
    
    takePayment.takePayment(
        request = request,
        completion = { transaction ->
            if (transaction.success == true) {
                println("Charged saved card successfully")
            }
        },
        error = { exception ->
            println("Charge failed: ${exception.message}")
        }
    )
}
```

### Managing Saved Cards

```kotlin
data class SavedCard(
    val token: String,
    val lastFour: String,
    val cardType: String,
    val customerId: String
)

class PaymentMethodRepository {
    
    private val savedCards = mutableListOf<SavedCard>()
    
    fun saveCard(token: String, lastFour: String, cardType: String, customerId: String) {
        val card = SavedCard(token, lastFour, cardType, customerId)
        savedCards.add(card)
        // Also save to database
    }
    
    fun getCustomerCards(customerId: String): List<SavedCard> {
        return savedCards.filter { it.customerId == customerId }
    }
    
    fun deleteCard(token: String) {
        savedCards.removeIf { it.token == token }
        // Also delete from database
    }
}
```

### Display Saved Cards UI

```kotlin
@Composable
fun SavedCardsList(
    customerId: String,
    onCardSelected: (SavedCard) -> Unit
) {
    val cards = remember { paymentMethodRepo.getCustomerCards(customerId) }
    
    LazyColumn {
        items(cards) { card ->
            SavedCardItem(
                card = card,
                onClick = { onCardSelected(card) }
            )
        }
        
        item {
            AddNewCardButton {
                // Navigate to new card flow
            }
        }
    }
}

@Composable
fun SavedCardItem(card: SavedCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(getCardIcon(card.cardType)),
                contentDescription = card.cardType
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("${card.cardType} •••• ${card.lastFour}")
                Text("Tap to use", style = MaterialTheme.typography.caption)
            }
        }
    }
}
```

---

## Tipping

Add tip amount to transactions:

### Pre-Calculated Tip

```kotlin
fun processPaymentWithTip(subtotal: Double, tipAmount: Double) {
    val total = subtotal + tipAmount
    
    val request = TransactionRequest(Amount(total))
    request.memo = "Subtotal: $$subtotal, Tip: $$tipAmount"
    
    Omni.shared()?.takeMobileReaderTransaction(request,
        completion = { transaction ->
            println("Total charged: $$total (includes $$tipAmount tip)")
        },
        error = { /* handle */ }
    )
}
```

### Tip Selection UI

```kotlin
@Composable
fun TipSelector(
    subtotal: Double,
    onTipSelected: (Double) -> Unit
) {
    var customTip by remember { mutableStateOf("") }
    var selectedTipPercent by remember { mutableStateOf<Int?>(null) }
    
    Column {
        Text("Add a tip?", style = MaterialTheme.typography.h6)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Percentage buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipButton(15, subtotal, selectedTipPercent) {
                selectedTipPercent = 15
                onTipSelected(subtotal * 0.15)
            }
            TipButton(18, subtotal, selectedTipPercent) {
                selectedTipPercent = 18
                onTipSelected(subtotal * 0.18)
            }
            TipButton(20, subtotal, selectedTipPercent) {
                selectedTipPercent = 20
                onTipSelected(subtotal * 0.20)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom tip input
        OutlinedTextField(
            value = customTip,
            onValueChange = {
                customTip = it
                selectedTipPercent = null
                onTipSelected(it.toDoubleOrNull() ?: 0.0)
            },
            label = { Text("Custom tip") },
            prefix = { Text("$") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // No tip button
        TextButton(onClick = {
            selectedTipPercent = null
            customTip = ""
            onTipSelected(0.0)
        }) {
            Text("No tip")
        }
    }
}

@Composable
fun TipButton(
    percent: Int,
    subtotal: Double,
    selectedPercent: Int?,
    onClick: () -> Unit
) {
    val tipAmount = subtotal * (percent / 100.0)
    val isSelected = selectedPercent == percent
    
    Button(
        onClick = onClick,
        colors = if (isSelected) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.outlinedButtonColors()
        }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$percent%")
            Text("$${"%.2f".format(tipAmount)}", style = MaterialTheme.typography.caption)
        }
    }
}
```

---

## Custom Metadata

Add custom data to transactions:

### Basic Metadata

```kotlin
val request = TransactionRequest(Amount(75.00))

// Add metadata
request.memo = "Table 5 - Dinner service"
request.reference = "ORDER-2026-001"
request.customerId = "CUST-123456"

// Additional custom fields
request.meta = mapOf(
    "server_name" to "John",
    "table_number" to "5",
    "order_type" to "dine_in",
    "location_id" to "store_001"
)
```

### Restaurant Order Example

```kotlin
data class RestaurantOrder(
    val orderId: String,
    val tableNumber: String,
    val serverName: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val tax: Double,
    val tip: Double
) {
    val total: Double
        get() = subtotal + tax + tip
}

fun processRestaurantPayment(order: RestaurantOrder) {
    val request = TransactionRequest(Amount(order.total))
    
    request.reference = order.orderId
    request.memo = "Table ${order.tableNumber} - ${order.serverName}"
    request.meta = mapOf(
        "table_number" to order.tableNumber,
        "server_name" to order.serverName,
        "subtotal" to order.subtotal.toString(),
        "tax" to order.tax.toString(),
        "tip" to order.tip.toString(),
        "items_count" to order.items.size.toString()
    )
    
    Omni.shared()?.takeMobileReaderTransaction(request,
        completion = { transaction ->
            println("Order ${order.orderId} paid successfully")
        },
        error = { /* handle */ }
    )
}
```

---

## Transaction History

Retrieve and display transaction history:

### Fetch Recent Transactions

```kotlin
import com.fattmerchant.omni.data.repository.TransactionRepository

class TransactionHistoryViewModel : ViewModel() {
    
    private val transactionRepo = TransactionRepository(Omni.shared()!!)
    
    fun fetchTransactions() {
        viewModelScope.launch {
            try {
                val transactions = transactionRepo.getTransactions(
                    page = 1,
                    perPage = 50
                )
                
                _transactions.value = transactions
                
            } catch (e: Exception) {
                _error.value = "Failed to load transactions: ${e.message}"
            }
        }
    }
    
    fun searchTransactions(query: String) {
        viewModelScope.launch {
            val results = transactionRepo.searchTransactions(query)
            _searchResults.value = results
        }
    }
}
```

### Display Transaction List

```kotlin
@Composable
fun TransactionHistoryScreen(viewModel: TransactionHistoryViewModel = viewModel()) {
    val transactions by viewModel.transactions.collectAsState()
    
    LazyColumn {
        items(transactions) { transaction ->
            TransactionListItem(
                transaction = transaction,
                onClick = { /* show details */ }
            )
        }
    }
}

@Composable
fun TransactionListItem(transaction: Transaction, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = transaction.cardType + " •••• " + transaction.lastFour,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = formatDate(transaction.created_at),
                    style = MaterialTheme.typography.caption
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${"%.2f".format(transaction.total)}",
                    style = MaterialTheme.typography.h6
                )
                StatusBadge(transaction)
            }
        }
    }
}

@Composable
fun StatusBadge(transaction: Transaction) {
    val (text, color) = when {
        transaction.success == true -> "Approved" to Color.Green
        transaction.isVoided == true -> "Voided" to Color.Gray
        else -> "Declined" to Color.Red
    }
    
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.caption
    )
}
```

---

## Next Steps

- **[UI/UX Guidelines](04-ui-ux-guidelines.md)** - Design great payment experiences
- **[Testing](06-testing.md)** - Test all transaction scenarios
- **[API Reference](09-api-reference.md)** - Detailed API documentation
