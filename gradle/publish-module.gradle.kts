import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing

// Configure publishing metadata
// Support both JitPack (passes -Pgroup via command line) and Maven Central (uses PUBLISH_GROUP_ID from gradle.properties)
// For JitPack: ./gradlew -Pgroup=com.github.user -Pversion=1.0 publishToMavenLocal
// For Maven Central: Uses PUBLISH_GROUP_ID and PUBLISH_VERSION from gradle.properties
val jitpackGroup = project.findProperty("group") as String?
val mavenGroup = project.findProperty("PUBLISH_GROUP_ID") as String?
val publishGroupId = when {
    jitpackGroup != null && jitpackGroup != project.group.toString() -> jitpackGroup
    mavenGroup != null -> mavenGroup
    else -> "com.fattmerchant"
}

val jitpackVersion = project.findProperty("version") as String?
val mavenVersion = project.findProperty("PUBLISH_VERSION") as String?
val publishVersion = when {
    jitpackVersion != null && jitpackVersion != project.version.toString() -> jitpackVersion
    mavenVersion != null -> mavenVersion
    else -> "2.7.0"
}

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
                // Use explicit groupId for Maven publication (don't use project.group)
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
                // New Central Portal (2024+) requires the central-publishing Gradle plugin
                // For now, publish to mavenLocal then manually upload the bundle
                // See: https://central.sonatype.org/publish/publish-gradle/
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
