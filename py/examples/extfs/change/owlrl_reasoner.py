"""
An example change observer. This will be invoked on any change to the store, such as deleting or creating a nanopublication.
"""

import logging

import rdflib

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
    print(len(assertions_graph), "assertions at start")
    expanded_assertions_graph = rdflib.Graph()
    for assertion in assertions_graph:
        expanded_assertions_graph.add(assertion)

    reasoner = owlrl.DeductiveClosure(owlrl.OWLRL_Extension)
    reasoner.expand(expanded_assertions_graph)
    print(len(expanded_assertions_graph), "assertions in expanded graph")

    new_assertions = expanded_assertions_graph - assertions_graph
    print(len(new_assertions), "new assertions")
    # for assertion in new_assertions:
    #     print(assertion)


if __name__ == "__main__":
    main()
