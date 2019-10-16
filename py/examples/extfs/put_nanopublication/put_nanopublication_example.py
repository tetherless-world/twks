"""
Python example observer.

This file is not marked executable, and so it is ignored by the loader.
The companion shell script is executable. It is needed to set up the virtual environment.
"""

from twks.ext import ExtensionArgumentParser

args = ExtensionArgumentParser().parse_args()
client = args.client

nanopublication = client.get_nanopublication(args.nanopublication_uri)
if nanopublication is not None:
    print(nanopublication)
