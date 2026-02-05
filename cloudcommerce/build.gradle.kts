/**
 * Cloud Commerce SDK Module
 * 
 * This module wraps the NMI Cloud Commerce SDK AAR file as required by
 * the official NMI documentation for Tap to Pay on Android.
 * 
 * This is a Maven publishing wrapper that exposes the AAR as an artifact
 * for JitPack distribution. It avoids the Gradle limitation of "Direct
 * local .aar file dependencies are not supported when building an AAR."
 * 
 * Reference: https://docs.nmi.com/docs/preparing-for-development-android
 */

plugins {
    `maven-publish`
}

group = findProperty("PUBLISH_GROUP_ID")?.toString() ?: "com.fattmerchant"
version = "5.3.0"

// Create a default configuration that will be consumed by cardpresent module
configurations.maybeCreate("default")

// Add the AAR artifact to the default configuration
// Use MTF (test) AAR by default for development
val aarFile = file("../cardpresent/libs/cloud-commerce-sdk-mtf-5.3.0.aar")
artifacts.add("default", aarFile)

// For production builds, uncomment this line and comment the MTF line above:
// val aarFile = file("../cardpresent/libs/cloud-commerce-sdk-5.3.0.aar")

// Maven publishing configuration for JitPack
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = findProperty("PUBLISH_GROUP_ID")?.toString() ?: "com.fattmerchant"
            artifactId = "cloudcommerce"
            version = "5.3.0"
            
            // Publish the AAR file as the main artifact
            artifact(aarFile)
            
            pom {
                name.set("Cloud Commerce SDK")
                description.set("NMI Cloud Commerce SDK AAR wrapper for Tap to Pay on Android")
                url.set("https://github.com/fattmerchantorg/fattmerchant-android-sdk")
            }
        }
    }
}
