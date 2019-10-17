"""
TWKS client example that puts a nanopublication to the server
"""

import os.path

from twks.client import TwksClient
from twks.nanopub import Nanopublication

if __name__ == '__main__':
    client = TwksClient(server_base_url="http://localhost:8080")
    nanopublication = Nanopublication.parse(source=os.path.join(os.path.dirname(__file__), "spec_nanopublication.trig"))
    client.put_nanopublication(nanopublication)
