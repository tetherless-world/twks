# Build image
FROM maven:3.6-jdk-11 as build-java
RUN git clone https://github.com/tetherless-world/tw-nanopub.git /tw-nanopub && cd /tw-nanopub/java && mvn -q package install -Dmaven.test.skip=true
COPY /java /twdb/java
RUN cd /twdb/java && mvn -q package -Dmaven.test.skip=true

# Deployment image
FROM jetty:9.4.18-jre11

USER jetty
RUN mkdir /var/lib/jetty/tw-webapps
COPY --from=build-java /twdb/java/dist/twdb-server-current.war /var/lib/jetty/tw-webapps
COPY /docker/twdb-server.xml /var/lib/jetty/webapps

USER root
RUN mkdir /data
RUN chown jetty.jetty /data

USER jetty
