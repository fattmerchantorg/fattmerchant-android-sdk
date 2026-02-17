/**
 * Cloud Commerce SDK Module - Production
 * 
 * This module wraps the NMI Cloud Commerce SDK AAR for production environment.
 */

plugins {
    `maven-publish`
}

group = findProperty("PUBLISH_GROUP_ID")?.toString() ?: (findProperty("group") as String? ?: "com.fattmerchant")
val publishVersion = "5.3.1"

// Production AAR artifact
val productionAarFile = file("../cardpresent/libs/cloud-commerce-sdk-5.3.1.aar")
configurations {
    create("default")
}
artifacts {
    add("default", productionAarFile)
}

// Publishing configuration for JitPack
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = group.toString()
            artifactId = "cloudcommerce-production"
            version = publishVersion
            
            artifact(productionAarFile)
            
            pom {
                name.set("Cloud Commerce SDK (Production)")
                description.set("NMI Cloud Commerce SDK for production Android applications")
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
