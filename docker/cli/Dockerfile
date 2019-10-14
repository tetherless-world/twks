# Docker image for the TWKS command line interface (CLI)
# Invokes the CLI with the parameters supplied in the Docker command. For example:
# docker run tetherlessworld/twks-cli put-nanopublication some_nanopublication.trig.

# Build image
FROM twks-java as build-java

# Deployment image
FROM openjdk:11-jre-slim-buster

COPY --from=build-java /twks/java/dist/twks-cli-current.jar /
COPY /docker/cli/docker-entrypoint.sh /

ENTRYPOINT ["/docker-entrypoint.sh"]
