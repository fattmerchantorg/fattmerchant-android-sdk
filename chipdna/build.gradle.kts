plugins {
    id("com.android.library")
    `maven-publish`
    signing
}

group = findProperty("PUBLISH_GROUP_ID")?.toString() ?: (findProperty("group") as String? ?: "com.fattmerchant")

android {
    namespace = "com.fattmerchant.chipdna"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
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

    // Flavor dimension to select production vs MTF Cloud Commerce SDK
    flavorDimensions += "environment"
    productFlavors {
        create("production") {
            dimension = "environment"
        }
        create("mtf") {
            dimension = "environment"
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
}

dependencies {
    // Depend on cardpresent for SDK interfaces (MobileReaderDriver, MobileReader, etc.)
    api(project(":cardpresent"))

    // NMI Cloud Commerce SDK AARs (flavor-specific)
    add("productionApi", project(":cloudcommerce-production"))
    add("mtfApi", project(":cloudcommerce-mtf"))

    // ChipDNA JARs (core reader SDK)
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Required by Cloud Commerce SDK
    //noinspection Aligned16KB
    implementation("net.zetetic:sqlcipher-android:4.7.2@aar")
    implementation("androidx.sqlite:sqlite:2.1.0")
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Retrofit & OkHttp (Required by Cloud Commerce SDK)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.4.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.4.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.4.0")

    // Moshi (Required by Cloud Commerce SDK)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")

    // RxJava (Required by Cloud Commerce SDK)
    implementation("io.reactivex.rxjava3:rxjava:3.0.0")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")

    // Google Play Services (Required for Tap to Pay)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.play:integrity:1.6.0")
    implementation("com.google.android.gms:play-services-safetynet:18.1.0")

    // Security (Required by Cloud Commerce SDK)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Apache Commons (Required by Cloud Commerce SDK)
    implementation("commons-codec:commons-codec:1.11")

    // SLF4J (Required by Cloud Commerce SDK)
    implementation("org.slf4j:slf4j-api:1.7.30")

    // Ktor (used by TransactionGateway)
    implementation("io.ktor:ktor-client-core:3.4.0")
    implementation("io.ktor:ktor-client-okhttp:3.4.0")
}

// Publishing configuration for product flavors
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("productionRelease") {
                from(components["productionRelease"])
                groupId = group.toString()
                artifactId = "chipdna-production"
                version = findProperty("PUBLISH_VERSION") as String? ?: "2.7.0"

                pom {
                    name.set("Fattmerchant Android SDK - ChipDNA Driver (Production)")
                    description.set("NMI ChipDNA mobile reader driver for the Fattmerchant Android SDK - Production version")
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

            create<MavenPublication>("mtfRelease") {
                from(components["mtfRelease"])
                groupId = group.toString()
                artifactId = "chipdna-mtf"
                version = findProperty("PUBLISH_VERSION") as String? ?: "2.7.0"

                pom {
                    name.set("Fattmerchant Android SDK - ChipDNA Driver (MTF)")
                    description.set("NMI ChipDNA mobile reader driver for the Fattmerchant Android SDK - MTF testing version")
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
