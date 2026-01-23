# Sample app for judoscale-spring-boot-starter

This is a minimal Spring Boot app to test the judoscale-spring-boot-starter library.

## Prerequisites

- Java 21 (we use [asdf](https://asdf-vm.com/) with the java plugin)
- Node.js (for the proxy server)
- [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli)

## Set up the app

If using asdf, run `asdf install` to install the correct Java version.

## Run the app (with queue time simulation)

Run `./bin/dev` to run the app in development mode. This will...

- Use `heroku local` and a `Procfile` to start the following processes:
  - A [tiny proxy server](https://github.com/judoscale/judoscale-adapter-proxy-server) that adds the `X-Request-Start` request header so we can test request queue time reporting.
  - The Spring Boot server.

## How to use this sample app

1. Open https://judoscale-java.requestcatcher.com in a browser. The sample app is configured to use this endpoint as a mock for the Judoscale Adapter API. This page will monitor all API requests sent from the adapter.

2. Run the app with `./bin/dev`

3. Access http://localhost:7980 (the proxy port) and continue to reload it to collect and report more request metrics.

4. Watch the request catcher page - you should see POST requests to `/api/v3/reports` every 10 seconds with the collected metrics.

## Run without proxy

If you just want to run the app directly (without queue time simulation):

```sh
./mvnw spring-boot:run
```

Then access http://localhost:8080 directly.

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
