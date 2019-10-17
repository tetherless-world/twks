from twks.ext import ExtensionArgumentParser

if __name__ == "__main__":
    # Parse standard arguments passed to the extension
    args = ExtensionArgumentParser().parse_args()
    # Pull out a TwksClient, which was configured from those arguments
    client = args.client

    # Run operations on the client
    nanopublication = client.get_nanopublication(args.nanopublication_uri)
    if nanopublication is not None:
        print(nanopublication)
