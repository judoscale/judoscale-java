# Contributing / Development

## Project Structure

This is a multi-module Gradle project:

```
judoscale-java/
├── build.gradle.kts                  # Root build file
├── settings.gradle.kts               # Module configuration
├── judoscale-core/                   # Shared core library
│   └── src/main/java/com/judoscale/core/
│       ├── ApiClient.java            # API client interface
│       ├── Metric.java               # Metric data record
│       └── MetricsStore.java         # Thread-safe metric storage
├── judoscale-spring-boot-starter/    # Spring Boot integration
│   └── src/main/java/com/judoscale/spring/
│       ├── JudoscaleAutoConfiguration.java
│       ├── JudoscaleConfig.java
│       ├── JudoscaleFilter.java
│       ├── JudoscaleApiClient.java
│       └── JudoscaleReporter.java
└── sample-apps/
    └── spring-boot-sample/           # Example application
```

- **judoscale-core** — Framework-agnostic code shared across integrations
- **judoscale-spring-boot-starter** — Spring Boot auto-configuration and servlet filter
- **sample-apps/** — Example applications for testing (not published)

## Prerequisites

- Java 21 (we use [asdf](https://asdf-vm.com/) with the java plugin)
- Gradle 8.5+ (included via wrapper)

## Build

From the project root:

```sh
# Build all modules
./gradlew build

# Build without running tests
./gradlew build -x test

# Clean and build
./gradlew clean build
```

## Testing

```sh
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :judoscale-core:test
./gradlew :judoscale-spring-boot-starter:test
```

> **Note:** Gradle caches test results. If no code has changed, tests will show as "up-to-date" and won't re-run. To force tests to run, use `./gradlew clean test`.

## Sample Application

See the [spring-boot-sample README](sample-apps/spring-boot-sample/README.md) for instructions on running the sample application locally.
