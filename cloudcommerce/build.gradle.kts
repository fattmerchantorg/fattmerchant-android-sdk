/**
 * Cloud Commerce SDK Module
 * 
 * This module wraps the NMI Cloud Commerce SDK AAR files as required by
 * the official NMI documentation for Tap to Pay on Android.
 * 
 * The Cloud Commerce SDK provides two AARs:
 * - cloud-commerce-sdk-mtf-5.3.0.aar (Test/MTF environment)
 * - cloud-commerce-sdk-5.3.0.aar (Production environment)
 * 
 * Reference: https://docs.nmi.com/docs/preparing-for-development-android
 */

// Create configuration and add the AAR artifact
configurations.maybeCreate("default")

// Use MTF (test) AAR by default for development
// Switch to production AAR for release builds
artifacts.add("default", file("../cardpresent/libs/cloud-commerce-sdk-mtf-5.3.0.aar"))

// For production builds, uncomment this line and comment the MTF line above:
// artifacts.add("default", file("../cardpresent/libs/cloud-commerce-sdk-5.3.0.aar"))
