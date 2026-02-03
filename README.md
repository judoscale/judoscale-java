# Judoscale for Java

[![Maven Central](https://img.shields.io/maven-central/v/com.judoscale/judoscale-spring-boot-starter)](https://central.sonatype.com/artifact/com.judoscale/judoscale-spring-boot-starter)
[![CI](https://github.com/judoscale/judoscale-java/actions/workflows/ci.yml/badge.svg)](https://github.com/judoscale/judoscale-java/actions/workflows/ci.yml)

Official Java libraries for [Judoscale](https://judoscale.com), the advanced autoscaler for Heroku, AWS, and other cloud platforms.

Judoscale automatically scales your application based on request queue time and background job queue depth, keeping your users happy and your hosting costs low.

**Supported Frameworks**

- **Spring Boot 3.x** — `judoscale-spring-boot-starter`
- **Spring Boot 2.x** — `judoscale-spring-boot-2-starter`

## judoscale-spring-boot-starter

For Spring Boot 3.x applications.

### Requirements

- Java 21 or later
- Spring Boot 3.2 or later

### Installation

#### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.judoscale</groupId>
    <artifactId>judoscale-spring-boot-starter</artifactId>
    <version>0.1.4</version>
</dependency>
```

#### Gradle

Add the dependency to your `build.gradle`:

```groovy
implementation 'com.judoscale:judoscale-spring-boot-starter:0.1.4'
```

## judoscale-spring-boot-2-starter

For Spring Boot 2.x applications (legacy support).

### Requirements

- Java 8 or later
- Spring Boot 2.6 or later (2.x series)

### Installation

#### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.judoscale</groupId>
    <artifactId>judoscale-spring-boot-2-starter</artifactId>
    <version>0.1.4</version>
</dependency>
```

#### Gradle

Add the dependency to your `build.gradle`:

```groovy
implementation 'com.judoscale:judoscale-spring-boot-2-starter:0.1.4'
```

### Usage

#### Getting Started

1. **Sign up** at [judoscale.com](https://judoscale.com) and connect your cloud provider (Heroku, AWS, or other supported platforms), or install the Heroku Judoscale add-on.
2. **Install the library** using the instructions above
3. **Set the `JUDOSCALE_URL` environment variable** — You'll find your unique API URL in your Judoscale dashboard. \_This is set automatically when using the Heroku add-on.

Once configured, the library works automatically. It will:

1. **Measure request queue time** — Captures the time requests spend waiting in your platform's router queue before reaching your application.
2. **Measure web utilization** — Captures how of often your web servers are busy handling requests.
3. **Report metrics** — Sends collected metrics to the Judoscale API every 10 seconds.

> **Note:** The library is automatically disabled in development or any environment where `JUDOSCALE_URL` is not set. It's safe to include in your project without affecting local development.

#### Configuration

The library can be configured via `application.properties` or environment variables:

```properties
# Judoscale is enabled by default. Set to false to disable.
judoscale.enabled=true
```

The API URL is configured via the `JUDOSCALE_URL` environment variable. You can find your unique API URL in your Judoscale dashboard.

To enable debug logging for the Judoscale starter, use the `LOGGING_LEVEL_COM_JUDOSCALE` environment variable:

```env
LOGGING_LEVEL_COM_JUDOSCALE=DEBUG
```

#### Accessing Queue Time

Judoscale captures queue time for each request. You can access this metric for your own logging or analysis via a request attribute:

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
