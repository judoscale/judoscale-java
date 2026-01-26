import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.maven.publish)
}

description = "Autoscaling for Spring Boot applications on Heroku, AWS, and other cloud hosts"

// Generate version properties file during build
tasks.register("generateVersionProperties") {
    val outputDir = layout.buildDirectory.dir("generated/resources")
    val versionValue = version.toString()

    outputs.dir(outputDir)

    doLast {
        val propsDir = outputDir.get().asFile.resolve("META-INF")
        propsDir.mkdirs()
        propsDir.resolve("judoscale.properties").writeText("version=$versionValue\n")
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("generateVersionProperties")
    from(layout.buildDirectory.dir("generated/resources"))
}

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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    coordinates(group.toString(), "judoscale-spring-boot-starter", version.toString())

    pom {
        name.set("Judoscale Spring Boot Starter")
        description.set(project.description)
        inceptionYear.set("2024")
        url.set("https://github.com/judoscale/judoscale-java")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
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
