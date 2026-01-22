# Production Deployment

Complete guide to launching your Tap to Pay app in production.

---

## Table of Contents

- [Pre-Launch Checklist](#pre-launch-checklist)
- [Google SDK Console](#google-sdk-console)
- [Stax Merchant Onboarding](#stax-merchant-onboarding)
- [Production Configuration](#production-configuration)
- [Google Play Requirements](#google-play-requirements)
- [Go-Live Process](#go-live-process)
- [Post-Launch Monitoring](#post-launch-monitoring)

---

## Pre-Launch Checklist

Complete this checklist before submitting for production approval:

### Technical Requirements

- [ ] **SDK Integration**
  - [ ] Latest SDK version (2.7.0+)
  - [ ] Proper initialization in Application class
  - [ ] Correct production API keys configured
  - [ ] Environment set to `Environment.LIVE`

- [ ] **Testing Complete**
  - [ ] All card brands tested (Visa, Mastercard, Amex, Discover)
  - [ ] Successful transactions
  - [ ] Declined transactions
  - [ ] Void transactions
  - [ ] Refund transactions
  - [ ] Network error handling
  - [ ] NFC issues handled gracefully

- [ ] **Compliance**
  - [ ] EMVCo contactless symbol displayed
  - [ ] Card brand logos are official versions
  - [ ] Receipts include all required fields
  - [ ] No sensitive card data stored or logged
  - [ ] UI follows accessibility guidelines

- [ ] **Error Handling**
  - [ ] Network errors handled
  - [ ] NFC unavailable handled
  - [ ] Card read errors handled with retry
  - [ ] Timeout scenarios handled
  - [ ] User-friendly error messages

- [ ] **Security**
  - [ ] API keys stored securely (not in source code)
  - [ ] No card data logged
  - [ ] HTTPS for all API calls
  - [ ] ProGuard/R8 enabled for release builds

### Business Requirements

- [ ] **Stax Account**
  - [ ] Merchant account created
  - [ ] Business verification complete
  - [ ] Bank account linked for settlements
  - [ ] Production API keys received

- [ ] **Google Compliance**
  - [ ] App registered in Google SDK Console
  - [ ] Test builds submitted and approved
  - [ ] Certification review passed
  - [ ] Production approval granted

- [ ] **Documentation**
  - [ ] User guide created
  - [ ] Support documentation ready
  - [ ] Privacy policy published
  - [ ] Terms of service published

---

## Google SDK Console

### Registration

1. **Access Google SDK Console**
   ```
   https://developers.google.com/pay/issuers/apis/push-provisioning/android/downloads/sdk-console
   ```

2. **Create New Application**
   - Log in with Google account
   - Click "Add Application"
   - Provide app details:
     - App name
     - Package name (com.yourcompany.yourapp)
     - App category
     - Description

3. **Submit App Information**
   - Screenshots of payment flow
   - App icon
   - Privacy policy URL
   - Support contact information

### Testing Phase

1. **Upload Test Build**
   ```bash
   # Build release APK for testing
   ./gradlew assembleRelease
   
   # Upload to Google SDK Console
   # (Done through web interface)
   ```

2. **Configure Test Environment**
   - Add test API keys
   - Configure test devices
   - Add tester email addresses

3. **Run Compliance Tests**
   - Google will run automated tests
   - Review results and fix any issues
   - Resubmit if necessary

### Production Approval

1. **Submit for Review**
   - Confirm all tests pass
   - Submit production build
   - Provide production API keys
   - Answer compliance questions

2. **Review Process**
   - Timeline: 1-2 weeks typically
   - Google reviews for compliance
   - May request additional information
   - Address any feedback promptly

3. **Approval**
   - Receive production approval email
   - Certificates issued
   - Ready to publish to Google Play

---

## Stax Merchant Onboarding

### Create Merchant Account

1. **Contact Stax Sales**
   - Email: sales@staxpayments.com
   - Phone: 1-800-XXX-XXXX
   - Website: https://www.staxpayments.com/

2. **Provide Business Information**
   - Business legal name
   - Tax ID (EIN)
   - Business address
   - Business type
   - Processing volume estimates

3. **Complete Verification**
   - Upload business documents
   - Verify bank account
   - Sign merchant agreement
   - Complete identity verification

### Get Production API Keys

```kotlin
// Development keys (for testing)
const val DEV_API_KEY = "test_your_dev_key_here"

// Production keys (for live transactions)
const val PROD_API_KEY = "live_your_prod_key_here"

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Use production key in release builds
        val apiKey = if (BuildConfig.DEBUG) {
            DEV_API_KEY
        } else {
            PROD_API_KEY
        }
        
        val environment = if (BuildConfig.DEBUG) {
            Environment.DEV
        } else {
            Environment.LIVE
        }
        
        Omni.initialize(
            context = this,
            apiKey = apiKey,
            environment = environment
        )
    }
}
```

### Secure API Key Storage

**Never hardcode production API keys!**

Use BuildConfig or secure storage:

```kotlin
// In build.gradle.kts
android {
    buildTypes {
        release {
            // Load from local.properties or environment variable
            val staxApiKey = project.findProperty("STAX_API_KEY") ?: System.getenv("STAX_API_KEY")
            buildConfigField("String", "STAX_API_KEY", "\"$staxApiKey\"")
        }
        
        debug {
            buildConfigField("String", "STAX_API_KEY", "\"test_api_key\"")
        }
    }
}

// In local.properties (DON'T commit this file!)
STAX_API_KEY=live_your_production_key_here

// In Application class
Omni.initialize(
    context = this,
    apiKey = BuildConfig.STAX_API_KEY,
    environment = if (BuildConfig.DEBUG) Environment.DEV else Environment.LIVE
)
```

---

## Production Configuration

### Release Build Configuration

```kotlin
// build.gradle.kts (app level)
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Signing configuration
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    signingConfigs {
        create("release") {
            // Store these securely, not in source control
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
}
```

### ProGuard Rules

```proguard
# proguard-rules.pro

# Keep Stax SDK classes
-keep class com.fattmerchant.** { *; }
-keep interface com.fattmerchant.** { *; }

# Keep payment processor SDK classes
-keep class com.creditcall.** { *; }

# Keep transaction models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Don't obfuscate SDK classes
-keepnames class com.fattmerchant.omni.Omni
-keepnames class com.fattmerchant.omni.data.models.**
-keepnames class com.fattmerchant.omni.usecase.**
```

### App Manifest - Production

```xml
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Required permissions -->
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!-- Optional for external readers -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <!-- Require NFC for installation -->
    <uses-feature
        android:name="android.hardware.nfc"
        android:required="true" />

    <application
        android:name=".MyApplication"
        android:usesCleartextTraffic="false"
        android:networkSecurityConfig="@xml/network_security_config"
        ...>
        
        <!-- Activities -->
        
    </application>

</manifest>
```

### Network Security Config

```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Production: Only allow HTTPS -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.staxpayments.com</domain>
    </domain-config>
    
    <!-- Debug: Allow localhost for development -->
    <debug-overrides>
        <domain-config cleartextTrafficPermitted="true">
            <domain includeSubdomains="true">localhost</domain>
            <domain includeSubdomains="true">10.0.2.2</domain>
        </domain-config>
    </debug-overrides>
</network-security-config>
```

---

## Google Play Requirements

### App Listing

**Title:**
- Must not contain "Tap to Pay" as Google restricts this
- Use descriptive name: "YourApp - Card Payments"

**Description:**
```
Accept card payments with your Android device.

[Your app name] enables secure contactless card payments using your 
Android phone or tablet. Accept Visa, Mastercard, American Express, 
and Discover cards with just a tap.

Features:
• Accept contactless card payments
• Support for all major card brands
• Secure, PCI-compliant transactions
• Digital receipts via email/SMS
• Transaction history and reporting
• Refunds and voids

Requirements:
• Android 9.0 or higher
• NFC-enabled device
• Active merchant account

[Your company] | Powered by Stax Payments
```

**Screenshots:**
- Payment screen with contactless symbol
- Transaction in progress
- Success confirmation
- Transaction history
- Settings/configuration

**Privacy Policy:**
Must include:
- What data is collected
- How payment data is handled
- Third-party services (Stax, Google)
- User rights and data retention

### Age Rating

- Financial app category
- Content rating: Everyone
- May require additional disclosures

### Target SDK Version

```kotlin
// build.gradle.kts
android {
    compileSdk = 34
    
    defaultConfig {
        minSdk = 28  // Android 9.0 minimum
        targetSdk = 34  // Latest for Google Play
    }
}
```

---

## Go-Live Process

### Step 1: Final Testing (1 week)

```kotlin
// Create production test plan
class ProductionTestPlan {
    val tests = listOf(
        // Real Card Tests
        "Process $1.00 Visa transaction",
        "Process $1.00 Mastercard transaction",
        "Process $1.00 Amex transaction",
        "Process $1.00 Discover transaction",
        
        // Transaction Management
        "Void a transaction",
        "Process a full refund",
        "Process a partial refund",
        
        // Error Scenarios
        "Handle network disconnection",
        "Handle card read failure",
        "Handle declined card",
        
        // Production Environment
        "Verify production API endpoint",
        "Confirm settlements to bank account",
        "Test receipt email delivery",
        "Verify transaction reporting"
    )
    
    fun executeTests() {
        tests.forEach { test ->
            println("[ ] $test")
        }
    }
}
```

### Step 2: Soft Launch (1-2 weeks)

- Release to limited users (beta testing)
- Monitor for issues
- Collect user feedback
- Fix any critical bugs

```kotlin
// Build beta version
./gradlew assembleBetaRelease

// Upload to Google Play Internal Testing
// Add beta testers
// Monitor crash reports and feedback
```

### Step 3: Production Release

1. **Build Production APK**
   ```bash
   # Clean build
   ./gradlew clean
   
   # Build release APK
   ./gradlew assembleRelease
   
   # APK location
   # app/build/outputs/apk/release/app-release.apk
   ```

2. **Upload to Google Play**
   - Google Play Console → Your App → Release → Production
   - Upload APK or AAB
   - Complete store listing
   - Set pricing (free or paid)
   - Choose countries/regions
   - Submit for review

3. **Google Play Review**
   - Review time: 1-7 days typically
   - May request additional information
   - Address any policy violations

4. **Launch**
   - Approve release when ready
   - Staged rollout recommended (10% → 50% → 100%)
   - Monitor crash reports and ratings

### Step 4: Production Monitoring

```kotlin
// Log production transactions for monitoring
class TransactionMonitor {
    
    fun logTransaction(transaction: Transaction) {
        // Send to analytics
        analytics.logEvent("transaction_completed", mapOf(
            "amount" to transaction.total,
            "card_type" to transaction.cardType,
            "success" to transaction.success,
            "timestamp" to System.currentTimeMillis()
        ))
        
        // Check for anomalies
        if (transaction.total!! > 1000.00) {
            alertHighValueTransaction(transaction)
        }
        
        if (transaction.success == false) {
            trackDecline(transaction)
        }
    }
    
    fun trackDecline(transaction: Transaction) {
        val declineReason = transaction.message ?: "Unknown"
        
        analytics.logEvent("transaction_declined", mapOf(
            "reason" to declineReason,
            "card_type" to transaction.cardType
        ))
    }
}
```

---

## Post-Launch Monitoring

### Metrics to Track

```kotlin
class ProductionMetrics {
    
    // Transaction Metrics
    fun trackTransactionMetrics() {
        - Total transaction volume
        - Average transaction value
        - Success rate (%)
        - Decline rate (%)
        - Void/refund rate (%)
        - Processing time (seconds)
    }
    
    // Technical Metrics
    fun trackTechnicalMetrics() {
        - App crashes
        - API errors
        - Network failures
        - NFC read failures
        - App startup time
        - Transaction completion time
    }
    
    // User Metrics
    fun trackUserMetrics() {
        - Daily active users
        - Transaction frequency
        - User retention
        - Support requests
        - App ratings/reviews
    }
}
```

### Crash Reporting

```kotlin
// Initialize Firebase Crashlytics
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable crash reporting
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        
        // Set user identifier (non-PII)
        FirebaseCrashlytics.getInstance().setUserId(merchantId)
        
        // Log custom keys
        FirebaseCrashlytics.getInstance().setCustomKey("environment", "production")
    }
}

// Log non-fatal errors
fun logError(error: Exception, context: String) {
    FirebaseCrashlytics.getInstance().apply {
        setCustomKey("error_context", context)
        recordException(error)
    }
}
```

### Performance Monitoring

```kotlin
// Track transaction performance
fun monitorTransactionPerformance(transactionId: String) {
    val trace = FirebasePerformance.getInstance().newTrace("transaction_flow")
    trace.start()
    
    // Start transaction
    val startTime = System.currentTimeMillis()
    
    Omni.shared()?.takeMobileReaderTransaction(
        request,
        completion = { transaction ->
            val duration = System.currentTimeMillis() - startTime
            
            trace.putMetric("duration_ms", duration)
            trace.putAttribute("card_type", transaction.cardType ?: "unknown")
            trace.putAttribute("success", transaction.success.toString())
            trace.stop()
            
            // Alert if too slow
            if (duration > 10000) {
                logError(Exception("Slow transaction: ${duration}ms"), "performance")
            }
        },
        error = { exception ->
            trace.putAttribute("error", exception.message ?: "unknown")
            trace.stop()
        }
    )
}
```

### Support & Maintenance

**Support Channels:**
- In-app support chat
- Email: support@yourcompany.com
- Phone: 1-800-XXX-XXXX
- Knowledge base / FAQ

**Response Times:**
- Critical issues (payment failures): < 1 hour
- High priority: < 4 hours
- Normal priority: < 24 hours

**Regular Maintenance:**
- Monitor SDK updates from Stax
- Update dependencies quarterly
- Review and address user feedback
- Perform security audits annually

---

## Rollback Plan

If critical issues arise post-launch:

```bash
# 1. Immediately disable new installs (Google Play Console)
# Pause production release

# 2. Roll back to previous version
# Promote previous release to production

# 3. Fix critical bug
./gradlew clean
# Fix code
./gradlew assembleRelease

# 4. Test fix thoroughly
# Run full test suite
# Test on production environment with small amount

# 5. Submit hotfix
# Upload to Google Play
# Emergency review (expedited)

# 6. Monitor closely
# Watch crash reports
# Check transaction success rate
# Communicate with affected users
```

---

## Success Criteria

Your launch is successful when:

- ✅ 99%+ transaction success rate
- ✅ < 1% crash rate
- ✅ < 5 second average transaction time
- ✅ 4+ star app rating
- ✅ < 1% support ticket rate
- ✅ Positive user reviews
- ✅ Meeting revenue projections

---

## Next Steps

- **[Troubleshooting](08-troubleshooting.md)** - Solve production issues
- **[API Reference](09-api-reference.md)** - Complete API documentation
- **[Testing](06-testing.md)** - Pre-production testing guide
