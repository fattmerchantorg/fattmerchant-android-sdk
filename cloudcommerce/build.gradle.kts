/**
 * Cloud Commerce SDK Module
 * 
 * This module wraps the NMI Cloud Commerce SDK AAR file as required by
 * the official NMI documentation for Tap to Pay on Android.
 * 
 * This is a simple wrapper module that exposes the AAR as an artifact.
 * It avoids the Gradle limitation of "Direct local .aar file dependencies 
 * are not supported when building an AAR."
 * 
 * Reference: https://docs.nmi.com/docs/preparing-for-development-android
 */

// Create a default configuration that will be consumed by cardpresent module
configurations.maybeCreate("default")

// Add the AAR artifact to the default configuration
// Use MTF (test) AAR by default for development
artifacts.add("default", file("../cardpresent/libs/cloud-commerce-sdk-mtf-5.3.0.aar"))

// For production builds, uncomment this line and comment the MTF line above:
// artifacts.add("default", file("../cardpresent/libs/cloud-commerce-sdk-5.3.0.aar"))
