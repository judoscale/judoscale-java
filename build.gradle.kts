plugins {
    java
    `java-library`
    jacoco
    alias(libs.plugins.maven.publish) apply false
}

val versionFile = file("version.txt")
val projectVersion = if (versionFile.exists()) versionFile.readText().trim() else "0.1.0"

group = "com.judoscale"
version = projectVersion

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "jacoco")

    group = "com.judoscale"
    version = projectVersion

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
