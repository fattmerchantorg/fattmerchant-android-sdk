# NMI Tap to Pay on Android - Onboarding Checklist

This document outlines the steps required to enable Tap to Pay functionality in the Stax Android SDK.

## Prerequisites

- Android device running Android 9.0 (API 28) or higher
- NFC hardware capability
- NMI Gateway account with API access
- App published to Google Play Console (for production)

## Setup Steps

### ✅ Step 1: Enable Tap to Pay in NMI Marketplace
- [ ] Log into NMI Partner Portal or Merchant Portal
- [ ] Navigate to Marketplace → Tap to Pay on Android
- [ ] Enable the Tap to Pay feature
- [ ] Watch the Cloud Commerce Tap to Pay application video

### ✅ Step 2: Configure Test Mode
- [ ] Log into Merchant Portal
- [ ] Navigate to Settings → Test Mode
- [ ] Enable Test Mode for sandbox testing
- [ ] Install [NMI Test Card Simulator](https://play.google.com/store/apps/details?id=com.nmi.testcardsimulator) Android app on your test device

### ✅ Step 3: Set Up Payment Gateway Account
- [x] Create NMI Gateway account
- [x] Generate API Key from Account Settings
- [x] Save API key securely (add to `local.properties` as `staxApiKey`)

### ✅ Step 4: Define Application Identifier
- [x] Choose unique identifier (A-Z, 0-9 only)
- [x] Current identifier: `StaxPaymentsSample`
- [ ] Use consistent identifier across all environments

### ✅ Step 5: Install Payment Device SDK
- [x] Test/MTF SDK: `cloud-commerce-sdk-mtf-5.3.0.aar` (for sandbox)
- [x] Production SDK: `cloud-commerce-sdk-5.3.0.aar` (for live)
- [x] SDK integrated in `cardpresent/libs/` directory

### ✅ Step 6: Android Project Setup
- [x] Add SDK dependencies to `build.gradle.kts`
- [x] Configure ProGuard rules if using R8/ProGuard
- [x] Add required permissions to `AndroidManifest.xml`

### ✅ Step 7: Android Permissions
Required permissions in `AndroidManifest.xml`:
- [x] `android.permission.NFC` - For contactless payments
- [x] `android.permission.INTERNET` - For API communication
- [x] `android.permission.BLUETOOTH` - For external readers (if hybrid mode)
- [x] `android.permission.BLUETOOTH_ADMIN` - For reader management
- [x] `android.permission.ACCESS_FINE_LOCATION` - Required for Bluetooth
- [x] `android.permission.ACCESS_COARSE_LOCATION` - Required for Bluetooth
- [x] `android.hardware.nfc` - NFC hardware requirement

### ✅ Step 8: SDK Initialization
- [x] Initialize ChipDNA SDK with Password and AutoConfirm parameters
- [x] Set API Key, Environment, and Application Identifier
- [x] Register RequestActivityListener for NFC operations
- [x] Implement connection status tracking

### ❌ Step 9: Extend ChipDnaApplication (OPTIONAL - CAUSES CRASHES)
**NOTE:** The NMI documentation recommends extending `ChipDnaApplication`, but this causes crashes in our implementation due to Cloud Commerce SDK initialization conflicts. We're manually initializing ChipDnaMobile in `MainApplication.kt` instead, which works correctly.

**If you want to attempt this:**
- [ ] Create class extending `com.creditcall.chipdnamobile.ChipDnaApplication`
- [ ] Override `onSDKInitializationSuccess()`
- [ ] Override `onSDKInitializationFailed()`
- [ ] Register in `AndroidManifest.xml`: `<application android:name=".YourAppClass">`

### ⚠️ Step 10: App Onboarding & Security (CRITICAL - BLOCKS TAP TO PAY)

**THIS IS THE BLOCKING STEP - WITHOUT THIS, TAP TO PAY WILL NOT WORK!**

You MUST email the following information to **taptopay-app-onboarding@nmi.com**:

#### Required Information:

1. **App Package Name**: `com.staxpayments.sample`
   
2. **Keystore Certificate Hash (SHA-256, Base64)**:
   - Debug Keystore: `dqWyBOJVho+5gumYDGPL1tYfQqiLVz++7gIgJRob/r0=`
   - Production Keystore: `[TO BE GENERATED FROM RELEASE KEYSTORE]`
   
   **How to generate:**
   ```bash
   # For debug keystore
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # For production keystore
   keytool -list -v -keystore /path/to/your/release.keystore -alias your-alias
   
   # Look for SHA256 fingerprint, then convert to Base64:
   echo "XX:XX:XX:..." | xxd -r -p | base64
   ```

3. **Play Integrity API Keys** (for production):
   - [ ] Log into Google Play Console
   - [ ] Navigate to Release → App Integrity → App Integrity API
   - [ ] Generate and link encryption keys
   - [ ] Copy the encryption keys and include in email

4. **Environment Request**:
   - [ ] Request MTF (test) environment access first
   - [ ] Once tested, request production environment access

#### Email Template:

```
To: taptopay-app-onboarding@nmi.com
Subject: Tap to Pay App Onboarding Request - [Your Company Name]

Hello,

We would like to onboard our Android application for Tap to Pay on Android functionality.

Application Details:
- App Package Name: com.staxpayments.sample
- Application Identifier: StaxPaymentsSample
- Company: [Your Company]
- Contact: [Your Name/Email]

Certificate Hashes:
- Debug Keystore SHA-256: dqWyBOJVho+5gumYDGPL1tYfQqiLVz++7gIgJRob/r0=
- Production Keystore SHA-256: [Your Production Hash]

Play Integrity Keys:
[Paste your encryption keys from Play Console]

Environment:
- Initially requesting MTF (test) environment access
- Will request production access after successful testing

Thank you,
[Your Name]
```

### ✅ Step 11: Network & Firewall Configuration
- [x] Allow HTTPS traffic to NMI endpoints
- [x] Configure `NetworkSecurityConfig` if needed
- [x] Ensure device has internet connectivity

### ✅ Step 12: Running Tap to Pay Transactions
Transaction parameters are set in `ChipDnaDriver.doConnectAndConfigure()`:
- [x] `TapToMobilePOI = TRUE` - Enable Tap to Pay
- [x] `PaymentDevicePOI = FALSE` - Disable external readers (for Tap to Pay only mode)
- [x] `PaymentDevicePOI = TRUE` - Enable external readers (for hybrid mode)

### ✅ Step 13: Transaction Results
Handled by `TransactionUpdateListener`:
- [x] APPROVED - Payment successful
- [x] DECLINED - Payment declined
- [x] VOIDED - Payment voided
- [x] DELAYED - Payment delayed (signature required)

### ⚠️ Step 14: Receipts
- [ ] Implement SMS receipt delivery
- [ ] Implement email receipt delivery
- [ ] Configure receipt templates in NMI portal

### ⚠️ Step 15: Compliance & Branding
- [ ] Add EMVCo contactless symbols to UI
- [ ] Display card brand logos (Visa, Mastercard, Amex, Discover)
- [ ] Follow EMVCo branding guidelines
- [ ] Follow Google Play Policies for payment apps

## Current Status

### Completed ✅
- SDK integration (MTF and Production versions)
- Android permissions configured
- ChipDNA initialization implemented
- RequestActivityListener registration
- Connection state tracking
- TapToMobilePOI/PaymentDevicePOI parameters
- Test mode with MTF SDK
- Mock mode for UI testing

### Blocked - Awaiting NMI ⏳
- **App Onboarding** - Email to taptopay-app-onboarding@nmi.com required
- Terminal configuration from NMI backend
- Real NFC transaction testing

### Pending ⚠️
- ChipDnaApplication extension (optional, currently causes crashes)
- Play Integrity API keys setup (production only)
- Receipt delivery implementation
- EMVCo branding compliance

## Troubleshooting

### Terminal isEnabled = false
**Cause:** NMI account not provisioned for Tap to Pay  
**Solution:** Complete Step 10 (App Onboarding)

### connectAndConfigure hangs
**Cause:** Waiting for terminal configuration that never arrives  
**Solution:** Use Mock Mode for testing, complete Step 10 for real transactions

### NFC prompt doesn't appear
**Cause:** Activity not registered for NFC operations  
**Solution:** Ensure `Omni.registerTapToPayActivityProvider()` is called

### Test Card Simulator not working
**Cause:** Mock mode disabled or device NFC not enabled  
**Solution:** Enable mock mode and check device NFC settings

## References

- [NMI Tap to Pay Documentation](https://docs.nmi.com/docs/mobile-point-of-sale-tap-to-pay-on-android)
- [ChipDNA Mobile SDK Guide](https://docs.nmi.com/docs/chipdna-mobile-sdk)
- [Google Play Integrity API](https://developer.android.com/google/play/integrity)
- [EMVCo Contactless Specifications](https://www.emvco.com/emv-technologies/contactless/)
