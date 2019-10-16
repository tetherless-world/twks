from argparse import ArgumentParser


class ExtensionArgumentParser(ArgumentParser):
    """
    ArgumentParser subclass that accepts common extension command line arguments, such as the server base URL.
    """

    def __init__(self, *args, **kwds):
        ArgumentParser.__init__(self, *args, **kwds)
        self.add_argument("--nanopublication-uri", required=True)
        self.add_argument("--server-base-url", required=True)

    def parse_args(self, *args, **kwds):
        args = ArgumentParser.parse_args(self, *args, **kwds)
        if args.server_base_url:
            from twks.client.twks_client import TwksClient
            args.client = TwksClient(base_url=args.server_base_url)
        return args
