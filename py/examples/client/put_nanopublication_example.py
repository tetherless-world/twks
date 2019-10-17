"""
TWKS client example that puts a nanopublication to the server
"""

import logging
import os.path
import pathlib

import rdflib

from twks.client import TwksClient
from twks.nanopub import Nanopublication

if __name__ == '__main__':
    client = TwksClient(server_base_url="http://localhost:8080")
    file_path = os.path.join(os.path.dirname(__file__), "relatives.ttl")
    nanopublication = Nanopublication.parse_assertions(
        format="ttl",
        source=file_path,
        source_uri=rdflib.URIRef(pathlib.Path(file_path).as_uri()))
    logging.info("putting nanopublication to the server")
    client.put_nanopublication(nanopublication)
