# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.3](https://github.com/judoscale/judoscale-java/compare/judoscale-java-v0.1.2...judoscale-java-v0.1.3) (2026-01-29)


### Features

* Add legacy support for Spring Boot 2.x / Java 8 via a new judoscale-spring-boot-2-starter ([5d83e8b](https://github.com/judoscale/judoscale-java/commit/5d83e8bc9b124302c5a9d10c73e923e8d6ff2a16))


### Bug Fixes

* Minor bugfixes & cleanup ([757a4c8](https://github.com/judoscale/judoscale-java/commit/757a4c82e1cba269b49f039884369798ad24c512))

## [0.1.2](https://github.com/judoscale/judoscale-java/compare/judoscale-java-v0.1.1...judoscale-java-v0.1.2) (2026-01-26)


### Bug Fixes

* testing release workflow ([74ef1c1](https://github.com/judoscale/judoscale-java/commit/74ef1c196a96d0048e922af8079a2053925e2803))

## [0.1.1](https://github.com/judoscale/judoscale-java/compare/judoscale-java-v0.1.0...judoscale-java-v0.1.1) (2026-01-26)


### Features

* initial release ([c010c90](https://github.com/judoscale/judoscale-java/commit/c010c9050d12a5a3209886630325b49f87f0ba9f))


### Bug Fixes

* testing release workflow ([860e2b2](https://github.com/judoscale/judoscale-java/commit/860e2b2ef95b91a7639b29920d964b183ad1074d))

## [0.1.0](https://github.com/judoscale/judoscale-java/releases/tag/judoscale-java-v0.1.0) (2026-01-26)

### Added
- Initial release of `judoscale-core` library
- Initial release of `judoscale-spring-boot-starter`
- Request queue time measurement from `X-Request-Start` header
- Application time measurement for each request
- Utilization tracking (percentage of time processing requests)
- Automatic metric reporting to Judoscale API
- Support for Heroku, Render, and NGINX timestamp formats
- Configuration via `application.properties` or environment variables
- Sample Spring Boot application

### Configuration Options
- `judoscale.enabled` - Enable/disable the library (default: true)
- `judoscale.report-interval-seconds` - Reporting interval (default: 10)
- `judoscale.max-request-size-bytes` - Max size before ignoring queue time (default: 100000)
- `judoscale.ignore-large-requests` - Whether to ignore large requests (default: true)
