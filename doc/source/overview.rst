TWKS Overview
=============

The Tetherless World Knowledge Store (TWKS) is a `provenance-aware <https://www.w3.org/TR/prov-o/>`_ `RDF <https://www.w3.org/RDF/>`_ store.

Features
--------



Interfaces
----------

TWKS is implemented `API-first <https://swagger.io/resources/articles/adopting-an-api-first-approach/>`_ and library-first. The library defines the public-facing APIs in a programming language (Java). These APIs are then exported via various means, such as a "default" server (described below) as well as a command line interface (CLI).

The goal is to make it possible for advanced users to build custom servers, CLIs, etc. that use TWKS as a library while also supplying a server, client implementations, a CLI, and other interfaces that simply work out of the box for more casual users.

Library interfaces
^^^^^^^^^^^^^^^^^^

TWKS is implemented in Java, and can be used as a library from Java. See :ref:`java-lib` for documentation.

Server interfaces
^^^^^^^^^^^^^^^^^

The default TWKS server (``twks-server``) implements the following

* a REST API for creating, reading, updating, and deleting `nanopublications <http://nanopub.org>`_
* a REST API for retrieving assertions from the graph
* a `SPARQL 1.1 <https://www.w3.org/TR/sparql11-protocol/>`_ endpoint

Client interfaces
^^^^^^^^^^^^^^^^^

* a Python client for the default server APIs (see :ref:`py-client`)
* a Java client for REST and SPARQL APIs (see :ref:`java-client`)
