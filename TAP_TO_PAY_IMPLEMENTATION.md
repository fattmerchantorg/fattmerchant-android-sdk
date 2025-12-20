# Tap to Pay on Android Implementation

This document tracks the implementation of Tap to Pay functionality in the Fattmerchant Android SDK to achieve parity with the iOS SDK (PHO-4522 branch).

## Overview

Tap to Pay on Android enables contactless NFC payments without requiring an external card reader. This implementation uses the NMI Cloud Commerce SDK 5.3.0 and integrates with the existing ChipDNA driver architecture.

## Implementation Phases

### Phase 1: Foundation ✅

**Objective**: Add required dependencies and configure application for Tap to Pay support.

#### 1.1 SDK Dependencies
- **File**: `cardpresent/build.gradle.kts`
- **Changes**:
  - Added Cloud Commerce SDK 5.3.0 (production and test variants)
  - Added RxJava 3.1.8 (required by Cloud Commerce SDK)
  - Added Google Play Services Location 21.0.1
  - Added Google Play Services Integrity 1.3.0 (for attestation)
  - Excluded conflicting Apache HTTP legacy library

#### 1.2 Android Manifest
- **File**: `app/src/main/AndroidManifest.xml`
- **Changes**:
  - Added NFC hardware feature requirements
  - Disabled `org.apache.http.legacy` to prevent conflicts

#### 1.3 Application Class
- **File**: `app/src/main/java/com/staxpayments/sample/MainApplication.kt`
- **Changes**:
  - Extended `ChipDnaApplication` instead of `Application`
  - Implemented SDK initialization callbacks

### Phase 2: Driver Enhancement ✅

**Objective**: Add Tap to Pay configuration and driver initialization logic.

#### 2.1 Configuration Model
- **File**: `cardpresent/src/main/java/com/fattmerchant/omni/data/TapToPayConfiguration.kt`
- **Purpose**: Central configuration for Tap to Pay settings
- **Properties**:
  - `enabled`: Enable/disable Tap to Pay
  - `allowExternalReaders`: Allow Bluetooth/USB readers alongside NFC
  - `certificateFingerprint`: Client certificate for attestation
  - `testMode`: Use test environment vs production
- **Factory Methods**:
  - `tapToPayOnly()`: NFC only, no external readers
  - `hybrid()`: NFC + external readers
  - `externalReadersOnly()`: Disable NFC, external readers only

#### 2.2 Activity Delegate
- **File**: `cardpresent/src/main/java/com/fattmerchant/android/chipdna/RequestActivityDelegate.kt`
- **Purpose**: Provide Activity context for NFC UI operations
- **Implementation**: `IRequestActivityListener` with `ActivityHolder` helper

#### 2.3 ChipDnaDriver Initialization
- **File**: `cardpresent/src/main/java/com/fattmerchant/android/chipdna/ChipDnaDriver.kt`
- **Changes**:
  - Added `tapToPayConfig` and `requestActivityDelegate` properties
  - Updated `initialize()` to configure TapToMobilePOI parameters
  - Set `PaymentDevicePOI` based on `allowExternalReaders` setting
  - Added `registerRequestActivityListener()` method for NFC operations
  - Added helper methods: `isTapToPayEnabled()`, `getTapToPayConfiguration()`

#### 2.4 Configuration Propagation
- **Files**: 
  - `cardpresent/src/main/java/com/fattmerchant/android/InitParams.kt`
  - `cardpresent/src/main/java/com/fattmerchant/android/Omni.kt`
  - `cardpresent/src/main/java/com/fattmerchant/omni/Omni.kt`
- **Changes**: Added `tapToPayConfig` parameter throughout initialization chain

### Phase 3: Transaction Flow ✅

**Objective**: Implement reader detection, connection, and transaction handling for Tap to Pay.

#### 3.1 Virtual Reader
- **File**: `cardpresent/src/main/java/com/fattmerchant/omni/data/TapToPayReader.kt`
- **Purpose**: Represent device's NFC capability as a MobileReader
- **Implementation**:
  - `getName()` returns "Tap to Pay"
  - `getConnectionType()` returns `ConnectionType.NFC`
  - No physical connection required

#### 3.2 Connection Type Enhancement
- **File**: `cardpresent/src/main/java/com/fattmerchant/android/chipdna/ChipDnaUtils.kt`
- **Changes**: Added `NFC` to `ConnectionType` enum with parsing logic

#### 3.3 Reader Detection
- **File**: `cardpresent/src/main/java/com/fattmerchant/android/chipdna/ChipDnaDriver.kt`
- **Method**: `searchForReaders()`
- **Logic**:
  - Returns `TapToPayReader` when `isTapToPayEnabled() == true`
  - Adds external readers if `allowExternalReaders != false` or Tap to Pay disabled
  - Delegates physical reader search to `searchForExternalReaders()`

#### 3.4 Reader Connection
- **File**: `cardpresent/src/main/java/com/fattmerchant/android/chipdna/ChipDnaDriver.kt`
- **Method**: `connectReader()`
- **Logic**:
  - Checks if reader is `TapToPayReader`
  - Returns immediately with `CONNECTED` status for NFC reader
  - Proceeds with normal connection flow for physical readers

#### 3.5 Ready State
- **File**: `cardpresent/src/main/java/com/fattmerchant/android/chipdna/ChipDnaDriver.kt`
- **Method**: `isReadyToTakePayment()`
- **Logic**:
  - For Tap to Pay: Only requires terminal enabled (no device connection)
  - For external readers: Requires device connected and terminal enabled

#### 3.6 Connected Reader
- **File**: `cardpresent/src/main/java/com/fattmerchant/android/chipdna/ChipDnaDriver.kt`
- **Method**: `getConnectedReader()`
- **Logic**:
  - Returns `TapToPayReader` when Tap to Pay enabled and SDK initialized
  - Falls back to checking physical device connection status

#### 3.7 Transaction Updates
- **File**: `cardpresent/src/main/java/com/fattmerchant/omni/data/TransactionUpdate.kt`
- **New Events**:
  - `PromptTapCard`: "Tap card or device to phone"
  - `CardTapped`: Card/device tapped for NFC payment
- **File**: `cardpresent/src/main/java/com/fattmerchant/android/chipdna/ChipDnaUtils.kt`
- **Method**: `mapTransactionUpdate()`
- **NFC Mappings**:
  - "ContactlessCardDetected" → `TransactionUpdate.CardTapped`
  - "PresentCard" → `TransactionUpdate.PromptTapCard`

## Architecture Overview

```
┌─────────────────────────────────────────┐
│         Sample App                      │
│  ┌───────────────────────────────┐     │
│  │ MainActivity                   │     │
│  │ - Configure Tap to Pay        │     │
│  │ - Initialize Omni SDK         │     │
│  └───────────────────────────────┘     │
└─────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│         Omni SDK                        │
│  ┌───────────────────────────────┐     │
│  │ InitParams                     │     │
│  │ - tapToPayConfig              │     │
│  └───────────────────────────────┘     │
│                                         │
│  ┌───────────────────────────────┐     │
│  │ TakeMobileReaderPayment        │     │
│  │ - getAvailableMobileReaderDriver│    │
│  │ - performTransaction           │     │
│  └───────────────────────────────┘     │
└─────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│      ChipDnaDriver                      │
│  ┌───────────────────────────────┐     │
│  │ Configuration                  │     │
│  │ - TapToPayConfiguration       │     │
│  │ - RequestActivityDelegate     │     │
│  └───────────────────────────────┘     │
│                                         │
│  ┌───────────────────────────────┐     │
│  │ Reader Management              │     │
│  │ - searchForReaders()          │     │
│  │   → TapToPayReader            │     │
│  │ - connectReader()             │     │
│  │ - getConnectedReader()        │     │
│  └───────────────────────────────┘     │
│                                         │
│  ┌───────────────────────────────┐     │
│  │ Transaction Flow               │     │
│  │ - isReadyToTakePayment()      │     │
│  │ - performTransaction()        │     │
│  │ - Transaction updates (NFC)   │     │
│  └───────────────────────────────┘     │
└─────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│    NMI Cloud Commerce SDK 5.3.0         │
│  ┌───────────────────────────────┐     │
│  │ TapToMobilePOI                │     │
│  │ - NFC transaction handling    │     │
│  │ - Attestation                 │     │
│  └───────────────────────────────┘     │
└─────────────────────────────────────────┘
```

## Configuration Examples

### Tap to Pay Only (No External Readers)
```kotlin
val initParams = InitParams(
    apiKey = "your-api-key",
    environment = Environment.LIVE,
    tapToPayConfig = TapToPayConfiguration.tapToPayOnly(
        certificateFingerprint = "YOUR_CERT_FINGERPRINT",
        testMode = false
    )
)
```

### Hybrid Mode (Tap to Pay + External Readers)
```kotlin
val initParams = InitParams(
    apiKey = "your-api-key",
    environment = Environment.LIVE,
    tapToPayConfig = TapToPayConfiguration.hybrid(
        certificateFingerprint = "YOUR_CERT_FINGERPRINT",
        testMode = false
    )
)
```

### External Readers Only (Disable Tap to Pay)
```kotlin
val initParams = InitParams(
    apiKey = "your-api-key",
    environment = Environment.LIVE,
    tapToPayConfig = TapToPayConfiguration.externalReadersOnly()
)
// Or simply omit tapToPayConfig for backward compatibility
```

## Transaction Flow Comparison

### With Tap to Pay
1. Initialize SDK with Tap to Pay configuration
2. Register Activity listener for NFC UI
3. Search for readers → Returns `TapToPayReader`
4. Connect reader → Immediate success (virtual reader)
5. Start transaction → SDK displays NFC prompt
6. User taps card/device → `CardTapped` event
7. Transaction processes → Authorization
8. Complete → Transaction result

### With External Reader (Legacy)
1. Initialize SDK
2. Search for readers → Returns Bluetooth/USB readers
3. Connect reader → Physical pairing required
4. Start transaction → Prompt insert/swipe
5. User inserts/swipes card
6. Transaction processes → Authorization
7. Complete → Transaction result

## Testing Strategy

### Unit Tests (To Be Added)
- `TapToPayConfiguration` factory methods
- `TapToPayReader` virtual reader behavior
- `ChipDnaDriver.searchForReaders()` with various configs
- `ChipDnaDriver.connectReader()` for NFC vs physical
- `ChipDnaDriver.isReadyToTakePayment()` logic
- Transaction update mapping for NFC events

### Integration Tests (To Be Added)
- Full transaction flow with Tap to Pay
- Hybrid mode switching between NFC and external readers
- Activity lifecycle handling for NFC prompts
- Error handling and transaction cancellation

### Manual Testing Checklist
- [ ] Tap to Pay only mode - NFC transaction success
- [ ] Tap to Pay only mode - NFC transaction decline
- [ ] Hybrid mode - Switch between NFC and external reader
- [ ] External readers only - Bluetooth reader pairing
- [ ] Test mode vs production mode
- [ ] Activity context handling for NFC prompts
- [ ] Transaction cancellation during NFC tap
- [ ] Signature capture flow (if required)

## Known Limitations

1. **NFC Hardware**: Requires Android device with NFC hardware capability
2. **API Level**: Minimum Android API 23 (Android 6.0)
3. **Google Play Services**: Requires Play Services for attestation
4. **Activity Context**: Must register Activity listener before transactions
5. **ChipDnaApplication**: Sample app must extend `ChipDnaApplication`

## Next Steps

### Phase 4: Sample App UI (Not Started)
- Add Tap to Pay toggle in MainActivity
- Display NFC-specific prompts during transactions
- Show transaction status updates
- Handle configuration switching

### Phase 5: Documentation (Not Started)
- Update README with Tap to Pay setup
- Add API documentation
- Create integration guide
- Add troubleshooting section

### Phase 6: Testing (Not Started)
- Add unit tests
- Add integration tests
- Manual testing on various devices
- Performance testing

## References

- iOS SDK: `fattmerchantorg/Fattmerchant-iOS-SDK` (branch: PHO-4522)
- NMI Cloud Commerce SDK: Version 5.3.0
- ChipDNA SDK: Included with Cloud Commerce SDK
- NMI Documentation: [Tap to Pay Integration Guide]

## Changelog

### 2024-01-XX - Phase 3 Complete
- ✅ Created TapToPayReader virtual reader
- ✅ Enhanced ConnectionType enum with NFC
- ✅ Updated reader detection and connection logic
- ✅ Modified isReadyToTakePayment for Tap to Pay
- ✅ Updated getConnectedReader for NFC mode
- ✅ Added NFC transaction updates (PromptTapCard, CardTapped)
- ✅ Updated transaction update mapping

### 2024-01-XX - Phase 2 Complete
- ✅ Created TapToPayConfiguration model
- ✅ Implemented RequestActivityDelegate
- ✅ Enhanced ChipDnaDriver initialization
- ✅ Propagated configuration through SDK layers

### 2024-01-XX - Phase 1 Complete
- ✅ Added Cloud Commerce SDK dependencies
- ✅ Updated Android manifest for NFC
- ✅ Extended ChipDnaApplication in sample app
- ✅ Fixed Gradle error (gradleLocalProperties)
