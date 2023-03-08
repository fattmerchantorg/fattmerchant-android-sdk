plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

group = "com.github.fattmerchantorg"

android {
    compileSdk = 33
    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
                isReturnDefaultValues = true
            }
        }
    }

    testOptions.unitTests.all { test ->
        test.jvmArgs("-noverify")
    }

    packagingOptions {
        resources {
            excludes += "META-INF/*.kotlin_module"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
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

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:runner:1.5.2")
    testImplementation("androidx.test:rules:1.5.0")
    testImplementation("androidx.test:core:1.5.0")

    testImplementation("org.robolectric:robolectric:4.9")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("net.zetetic:android-database-sqlcipher:4.5.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.0@aar")

    // AnywhereCommerce
    compileOnly(project(":awc"))

    implementation("com.dvmms:dejapay:2.0.37")
    implementation("com.google.code.gson:gson:2.9.1")

    // Ktor
    implementation("io.ktor:ktor-client-core:1.1.2")
    implementation("io.ktor:ktor-client-okhttp:1.1.2")
    implementation("io.ktor:ktor-client-ios:1.1.2")
    implementation("io.ktor:ktor-client-json:1.1.2")
    implementation("io.ktor:ktor-client-gson:1.1.2")
}