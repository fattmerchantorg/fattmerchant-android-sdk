/**
 * Cloud Commerce SDK Module - Production
 *
 * Local-only project dependency wrapper. NOT published to JitPack or Maven Central.
 * Consumer apps must add the Cloud Commerce AAR locally per NMI docs.
 */

// Production AAR artifact — exposed via "default" configuration for compileOnly use by :cardpresent
val productionAarFile = file("libs/cloud-commerce-sdk-5.3.1.aar")
configurations {
    create("default")
}
artifacts {
    add("default", productionAarFile)
}
