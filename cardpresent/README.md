# Omni Cardpresent


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
implementation 'com.github.fattmerchantorg:fattmerchant-android-sdk:v1.0.4'
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


## Connect a Mobile Reader
> Before connecting to a Miura reader, the reader must be paired to the Android device within the Settings app


In order to connect a mobile reader, you must first search for a list of available readers

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

## Taking a Payment
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


