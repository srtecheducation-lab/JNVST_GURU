# Deployment

## Docker and Render preparation

The backend is prepared for container deployment with the repository
[Dockerfile](../Dockerfile). It uses a multi-stage build: the first stage uses
the existing Maven Wrapper and project configuration to create the Spring Boot
executable JAR, and the final stage contains only the Java 25 runtime and that
JAR.

The [.dockerignore](../.dockerignore) excludes source-control metadata,
development tooling, generated build output, logs, documentation, and local
configuration from the Docker build context. It intentionally does not exclude
the Maven Wrapper, `pom.xml`, or `src/`, because those are required to build the
application.

## Port configuration

`server.port` is configured as `${PORT:8080}`. Render can therefore provide its
runtime `PORT` environment variable, while local development continues to use
port `8080` when `PORT` is not set. The Dockerfile does not provide a production
port value.

## Production configuration and secrets

Production configuration is supplied through Render Environment Variables; no
secret values are stored in the Dockerfile, application source, or
documentation. The production database remains the existing Supabase
PostgreSQL database, accessed through the Supabase Session Pooler connection
approach.

`local.properties` and `local/` are local-development-only. They are ignored by
Git and excluded from the Docker build context. The production container does
not depend on either one.

The existing Supabase JWT resource-server configuration and application
architecture are unchanged.
