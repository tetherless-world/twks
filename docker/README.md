# TWKS Docker-based server

## Prerequisites

1. [Docker](https://docs.docker.com/v17.12/install/)
1. [Docker Compose](https://docs.docker.com/compose/install/)

## Running the server

    cd docker
    script/server

The Docker container exposes the server's REST and SPARQL APIs via HTTP on port 8080, which is bound to localhost.

The server stores its data in a volume, `twks-data`.

## Running the command line interface as a client

Run the server as above, then:

    cd docker
    cat nanopublication.trig | docker-compose run -T twks-cli put-nanopublications --lang trig -

Explanation:
* `docker-compose run twks-cli` runs the TWKS command line interface container and connects it to the server.
* `put-nanopublication` is the CLI sub-command
* `--lang trig` specifies that the input will be in Trig format
* `put-nanopublication` reads from stdin since `-` was specified (a `-f file` would be expected to be in the container; it is easier to use stdin)
* `cat nanopublication.trig` writes the contents of that file to the container's stdin
