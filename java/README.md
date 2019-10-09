# TWKS Java

## Prerequisites

* [Install tw-nanopub](https://github.com/tetherless-world/tw-nanopub)

## One-time setup

Build the Java source:

    cd java
    mvn package -Dmaven.test.skip=true

## Use as a library

### Installation

Install the library to your local Maven repository:

    cd java
    mvn install -Dmaven.test.skip=true
    
Add the library to your Maven/SBT/Gradle/etc. dependencies:
        
        <dependency>
            <groupId>edu.rpi.tw.twks</groupId>
            <artifactId>twks-lib</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

### Development

The TWKS Java library consists of two modules, `twks-api` and `twks-lib`. The former contains the public-facing API (the class `Twdb`), while the latter contains implementations of the API.

You instantiate an instance of the store using `TwdbFactory` from `twks-lib`. The factory takes a `TwdbConfiguration`, which specifies the backing store and other options. The default configuration (`new TwdbConfiguration`) is an in-memory [TDB2](https://jena.apache.org/documentation/tdb2/) store. You can currently (20191007) configure TDB2 persistence to disk with `TwdbConfiguration`. Other implementations of the `Twdb` interface will follow.

See the `Twdb` class Javadoc and `TwdbTest.java` for examples of Java API use.

## Command line use

A command-line interface provides various sub-commands for manipulating TWKSs. After building, run:

    java -jar java/dist/twks-cli-current.jar --help

To see the available sub-commands and their options.
   
Note that TDB2 is a single process store, so you will not be able to access it separate library-using, command line, and/or server processes concurrently. 
