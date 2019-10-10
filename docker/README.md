# TWKS Docker-based server

## Prerequisites

1. [Docker](https://docs.docker.com/v17.12/install/)
1. [Docker Compose](https://docs.docker.com/compose/install/)

## Running

    cd docker
    script/server

The Docker container exposes the server's REST and SPARQL APIs via HTTP on port 8080, which is bound to localhost.

The server stores its data in a volume, `twks-data`.
