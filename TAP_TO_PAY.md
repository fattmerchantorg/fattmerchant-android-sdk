# Tap to Pay on Android

Contactless NFC payments without an external card reader, using the NMI Cloud Commerce SDK 5.3.1.

## Architecture

```
Consumer App
  ├── cardpresent (Stax SDK — includes ChipDNA driver + all transitive deps)
  └── Cloud Commerce AAR (added locally per NMI docs — NOT served through our SDK)

cardpresent internals:
  Omni (public API)
    → ChipDnaDriver (NMI ChipDNA driver)
      → TapToPayConfiguration (NFC config)
      → RequestActivityDelegate (Activity context for NFC UI)
      → TapToPayReader (virtual MobileReader for NFC)
    → Cloud Commerce SDK (compileOnly — consumer provides AAR at runtime)
```

## Cloud Commerce SDK Integration

The Cloud Commerce AAR is **not bundled** in our SDK. Consumer apps add it locally following the [NMI documentation](https://docs.nmi.com/docs/preparing-for-development-android).

### CloudCommerceSDK Module Pattern

Both the SDK sample app and fattpay-android use a local `CloudCommerceSDK` module — matching the [ChipDnaMobileKotlinDemo](https://docs.nmi.com/docs/preparing-for-development-android) reference project:

```
CloudCommerceSDK/
  cloud-commerce-sdk-5.3.1.aar       ← Production
  cloud-commerce-sdk-mtf-5.3.1.aar   ← MTF/sandbox
  build.gradle.kts                   ← Exposes mtf/prod configurations
```

**build.gradle.kts:**
```kotlin
val mtf = configurations.create("mtf")
val prod = configurations.create("prod")

artifacts {
    add("mtf", file("./cloud-commerce-sdk-mtf-5.3.1.aar"))
    add("prod", file("./cloud-commerce-sdk-5.3.1.aar"))
}
```

**Consumer app dependency:**
```kotlin
// Stax SDK (includes ChipDNA driver + all Cloud Commerce transitive deps)
implementation("com.github.fattmerchantorg.fattmerchant-android-sdk:cardpresent:<version>")

// Cloud Commerce AAR — select by build type or flavor
debugImplementation(project(path = ":CloudCommerceSDK", configuration = "mtf"))
releaseImplementation(project(path = ":CloudCommerceSDK", configuration = "prod"))
```

All Cloud Commerce transitive deps (Retrofit, RxJava, SQLCipher, OkHttp, Play Services, etc.) are bundled in `cardpresent` — consumers don't need to add them.

### Updating the Cloud Commerce SDK

When NMI releases a new version:
1. Replace AARs in `CloudCommerceSDK/`, `cloudcommerce-production/libs/`, and `cloudcommerce-mtf/libs/`
2. Update version strings in `cloudcommerce-production/build.gradle.kts` and `cloudcommerce-mtf/build.gradle.kts`

## Consumer App Setup

### 1. Application Class

The app must extend `ChipDnaApplication`:

```kotlin
class MyApplication : ChipDnaApplication() {
    override fun onCreate() {
        super.onCreate()
        // Your init code
    }
}
```

### 2. AndroidManifest

```xml
<uses-feature android:name="android.hardware.nfc" android:required="true" />
<uses-permission android:name="android.permission.NFC" />
```

### 3. SDK Initialization

```kotlin
Omni.initialize(
    params = InitParams(
        apiKey = "YOUR_API_KEY",
        appContext = context,
        appId = "your.app.id",
        environment = Environment.LIVE,
        sandBoxKey = false,  // true for MTF/sandbox
        tapToPayConfig = TapToPayConfiguration.tapToPayOnly(
            certificateFingerprint = null,  // auto-extracted from signing cert
            testMode = false
        )
    ),
    completion = { /* ready */ },
    error = { e -> /* handle */ }
)

// Register Activity provider for NFC UI
Omni.registerTapToPayActivityProvider { currentActivity }
```

### 4. Configuration Modes

```kotlin
// NFC only (no external readers)
TapToPayConfiguration.tapToPayOnly(certificateFingerprint = null, testMode = false)

// NFC + external Bluetooth/USB readers
TapToPayConfiguration.hybrid(certificateFingerprint = null, testMode = false)

// External readers only (disable NFC)
TapToPayConfiguration.externalReadersOnly()
```

### 5. Transaction Flow

1. Initialize SDK with `TapToPayConfiguration`
2. Register Activity provider via `Omni.registerTapToPayActivityProvider()`
3. `searchForReaders()` returns `TapToPayReader` (virtual NFC reader)
4. `connectReader()` succeeds immediately (no physical pairing)
5. `takeMobileReaderTransaction()` triggers NFC prompt
6. User taps card/device → `CardTapped` event
7. Transaction processes → result returned

## NMI App Onboarding (Required)

**Without NMI registration, Tap to Pay will not work.** You will get a `RegistrationRequired` error on `connectAndConfigure`.

Email **taptopay-app-onboarding@nmi.com** with:

1. **App Package Name** (e.g., `com.fattmerchant.fattpay`)
2. **Application Identifier** (e.g., `fattpayandroid`)
3. **Certificate SHA-256 fingerprints:**
   - Debug signing cert
   - Production cert (from Play Console → Release → Setup → App signing → SHA-256)
4. **Environment requested** (MTF first, then production)

Generate certificate fingerprint:
```bash
keytool -list -v -keystore ~/.android/debug.keystore \
    -alias androiddebugkey -storepass android -keypass android | grep SHA256
```

The SDK auto-extracts the certificate fingerprint at runtime via `CertificateUtils.getCertificateFingerprint(context)` — you don't need to hardcode it unless overriding.

### Current Registration Status

| App | Package | Status |
|-----|---------|--------|
| Sample app (chipdnatest) | `com.creditcall.chipdnamobiledev` | Pre-registered by NMI |
| Stax Pay (fattpay-android) | `com.fattmerchant.fattpay` | Pending NMI registration |

## Key Implementation Details

### Certificate Fingerprint

The SDK handles empty/null fingerprints gracefully:
- `ChipDnaDriver.kt`: `config.certificateFingerprint?.takeIf { it.isNotEmpty() } ?: auto-extract`
- Auto-extraction reads from `PackageManager.GET_SIGNING_CERTIFICATES`

### RequestActivityDelegate

`onRequestActivity()` calls `ChipDnaMobile.getInstance().continueRequestedActivity(activity)` on the main thread — this is required to unblock NFC transactions.

### connectAndConfigure Parameters

- `TapToMobilePOI = TRUE` — enables NFC
- `PaymentDevicePOI = FALSE` — TTM-only mode (no external readers)
- `ApplyFirmwareUpdate = FALSE`
- `IRequestActivityListener` must be registered at `initialize()` time

### Transaction Updates (NFC-specific)

| ChipDNA Event | SDK TransactionUpdate |
|--------------|----------------------|
| `ContactlessCardDetected` | `CardTapped` |
| `PresentCard` | `PromptTapCard` |

## Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| `RegistrationRequired` error | App not registered with NMI | Complete NMI onboarding (email above) |
| NFC prompt doesn't appear | Activity not registered | Call `Omni.registerTapToPayActivityProvider()` |
| `connectAndConfigure` hangs | Terminal not provisioned by NMI | Complete onboarding; use chipdnatest flavor to validate code path |
| Empty cert fingerprint rejected | `""` passed instead of null | Fixed in SDK — empty strings treated as null, auto-extracts |

## Testing

### chipdnatest Flavor (SDK sample app)

Pre-registered NMI test identity for validating the code path:
- `applicationId = "com.creditcall.chipdnamobiledev"`
- API key: `7WXb667D6MSj5CcWj85Bf3Jn5F6DqAW5`
- APP_ID: `STAXDEMO`

```bash
./gradlew :app:installChipdnatestDebug
```

If chipdnatest connects successfully, only NMI account authorization blocks production use.

### Test Cards

Use the [NMI Test Card Simulator](https://play.google.com/store/apps/details?id=com.nmi.testcardsimulator) app with MTF SDK and `sandBoxKey = true`.

## References

- [NMI Tap to Pay Documentation](https://docs.nmi.com/docs/mobile-point-of-sale-tap-to-pay-on-android)
- [NMI Preparing for Development (Android)](https://docs.nmi.com/docs/preparing-for-development-android)
- [Google Play Integrity API](https://developer.android.com/google/play/integrity)
