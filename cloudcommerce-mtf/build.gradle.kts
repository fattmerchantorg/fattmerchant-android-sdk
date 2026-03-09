/**
 * Cloud Commerce SDK Module - MTF
 *
 * Local-only project dependency wrapper. NOT published to JitPack or Maven Central.
 * Consumer apps must add the Cloud Commerce AAR locally per NMI docs.
 */

// MTF AAR artifact — exposed via "default" configuration for compileOnly use by :cardpresent
val mtfAarFile = file("libs/cloud-commerce-sdk-mtf-5.3.1.aar")
configurations {
    create("default")
}
artifacts {
    add("default", mtfAarFile)
}
