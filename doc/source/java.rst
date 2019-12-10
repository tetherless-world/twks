.. _java:

Use from Java
=============

Java prerequisites
------------------

* Java 8 or 11 JDK
* `Maven <https://maven.apache.org/>`_

Dependencies
^^^^^^^^^^^^

See `Maven Central <https://search.maven.org/search?q=edu.rpi.tw.twks>`_ for available artifacts.

The public-facing Java modules are:


* ``twks-agraph``\ : `AllegroGraph <https://franz.com/agraph/allegrograph/>`_ implementation of the ``Twks`` API
* ``twks-api``\ : public-facing Java library API, including the store library API ``Twks``
* ``twks-client``\ : public-facing Java client API
* ``twks-nanopub``\ : library for working with nanopublications, independently of TWKS
* ``twks-tdb``: `Jena TDB2 <https://jena.apache.org/documentation/tdb2/>`_ implementation of the ``Twks`` API
* ``twks-uri``\ : tiny type for URIs
* ``twks-vocabulary``\ : RDF vocabulary singletons, similar to ``org.apache.jena.vocabulary``

The above modules are supported by various internal modules, such as the ``twks-abc`` module of abstract base classes for ``Twks`` implementations. API users should not need these.

Releases
~~~~~~~~

Release dependencies can be added directly to your Maven/Gradle/SBT configuration:

.. code-block::

       <dependency>
           <groupId>edu.rpi.tw.twks</groupId>
           <artifactId>twks-api</artifactId>
           <version>1.0.2</version>
       </dependency>


Snapshots
~~~~~~~~~

Snapshots from Maven Central require enabling the OSSRH snapshot repository.

In Maven ``~/.m2/settings.xml``

.. code-block::

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


Or in ``build.sbt``\ :

.. code-block::

   resolvers += Resolver.sonatypeRepo("snapshots")

Then use a dependency as:

.. code-block::

       <dependency>
           <groupId>edu.rpi.tw.twks</groupId>
           <artifactId>twks-api</artifactId>
           <version>1.0.4-SNAPSHOT</version>
       </dependency>


.. _java-examples:

Java examples
-------------

The repository contains a `number of examples of using TWKS from Java <https://github.com/tetherless-world/twks/tree/master/java/examples>`_.


.. _java-client:

Using the Java client
---------------------

The easiest way to access the store is over the network, as a client of the TWKS server. This allows the server implementation to be isolated.

A Java client library is provided, `TwksClient <https://github.com/tetherless-world/twks/blob/master/java/client/src/main/java/edu/rpi/tw/twks/client/TwksClient.java>`_. It is available via the following Maven dependency:

.. code-block::

       <dependency>
           <groupId>edu.rpi.tw.twks</groupId>
           <artifactId>twks-client</artifactId>
           <version>1.0.0</version>
       </dependency>


See the :ref:`java-examples` for an example of Java client use.


.. _java-lib:

Using the store as a library from Java
--------------------------------------

Add a ``Twks`` implementation to your Maven/SBT/Gradle/etc. dependencies:

.. code-block::

       <dependency>
           <groupId>edu.rpi.tw.twks</groupId>
           <artifactId>twks-tdb</artifactId>
           <version>1.0.0</version>
       </dependency>

Java nanopublication library
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This library is an implementation of the current `Nanopublication Guidelines <http://nanopub.org/guidelines/working_draft/>`_. It can be used independently of TWKS.

The `Nanopublication <https://github.com/tetherless-world/twks/blob/master/java/nanopub/src/main/java/edu/rpi/tw/twks/nanopub/Nanopublication.java>`_ class is the primary abstraction.
You can parse nanopublications or loose assertion graphs with the `NanopublicationParser <https://github.com/tetherless-world/twks/blob/master/java/nanopub/src/main/java/edu/rpi/tw/twks/nanopub/NanopublicationParser.java>`_ class or build them from parts (named graphs) using the `NanopublicationFactory <https://github.com/tetherless-world/twks/blob/master/java/nanopub/src/main/java/edu/rpi/tw/twks/nanopub/NanopublicationFactory.java>`_ class.


Java library development
^^^^^^^^^^^^^^^^^^^^^^^^

The TWKS public-facing library API is in the class `Twks <https://github.com/tetherless-world/twks/blob/master/java/api/src/main/java/edu/rpi/tw/twks/api/Twks.java>`_.

Like Jena ``Model`` and ``Dataset``\ , ``Twks`` has multiple implementations. For example, `Tdb2Twks <https://github.com/tetherless-world/twks/blob/master/java/tdb/src/main/java/edu/rpi/tw/twks/tdb/Tdb2Twks.java>`__.
You can instantiate an implementation directly, or indirectly through `TwksFactory <https://github.com/tetherless-world/twks/blob/master/java/factory/src/main/java/edu/rpi/tw/twks/factory/TwksFactory.java>`_), which is what the server and command line interfaces do.


.. _java-cli:

Java command line use
---------------------

A command-line interface provides various sub-commands for manipulating TWKSs. After building, run:

.. code-block::

   java -jar java/dist/twks-cli-current.jar --help


To see the available sub-commands and their options.

Note that TDB2 is a single process store, so you will not be able to access it from separate library-using, command line, and/or server processes concurrently.


.. _java-server:

Running the TWKS server directly on the host
--------------------------------------------

You can run the server directly on your host machine in one of two ways:

Using your own servlet container
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

After building the project with ``mvn package``\ , copy the ``java/dist/twks-server-current.war`` to your servlet container's ``webapps`` directory e.g., ``/var/lib/jetty/webapps``.

You may want to rename the ``.war`` to ``ROOT.war`` to mount at the root context.

Running the server from Maven
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The server can be started directly using Maven:

.. code-block::

   cd java
   mvn jetty:run


See the `jetty-maven-plugin documentation <https://www.eclipse.org/jetty/documentation/9.4.x/jetty-maven-plugin.html>`_ for ``-D`` configuration options to control the port.

Various server options that require interaction with the host are disabled by default. You can use ``-D`` with properties to enable them. For example, to enable ``extfs`` against a directory:

.. code-block::

   cd java
   mvn jetty:run -Dtwks.extfs=$PWD/../py/examples/extfs/


Or persist to disk with the TDB implementation of the store:

.. code-block::

   cd java
   mvn jetty:run -Dtwks.tdbLocation=$PWD/../data

