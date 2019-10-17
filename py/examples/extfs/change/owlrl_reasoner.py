"""
An example change observer. This will be invoked on any change to the store, such as deleting or creating a nanopublication.
"""

import logging

from twks.ext import ExtensionArgumentParser


def main():
    try:
        import owlrl
    except ImportError:
        logging.warn("owlrl library not available, skipping")
        return

    args = ExtensionArgumentParser().parse_args()
    client = args.client

    assertions_graph = client.get_assertions()
    print(len(assertions_graph))


if __name__ == "__main__":
    main()
