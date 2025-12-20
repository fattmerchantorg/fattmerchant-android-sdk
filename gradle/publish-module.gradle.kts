import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing

// Configure publishing metadata
val publishGroupId = "com.fattmerchant"
val publishVersion = project.findProperty("PUBLISH_VERSION") as String? ?: "2.7.0"
val publishArtifactId = project.name

// Library metadata
val libraryName = "Fattmerchant Android SDK - ${project.name}"
val libraryDescription = "Accept payments on Android using card readers and NFC Tap to Pay"
val libraryUrl = "https://github.com/fattmerchantorg/fattmerchant-android-sdk"

// Developer information
val developerId = "fattmerchant"
val developerName = "Fattmerchant"
val developerEmail = "support@fattmerchant.com"

// SCM information
val scmUrl = "https://github.com/fattmerchantorg/fattmerchant-android-sdk"
val scmConnection = "scm:git:git://github.com/fattmerchantorg/fattmerchant-android-sdk.git"
val scmDevConnection = "scm:git:ssh://git@github.com:fattmerchantorg/fattmerchant-android-sdk.git"

// License
val licenseName = "MIT License"
val licenseUrl = "https://opensource.org/licenses/MIT"

// Generate javadoc JAR (empty for now)
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

afterEvaluate {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                groupId = publishGroupId
                artifactId = publishArtifactId
                version = publishVersion
                
                // Apply the component from Android Library plugin
                if (project.plugins.hasPlugin("com.android.library")) {
                    from(components["release"])
                }
                
                // Add javadoc artifact
                artifact(javadocJar)
                
                pom {
                    name.set(libraryName)
                    description.set(libraryDescription)
                    url.set(libraryUrl)
                    
                    licenses {
                        license {
                            name.set(licenseName)
                            url.set(licenseUrl)
                        }
                    }
                    
                    developers {
                        developer {
                            id.set(developerId)
                            name.set(developerName)
                            email.set(developerEmail)
                        }
                    }
                    
                    scm {
                        connection.set(scmConnection)
                        developerConnection.set(scmDevConnection)
                        url.set(scmUrl)
                    }
                }
            }
        }
        
        repositories {
            maven {
                name = "sonatype"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                    password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
                }
            }
        }
    }
    
    // Signing configuration (only if credentials are available)
    if (project.hasProperty("signing.keyId")) {
        configure<SigningExtension> {
            sign(the<PublishingExtension>().publications["release"])
        }
    }
    
    // Fix task dependencies - ensure source/javadoc jars are built before publishing
    tasks.matching { it.name == "generateMetadataFileForReleasePublication" }.configureEach {
        dependsOn(javadocJar)
    }
    tasks.withType<PublishToMavenRepository>().configureEach {
        dependsOn(javadocJar)
    }
    tasks.withType<PublishToMavenLocal>().configureEach {
        dependsOn(javadocJar)
    }
}
