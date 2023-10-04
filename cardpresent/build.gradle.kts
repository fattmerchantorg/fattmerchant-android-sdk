plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}

group = "com.github.fattmerchantorg"
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.fattmerchantorg"
                artifactId = "cardpresent"
                version = "2.6.1"
            }
        }
    }
}

android {
    namespace = "com.fattmerchant"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += "META-INF/*.kotlin_module"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
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
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("net.zetetic:android-database-sqlcipher:4.0.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:runner:1.5.2")
    testImplementation("androidx.test:rules:1.5.0")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.robolectric:robolectric:4.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ChipDNA (NMI)
    api(files("libs/CardEaseXMLClient.jar"))
    api(files("libs/ChipDnaMobile.jar"))

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("net.zetetic:android-database-sqlcipher:4.0.1@aar")

    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.github.smart-fun:XmlToJson:1.4.4")
    api("org.slf4j:slf4j-api:1.7.30")
    api("com.github.tony19:logback-android:2.0.0")
    api("com.googlecode.libphonenumber:libphonenumber:8.5.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.3.0")
    implementation("org.simpleframework:simple-xml:2.7.1") {
        exclude(group = "stax", module = "stax-api")
        exclude(group = "xpp3", module = "xpp3")
    }

    // Ktor
    implementation("io.ktor:ktor-client-core:1.1.4")
    implementation("io.ktor:ktor-client-okhttp:1.1.4")
    implementation("io.ktor:ktor-client-ios:1.1.4")
    implementation("io.ktor:ktor-client-json:1.1.4")
    implementation("io.ktor:ktor-client-gson:1.1.4")
}
