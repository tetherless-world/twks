TWKS Overview
=============

The Tetherless World Knowledge Store (TWKS) is a `provenance-aware <https://www.w3.org/TR/prov-o/>`_ `RDF <https://www.w3.org/RDF/>`_ store.

Its primary functions are:

- creating, reading, updating, and deleting `nanopublications <http://nanopub.org>`_
- querying aggregations of nanopublication parts, such as assertions and provenance

Features
--------

* Open source Java libraries on Maven Central for manipulating a store instance in-process with public APIs
* Java-based server that exports the public APIs via `REST <https://en.wikipedia.org/wiki/Representational_state_transfer>`_ and `SPARQL <https://www.w3.org/TR/sparql11-protocol/>`_
* Command line interface (CLI) for working with a store as a client or as a library
* Server and CLI Docker images on Dockerhub
* Python and Java clients for the server APIs
* Language-independent and Java-specific mechanisms for extending the store with e.g., `observers <https://en.wikipedia.org/wiki/Observer_pattern>`_ that react to changes in the store
* Documentation and examples of using the store from different languages

Application Programming Interfaces (APIs)
-----------------------------------------

TWKS is implemented `API-first <https://swagger.io/resources/articles/adopting-an-api-first-approach/>`_ and library-first in the Java programming language. The Java library defines the public-facing APIs as interfaces with well-defined contracts. These APIs are then exported via various means, such as an out-of-the-box server (described below) as well as a command line interface (CLI).

A primary design goal of the system is to make it possible for advanced users to build custom servers, CLIs, etc. that use TWKS as a library while also supplying a server, client implementations, a CLI, and other interfaces that simply work out of the box for more casual users.

Library APIs
^^^^^^^^^^^^

TWKS is implemented in Java, and can be used as a library from Java. See :ref:`java-lib` for documentation.

Server APIs
^^^^^^^^^^^

The out-of-the-box TWKS server (``twks-server``) implements the following APIs:

* a REST API for creating, reading, updating, and deleting `nanopublications <http://nanopub.org>`_
* a REST API for retrieving assertions from the graph
* a `SPARQL 1.1 <https://www.w3.org/TR/sparql11-protocol/>`_ endpoint

The server can be run directly on a host (see :ref:`java-server`) or via Docker (see :ref:`docker-server`).

Clients
^^^^^^^

There are several client implementations for the abovementioned server APIs:

* a Python client (see :ref:`py-client`)
* a Java client (see :ref:`java-client`)
