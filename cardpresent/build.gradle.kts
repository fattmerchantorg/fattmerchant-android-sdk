plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    `maven-publish`
    signing
}

// Set group: JitPack passes -Pgroup, otherwise use default for Maven Central
group = project.findProperty("group") as String? ?: "com.fattmerchant"

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

    // Define product flavors to match cloudcommerce module
    // This allows SDK users to choose between production and MTF (testing) environments
    flavorDimensions += "environment"
    productFlavors {
        create("production") {
            dimension = "environment"
            // Production flavor uses the standard Cloud Commerce SDK
        }
        create("mtf") {
            dimension = "environment"
            // MTF flavor uses the Merchant Test Framework version for testing
        }
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
        singleVariant("productionRelease") {
            withSourcesJar()
        }
        singleVariant("mtfRelease") {
            withSourcesJar()
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.3.0"
    }
}

dependencies {
    // AndroidX AppCompat (Required by Cloud Commerce SDK themes)
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    
    // Jetpack Compose - For TapToPayPrompt UI
    implementation("androidx.activity:activity-compose:1.12.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    api(platform("androidx.compose:compose-bom:2026.01.01"))
    api("androidx.compose.ui:ui")
    api("androidx.compose.ui:ui-graphics")
    api("androidx.compose.ui:ui-tooling-preview")
    api("androidx.compose.material3:material3")
    api("androidx.compose.runtime:runtime:1.10.2")

    // NMI Cloud Commerce SDK - Tap to Pay Support
    // Flavor-specific dependencies to use the appropriate AAR
    add("productionApi", project(path = ":cloudcommerce", configuration = "productionDefault"))
    add("mtfApi", project(path = ":cloudcommerce", configuration = "mtfDefault"))
    
    // NMI Legacy Dependencies
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    //noinspection Aligned16KB
    implementation("net.zetetic:android-database-sqlcipher:4.5.4@aar")
    implementation("androidx.sqlite:sqlite:2.6.2")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Retrofit & OkHttp (Required by Cloud Commerce SDK)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.retrofit2:converter-scalars:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:5.0.0-alpha.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    
    // Moshi & Gson (JSON Parsing)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.google.code.gson:gson:2.13.2")
    
    // RxJava (Required by Cloud Commerce SDK)
    implementation("io.reactivex.rxjava3:rxjava:3.1.12")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("com.squareup.retrofit2:adapter-rxjava3:3.0.0")
    
    // Google Play Services - Location, Integrity & SafetyNet (Required for Tap to Pay)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.play:integrity:1.6.0")
    implementation("com.google.android.gms:play-services-safetynet:18.1.0")
    
    // Security (Required by Cloud Commerce SDK)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Apache Commons (Required by Cloud Commerce SDK)
    implementation("commons-codec:commons-codec:1.11")
    
    // SLF4J (Required by Cloud Commerce SDK)
    implementation("org.slf4j:slf4j-api:1.7.30")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Ktor
    implementation("io.ktor:ktor-client-core:3.4.0")
    implementation("io.ktor:ktor-client-okhttp:3.4.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-gson:3.4.0")
}

// Ensure Kotlin metadata is properly generated for top-level functions
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        // Explicitly set module name to ensure .kotlin_module generation
        freeCompilerArgs.add("-module-name=cardpresent")
    }
}
// Publishing configuration for product flavors
afterEvaluate {
    publishing {
        publications {
            // Production variant publication
            create<MavenPublication>("productionRelease") {
                from(components["productionRelease"])
                groupId = project.group.toString()
                artifactId = "cardpresent-production"
                version = findProperty("PUBLISH_VERSION") as String? ?: "2.7.0"
                
                pom {
                    name.set("Fattmerchant Android SDK - Card Present (Production)")
                    description.set("Accept payments on Android using card readers and NFC Tap to Pay - Production version")
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
            
            // MTF (testing) variant publication
            create<MavenPublication>("mtfRelease") {
                from(components["mtfRelease"])
                groupId = project.group.toString()
                artifactId = "cardpresent-mtf"
                version = findProperty("PUBLISH_VERSION") as String? ?: "2.7.0"
                
                pom {
                    name.set("Fattmerchant Android SDK - Card Present (MTF)")
                    description.set("Accept payments on Android using card readers and NFC Tap to Pay - MTF testing version")
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