# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

TWKS is beta-quality software. It does not (yet) adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html). Maintenance versions may introduce breaking changes.

## [Unreleased]

## [1.0.4] - 2020-05-06

### Docker

#### Added
- Copied the `twks-cli` into the server image
- `docker/script/pull` script to pull stable images

#### Changed
- `docker/script/cli`: add --rm to run to remove the container after it exits

#### Fixed
- Change the runtime compose files to use a single network, `twks`, so that the command line can communicate with the server
- Documentation of Docker CLI usage
- Documentation explaining how to build and pull Docker images

### Java

#### Added
- Instrumentation using the Dropwizard Metrics library
- `Tdb2Twks`: allow explicitly specifying `mem` in the `tdbLocation` configuration variable
- Post nanopublications on server startup; specify with the configuration variables `initialNanopublicationFilePaths` and/or `initialNanopublicationsDirectoryPath`
- `WatchNanopublicationsCommand`: retry file parsing after n seconds, to accomodate file system write races
- New `QueryCommand`: query the store from the command line
- Renamed `InProcessTwksClient` to `DirectTwksClient` and move it from the CLI to its own library, `direct-twks-client`
- Switched the CLI to use logback
- Explicit nanopublication dialect strategies
- New `isEmpty` API method

#### Changed
- `PostNanopublicationsCommand`: optimizations for loading directories with many nanopublications
- Optimization of nanopublication parsing
- `NanopublicationParser`: stream nanopublications to a consumer (sink) rather than always returning a collection; re-implemented the latter in terms of the former
- Converted uses of `java.io.File` to `java.nio.Path`
- Upgrade Jena to 3.14.0
- Optimized querying and retrieving assertion graphs by keeping redundant copies of assertion triples. This change was transparent to clients.
- Reject blank graph names

#### Fixed
- Suppress Jetty `AnnotationParser` warnings in Docker image
- Suppress Jersey warnings
- `WatchNanopublicationsCommand`: ignore change event types and acts solely on observed changes to the file system
- SPARQL servlets: fixed bug parsing Content-Type's with parameters
- SPARQL servlets: fixed POST of query parameters
- Handle the case where an assertion graph is empty
- Reworked Whyis nanopublication parsing to rewrite parts of a nanopublication to conform to the specification

#### Removed
- Removed nanopublication dialect default languages (e.g., `TRIG` for `SPECIFICATION` language). The system now relies on Jena's machinery for guessing RDF languages from metadata (file extensions, Content-Type, etc.).
- Removed `NanopublicationParser.SPECIFICATION` singleton
- Renamed `twks-client` library to `twks-rest-client`, renamed `TwksClient` to `RestTwksClient` in that library, and moved `TwksClient` interface to `twks-api`

### Python

#### Added
- Add `sio:isAbout` to new nanopublication publication info if the assertions have an `rdf:type owl:Ontology`
- Exposed `getOntologyAssertions` API method in the Python client


## [1.0.3] - 2019-12-10

### Docker

#### Added
- `twks-agraph` `docker-compose.yml` for AllegroGraph implementation
- `docker/script/cli` with its own `docker-compose.yml`

#### Changed
- Reorganized `docker/compose` directory

### Java

#### Added
- Get the TWKS version from the `Twks` instance
- Command line interface: accept `-Dkey=value` and `-Dtwks.key=value` for configuration
- Split `MemTwks` implementation into its own library
- Enable Jena GeoSPARQL and a GeoSPARQL memory index on the TWKS store with the `enableGeoSPARQL` configuration key (no value) and optional `GeoSPARQLGeometryIndexSize` configuration key.
- OpenAPI annotations
- Separate `twks-servlet` library to facilitate servlet reuse
- New `InProcessTwksClient`, adapter from `TwksClient` to `Twks`, used by the command line for performance reasons
- New `WatchNanopublicationsCommand`
- AllegroGraph `Twks` implementation

#### Changed
- Split nanopublication and assertion query APIs
- Use `commons-configuration` for configuration to support different configuration mechanisms 
- Separate graph name management (+ caching) from `Twks` implementation
- Re-implemented SPARQL as Jersey resources


## [1.0.2] - 2019-10-31

### Documentation

#### Added
- Initial documentation using Sphinx
- Upload documentation to readthedocs.io

### Java

#### Added
- Initial release to Maven Central
- New `DeleteNanopublicationsCommand`
- New `TwksClient` Java client
- New `getOntologyAssertions` API method for nanopublications that are `sio:isAbout` an `owl:Ontology`
- New atch `postNanopublications` API method
- New batch `deleteNanopublications` API method
- New `dump` API method to dump the contents of the store to a (server-)local directory
- New configuration system (`TwksConfiguration`)
- New extension system for extensions that react to store changes (observer pattern): invoke observers from the Java CLASSPATH (`extcp`) or on the file system (`extfs`)
- Example project for `extcp` (classpath extensions)
- Example project for the `TwksClient`
- Enable `jetty-maven-plugin`
- Factory for different `Twks` implementations
- SPARQL servlets
- Jersey CRUD servlets for nanopublications and assertions

#### Changed
- Renamed `PutNanopublicationsCommand` to `PostNanopublicationsCommand`, kept old aliases 
- Renamed project from Twdb to Twks

### Python

#### Added
- Initial release to PyPI
- Support new server API methods in the client
- Example extending the TWKS Java server with Python scripts in the file system (`extfs`)
