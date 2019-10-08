# Tetherless World Database (TWDB)

TWDB is a [provenance-aware](https://www.w3.org/TR/prov-o/) [RDF](https://www.w3.org/RDF/) store.

TWDB uses [nanopublications](http://nanopub.org) as its core abstraction.

The database is implemented in Java, and exposes several interfaces:
* a REST API for creating, reading, updating, and deleting nanopublications
* a [SPARQL 1.1](https://www.w3.org/TR/sparql11-protocol/) endpoint
* a Java library for programmatic use

The primary API is defined by [`Twdb.java`](java/api/src/main/java/edu/rpi/tw/twdb/api/Twdb.java).

# Using the server

See the [Docker documentation](docker/README.md) for server setup.

# Programmatic use

See the language-specific documentation:
* [Java](java/README.md)
