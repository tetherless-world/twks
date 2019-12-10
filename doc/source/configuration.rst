Configuration
=============

The TWKS client, server, command line interface (CLI) can be configured using key-value properties from various sources:

* Environment variables: ``export twks.key=value`` on Linux or ``set twks.key=value`` on Windows. Note that the key is always prefixed by ``twks.``.
* System variables passed to the JVM: ``MAVEN_OPTS="-Dtwks.key=value``. Note that the key is always prefixed by ``twks.``.
* (CLI only) Command line options: ``java -jar twks-cli.jar -Dkey=value subcommand ...``. The key is not prefixed.
* (CLI only) The `-c` command line option for passing in a `Java-format .properties file <https://docs.oracle.com/javase/7/docs/api/java/util/Properties.html>`_: ``java -jar twks-cli.jar -c local.properties``
* (Server only) Servlet initialization parameters in a web.xml, where the key is not prefixed:

::

    <servlet>
         <display-name>TwksServlet</display-name>
         <servlet-name>TwksServlet</servlet-name>
         <init-param>
             <param-name>key</param-name>
             <param-value>value</param-value>
         </init-param>
    </servlet>

Available keys
^^^^^^^^^^^^^^

The following sections list the available keys as well as the expected types of values.

Boolean values are considered true if they are set at all. For example, setting ``-Dtwks.key`` as a JVM parameter implies ``twks.key`` is ``true``.

Client and command-line
~~~~~~~~~~~~~~~~~~~~~~~

* ``serverBaseUrl``: base URL of the TWKS server e.g., ``http://localhost:8080``

Server only
~~~~~~~~~~~

Backing store configuration: AllegroGraph

* ``agraphCatalogId`` (string): AllegroGraph catalog to use in the backing store
* ``agraphPassword`` (string): AllegroGraph server password to use in the backing store
* ``agraphRepositoryId`` (string): AllegroGraph repository to use in the backing store
* ``agraphUsername`` (string): AllegroGraph server password to use in the backing store
* ``agraphServerUrl`` (string): URL of the `AllegroGraph <https://franz.com/agraph/allegrograph/>`_ server e.g., ``http://twks-agraph:100035``. Setting this property tells the server to use AllegroGraph as its backing store.

Backing store configuration: Jena TDB

* ``tdbLocation`` (string): path to a directory where `Jena TDB2 <https://jena.apache.org/documentation/tdb2/index.html>`_ should persist its data. Settings this property tells the server to use TDB2 as its backing store.

TWKS extensions

* ``extcp`` (string): path to a directory of .jars to look for TWKS extensions in
* ``extfs`` (string): path to a directory of file-based extensions
* ``serverBaseUrl`` (string): tell file-based extensions where the server is located, since they use the TWKS client to communicate with the server

Other

* ``dump`` (string): path to a local directory where the server will dump its contents (one nanopublication per file) when the ``dump`` operation is invoked

Experimental

* ``cacheGraphNames`` (boolean): cache the names of assertion and other named graphs as an optimization, may lead to reading stale data on races
* ``enableGeoSPARQL`` (boolean): enable `Jena's GeoSPARQL extensions  <http://jena.apache.org/documentation/geosparql/>`_
