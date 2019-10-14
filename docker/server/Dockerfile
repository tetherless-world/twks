# Build image
FROM maven:3.6-jdk-11 as build-java
COPY /java /twks/java
RUN mv /twks/java/.m2 /root/
RUN cd /twks/java && mvn -q package -Dmaven.test.skip=true

# Deployment image
FROM jetty:9.4.18-jre11

USER jetty
RUN mkdir /var/lib/jetty/tw-webapps
COPY --from=build-java /twks/java/dist/twks-server-current.war /var/lib/jetty/tw-webapps
COPY /docker/server/twks-server.xml /var/lib/jetty/webapps

USER root
RUN mkdir /data
RUN chown jetty.jetty /data

USER jetty
