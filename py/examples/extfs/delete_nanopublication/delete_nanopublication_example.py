#!/usr/bin/env python3

from twks.ext import ExtensionArgumentParser

args = ExtensionArgumentParser().parse_args()
client = args.client

nanopublication = client.get_nanopublication(args.nanopublication_uri)
print(nanopublication)
