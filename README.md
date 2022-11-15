# Fattmerchant Android SDK

![Release](https://jitpack.io/v/fattmerchantorg/fattmerchant-android-sdk.svg)

The Fattmerchant Android SDK provides a simple way to accept a payment on your Android app by providing tokenization of payment methods. By using these tokens instead of card and bank information, you no longer have to worry about sending sensitive card information to your server.

* [Requirements](#requirements)
* [Installation](#installation)
* [Getting Started](#getting-started)
* [Testing](#testing)
* [Taking a payment with a mobile reader](https://fattmerchantorg.github.io/fattmerchant-android-sdk/cardpresent/)

![Tokenization Info](https://raw.githubusercontent.com/fattmerchantorg/Fattmerchant-iOS-SDK/master/assets/images/tokenization-info.png)


## <a name="requirements">Requirements</a>

* Android sdk version 21

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

`implementation 'com.github.fattmerchantorg:fattmerchant-android-sdk:v1.2.0'`

## <a name="getting-started">Getting Started</a>
To accept a payment, you'll need to collect information from the customer, tokenize it, and send the token to your server. Your server will then be responsible for using the Fattmerchant API to run the transaction.

#### Setup

You'll first need to setup the `FattmerchantClient` for usage.  All you have to do here is set the `webPaymentsToken` field on the shared `FattmerchantConfiguration`. `FattmerchantClient` will then use that configuration by default.

##### Kotlin

```kotlin

class MyApplication: Application() {

    override fun onCreate() { 
        super.onCreate()
        FattmerchantConfiguration.shared.webPaymentsToken = "mywebpaymentstoken"
    }
}

```

##### Java

```Java

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FattmerchantConfiguration.shared.webPaymentsToken = "mywebpaymentstoken";
    }
}

```

Alternatively, you may create a configuration object and pass it to the new `FattmerchantApi` instance as you need it.

##### Kotlin

```kotlin
val config = FattmerchantConfiguration("https://apidev01.fattlabs.com", "fattwars")
val client = FattmerchantClient(config)
```

##### Java

```Java
FattmerchantConfiguration config = new FattmerchantConfiguration("https://apidev01.fattlabs.com", "fattwars")
FattmerchantClient client = new FattmerchantClient(config)
```

#### Collect payment information
You first want to collect credit card information and populate a `CreditCard` or a `BankAccount` object.

```kotlin
val creditCard = CreditCard(personName = "Joan Parsnip",
	cardNumber = "4111111111111111",
	cardExp = "1230",
	addressZip = "32822")

// Or for a bank account...
val bankAccount = BankAccount(personName = "Jim Parsnip",
	bankType = "savings",
	bankAccount = "9876543210",
	bankRouting = "021000021",
	addressZip = "32822")
```

#### Get a payment method token
Once you have a `CreditCard` object, call the `tokenize(:)` method on  `FattmerchantClient` object and pass a listener to be notified once tokenization is complete.

```kotlin
var fattClient = FattmerchantClient(config)
fattClient.tokenize(card) { (response) in
  client.tokenize(card, object : FattmerchantClient.TokenizationListener {
            override fun onPaymentMethodCreated(paymentMethod: PaymentMethod) {
              // Success! You can now run a transaction with Fattmerchant using paymentToken as the PaymentMethod
            }

            override fun onPaymentMethodCreateError(errors: String) {
                System.out.print(errors)
            }
        })
}
```

#### Using the token
Now that you have the token representing the payment method, you can send it to your server to run a payment with it. You have to setup a way for your backend to accept the token and create a transaction with it.

## <a name="testing">Testing</a>
If you'd like to try tokenization without real payment information, you can use the `CreditCard.testCreditCard()` or `BankAccount.testBankAccount()` methods to get a test credit card or bank account.

```kotlin
val creditCard = CreditCard.testCreditCard()

val bankAccount = BankAccount.testBankAccount()
```

If you want to test failures, you can use the following methods

```kotlin
val failingCreditCard = CreditCard.failingTestCreditCard()

val failingBankAccount = BankAccount.failingTestBankAccount()
```

Or you can create the `CreditCard` or `BankAccount` object with the following testing payment information:

#### Credit card numbers

| Card Type | Good Card | Bad Card |
|---------|--------------------|-----------|
|VISA|4111111111111111|4012888888881881|
|Mastercard|5555555555554444|5105105105105100|
|Amex|378282246310005|371449635398431|
|Discover|6011111111111117|6011000990139424|
|JCB|3569990010030400|3528327757705979|
|Diners Club|30569309025904|30207712915383|

> Use any CVV number for the above

#### Bank routing & account numbers

* Routing: 021000021
* Account: 9876543210

To test failing bank accounts, use the given routing number and any other account number
