# Spring Boot 2 Sample Application

This sample application demonstrates the `judoscale-spring-boot-2-starter` integration with Spring Boot 2.6.7 and Java 8.

## Requirements

- Java 8
- Gradle (wrapper included)

## Running

From this directory:

```bash
./bin/dev
```

Or from the project root:

```bash
./gradlew :sample-apps:spring-boot-2-sample:bootRun
```

Then visit http://localhost:8080

## Testing Metrics

1. Visit http://localhost:8080
2. Open https://judoscale-java-sb2.requestcatcher.com in another tab
3. Make requests to the sample app
4. Watch metrics appear in the request catcher every 10 seconds

Use the `?sleep=N` parameter to simulate slow requests (e.g., `/?sleep=2` for a 2-second delay).
