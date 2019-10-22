FROM maven:3.6-jdk-11 as build-java

COPY /java /twks/java
WORKDIR /twks/java
RUN mv .m2 /root/ || true
RUN mvn -q package -Dmaven.test.skip=true
