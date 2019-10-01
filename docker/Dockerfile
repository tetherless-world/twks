# Build image
FROM maven:3.6-jdk-11 as build-java
COPY /java /java
RUN cd /java && mvn -q package -Dmaven.test.skip=true

# Deployment image
FROM jetty:9.4.18-jre11

COPY --from=build-java /java/dist/twdb-server-current.war /var/lib/jetty/webapps/ROOT.war
