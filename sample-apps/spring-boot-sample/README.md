# Sample app for judoscale-spring-boot-starter

This is a minimal Spring Boot app to test the judoscale-spring-boot-starter library.

## Prerequisites

- Java 21 (we recommend using [asdf](https://asdf-vm.com/) with the java plugin)
- [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli)

## Set up the app

If using asdf, run `asdf install` to install the correct Java version.

## Run the app

Run `./mvnw spring-boot:run` to run the app in development mode.

To test with the `X-Request-Start` header (for queue time reporting), you can use the [judoscale-adapter-proxy-server](https://github.com/judoscale/judoscale-adapter-proxy-server) in front of the app.

## How to use this sample app

Open https://judoscale-ruby.requestcatcher.com in a browser. The sample app can be configured to use this endpoint as a mock for the Judoscale Adapter API. This page will monitor all API requests sent from the adapter.

Run the app. Access http://localhost:8080 and continue to reload it to collect and report more request metrics.

## Deploy this app to Heroku

From this directory, run the following to create a new git repo and push it to Heroku:

```sh
git init
git add .
git commit -m "prep for Heroku"
heroku create
git push heroku main
```

To install Judoscale:

```sh
heroku addons:create judoscale
```
