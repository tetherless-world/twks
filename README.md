# Tetherless World Knowledge Store (TWKS)

TWKS is a [provenance-aware](https://www.w3.org/TR/prov-o/) [RDF](https://www.w3.org/RDF/) store.

The store is implemented in Java, and exposes several interfaces:
* a REST API for creating, reading, updating, and deleting [nanopublications](http://nanopub.org)
* a [SPARQL 1.1](https://www.w3.org/TR/sparql11-protocol/) endpoint
* a Java library for programmatic use

The primary API is defined by [`Twks.java`](java/core/src/main/java/edu/rpi/tw/twks/core/Twks.java).

## Use

### Start here: scripts

[`script/`](script/README.md) contains scripts for building and running parts of the project.

### Server use

See the [Docker documentation](docker/README.md) for server setup.

### Programmatic use

See the language-specific documentation:
* [Java](java/README.md)
* [Python](py/README.md)
