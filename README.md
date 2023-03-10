# Stax Android SDK

![Release](https://jitpack.io/v/fattmerchantorg/fattmerchant-android-sdk.svg)

The Stax Android SDK provides a simple way to accept a payment on your Android app by providing an interface for hardware credit card readers. By using a few small code snippets, you can connect to our readers and take payments easily.

* [Requirements](#requirements)
* [Installation](#installation)
  * [JitPack](#jitpack)
  * [Adding the SDK](#add-dependency)
* [Getting Started](#getting-started)
  * [Initialization](#initialization)
  * [Connecting to Readers](#connecting-to-readers)
  * [Taking a Payment](#taking-a-payment)
  * [Pre Authorizing a Transaction](#preauths)
  * [Voiding a Transaction](#voids)
  * [Refunding a Transaction](#refunds)
* [Hardware](#hardware)
* [Advanced Functionality](#advanced)
  * [Subscribing to Transaction Updates](#subcribing-to-transaction-updates)
  * [Providing a Signature](#providing-a-signature)
* [Sample](#sample)

![Tokenization Info](https://raw.githubusercontent.com/fattmerchantorg/Fattmerchant-iOS-SDK/master/assets/images/tokenization-info.png)


## <a name="requirements">Requirements</a>

The Stax Android SDK requires a `minSdk` version of Android 6.0 (API Level 23). Stax recommends setting your `targetSdk` and `compileSdk` versions to the latest available (API Level 33).

## <a name="installation">Installation</a>

To install the Stax Android SDK, you'll need to add [JitPack](https://jitpack.io) to your sources in your project's root-level gradle files.

### JitPack

To add JitPack as a repository to your project, you can use the following snippets below. Remember to add these to your root-level gradle files.

#### Gradle
```groovy
repositories {
    // other repositories ...
    maven { url 'https://jitpack.io' }
}
```

#### Kotlin DSL
```groovy
repositories {
    // other repositories ...
    maven(url = "https://jitpack.io")
}
```

### Add Dependency

Once JitPack has been added as a repository to your project, you can add the dependency like any other Android dependency.

#### Gradle

```groovy
implementation 'com.github.com.fattmerchantorg:fattmerchant-android-sdk:cardpresent:2.5.0'
```

#### Kotlin DSL

```groovy
implementation("com.github.fattmerchantorg:fattmerchant-android-sdk:cardpresent:2.5.0")
```


## <a name="getting-started">Getting Started</a>

To accept a payment, you'll need to have the following items:
- A Stax API Key
- A Stax Credit Card Reader

If you have questions about either one of the above items, you can contact support@staxpayments.com

### Initialization

First, you'll need to initialize the main Stax SDK. To do this, you can use the `Stax.initialize()` function. You can initialize at any time. If you try using any other Stax function before initializing, you may encounter errors or crashes.

#### Kotlin
```kotlin
Stax.initialize(
    context =           // The app's ApplicationContext
    application = ,     // The app's customer Application class
    apiKey = ,          // Your Stax API Key
    onCompletion = {    // A function called when successfully initialized

    },
    onError = {         // A function called when an error is thrown while initializing

    }
)
```

#### Java
```java
// TODO: Documentation for Java coming soon!
```

### Connecting to Readers

After initializing you can search and connect for readers. This functionality requires `ACCESS_FINE_LOCATION` permissions on all Android devices. On Android 12 (API Level 31) and up, you'll additionally need `BLUETOOTH_CONNECT` and `BLUETOOTH_SCAN`. These permissions are _required_ to use the hardware readers. If you do not prompt for these permissions, you will run into errors and crashes.

Communicating with a reader is split into two parts: **searching** for readers, and **connecting** to readers.

To search for readers, you can call `Stax.instance().getAvailableReaders()`. This function takes in a callback that returns a list of mobile readers that you can connect to over Bluetooth Low Energy.

####  Kotlin
```kotlin
Stax.instance().getAvailableReaders { readers: List<MobileReader> ->
    // Perform functionality with readers here
}
```

#### Java
```java
// TODO: Documentation for Java coming soon!
```

Now that you have a list of readers to connect to, you can pick one of them and initiate a connection. 

#### Kotlin
```kotlin
val selectedReader: MobileReader // The reader we selected from `Stax.instance().getAvailableReaders()`

Stax.instance().connectReader(
    mobileReader = selectedReader,
    onConnected = { reader ->
        // The success callback
    },
    onFail = { errorMsg ->
        // The failure callback
    },
)
```

#### Java
```java
// TODO: Documentation for Java coming soon!
```

After selecting and connecting to a reader, the SDK manages further connectivity with the hardware. You'll have the ability to disconnect and read the reader's diagnostics; but as long as the reader stays connected, you won't have to call `Stax.instance().connectReader()` again. 

> Note: If you reader is flashing white & blue, it is in the process of updating it's firmware to be compatible with our latest security standards. This can take up to 30 minutes. This process happens annually and we will attempt to send notice. We are working on additional functionality to our SDK in order to allow you to control firmware updates more easily.

### Taking a Payment

After you've initialized the Stax SDK and connected to a specific reader, you can take a payment. To take a payment, you'll create a `TransactionRequest` which takes in an `Amount`.

#### Kotlin
```kotlin
// Create a 1¢ amount
val amount = Amount(1)

// Create a `TransactionRequest` using the amount
val transactionRequest = TransactionRequest(amount)

// Initiate a payment
Stax.instance().takeMobileReaderTransaction(
    request = transactionRequest,
    onCompletion = { transaction ->
        // Called if the transaction, whether it failed or was successful
        // The transaction result is logged in the Stax Database
    },
    onError = {
        // Called if an exception occured during the transaction
        // The transaction result is _not_ logged in the Stax Database
    }
)
```

#### Java
```java
// TODO: Documentation for Java coming soon!
```

In the above minimal example, a new payment, invoice, and customer will all be created. However, you can attach customer IDs, invoice IDs, and more to help add granularity to your transactions.

### PreAuths

To run a pre-authorization, take the above code and add `preauth = true` to the `TransactionRequest`

#### Kotlin
```kotlin
// Create a $0.01 amount
val amount = Amount(0.01)

// Create a `TransactionRequest` using the amount
val transactionRequest = TransactionRequest(amount)
transactionRequest.preauth = true

// Initiate a payment
Stax.instance().takeMobileReaderTransaction(
    request = transactionRequest,
    onCompletion = { transaction ->
        // Called if the transaction, whether it failed or was successful
        // The transaction result is logged in the Stax Database
    },
    onError = {
        // Called if an exception occured during the transaction
        // The transaction result is _not_ logged in the Stax Database
    }
)
```

#### Java
```java
// TODO: Documentation for Java coming soon!
```

To capture the PreAuth, you can call the `Stax.instance().capturePreAuthTransaction()` function

#### Kotlin
val id = "MyTransactionId"
val amount = Amount(100)

// Capture $1.00
Stax.instance().capturePreAuthTransaction(
    transactionId = id,
    amount = amount, /
    onCompletion = { transaction ->
        if (transaction.success) {
            // Logged to Stax Pay as a success
        } else {
            // Logged to Stax Pay as a failure
        }
    },
    onError = {
        // Not logged to Stax Pay
    }
)

#### Java
```java
// TODO: Documentation for Java coming soon!
```

To capture the full amount of the pre authorization, you can leave the `Amount` parameter blank or pass in `null`.

### Voids

To void a previous transaction, you can use the `Stax.instance().voidTransaction()` function. If you attempt to void a transaction that has already settled, you may run into issues. If a transaction was made a few days ago, or it has already settled, it is better to do a refund.

#### Kotlin

```kotlin
val id = "MyTransactionId"
Stax.instance().voidTransaction(
    transactionId = id,
    onCompletion = { transaction ->
        if (transaction.success) {
            // Logged to Stax Pay as a success
        } else {
            // Logged to Stax Pay as a failure
        }
    },
    onError = {
        // Not logged to Stax Pay
    }
)
```

#### Java
```java
// TODO: Documentation for Java coming soon!
```

### Refunds

To initiate a refund via the mobile readers, you can run `Stax.instance().refundMobileReaderTransaction()`. However, in most cases, it would be better to void the transaction if it is the same day. Alternatively, you can process a refund via the Stax API so that hardware is not required.

#### Kotlin

```kotlin
val myTransaction = Foo.getMyRefundTransaction()
Stax.instance().refundMobileReaderTransaction(
    transaction = myTransaction,
    onCompletion = { transaction ->
        if (transaction.success) {
            // Logged to Stax Pay as a success
        } else {
            // Logged to Stax Pay as a failure
        }
    },
    onError = {
        // Not logged to Stax Pay
    }
)
```

#### Java
```java
// TODO: Documentation for Java coming soon!
```

## Hardware

Below are the list of hardware readers provided by Stax

### Pairing the Reader
**Miura M010 (LEGACY)** 

1. Turn the Miura M010 mobile reader on and wait until the display shows "MIURA SYSTEMS" along with a bluetooth indicator.
2. Press and hold the bluetooth indicator button until it flashes rapidly (this lets you know it is in discovery mode)
3. Find the Miura on your Android device's list of bluetooth devices and pair it

**BBPOS Chipper 2XBT**

1. Turn the BBPOS Chipper 2XBT mobile reader on by holding the power button
2. Wait until the LED indicator repeatedly flashes blue (this lets you know it is in discovery mode)
3. In the Android device's list of bluetooth devices, find the BBPOS device. Note that the mobile reader will appear twice; once with a phone icon and another time with a Bluetooth icon. Choose the one with the phone icon and pair.

## Advanced

### Subcribing to Transaction Updates
To receive Transaction updates, register a `TransactionUpdateListener` on the Stax object. This object will receive transaction updates such as:

- `PromptSwipeCard` - The mobile reader is waiting for a card to be swiped
- `CardSwiped` - A card was swiped on the mobile reader
- `Authorizing` - The payment is being authorized
- `PromptProvideSignature` - The payment requires a signature

These will be instances of `TransactionUpdate`, and will each have a `value` and a `userFriendlyMessage`. The `value` is a key you can use to identify the event, and the `userFriendlyMessage` is a string that can be shown to an end user, should you choose to.

#### Kotlin
```kotlin
// Register to listen to the transaction events
Stax.instance().transactionUpdateListener = object : TransactionUpdateListener {
    override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
        print("${transactionUpdate.value} | ${transactionUpdate.userFriendlyMessage}")
    }
}

// Begin the transaction
Stax.instance().takeMobileReaderTransaction(...) 
```

#### Java
```java
// TODO: Documentation for Java coming soon!
```

### Providing a Signature

Should a transaction require a signature, one can be provided by registering a `SignatureProviding` on the Stax object. This object will be required to implement a method called 

```kotlin
/**
 * Called when a transaction requires a signature
 * @param onCompletion a block to run once the signature is complete. This should be given the
 * signature as a Base64 encoded string
 */
fun signatureRequired(onCompletion: (String) -> Unit)
```

You can then pass a base64 encoded string representation of the signature and pass it to the completion block.

```kotlin
Stax.instance().signatureProvider = object : SignatureProviding {
  override fun signatureRequired(onCompletion: (String) -> Unit) {
       var base64EncodedSignature = // ...
       onCompletion(base64EncodedSignature)
  }
}
```

## Sample

A sample application is provided in the `app` directory of this project. It is a simple Jetpack Compose app that you can install on your own device.

To use it, you can:
- Clone this repository
- Add a value for staxApiKey to the `local.properties` file
- Run the `app` module from Android Studio

### Adding your Stax API Key to the sample

To test the sample app with your personal Stax API Key, modify your local.properties file at the root of this project to add the following line.

```groovy
staxApiKey="MyStaxApiKey"
```