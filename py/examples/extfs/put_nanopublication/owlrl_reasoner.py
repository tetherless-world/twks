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
    # Don't care which nanopublication changed

    assertions_graph = client.get_assertions()


if __name__ == "__main__":
    main()
