plugins {
    `java-library`
    `maven-publish`
}

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
        create<MavenPublication>("gpr") {
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
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/judoscale/judoscale-java")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
}
