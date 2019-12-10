Overview
========

The `Tetherless World Knowledge Store <http://twks.tw.rpi.edu/>`_ (TWKS, pronounced "twicks") is a `provenance-aware <https://www.w3.org/TR/prov-o/>`_ `RDF <https://www.w3.org/RDF/>`_ store.

Its primary functions are:

- creating, reading, updating, and deleting `nanopublications <http://nanopub.org>`_
- querying aggregations of nanopublication parts, such as assertions and provenance

Motivation
----------

TWKS is designed to store and retrieve *knowledge graphs*. `McCusker et al. <http://www.semantic-web-journal.net/content/what-knowledge-graph>`_ define a knowledge graph as:

    A graph, composed of a set of assertions (edges labeled with relations) that are expressed between entities (vertices), where the meaning of the graph is encoded in its structure, the relations and entities are unambiguously identified, a limited set of relations are used to label the edges, and the graph encodes the provenance, especially justification and attribution, of the assertions.

In the TWKS universe both assertions and provenance are encoded using the `Resource Description Framework (RDF) <https://www.w3.org/RDF/>`_ and persisted in an underlying `graph database <https://en.wikipedia.org/wiki/Graph_database>`_. TWKS supports related Semantic Web technologies such as the `SPARQL Query Language for RDF <https://www.w3.org/TR/sparql11-overview/>`_ for querying assertions and provenance.

TWKS is not a general graph database itself. TWKS exposes the narrowest API that can support knowledge graph applications: reading and writing nanopublications, querying assertions and provenance. Having a narrow but well-supported API places fewer constraints on the underlying implementation and leaves the door open for future optimization.

Features
--------

* Open source Java libraries on `Maven Central <https://search.maven.org/search?q=edu.rpi.tw.twks>`_ for manipulating a store instance in-process with public APIs
* Java-based server that exports the public APIs via `REST <https://en.wikipedia.org/wiki/Representational_state_transfer>`_ and `SPARQL <https://www.w3.org/TR/sparql11-protocol/>`_
* Command line interface (CLI) for working with a store as a client or as a library
* `Server <https://hub.docker.com/r/tetherlessworld/twks-server>`_ and `CLI <https://hub.docker.com/r/tetherlessworld/twks-cli>`_ Docker images on Dockerhub
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

License
-------

TWKS is distributed as open source under the `Apache 2.0 license <https://www.apache.org/licenses/LICENSE-2.0>`_.
