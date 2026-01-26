description = "Autoscaling for Spring Boot applications on Heroku and Render"

dependencies {
    // Judoscale Core
    api(project(":judoscale-core"))

    // Spring Boot Web (provided - the app will have this)
    compileOnly(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.starter.web)

    // Spring Boot Auto-configuration
    implementation(libs.spring.boot.autoconfigure)

    // For @ConfigurationProperties
    annotationProcessor(libs.spring.boot.configuration.processor)

    // JSON processing (for API client)
    implementation(libs.jackson.databind)

    // Logging
    implementation(libs.slf4j.api)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.byte.buddy)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Judoscale Spring Boot Starter")
                description.set(project.description)
                url.set("https://github.com/judoscale/judoscale-java")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("judoscale")
                        name.set("Judoscale")
                        email.set("support@judoscale.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/judoscale/judoscale-java.git")
                    developerConnection.set("scm:git:ssh://github.com/judoscale/judoscale-java.git")
                    url.set("https://github.com/judoscale/judoscale-java")
                }
            }
        }
    }

    repositories {
        maven {
            name = "MavenCentral"
            url = if (version.toString().endsWith("-SNAPSHOT")) {
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
            credentials {
                username = System.getenv("MAVEN_USERNAME") ?: project.findProperty("maven.username") as String?
                password = System.getenv("MAVEN_PASSWORD") ?: project.findProperty("maven.password") as String?
            }
        }
    }
}

signing {
    val signingKey = System.getenv("GPG_SIGNING_KEY")
    val signingPassword = System.getenv("GPG_SIGNING_PASSWORD")
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications["mavenJava"])
}
