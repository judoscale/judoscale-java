# Judoscale for Java

Official Java libraries for [Judoscale](https://judoscale.com), the advanced autoscaler for Heroku, AWS, and other cloud platorms.

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
    <version>0.1.0</version>
</dependency>
```

### Gradle

Add the dependency to your `build.gradle`:

```groovy
implementation 'com.judoscale:judoscale-spring-boot-starter:0.1.0'
```

## Usage

Once added to your project, the library works automatically with zero configuration when running on Heroku or Render with the Judoscale add-on installed.

The library will:

1. **Measure request queue time** — Captures the time requests spend waiting in your platform's router queue before reaching your application.
2. **Measure application time** — Captures how long your application takes to process each request.
3. **Report metrics** — Sends collected metrics to the Judoscale API every 10 seconds.

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

The API URL is automatically configured via the `JUDOSCALE_URL` environment variable, which is set by the Judoscale add-on.

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

This is a multi-module Maven project:

```
judoscale-java/
├── pom.xml                           # Parent POM
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
- Maven 3.6+

### Build

From the project root:

```sh
# Build all modules
mvn clean install

# Build without running tests
mvn clean install -DskipTests
```

### Testing

```sh
# Run all tests
mvn test

# Run tests for a specific module
mvn test -pl judoscale-core
mvn test -pl judoscale-spring-boot-starter
```

### Sample Application

See the [spring-boot-sample README](sample-apps/spring-boot-sample/README.md) for instructions on running the sample application locally.
