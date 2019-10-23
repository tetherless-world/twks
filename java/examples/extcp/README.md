# TWKS Java classpath extension

This example demonstrates the use of the TWKS extension system.

It uses [Java's service-provider loading facility](https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html) to register an extension from a .jar file on the TWKS server classpath.

## Prerequisites

* A Java Development Kit
* Maven
* Installed TWKS dependencies

See the [parent Java documentation](../..) for the above.

## Building

    cd java/examples/extcp
    mvn package

## Running

    cd java
    mvn package jetty:run -DskipTests -Dtwks.extcp=$PWD/java/examples/extcp/target/twks-examples-extcp-1.0.0-SNAPSHOT.jar

Then perform an operation using a client:

    cd java
    java -jar java/dist/twks-cli-current.jar put-nanopublications test/src/main/resources/edu/rpi/tw/twks/test/spec_nanopublication.trig
