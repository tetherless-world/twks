# Docker image for the TWKS command line interface (CLI)
# Invokes the CLI with the parameters supplied in the Docker command. For example:
# docker run tetherlessworld/twks-cli put-nanopublication some_nanopublication.trig.

# Build image
FROM maven:3.6-jdk-11 as build-java
COPY /java /twks/java
RUN mv /twks/java/.m2 /root/
RUN cd /twks/java && mvn -q package -Dmaven.test.skip=true

# Deployment image
FROM openjdk:11-jre-slim-buster

COPY --from=build-java /twks/java/dist/twks-cli-current.jar /
COPY /docker/cli/docker-entrypoint.sh /

ENTRYPOINT ["/docker-entrypoint.sh"]
