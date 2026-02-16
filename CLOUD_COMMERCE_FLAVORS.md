# Cloud Commerce SDK Flavors

The Fattmerchant Android SDK now supports two build flavors to serve different versions of the NMI Cloud Commerce SDK based on your environment needs.

## ⚠️ Important: JitPack Compatibility Note

Due to Gradle's limitation on "AAR-in-AAR" dependencies, the flavor-based approach currently requires local building and publishing. We're working on making this fully JitPack-compatible in a future release.

**Current Workaround:**
1. Clone the repository
2. Build locally: `./gradlew :cardpresent:publishToMavenLocal`
3. Use from Maven Local in your project

## Available Flavors

### Production Flavor
- **Artifact**: `cardpresent-production`
- **Description**: Uses the production version of the Cloud Commerce SDK
- **Use Case**: Production applications and live payment processing

### MTF Flavor (Merchant Test Framework)
- **Artifact**: `cardpresent-mtf`
- **Description**: Uses the MTF version of the Cloud Commerce SDK for testing
- **Use Case**: Development, testing, and QA environments

## Installation

### Option 1: Local Build (Recommended for now)

1. Clone the SDK repository:
```bash
git clone https://github.com/fattmerchantorg/fattmerchant-android-sdk.git
cd fattmerchant-android-sdk
```

2. Build and publish to Maven Local:
```bash
# For production variant
./gradlew :cardpresent:publishProductionReleasePublicationToMavenLocal :cloudcommerce:publishProductionReleasePublicationToMavenLocal

# For MTF variant
./gradlew :cardpresent:publishMtfReleasePublicationToMavenLocal :cloudcommerce:publishMtfReleasePublicationToMavenLocal
```

3. Add Maven Local to your project's repositories:
```kotlin
repositories {
    mavenLocal()
    google()
    mavenCentral()
}
```

4. Add the dependency:
```kotlin
dependencies {
    // For production
    implementation("com.fattmerchant:cardpresent-production:2.7.0")
    
    // Or for MTF (testing)
    implementation("com.fattmerchant:cardpresent-mtf:2.7.0")
}
```

### Option 2: Using JitPack (When Available)

Once JitPack compatibility is resolved, you'll be able to use:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // For production
    implementation("com.github.fattmerchantorg.fattmerchant-android-sdk:cardpresent-production:VERSION")
    
    // For testing (MTF)
    implementation("com.github.fattmerchantorg.fattmerchant-android-sdk:cardpresent-mtf:VERSION")
}
```

## Using Build Variants in Your App

If you want to automatically switch between production and MTF based on your app's build type, you can use Gradle's `matchingFallbacks`:

```kotlin
android {
    buildTypes {
        debug {
            // Use MTF flavor for debug builds
            matchingFallbacks += listOf("mtf")
        }
        release {
            // Use production flavor for release builds
            matchingFallbacks += listOf("production")
        }
    }
}

dependencies {
    debugImplementation("com.github.fattmerchantorg.fattmerchant-android-sdk:cardpresent-mtf:1.3.0")
    releaseImplementation("com.github.fattmerchantorg.fattmerchant-android-sdk:cardpresent-production:1.3.0")
}
```

## Migration from Previous Versions

If you're upgrading from a previous version that didn't have flavors:

**Before:**
```kotlin
implementation("com.github.fattmerchantorg:fattmerchant-android-sdk:1.2.0")
```

**After (for production):**
```kotlin
implementation("com.github.fattmerchantorg.fattmerchant-android-sdk:cardpresent-production:1.3.0")
```

**After (for testing):**
```kotlin
implementation("com.github.fattmerchantorg.fattmerchant-android-sdk:cardpresent-mtf:1.3.0")
```

## Code Changes

No code changes are required in your application. The API remains the same across both flavors. The only difference is which version of the NMI Cloud Commerce SDK is bundled with the SDK.

## Building from Source

If you're building the SDK from source, you can specify the flavor:

### Build Production Variant
```bash
./gradlew :cardpresent:assembleProductionRelease
```

### Build MTF Variant
```bash
./gradlew :cardpresent:assembleMtfRelease
```

### Publish to Maven Local
```bash
# Publish production variant
./gradlew :cardpresent:publishProductionReleasePublicationToMavenLocal

# Publish MTF variant
./gradlew :cardpresent:publishMtfReleasePublicationToMavenLocal
```

## Troubleshooting

### Variant Conflict Error

If you see an error like:
```
No matching variant of project :cloudcommerce was found
```

Ensure that:
1. Both `cardpresent` and `cloudcommerce` modules have matching flavor dimensions
2. You're using the correct flavor name in your dependencies
3. Your project's build configuration properly specifies the flavor

### JitPack Build Issues

JitPack automatically builds all variants. You can check the build status at:
`https://jitpack.io/#fattmerchantorg/fattmerchant-android-sdk`

## FAQ

**Q: Which flavor should I use?**
A: Use the `production` flavor for live applications processing real payments. Use the `mtf` flavor for development, testing, and QA environments.

**Q: Can I switch between flavors at runtime?**
A: No, the flavor is determined at compile time. You must use the appropriate flavor dependency for your build.

**Q: Do I need different API keys for each flavor?**
A: Yes, you should use test credentials with the MTF flavor and production credentials with the production flavor.

**Q: Is there any performance difference?**
A: The flavors are functionally identical. The only difference is which NMI Cloud Commerce SDK binary is included.
