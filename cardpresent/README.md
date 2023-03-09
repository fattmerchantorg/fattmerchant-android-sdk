# Stax Cardpresent


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

* **Ephemeral Stax API Key**: Using your Stax API key, you will need to create an ephemeral token. The cardpresent codebase does not store your token, so you'll need to pass one in every time you create the `Stax` object. [Here](https://fattmerchant.docs.apiary.io/#reference/0/authentication-tokens/generate-an-ephemeral-token) is a helpful guide showing you how to create an ephemeral token
* **App Name**: A name for your app!
* **Mobile Reader**: A Fattmerchant-provided mobile reader


## Initialize

Create an instance of `InitParams`

```kotlin
var initParams = InitParams(applicationContext, ephemeralApiKey, StaxApi.Environment.DEV)
```

Pass the initParams to `Stax.initialize(...)`, along with a completion lambda and an error lambda

```kotlin
Stax.initialize(params, {
	// Success!
    System.out.println("Stax is initialized")
}) {
	// There was an error
}
```

You can now use `Stax.shared()` to get the instance of Stax that you will be using

## Pairing the Reader
**Miura M010**

1. Turn the Miura M010 mobile reader on and wait until the display shows "MIURA SYSTEMS" along with a bluetooth indicator.
2. Press and hold the bluetooth indicator button until it flashes rapidly (this lets you know it is in discovery mode)
3. Find the Miura on your Android device's list of bluetooth devices and pair it

**BBPOS Chipper 2XBT**

1. Turn the BBPOS Chipper 2XBT mobile reader on by holding the power button
2. Wait until the LED indicator repeatedly flashes blue (this lets you know it is in discovery mode)
3. In the Android device's list of bluetooth devices, find the BBPOS device. Note that the mobile reader will appear twice; once with a phone icon and another time with a Bluetooth icon. Choose the one with the phone icon and pair.


## Connect a Mobile Reader
Once the reader has been paired, you should be able to find and connect to it using the fattmerchant android sdk

The first step is to search for a list of available readers

```kotlin
Stax.shared().getAvailableReaders { readers ->
	
}
```

Once you have the list of available ones, you can choose which one you'd like to connect

```kotlin
Stax.shared().getAvailableReaders { readers ->
	Stax.shared().connectReader(mobileReader, onConnected: { reader ->
		// Reader is connected
	}, onFail: { error ->
		// Error connecting reader
	}
}
```

## Taking a Payment
To take a payment, simply create a `TransactionRequest` and pass it along to `Stax.takeMobileReaderTransaction(...)`

```kotlin
// Create an Amount
var amount = Amount(50)
    
// Create the TransactionRequest
var request = TransactionRequest(amount)
    
// Take the payment
Stax.shared()?.takeMobileReaderTransaction(request, {
    // Payment successful!
}) {
    // Error
}
```

## Subcribing to Transaction Updates
To receive Transaction updates, register a `TransactionUpdateListener` on the Stax object. This object will receive transaction updates such as:

- `PromptSwipeCard` - The mobile reader is waiting for a card to be swiped
- `CardSwiped` - A card was swiped on the mobile reader
- `Authorizing` - The payment is being authorized
- `PromptProvideSignature` - The payment requires a signature

These will be instances of `TransactionUpdate`, and will each have a `value` and a `userFriendlyMessage`. The `value` is a key you can use to identify the event, and the `userFriendlyMessage` is a string that can be shown to an end user, should you choose to.

```kotlin
// Register to listen to the transaction events
Stax.shared()?.transactionUpdateListener = object: TransactionUpdateListener {
    override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
        print("${transactionUpdate.value} | ${transactionUpdate.userFriendlyMessage}")
    }
}

// Begin the transaction
Stax.shared()?.takeMobileReaderTransaction(...) 
```

## Providing a Signature
Should a transaction require a signature, one can be provided by registering a `SignatureProviding` on the Stax object. This object will be required to implement a method called 

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
Stax.shared()?.signatureProvider = object : SignatureProviding {
  override fun signatureRequired(completion: (String) -> Unit) {
       var base64EncodedSignature = // ...
       completion(base64EncodedSignature)
  }
}
```


## Refunding a Payment
To refund a payment, you must first get the `Transaction` that you want to refund. You can use the [Stax API](https://fattmerchant.docs.apiary.io/#reference/0/transactions) to do so. 
Once you get the transaction, you can use the `refundMobileReaderTransaction` method to attempt the refund.

> At this time, you may only refund transactions that were performed on the same device that performed the original transaction 

```kotlin
// Attain a transaction
var transaction = Transaction()
    
// Perform refund
Stax.shared()?.refundMobileReaderTransaction(transaction, {
    // Refund successful!
}) {
    // Error
}
```


