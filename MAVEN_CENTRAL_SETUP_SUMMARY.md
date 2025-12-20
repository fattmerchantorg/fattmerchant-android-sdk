# Maven Central Publishing - Setup Complete

## ✅ Completed Steps

Your SDK is now configured for Maven Central publishing. The following files have been created and configured:

### 1. Publishing Configuration Files

- ✅ **`gradle/publish-module.gradle.kts`** - Reusable publishing script applied to both modules
- ✅ **`PUBLISHING.md`** - Comprehensive guide for Maven Central publishing process
- ✅ **`.github/workflows/publish.yml`** - GitHub Actions workflow for automated releases
- ✅ **`gradle.properties`** - Updated with publishing configuration (PUBLISH_GROUP_ID, PUBLISH_VERSION)

### 2. Module Configuration

Both modules are configured for Maven Central:

- ✅ **cardpresent** (v2.7.0)
  - Group ID: `com.fattmerchant`
  - Artifact ID: `cardpresent`
  - Includes: AAR, POM, sources JAR, javadoc JAR, Gradle module metadata

- ✅ **tokenization** (v2.7.0)
  - Group ID: `com.fattmerchant`
  - Artifact ID: `tokenization`
  - Includes: AAR, POM, sources JAR, javadoc JAR, Gradle module metadata

### 3. Local Testing Verified

Both modules successfully published to Maven Local (`~/.m2/repository/`):
```
✓ com.fattmerchant:cardpresent:2.7.0
✓ com.fattmerchant:tokenization:2.7.0
```

All required artifacts are generated:
- `.aar` (Android Archive)
- `.pom` (Maven POM with complete metadata)
- `-sources.jar` (Source code)
- `-javadoc.jar` (Documentation)
- `.module` (Gradle metadata)

## 📋 Next Steps (Manual Setup Required)

### Step 1: Create Sonatype OSSRH Account

1. Go to https://issues.sonatype.org/
2. Create a JIRA account
3. Create a "New Project" ticket to claim `com.fattmerchant` group ID
4. Wait for approval (usually 1-2 business days)
5. You may need to prove domain ownership

### Step 2: Generate GPG Keys

```bash
# Generate new GPG key
gpg --gen-key

# List keys to get KEY_ID
gpg --list-secret-keys --keyid-format=long

# Publish public key to keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Export secret keyring for GitHub Actions
gpg --export-secret-keys -o ~/.gnupg/secring.gpg
```

### Step 3: Configure Credentials

Add to `~/.gradle/gradle.properties` (NOT the project's gradle.properties):

```properties
ossrhUsername=your_sonatype_username
ossrhPassword=your_sonatype_password

signing.keyId=YOUR_KEY_ID
signing.password=your_gpg_password
signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
```

### Step 4: Configure GitHub Secrets

For automated publishing via GitHub Actions, add these secrets to your repository:

1. Go to GitHub repository → Settings → Secrets and variables → Actions
2. Add the following secrets:
   - `OSSRH_USERNAME` - Your Sonatype username
   - `OSSRH_PASSWORD` - Your Sonatype password
   - `SIGNING_KEY_ID` - Your GPG key ID
   - `SIGNING_PASSWORD` - Your GPG key password
   - `GPG_KEY_BASE64` - Base64 encoded secret keyring:
     ```bash
     base64 ~/.gnupg/secring.gpg | pbcopy
     ```

### Step 5: Test Publishing to Staging

Once credentials are configured:

```bash
# Publish both modules to Sonatype staging
./gradlew publish

# Or publish individual modules
./gradlew :cardpresent:publish
./gradlew :tokenization:publish
```

### Step 6: Release to Maven Central

1. Log in to https://s01.oss.sonatype.org/
2. Go to "Staging Repositories"
3. Find your staging repository (com.fattmerchant-xxxx)
4. Click "Close" and wait for validation
5. If validation passes, click "Release"
6. Wait 2-4 hours for sync to Maven Central
7. Verify at https://search.maven.org/search?q=g:com.fattmerchant

### Step 7: Register with Google SDK Console

Once published to Maven Central:

1. Go to https://console.cloud.google.com/sdks
2. Click "Register SDK"
3. Provide Maven coordinates:
   - **Card Present SDK**: `com.fattmerchant:cardpresent:2.7.0`
   - **Tokenization SDK**: `com.fattmerchant:tokenization:2.7.0`
4. Upload documentation (use `cardpresent/README.md`)
5. Add usage examples from the README
6. Submit for review

## 📦 Maven Coordinates

Once published, developers can add your SDK to their projects:

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("com.fattmerchant:cardpresent:2.7.0")
    implementation("com.fattmerchant:tokenization:2.7.0")
}
```

### Gradle (Groovy)
```groovy
dependencies {
    implementation 'com.fattmerchant:cardpresent:2.7.0'
    implementation 'com.fattmerchant:tokenization:2.7.0'
}
```

## 🚀 Automated Publishing Workflow

The GitHub Actions workflow (`.github/workflows/publish.yml`) is configured to:

1. **Trigger**: When you create a new release on GitHub
2. **Build**: Compile and test both modules
3. **Publish**: Upload to Maven Central staging
4. **Manual Alternative**: Can also be triggered manually from Actions tab

## 🔍 Troubleshooting

### Common Issues

1. **"401 Unauthorized"** during publish
   - Check OSSRH credentials in `~/.gradle/gradle.properties`
   - Verify account has access to `com.fattmerchant` group

2. **"GPG signing failed"**
   - Verify GPG key ID and password
   - Check secret keyring file path
   - Ensure key hasn't expired

3. **"POM validation failed"**
   - Ensure all required POM elements are present (already configured)
   - Check that SCM URLs are accessible

4. **"Artifact already exists"**
   - Can't republish same version to Maven Central
   - Increment version number for new releases

### Useful Commands

```bash
# Test local publishing (no credentials needed)
./gradlew publishToMavenLocal

# Check published artifacts
ls ~/.m2/repository/com/fattmerchant/*/2.7.0/

# View all publishing tasks
./gradlew tasks --group=publishing

# Publish with verbose output
./gradlew publish --info

# Clean and rebuild before publishing
./gradlew clean build publish
```

## 📚 Additional Resources

- [PUBLISHING.md](./PUBLISHING.md) - Complete publishing guide
- [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Gradle Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
- [GPG Quick Start](https://central.sonatype.org/publish/requirements/gpg/)

## ⚠️ Security Notes

- **Never commit** credentials to version control
- Store credentials only in `~/.gradle/gradle.properties` (user home, not project)
- GitHub Secrets are encrypted and only accessible to workflows
- GPG private keys should be kept secure and backed up

## 📝 Version Management

Current versions in `gradle.properties`:
- `PUBLISH_GROUP_ID=com.fattmerchant`
- `PUBLISH_VERSION=2.7.0`

To release a new version:
1. Update `PUBLISH_VERSION` in `gradle.properties`
2. Commit the change
3. Create a GitHub release with tag matching the version
4. GitHub Actions will automatically publish (if configured)

Or manually:
```bash
./gradlew clean build publish
```

## ✨ What's Next?

1. Complete the Sonatype OSSRH account setup
2. Generate and configure GPG keys
3. Test publishing to Maven Central staging
4. Release your first version to Maven Central
5. Register with Google SDK Console
6. Update your documentation with Maven coordinates
7. Celebrate! 🎉
