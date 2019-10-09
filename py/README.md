# TWKS Python client library

## One-time setup

Install the library:

    cd py
    python3 setup.py install

## Use

[`TwksClient`](twks/client/twks_client.py) is the entry point class. It is a client of the [TWKS server](../docker/README.md).

The client API mirrors the primary TWKS API in [`Twks.java`](../java/lib/src/main/java/edu/rpi/tw/twks/lib/Twks.java):
* CRUD operations on nanopublications
* querying assertions and nanopublications via SPARQL

See [`test_twks_client.py`](tests/test_twks_client.py) for examples of using the client.
