# Docker image for the TWKS command line interface (CLI)

# Build image
FROM twks-java as build-java

# Deployment image
FROM openjdk:11-jre-slim-buster

COPY --from=build-java /twks/java/dist/twks-cli-current.jar /
COPY /docker/image/cli/twks-cli-stub /
RUN cat /twks-cli-stub /twks-cli-current.jar >/twks-cli && chmod +x /twks-cli && rm /twks-cli-stub /twks-cli-current.jar

ENTRYPOINT ["/twks-cli"]
