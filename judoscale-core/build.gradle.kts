import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.maven.publish)
}

description = "Core library for Judoscale Java integrations"

// judoscale-core targets Java 8 for maximum compatibility
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    coordinates(group.toString(), "judoscale-core", version.toString())

    pom {
        name.set("Judoscale Core")
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
