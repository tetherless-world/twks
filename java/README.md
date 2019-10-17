# TWKS Java

## Prerequisites

* Java 8 or 11 JDK
* [Maven](https://maven.apache.org/)

### Dependencies

See [Maven Central](https://search.maven.org/search?q=edu.rpi.tw.twks) for available artifacts.

#### Releases

Release dependencies can be added directly to your Maven/Gradle/SBT configuration:

        <dependency>
            <groupId>edu.rpi.tw.twks</groupId>
            <artifactId>twks-api</artifactId>
            <version>1.0.0</version>
        </dependency>

#### Snapshots

Snapshots from Maven Central require enabling the OSSRH snapshot repository. In Maven `~/.m2/settings.xml`

    <profiles>
      <profile>
         <id>allow-snapshots</id>
            <activation><activeByDefault>true</activeByDefault></activation>
         <repositories>
           <repository>
             <id>snapshots-repo</id>
             <url>https://oss.sonatype.org/content/repositories/snapshots</url>
             <releases><enabled>false</enabled></releases>
             <snapshots><enabled>true</enabled></snapshots>
           </repository>
         </repositories>
       </profile>
    </profiles>

Then use a dependency like:

        <dependency>
            <groupId>edu.rpi.tw.twks</groupId>
            <artifactId>twks-api</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

## Use as a client

The preferred way to access the store is over the network, as a client of the TWKS server. This allows the server implementation to be isolated.

A Java client library is provided, [`TwksClient`](client/src/main/java/edu/rpi/tw/twks/client/TwksClient.java). It is available via the following Maven dependency:

        <dependency>
            <groupId>edu.rpi.tw.twks</groupId>
            <artifactId>twks-client</artifactId>
            <version>1.0.0</version>
        </dependency>

## Use as a library

Add a `Twks` implementation to your Maven/SBT/Gradle/etc. dependencies:
        
        <dependency>
            <groupId>edu.rpi.tw.twks</groupId>
            <artifactId>twks-tdb</artifactId>
            <version>1.0.0</version>
        </dependency>

### Development

The TWKS public-facing library API is in the class [`Twks`](api/src/main/java/edu/rpi/tw/twks/api/Twks.java).

See [`TwksTest.java`](test/src/main/java/edu/rpi/tw/twks/test/TwksTest.java) for examples of Java library API use.

Like Jena `Model` and `Dataset`, `Twks` has multiple implementations. For example, [`Tdb2Twks`](tdb/src/main/java/edu/rpi/tw/twks/tdb/Tdb2Twks.java).
You can instantiate an implementation directly, or indirectly through [`TwksFactory`](factory/src/main/java/edu/rpi/tw/twks/factory/TwksFactory.java)), which is what the server and command line interfaces do.

## Command line use

A command-line interface provides various sub-commands for manipulating TWKSs. After building, run:

    java -jar java/dist/twks-cli-current.jar --help

To see the available sub-commands and their options.
   
Note that TDB2 is a single process store, so you will not be able to access it separate library-using, command line, and/or server processes concurrently. 
