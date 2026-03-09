import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.android.utils.jvmArchitecture

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val staxApiKey: String = gradleLocalProperties(rootDir, providers).getProperty("staxApiKey") ?: "NoApiKey"

android {
    namespace = "com.staxpayments.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.staxpayments.sample"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // TODO: Enable Tap to Pay
        // These BuildConfig fields are used by the chipdnatest flavor (commented out below).
        // Uncomment when enabling the chipdnatest flavor.
        // buildConfigField("Boolean", "IS_NMI_TEST_FLAVOR", "false")
        // buildConfigField("String", "NMI_TEST_API_KEY", "\"\"")
        // buildConfigField("String", "NMI_TEST_APP_ID", "\"\"")
        // buildConfigField("String", "NMI_TEST_CERT_FINGERPRINT", "\"\"")
    }

    packaging {
        resources {
            excludes += "META-INF/*.kotlin_module"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Flavors select which Cloud Commerce SDK variant to use
    flavorDimensions += "environment"
    productFlavors {
        create("production") {
            dimension = "environment"
        }
        create("mtf") {
            dimension = "environment"
            applicationIdSuffix = ".mtf"
        }
        // TODO: Enable Tap to Pay
        // chipdnatest: uses NMI's pre-registered test identity so you can validate the SDK
        // code path against the same sandbox environment where ChipDnaMobileKotlinDemo succeeds.
        // Make sure the applicationId matches the one registered with NMI for Tap to Pay.
        // Make sure the APK is signed with the keystore matching the certificate fingerprint
        // shared with NMI.
        //
        // create("chipdnatest") {
        //     dimension = "environment"
        //     applicationId = "com.creditcall.chipdnamobiledev"  // NMI pre-registered package name
        //     matchingFallbacks += listOf("mtf")
        //     buildConfigField("Boolean", "IS_NMI_TEST_FLAVOR", "true")
        //     buildConfigField("String", "NMI_TEST_API_KEY", "\"YOUR_NMI_API_KEY\"")
        //     buildConfigField("String", "NMI_TEST_APP_ID", "\"YOUR_APP_ID\"")
        //     buildConfigField("String", "NMI_TEST_CERT_FINGERPRINT", "\"YOUR_CERT_SHA256_FINGERPRINT\"")
        // }
    }

    buildTypes {
        release {
            buildConfigField("String", "STAX_API_KEY", "\"$staxApiKey\"")
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
        debug {
            isDefault = true
            isDebuggable = true
            buildConfigField("String", "STAX_API_KEY", "\"$staxApiKey\"")
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Stax SDK (includes ChipDNA driver + all Cloud Commerce transitive deps)
    implementation(project(":cardpresent"))

    // TODO: Enable Tap to Pay
    // The NMI Cloud Commerce SDK AAR is ONLY required for Tap to Pay (NFC) transactions.
    // If you don't need Tap to Pay, you can leave these commented out.
    // https://docs.nmi.com/docs/preparing-for-development-android
    // add("productionImplementation", project(mapOf("path" to ":CloudCommerceSDK", "configuration" to "prod")))
    // add("mtfImplementation", project(mapOf("path" to ":CloudCommerceSDK", "configuration" to "mtf")))
    // add("chipdnatestImplementation", project(mapOf("path" to ":CloudCommerceSDK", "configuration" to "mtf")))

    // Dependencies
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.4")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2026.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")

    // Google Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
}
