# Tap to Pay on Android - Setup Guide

## Current Status
The Tap to Pay infrastructure is implemented but not functional due to:
1. Cloud Commerce SDK initialization issues when extending `ChipDnaApplication`
2. Missing certificate fingerprint configuration
3. Internal API visibility issues (`ChipDnaDriver` is internal)

## What's Already Implemented ✅

### 1. SDK Integration
- ✅ Cloud Commerce SDK 5.3.0 integrated (`cardpresent/libs/cloud-commerce-sdk-5.3.0.aar`)
- ✅ ChipDNA Mobile SDK integrated
- ✅ IDTech Universal SDK for device support

### 2. Code Infrastructure
- ✅ `TapToPayConfiguration` data class with 3 modes (Disabled, Tap to Pay Only, Hybrid)
- ✅ `ChipDnaDriver` with Tap to Pay initialization logic
- ✅ `RequestActivityDelegate` for providing Activity context to NFC operations
- ✅ `TapToPayReader` virtual reader implementation
- ✅ UI components (`TapToPayPrompt`) for NFC payment flow
- ✅ Sample app UI with Tap to Pay mode selection

### 3. Permissions & Manifest
- ✅ NFC permissions configured
- ✅ NFC hardware features declared
- ✅ Target SDK 36 with proper permission handling
- ✅ Large heap enabled for SDK memory requirements

## What Needs to Be Done 🔧

### 1. **Fix Cloud Commerce SDK Initialization**

**Problem:** `MainApplication` crashes when extending `ChipDnaApplication`
```
ArrayIndexOutOfBoundsException in com.mastercard.cpos.facade.CposApplication.onCreate()
```

**Solutions to Try:**

#### Option A: Proper Cloud Commerce SDK Configuration
The SDK likely requires configuration files or specific initialization sequence:

```kotlin
// MainApplication.kt
class MainApplication : ChipDnaApplication() {
    override fun onCreate() {
        // BEFORE calling super.onCreate(), may need to:
        // 1. Configure terminal settings
        // 2. Set up SDK properties
        // 3. Initialize required components
        
        super.onCreate()  // This is where it crashes
        
        context = applicationContext
        application = this
    }
}
```

**Action Items:**
- [ ] Contact NMI/Mastercard for Cloud Commerce SDK documentation
- [ ] Check if terminal.json configuration needs updates
- [ ] Verify if SDK requires specific asset files
- [ ] Check if there's a separate initialization method before `onCreate()`

#### Option B: Alternative Initialization
```kotlin
// Don't extend ChipDnaApplication, initialize programmatically
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Cloud Commerce SDK manually
        // (requires finding the correct API)
    }
}
```

### 2. **Get Certificate Fingerprint** (Required for Production)

**What:** SHA-256 fingerprint of your app's signing certificate (Base64 encoded)

**How to Generate:**
```bash
# For debug keystore
keytool -list -v -keystore ~/.android/debug.keystore \
    -alias androiddebugkey -storepass android -keypass android | \
    grep SHA256 | cut -d' ' -f3 | xxd -r -p | base64

# For production keystore
keytool -list -v -keystore /path/to/your/keystore.jks \
    -alias your-key-alias | \
    grep SHA256 | cut -d' ' -f3 | xxd -r -p | base64
```

**Where to Add:**
```kotlin
// StaxViewModel.kt line 156
TapToPayConfiguration.tapToPayOnly(
    certificateFingerprint = "YOUR_BASE64_FINGERPRINT_HERE",  // <-- Add here
    testMode = true
)
```

### 3. **Make ChipDnaDriver API Public**

**Problem:** `ChipDnaDriver` and `RequestActivityDelegate` are `internal`

**Current State:**
```kotlin
// ChipDnaDriver.kt
internal class ChipDnaDriver : MobileReaderDriver { ... }

// RequestActivityDelegate.kt  
class RequestActivityDelegate(
    private val activityProvider: () -> Activity?
) {
    internal fun onRequestActivity() { ... }  // Called by ChipDNA SDK
}
```

**Solution:**
```kotlin
// Make public so app module can use them
class ChipDnaDriver : MobileReaderDriver { ... }

// And register the delegate
fun registerRequestActivityListener(delegate: RequestActivityDelegate)
```

**Action Items:**
- [ ] Change `internal class ChipDnaDriver` to `class ChipDnaDriver`
- [ ] Add public method in `Omni` to register Activity delegate:
```kotlin
// Omni.kt
fun registerTapToPayActivityDelegate(activityProvider: () -> Activity?) {
    val driver = mobileReaderDriverRepository.drivers
        .firstOrNull() as? ChipDnaDriver
    driver?.registerRequestActivityListener(
        RequestActivityDelegate(activityProvider)
    )
}
```

### 4. **Test Mode Configuration**

**Current:** Using `testMode = true` which requires MTF SDK variant

**Files:**
- Production: `cloud-commerce-sdk-5.3.0.aar`
- Test: `cloud-commerce-sdk-mtf-5.3.0.aar` (currently commented out)

**To Switch to Test Mode:**
```kotlin
// cardpresent/build.gradle.kts
compileOnly(files("libs/cloud-commerce-sdk-mtf-5.3.0.aar"))  // Uncomment
// compileOnly(files("libs/cloud-commerce-sdk-5.3.0.aar"))   // Comment out

// app/build.gradle.kts
implementation(files("../cardpresent/libs/cloud-commerce-sdk-mtf-5.3.0.aar"))
// implementation(files("../cardpresent/libs/cloud-commerce-sdk-5.3.0.aar"))
```

**Note:** Can only use ONE SDK variant at a time (duplicate class issues)

### 5. **Google Play Integrity API** (Production Requirement)

For production Tap to Pay, NMI requires app attestation via Google Play Integrity API:

```kotlin
// Already in dependencies (cardpresent/build.gradle.kts)
implementation("com.google.android.play:integrity:1.3.0")
```

**Verify Integrity API Integration:**
- [ ] Ensure app is published to Google Play (internal test track OK)
- [ ] Configure Play Console for integrity API
- [ ] Test attestation flow

### 6. **NFC Testing Requirements**

**Hardware:**
- Android device with NFC (API 30+)
- For production: Device must pass Google's Tap to Pay certification

**Test Cards:**
- Test mode: Use NMI Test Card Simulator app
- Production mode: Use real contactless cards

## Recommended Implementation Steps

1. **Phase 1: Fix Initialization (Critical)**
   - Contact NMI for Cloud Commerce SDK setup guide
   - Get proper initialization sequence
   - Fix `ChipDnaApplication` crash

2. **Phase 2: Test Mode Setup**
   - Switch to MTF SDK variant
   - Generate test certificate fingerprint
   - Test with NMI Test Card Simulator

3. **Phase 3: Make APIs Public**
   - Expose ChipDnaDriver functionality
   - Enable Activity delegate registration
   - Update sample app to use public APIs

4. **Phase 4: Production Setup**
   - Switch to production SDK
   - Generate production certificate fingerprint
   - Set up Play Integrity API
   - Test with real cards

5. **Phase 5: Certification**
   - Submit for Google Tap to Pay certification
   - Complete NMI onboarding requirements
   - Production deployment

## Key Files Reference

| File | Purpose |
|------|---------|
| `TapToPayConfiguration.kt` | Configuration modes |
| `ChipDnaDriver.kt` | Main driver with NFC logic |
| `RequestActivityDelegate.kt` | Activity provider for NFC |
| `TapToPayPrompt.kt` | UI component for payment flow |
| `MainApplication.kt` | App initialization (currently broken) |
| `StaxViewModel.kt` | ViewModel with Tap to Pay config |
| `terminal.json` | Cloud Commerce SDK config |

## Questions for NMI/Mastercard Support

1. What is the correct initialization sequence for Cloud Commerce SDK?
2. Are there required configuration files beyond terminal.json?
3. How to properly extend ChipDnaApplication without crashes?
4. What are the complete Tap to Pay certification requirements?
5. Documentation for test mode setup with MTF SDK?

## Next Immediate Action

**Contact NMI Support** to get:
- Cloud Commerce SDK initialization documentation
- Tap to Pay on Android setup guide
- Sample application reference (if available)
- Test credentials and certification process details
