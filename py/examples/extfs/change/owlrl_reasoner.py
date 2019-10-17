"""
An example change observer. This will be invoked on any change to the store, such as deleting or creating a nanopublication.
"""

import logging

import rdflib

from twks.ext import ExtensionArgumentParser
from twks.nanopub import Nanopublication


def main():
    try:
        import owlrl
    except ImportError:
        logging.warn("owlrl library not available, skipping")
        return

    args = ExtensionArgumentParser().parse_args()
    client = args.client

    assertions_graph = client.get_assertions()
    logging.info("owlrl_reasoner: {} assertions at start", len(assertions_graph))
    expanded_assertions_graph = rdflib.Graph()
    for assertion in assertions_graph:
        expanded_assertions_graph.add(assertion)

    reasoner = owlrl.DeductiveClosure(owlrl.OWLRL_Extension, rdfs_closure=False, axiomatic_triples=False,
                                      datatype_axioms=False)
    reasoner.expand(expanded_assertions_graph)
    logging.info("owlrl_reasoner: {} assertions in expanded graph", len(expanded_assertions_graph))

    new_assertions = expanded_assertions_graph - assertions_graph
    logging.info("owlrl_reasoner: {} new assertions in expanded graph", len(new_assertions))
    if not new_assertions:
        return

    new_nanopublication = Nanopublication.from_assertions(new_assertions)
    logging.info("owlrl_reasoner: new nanopublication: {}", new_nanopublication)
    client.put_nanopublication(new_nanopublication)


if __name__ == "__main__":
    main()
