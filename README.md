# Judoscale for Java

[![Maven Central](https://img.shields.io/maven-central/v/com.judoscale/judoscale-spring-boot-starter)](https://central.sonatype.com/artifact/com.judoscale/judoscale-spring-boot-starter)
[![CI](https://github.com/judoscale/judoscale-java/actions/workflows/ci.yml/badge.svg)](https://github.com/judoscale/judoscale-java/actions/workflows/ci.yml)

Official Java libraries for [Judoscale](https://judoscale.com), the advanced autoscaler for Heroku, AWS, and other cloud platforms.

Judoscale automatically scales your application based on request queue time and background job queue depth, keeping your users happy and your hosting costs low.

## Supported Frameworks

- **Spring Boot** — `judoscale-spring-boot-starter`

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.judoscale</groupId>
    <artifactId>judoscale-spring-boot-starter</artifactId>
    <version>0.1.1</version>
</dependency>
```

### Gradle

Add the dependency to your `build.gradle`:

```groovy
implementation 'com.judoscale:judoscale-spring-boot-starter:0.1.1'
```

## Usage

### Getting Started

1. **Sign up** at [judoscale.com](https://judoscale.com) and connect your cloud provider (Heroku, Render, AWS, etc.)
2. **Install the library** using the instructions above
3. **Set the `JUDOSCALE_URL` environment variable** — You'll find your unique API URL in your Judoscale dashboard

Once configured, the library works automatically. It will:

1. **Measure request queue time** — Captures the time requests spend waiting in your platform's router queue before reaching your application.
2. **Measure application time** — Captures how long your application takes to process each request.
3. **Report metrics** — Sends collected metrics to the Judoscale API every 10 seconds.

> **Note:** The library has no impact in development or any environment where `JUDOSCALE_URL` is not set. It's safe to include in your project without affecting local development.

### Configuration

The library can be configured via `application.properties` or environment variables:

```properties
# Judoscale is enabled by default. Set to false to disable.
judoscale.enabled=true

# How often to report metrics (in seconds). Default: 10
judoscale.report-interval-seconds=10

# Maximum request body size before ignoring queue time (bytes). Default: 100000
# Large uploads can skew queue time measurements.
judoscale.max-request-size-bytes=100000

# Whether to ignore queue time for large requests. Default: true
judoscale.ignore-large-requests=true
```

The API URL is configured via the `JUDOSCALE_URL` environment variable. You can find your unique API URL in your Judoscale dashboard.

### Accessing Queue Time

You can access the measured queue time in your application via a request attribute:

```java
@GetMapping("/example")
public String example(HttpServletRequest request) {
    Long queueTime = (Long) request.getAttribute("judoscale.queue_time");
    if (queueTime != null) {
        logger.info("Request waited {}ms in queue", queueTime);
    }
    return "Hello!";
}
```

## Requirements

- Java 21 or later
- Spring Boot 3.2 or later

---

## Development

### Project Structure

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

### Prerequisites

- Java 21 (we use [asdf](https://asdf-vm.com/) with the java plugin)
- Gradle 8.5+ (included via wrapper)

### Build

From the project root:

```sh
# Build all modules
./gradlew build

# Build without running tests
./gradlew build -x test

# Clean and build
./gradlew clean build
```

### Testing

```sh
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :judoscale-core:test
./gradlew :judoscale-spring-boot-starter:test
```

> **Note:** Gradle caches test results. If no code has changed, tests will show as "up-to-date" and won't re-run. To force tests to run, use `./gradlew clean test`.

### Sample Application

See the [spring-boot-sample README](sample-apps/spring-boot-sample/README.md) for instructions on running the sample application locally.
