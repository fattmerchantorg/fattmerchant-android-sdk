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

group = findProperty("PUBLISH_GROUP_ID")?.toString() ?: (findProperty("group") as String? ?: "com.fattmerchant")
val publishVersion = "5.3.0"

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

// Publishing configuration for JitPack
publishing {
    publications {
        // Production variant publication
        create<MavenPublication>("productionRelease") {
            groupId = group.toString()
            artifactId = "cloudcommerce-production"
            version = publishVersion
            
            artifact(productionAarFile)
            
            pom {
                name.set("Cloud Commerce SDK (Production)")
                description.set("NMI Cloud Commerce SDK AAR wrapper for Tap to Pay - Production")
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
        
        // MTF variant publication
        create<MavenPublication>("mtfRelease") {
            groupId = group.toString()
            artifactId = "cloudcommerce-mtf"
            version = publishVersion
            
            artifact(mtfAarFile)
            
            pom {
                name.set("Cloud Commerce SDK (MTF)")
                description.set("NMI Cloud Commerce SDK AAR wrapper for Tap to Pay - MTF Testing")
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



