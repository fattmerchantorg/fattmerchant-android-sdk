h1. Maven Central Account Setup for Android SDK Publishing

*Summary:* Setup Maven Central publishing account and GPG signing keys for Fattmerchant Android SDK distribution

*Priority:* Medium
*Issue Type:* Service Request
*Category:* Developer Tools & Access
*Time Estimate:* 2-3 hours
*Reporter:* Engineering Team
*Date:* December 10, 2025

----

h2. Request Summary

We need to publish our Fattmerchant Android SDK to Maven Central Repository to enable distribution through the Google SDK Console. This requires setting up a Maven Central Publishing account and generating GPG signing keys for artifact verification.

*Maven Coordinates:*
* Group ID: {{com.fattmerchant}}
* Artifacts: {{cardpresent}} (v2.7.0), {{tokenization}} (v2.7.0)
* Repository: [Maven Central|https://central.sonatype.org/]

----

h2. What We Need From IT

# *Maven Central Publisher Account* with access to {{com.fattmerchant}} namespace
# *GPG Key Pair* for artifact signing (or credentials to generate one)
# *Secure credential delivery* for:
## Maven Central username/password
## GPG Key ID, password, and secret keyring file

----

h2. Step-by-Step Setup Process

h3. Step 1: Create Maven Central Account

*Platform:* Sonatype Central Portal (New System - Launched 2024)

# *Navigate to:* [https://central.sonatype.org/register/central-portal/]
# *Click "Sign Up"* and create account with:
#* Company email address (recommended: use shared team email like {{android-dev@fattmerchant.com}} or IT managed email)
#* Secure password (store in company password manager)
# *Verify email address* by clicking the confirmation link
# *Sign in* at [https://central.sonatype.org/]

h3. Step 2: Claim Namespace (Group ID)

We need to claim the {{com.fattmerchant}} namespace to publish under our company domain.

# *Navigate to:* "Namespaces" section in Central Portal
# *Click "Add Namespace"*
# *Enter:* {{com.fattmerchant}}
# *Verification Options - Choose ONE:*

*Option A: Domain Ownership (Recommended)*
* Add DNS TXT record to {{fattmerchant.com}}:
{code}
Type: TXT
Name: central (or @)
Value: [Verification token provided by Sonatype]
TTL: 3600
{code}
* Submit for verification
* Remove TXT record after approval

*Option B: GitHub Repository*
* Create public repository: {{https://github.com/fattmerchantorg/OSSRH-XXXXX}}
* Name must match ticket number provided by Sonatype
* Submit repository URL for verification

# *Wait for Approval* (Usually 1-2 business days)
#* You'll receive email notification when approved
#* Status visible in Central Portal dashboard

h3. Step 3: Generate User Token for Publishing

After namespace approval:

# *Go to:* Account Settings → User Tokens
# *Click "Generate User Token"*
# *Save credentials securely:*
#* Username: (auto-generated token, looks like random string)
#* Password: (auto-generated token, looks like random string)
#* *CRITICAL:* Store in company password manager (1Password, LastPass, etc.)
#* These credentials cannot be recovered, only regenerated

h3. Step 4: Generate GPG Key for Signing

All artifacts published to Maven Central must be cryptographically signed.

*On a secure workstation (preferably IT-managed machine):*

# *Install GPG* (if not already installed):
{code:bash}
# macOS
brew install gnupg

# Verify installation
gpg --version
{code}

# *Generate new GPG key pair:*
{code:bash}
gpg --full-generate-key
{code}

# *Follow prompts:*
#* Key type: {{(1) RSA and RSA}}
#* Key size: {{4096}} bits
#* Expiration: {{0}} (no expiration) or {{2y}} (2 years, recommended for security)
#* Real name: {{Fattmerchant Engineering}} or {{Fattmerchant, Inc.}}
#* Email: {{android-dev@fattmerchant.com}} (or company email)
#* Comment: {{Android SDK Signing Key}}
#* Passphrase: *Create strong passphrase* (store in password manager)

# *List keys to get Key ID:*
{code:bash}
gpg --list-secret-keys --keyid-format=long
{code}
Output example:
{code}
sec   rsa4096/ABCD1234EFGH5678 2025-12-10 [SC]
{code}
The Key ID is: {{ABCD1234EFGH5678}} (save this)

# *Publish public key to keyserver:*
{code:bash}
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EFGH5678

# Also publish to additional servers for redundancy:
gpg --keyserver keys.openpgp.org --send-keys ABCD1234EFGH5678
gpg --keyserver pgp.mit.edu --send-keys ABCD1234EFGH5678
{code}

# *Export secret keyring for development team:*
{code:bash}
# Export secret key
gpg --export-secret-keys -o fattmerchant-android-signing-key.gpg

# Create ASCII-armored version (more portable)
gpg --armor --export-secret-keys ABCD1234EFGH5678 > fattmerchant-android-signing-key.asc
{code}

# *Backup GPG keys securely:*
#* Store in company vault/secrets management system
#* Document location in IT knowledge base
#* Consider creating revocation certificate:
{code:bash}
gpg --gen-revoke ABCD1234EFGH5678 > revocation-cert.asc
{code}

h3. Step 5: Prepare Credentials Package for Engineering Team

Create a secure package containing:

# *Maven Central Credentials:*
{code}
Maven Central Username: [user token from Step 3]
Maven Central Password: [password token from Step 3]
Portal URL: https://central.sonatype.org/
{code}

# *GPG Signing Credentials:*
{code}
GPG Key ID: ABCD1234EFGH5678
GPG Key Passphrase: [passphrase from Step 4]
{code}

# *Files to securely transfer:*
#* {{fattmerchant-android-signing-key.gpg}} (secret keyring)
#* {{fattmerchant-android-signing-key.asc}} (ASCII-armored version)
#* {{revocation-cert.asc}} (revocation certificate for emergencies)

# *Delivery Method - Choose secure option:*
#* Company secrets management system (Vault, AWS Secrets Manager, etc.)
#* Encrypted password manager shared folder
#* Encrypted email with password shared separately
#* *DO NOT:* Send via Slack, unencrypted email, or commit to repository

----

h2. Security Requirements

h4. Critical Security Notes:

# *Never commit credentials to Git:*
#* No passwords in code
#* No keys in repository
#* Use {{.gitignore}} for credential files

# *Access Control:*
#* Maven Central account should be IT-managed
#* Consider shared team account vs individual accounts
#* Document who has access

# *Key Management:*
#* GPG private keys stored in secure vault
#* Passphrase stored separately in password manager
#* Regular key rotation policy (every 2 years recommended)

# *Backup Strategy:*
#* Store GPG keys in company backup system
#* Document recovery procedures
#* Test key recovery process

h4. Developer Access Pattern:

Developers will receive credentials to store locally in:
{code}
~/.gradle/gradle.properties
{code}

This file should contain:
{code:properties}
ossrhUsername=<Maven Central username token>
ossrhPassword=<Maven Central password token>

signing.keyId=<GPG Key ID>
signing.password=<GPG passphrase>
signing.secretKeyRingFile=/Users/<username>/.gnupg/fattmerchant-signing-key.gpg
{code}

----

h2. Verification Checklist

After setup, verify:

* (/) Maven Central account created and email verified
* (/) {{com.fattmerchant}} namespace claimed and approved
* (/) User token generated and stored in password manager
* (/) GPG key pair generated (4096-bit RSA)
* (/) GPG public key published to multiple keyservers
* (/) GPG private key exported and backed up securely
* (/) Revocation certificate created and stored
* (/) All credentials documented in IT knowledge base
* (/) Credentials package prepared for engineering team
* (/) Secure delivery method confirmed

----

h2. Expected Timeline
||Task||Estimated Time||
|Account creation & namespace claim|30 minutes|
|Namespace verification (waiting period)|1-2 business days|
|GPG key generation & publishing|30 minutes|
|Credential packaging & documentation|30 minutes|
|Secure delivery to dev team|15 minutes|
|*Total Active Work*|~2 hours|
|*Total Calendar Time*|1-3 business days|

----

h2. Troubleshooting

h4. Common Issues:

*1. Namespace verification fails:*
* *DNS option:* Ensure TXT record propagated (check with {{dig TXT fattmerchant.com}})
* *GitHub option:* Verify repository is public and correctly named
* Contact [Sonatype support|https://central.sonatype.org/support/]

*2. GPG key generation fails:*
* Ensure GPG is properly installed: {{gpg --version}}
* May need more entropy (move mouse, type on keyboard during generation)
* On servers: Install {{rng-tools}} or {{haveged}}

*3. Keyserver upload fails:*
* Try different keyserver
* Check firewall rules (keyservers use port 11371)
* Some keyservers may be temporarily down

*4. User token not working:*
* Verify token was copied completely (no extra spaces)
* Check if namespace approval is complete
* Try regenerating token if issues persist

----

h2. Support Contacts

*Sonatype Support:*
* [Help Center|https://central.sonatype.org/support/]
* [Community Forum|https://community.sonatype.com/]
* [Documentation|https://central.sonatype.org/publish/]

*Internal Contacts:*
* Engineering Team Lead: [Contact info]
* DevOps/IT Manager: [Contact info]
* Security Team: [Contact info]

----

h2. Additional Resources

*Official Documentation:*
* [Maven Central Portal Guide|https://central.sonatype.org/register/central-portal/]
* [Publishing Guide|https://central.sonatype.org/publish/]
* [GPG Setup|https://central.sonatype.org/publish/requirements/gpg/]

*Verification Tools:*
* [DNS Propagation Check|https://dnschecker.org/]
* [Keyserver Search|https://keyserver.ubuntu.com/]
* GPG Key Verification: {{gpg --list-keys}}

----

h2. Post-Setup Documentation

*Please document in IT knowledge base:*

# *Account Details:*
#* Maven Central account email
#* Namespace claimed: {{com.fattmerchant}}
#* Approval date
#* Location of credentials in secrets manager

# *GPG Key Information:*
#* Key ID
#* Creation date
#* Expiration date (if set)
#* Backup location
#* Revocation certificate location

# *Access Log:*
#* Who has access to credentials
#* Date credentials were distributed
#* Next rotation date

----

h2. Success Criteria

This ticket is complete when:

# (/) Engineering team receives all credentials securely
# (/) Test publication succeeds to Maven Central staging
# (/) Published artifacts appear on Maven Central search
# (/) All credentials documented and backed up
# (/) Access procedures documented in knowledge base

----

{panel:title=Note to IT Team|borderStyle=solid|borderColor=#ccc|titleBGColor=#f0f0f0|bgColor=#fafafa}
Once setup is complete, engineering will handle the actual artifact publishing through automated CI/CD pipeline. The credentials you provide will be stored in GitHub Secrets for automated releases and in developers' local Gradle configuration for manual testing.
{panel}

If you have any questions during setup, please contact the requesting developer or reference the detailed guides at the URLs provided above.

{color:#14892c}Thank you for your support!{color}
