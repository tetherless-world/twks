# TWKS Python client and library

## Prerequisites

* [Python 3](https://www.python.org/)

## One-time setup

Install the library:

    cd py
    python3 setup.py install

Or add a dependency from PyPI: [`twks-client`](https://pypi.org/project/twks-client/).

## Client use

[`TwksClient`](twks/client/twks_client.py) is the entry point class. It is a client of the [TWKS server](../docker/README.md).

The client API mirrors the primary TWKS API in [`Twks.java`](../java/api/src/main/java/edu/rpi/tw/twks/api/Twks.java):
* CRUD operations on nanopublications
* querying assertions and nanopublications via SPARQL

See [`test_twks_client.py`](tests/twks_test/client/test_twks_client.py) for examples of using the client.

## Other examples

See the [`examples`](examples/) directory for other examples.
