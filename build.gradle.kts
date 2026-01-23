plugins {
    java
    `java-library`
}

group = "com.judoscale"
version = "0.1.0-SNAPSHOT"

// Dependency versions
val springBootVersion = "3.2.2"
val jacksonVersion = "2.16.1"
val slf4jVersion = "2.0.11"
val junitVersion = "5.10.1"
val assertjVersion = "3.24.2"
val mockitoVersion = "5.8.0"
val byteBuddyVersion = "1.14.11"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "com.judoscale"
    version = "0.1.0-SNAPSHOT"

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
