# Build image
FROM twks-java as build-java

# Deployment image
FROM jetty:9.4.18-jre11

USER jetty
RUN mkdir /var/lib/jetty/tw-webapps
COPY --from=build-java /twks/java/dist/twks-server-current.war /var/lib/jetty/webapps/ROOT.war

USER root
RUN mkdir /data && mkdir /dump && mkdir /extfs
RUN chown jetty.jetty /data /dump /extfs
RUN rmdir /var/lib/jetty/lib/ext && ln -s /extcp /var/lib/jetty/lib/ext
USER jetty

# The way to pass through properties to the servlet
ENV JAVA_OPTIONS -Dtwks.extcp=/extcp -Dtwks.dump=/dump -Dtwks.extfs=/extfs -Dtwks.tdbLocation=/data

EXPOSE 8080

VOLUME ["/data", "/dump", "/extcp", "/extfs"]