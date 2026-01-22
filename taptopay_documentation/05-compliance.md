# Compliance & Certification

Requirements for card brand compliance and EMVCo certification when accepting contactless payments.

---

## Table of Contents

- [Overview](#overview)
- [EMVCo Requirements](#emvco-requirements)
- [Card Brand Logos](#card-brand-logos)
- [Contactless Symbols](#contactless-symbols)
- [Receipt Requirements](#receipt-requirements)
- [Data Security](#data-security)
- [Certification Process](#certification-process)

---

## Overview

When accepting contactless payments with Tap to Pay, you must comply with:

1. **EMVCo** - Global contactless payment standards
2. **Card Networks** - Visa, Mastercard, American Express, Discover rules
3. **PCI DSS** - Payment Card Industry Data Security Standard

The Stax SDK handles most technical compliance requirements, but you're responsible for:
- Displaying required symbols and logos
- Following UI/UX guidelines
- Proper receipt formatting
- Data handling practices

---

## EMVCo Requirements

### What is EMVCo?

EMVCo is the organization that defines global standards for secure payment transactions. For Tap to Pay (contactless payments), specific UI requirements must be met.

### Contactless Symbol Display

**REQUIRED:** Display the EMVCo contactless symbol when prompting for card:

```kotlin
@Composable
fun ContactlessPrompt() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        // EMVCo Contactless Symbol - REQUIRED
        Image(
            painter = painterResource(R.drawable.emvco_contactless_symbol),
            contentDescription = "Contactless Payment",
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Hold card near device",
            style = MaterialTheme.typography.h6
        )
    }
}
```

### Symbol Requirements

- **Size:** Minimum 15mm (approximately 56 pixels at standard DPI)
- **Color:** Four waves, typically displayed in black or brand color
- **Placement:** Clearly visible where contactless payment is accepted
- **Context:** Must be shown when prompting for card tap

### Where to Get the Symbol

Download the official EMVCo contactless symbol:
- [EMVCo Contactless Symbol](https://www.emvco.com/emv-technologies/contactless/)

**Important:** Use the official symbol - do not create your own version.

---

## Card Brand Logos

### Display Guidelines

When showing payment options or transaction history, display card brand logos appropriately:

```kotlin
@Composable
fun CardBrandLogo(cardType: String, modifier: Modifier = Modifier) {
    val logoResource = when (cardType.lowercase()) {
        "visa" -> R.drawable.logo_visa
        "mastercard" -> R.drawable.logo_mastercard
        "american express", "amex" -> R.drawable.logo_amex
        "discover" -> R.drawable.logo_discover
        else -> R.drawable.ic_credit_card_generic
    }
    
    Image(
        painter = painterResource(logoResource),
        contentDescription = cardType,
        modifier = modifier.height(32.dp)
    )
}
```

### Logo Requirements

**Visa:**
- Must use official Visa logo
- Blue and gold colors must be accurate
- Minimum height: 8mm (30 pixels)

**Mastercard:**
- Use current red and yellow overlapping circles
- Do not use outdated Mastercard logos
- Maintain proper aspect ratio

**American Express:**
- Use official blue American Express logo
- "American Express" text must be legible

**Discover:**
- Orange and black Discover logo
- Network mark must be clearly visible

### Where to Get Logos

- **Visa:** [Visa Brand Mark](https://usa.visa.com/about-visa/visa-brand-center.html)
- **Mastercard:** [Mastercard Brand Center](https://brand.mastercard.com/)
- **American Express:** [Amex Brand Guidelines](https://www.americanexpress.com/us/merchant/supplies-and-services.html)
- **Discover:** [Discover Brand Guidelines](https://www.discover.com/)

---

## Contactless Symbols

### NFC / Contactless Indicator

Your app should display the contactless symbol in these locations:

#### 1. Payment Method Selection

```kotlin
@Composable
fun PaymentMethodOptions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PaymentMethodCard(
            icon = R.drawable.emvco_contactless_symbol,
            label = "Tap to Pay",
            description = "Contactless payments"
        )
        
        PaymentMethodCard(
            icon = R.drawable.ic_card_reader,
            label = "Card Reader",
            description = "External reader"
        )
    }
}
```

#### 2. Transaction Screens

```kotlin
@Composable
fun TapCardScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Required contactless symbol
        Image(
            painter = painterResource(R.drawable.emvco_contactless_symbol),
            contentDescription = "Contactless Payment",
            modifier = Modifier.size(100.dp)
        )
        
        Text("Hold card near top of device")
        
        // Optional: Show where to position card
        PhoneWithCardPositionDiagram()
    }
}
```

#### 3. App Marketing

Display the contactless symbol in:
- App Store screenshots showing payment features
- Marketing materials
- Help documentation
- Tutorial screens

---

## Receipt Requirements

### Required Information

Every receipt must include:

```kotlin
data class Receipt(
    // REQUIRED fields
    val merchantName: String,
    val merchantAddress: String,
    val transactionId: String,
    val transactionDate: String,
    val transactionTime: String,
    val cardType: String,
    val lastFourDigits: String,
    val amount: Double,
    val transactionType: String, // "SALE", "REFUND", "VOID"
    
    // OPTIONAL but recommended
    val authCode: String? = null,
    val aidLabel: String? = null, // Application Identifier
    val entryMode: String? = null, // "CONTACTLESS", "INSERT", "SWIPE"
    val tip: Double? = null,
    val tax: Double? = null,
    val subtotal: Double? = null
)

fun generateReceipt(transaction: Transaction): String {
    return """
        ${getMerchantInfo().name}
        ${getMerchantInfo().address}
        ${getMerchantInfo().phone}
        
        --------------------------------
        
        Date: ${formatDate(transaction.created_at)}
        Time: ${formatTime(transaction.created_at)}
        Transaction ID: ${transaction.id}
        
        --------------------------------
        
        ${transaction.cardType.uppercase()} •••• ${transaction.lastFour}
        Entry Method: CONTACTLESS
        
        ${if (transaction.authCode != null) "Auth Code: ${transaction.authCode}\n" else ""}
        --------------------------------
        
        Subtotal:        ${"$%.2f".format(transaction.subtotal ?: transaction.total)}
        ${if (transaction.tax != null) "Tax:             ${"$%.2f".format(transaction.tax)}\n" else ""}
        ${if (transaction.tipAmount != null) "Tip:             ${"$%.2f".format(transaction.tipAmount)}\n" else ""}
        
        Total:           ${"$%.2f".format(transaction.total)}
        
        --------------------------------
        
        ${transaction.transactionType.uppercase()}
        
        ${if (transaction.isVoided == true) "*** VOIDED ***\n" else ""}
        
        Thank you!
    """.trimIndent()
}
```

### Receipt Format Example

```
ABC Coffee Shop
123 Main Street
San Francisco, CA 94102
(415) 555-1234

--------------------------------

Date: January 15, 2025
Time: 2:34 PM
Transaction ID: txn_abc123xyz

--------------------------------

VISA •••• 1234
Entry Method: CONTACTLESS

Auth Code: 123456

--------------------------------

Subtotal:        $12.50
Tax:             $1.13
Tip:             $2.50

Total:           $16.13

--------------------------------

SALE

Thank you!
```

### Digital Receipt

```kotlin
fun sendEmailReceipt(transaction: Transaction, email: String) {
    val receipt = generateReceipt(transaction)
    val subject = "Receipt from ${getMerchantInfo().name}"
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, receipt)
    }
    
    startActivity(Intent.createChooser(intent, "Send receipt via"))
}

fun sendSmsReceipt(transaction: Transaction, phone: String) {
    val shortReceipt = """
        ${getMerchantInfo().name}
        ${transaction.transactionType}: ${"$%.2f".format(transaction.total)}
        ${transaction.cardType} •••• ${transaction.lastFour}
        ${formatDate(transaction.created_at)}
        ID: ${transaction.id}
    """.trimIndent()
    
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("sms:$phone")
        putExtra("sms_body", shortReceipt)
    }
    
    startActivity(intent)
}
```

---

## Data Security

### PCI DSS Compliance

The Stax SDK is PCI compliant, but you must follow these rules:

#### ✅ DO

- **Use the SDK** - All card data flows through secure SDK methods
- **Store transaction IDs** - Safe to store transaction IDs for history
- **Store last 4 digits** - OK to display last 4 digits of card
- **Log transaction events** - Log actions like "payment started", "payment completed"

#### ❌ DON'T

- **Never store full card numbers** - SDK handles this
- **Never log card data** - Don't log PAN, CVV, PIN
- **Never transmit card data** - Don't send to your own servers
- **Never store security codes** - CVV, CVV2, PIN

### Secure Data Handling

```kotlin
// ✅ GOOD - Safe to store
data class TransactionRecord(
    val transactionId: String,
    val timestamp: Long,
    val amount: Double,
    val lastFour: String,
    val cardType: String,
    val status: String
)

// ❌ BAD - Never store this
data class BadTransactionRecord(
    val fullCardNumber: String,  // ❌ NEVER
    val cvv: String,              // ❌ NEVER
    val pin: String,              // ❌ NEVER
    val track1Data: String,       // ❌ NEVER
    val track2Data: String        // ❌ NEVER
)

// ✅ GOOD - Storing transaction history
fun saveTransaction(transaction: Transaction) {
    val record = TransactionRecord(
        transactionId = transaction.id!!,
        timestamp = System.currentTimeMillis(),
        amount = transaction.total!!,
        lastFour = transaction.lastFour!!,
        cardType = transaction.cardType!!,
        status = if (transaction.success == true) "APPROVED" else "DECLINED"
    )
    
    database.insert(record)
}
```

### Logging Best Practices

```kotlin
// ✅ GOOD - Safe logging
Log.d("Payment", "Transaction started, amount: $amount")
Log.d("Payment", "Transaction completed, ID: ${transaction.id}")
Log.d("Payment", "Card type: ${transaction.cardType}")

// ❌ BAD - Never log card data
Log.d("Payment", "Card number: ${cardNumber}")  // ❌ NEVER
Log.d("Payment", "Track data: ${trackData}")    // ❌ NEVER
Log.d("Payment", "CVV: ${cvv}")                 // ❌ NEVER
```

---

## Certification Process

### Google Wallet SDK Compliance

To publish your app with Tap to Pay functionality:

1. **Register with Google**
   - Access Google SDK Console
   - Submit app for review
   - Provide test credentials

2. **Stax Account Setup**
   - Contact Stax to set up merchant account
   - Receive API keys for development and production
   - Complete merchant onboarding

3. **Testing Phase**
   - Test with physical cards
   - Verify all card brands (Visa, Mastercard, Amex, Discover)
   - Test decline scenarios
   - Validate receipt output

4. **Submit for Review**
   - Submit app to Google SDK Console
   - Google reviews app for compliance
   - Address any feedback

5. **Production Approval**
   - Receive production approval from Google
   - Switch to production API keys
   - Launch app on Google Play

### Timeline

- **Development:** 1-2 weeks
- **Internal testing:** 1 week
- **Google review:** 1-2 weeks
- **Total:** Approximately 3-5 weeks

### Checklist

Before submitting for review:

- [ ] EMVCo contactless symbol displayed correctly
- [ ] Card brand logos are official versions
- [ ] Receipts include all required fields
- [ ] No card data is stored or logged
- [ ] Tested with all card brands
- [ ] Tested decline scenarios
- [ ] UI follows card brand guidelines
- [ ] App handles network errors gracefully
- [ ] Refund and void functionality works
- [ ] Production API keys configured

---

## Resources

### Official Guidelines

- **EMVCo:** https://www.emvco.com/
- **PCI Security Standards:** https://www.pcisecuritystandards.org/
- **Visa:** https://usa.visa.com/
- **Mastercard:** https://www.mastercard.us/
- **American Express:** https://www.americanexpress.com/
- **Discover:** https://www.discover.com/

### Stax Resources

- **Developer Support:** developer@staxpayments.com
- **Merchant Onboarding:** onboarding@staxpayments.com
- **Technical Documentation:** https://www.staxpayments.com/developers

---

## Next Steps

- **[Production Deployment](07-production-deployment.md)** - Launch checklist
- **[Testing](06-testing.md)** - Comprehensive testing guide
- **[UI/UX Guidelines](04-ui-ux-guidelines.md)** - Design best practices
