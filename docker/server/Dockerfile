# Build image
FROM twks-java as build-java

# Deployment image
FROM jetty:9.4.18-jre11

USER jetty
RUN mkdir /var/lib/jetty/tw-webapps
COPY --from=build-java /twks/java/dist/twks-server-current.war /var/lib/jetty/tw-webapps
COPY /docker/server/twks-server.xml /var/lib/jetty/webapps

USER root
RUN ln -s /extcp /var/lib/jetty/lib/ext

EXPOSE 8080

VOLUME ["/data", "/extcp", "/extfs"]
