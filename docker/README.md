# TWDB Docker

## Prerequisites

1. [Install Docker](https://docs.docker.com/v17.12/install/)
1. [Install Docker Compose](https://docs.docker.com/compose/install/)

## Building

    cd docker
    docker-compose build

## Running

    cd docker
    docker-compose up

The Docker container exposes the server's REST and SPARQL APIs via HTTP on port 8080, which is bound to localhost.

The server stores its data in a volume, `twdb-data`.
