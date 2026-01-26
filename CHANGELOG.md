# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.1](https://github.com/judoscale/judoscale-java/compare/judoscale-java-v0.1.0...judoscale-java-v0.1.1) (2026-01-26)


### Features

* initial release ([c010c90](https://github.com/judoscale/judoscale-java/commit/c010c9050d12a5a3209886630325b49f87f0ba9f))

## [Unreleased]

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

[Unreleased]: https://github.com/judoscale/judoscale-java/compare/v0.1.0...HEAD
