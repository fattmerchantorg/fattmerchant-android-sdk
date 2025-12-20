# Publishing the SDK to Maven Central

This guide explains how to publish the Fattmerchant Android SDK to Maven Central Repository, which is required for listing in the Google SDK Console.

## Table of Contents
- [Prerequisites](#prerequisites)
- [One-Time Setup](#one-time-setup)
- [Configuration](#configuration)
- [Publishing Process](#publishing-process)
- [Google SDK Console Registration](#google-sdk-console-registration)
- [Troubleshooting](#troubleshooting)

## Prerequisites

Before publishing to Maven Central, you need:

1. **Sonatype OSSRH Account**
   - Create an account at [issues.sonatype.org](https://issues.sonatype.org)
   - Create a JIRA ticket to claim your group ID (e.g., `com.fattmerchant` or `io.github.fattmerchantorg`)
   - Wait for approval (usually 1-2 business days)

2. **GPG Key for Signing**
   ```bash
   # Generate a GPG key
   gpg --gen-key
   
   # List your keys
   gpg --list-keys
   
   # Publish your public key to a key server
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

3. **Gradle Properties**
   Create or update `~/.gradle/gradle.properties` with your credentials:
   ```properties
   # Sonatype credentials
   ossrhUsername=your-jira-username
   ossrhPassword=your-jira-password
   
   # GPG signing
   signing.keyId=YOUR_8_CHAR_KEY_ID
   signing.password=YOUR_GPG_PASSPHRASE
   signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
   ```

## One-Time Setup

### 1. Add Maven Publishing Plugin to Root build.gradle.kts

```kotlin
// build.gradle.kts (root)
plugins {
    id("com.android.application") version "8.13.1" apply false
    id("com.android.library") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("maven-publish") apply false
    id("signing") apply false
}
```

### 2. Create Publishing Configuration Script

Create a new file `gradle/publish-module.gradle.kts`:

```kotlin
apply(plugin = "maven-publish")
apply(plugin = "signing")

// Read properties from gradle.properties or environment variables
val ossrhUsername: String? = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
val ossrhPassword: String? = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")

// Define library metadata
ext {
    set("PUBLISH_GROUP_ID", "com.fattmerchant")
    set("PUBLISH_VERSION", "2.7.0")
    set("PUBLISH_ARTIFACT_ID", project.name) // Uses module name
    
    // Library information
    set("LIBRARY_NAME", "Fattmerchant Android SDK")
    set("LIBRARY_DESCRIPTION", "Accept payments on Android using card readers and NFC Tap to Pay")
    set("LIBRARY_URL", "https://github.com/fattmerchantorg/fattmerchant-android-sdk")
    
    // Developer information
    set("DEVELOPER_ID", "fattmerchant")
    set("DEVELOPER_NAME", "Fattmerchant")
    set("DEVELOPER_EMAIL", "support@fattmerchant.com")
    
    // SCM information
    set("SCM_URL", "https://github.com/fattmerchantorg/fattmerchant-android-sdk")
    set("SCM_CONNECTION", "scm:git:git://github.com/fattmerchantorg/fattmerchant-android-sdk.git")
    set("SCM_DEV_CONNECTION", "scm:git:ssh://git@github.com:fattmerchantorg/fattmerchant-android-sdk.git")
    
    // License
    set("LICENSE_NAME", "MIT License")
    set("LICENSE_URL", "https://opensource.org/licenses/MIT")
}

// Create source jar
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    if (project.plugins.hasPlugin("com.android.library")) {
        from(android.sourceSets.getByName("main").java.srcDirs)
    } else {
        from(sourceSets.main.get().java.srcDirs)
    }
}

// Create javadoc jar
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from("$buildDir/javadoc")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = ext.get("PUBLISH_GROUP_ID") as String
                artifactId = ext.get("PUBLISH_ARTIFACT_ID") as String
                version = ext.get("PUBLISH_VERSION") as String
                
                // If this is an Android library, use the release component
                if (project.plugins.hasPlugin("com.android.library")) {
                    from(components["release"])
                }
                
                // Attach sources and javadoc
                artifact(sourcesJar)
                artifact(javadocJar)
                
                pom {
                    name.set(ext.get("LIBRARY_NAME") as String)
                    description.set(ext.get("LIBRARY_DESCRIPTION") as String)
                    url.set(ext.get("LIBRARY_URL") as String)
                    
                    licenses {
                        license {
                            name.set(ext.get("LICENSE_NAME") as String)
                            url.set(ext.get("LICENSE_URL") as String)
                        }
                    }
                    
                    developers {
                        developer {
                            id.set(ext.get("DEVELOPER_ID") as String)
                            name.set(ext.get("DEVELOPER_NAME") as String)
                            email.set(ext.get("DEVELOPER_EMAIL") as String)
                        }
                    }
                    
                    scm {
                        connection.set(ext.get("SCM_CONNECTION") as String)
                        developerConnection.set(ext.get("SCM_DEV_CONNECTION") as String)
                        url.set(ext.get("SCM_URL") as String)
                    }
                }
            }
        }
        
        repositories {
            maven {
                name = "sonatype"
                url = if (version.toString().endsWith("SNAPSHOT")) {
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                }
                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
    }
    
    signing {
        sign(publishing.publications["release"])
    }
}
```

### 3. Update Module build.gradle.kts Files

Update both `cardpresent/build.gradle.kts` and `tokenization/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

// Apply the publishing configuration
apply(from = "${rootProject.projectDir}/gradle/publish-module.gradle.kts")

// Module-specific version (override if needed)
version = "2.7.0"

// Rest of your existing configuration...
```

### 4. Create gradle.properties at Project Root

Create or update `gradle.properties` in your project root:

```properties
# Project properties
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official

# Publishing properties (use for local testing)
# NEVER commit real credentials to git!
# Use ~/.gradle/gradle.properties for actual credentials
PUBLISH_GROUP_ID=com.fattmerchant
PUBLISH_VERSION=2.7.0

# Signing (required for Maven Central)
signing.required=true
```

## Publishing Process

### Step 1: Prepare Release

1. **Update version numbers** in:
   - `gradle/publish-module.gradle.kts` (PUBLISH_VERSION)
   - Module-specific `build.gradle.kts` files if overriding

2. **Update CHANGELOG.md** with release notes

3. **Commit and tag** the release:
   ```bash
   git add .
   git commit -m "Release version 2.7.0"
   git tag -a v2.7.0 -m "Version 2.7.0"
   git push origin develop
   git push origin v2.7.0
   ```

### Step 2: Build and Test

```bash
# Clean build
./gradlew clean

# Build all modules
./gradlew build

# Run tests
./gradlew test

# Verify publishing configuration
./gradlew publishToMavenLocal
```

### Step 3: Publish to Maven Central

```bash
# Publish all modules to staging repository
./gradlew publish

# Or publish individual modules
./gradlew :cardpresent:publish
./gradlew :tokenization:publish
```

### Step 4: Release via Sonatype

1. Log in to [s01.oss.sonatype.org](https://s01.oss.sonatype.org/)

2. Navigate to **Staging Repositories**

3. Find your staging repository (e.g., `comfattmerchant-1001`)

4. **Close** the repository
   - Sonatype will validate your artifacts
   - Check for any errors in the Activity tab

5. Once validation passes, **Release** the repository
   - Artifacts will sync to Maven Central within 30 minutes
   - Full indexing may take 2-4 hours

### Step 5: Verify Publication

After 2-4 hours, verify your artifacts are available:

```
https://search.maven.org/artifact/com.fattmerchant/cardpresent/2.7.0/aar
https://search.maven.org/artifact/com.fattmerchant/tokenization/2.7.0/aar
```

## Google SDK Console Registration

Once published to Maven Central, you can register in the Google SDK Console:

### 1. Access SDK Console

Visit [Google SDK Console](https://console.cloud.google.com/apis/library/sdk-registry.googleapis.com)

### 2. Register Your SDK

Fill in the required information:

```yaml
SDK Name: Fattmerchant Android SDK
Package Name: com.fattmerchant
Maven Coordinates: com.fattmerchant:cardpresent:2.7.0
Repository URL: https://github.com/fattmerchantorg/fattmerchant-android-sdk
Documentation: https://github.com/fattmerchantorg/fattmerchant-android-sdk/blob/develop/README.md
SDK Description: Accept payments on Android using card readers and NFC Tap to Pay
```

### 3. Provide Metadata

- **Category**: Payments
- **Platform**: Android
- **Min SDK Version**: 23 (tokenization) or 30 (cardpresent)
- **Target SDK Version**: 34
- **License**: MIT

### 4. Upload Privacy Policy

Provide links to:
- Privacy Policy: Your company's privacy policy URL
- Terms of Service: Your company's terms URL

### 5. Add Usage Examples

Include code snippets from your README.md showing:
- SDK initialization
- Basic payment processing
- Tap to Pay implementation

## Automated Publishing with GitHub Actions

Create `.github/workflows/publish.yml`:

```yaml
name: Publish to Maven Central

on:
  release:
    types: [created]

jobs:
  publish:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build and publish
      env:
        OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
        OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
        SIGNING_KEY_ID: ${{ secrets.SIGNING_KEY_ID }}
        SIGNING_PASSWORD: ${{ secrets.SIGNING_PASSWORD }}
        SIGNING_SECRET_KEY_RING_FILE: ${{ secrets.SIGNING_SECRET_KEY_RING_FILE }}
      run: |
        echo "$SIGNING_SECRET_KEY_RING_FILE" | base64 --decode > ~/.gnupg/secring.gpg
        ./gradlew publish --no-daemon
```

### Add GitHub Secrets

In your repository settings, add these secrets:
- `OSSRH_USERNAME`: Your Sonatype username
- `OSSRH_PASSWORD`: Your Sonatype password
- `SIGNING_KEY_ID`: Your GPG key ID (8 characters)
- `SIGNING_PASSWORD`: Your GPG key passphrase
- `SIGNING_SECRET_KEY_RING_FILE`: Base64 encoded secret keyring
  ```bash
  base64 ~/.gnupg/secring.gpg | pbcopy
  ```

## Troubleshooting

### Common Issues

**1. "Failed to find signing key"**
```bash
# Export your GPG key to the legacy secring.gpg format
gpg --export-secret-keys -o ~/.gnupg/secring.gpg
```

**2. "401 Unauthorized" when publishing**
- Verify credentials in `~/.gradle/gradle.properties`
- Ensure your Sonatype account has permissions for the groupId

**3. "POM validation failed"**
- Ensure all required POM elements are present (name, description, url, license, developers, scm)
- Check that URLs are valid and accessible

**4. "Signature validation failed"**
- Verify your GPG key is published to a key server
- Ensure the key hasn't expired
- Check that signing.keyId matches your actual key ID

**5. "Component not found: release"**
- Ensure you're using `afterEvaluate` block
- Verify Android Gradle Plugin is applied correctly

### Useful Commands

```bash
# Test local publishing
./gradlew publishToMavenLocal

# Check published artifacts
ls ~/.m2/repository/com/fattmerchant/

# View all publishable tasks
./gradlew tasks --group publishing

# Clean and rebuild
./gradlew clean build

# Verify signatures
gpg --verify <file>.asc <file>
```

## Best Practices

1. **Version Management**
   - Use semantic versioning (MAJOR.MINOR.PATCH)
   - Never republish the same version
   - Use SNAPSHOT for development versions

2. **Security**
   - Never commit credentials to git
   - Use environment variables or gradle.properties in user home
   - Rotate credentials periodically

3. **Testing**
   - Always test with `publishToMavenLocal` first
   - Verify artifacts before releasing from staging
   - Test integration in a separate project

4. **Documentation**
   - Update README.md with new version
   - Maintain CHANGELOG.md
   - Include migration guides for breaking changes

5. **Release Process**
   - Create release branch
   - Run full test suite
   - Tag release in git
   - Publish to staging
   - Verify artifacts
   - Release to Maven Central
   - Announce release

## References

- [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Maven Publishing Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
- [Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)
- [Google SDK Console](https://developers.google.com/sdk-console)
