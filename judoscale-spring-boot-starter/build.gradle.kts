plugins {
    `java-library`
}

description = "Autoscaling for Spring Boot applications on Heroku and Render"

val springBootVersion = "3.2.2"
val jacksonVersion = "2.16.1"
val slf4jVersion = "2.0.11"
val junitVersion = "5.10.1"
val assertjVersion = "3.24.2"
val mockitoVersion = "5.8.0"
val byteBuddyVersion = "1.14.11"

dependencies {
    // Judoscale Core
    api(project(":judoscale-core"))

    // Spring Boot Web (provided - the app will have this)
    compileOnly("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")

    // Spring Boot Auto-configuration
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")

    // For @ConfigurationProperties
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")

    // JSON processing (for API client)
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    // Logging
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("net.bytebuddy:byte-buddy:$byteBuddyVersion")
}
