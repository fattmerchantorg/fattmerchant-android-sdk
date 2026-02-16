/**
 * Cloud Commerce SDK Module
 * 
 * This module wraps the NMI Cloud Commerce SDK AAR files for different environments.
 * 
 * Product Flavors:
 * - production: Uses the production Cloud Commerce SDK
 * - mtf: Uses the MTF (Merchant Test Framework) Cloud Commerce SDK for testing
 * 
 * Reference: https://docs.nmi.com/docs/preparing-for-development-android
 */

plugins {
    `maven-publish`
}

group = findProperty("PUBLISH_GROUP_ID")?.toString() ?: "com.fattmerchant"
version = "5.3.0"

// Create configurations for each flavor
configurations {
    create("productionDefault")
    create("mtfDefault")
}

// Production AAR artifact
val productionAarFile = file("../cardpresent/libs/cloud-commerce-sdk-5.3.0.aar")
artifacts {
    add("productionDefault", productionAarFile)
}

// MTF AAR artifact  
val mtfAarFile = file("../cardpresent/libs/cloud-commerce-sdk-mtf-5.3.0.aar")
artifacts {
    add("mtfDefault", mtfAarFile)
}



