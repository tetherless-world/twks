# Build image
FROM twks-java as build-java

# Deployment image
FROM jetty:9.4.27-jre11

# Copy in the server .war
USER jetty
RUN mkdir /var/lib/jetty/tw-webapps
COPY --from=build-java /twks/java/dist/twks-server-current.war /var/lib/jetty/webapps/ROOT.war

USER root
# Copy in twks-cli
COPY --from=build-java /twks/java/dist/twks-cli-current.jar /
COPY /docker/image/cli/twks-cli-stub /
RUN cat /twks-cli-stub /twks-cli-current.jar >/twks-cli && chmod +x /twks-cli && rm /twks-cli-stub /twks-cli-current.jar
RUN chown jetty.jetty /twks-cli
# Create the /dump directory
RUN mkdir /data && mkdir /dump && mkdir /extfs
RUN chown jetty.jetty /data /dump /extfs
RUN rmdir /var/lib/jetty/lib/ext && ln -s /extcp /var/lib/jetty/lib/ext
USER jetty

# The way to pass through properties to the servlet
ENV JAVA_OPTIONS -Dorg.eclipse.jetty.annotations.AnnotationParser.LEVEL=OFF -Dtwks.extcp=/extcp -Dtwks.dump=/dump -Dtwks.extfs=/extfs -Dtwks.tdbLocation=/data

EXPOSE 8080

VOLUME ["/data", "/dump", "/extcp", "/extfs"]
