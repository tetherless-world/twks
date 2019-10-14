FROM maven:3.6-jdk-11 as build-java

COPY /java /twks/java
RUN mv /twks/java/.m2 /root/
WORKDIR /twks/java
mvn package -Dmaven.test.skip=true
