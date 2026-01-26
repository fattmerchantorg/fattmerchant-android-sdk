# JitPack and Maven Central Compatibility

This SDK supports publishing to **both** JitPack and Maven Central repositories simultaneously.

## Issue Fixed

**Problem**: JitPack builds were failing with:
```
Value '/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home' given for org.gradle.java.home Gradle property is invalid (Java home supplied is invalid)
```

**Root Cause**: `gradle.properties` contained hardcoded macOS Java path that doesn't exist in JitPack's Linux environment.

**Fix**: Removed `org.gradle.java.home` from gradle.properties. Java home should never be committed to version control as it's machine-specific.

## How It Works

### Publishing Script Logic

The [gradle/publish-module.gradle.kts](gradle/publish-module.gradle.kts) intelligently detects which publishing system is being used:

#### For JitPack:
- JitPack passes `-Pgroup=com.github.user -Pversion=branch-SNAPSHOT` via command line
- Script detects these properties and uses them for groupId/version
- Example: `com.github.fattmerchantorg:cardpresent:PHO-4519-SNAPSHOT`

#### For Maven Central:
- Uses `PUBLISH_GROUP_ID` and `PUBLISH_VERSION` from gradle.properties
- Example: `com.fattmerchant:cardpresent:2.7.0`

### Property Resolution Order

```kotlin
val publishGroupId = when {
    jitpackGroup != null && jitpackGroup != project.group.toString() -> jitpackGroup  // JitPack
    mavenGroup != null -> mavenGroup  // Maven Central
    else -> "com.fattmerchant"  // Fallback
}
```

## Usage

### JitPack (Existing Users - No Changes Needed)

Add to your app's `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.fattmerchantorg:cardpresent:2.7.0'
    implementation 'com.github.fattmerchantorg:tokenization:2.7.0'
}
```

### Maven Central (After Publishing)

```gradle
dependencies {
    implementation 'com.fattmerchant:cardpresent:2.7.0'
    implementation 'com.fattmerchant:tokenization:2.7.0'
}
```

## Testing Locally

Both publishing configurations work:

```bash
# Test Maven Central publishing
./gradlew publishToMavenLocal

# Test JitPack-style publishing
./gradlew -Pgroup=com.github.fattmerchantorg -Pversion=test-SNAPSHOT publishToMavenLocal
```

## Configuration Files Modified

1. **gradle.properties**: Removed hardcoded `org.gradle.java.home`
2. **gradle/publish-module.gradle.kts**: Added smart group/version detection
3. **cardpresent/build.gradle.kts**: Dynamic group assignment
4. **tokenization/build.gradle.kts**: Dynamic group assignment

## Important Notes

- ✅ JitPack builds will now succeed (no more Java home error)
- ✅ Maven Central publishing unchanged
- ✅ No breaking changes for existing users
- ✅ Both systems can coexist
- ⚠️ Never commit `local.properties` or machine-specific Java paths to Git

## Next Steps

1. **For JitPack**: Push changes and create a new release tag
2. **For Maven Central**: Complete IT team setup, then publish
3. **For Google SDK Console**: Register after Maven Central publish succeeds

## Related Documentation

- [PUBLISHING.md](PUBLISHING.md) - Maven Central publishing guide
- [IT_TICKET_MAVEN_CENTRAL_SETUP.md](IT_TICKET_MAVEN_CENTRAL_SETUP.md) - IT setup instructions
- [JitPack Documentation](https://jitpack.io/docs/)
