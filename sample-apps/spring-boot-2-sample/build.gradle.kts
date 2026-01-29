plugins {
    java
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
}

group = "com.judoscale"
version = "0.0.1-SNAPSHOT"

description = "Sample app for testing judoscale-spring-boot-2-starter"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // Judoscale Spring Boot 2 Starter
    implementation(project(":judoscale-spring-boot-2-starter"))

    // Development tools (auto-restart on file changes)
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
