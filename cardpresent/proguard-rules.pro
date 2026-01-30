# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================================================
# Fattmerchant Android SDK - Public API
# ============================================================================

# Keep all public classes, methods, and fields in the SDK's public API
-keep public class com.fattmerchant.** { public *; }
-keep public interface com.fattmerchant.** { *; }
-keep public enum com.fattmerchant.** { *; }

# Keep Compose function names to prevent "Unresolved reference" errors
-keepclassmembers class com.fattmerchant.omni.ui.** {
    @androidx.compose.runtime.Composable public <methods>;
}

# Keep specific UI components that are part of the public API
-keep class com.fattmerchant.omni.ui.TapToPayPromptKt { *; }
-keep class com.fattmerchant.omni.ui.TapToPayState** { *; }

# Keep data classes and their properties (used in public API)
-keepclassmembers class com.fattmerchant.omni.data.** {
    public <fields>;
    public <methods>;
}

# Keep model classes
-keep class com.fattmerchant.omni.data.models.** { *; }

# Keep listeners and callbacks
-keep interface com.fattmerchant.omni.TransactionUpdateListener { *; }
-keep interface com.fattmerchant.omni.UserNotificationListener { *; }
-keep interface com.fattmerchant.omni.MobileReaderConnectionStatusListener { *; }
-keep interface com.fattmerchant.omni.SignatureProviding { *; }

# ============================================================================
# Jetpack Compose
# ============================================================================

# Keep Compose runtime classes
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep all @Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ============================================================================
# ChipDNA / NMI Cloud Commerce SDK
# ============================================================================

# Keep ChipDNA classes
-keep class com.creditcall.chipdnamobile.** { *; }
-keep interface com.creditcall.chipdnamobile.** { *; }

# ============================================================================
# Kotlin
# ============================================================================

# Keep Kotlin metadata for reflection
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep data class components (copy, equals, hashCode, toString)
-keepclassmembers class * {
    public ** component1();
    public ** component*();
    public ** copy(...);
}

# ============================================================================
# Retrofit & OkHttp
# ============================================================================

# Keep Retrofit interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ============================================================================
# SQLCipher
# ============================================================================

# Keep SQLCipher classes
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
