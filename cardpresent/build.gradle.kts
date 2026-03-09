plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    `maven-publish`
    signing
}

// Set group: JitPack passes -Pgroup, otherwise use default for Maven Central
// Module group - use PUBLISH_GROUP_ID for Maven/JitPack compatibility, fallback for other builds
group = findProperty("PUBLISH_GROUP_ID")?.toString() ?: (findProperty("group") as String? ?: "com.fattmerchant")

android {
    namespace = "com.fattmerchant"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Export ProGuard rules to consuming apps
        consumerProguardFiles("proguard-rules.pro")
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.3.0"
    }
}

dependencies {
    // NMI Cloud Commerce SDK — compile-only, NOT bundled in the published artifact.
    // Consumer apps must add the Cloud Commerce AAR + its dependencies per NMI docs:
    // https://docs.nmi.com/docs/preparing-for-development-android
    compileOnly(project(":cloudcommerce-production"))

    // ChipDNA JARs (NMI reader SDK — bundled with cardpresent)
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // AndroidX AppCompat
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Jetpack Compose - For TapToPayPrompt UI
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    api(platform("androidx.compose:compose-bom:2026.02.00"))
    api("androidx.compose.ui:ui")
    api("androidx.compose.ui:ui-graphics")
    api("androidx.compose.ui:ui-tooling-preview")
    api("androidx.compose.material3:material3")
    api("androidx.compose.runtime:runtime:1.10.3")

    // Moshi & Gson (JSON Parsing — used by data models)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.google.code.gson:gson:2.8.6")

    // Kotlin
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Ktor
    implementation("io.ktor:ktor-client-core:3.4.0")
    implementation("io.ktor:ktor-client-okhttp:3.4.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-gson:3.4.0")

    // Required by Cloud Commerce SDK (bundled so consumer apps don't need to add these)
    //noinspection Aligned16KB
    implementation("net.zetetic:sqlcipher-android:4.7.2@aar")
    implementation("androidx.sqlite:sqlite:2.1.0")
    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.4.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.4.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.4.0")
    implementation("io.reactivex.rxjava3:rxjava:3.0.0")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.play:integrity:1.6.0")
    implementation("com.google.android.gms:play-services-safetynet:18.1.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("commons-codec:commons-codec:1.11")
    implementation("org.slf4j:slf4j-api:1.7.30")
}

// Ensure Kotlin metadata is properly generated for top-level functions
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        // Explicitly set module name to ensure .kotlin_module generation
        freeCompilerArgs.add("-module-name=cardpresent")
    }
}

// Publishing configuration - single variant (no more production/mtf flavors)
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = group.toString()
                artifactId = "cardpresent"
                version = findProperty("PUBLISH_VERSION") as String? ?: "2.7.0"

                pom {
                    name.set("Fattmerchant Android SDK - Card Present")
                    description.set("Accept payments on Android using card readers and NFC Tap to Pay")
                    url.set("https://github.com/fattmerchantorg/fattmerchant-android-sdk")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("fattmerchant")
                            name.set("Fattmerchant")
                            email.set("support@fattmerchant.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/fattmerchantorg/fattmerchant-android-sdk.git")
                        developerConnection.set("scm:git:ssh://git@github.com:fattmerchantorg/fattmerchant-android-sdk.git")
                        url.set("https://github.com/fattmerchantorg/fattmerchant-android-sdk")
                    }
                }
            }
        }
    }
}
