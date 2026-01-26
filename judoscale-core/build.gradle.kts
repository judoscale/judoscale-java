description = "Core library for Judoscale Java integrations"

dependencies {
    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
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
                name.set("Judoscale Core")
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
