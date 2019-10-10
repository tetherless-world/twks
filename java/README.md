# TWKS Java

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
            <artifactId>twks-core</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

### Development

The TWKS Java library is in the module `twks-core`. The public-facing API is in the class [`Twks`](core/src/main/java/edu/rpi/tw/twks/core/Twks.java).

You instantiate a `Twks` using `TwksFactory`. The factory takes a `TwksConfiguration`, which specifies the backing store and other options. The default configuration (`new TwksConfiguration`) is an in-memory [TDB2](https://jena.apache.org/documentation/tdb2/) store. You can currently (20191007) configure TDB2 persistence to disk with `TwksConfiguration`. Other implementations of the `Twks` interface will follow.

See the `Twks` class Javadoc and `TwksTest.java` for examples of Java API use.

## Command line use

A command-line interface provides various sub-commands for manipulating TWKSs. After building, run:

    java -jar java/dist/twks-cli-current.jar --help

To see the available sub-commands and their options.
   
Note that TDB2 is a single process store, so you will not be able to access it separate library-using, command line, and/or server processes concurrently. 
