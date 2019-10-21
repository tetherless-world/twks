# Tetherless World Knowledge Store (TWKS)

TWKS is a [provenance-aware](https://www.w3.org/TR/prov-o/) [RDF](https://www.w3.org/RDF/) store.

The store is implemented in Java, and exposes several interfaces:
* a REST API for creating, reading, updating, and deleting [nanopublications](http://nanopub.org)
* a [SPARQL 1.1](https://www.w3.org/TR/sparql11-protocol/) endpoint
* a Java library for programmatic use

## Use

TWKS consists of multiple sub-projects. Each sub-project directory ([`docker/`](docker/README.md), [`java/`](java/README.md), [`py/`](py/README.md)) has a `script/` subdirectory for building and running that part of the project. The `script/` directories conform to the [Scripts to Rule Them All](https://github.com/github/scripts-to-rule-them-all) conventions.

* [Docker-based command-line interface (CLI) and server](docker/script/): the preferred way to run the server
* [Java client, command-line-interface, library, and server](java/script/): for closer integration in Java and for running the server directly on the host
* [Python client and library](py/script/): for accessing the server from Python
